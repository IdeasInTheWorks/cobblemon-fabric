# Cobblemon — independent Fabric compatibility port

**Development status: not compiled or validated for Minecraft 26.3. No installable custom release is available yet.**

This project ports [Cobblemon](https://gitlab.com/cable-mc/cobblemon) to stable Minecraft Java Edition **26.3**, with Fabric client and dedicated-server support as the goal. It is maintained independently by [IdeasInTheWorks](https://github.com/IdeasInTheWorks) and is not an official Cobblemon release. Original gameplay, source, models, textures, animations and attribution are retained from Cobblemon 1.8.2 (Minecraft 1.21.1).

Development: [feature/latest-minecraft-fabric](https://github.com/IdeasInTheWorks/cobblemon-fabric/tree/feature/latest-minecraft-fabric). Binaries, once validated: [GitHub Releases](https://github.com/IdeasInTheWorks/cobblemon-fabric/releases). Current builds: [GitHub Actions](https://github.com/IdeasInTheWorks/cobblemon-fabric/actions).

The [native Google Sheets roadmap and dashboard](https://docs.google.com/spreadsheets/d/1_H2BdYlb3f3CnsmP99R2GqlGeuOwax2-3NvjWuVqOQE/edit) tracks 44 work packages, effort ranges, sprint/build evidence and handoffs. Access follows its existing private Drive permissions. Continue from [porting/RESUME.md](porting/RESUME.md); read [AGENTS.md](AGENTS.md) and acquire a scoped work lease before editing. Sprint S04 migrates fossil and entity persistence, legacy item/memory payloads, Pomohana and creative-tab access. Common Kotlin compilation still fails with 6,733 diagnostic lines across 998 files (718 fewer than S03); 76 focused migration checks and 20 tooling tests pass. Compilation and gameplay release gates remain open; R013 and R015 still have outstanding acceptance work.

## Intended runtime requirements

| Component | Target configuration | Verification |
| --- | --- | --- |
| Minecraft Java | 26.3 exactly | Stable release verified from Mojang manifest on 2026-10-24 |
| Java | 25 | Official game metadata; portable development JDK installed |
| Fabric Loader | 0.20.1 or newer compatible stable version | Fabric metadata; full startup untested |
| Fabric API | 0.165.0+26.3 | Maven availability checked; full startup untested |
| Kotlin | Fabric Language Kotlin 1.14.2+kotlin.2.4.21, intended bundled | Packaging/startup pending |
| Battle/persistence engines | GraalJS and MongoDB libraries, intended bundled | Java 25 runtime compatibility pending |

Only Fabric is built. Retained NeoForge files are upstream source history/reference. Updating metadata does not establish game compatibility; [COMPATIBILITY.md](COMPATIBILITY.md) lists actual results and unresolved APIs/dependencies.

## Installation guide

These are the installation steps for a **future validated release**. Do not use this development source or a development-slim jar on a production server.

1. Back up the complete existing world, player data, Cobblemon data/database, configuration, data packs and resource packs while the old server is stopped. Use a copy in an isolated test instance.
2. Install Java 25 and Fabric Loader for Minecraft 26.3 in both the client instance and a separate dedicated-server directory.
3. Download the validated custom Fabric JAR from this repository's Releases page and verify its SHA-256. Add it and Fabric API for 26.3 to each instance's `mods/` directory. Follow the exact dependency list published with that release; avoid duplicate Cobblemon jars.
4. Launch the client into a disposable world. Start the isolated dedicated server using Fabric's generated launch file after the server operator has reviewed and accepted Mojang's EULA. Connect two matching modded clients.
5. Check startup logs, spawning, capture, wild/PvP battles, progression, party/PC, reconnect, saving and server restart. Only consider migration after the published validation and data-compatibility report covers your content and addons.

No migration or backwards-compatible world format is currently promised. Existing 1.21.1 addons, resource packs and datapacks need individual porting checks.

## Build and maintenance

Use JDK 25 and the committed wrapper. Exact Windows/Linux commands, output locations and troubleshooting are in [BUILDING.md](BUILDING.md).

```sh
bash ./gradlew :common:compileKotlin :common:compileJava :common:test :fabric:build --stacktrace --console=plain
```

The current command may fail during migration. CI retains diagnostics and only uploads a package when compilation, tests and structural validation succeed. It does not publish releases or prove gameplay correctness. See [the technical changelog](CHANGELOG.md), [initial assessment](porting/ASSESSMENT.md) and [upstream maintenance instructions](UPSTREAM.md). No production server files are part of this project.

## Licensing and attribution

Cobblemon is developed by the Cobblemon Team and its contributors. The original Git history, [MPL-2.0 licence](LICENSE), source copyright headers and per-asset Creative Commons notices are preserved. Asset licences have additional conditions; do not assume the source licence covers every asset. Source corresponding to any future binary release will be available in this repository. No official Cobblemon, Pokémon or Mojang endorsement is claimed.

## Original upstream README (preserved)

An open-source Pokémon mod for Minecraft Java Edition, written in Kotlin for Fabric and NeoForge.

## Getting started

To set up the development workspace, first clone the project and open the build.gradle with Intellij. Make sure that you clone it to a folder that has no spaces in its path (for example, C:/Development/Cobblemon Stuff/cobblemon/ is bad) since Architectury Plugin seems to dislike it.

After it takes ages to load, you should hopefully have runnable configurations of the project in the top right, such as Minecraft Client (:fabric). If not, try running `./gradlew genEclipseRuns`.

Troubleshooting:
- Try running `./gradlew --refresh-dependencies`
- Try File -> Invalidate Caches.
- Try deleting the `.idea` folder in the project root (make sure IntelliJ is closed when you try it).
- Try completely reclone the thing lmao.

## Contributing

If you're interested in contributing to the project, you can simply fork the repository and comment on any of the issues 
[here](https://gitlab.com/cable-mc/cobblemon/-/issues?scope=all&state=opened&label_name[]=accepted&assignee_id=None).

If you want a more involved role in the project, involving branches on the main repository instead of forking and involvement
in design discussions, you can either apply in the `application-box` forum of our Discord or add and message Hiroku (`hiroku_dev`) on our Discord here: https://discord.gg/cobblemon.
