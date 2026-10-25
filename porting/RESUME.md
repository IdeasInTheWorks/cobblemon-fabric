# Development checkpoint

This is an incomplete migration, not an installable mod. Work is on `feature/latest-minecraft-fabric`; GitHub `main` remains the original 1.8.2 release. No release or completed-work pull request has been created because build/gameplay release gates have not passed.

## Sprint S05 checkpoint (2026-10-25)

Partial work: R014/R019/R020, work ID `S05-R014-R019-R020`, owner `codex-sprint05-01a16f8a`, exclusive integration resource. This is a historical handoff, not authority: read the live registry and acquire a new scoped lease before editing after closure. Release this lease only after its durable checkpoint is pushed. The native Sheet remains the primary roadmap and has the current CI and closure snapshot.

Implemented food remainders and separate consumables, quick-food duration, dip absorption, tea drinking metadata, model-data float payloads, and modern item results across 39 use/animation files. Success preserves transformed held stacks and legacy sided swing semantics; audited pass/fail callers retain their incoming stack. Aprijuice/PokePuff retain custom conditional nutrition and consume once; they return the handled stack rather than invoking native consumption twice. Source create-ID and central nutrition/saturation expression ordering is unchanged. Model assets and real eating are not validated.

Native `makeBrain(Brain.Packed)` now constructs configured Pokemon/NPC brains. Live reconfiguration snapshots current values, expiry and runtime-only memories with Brain.Visitor instead of replaying stale initial data or using disk-filtered pack. Native disk saves stay unchanged. Sensor-required memories, original task weights/conditions and manual core/default activities are retained; unset environmental schedule matches the previous empty timetable. NPC returns its final configured brain to vanilla. Legacy Dynamic callers retain registry-aware adapters; addon implementors need the new Packed method. Scripted VariableStruct data/config roots explicitly fail if unexpectedly null; supported child serialization is unchanged.

Executed results:

- Four source compiler runs: attempts 04/05 each 6,442 lines /965 files, attempt 06 6,422 /960, then attempt 07 **6,420 /959**, down 313 (4.6%) from S04. Compilation still fails. Counts include cascades, not independent defects or a completion percentage. Attempts 01-03 failed before source diagnostics (locked generated caches, malformed PowerShell property argument, locked build-logic output); all logs retained.
- **127 focused assertions pass**, compiling production ScriptedBrain, ItemUseResults, CobblemonConsumables and prior helpers against the SHA-1-verified official target JAR and matching libraries. Includes runtime-only memory identity/expiry, sensor memories, activity transition/conditions, strict malformed brain refusal, food/drink metadata/codecs and held-stack/swing results. Minimal missing components are supplied only for two inventory fixture items. Earlier activity/assertion and tea fixture type failures are retained, not counted as passes.
- **20 Python tooling tests pass**; the common JUnit wrapper has not run because common compilation fails. No Minecraft client/server, gameplay, multiplayer or world-migration processes executed; no candidate JAR or validated release.
- Final `:fabric:validateAccessWidener :common:checkLicenses --continue` still reports **54 missing targets**. Common main/test licence headers pass. Retain the validator and reconcile it with migrated callers.

Source milestones: `1d3933d97ae9543f39a0c77f440d7e26014b3ac8` (brains) and `b08693a76c5026598570eaa490f7e4cd69d957fa` (items). The final checkpoint archives tests, evidence and this handoff.

Logs and machine summaries: `sprint-05-compiler-summary.json`, `sprint-05-validation-summary.json`, `sprint-05-summary.json`, food/item-use/brain migration audits and `logs/sprint-05-*`. Inspect Actions at the final published head; actual CI outcome is in the same Sheet and Actions diagnostics, not an assumed pass.

Remaining planning ranges (low/likely/high engineering person-hours, low confidence): R014 **16/32/64** (original 20/40/80), R019 **28/56/112** (original 32/64/128), R020 **22/44/88** (original 24/48/96). Actual hours unmeasured. Original acceptance gates stay open. R013 remains 6/12/24 and R015 8/16/32.

