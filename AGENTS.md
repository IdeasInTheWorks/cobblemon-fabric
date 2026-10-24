# Working on this independent Fabric port

## Scope and evidence

Work only in IdeasInTheWorks/cobblemon-fabric. GitLab upstream is fetch-only; its push URL must stay `DISABLED`. Never force-push, delete branches/history, merge the development PR, publish an unvalidated binary, or touch production server/world/database files. Preserve upstream licences, attribution, gameplay and assets.

Read `porting/RESUME.md`, `COMPATIBILITY.md`, the latest compiler summary and this file before editing. The target is pinned Minecraft 26.3 with Java 25. Compiler counts include cascading errors; source retention, a smaller count or passing tool tests do not prove gameplay. Keep all executed results and unexecuted tests explicit.

## Tracker and authoritative coordination

Human-facing roadmap/dashboard: https://docs.google.com/spreadsheets/d/1_H2BdYlb3f3CnsmP99R2GqlGeuOwax2-3NvjWuVqOQE/edit

Reuse that native Google Sheet; do not create duplicate trackers or Excel intermediates. Keep human edits, formulas, formatting and sharing intact. Update only relevant task/status/evidence cells, append sprint/build history, refresh the Claims snapshot, and verify formulas/readback. Estimates are engineering person-hours, not measured time or AI wall-clock forecasts. Re-estimate after each sprint using evidence.

The authoritative claim registry is the separate GitHub branch `coordination/work-claims`, `state.json`. The Sheet is a snapshot, not a mutex. Do not change registry JSON through Sheets or manually edit/merge it. Use `scripts/work_claims.py`: it commits each transition with one parent and advances the registry ref with `force=false`. A concurrent stale child is rejected and must re-read state. All claim events remain in Git history.

## Claim before editing

Use a unique owner label for each agent/session and a stable task/work ID. Read registry status and relevant source before choosing narrow file/directory scopes. Scopes overlap by directory boundaries, so `common/src` conflicts with its descendants but not `common/src2`. Dependencies and shared build files need their own scopes. Named resources are exclusive, too.

```sh
python scripts/work_claims.py status
python scripts/work_claims.py acquire --work-id R015 --owner agent-session-unique --branch work/r015-agent-session --scope common/src/main/kotlin/com/cobblemon/mod/common/api/storage --minutes 30 --save
```

Acquisition is successful only after the CLI reports success and the remote record matches the lease ID/owner. A refused/unknown request is not permission to start. Re-read after an uncertain outcome. Lease IDs are public fencing identifiers, not GitHub credentials.

For multiple agents, use a separate Git worktree and branch for each worker. Never share one mutable checkout, even for different tasks. The work branch must be created from a verified remote integration commit, using GitHub's create-branch API/tool with that exact SHA; do not replace an existing branch. Fetch it and attach/create your isolated worktree. Workers do not push directly to the shared integration branch.

The coordinator may use `feature/latest-minecraft-fabric` only with the named resource `integration:feature/latest-minecraft-fabric` plus scopes covering every file in the push. One coordinator integrates reviewed worker commits in order; no automatic merges. Do not hold broad scopes while idle. There is no instruction here to spawn agents automatically.

## Heartbeat, pause, takeover and push

```sh
python scripts/work_claims.py heartbeat --work-id R015 --owner agent-session-unique --lease-id LEASE_ID
```

Heartbeat at least every ten minutes for a 30-minute lease, and immediately before a long check or publication step. Lease durations are 5–120 minutes; server UTC time determines expiry. If the lease expires, is replaced, or verification fails, stop editing/pushing, preserve your own work, reacquire and reconcile current remote state. Never revive an expired lease by editing a timestamp. Do not delete another agent's uncommitted files. A takeover must inspect its durable handoff and remote branch first; expiry is not proof that no unpublished work exists.

Install the origin/claim pre-push gate in each worktree/clone:

```sh
git config core.hooksPath .githooks
git config cobblemon.claimPython /absolute/path/to/python3
git config remote.pushDefault origin
git config push.default current
git remote set-url --push upstream DISABLED
```

Use an absolute Python executable path; Windows paths can use forward slashes. The ignored `.work-lease.json` is local to each worktree and supplies owner/lease ID to the hook. Alternatively provide `COBBLEMON_CLAIM_OWNER` and `COBBLEMON_LEASE_ID` in that process. Do not commit the local lease file or credentials. The CLI uses existing Git Credential Manager authentication, verifies IdeasInTheWorks and keeps the credential in memory.

The hook refuses missing/stale/wrong-owner/wrong-branch leases, out-of-scope file changes, branch deletions, tag pushes, direct main pushes, and unprepared new remote branches. It verifies fast-forward ancestry. Fetch current origin first and push an explicit allowed branch. Do not bypass the hook to resolve a refusal.

These are cooperative safeguards, not GitHub server-side access controls or an atomic transaction spanning both claim and source refs. Use isolated worktrees, renew before pushes, verify after publication, and retain normal fast-forward checks. An agent that bypasses the protocol can still cause conflicts. Never claim a guarantee against malicious clients, machine crashes or a process paused after verification.

## Finish or hand off

1. Execute focused tests and the relevant target compilation/validation. Save logs and update machine-readable summaries.
2. Commit changes with a meaningful milestone message. Verify the lease and remote ancestry, push only to origin, then confirm the published SHA.
3. Update `porting/RESUME.md` and the existing Google Sheet with source changes, exact commands, actual results, remaining blockers, next task IDs and estimate confidence. Do not invent actual hours.
4. Close the lease with a durable commit/handoff reference. `complete` means the claimed scoped deliverable is done; it does not mean the entire port is ready. Use `release` for incomplete work and state its limitations.

```sh
python scripts/work_claims.py complete --work-id R015 --owner agent-session-unique --lease-id LEASE_ID --checkpoint "COMMIT_SHA; focused tests/result; next work R016; see porting/RESUME.md"
```

The CLI refuses closing without a checkpoint. Closed/expired/replaced lease IDs cannot verify a push. Refresh the Claims tab from live registry state after closing; a stale Sheet row never grants permission. Retain the same Sheet URL and coordination branch so future sessions can continue.
