# Building the independent Fabric port

## S11 native rendering checkpoint

The latest common compile fails at 4,556 diagnostic lines / 548 files; normal widener validation still fails on 13 targets. These commands preserve all normal gates. No release JAR has been produced.

The OneDrive generated build-logic directory locked again in validation attempts01/03. Keep the old outputs and select a fresh basename for generated included-build outputs, for example:

```powershell
$env:JAVA_HOME=(Resolve-Path '../tools/jdk25/jdk-25.0.4.1+1').Path
$env:GRADLE_USER_HOME=(Resolve-Path '../tools/gradle-cache').Path
.\gradlew.bat --init-script scripts/isolated_build_logic.gradle '-Dcobblemon.localBuildLogicOutput=local-build-logic-s11-compile04' :common:compileKotlin '-Pkotlin.incremental=false' '-Pkotlin.compiler.execution.strategy=in-process' --stacktrace --console=plain
.\gradlew.bat --init-script scripts/isolated_build_logic.gradle '-Dcobblemon.localBuildLogicOutput=local-build-logic-s11-validation04' :fabric:validateAccessWidener :common:checkLicenses :fabric:checkLicenses --continue --stacktrace --console=plain
```

If a generated directory locks, choose a new basename under build/ rather than deleting or moving it. The optional script redirects only included-build generated output. Normal CI/Linux builds continue to use the wrapper without this local init script.