Next: finish remaining item/equipment contracts (R014), entity lifecycle/synchronized data (R019) and movement/navigation/AI APIs (R020), then validate real construction and legacy-data fixtures (R013/R015). Coordinate item model assets with R022 and boat/projectile changes with R021. Keep ownership, memories, tasks, gameplay and assets. Rendering/input, networking and AW54 remain release blockers.

For local locked generated outputs, use the quoted nonincremental/in-process command in BUILDING.md. Old common/build and build-logic/build were preserved under ignored `build/sprint-05-cache-backups/` after checking absolute paths and stopping compilers. Do not delete another worker's files or production data. Source scopes and shared integration must be leased; parallel workers need isolated branches/worktrees.

## Sprint S04 checkpoint (historical, 2026-10-24/25)

Work is claimed as `S04-R013-R015`, owner `codex-sprint04-01a16f8a`, with the exclusive integration resource. Always re-read the live registry before resuming; this paragraph is a historical handoff, not a lock.

The fossil multiblock now writes into `ValueOutput`, including nested structure data and registry-aware item lists. Monitor discs use the save/load context supplied by Minecraft. The original `writeToNbt(provider)` caller API remains as a context-aware adapter that throws if serialization reports errors rather than returning a partial save. Implementors of `MultiblockStructure` must implement `write(ValueOutput)` now. Existing field names, owner UUIDs, progress/protection values, created-Pokémon migration flag and previous animation age/partial ticks are retained.

Pokémon and NPC root save/load overrides use the target ValueInput/ValueOutput APIs, retaining their original superclass ordering, scripting, party/battle data, tethering, effects, event hooks and synchronized fields. Generic entity and boat callbacks are migrated; chest boats use native context-aware inventory callbacks, and boats call the superclass so target leash data is retained. Removed block-position helpers are replaced with the exact legacy three-int array format, verified against the original 1.21.1 bytecode. Pomohana now extends FlowerBedBlock; the target still uses `flower_amount` and horizontal `facing`, and its custom collision shape is retained.

Executed S04 results:

- Two source compilations failed: 7,396 lines / 1,002 files (attempt 02), then **6,733 / 998** (attempt 03). This is 718 fewer diagnostic lines (9.6%) and seven fewer affected files than S03. Counts include cascades, not independent bugs or gameplay completion. Attempt 01 failed before compilation because the restricted process could not open local daemon sockets; all logs are retained.
- **76 focused checks passed** through the shared standalone/JUnit fixture source. New cases cover nested item components/ownership/progress, empty inventories, malformed positions, strict partial-save refusal, optional empty shoulder items and the legacy brain-memory UUID/expiry format. These execute production helpers and target classes, not full fossil/Pokemon/NPC gameplay. The two failed fixture reports (Java lambda naming and collection-versus-set comparison in a property assertion) are retained separately from the successful 67- and 76-check runs.
- **20 tooling tests passed**; no Minecraft game processes were launched. The JUnit wrapper remains blocked by common compilation.
- `:fabric:validateAccessWidener :common:checkLicenses --continue` still fails on **54 obsolete widener targets**; the two new creative callback class targets are found. Common main/test licence headers pass. See `sprint-04-validation-summary.json` and the full log. The validator remains enabled.
- CreativeModeTab.Output and TabVisibility became protected. Two valid class wideners retain existing creative collectors without suppressing the validator. That file's diagnostics fall from 646 to four CustomModelData component errors, assigned to R014. Pomohana and MultiblockEntity now have zero diagnostics in the attempted common compile; full startup remains untested.

Source milestones: `0f4a36525000f5a2c87fd93f919650b0289f0828` (persistence/fixtures) and `f6a9c932e49abb52dea1c585955fefa4c852718a` (registration/access/value lookups). The final checkpoint also archives logs, summaries and this handoff. Inspect the latest Actions run at the published development head; CI results are retained as workflow artifacts and in the existing native Sheet. Close the current lease only after confirming that head was pushed.

