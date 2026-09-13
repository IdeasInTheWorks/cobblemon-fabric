# Changelog
## [1.8.2 (MONTH xth, 2026)](#1-8-2)

### Additions

### New rideable Pokémon

### Pokémon Added

#### Regional Forms

### Model updates for the following Pokémon

### Animation updates for the following Pokémon

#### Added sounds to the following moves

### Changes
- Stat increase and decrease particles have been changed, with the addition of alpha boost particles. 
- Dragonite's mailbag cosmetic can now also be obtained with a chest in the cosmetic slot
- Adjusted Croagunk's walkspeed
- Adjusted Aerodactyl's walkspeed
- Sharp Beak can now be crafted
- Metal Alloy can now be crafted
- Smoke Ball can now be crafted
- Light Ball can now be crafted
- Ability Capsules can now be brewed
- Using an Ability Capsule will now return an Empty Bottle
- The following items can now be obtained by brushing the following Pokémon:
  - Pink Wool - Wigglytuff
  - White Wool - Swablu, Altaria
  - Shed Shell - Ekans, Arbok, Dratini, Dragonair, Dunsparce, Treecko, Grovyle, Sceptile, Seviper, Scraggy, Scrafty, Helioptile, Heliolisk, Salandit, Salazzle, Sobble, Drizzile, Inteleon, Silicoblra, Sandaconda, Toxel, Toxtricity, Orthworm, Dudunsparce
  - Phantom Membrane - Dreepy, Drakloak, Dragapult
- The following items can now be obtained by evolving the following Pokémon into any of their evolutions:
  - Turtle Scute - Squirtle, Wartortle, Turtwig, Grotle, Tirtouga, Chewtle
  - Shed Shell - Metapod, Kakuna, Ekans, Paras, Venonat, Krabby, Scyther, Kabuto, Dratini, Dragonair, Ledyba, Spinarak, Yanma, Pineco, Dunsparce, GLigar, Pupitar, Treecko, Grovyle, Silcoon, Cascoon, Surskit, Nincada, Trapinch, Vibrava, Corphish, Anorith, Bagon, Shelgon, Kricketot, Burmy, Combee, Skorupi, Swadloon, Whirlipede, Dwebble, Scraggy, Karrablast, Joltik, Larvesta, Spewpa, Clauncher, Helioptile, Charjabug, Crabrawler, Cutiefly, Dewpider, Salandit, Wimpod, Poipole, Sobble, Drizzile, Dottler, Silicobra, Toxel, Sizzlipede, Snom, Tarountula, Nymble, Rellor
