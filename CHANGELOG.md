# Changelog

## Unreleased — Cobblemon 1.8.2 custom Fabric port targeting Minecraft 26.3

Migration in progress. No validated release or gameplay claim.

- Imported the original 1.8.2 release with its ancestor Git history and all source/assets/licences.
- Created the independent public IdeasInTheWorks development destination and development branch.
- Recorded target-version evidence, migration blockers, validation status and upstream maintenance instructions.
- Added an origin-only pre-push guard. Upstream GitLab is fetch-only.
- Began Java 25 / Gradle 9.6 / unobfuscated Loom tooling migration, retaining common and Fabric source modules.
- Migrated the renamed Minecraft identifier type across 452 source/access-widener files and switched Fabric log stripping to block transformers.
- Added build/test CI, package structure validation, and six validator regression tests using synthetic ZIP fixtures.

| Area | What changed | Why it changed | Why it would not work without the change | Blocker severity (1–10) |
| --- | --- | --- | --- | --- |
| Repository | GitHub origin, disabled upstream push URL, origin-only pre-push hook | Keep development under IdeasInTheWorks and protect upstream | An unguarded clone can accidentally push to the original project | 8 |
| Compatibility evidence | Added assessment and explicit test status | Pin the actual stable target and expose unresolved work | Version-only edits could be mistaken for a working port | 10 |
| Game/toolchain | Target 26.3, Java 25, Kotlin 2.4.21, Gradle 9.6.0 with checksum-pinned official wrapper | Minecraft now requires Java 25; old Gradle does not support this JVM | Compilation/build tooling cannot handle the target runtime | 10 |
| Loom and modules | Pin Architectury Loom 1.17.493 in no-remap mode; common + Fabric only; standard Java configurations replace mod-remapping configurations | 26.x game distributions are unobfuscated | Old mappings and remap tasks cannot process the new distribution | 10 |
| Packaging | Merge common output into Fabric Shadow JAR; nest Kotlin/Graal/Mongo libraries; preserve licences/assets; deterministic archive order and timestamps | Old transform/remap output is absent in no-remap mode | A slim Fabric-only jar omits gameplay classes and mandatory runtimes | 10 |
| Build plugins | Shadow 9.6.1, Idea-ext 1.4.1; replace Cadix Licenser 0.6.1 with Yumi Licenser 4.0.0 | Old licence plugin produced StackOverflowError on Gradle 9 | Build fails before source compilation | 10 |
| Fabric dependencies | Loader 0.20.1, API 0.165.0+26.3, Kotlin language adapter 1.14.2+kotlin.2.4.21 | Resolve packages published for target game/toolchain | 1.21.1 runtime API references are incompatible | 10 |
| Integrations | Begin pins for JEI 31.14.0.72, Mod Menu 21.0.0, LambDynamicLights 4.13.0+26.3; preserve Adorn integration pending a compatible build | These extensions depend on Minecraft internals | Old binary API types/mappings may fail compilation/loading | 10 |
| Identifier API | Replace ResourceLocation type references with official Identifier in common/Fabric source and access-widener descriptors | The old class is absent from the verified 26.3 client JAR | Imports and JVM method descriptors refer to a nonexistent class | 10 |
| Block interaction | Replace StrippableBlockRegistry with BlockTransformerHelper.registerStripping, retaining all log/stripped-block pairs | Fabric removed the old registry | Registration fails to compile and log stripping is unavailable | 10 |
| Block entity registration | Replace removed BlockEntityType.Builder with the supported constructor through a shared factory helper, preserving all factories and valid-block sets | The builder no longer exists in 26.3 | 26 source diagnostics prevent compilation of healing machines, PCs, storage and other block entities; those diagnostics are eliminated in attempt 06 | 10 |
| Fabric metadata | Link sources/issues to IdeasInTheWorks, mark independent port, use official ModelPart name for injected Bone interface | Runtime uses official names and bugs belong in this repository | Intermediary class name targets a nonexistent class | 10 |
| Publication | Remove upstream Maven publishing destinations/credential configuration | All custom port publications belong under IdeasInTheWorks | Existing publish tasks target infrastructure outside this project's destination | 8 |
| CI and checks | Read-only token, pinned official Actions, common tests, Fabric build, diagnostics upload, checked candidate upload; no automatic releases | Preserve failed-build evidence and reject incomplete packages | A compiled but incomplete archive could be offered for installation | 10 |

All changes above are migration work in progress. Full project compilation has not passed. Pending work includes completing build configuration/dependency resolution, access-widener migration, Minecraft/Fabric APIs, rendering/input, networking, gameplay regression validation, world migration testing and validated release packaging.