Remaining planning effort: R013 **6/12/24** and R015 **8/16/32** engineering person-hours (low/likely/high, low confidence). Previous remaining ranges were 8/16/32 and 12/24/48; original plans were 16/32/64 and 20/40/80. Actual person-hours are unmeasured. Both tasks retain their original acceptance gates and stay open. The native Sheet is the primary roadmap.

Next: continue the registry/serialization audit and representative old-data fixtures (R015), validate construction/startup once compilation allows (R013), then consumable/CustomModelData APIs (R014). Coordinate entity brain construction/provider/activity changes with R019/R020: Brain.Packed's tested serialization format is supported, but old Dynamic-based brain construction remains unported. R021 contains remaining boat/projectile interaction and lifecycle APIs. Do not remove brain memories, activities, assets or gameplay to reduce diagnostics. Rendering/input and 54 previously observed obsolete widener targets remain substantial work. No installable JAR, release, completed-work PR, client/server or multiplayer success is claimed.

## Sprint S03 checkpoint (historical, 2026-10-24)

R013/R015 were claimed together as `S03-R013-R015` under owner `codex-sprint03-01a16f8a`, using an expiring remote lease and the exclusive integration resource. Read live registry state before resuming; a saved local lease or Sheet snapshot is not authority.

Implemented typed item/block keys before construction, fresh plant properties, explicit properties through 43 custom item classes and 128 central constructor calls, removed vanilla-helper replacements, item/block translation prefixes and a template wrapper retaining the custom upgrade heading. All 140 item and 340 block ordered create-ID expressions match the pre-sprint source. Original licences and gameplay assets remain intact.

R015 adds explicit legacy NBT defaults, strict four-int UUID preservation, typed-list checks, root ValueInput/ValueOutput merging and supported holder/value/registry lookups. Twenty-six callbacks across 13 block entities now use the target persistence interfaces; container fields use native context-aware ContainerHelper methods. The fossil multiblock writer and entity persistence still need work.

Executed results:

- Five `:common:compileKotlin` attempts: 7,822 / 7,580 / 7,539 / 7,458 / **7,451** diagnostic lines. Final result: **1,005 affected files**, down 728 lines (8.9%) and 57 files from S02. Counts include cascades, not independent bugs or gameplay completion. `sprint-03-compiler-summary.json` and all five full logs are preserved.
- `scripts/check_data_contracts.py`: **48 checks passed**, compiling the actual production Java helpers and executing the shared common-test fixture against the verified official game JAR. It tests registration metadata, NBT defaults/types/UUIDs, binary data, registry codecs, root merging and inventory slots/counts. Inventory cases initialize minimal components for two fixture items; vanilla datapack defaults and gameplay are not tested. Failed setup probes are retained separately.
- Python discovery: **20 tooling tests passed**. No game runtime tests executed.
- `:fabric:validateAccessWidener :common:checkLicenses --continue`: 54 AW targets still fail; common main/test licence checks pass. The first header attempt failed on six new-file years; project headers plus separate 2026 attribution fixed that failure. Both validation logs are retained.
- A JUnit entry point now invokes that same fixture during common tests, including existing CI once compilation is fixed. The JUnit wrapper itself has not executed.

R013 and R015 remain open for their original acceptance gates. Planning remainder: R013 **8/16/32** and R015 **12/24/48** engineering person-hours (low/likely/high, low confidence). Original estimates were 16/32/64 and 20/40/80; actual person-hours were not measured. The same Google Sheet is the primary roadmap; no sharing changes or duplicate tracker.

Next: finish the fossil multiblock's context-aware serialization and remaining local holder/ItemStack lookups (R015), port Pomohana's superclass and validate all construction paths (R013), then food/consumable components (R014). Rendering/input, entity lifecycle and the retained access-widener gate remain substantial separate work. Do not publish a binary or claim world/client/server/gameplay compatibility.

## Sprint S02 checkpoint (historical, 2026-10-24)

