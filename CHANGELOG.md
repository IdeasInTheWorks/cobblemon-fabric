# Changelog

## Unreleased â€” Cobblemon 1.8.2 custom Fabric port targeting Minecraft 26.3

Migration in progress. No validated release or gameplay claim.

Sprint S03 (R013/R015, 2026-10-24) assigns registry keys before item/block construction, passes explicit properties through 43 custom item classes, replaces removed vanilla construction helpers, and migrates NBT/registry APIs and 26 persistence callbacks on 13 block entities. All 140 item and 340 block ordered create-ID expressions are unchanged. Original licences and assets remain intact.

Five common compilation attempts end at 7,451 diagnostic lines in 1,005 files, down 728 (8.9%) from S02; compilation still fails. Forty-eight focused migration checks and 20 tooling tests passed. Inventory checks use minimal component fixtures, not vanilla gameplay defaults. A JUnit entry point shares the executed fixture; the JUnit wrapper has not run because common compilation is blocked. R013/R015 retain outstanding acceptance work, documented in the native roadmap and resume note.

Sprint S02 (2026-10-24) migrates 37 verified class relocations, seven entity registration keys, collision naming and identifier payload methods. Common compilation executed and still fails: 8,179 diagnostic lines in 1,062 files versus 10,586 in 1,207 before this sprint. Twenty offline tooling tests, four real-library identifier wire checks and a live two-contender lease test passed. Access-widener validation still reports 54 missing targets; common licence checks passed. No client/server or gameplay tests ran.

The native Google Sheets roadmap records 44 work packages, low/likely/high person-hour ranges, dashboard charts, build/sprint evidence, claim snapshots and handoff instructions. GitHub coordination uses scoped, expiring leases with stale-owner fencing, fast-forward claim transactions, isolated-worktree rules and a local push gate. These are cooperative safeguards; they do not create an atomic lock spanning claim and source pushes.

- Imported the original 1.8.2 release with its ancestor Git history and all source/assets/licences.
- Created the independent public IdeasInTheWorks development destination and development branch.
- Recorded target-version evidence, migration blockers, validation status and upstream maintenance instructions.
- Added an origin-only pre-push guard. Upstream GitLab is fetch-only.
- Began Java 25 / Gradle 9.6 / unobfuscated Loom tooling migration, retaining common and Fabric source modules.
- Migrated the renamed Minecraft identifier type across 452 source/access-widener files and switched Fabric log stripping to block transformers.
- Added build/test CI, package structure validation, and six validator regression tests using synthetic ZIP fixtures.