Exact focused fixture command from the checkout (Python 3 and the workspace's resolved libraries):

```powershell
python scripts/check_data_contracts.py --game-jar ../tools/minecraft-26.3-client.jar --gradle-cache ../tools/gradle-cache --javac ../tools/jdk25/jdk-25.0.4.1+1/bin/javac.exe --fabric-api-jar ../tools/gradle-cache/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-api/0.165.0+26.3/8f8a23d6c9fbf7b075b31b4fb0a1007b0488d1d2/fabric-api-0.165.0+26.3.jar --legacy-game-jar ../tools/gradle-cache/caches/fabric-loom/1.21.1/minecraft-client.jar --legacy-version-json ../tools/gradle-cache/caches/fabric-loom/1.21.1/mojang_minecraft_info.json --report porting/logs/sprint-11-data-contracts-04.json
python -m unittest discover -s scripts -p 'test_*.py' -v
```

Final focused result: 2,723 core checks (including 1,024 interpolation cases), 19 Fabric contracts and 810 legacy cases / 1,620 checks pass. Tooling: 20 pass. Native descriptor construction uses reflection only in the fixture to invoke the private RenderType factory; production uses its validated widener. The atlas registry requires an applied Fabric accessor mixin: attempt03 preserves that failure. Attempt04 tests descriptors, a controlled reload barrier and apply executor; it does not start Fabric or stitch/upload an atlas. Existing minimal inventory components are still fixture-only. No full Java/JUnit/mixins/GPU/client/server/gameplay pass is claimed. Other render entrypoints and original acceptance remain open; see the S11 source audit and COMPATIBILITY.md.


Status: migration in progress. These commands reproduce the current build attempt; they do not yet produce a validated Minecraft 26.3 mod. See [COMPATIBILITY.md](COMPATIBILITY.md) and committed logs before installing anything.

## Requirements

- JDK **25** (a JDK, including `javac`, not only a Java runtime). Baseline 1.21.1 uses JDK 21.
- Official Gradle wrapper, upgraded to **9.6.0** with a pinned distribution SHA-256. No global Gradle install is needed.
- Kotlin Gradle plugin/standard library **2.4.21**, resolved automatically by Gradle.
- Architectury Loom **1.17.493**, using `dev.architectury.loom-no-remap` for unobfuscated game distributions.
- Git, Python 3 for package checks, internet access to Mojang/Fabric/Gradle/Maven repositories, and enough memory for the configured Gradle heap (`-Xmx8G` by default).

Use a short path without spaces, outside OneDrive, for a fresh checkout where practical; upstream Architectury tooling warns about paths containing spaces and current Loom warns about OneDrive. The first local attempt is in the user-provided workspace and retains its diagnostics.

## Checkout and configure

```sh
git clone https://github.com/IdeasInTheWorks/cobblemon-fabric.git
cd cobblemon-fabric
git switch feature/latest-minecraft-fabric
git remote add upstream https://gitlab.com/cable-mc/cobblemon.git
git remote set-url --push upstream DISABLED
git config remote.pushDefault origin
git config core.hooksPath .githooks
git config cobblemon.claimPython /absolute/path/to/python3
java -version
javac -version
```

Both Java commands must identify version 25. Set `JAVA_HOME` to the JDK root and include its `bin` directory in PATH. No project-specific secrets are required. Only common and Fabric projects are included in settings; imported NeoForge files remain preserved for history/reference.

Reading/building needs no lease. Editing and pushing require the scoped lease protocol in [AGENTS.md](AGENTS.md), using existing IdeasInTheWorks Git authentication. Never put tokens in commands, tracked files or logs. Each concurrent worker uses an isolated worktree and branch; the shared development branch requires an exclusive integration resource lease.

## Exact build commands

Linux/macOS:

```sh
bash ./gradlew :common:compileKotlin :common:compileJava :common:test :fabric:build --stacktrace --console=plain
python3 -m unittest discover -s scripts -p 'test_*.py' -v
python3 scripts/validate_jar.py fabric/build/libs/cobblemon-1.8.2-fabric-custom.1-mc26.3-$(git rev-parse --short=7 HEAD).jar --output-dir dist
```

PowerShell:

```powershell
.\gradlew.bat :common:compileKotlin :common:compileJava :common:test :fabric:build --stacktrace --console=plain
if ($LASTEXITCODE -ne 0) { throw 'Fabric build failed' }
python -m unittest discover -s scripts -p 'test_*.py' -v
$commit = git rev-parse --short=7 HEAD
python scripts/validate_jar.py "fabric/build/libs/cobblemon-1.8.2-fabric-custom.1-mc26.3-$commit.jar" --output-dir dist
```

The Kotlin toolchain is downloaded as a Gradle dependency. Game classes use Mojang's official names directly; there is no Yarn/Parchment remapping stage for 26.3. The common source output is merged into the Fabric Shadow JAR and mandatory runtime dependencies are nested with Loom. A build must retain the bundled Showdown engine, Pokémon assets and individual asset licences.

## Outputs and test scope

The shared Java regression fixture and JUnit entry point are in `common/src/test/java/com/cobblemon/mod/common/porting/`. S03 executed 48 checks; S04 extended the fixture to 76. S05 passes 127 assertions, adding actual scripted-brain construction, live runtime memories/expiry, sensor requirements, activity transitions, item-use result semantics and food/drink/model component metadata. S06 passes 170 assertions, adding registered battle UUID framing/truncation, native owner storage, all 16 wool registry IDs and legacy teleport-support policy. It also compiles the Java nullable-navigation bridge against the actual game API. Run it against the checksum-verified target JAR and matching libraries, choosing a new report path to preserve prior evidence:

```sh
python3 scripts/check_data_contracts.py --game-jar /path/to/minecraft-26.3-client.jar --gradle-cache /path/to/gradle-user-home --javac /path/to/jdk25/bin/javac --report porting/logs/data-contracts-new-run.json
bash ./gradlew :common:test --tests '*Minecraft263DataMigrationTest' --stacktrace --console=plain
```

The Gradle/JUnit command is blocked until common compilation succeeds; its JUnit wrapper has not executed. Existing CI already runs `:common:test`, so it will include this regression after the compile gate is fixed. The standalone runner compiles the actual production registration/NBT/template helpers and runs the same fixture without compiling the rest of Cobblemon. It bootstraps vanilla registries, uses a synthetic registry and minimal components for two inventory test items, and exercises IDs, legacy UUID arrays, malformed/default reads, typed lists, binary NBT, registry codecs, root ValueInput/ValueOutput merging and inventory slot/count round trips. These are unit fixtures, not client/server startup, gameplay, vanilla datapack-default validation or production-world migration tests. JOML/Unsafe and the root MapCodec adapter emit warnings on Java 25; the successful result records them.

Initial standalone probes exposed frozen intrusive registries and unbound item components. Their failed reports are retained as `sprint-03-data-contracts-01.json` and `-02.json`; the fixture now inspects construction metadata and initializes only missing test-item components. No runtime registry or component bypass was added to the mod itself.

Sprint S02 executed 20 offline Python tests: six synthetic package-validator tests and 14 work-claim protocol tests. These do not run Minecraft. Additional signature and identifier wire-format checks use the checksum-verified official target JAR and its exact cached runtime-library versions:

```sh
python3 scripts/check_api_contracts.py --game-jar /path/to/minecraft-26.3-client.jar --gradle-cache /path/to/gradle-user-home --javac /path/to/jdk25/bin/javac
```

The checker compares the game SHA-1 with `porting/version-evidence.json`, compiles four source contracts and executes four identifier round-trip/wire checks against real game libraries. It neither launches a game nor validates Cobblemon networking end to end. The live two-contender claim-race probe is an explicit network test (`scripts/check_live_claim_race.py`), not an automatic CI mutation.

- Candidate package: `fabric/build/libs/cobblemon-1.8.2-fabric-custom.1-mc26.3-<commit>.jar`, also copied to root `build/libs/` by assemble.
- Other jars marked `dev-slim` or `sources` are development products; do not install them.
- Structurally checked candidate, SHA-256 and JSON report: `dist/`, only after the validator succeeds.
- Unit results: `common/build/test-results/test/` and `common/build/reports/tests/test/`.
- Local migration attempts: `porting/logs/`.
- GitHub Actions uploads candidate packages only after build, tests and structural checks pass. Diagnostic reports are uploaded on failures. It never publishes a release automatically.

Package validation checks metadata, entrypoint/mixin classes, runtime dependencies, licences, assets, Showdown presence, namespace and classfile compatibility. It does not establish game startup or gameplay correctness. Reproducibility between two clean successful builds has not been demonstrated. Stable build/dependency pins and deterministic archive ordering/timestamps are configured, but a reproducible release requires completing and comparing those builds.

## Troubleshooting and release gates

S05 local source compilation used the following PowerShell command after generated Kotlin caches were locked. Quote the properties to keep PowerShell from splitting them. This changes compiler execution only; it does not bypass source or release checks.

```powershell
.\gradlew.bat :common:compileKotlin '-Pkotlin.incremental=false' '-Pkotlin.compiler.execution.strategy=in-process' --stacktrace --console=plain
.\gradlew.bat :fabric:validateAccessWidener :common:checkLicenses --continue '-Pkotlin.incremental=false' '-Pkotlin.compiler.execution.strategy=in-process' --stacktrace --console=plain
```

Stop Gradle/IDE compilers before moving generated caches. Verify absolute paths stay inside the checkout, preserve old outputs in an ignored backup directory, and regenerate only build outputs. S05 preserved old `common/build` and `gradle/build-logic/build` under ignored `build/sprint-05-cache-backups/`; no source or release artifacts were deleted. Prefer a fresh checkout outside OneDrive for future builds.

S07 repeated the source compile (6,005 lines /940 files, still failed) and executed286 focused assertions plus20 tooling tests. Both common and Fabric licence checks pass;49 widener targets remain. The default build-logic output directory hit locked/read-only OneDrive files. Attempts to preserve generated output partly succeeded; nothing in source was deleted. Use this optional local-only init script to regenerate included-build outputs under ignored `build/local-build-logic`:

```powershell
.\gradlew.bat --init-script scripts/isolated_build_logic.gradle :fabric:validateAccessWidener :common:checkLicenses :fabric:checkLicenses --continue '-Pkotlin.incremental=false' '-Pkotlin.compiler.execution.strategy=in-process' --stacktrace --console=plain
```

It changes generated output location only. Validation, source compilation and release gates remain enabled; CI uses the normal wrapper. The standalone runner now compiles shoulder/gamerule/movement helpers too. Its286 assertions do not run full entities, mixin transformation, JUnit, Minecraft or world migration. See `porting/sprint-07-summary.json` and the same native roadmap for current estimates, post-push CI and continuation.

Read the first error in the Gradle log before the stack trace. Do not suppress missing Minecraft APIs or widen Fabric's version requirement to make an older binary load. Resolve obsolete access-widener entries against the actual target classes. Verify integration coordinates and official-name compatibility; old intermediary-mapped mods cannot be assumed compatible.

The old Cadix licence plugin crashes on Gradle 9; it was replaced by Yumi Licenser with header checks retained. If the checker reports a mismatch, preserve existing copyright notices while fixing the rule or missing headers; do not bulk rewrite attribution. A missing Adorn release or other integration is a real dependency issue, not permission to remove its source.

After compilation, use separate disposable `fabric/runClient` and `fabric/runServer` directories. Dedicated server testing requires the server operator's Minecraft EULA acceptance. Keep test databases isolated. Do not use production worlds or change a running production server. Complete the gameplay, two-player networking and persistence matrix in COMPATIBILITY.md before tagging or publishing a release.

## S08 hive, AI and legacy-random checks

The standard focused runner now compiles typed hive, random-position and targeting helpers as well. S08 passed352 assertions; this does not compile the entire Java module or transform mixins. To also compare actual old and target direction generation, supply both official1.21.1 inputs:

```sh
python3 scripts/check_data_contracts.py --game-jar /path/to/minecraft-26.3-client.jar --gradle-cache /path/to/gradle-user-home --javac /path/to/jdk25/bin/javac --legacy-game-jar /path/to/gradle-user-home/caches/fabric-loom/1.21.1/minecraft-client.jar --legacy-version-json /path/to/gradle-user-home/caches/fabric-loom/1.21.1/mojang_minecraft_info.json --report porting/logs/data-contracts-local.json
```

The optional inputs must be paired. The runner verifies both game checksums and the old metadata version, then compares810 cases/1620 assertions including null candidates and subsequent RNG draws. It launches Java fixtures, not Minecraft. Hive fixtures use real vanilla registry entries and native codecs, not full Pokemon construction. S08 final common compile still fails5880/895;49 widener misses remain,20 tooling tests and headers pass. See `porting/sprint-08-summary.json`; use the same isolated-build-logic command above for local OneDrive output locks.

## S09 owner and vehicle contracts

The same standalone data-contract command above now compiles production BoatDropItem and a native reference/container fixture. S09 passes375 assertions (23 new cases) plus the optional810 actual legacy navigation cases/1620 checks. The fixture covers owner UUID saves and unload/reload lookup, current variant drop selection, sparse27-slot chest contents/custom components and unopened loot-table/seed saves. It deliberately throws if save/load unexpectedly requests a world or menu. This does not construct real boats, players or Pokemon, transform mixins, execute JUnit or validate capture/fishing/gameplay. First fixture attempt fails on private CODEC access; the public save/read replacement passes. See committed `porting/logs/sprint-09-data-contracts-02.json`.

Final S09 Kotlin compile5804/889 andAW49 still fail;20 tooling tests and normal common/Fabric headers pass. Use the existing official wrapper/init-script commands, without suppressing validation. See `porting/sprint-09-summary.json` and the same native roadmap for CI, estimates and continuation.

## S10 model and Fabric registry fixtures

The standalone runner now compiles the production legacy item-model/key helpers. The final run passes1559 assertions, including parsing1153 shipped item JSONs/287 overrides and native update selection, dependencies, callback evaluation, ARGB tint and explicit-native precedence contracts. A real model bake, resource reload, texture atlas and GPU are not exercised. The one pokemon_model builtin/entity root still needs the special-renderer port.

Add the pinned aggregate JAR to also compile the production Fabric standalone registry and execute12 captured-context assertions:

```sh
python3 scripts/check_data_contracts.py --game-jar /path/to/minecraft-26.3-client.jar --gradle-cache /path/to/gradle-user-home --javac /path/to/jdk25/bin/javac --legacy-game-jar /path/to/minecraft-1.21.1-client.jar --legacy-version-json /path/to/1.21.1/mojang_minecraft_info.json --fabric-api-jar /path/to/fabric-api-0.165.0+26.3.jar --report porting/logs/data-contracts-local.json
```

Gradle resolves the aggregate under `caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-api/0.165.0+26.3/`. The fixture checks its id/version and records SHA25618442e2ad466d6ef41c37dbb4cb68495e6ca6658b1f1ee9be76143d155ef2c88 for the executed input. Nested modules extract only into ignored `build/data-contracts/fabric-api`; game checksums remain enforced. Registration/lookup simulations are not a Fabric game startup or actual reload. Run from the checkout root for asset/metadata fixtures.

Common compile4845/558 andAW47 still fail. Do not remove remaining validators to package a JAR. Native ColorProviderRegistry.ITEM and old ItemProperties are replaced by public legacy predicate/color adapters; addon implementors must update those APIs. Original assets remain; modern explicit item definitions take precedence over runtime legacy synthesis. See `porting/sprint-10-summary.json` for commands, attempts and limits.