- Saccharine Leaves now drop Tart, Sweet, or Syrupy Apples, depending on their honey coating stage, instead of vanilla Apples
- Fixed the height of the Tumblestone Bud items when held in hand, placed on head, viewed in a GUI, and placed in an Item Frame
- Refactored all advancements, removing several deemed unnecessary. Due to this refactor, all previously obtained advancements will be reset besides the root (for picking a starter Pokémon). We are sorry for the inconvience, but it makes the tab cleaner and simplifies future advancements.
- Restructured Botany Pots integration folder structure
- Decrease the default pastured mon wander radius to 32.
- Factor in the weight of Pokémon when calculating pushing forces between hitboxes.
- Remade the Starter Selection Screen with new assets
- Refactored berry trees to be less impactful on TPS (more performant)
- Changed the bait effects of the following items: (Vanilla) Sweet & Glow Berries, Golden & Enchanted Golden Apples (Cobblemon) Custap, Eggant, Hopo & Micle Berries, Sweet Hearts
- Wild Pokémon now reliably have at least one offensive move if they can have offensive moves at that level.
- Data Monitor can accept Disks (TMs, Music Disks, Upgrade, Dubious Disk, etc.) to change what's being displayed on the screen
- Rider positioning is now client-driven (Remove poseOffsets and offset per seat/pose) in favour of sending real-time passenger position.
- Cleaned up seat data. Removed all now unused fields from seats in all species files. Also made locator seat names explicit in the species files (however, this doesn't break the legacy format)
- Marks can now provide aspects.
- Added new shiny and Alpha chance configs
- Apricorns now make a new sound when harvested
- Lowered the volume of harvesting berries
- Added alternate versions of the basic PC Wallpapers based on the original design of Cobblemon's PC, before public release
- Improved compatibility with various mods
    - Farmer's Delight's cutting board can now be used to strip Apricorn and Saccharine's logs, or salvage their planks from various blocks. It can also separate their boats from chests
    - Farmer's Delight's cutting board can be used to get two white dye from Pep-Up Flowers and two magenta dye from Medicinal Leeks
    - Farmer's Delight's cutting board can separate Tumblestone Blocks back into Tumblestones
    - Farmer's Delight's cutting board can salvage Type Gems from their Gem Blocks
    - Farmer's Delight's Onions, Tomatoes, and Cabbages can be used to cook Open-Faced Sandwiches. Nature's Spirit's Olives also can be used
    - Farmer's Delight's cutting board can be used on Hearty Grains to obtain rice and straw, similarly to Farmer's Delight's Rice Panicles
    - Farmer's Delight's Apple Pies and Apple Ciders can now be made using Applin Apples, alongside the Vanilla counterpart
    - Amendments allows for cooking Leek and Potato Stew in a Cauldron
    - Any mods that utilise Moonlight's Soft Fluid system can store Leek and Potato Stew, Medicinal Brew, Antidote, Awakening, Burn Heal, Ice Heal, Paralyze Heal, Full Heal, Potion, Super Potion, Hyper Potion, Max Potion, Full Restore, Ability Capsules, Moomoo Milk, and Berry Juice in liquid form (such as Suplementaries' Jar or Goblet, or Cauldrons while using Amendments)
    - Supplementaries' Hourglass can now use Bright Powder, Quick Powder, Metal Powder, Heal Powder, Silver Powder, and Remedy
    - Berry Trees are now compatible with the Planter properly
    - Saccharine Way Signs (added by Supplementaries automagically) now use a premade texture to prevent odd visual issues
    - Supplementaries' Jar can now store various Cobblemon cookies, candies, and similar items
    - Some of Supplementaries' items have been added to the `#cobblemon:held/container_held_items` item tag, including sacks, safes, and lunch baskets
    - Experience Nuggets can be used in place of Sculk for making the EXP Candies if using Create
    - Rage Candy Bar has a Create Mixing recipe
    - All Cobblemon ores can now be crushed in Create's Crushing Wheels
    - All Gem and Tumblestone Clusters can be crushed for automatic, low return, gems
    - Hearty Grains can be milled like Farmer's Delight Rice Panicles if both Farmer's Delight and Create are installed
    - Big Roots and Revival Herbs can be milled to their respective powders
    - Pep-Up Flowers, Medicinal Leeks, and all 70 berries can be milled in Create's Millstone for additional dye drops
    - Auspicious Armour and Upgrades can be Haunted using Create's crafting systems
    - Applin Apples can be used to make Honeyed Apples in Create
- Golden Apples can now be crafted using the Applin Apples
- Prismarine Crystals and Amethyst Shards can now be used to craft Pokedexes
- Updated Pokédex Screens item tag to support tagged variants of Redstone and Glowstone Dust
- Sounds for interacting with various Cobblemon blocks now play from the centre of the block, rather than the corner
- The `/pc` command no longer plays PC activation sounds
- The following items can now be placed on the ground as blocks:
  - King's Rock, Galarica Cuff, Unremarkable Teacup, Masterpiece Teacup, Chipped Pot, Cracked Pot, Sachet, Metal Powder, Quick Powder, Bright Powder, Icy Rock, Heat Rock, Smooth Rock, Damp Rock, Destiny Knot, Luminous Moss, Never-Melt Ice, Wide Lens, Scope Lens, Zoom Lens
  - Iron Ball, Metal Coat, Throat Spray, HP Up, Protein, Iron, Carbos, Calcium, Zinc, PP Up, PP Max, Upgrade, Dubious Disc, Loaded Dice
- The following items can now be placed on various surfaces:
  - Galarica Wreath, Magnet, Flame Orb, Life Orb, Toxic Orb, Light Ball, Smoke Ball
- The following items can now be placed as blocks, and have additional functionality:
  - Ring Target, Metronome, Eject Button
- The following items can now be worn by Players and Pokémon:
  - Galarica Wreath, Cleanse Tag, Spell Tag, Sharp Beak, Covert Cloak, Shell Helmet, Never-Melt Ice
- Wearable items can now be equipped by a player by holding it and right-clicking (or rebound use/interact)
  - Wearable items that can be placed down are exempt from this behaviour

### Fixes

### Developer

### Molang & Datapacks

### Particles

#### New Move Animations

### Localization
- Updated translations for: