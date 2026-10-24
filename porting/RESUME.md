# Development checkpoint

This is an incomplete migration, not an installable mod. Work is on `feature/latest-minecraft-fabric`; GitHub `main` remains the original 1.8.2 release. No release or completed-work pull request has been created because build/gameplay release gates have not passed.

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

1. Continue from common compile attempt 06. The BlockEntityType constructor migration has eliminated its source-file diagnostics. Resolve remaining registration and properties-key changes while preserving all factories and valid-block sets.
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
