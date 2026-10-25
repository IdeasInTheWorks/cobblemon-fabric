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
| Common Kotlin source compilation | Failed: S09 attempt03 has5,804 diagnostic lines /889 files, down76 (1.3%) from S08; includes cascades | `porting/logs/sprint-09-common-compile-03.txt`, `porting/sprint-09-compiler-summary.json` |
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
| S04 fossil/entity persistence | Four multiblock and ten entity callbacks use target ValueInput/ValueOutput; fossil inventory/discs retain registry context; root field and superclass ordering retained | `porting/sprint-04-persistence-migration.json` |
| S04 focused regressions | 76 checks pass against actual production helpers and target classes, including item components, three-int positions, empty shoulder stacks and legacy brain UUID/expiry payloads; no game launch | `porting/logs/sprint-04-data-contracts-04.json` |
| S04 registration/access | Pomohana extends FlowerBedBlock with existing state names and shape; valid Output/TabVisibility wideners retain creative collectors (646 diagnostics become four component-related errors); central registration source files are unchanged | `porting/sprint-04-compiler-summary.json` |
| S04 offline tooling | 20 tests pass | `porting/logs/sprint-04-tool-tests-01.txt` |
| S04 access widener and headers | 54 old targets still fail; both added creative callback class targets exist; common main/test licence headers pass | `porting/sprint-04-validation-summary.json`, `porting/logs/sprint-04-validation-01.txt` |
| S05 scripted brains | Native Packed construction, dynamic-call adapters, live runtime-only memory/TTL snapshot, configured activities/conditions and final NPC brain return; no actual entity tick | `porting/sprint-05-brain-migration.json` |
| S05 items/components | 39 use/animation files, transformed-stack result semantics, food remainders, quick food, dip effects, drink sounds and model-data float channel; gameplay remains untested | `porting/sprint-05-item-use-migration.json`, `porting/sprint-05-food-migration.json` |
| S05 focused regressions | 127 assertions pass using production helpers/target classes; 20 offline tooling tests pass. Earlier fixture failures retained separately | `porting/logs/sprint-05-data-contracts-07.json`, `porting/logs/sprint-05-tool-tests-01.txt` |
| S05 widener/headers | 54 old targets still fail; main/test licence headers pass, including three new production helpers | `porting/sprint-05-validation-summary.json` |
| S06 ownership and synchronization | UUID property bridges native owner references; appended custom optional UUID serializer retains payload and field order. Native data-frame/legacy-owner tests pass; no connected clients | `porting/sprint-06-source-audit.json`, `porting/logs/sprint-06-data-contracts-04.json` |
| S06 movement and lifecycle | Typed control, memory bounds, nullable path contract, server-level damage/AI, NPC scale/profile APIs and inherited debug subscriptions; four principal files have zero diagnostics | `porting/sprint-06-compiler-summary.json`, `porting/logs/sprint-06-native-contracts.txt` |
| S06 interactions and regressions | Server shearing retains color IDs, count/offset/regrowth; dye component and sided swing results; active ridden travel retained;170 focused assertions and 20 tooling tests pass | `porting/sprint-06-source-audit.json`, `porting/logs/sprint-06-tool-tests-01.txt` |
| S06 widener and headers | AW54 remains; common/Fabric headers pass, including three new production Java files | `porting/sprint-06-validation-summary.json` |
| S07 shoulders/lifecycle | Native server storage and save keys retained; copied client NBT, relocated hooks, walking/XP/evolution APIs; principal Pokemon/delegate files zero diagnostics | `porting/sprint-07-source-audit.json` |
| S07 gamerules | Six typed rules retain defaults/categories; strict legacy adapter follows native converter;122 aliases in28 languages preserve old translations | `porting/sprint-07-language-aliases.json`, `porting/sprint-07-source-audit.json` |
| S07 focused regressions | 286 assertions pass twice using production Java helpers and actual target classes;20 tooling tests pass. No full Java/mixin/JUnit/game execution | `porting/logs/sprint-07-data-contracts-02.json`, `porting/logs/sprint-07-tool-tests-01.txt` |
| S07 widener/headers | Five shoulder targets migrated;49 remaining misses; common main/test and Fabric headers pass. First3 attempts stop in generated-cache preparation | `porting/sprint-07-validation-summary.json` |
| S08 AI/entity/hive | Native clocks, schedule contexts, task memories, targeting/combat, nullable random paths and typed hive snapshots/legacy-placeholder conversion. AI directories/generic/NPC delegate zero Kotlin diagnostics;14 Saccharine worldgen diagnostics remain | `porting/sprint-08-source-audit.json` |
| S08 focused checks |352 assertions plus810 actual old/random cases (1620 assertions) pass;20 tooling tests pass. Full Java/mixins/JUnit/gameplay unexecuted | `porting/logs/sprint-08-data-contracts-03.json` |
| S08 widener/headers |49 missing targets unchanged; common main/test and Fabric headers pass | `porting/sprint-08-validation-summary.json` |
| S09 capture/fishing/boats | Resolved owner references, typed server drops, dynamic boat drop supplier, native vehicle container/interaction callbacks and rod/bait metadata. All six migrated Kotlin files zero diagnostics; full Java/runtime untested | `porting/sprint-09-source-audit.json` |
| S09 focused checks |375 assertions (23 new owner/drop/container cases),810 actual old navigation cases/1620 checks and20 tooling tests pass. First fixture fails on private CODEC access; corrected public APIs pass | `porting/logs/sprint-09-data-contracts-02.json` |
| S09 widener/headers |49 misses unchanged; common main2997/test18 and Fabric22 header checks pass | `porting/sprint-09-validation-summary.json` |
| S03 JUnit regression wrapper | Added to common tests; not executed because common compilation fails. The same fixture executed through the standalone runner | `common/src/test/java/com/cobblemon/mod/common/porting/` |
| GitHub Actions (S05 baseline) | 20 tooling tests/Fabric headers pass; AW54 stops before common source diagnostics/JUnit, package skipped. Inspect Actions at S06 published head and current Sheet for new outcome | [Run40145668023](https://github.com/IdeasInTheWorks/cobblemon-fabric/actions/runs/40145668023) |
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

S09 advances R021 while keeping its original runtime acceptance open. Native owner references retain UUIDs through nullable lookup/unload/reload. Boat drop selection remains dynamic across normal/chest variants; native container callbacks retain27 slots and original save keys. The old BAMBOO passenger-height arithmetic is retained, as is the pre-existing boat fall guard. Native double-angle trig can differ in lookup precision; exact fishing trajectories are unverified. Full Java/mixin construction, capture/Showdown/ownership, bait/loot/reel/cancellation, riding/drops/inventory/destruction/reconnect and copied-world migration need actual game tests. No production data has been touched. See S09 evidence and the same native Sheet for actual CI, not an assumed pass.

S08 advances R019/R020 and hive R015 without closing original acceptance. Typed hive snapshots preserve root exclusions, nested ownership and nectar residence. Both historical placeholder IDs normalize before native registry decode; malformed properties are refused. This cannot restore fields already discarded by an earlier upgrade. Keep untouched backups and test copied worlds. Native helper fixtures use vanilla BEE and string typed-ID codecs; real Pokemon registration, full Java/mixin application, hive release/aggro, sleep, movement, combat and migration remain untested. Two existing navigation bugs (discarded scores and solid/fluid predicate results) are fixed deliberately; actual path safety still needs game tests. Broader Saccharine worldgen remains blocked. See S08 audit/logs and the native roadmap for latest CI/estimates.

S07 advances R019 and R016 while preserving their original acceptance rules. Principal Pokemon entity/delegate and gamerule definitions have zero diagnostics in this pass, but full construction, Java compilation and actual mixin application are untested. Native server shoulder saves remain authoritative and copied NBT restores client representation; matching mod builds on both sides are required. Real shoulder mounting/clearing, tracking, scanning, render extraction, recall, player restart and world migration need tests after a successful build. Native movement-emission hooks retain horizontal walking distance; live animation/AI/riding has not run. Six gamerules have new namespaced command IDs; old values convert only when the original data reaches the native old-world conversion. An earlier upgrade that discarded custom keys cannot be repaired from absent data. Keep untouched backups. R014 equipment, R013/R015 construction/legacy-data fixtures, remaining AI/generic entities, rendering/input, networking and AW49 remain major blockers. Original licences/assets and gameplay implementation remain retained; no gameplay or multiplayer success is claimed.

The target build resolves the project configuration, processes resources and dependencies, and runs the retained access-widener validator. S07 reports 49 remaining obsolete targets, including RenderStateShard subclasses, RenderType.CompositeState, particle registration, item colour APIs, texture-atlas access, block codecs and humanoid animation methods. These must be reconciled with their callers and replacement APIs; removing the validator or widening metadata compatibility is not a fix.

An isolated common-module compile is being used to collect the remaining source API diagnostics without changing release validation. Additional known work includes removed CompostingChanceRegistry usage, unified world-generation features, the Identifier rename's downstream method/codec changes, SDL input, render extraction and pipeline APIs, networking, persistence and integration compatibility. All gameplay implementation and assets remain in source. No functioning gameplay port or production data migration is claimed.

The initial validator test invocation hit the sandbox's temporary-directory permissions and failed before creating fixtures. Rerunning with a writable workspace temporary directory executed all six tests successfully. Both logs are retained to distinguish environment failures from executed test results.

Licence inventory: all 531 tracked licence/notice files remain preserved. Their bytes comprise two distinct texts: the root MPL-2.0 and 530 identical model-directory Creative Commons notices. See `porting/license-inventory.json`. This inventory does not establish rights clearance for every bundled dependency or trademark; binary redistribution review is still a release gate.