Human-facing planning is maintained in the same native Google Sheet recorded in [tracker.json](tracker.json): Dashboard, Roadmap, Sprint Log, Build History, Claims, Handoff and Assumptions. It contains 44 work packages with low/likely/high engineering person-hour ranges. These are uncertain planning estimates, not actual time or AI execution forecasts. Actual person-hours remain unmeasured. Re-estimate after each sprint and preserve human edits/sharing.

Read root [AGENTS.md](../AGENTS.md) before editing. Use `scripts/work_claims.py status`, acquire narrow scopes on an isolated worker branch, heartbeat during work, verify before pushing, and close with a durable commit/handoff. The separate `coordination/work-claims` branch is the claim authority; Sheet rows are snapshots. Shared integration requires the exclusive `integration:feature/latest-minecraft-fabric` resource. The pre-push hook additionally requires `cobblemon.claimPython` to name an absolute Python executable. These safeguards are cooperative, not an atomic lock across source and coordination refs.

S02 implemented 37 verified exact type relocations, seven typed entity registration keys, 33 collision-method renames, 30 identifier reads and 29 writes, and entity-tag holder lookup. No gameplay features or asset licences were removed. Relocation evidence is in `sprint-02-api-relocations.json`.

Executed S02 checks:

- `:common:compileKotlin --stacktrace --console=plain`: failed with **8,179 diagnostic lines across 1,062 files**, down 2,407 lines (22.7%) and 145 files from attempt 06. Counts include cascades, not independent defects or a gameplay completion percentage. PokemonEntity.kt diagnostics fell from 646 to 162. See `sprint-02-compiler-summary.json` and `logs/sprint-02-common-compile-01.txt`.
- `:fabric:validateAccessWidener :common:checkLicenses --continue --stacktrace --console=plain`: 54 widener targets still fail; common main/test licence-header checks pass. See `sprint-02-validation-summary.json` and its full log. Do not bypass this gate.
- `python -m unittest discover -s scripts -p 'test_*.py' -v`: **20 passed** (14 claim-protocol tests, six synthetic package fixtures). `logs/sprint-02-tool-tests-03.txt`.
- `scripts/check_api_contracts.py`: four source contracts compile against the SHA-1-verified target game JAR and exact matching cached libraries; four identifier round-trip/wire-format cases pass against real game/Netty classes. No Minecraft process launched. `logs/sprint-02-api-contracts.json`.
- `scripts/check_live_claim_race.py`: two simultaneous remote claim contenders, one winner, one refusal, one remote fast-forward conflict; temporary lease released. `logs/sprint-02-live-claim-race.json`.
- Live CLI/push-guard probes: active in-scope lease accepted; out-of-scope path, wrong owner and stale lease refused. No source refs changed by probes. `logs/sprint-02-live-push-guard.json`.

Full common/Fabric compilation, JAR packaging, Minecraft startup, client/server, multiplayer, Showdown and gameplay/data migration are still unvalidated. No installation-ready artifact is available.

## Completed work

- Verified IdeasInTheWorks through both connected GitHub and existing local Git authentication. Created the requested public repository, imported unmodified Cobblemon 1.8.2 and its ancestor history, and pushed incremental development commits.
- Preserved the full upstream clone and configured upstream fetch-only (`DISABLED` push URL), origin-only push defaults and a tested pre-push guard.
- Verified Minecraft 26.3 as the stable target and Java 25 as its runtime requirement. Saved a version/dependency evidence snapshot.
- Installed checksum-verified portable JDK 21 and 25 in the parent workspace's `tools/` directory; these are not committed or required at those absolute paths on other machines.
- Migrated build configuration to Gradle 9.6.0, Kotlin 2.4.21, Architectury Loom 1.17.493 no-remap mode and common/Fabric-only settings. Removed upstream Maven publication destinations. Retained original NeoForge source files without building them.
- Switched to supported Shadow and licence plugins. Build logic and project configuration now compile/evaluate. Fabric source-header checks execute successfully.
- Replaced the ResourceLocation type with Identifier in common/Fabric source, changed the access-widener namespace to official and updated Fabric log stripping registration to the new block-transformer helper.
- Implemented GitHub Actions with read-only token permissions, pinned supported official Actions, Gradle cache, compilation/test commands, failure diagnostics and conditional candidate-package upload. Verified runs on Linux. No automatic publishing.
- Added package structure checks and six regression tests. All six passed locally after selecting a writable temp directory and on Actions; these use synthetic ZIP fixtures and do not test Minecraft.

