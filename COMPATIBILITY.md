# Compatibility and validation

Status: **migration in progress; no validated Minecraft 26.3 build**.

The imported Cobblemon 1.8.2 release supports Minecraft 1.21.1. This port targets stable Java Edition 26.3, verified against Mojang's manifest on 2026-10-24. It is an independent Fabric client and dedicated-server project. NeoForge, Forge and Quilt ports are outside scope. See [the assessment](porting/ASSESSMENT.md) for evidence and blockers.

## Dependencies and API migration

Original pins are preserved in Git history. Target configuration and resolved versions are recorded in the version catalogue and [technical changelog](CHANGELOG.md). Java 25 is required by Minecraft 26.3. The migration must address unobfuscated game tooling, removed Fabric registries, feature and data-pack formats, rendering and SDL input, networking, codecs and world storage. A declared target version is not proof of compatibility.

## Executed checks

| Check | Result | Evidence |
| --- | --- | --- |
| GitHub identity and repository | Passed: IdeasInTheWorks, public repository, admin/push access | GitHub API identity and repository read-back |
| Source import | Passed: unmodified 1.8.2 release and ancestor history pushed | GitHub main at `643bf9068b51c829316429916373e5aa9864b8b9` |
| Upstream protection | Passed: upstream push URL DISABLED, default push remote origin | Local remote/config verification |
| JDK installation | Passed: portable checksum-verified JDK 21 and 25 | Workspace toolchain and baseline Java log |
| Baseline build/tests (1.21.1) | Stopped after build-logic compilation to focus on target; no baseline build/test pass | `porting/logs/baseline-build.txt` |
| Gradle 9.6 build-logic compilation | Passed after licence-plugin and Kotlin DSL fixes | `porting/logs/target-build-04.txt` |
| Target build/tests (26.3) | Failed: 54 missing access-widener targets; mod tests did not run | `porting/logs/target-build-04.txt`, `porting/access-widener-blockers.json` |
| Common Kotlin source compilation | Failed: S03 attempt 05 has 7,451 diagnostic lines across 1,005 files; 728 fewer lines (8.9%) and 57 fewer files than S02. Counts include cascading errors | `porting/logs/sprint-03-common-compile-05.txt`, `porting/sprint-03-compiler-summary.json` |
| Block entity registration migration | All 26 diagnostics in CobblemonBlockEntities.kt from attempt 05 are absent after constructor migration in attempt 06; runtime untested | Comparison in `porting/compiler-summary.json` |
| Fabric source-header checks | Passed in target build attempt 04 | `:fabric:checkLicenses` in build log |
| Package validator regression tests | 6 passed locally and on GitHub Actions; synthetic ZIP fixtures only | `porting/logs/package-validator-tests-02.txt` |
| S02 offline tooling regression tests | 20 passed: 14 lease/state tests plus six package fixtures; no gameplay tests | `porting/logs/sprint-02-tool-tests-03.txt` |
| S02 real target API contracts | Four signatures compile against verified Minecraft 26.3; four identifier wire/round-trip checks execute successfully using actual game/Netty libraries | `porting/logs/sprint-02-api-contracts.json` |
| S02 live claim contention | Two simultaneous contenders; one acquisition, one refusal and one observed remote fast-forward conflict; test lease released | `porting/logs/sprint-02-live-claim-race.json` |
| S02 access widener and common source headers | Widener validation still fails on 54 targets; common main/test licence-header checks pass | `porting/logs/sprint-02-validation-01.txt`, `porting/sprint-02-validation-summary.json` |
| S03 registration construction | Keyed central property factories and 43 custom item constructors; all 140 item and 340 block ordered create-ID expressions are unchanged | `porting/sprint-03-registration-migration.json` |
| S03 focused migration fixtures | 48 checks passed with actual production helpers and real target classes; inventory cases use minimal fixture components; no game launch | `porting/logs/sprint-03-data-contracts.json` |
| S03 persistence callbacks | 26 save/load callbacks on 13 block entities migrated to ValueInput/ValueOutput; root fields, superclass data and native container serialization retained | `porting/sprint-03-nbt-migration.json` |
| S03 offline tooling | 20 tests passed; package/claim tooling only | `porting/logs/sprint-03-tool-tests-01.txt` |
| S03 access widener and source headers | AW gate still fails on 54 targets; common main/test licence checks pass after correcting six new-file headers to the project template with separate 2026 attribution | `porting/logs/sprint-03-validation-02.txt`, `porting/sprint-03-validation-summary.json` |
| S03 JUnit regression wrapper | Added to common tests; not executed because common compilation fails. The same fixture executed through the standalone runner | `common/src/test/java/com/cobblemon/mod/common/porting/` |
| GitHub Actions | Executed; latest checked run reproduces access-widener failure on Linux; package upload skipped, diagnostics uploaded | [Run 40130623175](https://github.com/IdeasInTheWorks/cobblemon-fabric/actions/runs/40130623175) |
| JAR validation | Not executed; no target JAR | Pending |
| Client launch/world load | Not executed | Pending |
| Dedicated server start/restart | Not executed | Pending |
| Multiple clients, PvP, packets, reconnect | Not executed | Pending |
| Spawning/biomes/rarity/time | Not executed | Pending |
| Capture/ownership/animations | Not executed | Pending |
| Showdown battles/moves/abilities/rewards | Not executed | Pending |
| Progression/evolution/EV/IV/nature/friendship | Not executed | Pending |
| Party/PC/transfers/persistence | Not executed | Pending |
| Models/sounds/items/blocks/recipes/riding | Not executed | Pending |
| Addons/data packs/resource packs | Not executed | Pending |
| World/player migration | Not executed | Pending |

## World and player data

No migration has been validated. Do not open a production world using this development branch. Before future testing, make complete backups of the world, player data, Cobblemon party/PC files or database, configuration, datapacks and resource packs while the original server is stopped. Restore backups into an isolated test directory and keep an untouched original copy. Include at least two players, owned Pokémon, parties, occupied PC boxes, stored items, progression, custom species and addon content in migration fixtures.

Minecraft 26.x changes vanilla world storage paths, and 26.3 changes feature registration and data formats. Preserve namespaced Cobblemon identifiers, ownership UUIDs, moves, abilities and progression fields; source serializers require inspection and fixture tests before any migration is claimed. External database persistence needs an isolated test database. Downgrading a world after newer Minecraft has saved it is not a recovery strategy; restore the complete backup instead.

Release gates require client and dedicated server launches, two simultaneous players, wild/PvP battles, capture, ownership, PC and party sync, disconnect/reconnect, restart/reload and log review. Every unexecuted test remains an outstanding issue.

## Current technical blockers

S03 leaves R013/R015 open. R013 still needs Pomohana's removed PinkPetalsBlock superclass port and full registry/startup validation; its six remaining central item diagnostics concern food/consumable APIs tracked in R014. R015 still needs the fossil multiblock's registry-aware writer, remaining entity save/load APIs, ItemStack codecs and local holder lookups, and broader legacy-data fixtures. The root NBT adapter preserves keys and tested primitive/UUID/list behavior; this does not establish full 1.21.1 world compatibility. Addons constructing the 43 updated item classes must now supply keyed Item.Properties as the first argument. No gameplay implementation or asset licences were removed.

The target build resolves the project configuration, processes resources and dependencies, and runs the retained access-widener validator. It reports 54 obsolete targets, including RenderStateShard subclasses, RenderType.CompositeState, shoulder-entity methods, particle registration, item colour APIs, texture-atlas access, block codecs and humanoid animation methods. These must be reconciled with their callers and replacement APIs; removing the validator or widening metadata compatibility is not a fix.

An isolated common-module compile is being used to collect the remaining source API diagnostics without changing release validation. Additional known work includes removed CompostingChanceRegistry usage, unified world-generation features, the Identifier rename's downstream method/codec changes, SDL input, render extraction and pipeline APIs, networking, persistence and integration compatibility. All gameplay implementation and assets remain in source. No functioning gameplay port or production data migration is claimed.

The initial validator test invocation hit the sandbox's temporary-directory permissions and failed before creating fixtures. Rerunning with a writable workspace temporary directory executed all six tests successfully. Both logs are retained to distinguish environment failures from executed test results.

Licence inventory: all 531 tracked licence/notice files remain preserved. Their bytes comprise two distinct texts: the root MPL-2.0 and 530 identical model-directory Creative Commons notices. See `porting/license-inventory.json`. This inventory does not establish rights clearance for every bundled dependency or trademark; binary redistribution review is still a release gate.
