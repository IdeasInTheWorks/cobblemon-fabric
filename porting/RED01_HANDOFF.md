# S13 Red01 implementation checkpoint (2026-10-28)

S13 source milestone `940ced7b1e30f0559d6ca937cd3a70fa63ae95ac` is published through the normal live-lease hook. Completed Actions40611669671 independently reproduces4,215/522; guards/AW pass, package validation/upload are skipped. Exact log/status are archived and linked from sprint-13-summary.json. The latest archive, not this source SHA alone, is the resume point.

Read the current S13 section in RESUME.md, RED01_AGENT_PROMPT.md, sprint-13-summary.json and compiler/validation/source audits first. The S12 and older handoffs below are historical. Native Pokémon/NPC bodies/labels, shoulder/recall layers, hand/reload/locator-debug mixins are migrated in source; final Kotlin still fails4,215/522. AW0/headers,2,818 core+19 Fabric contracts,810 legacy cases/1,620 checks and20 tooling tests pass. These are contracts, not actual game/mixin/GPU tests. No installable JAR.

Next: native held-item locator/front-light path (still failing), boats, habitat/lectern, GUI/deferred/ClientPlayerIcon and other mixins. Preserve fossil/OIT, pokemon_model and R013/R015 original data/runtime requirements. Accepted12/44 unchanged; whole remaining512/1,024/2,056 engineering person-hours, Low confidence, actuals unmeasured. Publish and release partial S13 only after a durable archive; verify live closure and update the same Sheet. Never reuse a closed lease.

# S12 Red01 published implementation checkpoint (2026-10-27)

Red01 now resolves locally to codex-gpu01 at 10.0.80.2. Java25/GitHub IdeasInTheWorks and a fresh S12 lease/integration resource were verified. Source milestone `137e6b67bd5bc516289da24559db78da3641b935` is published through the normal push hook. Read the S12 section in [RESUME.md](RESUME.md), sprint-12-summary.json, exact command ledger and current native Sheet first. The older documentation handoff below is historical.

Normal widener validation now passes (13 -> 0), as do source headers. Common Kotlin fails 4,297/527, independently reproduced by CI40590974021. Core2,788 + Fabric19 contracts,810 legacy cases/1,620 checks and20 tooling tests pass; none are game tests. CI fails compilation and skips packaging. No installable JAR. Several living/boat/block/locator/GUI and Java entrypoints remain; R013/R015 original data/serialization and runtime gates stay open. Accepted completion stays12/44; remaining514/1,028/2,064 engineering person-hours, Low confidence, actuals unmeasured.

Release the partial S12 claim only after the archive is published; verify closure in the registry and refresh the same Sheet. Future work needs a fresh lease; never reuse the S11, documentation or released S12 lease. Keep the integration resource requirement and all release safeguards.

# Red01 takeover — 27 October 2026

This documentation-only handoff preserves S11. No code or gameplay test was executed for this handoff. Red01 has not been provisioned or contacted; start a new Codex chat there using [RED01_AGENT_PROMPT.md](RED01_AGENT_PROMPT.md).

## Verified checkpoint

- Repository: https://github.com/IdeasInTheWorks/cobblemon-fabric
- Branch: `feature/latest-minecraft-fabric`
- Code checkpoint: `7da74a7f435cc9528eefbe89e73f3de8f8aae435`. Fetch latest, including this handoff; never reset newer work to this SHA.
- On 2026-10-27 the checkout was clean, origin matched that SHA, upstream push was DISABLED, and no implementation claims were active.
- S11 lease was released. The documentation lease HANDOFF-RED01-20261027 is separate and must never be reused. Read the live registry before any editing.
- Target remains Minecraft 26.3, Java 25, Fabric client and dedicated server. Preserve Kotlin/Java, gameplay, assets, licences and attribution.

## Read first

AGENTS.md, RESUME.md, ../BUILDING.md, ../COMPATIBILITY.md, sprint-11-summary.json, sprint-11-compiler-summary.json, sprint-11-validation-summary.json and source audits/logs. Find exact existing filenames with `rg --files porting`; historical evidence is not a fresh Red01 test.

## Safe Red01 setup

Use a clean checkout outside OneDrive. Inspect any existing checkout and unpublished work before changing it. The Windows tools, caches and ignored lease were not transferred.

For a new clone only:

```sh
git clone --branch feature/latest-minecraft-fabric https://github.com/IdeasInTheWorks/cobblemon-fabric.git
cd cobblemon-fabric
git remote add upstream https://gitlab.com/cable-mc/cobblemon.git
git remote set-url --push upstream DISABLED
git config core.hooksPath .githooks
git config remote.pushDefault origin
git config push.default current
git config cobblemon.claimPython "$(command -v python3)"
git remote -v
python3 scripts/work_claims.py status
```

Verify GitHub account IdeasInTheWorks and resolve Python to an absolute executable. Install Java 25 and use the committed Gradle wrapper/pinned dependencies (Gradle 9.6.0, Kotlin 2.4.21, Architectury Loom 1.17.493). Resolve paths on Red01. Target client SHA1 e877b6a07acd633fb3bb475002175cec036e7b87; use official URLs/checksums in version-evidence.json. Never commit game jars, tools, caches, credentials or local leases.