| Area | What changed | Why it changed | Why it would not work without the change | Blocker severity (1â€“10) |
| --- | --- | --- | --- | --- |
| S03 construction IDs | Typed item/block keys are set before property-derived descriptions, models and loot; shared plant properties are fresh | Modern constructors require identity before instantiation | Eager unkeyed construction fails startup or derives incorrect metadata | 10 |
| S03 item constructor chain | Explicit keyed properties through 43 item classes and 128 central constructor calls | Internal default property builders cannot infer their registration key | Berries, balls, medicine, mints and other items construct without required IDs | 10 |
| S03 vanilla helper removal | Supported log/leaf/pot constructors, keyed button invoker, sign placement and translation prefixes; custom template heading retained | Old Blocks helpers, SignItem and ItemNameBlockItem are absent | Missing classes/invokers fail compilation or mixin startup; lost prefixes change translations | 10 |
| S03 NBT reads | Explicit legacy scalar defaults, compound reads, typed list filtering and strict four-int UUID helpers | Modern reads return Optional and lists may be heterogeneous | Old readers fail compilation or silently compare Optional values; ownership/data can be corrupted | 10 |
| S03 persistence | Flat root MapCodec adapter, 26 ValueInput/ValueOutput callbacks and context-aware container helpers | Block-entity save/load signatures changed | Old overrides do not run; inventories and custom state cannot persist correctly | 10 |
| S03 registries/codecs | Separate value/holder lookups, lookupOrThrow and registry adapter identifiers | Registry.get now yields optional holders; ResourceKey accessor changed | Serializers and gameplay callers receive the wrong type or cannot resolve IDs | 10 |
| S03 regressions/handoff | Shared JUnit/standalone fixture with 48 checks, 20 tooling tests, five compile logs and re-estimated remainder | Migration needs reproducible evidence and safe continuation | Compilation alone cannot detect changed defaults, UUID formats, slot loss or root-field relocation | 8 |
| S02 game class relocations | Migrate 37 exact class/package relocations and matching widener descriptors | Target moved entity, advancement, rendering, gamerule and library classes | Old qualified names cannot resolve, including PokemonEntity superclass | 10 |
| S02 entity registration | Pass typed ENTITY_TYPE ResourceKeys into all seven entity builders, retaining IDs | Builder.build no longer accepts String ID | Every custom entity registration fails compilation | 10 |
| S02 collision properties | Rename noCollission to noCollision at 33 callers | Target corrected the method name | Block property construction fails compilation | 10 |
| S02 payload identifiers | Replace 30 reads and 29 writes with readIdentifier/writeIdentifier | ResourceLocation methods no longer exist; Identifier retains namespaced UTF-8 wire format | Storage, riding, spawning and UI payloads fail compilation; end-to-end behavior needs tests | 10 |
| S02 entity tag membership | Query entity type holder before testing a tag | EntityType.is(TagKey) was removed | Entity name-tag restriction check fails compilation | 10 |
| S02 work coordination | Atomic claim-ref updates, lease IDs/epochs/expiry, scoped push guard and isolated-worker handoffs | Multiple sessions need durable ownership and collision handling | Concurrent agents can overlap work or publish stale/out-of-scope changes | 8 |
| S02 planning/evidence | Native Sheets dashboard, 44-package roadmap, estimates, checks and resume pointers | Port spans multiple sprints with substantial unresolved APIs | Future sessions lack an evidence-based queue, ownership and release status | 6 |
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
## Original upstream changelog (preserved)

# Changelog
## [1.8.2 (October 11th, 2026)](#1-8-2)

### Additions
- The following items can now be placed on the ground as blocks:
  - King's Rock, Galarica Cuff, Unremarkable Teacup, Masterpiece Teacup, Chipped Pot, Cracked Pot, Sachet, Metal Powder, Quick Powder, Bright Powder, Soft Sand, Electirizer, Magmarizer, Icy Rock, Heat Rock, Smooth Rock, Damp Rock, Destiny Knot, Luminous Moss, Never-Melt Ice, Wide Lens, Scope Lens, and Zoom Lens.
  - I wasn't finished. Iron Ball, Metal Coat, Throat Spray, Terrain Extender, Medicinal Brew, Moomoo Milk, HP Up, Protein, Iron, Carbos, Calcium, Zinc, PP Up, PP Max, Upgrade, Dubious Disc, and Loaded Dice.
- The following items can now be placed on various surfaces:
  - Galarica Wreath, Magnet, Flame Orb, Life Orb, Toxic Orb, Light Ball, and Smoke Ball.
- The following items can now be placed as blocks and have additional functionality:
  - Ring Target, Metronome, and Eject Button.
- The following items can now be worn by Players and Pokémon:
  - Galarica Wreath, Cleanse Tag, Spell Tag, Sharp Beak, Covert Cloak, Shell Helmet, and Never-Melt Ice.
