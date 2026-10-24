# Minecraft 26.3 Fabric port assessment

Assessment started 2026-10-24 (Europe/London). This is an independent IdeasInTheWorks compatibility port, not an official Cobblemon release.

## Verified baseline and target

- Mojang's [version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json) reports stable Java Edition **26.3**, while 26.4-snapshot-5 is a snapshot. [Official release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3).
- Latest stable Cobblemon release inspected: **1.8.2**, upstream tag `1.8.2`, commit `643bf9068b51c829316429916373e5aa9864b8b9`. Its Gradle properties target Minecraft **1.21.1**, Java 21, Kotlin 2.2.20, Gradle 8.14.3, Architectury Loom 1.11-SNAPSHOT, Fabric Loader 0.17.2 and Fabric API 0.116.6+1.21.1.
- Upstream main at inspection: `f9f4745e933b18204a6f92bd9d7a349093711c79`, version 1.8.3-SNAPSHOT, still Minecraft 1.21.1. The release tag is the development baseline to avoid mixing unreleased gameplay changes into the port.
- Full upstream clone includes the remote branches and tags. No branch name explicitly targeting 26.3 was found. GitLab API searches for `26.3` in issues and merge requests returned no entries; that does not establish that no relevant work exists under other names.
- No official 26.3-compatible release was found in the release evidence inspected. [Publisher release listing](https://modrinth.com/mod/cobblemon/version/GJ2a1b8O) lists 1.8.2 for 1.21.1.
- Fabric metadata at assessment time reports stable Loader **0.20.1** for 26.3 and Maven lists Fabric API **0.165.0+26.3**. Exact build-tool pins will be recorded after dependency resolution.

## Major migration work

[Fabric's 26.1 migration guide](https://www.fabricmc.net/2026/03/14/261.html) requires Java 25 and new tooling for unobfuscated game distributions. The [26.3 migration guide](https://fabricmc.net/2026/09/15/263.html) recommends Loom 1.17 and Gradle 9.6.0 at release time. This project uses Architectury-specific common transforms and bundling, so those cannot simply be replaced by changing the plugin version.

| Area | Evidence and required work | Severity |
| --- | --- | --- |
| Build system | Old remapping pipeline, Java 21 and Gradle 8 must migrate to the Java 25/unobfuscated pipeline. | 10/10 |
| Fabric registries | CobblemonFabric imports removed CompostingChanceRegistry and StrippableBlockRegistry; migrate to item components and block transformers. | 10/10 |
| World generation | Custom features use ConfiguredFeature and FeatureConfiguration; 26.3 combines these into Feature and changes registry/data paths. | 10/10 |
| Rendering and input | Existing code uses GLFW and older rendering APIs; 26.3 uses SDL and substantially changed rendering infrastructure. | 10/10 |
| Dependencies | JEI, Mod Menu, LambDynamicLights, optional development mods, Graal/Showdown and Kotlin integration require individual compatibility checks. | 10/10 |
| Networking and storage | Payload codecs, entity synchronisation and world paths need source migration and disposable-world regression tests. | 10/10 |
| Gameplay | Capture, battle, spawning, progression and storage are release gates; source retention is not evidence they still work. | 10/10 |

## Licensing and isolation

The root LICENSE is MPL-2.0. Asset directories also contain their own Creative Commons and contributor notices. Preserve all of them and audit bundled dependencies before distributing a binary; the code licence alone does not cover all assets. Modified source must remain available with any binary release, and original copyright/attribution must remain intact. See [MPL sections 3.1–3.4](https://www.mozilla.org/en-US/MPL/2.0/).

Origin is exclusively `https://github.com/IdeasInTheWorks/cobblemon-fabric.git`. Upstream fetches from GitLab and has push URL `DISABLED`; `remote.pushDefault=origin`. No production server or world is in the build/test scope.

## Validation status

Source import and repository creation completed. Build, client, dedicated server, multiplayer, gameplay and migration validation are pending. No installable 26.3 JAR or release is claimed. Compilation errors and executed test results will be recorded in `porting/logs/` and `COMPATIBILITY.md`.
