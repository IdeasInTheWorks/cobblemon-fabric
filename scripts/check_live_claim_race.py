"""Explicit live GitHub race probe. Never run by offline unittest discovery."""
import json
from pathlib import Path
import threading
from concurrent.futures import ThreadPoolExecutor
import uuid
from work_claims import GitHub, ClaimError, Conflict


def main():
    api = GitHub()
    barrier = threading.Barrier(2)
    suffix = uuid.uuid4().hex[:12]
    work_id = "RACE-" + suffix

    class Racer(GitHub):
        def __init__(self):
            self.token = api.token
            self.first = True
            self.cas_conflicts = 0

        def read(self):
            result = super().read()
            if self.first:
                self.first = False
                barrier.wait(timeout=40)
            return result

        def request(self, method, path, body=None, **kwargs):
            try:
                return super().request(method, path, body, **kwargs)
            except ClaimError as error:
                if method == "PATCH" and str(error) in ("GitHub HTTP 409", "GitHub HTTP 422"):
                    self.cas_conflicts += 1
                raise

    def contender(owner):
        client = Racer()
        operation = {"kind": "acquire", "work_id": work_id, "owner": owner,
            "operation_id": str(uuid.uuid4()), "branch": "work/coordination-race-test",
            "scopes": ["__claim_test__/" + suffix], "resources": [], "minutes": 5}
        try:
            return {"result": "acquired", "claim": client.transact(operation), "cas_conflicts": client.cas_conflicts}
        except Conflict:
            return {"result": "refused", "cas_conflicts": client.cas_conflicts}

    with ThreadPoolExecutor(max_workers=2) as pool:
        results = list(pool.map(contender, ["race-agent-a", "race-agent-b"]))
    winners = [r for r in results if r["result"] == "acquired"]
    try:
        assert len(winners) == 1, "Exactly one simultaneous claimant must win"
        assert sum(r["cas_conflicts"] for r in results) >= 1, "Probe must exercise the remote conflict path"
        _, state, _ = api.read()
        assert state["claims"][work_id]["lease_id"] == winners[0]["claim"]["lease_id"]
        report = {"passed": True, "scope": "live remote claim collision only; no gameplay or mod build",
            "work_id": work_id, "contenders": 2, "winners": len(winners),
            "refusals": sum(r["result"] == "refused" for r in results),
            "remote_conflicts": sum(r["cas_conflicts"] for r in results)}
        print(json.dumps(report, indent=2))
        Path("porting/logs/sprint-02-live-claim-race.json").write_text(json.dumps(report, indent=2) + "\n")
    finally:
        for result in winners:
            claim = result["claim"]
            api.transact({"kind": "release", "work_id": work_id, "owner": claim["owner"],
                "lease_id": claim["lease_id"], "checkpoint": "Live simultaneous-acquire probe complete; no source files changed"})


if __name__ == "__main__":
    main()