- Wearable items can now be equipped by a player by holding it and right-clicking (or whatever you've rebound use/interact to).
  - Wearable items that can be placed down are exempt from this behaviour.
- Mint Leaves can now be placed on the ground as Leaf Litter, or in Flower Pots.
- The recall animation now renders the ball in hand with the player's arm raised.
- Added Oricorio Flowers.
- Added Terrain Plants, four plants grown from Electric, Grassy, Misty, and Psychic Seeds, which glow under specific environmental conditions and can be farmed for more of their seeds.
- Berries can now be placed in Flower Pots.
- The evolution tree for a Pokémon can now be viewed in the Pokédex, showing the Pokémon's full family line and what's needed to evolve into each stage.
- Added custom sounds to a bunch of items that can be placed as blocks: liquid bottles, pill bottles, powders, and dice.
- Added paper as a new cosmetic item for Pikachu.
- Burmy changes its cloak after a battle it took part in, based on the biome.
  - Trash Cloak in the Deep Dark, Nether and End.
  - Plant Cloak in lush biomes such as forests, jungles, plains and swamps.
  - Sandy Cloak in sandy biomes, beaches and caves.

### Pokémon Added
#### Generation 4
- Snover
- Abomasnow
- Burmy
- Wormadam
- Mothim

#### Generation 5
- Tympole
- Palpitoad
- Seismitoad

#### Generation 6
- Helioptile
- Heliolisk

#### Generation 7
- Grubbin
- Charjabug
- Vikavolt
- Minior

#### Generation 8
- Eternatus

#### Generation 9
- Greavard
- Houndstone

### Model updates for the following Pokémon
- Sudowoodo
- Spheal
- Cacnea
- Cacturne
- Binacle
- Barbaracle
- Glimmet
- Glimmora
- Deerling
- Sawsbuck (All forms)
- Absol
- Poliwag
- Politoed
- Ekans
- Oddish
- Gloom
- Vileplume
- Bellossom
- Shroomish
- Chespin
- Quilladin
- Chesnaught
- Fennekin
- Braixen
- Delphox
- Froakie
- Frogadier
- Greninja
- Shinx
- Luxio
- Luxray
- Slakoth
- Riolu
- Lucario
- Corsola
- Mime Jr.

### Animation updates for the following Pokémon
- Treecko (New Shoulder Mount!)
- Bonsly (New Shoulder Mount!)
- Sudowoodo
- Cacnea
- Cacturne
- Ekans
- Arbok
- Oddish
- Gloom
- Vileplume
- Bellossom
- Pichu
- Pikachu
- Raichu (+ Alolan)
- Greninja
- Slakoth

### Changes
- Slowed the speed of Type Gem growth, and revamped their generation to grow in a more crystal-like shape.
- Terrain Seeds can now be planted.
- Terrain Seeds are now consumed during PvP and NPC battles.
- Glow Item Frames can be crafted using Bright Powder as an alternative to Glow Ink Sacs.
- Bright Powder can be used on Signs and Hanging Signs to make text glow, as an alternative to Glow Ink Sacs.
- Updated various recipes to display in Recipe Groups within the Recipe Book.
- Updated most recipes to display more logically and consistently within the Recipe Book's categories.
- Updated the following recipes
  - Never-Melt Ice, Focus Sash, Blunder Policy, Weakness Policy, Blank TM.
- Made ride rotations less jittery in multiplayer.
- Changes Disc Shelf TMs to use textures instead of tinting and overlays to line up with item display.
- Blank TM can now be inserted into the Disc Shelf.
- Added a TM creative tab.
- Made regional and Vivillon split-evo locations mutually exclusive.
- Vivillon's patterns are now viewable as forms with form names and unique Pokédex entries displayed.
- Item evolutions now unlock **all** locked evolutions whose requirements are met instead of stopping after the first.
  - Exceptions being "forced" evolutions which stop after the first one.
- Removed Mega Drain as a TM.
- PokeSnacks (and other area spawns) will now start the vertical offset checking from the center rather than base coordinate
  - This will help PokeSnacks to work inside caves or tighter spaces better.

### Fixes
- Type Gem blocks now properly generate clusters off of the block.
- Correctly grant "They're Minerals!" advancement when planting a Type Gem.
- Shoulder mounted Pokémon now follow player body animations, improving their positioning with custom player animations and default actions.
- Shoulder mounted Pokémon now sit slightly higher with chest armor equipped for better alignment, with a configurable blacklist.
- Type Gem Blocks now have advancements to give you the recipe in the recipe book (the data for the advancements were inside the recipes somehow??).
  - Some other recipes without advancements have also been corrected.
- Corrected the TM Recipes for Charge Beam and Payday.
- Starter Screen Categories will no longer stop rendering if they are bigger than the viewport and you are not viewing the top or bottom edges.
- The Random Starter option no longer runs off into other categories or becomes unaccessible due to scroll limits if it causes a new row.
- Mochi and Feathers no longer drop glass bottles on use.
- Fixed a rare set of concurrency bugs related to reloading packs on NeoForge; fixes a ModernFix compatibility issue.
- Removed unnecessary conditions for Mimikyu's standing pose. Hopefully fixes the rare instance of it using its shoulder animations during battle in multiplayer scenarios.
- Fixed Rockruff line not blinking.
- Fixed Flygon sometimes using its running animation instead of walking.
- Fixed Bramblin always sleeping outside of battle.
- Fixed Dragonair using its battle idle as a normal idle.
- Fixed Lycanroc's head movement.
- Added head movement to Archaludon.
- Fixed Duraludon line not having any sleep animations.
- Fixed Diglett line not having their faint animations.
- Fixed Kingambit moving its legs while walking.
- Fixed Alolan Diglett's cry not playing.
- Fixed shiny alpha Snorlax using the non-shiny texture.
- Fixed Crabominable's head breaking when looking around.
- Fixed Shiny Dipplin and Hydrapple's textures.
- Various structure fixes.
- Fixed shiny Pokémon under certain circumstances forgetting taught TMs.
- Fixed certain Pokémon sounds sometimes being doubled when played at specific times in their animations.
- Fixed ball capture beam origin coming from behind the ball.
- Fixed Safari Ball catch rate outside of battle.
- Fixed alpha eye (and other) particles not respecting the client particle density setting in video options.
- Fixed the TM Machine UI not closing when player is moving away from it.
- Moved the config category for alpha/shiny chance config fields for fossil machine and honey log slathering to Spawning.
- Fixed crashes for addon species with non-Cobblemon namespaces in evolution, fishing bait, and Pokédex lookups.
- Bees will now grow Apricorns and the Galarica Nut Bush, which were in the bee growables tag but never handled, and correctly spend their nectar when they do.
- Saccharine Leaves can now be bone mealed.
- Fixed equipping a stack of wearable items instead of one item of a stack.
- Fixed TM stack consumption in TM Machine screen when a player teaches a move to a Pokémon.
- Fixed every Miltank giving mushroom stew, not only mushroom variants.
- Medicinal Leeks can now be eaten when they aren't being planted, matching their food properties.
- Sweeping attacks no longer set owned Pokémon on fire (e.g. with Fire Aspect) or knock them back. Friendly Fire is now off.
- Fixed Pokémon not losing their previous form's signature moves when changing forms (e.g. Rotom keeping Overheat after leaving its Heat form).
- Form-specific moves gained from a form change are now only granted if the Pokémon is high enough level to learn them, and no longer create duplicates in benched moves.
- Spinda now learns Teeter Dance at level 32 instead of via TM.
- Fixed Oricorio Pa'u form description in the Pokédex.
- Fixed PC playing sound on close when opened via command.

### Developer
- Evolution requirements can now set `pokedexIcon` and `pokedexTooltip` directly in their JSON to control how they're shown on the Pokédex's evolution tree, for example:
  ```json
  {
    "variant": "attack_defence_ratio",
    "pokedexIcon": "minecraft:diamond_sword",
    "pokedexTooltip": "cobblemon.ui.pokedex.requirement.attack_defence_ratio.tooltip"
  }
  ```
- Fixed the wrong order of operations in the code flow prior to the `FOSSIL_REVIVED` event causing Pokémon to appear without the entity link.
- Fixed TestPartySlot command not correctly comparing the provided spec with the target Pokémon.

### Molang & Datapacks
- A new `negate` variant for evolution requirements has been added, which will negate (invert) the provided inner condition.
- Added Molang functions for individual Pokédex ids, wherein a subdex is passed in as a string to `dex_caught_count` etc.
- Added a `battle_ended` callback, backed by the new `BATTLE_ENDED` event, which fires whenever a battle ends for any reason, alongside a multitude of flags not previously covered by `BATTLE_VICTORY`.
- Added `battle_pokemon` to battle actor structs and `has_faced_opponents` to battle Pokémon structs.
- Added the `cobblemon:form_change/bagworm_cloak/plant_cloak`, `sandy_cloak` and `trash_cloak` biome tags, which control Burmy's cloak after battle.

### Particles

#### New Move Animations

### Localization
- Updated translations for:
  - Brazilian Portuguese
  - Canadian French
  - Japanese
  - Korean
  - Russian
  - Ukrainian
