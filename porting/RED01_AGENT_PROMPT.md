# Red01 agent prompt

Continue the Cobblemon Fabric port on Red01 as the integration coordinator. Do the next major implementation sprint, prioritizing the remaining build blockers and native renderer entrypoints. Implement, compile, debug, test, commit and push; do not stop at a plan.

Repository: https://github.com/IdeasInTheWorks/cobblemon-fabric
Integration branch: feature/latest-minecraft-fabric
Verified code checkpoint: 7da74a7f435cc9528eefbe89e73f3de8f8aae435 (S11). Fetch the latest branch, including the newer handoff documentation; never reset or force-push to this checkpoint if newer work exists.
Target: Minecraft Java 26.3, Java 25, Fabric client and dedicated server only. Retain Kotlin/Java, gameplay, assets, Showdown integration, licences and attribution.

Read AGENTS.md, porting/RED01_HANDOFF.md, porting/RESUME.md, COMPATIBILITY.md, BUILDING.md, and the S11 compiler/validation/source-audit JSON files before editing. Red01 setup is not yet verified. Use a clean checkout on Red01 outside OneDrive; do not assume the Windows checkout, ignored tools, JDK or Gradle caches were transferred. Resolve toolchain paths on Red01 and use the committed wrapper and pinned dependencies.

Verify GitHub authentication is IdeasInTheWorks. Origin must be IdeasInTheWorks/cobblemon-fabric; upstream must be https://gitlab.com/cable-mc/cobblemon.git with push URL DISABLED. Configure core.hooksPath=.githooks, remote.pushDefault=origin, push.default=current and cobblemon.claimPython to Red01's absolute Python 3 executable. If account or claim-registry access is missing, request access; do not use another account/repository or bypass the hook.

Before editing, read the live coordination/work-claims registry through scripts/work_claims.py. Acquire and verify a fresh, uniquely owned, scoped lease. Direct integration pushes additionally require resource integration:feature/latest-minecraft-fabric. Never reuse the closed S11 or documentation-handoff leases. The Sheet is only a snapshot; claims and source refs are not an atomic lock, and S10 observed delayed registry readback. Reconcile uncertain outcomes. If other agents are active, respect their scopes and use separate branches/worktrees; worker branches must be created through GitHub from a verified integration SHA. Do not spawn agents unless separately authorized. Heartbeat during work and immediately before long checks/pushes.

Historical S11 results, executed on 2026-10-25: common Kotlin fails with 4,556 diagnostic lines across 548 files; normal access-widener validation fails on 13 targets. There is no installable JAR. 2,723 focused core checks, 19 Fabric contract checks, 810 legacy navigation cases / 1,620 checks and 20 tooling tests pass. These are not game tests. Actual AtlasRegistry initialization in focused attempt03 fails because Fabric's accessor mixin is required; attempt04 tests descriptors and controlled reload ordering only. Full Java/mixins/JUnit, actual atlas stitching/reloading/GPU, client/server/multiplayer/gameplay and copied-world migration remain unverified.

First audit the 13 remaining widener targets with their callers, then migrate the remaining entity/player/block/special-item renderer callbacks and old Java mixins. ModelBufferSource is an immutable CPU-capture API, not a substitute native renderer callback. Preserve the fossil breaking overlay, pokemon_model special rendering, item locators, animation and texture lifetimes; profile snapshot allocations after actual startup. Do not remove features or validators simply to reduce errors. The handoff lists exact targets, source locations and known risks.

Reproduce the relevant baseline, make coherent fixes and add meaningful regressions. Use BUILDING.md and the handoff for normal Gradle and focused fixture commands. Resolve local jar/cache paths and verify game checksums from porting/version-evidence.json. Keep failed attempts and exact executed results under porting/logs/; distinguish source diagnostics, contract fixtures, actual game tests and package validation. Do not mark unexecuted functionality verified.

Update the same native Google Sheet, preserving human edits, formulas, native tables/charts and private sharing:
https://docs.google.com/spreadsheets/d/1_H2BdYlb3f3CnsmP99R2GqlGeuOwax2-3NvjWuVqOQE/edit
Update Roadmap, Dashboard, Sprint Log, Build History, Claims and Handoff. Do not create another tracker or Excel file. Accepted completion is currently 12/44 (27.3%), with 32 remaining (72.7%); partial migrations remain open. Historical remaining estimates are 518/1,036/2,080 engineering person-hours, Low confidence, not measured actuals. Re-estimate from evidence.

Push meaningful fast-forward commits only to origin after verifying the live lease and remote ancestry; inspect actual Actions results. Update RESUME, compatibility/change reports and machine-readable checkpoints, then close the lease with a durable published handoff. Release partial work; complete only the claimed deliverable actually finished. Preserve all source/logs if the sprint cannot finish. Never push upstream, overwrite others' work, commit credentials or touch production worlds. Publish a release only after compilation/package and required runtime validation; create the completed-work PR in this repository when working, and do not merge it automatically.


