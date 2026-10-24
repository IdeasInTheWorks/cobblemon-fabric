"""Cooperative work leases on a separate, append-only GitHub coordination branch.

No force updates: competing children of the same commit cannot both advance the ref.
Credentials stay in memory. Sheets is a human-facing view, never a lock service.
"""
from __future__ import annotations

import argparse
import base64
import copy
from datetime import datetime, timedelta, timezone
from email.utils import parsedate_to_datetime
import json
import os
from pathlib import Path
from pathlib import PurePosixPath
import re
import subprocess
import sys
import time
from urllib.error import HTTPError, URLError
from urllib.parse import quote
from urllib.request import Request, urlopen
import uuid

REPO = "IdeasInTheWorks/cobblemon-fabric"
BRANCH = "coordination/work-claims"
INTEGRATION = "feature/latest-minecraft-fabric"


class ClaimError(Exception):
    pass


class Conflict(ClaimError):
    pass


def timestamp(value):
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def utc(value):
    return value.astimezone(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z")


def scope(value):
    if "\\" in value or value.startswith("/") or ":" in value:
        raise ClaimError("Scopes must be relative POSIX repository paths")
    if any(p in ("", ".", "..") for p in value.split("/")):
        raise ClaimError("Empty, dot and parent path segments are not allowed")
    return str(PurePosixPath(value))


def overlaps(left, right):
    return left == right or left.startswith(right + "/") or right.startswith(left + "/")


def active(claim, now):
    return claim["status"] == "active" and timestamp(claim["expires_at"]) > now


def apply_operation(state, operation, now):
    """Pure state transition: testable with deterministic server time."""
    state = copy.deepcopy(state)
    claims = state["claims"]
    kind = operation["kind"]
    work_id = operation.get("work_id")
    if kind == "acquire":
        prior = claims.get(work_id)
        if prior and prior.get("operation_id") == operation["operation_id"]:
            if active(prior, now) and prior["owner"] == operation["owner"]:
                return state, prior
            raise Conflict("This acquire operation is expired, closed or owned elsewhere; use a fresh lease ID")
        scopes = sorted(set(scope(x) for x in operation["scopes"]))
        resources = sorted(set(operation["resources"]))
        if not scopes and not resources:
            raise ClaimError("At least one path scope or named resource is required")
        if not 5 <= operation["minutes"] <= 120:
            raise ClaimError("Lease duration must be between 5 and 120 minutes")
        for other in claims.values():
            if active(other, now) and (other["work_id"] == work_id or
                set(resources) & set(other["resources"]) or
                any(overlaps(a, b) for a in scopes for b in other["scopes"])):
                raise Conflict("Work overlaps active claim " + other["work_id"] + " owned by " + other["owner"])
        claim = {"work_id": work_id, "owner": operation["owner"],
                 "branch": operation["branch"], "scopes": scopes, "resources": resources,
                 "lease_id": operation["operation_id"], "operation_id": operation["operation_id"],
                 "epoch": (prior or {}).get("epoch", 0) + 1, "status": "active",
                 "acquired_at": utc(now), "heartbeat_at": utc(now),
                 "expires_at": utc(now + timedelta(minutes=operation["minutes"])),
                 "minutes": operation["minutes"], "checkpoint": ""}
        claims[work_id] = claim
    else:
        claim = claims.get(work_id)
        if not claim or claim["lease_id"] != operation["lease_id"] or claim["owner"] != operation["owner"]:
            raise Conflict("Lease was replaced or is not owned by this agent")
        if not active(claim, now):
            raise Conflict("Lease is expired or closed; reacquire and reconcile before continuing")
        if kind == "heartbeat":
            claim["heartbeat_at"] = utc(now)
            claim["expires_at"] = utc(now + timedelta(minutes=claim["minutes"]))
        elif kind in ("release", "complete"):
            if not operation.get("checkpoint"):
                raise ClaimError("Closing work requires a durable checkpoint/commit and handoff note")
            claim["status"] = "completed" if kind == "complete" else "released"
            claim["closed_at"] = utc(now)
            claim["checkpoint"] = operation["checkpoint"]
        else:
            raise ClaimError("Unknown operation")
    state["generation"] += 1
    state["events"].append({"generation": state["generation"], "at": utc(now),
        "kind": kind, "work_id": work_id, "owner": claim["owner"],
        "lease_id": claim["lease_id"], "epoch": claim["epoch"],
        "checkpoint": claim.get("checkpoint", "")})
    return state, claim


class GitHub:
    def __init__(self):
        env = dict(os.environ, GCM_INTERACTIVE="Never", GIT_TERMINAL_PROMPT="0")
        result = subprocess.run(["git", "credential", "fill"], input=
            "protocol=https\nhost=github.com\nusername=IdeasInTheWorks\n\n",
            text=True, capture_output=True, env=env)
        fields = dict(line.split("=", 1) for line in result.stdout.splitlines() if "=" in line)
        self.token = fields.get("password")
        if result.returncode or not self.token:
            raise ClaimError("Existing IdeasInTheWorks Git authentication is required; no credentials are logged")
        user, _ = self.request("GET", "/user", absolute=True)
        if user.get("login", "").lower() != "ideasintheworks":
            raise ClaimError("Authenticated account must be IdeasInTheWorks")

    def request(self, method, path, body=None, *, absolute=False):
        url = "https://api.github.com" + (path if absolute else "/repos/" + REPO + path)
        if method == "GET":
            # Ref state and the server clock must not come from an intermediary cache.
            url += ("&" if "?" in url else "?") + "_coord_read=" + uuid.uuid4().hex
        request = Request(url, data=None if body is None else json.dumps(body).encode(), method=method,
            headers={"Authorization": "Bearer " + self.token, "Accept": "application/vnd.github+json",
                     "Content-Type": "application/json", "X-GitHub-Api-Version": "2022-11-28",
                     "Cache-Control": "no-cache, no-store",
                     "User-Agent": "Cobblemon-Work-Claims"})
        try:
            with urlopen(request, timeout=30) as response:
                date = response.headers.get("Date")
                if not date:
                    raise ClaimError("Missing GitHub server clock; refusing lease decisions")
                return json.load(response), parsedate_to_datetime(date).astimezone(timezone.utc)
        except HTTPError as error:
            raise ClaimError("GitHub HTTP " + str(error.code)) from None
        except (URLError, TimeoutError):
            raise ClaimError("GitHub request outcome unavailable; re-read state before retrying") from None

    def read(self):
        ref, now = self.request("GET", "/git/ref/heads/" + quote(BRANCH, safe="/"))
        head = ref["object"]["sha"]
        content, now = self.request("GET", "/contents/state.json?ref=" + head)
        state = json.loads(base64.b64decode(content["content"]))
        if state.get("schema") != 1:
            raise ClaimError("Unknown claim registry schema")
        return head, state, now

    def initialize(self):
        try:
            return self.read()
        except ClaimError as error:
            if str(error) != "GitHub HTTP 404":
                raise
        state = {"schema": 1, "generation": 0, "claims": {}, "events": []}
        commit = self.make_commit(state, None, "coordination: initialize work lease registry")
        try:
            self.request("POST", "/git/refs", {"ref": "refs/heads/" + BRANCH, "sha": commit})
        except ClaimError:
            # A competing initializer may have won; never overwrite its registry.
            pass
        return self.read()

    def make_commit(self, state, parent, message):
        blob, _ = self.request("POST", "/git/blobs", {
            "content": json.dumps(state, indent=2) + "\n", "encoding": "utf-8"})
        tree, _ = self.request("POST", "/git/trees", {"tree": [
            {"path": "state.json", "mode": "100644", "type": "blob", "sha": blob["sha"]}]})
        commit, _ = self.request("POST", "/git/commits", {
            "message": message, "tree": tree["sha"], "parents": [parent] if parent else []})
        return commit["sha"]

    def transact(self, operation):
        for attempt in range(6):
            head, state, now = self.read()
            updated, claim = apply_operation(state, operation, now)
            if updated == state:
                return claim
            commit = self.make_commit(updated, head,
                "coordination: " + operation["kind"] + " " + operation["work_id"])
            try:
                self.request("PATCH", "/git/refs/heads/" + quote(BRANCH, safe="/"),
                             {"sha": commit, "force": False})
                return claim
            except ClaimError as error:
                # PATCH outcome may be unknown. Re-read; never force/rebase a stale state.
                new_head, new_state, _ = self.read()
                if new_head == commit:
                    return claim
                if str(error) not in ("GitHub HTTP 409", "GitHub HTTP 422"):
                    raise
                if new_head == head:
                    raise
                time.sleep(0.15 * (attempt + 1))
        raise Conflict("Registry busy; no lease was acquired, retry after reading current state")


def verify(state, owner, lease_id, branch, paths, now):
    claim = next((c for c in state["claims"].values() if c["lease_id"] == lease_id), None)
    if not claim or claim["owner"] != owner or not active(claim, now) or claim["branch"] != branch:
        raise Conflict("No current lease matches this owner, branch and lease ID")
    if branch == INTEGRATION and "integration:" + INTEGRATION not in claim["resources"]:
        raise Conflict("Shared integration branch requires the integration resource lease")
    for path in paths:
        if not any(path == p or path.startswith(p + "/") for p in claim["scopes"]):
            raise Conflict("Changed path is outside the lease: " + path)
    return claim


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=["init", "status", "acquire", "heartbeat", "release", "complete", "verify", "verify-push"])
    parser.add_argument("--work-id")
    parser.add_argument("--owner", default=os.environ.get("COBBLEMON_CLAIM_OWNER"))
    parser.add_argument("--lease-id", default=os.environ.get("COBBLEMON_LEASE_ID"))
    parser.add_argument("--branch", default=INTEGRATION)
    parser.add_argument("--scope", action="append", default=[])
    parser.add_argument("--resource", action="append", default=[])
    parser.add_argument("--minutes", type=int, default=30)
    parser.add_argument("--checkpoint")
    parser.add_argument("--save", action="store_true", help="Save the acquired lease in this worktree's ignored .work-lease.json")
    args = parser.parse_args(argv)
    if args.save and args.command != "acquire":
        parser.error("--save is only supported for acquire")
    try:
        api = GitHub()
        if args.command == "init":
            head, state, now = api.initialize()
            print(json.dumps({"branch": BRANCH, "head": head, "generation": state["generation"]}))
            return 0
        if args.command == "status":
            head, state, now = api.read()
            print(json.dumps({"head": head, "server_time": utc(now), **state}, indent=2))
            return 0
        if args.command == "verify-push" and Path(".work-lease.json").is_file():
            saved = json.loads(Path(".work-lease.json").read_text())
            args.owner = args.owner or saved["owner"]
            args.lease_id = args.lease_id or saved["lease_id"]
        if not args.owner:
            raise ClaimError("A unique agent/session owner label is required")
        if args.command in ("verify", "verify-push"):
            _, state, now = api.read()
            if args.command == "verify-push":
                for line in sys.stdin:
                    local_ref, local_sha, remote_ref, remote_sha = line.split()
                    if set(local_sha) == {"0"} or remote_ref == "refs/heads/main" or not remote_ref.startswith("refs/heads/"):
                        raise ClaimError("Branch deletion, main pushes and tag pushes require separate owner review")
                    branch = remote_ref.removeprefix("refs/heads/")
                    if set(remote_sha) == {"0"}:
                        raise ClaimError("Publish a new work branch with its reviewed base via GitHub API first")
                    ancestor = subprocess.run(["git", "merge-base", "--is-ancestor", remote_sha, local_sha], capture_output=True)
                    if ancestor.returncode:
                        raise ClaimError("Remote base unavailable or push is not fast-forward; fetch and reconcile")
                    changed = subprocess.check_output(["git", "diff", "--name-only", "--no-renames", remote_sha, local_sha], text=True).splitlines()
                    verify(state, args.owner, args.lease_id, branch, changed, now)
                print("Work lease verified for push")
            else:
                print(json.dumps(verify(state, args.owner, args.lease_id, args.branch, args.scope, now)))
            return 0
        if not args.work_id or not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._-]{0,79}", args.work_id):
            raise ClaimError("A stable work ID using letters, digits, dots, underscores and hyphens is required")
        operation = {"kind": args.command, "work_id": args.work_id, "owner": args.owner,
            "lease_id": args.lease_id, "operation_id": str(uuid.uuid4()), "branch": args.branch,
            "scopes": args.scope, "resources": args.resource, "minutes": args.minutes,
            "checkpoint": args.checkpoint}
        claim = api.transact(operation)
        if args.save:
            Path(".work-lease.json").write_text(json.dumps(claim, indent=2) + "\n")
        print(json.dumps(claim, indent=2))
        return 0
    except (ClaimError, ValueError, KeyError, OSError) as error:
        print("Work claim refused: " + str(error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