## Observed blockers

`porting/logs/target-build-04.txt` fails the retained access-widener validator with **54 missing members/classes**. The same failure is reproduced by [Linux CI](https://github.com/IdeasInTheWorks/cobblemon-fabric/actions/runs/40130623175); its diagnostics artifact is linked from the run.

Standalone `:common:compileKotlin` attempt 05 executed and failed with **10,613 compiler diagnostic lines across 1,208 source files**. These include cascading errors, not 10,613 independent API changes. First failure is removed `BakedModel` / `ModelResourceLocation` in `BakingOverride.kt`; diagnostics also cover block-entity factories, entity APIs, codecs, GUI/rendering, items and world generation. See `compiler-summary.json` and the full log.

Attempt 06 after migrating BlockEntityType factories fails with **10,586 diagnostic lines across 1,207 files**. All 26 previous diagnostics in `CobblemonBlockEntities.kt` are eliminated. Factories and valid-block sets remain intact; runtime behavior remains untested. No complete common/Fabric build or existing gameplay test suite has passed.

An import-presence scan against the official client JAR identifies 111 distinct absent game-owned imports at 540 source locations. It excludes separately distributed Mojang libraries; it is supplementary evidence, not a complete compiler/API analysis. See `missing-game-imports.json`.

Adorn has no published 26.3 version in the listing checked. Its source integration is retained with its old compile-only dependency pending investigation. Do not silently drop it. Graal/Showdown and all gameplay remain unvalidated on Java 25.

## Next implementation work

1. Continue from S05 common attempt 07 and `sprint-05-compiler-summary.json`. Claim the next narrow R014/R019/R020 slice or remaining **R013/R015** audit; their acceptance gates are not complete. Inspect current file scopes and handoff before claiming. Preserve factories, IDs, data and gameplay. Resolve cascading errors by coherent API subsystem.
2. Port the rendering stack coherently: model baking, item model definitions, render-state extraction, GUI graphics extraction, renderer pipelines, particles, shaders and animation layers. `GuiGraphics`, `MultiBufferSource`, old RenderType and many RenderStateShard classes are gone or relocated. Update callers and wideners together.
3. Port input to SDL-backed vanilla input events/constants, including text-input focus notification. Keep custom widgets functional.
4. Migrate removed Fabric composting registration to data components/providers while preserving the existing chance calculations. Do not replace all probabilities with a constant or remove composting.
5. Migrate feature interfaces, feature-type codecs, registry keys and data-pack paths/configuration. Preserve spawning conditions and biome/feature behavior.
6. Reconcile networking, entity synchronization, persistence/serialization, commands and remaining source/compiler errors. Source identifier names must stay stable on disk and over protocol where intended.
7. Resolve or source-build integrations and validate nested runtime libraries. Rerun complete build and existing common tests; add focused gameplay/data regression tests as implementations land.
8. Complete package checks, isolated client/server startup, two-player gameplay/networking and disposable-world migration testing. Only then prepare a validated release and completed-work PR into this repository's main branch. Do not merge automatically.

## Reproduction

Use JDK 25 in `JAVA_HOME`, then the committed wrapper:

```sh
bash ./gradlew :common:compileKotlin --stacktrace --console=plain
bash ./gradlew :common:compileKotlin :common:compileJava :common:test :fabric:build --stacktrace --console=plain
python3 -m unittest discover -s scripts -p 'test_*.py' -v
```

Windows uses `gradlew.bat` instead. Set a writable `TEMP`/`TMP` directory for Python in restricted local sessions. See BUILDING.md for complete setup and expected outputs. Never install a sources/dev-slim archive or report an unexecuted test as successful. No production world/server has been touched and nothing has been pushed to GitLab.