Acquire a fresh unique scoped claim with `scripts/work_claims.py acquire` and verify its remote owner/lease. Integration pushes require resource `integration:feature/latest-minecraft-fabric` and all changed file scopes. Use CLI help for exact arguments. Heartbeat at least every ten minutes and before long checks/pushes. The Sheet is a snapshot. Claims and source pushes are cooperative, separate transactions; S10 observed delayed registry readback. Reconcile uncertain outcomes instead of assuming ownership. If workers exist, use distinct branches/worktrees and follow AGENTS.md branch-creation protocol. No authorization to spawn agents is implied by this handoff.

## Prioritized sprint

1. Reproduce the baseline and inspect the remaining 13 access-widener misses with their callers.
2. Migrate native entity/player/block/special-item renderer callbacks and old Java mixins, retaining animation, item locators and texture lifetimes.
3. Preserve fossil breaking overlays and pokemon_model special rendering. ModelBufferSource captures immutable CPU vertices; it is not a native callback replacement.
4. Resolve startup, actual atlas registration/stitch/reload and GPU lifetimes only after compilation permits runtime testing. Profile CPU snapshot allocations.
5. Keep R013/R015 copied-data, Showdown, networking and actual multiplayer/gameplay acceptance open until executed.

Remaining widener targets (exact descriptors in validation summary): BlockBehaviour.codec; Inventory.compartments; LevelRenderer.calculateParticleLevel and cullingFrustum; ParticleEngine.register and textureAtlas; VillagerTrades.ItemsForEmeralds; Font.StringRenderOutput; RecipeBookSettings.states; Block.spawnDestroyParticles; HumanoidModel.setupAttackAnimation, poseRightArm and poseLeftArm.

Source starting points: cobblemon-common.accesswidener; RestorationTankRenderer.kt; PokemonItemRenderer.kt; MountedPlayerRenderer.kt; SnowstormParticle.kt; ParticleMaterials.kt; PlayerInventoryExtensions.kt; CobblemonTradeOffers.kt; StringRenderOutputMixin.java; BuiltinModelItemRendererMixin.java. Locate with rg. Native helpers are in common/.../client/model and fabric/.../client/model. Do not remove features, assets or validators to reduce diagnostics.

## Historical evidence and limits

S11 on 2026-10-25: common Kotlin failed with 4,556 diagnostic lines across 548 files; normal access-widener validation failed on 13 targets. No installable JAR. 2,723 focused core checks, 19 Fabric contracts, 810 legacy navigation cases / 1,620 checks and 20 tooling tests passed. These are fixtures/tool tests, not gameplay.

Focused attempt03 failed actual AtlasRegistry initialization because Fabric's accessor mixin is required. Attempt04 validates descriptors and controlled callback ordering only. Full Java/mixins/JUnit, actual atlas stitching/reload/GPU, client/server/multiplayer/gameplay and copied-world migration remain unverified.

CI: https://github.com/IdeasInTheWorks/cobblemon-fabric/actions/runs/40214138751 (failure). Artifact 12606597889 contains diagnostics, not a mod; expiry recorded as 2026-11-24T16:58:30Z. Durable logs remain in porting/logs.

## Execute and record

```sh
python3 -m unittest discover -s scripts -p 'test_*.py' -v
bash ./gradlew :fabric:validateAccessWidener :common:checkLicenses :fabric:checkLicenses --continue --stacktrace --console=plain
bash ./gradlew :common:compileKotlin -Pkotlin.incremental=false -Pkotlin.compiler.execution.strategy=in-process --stacktrace --console=plain
bash ./gradlew :common:compileKotlin :common:compileJava :common:test :fabric:build --stacktrace --console=plain
```

Use BUILDING.md for focused `scripts/check_data_contracts.py` commands. Supply verified target game jar, pinned Fabric API aggregate, resolved Gradle cache and JDK25 javac; optional legacy game jar and official metadata must be supplied together. Keep failed attempts, full logs, exact commands and reports. Run normal gates without disabling validators. The OneDrive build-output workaround is documented but may be unnecessary on a fresh Red01 checkout.

## Tracker and publication

Reuse the existing private native Sheet; preserve sharing, formulas, charts and human edits:
https://docs.google.com/spreadsheets/d/1_H2BdYlb3f3CnsmP99R2GqlGeuOwax2-3NvjWuVqOQE/edit

Update Roadmap, Dashboard, Sprint Log, Build History, Claims and Handoff after actual implementation. Accepted completion remains 12/44 (27.3%); 32/44 (72.7%) remain. Historical remaining estimates 518/1,036/2,080 engineering person-hours, Low confidence; actuals unmeasured. This handoff changes none of these.

Full copyable native Google Doc:
https://docs.google.com/document/d/1PZvGYMJDkSd7qwaF90AyzBFDRwRX5EA3bkMvUIsk6G8/edit

Push meaningful fast-forward commits to origin only after a fresh lease/ancestry check. Inspect actual Actions results. Update RESUME and machine-readable reports, close the claim with a published checkpoint, and verify closure. Release partial work rather than claiming unfinished deliverables complete. No validated release or completed-work PR exists. Do not merge automatically, force-push, push upstream, publish unvalidated binaries or touch production worlds.

