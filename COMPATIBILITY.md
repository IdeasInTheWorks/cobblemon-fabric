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
| Common Kotlin source compilation | Failed: latest attempt 06 has 10,586 diagnostic lines across 1,207 files, including cascading errors | `porting/logs/common-compile-06.txt`, `porting/compiler-summary.json` |
| Block entity registration migration | All 26 diagnostics in CobblemonBlockEntities.kt from attempt 05 are absent after constructor migration in attempt 06; runtime untested | Comparison in `porting/compiler-summary.json` |
| Fabric source-header checks | Passed in target build attempt 04 | `:fabric:checkLicenses` in build log |
| Package validator regression tests | 6 passed locally and on GitHub Actions; synthetic ZIP fixtures only | `porting/logs/package-validator-tests-02.txt` |
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

The target build resolves the project configuration, processes resources and dependencies, and runs the retained access-widener validator. It reports 54 obsolete targets, including RenderStateShard subclasses, RenderType.CompositeState, shoulder-entity methods, particle registration, item colour APIs, texture-atlas access, block codecs and humanoid animation methods. These must be reconciled with their callers and replacement APIs; removing the validator or widening metadata compatibility is not a fix.

An isolated common-module compile is being used to collect the remaining source API diagnostics without changing release validation. Additional known work includes removed CompostingChanceRegistry usage, unified world-generation features, the Identifier rename's downstream method/codec changes, SDL input, render extraction and pipeline APIs, networking, persistence and integration compatibility. All gameplay implementation and assets remain in source. No functioning gameplay port or production data migration is claimed.

The initial validator test invocation hit the sandbox's temporary-directory permissions and failed before creating fixtures. Rerunning with a writable workspace temporary directory executed all six tests successfully. Both logs are retained to distinguish environment failures from executed test results.

Licence inventory: all 531 tracked licence/notice files remain preserved. Their bytes comprise two distinct texts: the root MPL-2.0 and 530 identical model-directory Creative Commons notices. See `porting/license-inventory.json`. This inventory does not establish rights clearance for every bundled dependency or trademark; binary redistribution review is still a release gate.
