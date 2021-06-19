# Refined Storage Changelog

### 1.9.13

- Allow simple math input in AmountSpecifyingScreen (mooofins)
- Fixed count on Storage Monitor having Z fighting (tivec)
- Fixed items on Storage Monitor not being flat (raoulvdberge)
- Removed experimental pipeline nagging message (raoulvdberge)
- Fixed crash when using an External Storage on a fluid inventory (jeremiahwinsley)
- Added some performance improvements for autocrafting (necauqua, Darkere)
- Fixed a memory leak in the pattern cache (necauqua)
- Fixed Detector crashing when dyed (Darkere)
- Fixed autocrafting being stuck after clicking "Start" (necauqua)
- Fixed Crafting Monitor not being able to show hours (Darkere)
- Fixed capacity rendering of infinite storages (Darkere)
- Fixed wrong alignment for the JEI request autocrafting tooltip (Darkere)
- Fixed mobs getting stuck in Refined Storage cables (Darkere)
- Fixed dismantling storage blocks ignoring stack size (Darkere)
- Fixed Ice and Fire banners breaking with Refined Storage (Darkere, necauqua)
- Fixed empty keybinding causing GL errors (Darkere)
- Fixed some parts of the Japanese translation (akihironagai)
- Fixed rendering issue on blocks when using OptiFine (mooofins)

### 1.9.12

- Fixed some issues when using the Grid when it's offline (Darkere)
- Fixed crafting events not being fired in some cases in the Grid (Darkere)
- Fixed not being able to set fluid filter slot output quantity (Darkere)
- Fixed mod id search not working for Industrial Foregoing (Darkere)
- Fixed fluid autocrafting duplicating fluids (Darkere)
- Fixed some Grid crashes (ScoreUnder)
- Fixed constructor not using compare mode correctly in some cases (ScoreUnder)
- Fixed duplication bug in the Interface (Darkere)

### 1.9.11

- Fixed disks and network devices not loading when they did not previously exist

If you are affected by this please go to the world/data/ folder and remove the ".temp" ending from the files before
launching.

### 1.9.10

- Improve performance of the Grid view (ScoreUnder)
- Fixed Disk Manipulator model glitches (Darkere)
- Improve performance of the Disk Manipulator (Darkere)
- Fixed being unable to set quantity in output slots of the Pattern Grid (Darkere)
- Fixed External Storage in fluid mode losing track of fluids sometimes (Darkere)
- Added code to avoid / minimize data corruption issues caused by Minecraft (Darkere)
- Fixed processing autocrafting orders stealing items from each other (Darkere)
- Fixed Constructor in fluid mode voiding fluid source blocks in front of it (Darkere)
- Update Japanese translation (alyxferrari)
- Fixed crash when recoloring blocks that have no rotation component (Darkere)
- Fixed reloading resource packs breaking Refined Storage textures (Darkere)

### 1.9.9

- Fixed Refined Storage sidebuttons displaying over the JEI bookmark pagination buttons (raoulvdberge)
- Fixed issue where Crafters may fail to recognize an inventory/tank for some patterns (Darkere)
- Fixed issue where the Crafter Manager can crash on invalid patterns (raoulvdberge)
- Fixed issue where alternatives in the Pattern Grid weren't being saved properly (raoulvdberge)
- Fixed not being able to change the Exporter filter slot count with regulator mode without closing and re-opening the
  container (raoulvdberge)

### 1.9.8

- Fixed server crash when scrolling in Grid (Darkere)
- Fixed various issues with Grid interactions working without power (Darkere)
- Fixed changing rotation not updating blocks (Darkere)
- Added a JEI synchronized (two-way) search box mode to the Grid (ScoreUnder)
- Added a nag message when a player joins the world that asks the player to enable the experimental Forge lighting
  pipeline to ensure correct rendering (raoulvdberge)

### 1.9.7

- Added functionality to move items in the Grid with shift/ctrl + scrolling (Darkere)
- Changed JEI transfer error mechanics (raoulvdberge)
- Fixed crash when opening Controller GUI (Darkere)
- Fixed dye being consumed without effect in some cases (Darkere)
- Fixed deadlock caused by Portable Grid (Darkere)
- Fixed custom tooltips not working in the Grid (Darkere)

### 1.9.6

- Port to Minecraft 1.16.3 (raoulvdberge)
- Added colored block variants (Darkere)
- Added functionality to show missing items in the JEI transfer screen (Darkere)
- Added functionality to request missing items from autocrafting in the JEI transfer screen (Darkere)
- Added client config option to remember the Grid search query (raoulvdberge)
- Fixed Portable Grid losing enchantments when placing and breaking (raoulvdberge)

### 1.9.5

- Re-added the `/refinedstorage disk create <player> <id>` command (raoulvdberge)
- Added the `/refinedstorage disk list` command (raoulvdberge)
- Added the `/refinedstorage disk list <player>` command (raoulvdberge)
- Added the `/refinedstorage network list <dimension>` command (raoulvdberge)
- Added the `/refinedstorage network get <dimension> <pos>` command (raoulvdberge)
- Added the `/refinedstorage network get <dimension> <pos> autocrafting list` command (raoulvdberge)
- Added the `/refinedstorage network get <dimension> <pos> autocrafting get <id>` command (raoulvdberge)
- Added the `/refinedstorage network get <dimension> <pos> autocrafting cancel` command (raoulvdberge)
- Added the `/refinedstorage network get <dimension> <pos> autocrafting cancel <id>` command (raoulvdberge)
- Added JEI ghost ingredient dragging support (raoulvdberge)
- Fixed text field not being focused in amount specifying screens (raoulvdberge)

### 1.9.4

- Fixed JEI recipes/usages keys not working in Grids (raoulvdberge)

### 1.9.3

- Port to Minecraft 1.16.2 (raoulvdberge)
- Fixed duplication bug with the Constructor (Darkere)
- Updated Japanese translation (a2y4)
- Updated Taiwanese translation (ForFunPenguin)
- Refactored autocrafting code (raoulvdberge)

### 1.9.2b

Fixed duplication bug with the Constructor (Darkere, raoulvdberge)

### 1.9.2

- Fixed crash with Forge version 67 (Darkere)
- Fixed cases where Refined Storage unwillingly acts like a chunkloader (raoulvdberge)
- Fixed Network Transmitters being able to connect to any network device (raoulvdberge)
- Fixed Crafting Tweaks buttons being in the wrong position after changing the size configuration of the Grid (
  raoulvdberge)
- Networks that are in a chunk that isn't loaded will no longer work, they will turn off. Chunkload the Controller to
  maintain a functioning network over long distances (Darkere/raoulvdberge)
- Re-added interdimensional networks with the Network Transmitter and Network Receiver (raoulvdberge)
- Re-added MouseTweaks integration (raoulvdberge)

### 1.9.1

- Fixed server crash (raoulvdberge)

### 1.9

- Port to Minecraft 1.16 (raoulvdberge)
- Fixed wrench requiring shift click to rotate blocks (raoulvdberge)

### 1.8.8

- Fixed duplication bug and weird behavior in the Crafting Grid matrix (Darkere)

### 1.8.7

- Fixed Regulator mode item and fluid counts not saving properly (raoulvdberge)
- Fixed Wireless Crafting Monitor not closing properly (raoulvdberge)
- Fixed Controller always using energy, even when disabled with redstone (raoulvdberge)
- Fixed internal crafting inventory not being returned when Controller is broken (raoulvdberge)
- Fixed bug where autocrafting tasks started on the same tick make the wrong assumption about available items and
  fluids (Darkere)
- Fixed bug where the "To craft" amount in the Crafting Preview window is wrong (raoulvdberge)
- Fixed bug where non-pattern items are able to be inserted into the Crafter Manager (Darkere)
- Fixed performance issue where shapes of cable blocks were constantly being recalculated (raoulvdberge)
- Drastically improved shift clicking performance in Crafting Grid (Darkere)
- Removed autocrafting engine version from crafting preview screen (raoulvdberge)

### 1.8.6

- Fixed Constructor duplication bug (Darkere)

### 1.8.5

- Cancelling a crafting task now also unlocks all Crafters related to that task (Darkere)
- External Storage will now always show the exact maximum capacity as reported by the attached inventory (Darkere)
- Crafters no longer expose their inventory to the side they are facing (Darkere)
- Fixed Portable Grid voiding the disk when extracting with full inventory (Darkere)
- Fixed Constructor extracting 2 buckets when placing fluid (Darkere)
- Fixed Stack Overflow error with regulator upgrades (Darkere)
- Fixed visual bug with the Detector not updating its values (Darkere)
- Fixed Constructor placing the filtered item instead of the extracted (Darkere)
- Fixed duplication bug with filter slots (Darkere)
- Fixed shift crafting in a Grid not using the player (Darkere)
- Re-added all the language files (TheDirectorX)
- Added Japanese translation file (KusozakoAtama10k)
- Changed package name to "com.refinedmods.refinedstorage", this is a breaking change for addons (raoulvdberge)
- Fixed bug where shift clicking gives too many items (Darkere)

### 1.8.4

- Fixed autocrafting Crafting Monitor crash (Darkere)

### 1.8.3

- Added a new experimental autocrafting engine that's enabled by default. This should improve autocrafting performance (
  Darkere)
- Wireless Transmitters can now be placed on any block and in any direction (raoulvdberge)
- Fixed Exporter not exporting anything when using a Stack Upgrade and there isn't space for 64 items in the inventory (
  raoulvdberge)
- Fixed Controller always using the base usage even when turned off (raoulvdberge)
- Added the Regulator Upgrade that can be inserted into a Exporter. This ensures a certain amount of items and fluids is
  kept in stock in a connected inventory (raoulvdberge)
- Fixed severe memory leak in the storage cache (raoulvdberge)
- Added debug logging on the server when an expensive operation occurs (raoulvdberge)

### 1.8.2

- Add Refined Storage silicon to forge:silicon tag for mod compatibility (jeremiahwinsley)
- Update pt_br translation (Arthur-o-b)
- Added waterlogging to all cable blocks (Darkere)
- Fixed storage block dropping extra processor (Darkere)
- Create zh_tw translation (ForFunPenguin)
- Re-added zh_cn translation (ppoozl)

### 1.8.1

- Port to Minecraft 1.15.2 (raoulvdberge)
- The Storage Monitor supports fluids as well now (V1RTUOZ)

### 1.8

- Port to Minecraft 1.15 (raoulvdberge)

### 1.7.3

- Fixed severe energy update lag introduced by version 1.7.2 (raoulvdberge)

### 1.7.2

- Resource packs can now define the font colors that Refined Storage GUIs need to use (raoulvdberge)
- Patterns being added or removed from the network are now propagated as well to clients that are watching a Grid (
  raoulvdberge)
- When pressing ESCAPE in the search box on the Grid or Crafter Manager, focus on the search bar will be lost first
  before closing the GUI immediately. Then on the next ESCAPE press, the GUI will be closed (raoulvdberge)
- Fixed crash when loading a network (raoulvdberge, LezChap)
- Fixed being able to drain energy from the Refined Storage Controller (raoulvdberge)
- Fixed the Grid crashing on a item/fluid update-heavy storage system (raoulvdberge, Darkere, noobanidus)
- Fixed the Grid displaying the old quantity when shift clicking an entire stack out (raoulvdberge)
- Fixed crash with the Disk Manipulator and using item/fluid filters when inserting into the network (raoulvdberge)
- Fixed the network being able to run off 1 FE/t (raoulvdberge)

### 1.7.1

- Exact mode for processing patterns no longer exist, you can now define per-slot which item/fluid tags are allowed to
  be used by autocrafting, by using CTRL + click on the filter slot in the Pattern Grid (raoulvdberge)
- The Pattern Grid now switches automatically between crafting pattern and processing pattern mode when using JEI
  transfer (raoulvdberge)
- The Pattern Grid now switches automatically between crafting pattern and processing pattern mode when re-inserting an
  existing Pattern (raoulvdberge)
- Removed migration code for the development builds that were released on Discord (not on CurseForge). If you used the
  development builds and never used version 1.7 before, first switch to 1.7, open your world, modify a storage disk, and
  then upgrade to 1.7.1 (raoulvdberge)
- Grids now do not sort if you interact with it while holding shift (Darkere)
- Fixed Pattern Grid causing world hanging on load (raoulvdberge)
- Fixed External Storage not refreshing when the storage is broken or replaced (raoulvdberge)
- Fixed delay in block update when placing a cable block (raoulvdberge)
- Fixed holder of cable blocks sometimes conflicting with a cable connection while rendering (raoulvdberge)
- Fixed being able to move wireless items in inventory when using a keybinding to open (raoulvdberge)
- Fixed crash when breaking a Grid, Crafting Monitor, Crafter Manager or Portable Grid when another player is still
  using it (raoulvdberge)

### 1.7

NOTE: This is an alpha release. Bugs may happen. Remember to take backups.

- Port to Minecraft 1.14 (raoulvdberge)
- Removed the Reader and Writer, this will return later in an addon mod (raoulvdberge)
- Removed cross dimensional functionality on the Network Transmitter for the moment, this will return later (
  raoulvdberge)
- Removed covers (raoulvdberge)
- Fixed the Fluid Grid not having a View type setting (raoulvdberge)
- Oredict mode for Patterns has been replaced with "Exact mode" (by default on). When exact mode is off, Refined Storage
  will use equivalent items or fluids from the Minecraft item/fluid tag system (raoulvdberge)
- Grid filtering with "$" now does filtering based on item/fluid tag name instead of oredict name (raoulvdberge)
- When binding a network item to a network you can now bind to any network block, not only the Controller (raoulvdberge)

### 1.6.16

- Updated Russian translation (Bytegm)
- Fixed erroring controller tile entity (ian-rampage)
- Fixed Inventory Tweaks sorting not respecting locked slots (Landmaster)
- Fixed OpenComputers driver voiding excess fluids (BlueAgent)
- Fixed being able to move wireless items in inventory (raoulvdberge, Dabombber)

### 1.6.15

- Fixed recipes with more than 1 bucket of fluid not transferring from JEI (Darkere)
- Fixed oredict crafting patterns redefining recipes (Darkere)
- Fixed Portable Grids not keeping their enchantments when placed (Darkere)
- Shortened crafting text for the Russion translation to fix Grid overlays (yaroslav4167)
- Fixed JEI hotkeys not working on fluid filter slots (raoulvdberge)
- Fixed crash when opening Crafter Manager with FTB Quests installed (raoulvdberge)
- GregTech Community Edition Wires and Machines are now banned from rendering on Refined Storage patterns because they
  are causing crashes (raoulvdberge/Darkere)
- Fixed a bug where the container slots weren't synced when opening a Grid (raoulvdberge)

### 1.6.14

- Fixed server crash (raoulvdberge)

### 1.6.13

- Fixed Interface with Crafting Upgrade being stuck if an earlier item configuration has missing items or fluids (
  raoulvdberge)
- Added keybindings to open wireless items. The default one set to open a Wireless Crafting Grid from Refined Storage
  Addons is CTRL + G (raoulvdberge)
- Added Grid quantity formatting for item counts over 1 billion (raoulvdberge)
- Updated German translation (cydhra)
- Updated Chinese translation (KoderX)
- Fixed wrong item count for oredict patterns (the-eater)
- Fixed autocrafting duplication bug (Radviger / notcake)
- Fixed Crafting Pattern not rendering tile entity items like a chest (zhykzhykzhyk / raoulvdberge)
- Let Constructor and Destructor interact with world using their owner's profile (Radviger)

### 1.6.12

- Increased the speed of autocrafting (raoulvdberge)
- Fixed External Storage sending storage updates when it is disabled (raoulvdberge)
- Fixed slight performance issue with loading Crafters from disk (raoulvdberge)
- Fixed storage GUIs overflowing on large numbers (raoulvdberge)
- Added a completion percentage to the Crafting Monitor (raoulvdberge)
- Updated Russian translation (kellixon)

### 1.6.11

- Fixed blocks neighboring a controller breaking when returning from a dimension in a unchunkloaded area (raoulvdberge)

### 1.6.10

- Fixed client FPS stalling when using "@" mod search in the Grid (raoulvdberge)
- Fixed client FPS stalling when using "#" tooltip search in the Grid (raoulvdberge)
- Fixed fluid inputs/outputs in the Pattern Grid not being set when you re-insert a Pattern with fluid inputs/outputs (
  raoulvdberge)
- Fixed bug where the Pattern Grid doesn't update it's output slot when manually configuring a crafting pattern (
  raoulvdberge)
- Fixed network node scanning allowing multiple controllers in some cases (raoulvdberge)
- Fixed OpenComputers integration not giving back a crafting task instance in the schedule task API (raoulvdberge)
- Fixed OpenComputers integration causing log spam when getting processing patterns (raoulvdberge)
- Fixed OpenComputers voiding items with extract item API when there is no inventory space (raoulvdberge)
- Fixed CraftingTweaks buttons resetting sometimes in the Crafting Grid (raoulvdberge)
- Fixed Refined Storage jars not being signed (raoulvdberge)
- Fixed crafting task stalling when there's not enough space in the inventory (raoulvdberge)
- Fixed another duplication bug with a disconnected Crafting Grid (raoulvdberge)
- Fixed oredict mode in autocrafting not working at all (raoulvdberge)
- Removed getMissingItems() and getMissingFluids() functions from the OpenComputers integration, that info is now
  accessible through schedule(Fluid)Task(). If you just want to check if there are missing items/fluids but don't want
  to start an actual task, use the "canSchedule" parameter (raoulvdberge)
- Removed the Interdimensional Upgrade, Network Transmitters are now cross dimensional by default (raoulvdberge)
- Removed the per block FE cost of the Network Transmitter, it draws a fixed amount of FE/t now (raoulvdberge)
- Updated Russian translation (kellixon)
- Added fluid functions for the fluid autocrafting to the OpenComputers integration (raoulvdberge)
- Slightly increased performance of the External Storage (raoulvdberge)

### 1.6.9

- Fixed OpenComputers "unknown error" when using extract item API (raoulvdberge)
- Fixed client FPS stuttering when opening a Crafting Grid (raoulvdberge)
- Fixed rare Grid crashing issue (raoulvdberge)
- You can now interact with the fluid container input slot in the Fluid Interface (raoulvdberge)

### 1.6.8

- Fixed Ender IO incompatibility (raoulvdberge)

### 1.6.7

- Fixed the Raw Processor recipes not taking oredicted silicon (raoulvdberge)
- Fixed the Processor Binding recipe not taking oredicted slimeballs (raoulvdberge)
- The Processor Binding recipe now only gives 8 items instead of 16 (raoulvdberge)

### 1.6.6

- Added new Crafter modes: ignore redstone signal, redstone signal unlocks autocrafting, redstone signal locks
  autocrafting and redstone pulse inserts next set (replacement for blocking mode) (raoulvdberge)
- Added a config option to configure the autocrafting calculation timeout in milliseconds (raoulvdberge)
- Added throttling for network devices that can request autocrafting (raoulvdberge)
- Renamed Cut Processors to Raw Processors and those are now made with Processor Binding instead of a Cutting Tool (
  raoulvdberge)
- Fixed an autocrafting bug where it crashed when external inventories couldn't be filled (raoulvdberge)
- Fixed a duplication bug with a disconnected Crafting Grid (raoulvdberge)
- Fixed oredict autocrafting sometimes reporting that a craftable item is missing (raoulvdberge)
- Fixed fluid autocrafting without item inputs locking when there's not enough space for the fluids (raoulvdberge)
- Fixed Grid "last changed" date not changing when using clear button or JEI transfer (raoulvdberge)
- Fixed a duplication bug when pressing clear on a Wireless Crafting Grid from Refined Storage Addons (raoulvdberge)
- Fixed a duplication bug with autocrafting and External Storages (raoulvdberge)
- Fixed Crafting Manager displaying wrong name for chained crafters connected to some blocks (raoulvdberge)
- Fixed crafting task losing internal buffer when network runs out of energy (raoulvdberge)
- Removed handling of reusable items in autocrafting, to avoid problems (raoulvdberge)
- You can no longer start a crafting task if it has missing items or fluids (raoulvdberge)
- The Security Manager now supports Security Cards that have no player assigned to them. It is the default security card
  for players that aren't configured (raoulvdberge)
- If no default Security Card is configured in the Security Manager, an unconfigured player is allowed to do everything
  in the network. Create a default Security Card (craft a Security Craft and don't assign it to a player, it acts as a
  fallback) to handle unconfigured players (raoulvdberge)

### 1.6.5

- Fixed Refined Storage silicon's oredict entry being registered too late (raoulvdberge)
- Fixed duplication bug with filter slots (raoulvdberge)
- The Pattern Grid in fluid mode now supports up to 64 buckets in the input and output processing slots (raoulvdberge)

### 1.6.4

- Rewrote autocrafting again, bringing performance up to par with other autocrafting mods (raoulvdberge)
- Autocrafting now reserves items and fluids in an internal inventory to avoid having the storage network steal stacks
  required for autocrafting (raoulvdberge)
- Reworked the Crafting Monitor to be more condensed and more clear (raoulvdberge)
- Removed left / right click functionality on filter slots to increase / decrease the amount, replaced that
  functionality with a dialog (raoulvdberge)
- Fixed not being able to craft upgrades that require enchanted books (raoulvdberge)
- Fixed quick jittering of the Grid and Crafting Monitor when opening them because the tabs appear (raoulvdberge)

### 1.6.3

- Fixed crash with Wireless Fluid Grid (raoulvdberge)
- Fixed Reborn Storage crafting being slower than normal (raoulvdberge)
- Re-added a single mode Wrench that can rotate blocks and break Refined Storage covers (raoulvdberge)

### 1.6.2

- Fixed Grid searching not working (raoulvdberge)

### 1.6.1

- Added fluid autocrafting (raoulvdberge)
- Added Crafting Upgrade support for fluids on the Exporter, Constructor and Fluid Interface (raoulvdberge)
- Added config option to hide covers in the creative mode tabs and JEI (raoulvdberge)
- The Portable Grid now supports fluid disks (raoulvdberge)
- Filters now support fluids and can be inserted in the Fluid Grid (raoulvdberge)
- Removed "emit signal when item is being autocrafted" option in the Detector (raoulvdberge)
- The Crafting Upgrade no longer schedules requests when there are items or fluids missing (raoulvdberge)
- You can now keep fluids in stock by attaching a External Storage in fluid mode to a Fluid Interface with a Crafting
  Upgrade (raoulvdberge)
- You can now specify the amount to export in the Fluid Interface (raoulvdberge)
- Made the Crafting Preview window bigger (raoulvdberge)
- Fixed crash log when opening Pattern Grid GUI (raoulvdberge)
- Fixed being able to put non fluid containers in Fluid Interface input slot (raoulvdberge)
- Fixed Grid filters not updating Grid (raoulvdberge)
- Updated Russian translation (kellixon)
- Overhauled and updated German translation (Cydhra)

### 1.6

NOTE: Worlds that used Refined Storage 1.5.x are fully compatible with Refined Storage 1.6.x and are getting converted
upon loading the world. It is however not possible to revert back to Refined Storage 1.5.x when a world has already been
converted to Refined Storage 1.6.x.

- Removed Regulator mode in the Exporter (raoulvdberge)
- Removed MCMultiPart integration (raoulvdberge)
- Removed Project E integration (raoulvdberge)
- Removed blocking mode in autocrafting (raoulvdberge)
- Removed the Wrench (raoulvdberge)
- Removed "void excess items or fluids" functionality on storages (raoulvdberge)
- Removed the Solderer (raoulvdberge)
- Removed "compare oredict" buttons on Exporter, Importer, etc. (raoulvdberge)
- Removed ConnectedTexturesMod integration for fullbright textures, RS now has fullbright textures natively (
  raoulvdberge)
- Removed autocrafting with fluids (the bucket filling mechanic). This will be replaced in a later version with native
  fluid autocrafting, where Crafters can insert fluids to external inventories (raoulvdberge)
- Added the Cutting Tool (raoulvdberge)
- Renamed "Printed Processors" to "Cut Processors" (raoulvdberge)
- Added covers (raoulvdberge)
- Rewrote autocrafting (raoulvdberge)
- Rewrote network energy storage (samtrion)
- Autocrafting tasks that take longer than 5 seconds to CALCULATE (NOT execute) are automatically stopped to avoid
  server strain (raoulvdberge)
- Added new storage disk system where the storage disk data (items, fluids) are stored off the disk itself, in another
  file (refinedstorage_disks.dat). The disk itself only stores its ID (raoulvdberge)
- Added /createdisk command which creates a disk based on the disk ID. Turn on advanced tooltips to see the disk ID on a
  disk item (raoulvdberge)
- Added config option to configure controller max receive rate (samtrion)
- Added config option to configure energy capacity of Refined Storage items (raoulvdberge)
- Added config option to change Reader / Writer channel energy capacity (raoulvdberge)
- Added a fully charged regular Controller to the creative menu (raoulvdberge)
- The Controller item now shows a durability bar for the energy (raoulvdberge)
- Changed fluid storage progression to be 64k - 256k - 1024k - 4096k (raoulvdberge)
- You can no longer put a Filter in filter slots to gain additional filter slots (raoulvdberge)
- You can now re-insert Processing Patterns in the Pattern Grid and have the inputs and outputs be completed (
  raoulvdberge)
- Fixed bug where pattern was recipe pattern was creatable when there was no recipe output (raoulvdberge)
- Fixed a crash when breaking an Ender IO conduit with the Destructor (raoulvdberge)
- Fixed bug where storage disks in Portable Grids could be moved into themselves (raoulvdberge)
- Fixed the Crafter crashing when opening it while connected to a Primal Tech Grill or Kiln (raoulvdberge)
- Fixed bug where Crafting Upgrade on Interface kept too many items in stock (raoulvdberge)
- Fixed bug where External Storage could only handle 1 fluid inventory per block (raoulvdberge)
- Fixed shift clicking a created pattern going into Grid inventory (raoulvdberge)
- Fixed crash when moving a wireless item with the number keys (raoulvdberge)
- Fixed bug where item storage tracker didn't save sometimes (raoulvdberge)
- Fixed bug where External Storage doesn't detect new inventory when rotating (raoulvdberge)
- Fixed JEI recipe transferring in Pattern Grid allowing non-processing recipes in processing mode and vice-versa (
  raoulvdberge)
- Fixed using Interfaces for minimum stock levels failing when requester is also an Interface (raoulvdberge)
- Fixed ItemZoom incompatibility in Grid and crafting preview window (raoulvdberge)
- Fixed shift clicking upgrades into Interface making upgrades go to import slots (raoulvdberge)
- Fixed duplication glitch with storages (raoulvdberge)
- Prevent accidental Grid scrollbar click after clicking JEI recipe transfer button (raoulvdberge)
- Added a missing config option for Crafter Manager energy usage (raoulvdberge)
- Added support for Disk Drive / Storage Block storage and capacity to OC integration (zangai)
- Added "Search box mode" button to the Crafter Manager (raoulvdberge)
- If an Interface is configured to expose the entire network storage (by configuring no export slots), it will no longer
  expose the entire RS storage, due to performance issues (raoulvdberge)
- The Portable Grid no longer exposes a inventory for crossmod interaction, due to performance issues (raoulvdberge)
- The Crafting Monitor is now resizable and its size can be configured (stretched, small, medium, large) (raoulvdberge)
- The Crafting Monitor now splits its tasks over tabs (raoulvdberge)
- Made all IO blocks have a blacklist instead of a whitelist by default (raoulvdberge)
- An empty blacklist now means: accept any item. An empty whitelist now means: don't accept any item (an empty whitelist
  USED to mean: accept any item) (raoulvdberge)
- The Importer now skips over empty slots (raoulvdberge)
- The Exporter now round-robins over every configured item or fluid to export instead of exporting them all at once (
  raoulvdberge)
- Updated Russian translation (kellixon)

### 1.5.34

- Allow crafters to be daisy-chained (tomKPZ)
- Empty patterns can no longer be inserted in the pattern result slot in the Pattern Grid with hoppers (raoulvdberge)
- Added OR search operator to the Grid with "|" (raoulvdberge)
- getPatterns() now only returns all the outputs, this to limit memory usage in OpenComputers (only affects OC
  integration). (fspijkerman)
- Added new getPattern(stack:table) function for OpenComputers integration (fspijkerman)
- Fixed repeated key events not getting handled in some cases (tomKPZ)

### 1.5.33

- Added Crafter Manager (raoulvdberge)
- Patterns in the Crafter slots now automatically render the output without pressing shift (raoulvdberge)
- Fixed Disk Manipulator not extracting items (ineternet)
- Fixed filter slots not caring about max stack size (raoulvdberge)
- Fixed model warning about Portable Grid (raoulvdberge)
- Fixed crash when autocompleting Ender IO recipes from JEI (raoulvdberge)
- Fixed Grid not always using all combinations when using JEI autocompletion (raoulvdberge)
- Increased Grid performance (raoulvdberge)
- Various internal refactors (raoulvdberge)

### 1.5.32

- Added Spanish translation (Samuelrock)
- Fixed issue where the Pattern Grid can only overwrite patterns when blank ones are present (ineternet)
- Fixed not being able to extract half a stack of items with max stack size 1 in Grid when using right click (
  raoulvdberge)
- Fixed 2 same stacks using capabilities without NBT tag not treated equal (raoulvdberge)
- Changed stack quantity of craftable items from 1 to 0 to fix Quantity Sorting (ineternet)
- Changed fluid stack amount to not display "0" anymore (ineternet)
- Fixed NBT/metadata check on exporting in an Interface (ineternet)
- Fixed Disk Manipulator being stuck on unemptiable, non-empty disks (ineternet)
- Fixed orientations of the Portable Grid (TeamSpen210)
- Fixed crafting event in Crafting Grid being fired twice (raoulvdberge)
- Fixed a crash when the Constructor tries to place a block when a multipart is attached to it (raoulvdberge)
- Fixed an autocrafting crash (raoulvdberge)
- Attempted to fix FPS drop on Grid sorting (raoulvdberge)
- Disk Manipulator in fluid mode will now extract a bucket at a time instead of 1 mB (or 64 buckets at a time with a
  Stack Upgrade instead of 64 mB) (raoulvdberge)

### 1.5.31

- Improved the "cannot craft! loop in processing..." error message (raoulvdberge)
- Fixed error logs when toggling the Pattern Grid from and to processing mode (raoulvdberge)
- Fixed pattern slots in Crafters not being accessible (raoulvdberge)
- Fixed rare Grid crash (raoulvdberge)
- Fixed OpenComputers cable showing up in Grid as air (raoulvdberge)
- Storage disk and block stored and capacity counts are formatted now in the tooltip (raoulvdberge)
- Made the Disk Manipulator unsided (inserting goes to insert slots and extracting from output slots) (raoulvdberge)

### 1.5.30

- Fixed crashing bug when MCMultiPart is not installed (raoulvdberge)

### 1.5.29

- Update Forge to 2577 (minimum Forge version required is now 2555 for MC 1.12.2) (raoulvdberge)
- Fixed bug where MCMP multiparts were blocking RS network connections (raoulvdberge)
- Fixed Reader/Writers for energy extracting energy when not needed (raoulvdberge)

### 1.5.28

- Fixed Writers not pushing energy (raoulvdberge)
- Item Reader/Writers can now store 16 stacks (raoulvdberge)
- Fluid Reader/Writers can now store 16 buckets (raoulvdberge)
- Energy Reader/Writers can now store 16000 FE (raoulvdberge)

### 1.5.27

- Fixed non-oredict patterns not consuming resources (raoulvdberge)

### 1.5.26

- Added Funky Locomotion integration (raoulvdberge)
- Minor fixes to autocrafting (raoulvdberge)
- Fixed Exporter in Regulator Mode not regulating properly when same item is specified multiple times (raoulvdberge)
- Fixed air appearing in Grid (raoulvdberge)
- Fixed config categories not correctly appearing in ingame config GUI (raoulvdberge)
- Fixed craftable items showing "1 total" if not stored in system in Grid (raoulvdberge)
- Removed "detailed" Grid view type variant, made detailed tooltips a config option instead (raoulvdberge)

### 1.5.25

- Fixed not being able to autocraft different Storage Drawers' wood drawers (raoulvdberge)
- Fixed not being able to autocraft certain Modular Routers items (raoulvdberge)
- Fixed last modified date not being sent when extracting from an External Storage (raoulvdberge)

### 1.5.24

- The Grid now displays last modified information (player name and date) and size on tooltips of stacks (raoulvdberge)
- Added "Last modified" sorting option in the Grid (raoulvdberge)
- Added a "detailed" variant for every Grid view type option, to disable the modified information on the tooltip (
  raoulvdberge)
- Removed craft-only mode for the Exporter (raoulvdberge)
- Fixed Exporter with Stack Upgrade not working correctly in Regulator Mode (raoulvdberge)
- Fixed crash with the Constructor (raoulvdberge)
- Fixed patterns being able to crash when no inputs are provided (raoulvdberge)
- Fixed possible crash with network scanning (raoulvdberge)

### 1.5.23

- Fixed duplication bug with autocrafting (raoulvdberge)
- Fixed Fluid Interface with Stack Upgrade not exporting fluids (raoulvdberge)
- Fixed fluids in Fluid Grid not showing actual mB on tooltip when pressing CTRL + SHIFT (raoulvdberge)

### 1.5.22

- Added oredict, blocking, processing, ore inputs access to OpenComputers API (raoulvdberge)
- Added shortcut to clear Grid crafting matrix (CTRL+X) (raoulvdberge)
- The Crafter can now only store 1 stack size pattern per slot (raoulvdberge)
- You can now re-insert a Pattern in the pattern output slot in the Pattern Grid to modify an existing pattern (
  raoulvdberge)
- Fixed not being able to use JEI R and U keys on Grid with tabs (raoulvdberge)
- Fixed lag when opening a Grid with lots of items by offloading the grid sorting to another thread (raoulvdberge)
- The Refined Storage jar is now signed (raoulvdberge)
- Updated Chinese translation (TartaricAcid)
- Performance improvement when adding patterns to the network (xinyuan-liu)

### 1.5.21

- Updated Portuguese (Brazilian) translation (Pinz714)
- Fixed crash with External Storage (raoulvdberge)
- Fixed stack-crafting in the crafting grid (crafting table) causing lag on a dedicated server (Lordmau5)
- Fixed cable blocks, Wireless Transmitter, Detector and Portable Grid acting as full blocks (being able to place
  torches on them etc) (raoulvdberge)

### 1.5.20

- Restore MC 1.12.0 compatibility (raoulvdberge)

### 1.5.19

- Updated Forge to 2493 (MC 1.12.2) (raoulvdberge)
- Fixed Refined Storage blocks requiring a pickaxe to be broken (raoulvdberge)
- Fixed Grid GUI crash (raoulvdberge)
- Fixed device names overflowing Controller GUI (raoulvdberge)
- Fixed high CPU load when Refined Storage GUIs are open (raoulvdberge)
- Fixed not being able to extract Mekanism tanks and bins from the Grid (raoulvdberge)
- Fixed not being able to craft Immersive Engineering Revolver (raoulvdberge)
- Fixed rare bug when server crashes on startup due to network node not existing (raoulvdberge)

### 1.5.18

- Added Project E integration for the External Storage on the Transmutation Table (raoulvdberge)
- Added Project E integration for the energy values of Solderer items (raoulvdberge)
- Added support for more than 4 grid tabs in the Grid by putting filters IN filters (raoulvdberge)
- Added protection for other mods causing crashes when drawing an item or display name (raoulvdberge)
- Fixed network not disconnecting when Controller is broken (raoulvdberge)
- Fixed bug where when multiple Fortune Upgrades are inserted, it chooses the first Fortune Upgrade instead of the
  highest one (raoulvdberge)
- Fixed some translations having too big "Craft" text (raoulvdberge)
- Fixed crash with GUI when toggling the Grid size quickly (raoulvdberge)
- Fixed scrollbar not scrolling correctly when clicked with mouse when grid tabs are visible (raoulvdberge)
- Fixed Reader and Writers GUIs still displaying channels even if not connected (raoulvdberge)
- Fixed Solderer resetting progress when the inventory changes (raoulvdberge)
- Reader and Writer blocks now face the block you're placing it on, not the player (raoulvdberge)
- The Fortune Upgrade doesn't use NBT anymore to store the fortune level (raoulvdberge)
- Pressing SHIFT over an item in the Grid will no longer display the full unformatted count, instead, use CTRL + SHIFT
  and it will be displayed in the tooltip (raoulvdberge)

### 1.5.17

- Re-added support for OpenComputers (raoulvdberge)
- Fixed crash with Grid (raoulvdberge)

### 1.5.16

- Fixed crash when placing a Controller (raoulvdberge)
- Fixed crash when configuring an Exporter (raoulvdberge)
- Fixed Refined Storage not running in MC 1.12 and only on MC 1.12.1 (raoulvdberge)

### 1.5.15

- Updated Forge to 2444 (MC 1.12.1) (raoulvdberge)
- Added InventoryTweaks Grid sorting (cooliojazz)
- Added InventoryTweaks inventory sort ability in Refined Storage GUIs (raoulvdberge)
- Added CTM integration for Disk Manipulator (raoulvdberge)
- Fixed possible rare dupe bug with Importer (raoulvdberge)
- Fixed Shulker Box dupe bug with Destructor (raoulvdberge)
- Fixed Grid crash with search history (raoulvdberge)
- Fixed Grid crash with search field (raoulvdberge)
- Fixed External Storage not working without Storage Drawers (raoulvdberge)
- Fixed External Storage not calculating max stack size in the calculation of it's capacity display in the GUI (
  raoulvdberge)
- Fixed Refined Storage not drawing small text correctly with Unicode font (raoulvdberge)
- Fixed dupe bug with External Storage connected to an item handler (raoulvdberge)

### 1.5.14

- Updated Forge to 2426 (raoulvdberge)
- Updated French translation (cylek56)
- Fixed more crashes relating to scrollbar in GUIs (raoulvdberge)
- Fixed crash with Detector (raoulvdberge)
- Fixed bug where pattern create button wasn't visible when grid tabs were selected (raoulvdberge)
- Fixed performance issue with Controllers turning off and on and Interfaces (raoulvdberge)
- Fixed Interfaces exposing network inventory don't hide storages that are disconnected (raoulvdberge)
- Added config option to modify the Solderer speed per Speed Upgrade, defaulting to 22.5% faster per upgrade, making it
  90% faster on a fully upgraded Solderer (raoulvdberge)
- Added CTM integration (raoulvdberge)

### 1.5.13

- Fixed Wireless Fluid Grid not using up energy (raoulvdberge)
- Fixed Wireless Crafting Monitor remaining in network item list (raoulvdberge)

### 1.5.12

- The Network Transmitter now uses 1 FE/t per block instead of 4 FE/t (raoulvdberge)
- Added additional API for grids (raoulvdberge)

### 1.5.11

- Fixed not being able to smelt quartz into silicon (raoulvdberge)
- Fixed Grid extracting wrong enchanted books (raoulvdberge)

### 1.5.10

- Converted Solderer recipes to JSON (raoulvdberge)
- Implemented controller update throttling, should fix lag issues with controllers that constantly turn off and on (
  raoulvdberge)
- Fixed crash relating to MCMP (raoulvdberge)

### 1.5.9

- Fixed not being able to extract anything when connecting an External Storage to Storage Drawers (raoulvdberge)

### 1.5.8

- Updated Forge to 2400 (raoulvdberge)
- Updated Storage Drawers API (raoulvdberge, jaquadro)
- Fixed bug where disks have to be re-inserted in the Disk Drive in order to work again after rejoining a chunk (
  raoulvdberge)
- Fixed bug where items inserted in Storage Drawers through External Storage with a Drawer Controller wouldn't respect
  drawer priority rules (raoulvdberge, jaquadro)
- Fixed crash on server when getting an advancement (raoulvdberge)
- Removed migration support for worlds from MC 1.10.2. To migrate your world to MC 1.12 from MC 1.10.2, first open it in
  MC 1.11.2 (raoulvdberge)
- Autocrafting can now fill water bottles with water from the fluid storage - regular bottles or pattern for regular
  bottles are required (raoulvdberge)

### 1.5.7

- Exposed pattern inventory for Pattern Grid (raoulvdberge)
- Fixed crashes relating to scrollbar in GUIs (raoulvdberge)
- Added advancements (raoulvdberge)

### 1.5.6

- Updated Forge to stable 2387 (raoulvdberge)
- Fixed bug where players couldn't place regular blocks next to secured networks (raoulvdberge)
- Removed Processing Pattern Encoder, that functionality is now available in the Pattern Grid (raoulvdberge)

### 1.5.5

- Updated Forge to 2363 (raoulvdberge)

### 1.5.4

- Fixed External Storage crashing (raoulvdberge)
- Fixed crash when node data mismatches between world and dat file (raoulvdberge)

### 1.5.3

- Updated Forge to 2359 (raoulvdberge)
- Updated MCMultiPart to 2.2.1 (raoulvdberge)
- Fixed Solderer crashing (raoulvdberge)
- Fixed Solderer being able to work with insufficient ingredients (raoulvdberge)
- Fixed Interface extracting from itself when trying to keep items in stock (raoulvdberge)
- Fixed Quartz Enriched Iron recipe only giving 1 instead of 4 (jhjaggars)
- Fixed Debug Storage disks not working correctly (raoulvdberge)
- Fixed Disk Drive giving incorrect capacity for creative and debug storage disks (raoulvdberge)
- The Portable Grid now exposes an inventory for interaction with other mods or vanilla (raoulvdberge)
- The Interface now exposes the entire storage inventory (if no slots are set for exporting) for interaction with other
  mods or vanilla (raoulvdberge)
- The Relay now reacts instantly to a redstone signal again, removed throttling for it (raoulvdberge)

### 1.5.2

- Fixed a bug where loading nodes would abort when a single node has an error while reading (raoulvdberge)
- Fixed Filters not persisting correctly in Portable Grid (raoulvdberge)

### 1.5.1

- Updated Forge to 2340 (raoulvdberge)
- Re-added MCMultiPart support (raoulvdberge)
- Removed Tesla integration (raoulvdberge)
- Removed RS energy units, the entire mod is powered with Forge Energy now (raoulvdberge)
- Added back crafting recipes (raoulvdberge)
- Changed Grid recipe (raoulvdberge)
- Changed Crafting Monitor recipe (raoulvdberge)
- Fixed Filters not persisting correctly in Wireless Grid and Wireless Crafting Monitor (raoulvdberge)
- Fixed Disk Drive recipe not using ore dictionary for chest (raoulvdberge)
- Fixed crash when getting tooltip for grid item (way2muchnoise)

### 1.5

- Port to Minecraft 1.12 (raoulvdberge)
- Removed MCMultiPart support (will be re-added as soon as MCMultiPart for MC 1.12 is available) (raoulvdberge)
- Removed OpenComputers support (will be re-added as soon as OpenComputers for MC 1.12 is available) (raoulvdberge)
- Removed crafting recipes, until Forge adds the recipe system back (raoulvdberge)
- The Portable Grid now doesn't despawn anymore when dropped in the world (raoulvdberge)
- Fixed bug where oredict autocrafting didn't work in some cases (way2muchnoise)

### 1.4.20

- Fixed crash relating to MCMP (raoulvdberge)

### 1.4.19

- Fixed bug where players couldn't place regular blocks next to secured networks (raoulvdberge)
- Fixed crashes relating to scrollbar in GUIs (raoulvdberge)
- Fixed bug where disks have to be re-inserted in the Disk Drive in order to work again after rejoining a chunk (
  raoulvdberge)
- Implemented controller update throttling, should fix lag issues with controllers that constantly turn off and on (
  raoulvdberge)

### 1.4.18

- Fixed Solderer crashing (raoulvdberge)
- Fixed Interface extracting from itself when trying to keep items in stock (raoulvdberge)
- Fixed Solderer being able to work with insufficient ingredients (raoulvdberge)
- Fixed Disk Drive giving incorrect capacity for creative and debug storage disks (raoulvdberge)
- Fixed External Storage crashing (raoulvdberge)
- Fixed crash when node data mismatches between world and dat file (raoulvdberge)
- The Relay now reacts instantly to a redstone signal again, removed throttling for it (raoulvdberge)

### 1.4.17

- Fixed Filters not persisting correctly in Wireless Grid, Wireless Crafting Monitor and Portable Grid (raoulvdberge)
- Fixed Disk Drive recipe not using ore dictionary for chest (raoulvdberge)
- Fixed a bug where loading nodes would abort when a single node has an error while reading (raoulvdberge)

### 1.4.16

- The Portable Grid now doesn't despawn anymore when dropped in the world (raoulvdberge)
- Fixed bug where oredict autocrafting didn't work in some cases (way2muchnoise)

### 1.4.15

- Updated Storage Drawers API, fixes crashes (raoulvdberge)

### 1.4.14

- Fixed Solderer not accepting books made in anvil (raoulvdberge)

### 1.4.13

- Fixed Portable Grid model (raoulvdberge, CyanideX)
- The Portable Grid now has an indicator whether it's connected or disconnected and shows the disk (raoulvdberge,
  CyanideX)
- Fixed ore dictionary causing problems with Solderer (raoulvdberge)
- Fixed ore dictionary items not showing up in JEI for the Solderer (raoulvdberge)
- Removed Quartz Enriched Iron ore dictionary entry (raoulvdberge)

### 1.4.12

- Updated Forge to 2315 (raoulvdberge)
- Updated JEI to 4.5.0 (raoulvdberge)
- Removed Collosal Chests integration (raoulvdberge)
- You can now shift click items from the Grid crafting slots to the player inventory when the Grid is disconnected (
  raoulvdberge)
- Added Korean translation (01QueN10)
- Fixed error logs when watching a Controller when a network changes (raoulvdberge)
- Implemented block update throttling when network turns on and off (raoulvdberge)

### 1.4.11

- Removed debug log configuration option, as it's no longer needed (raoulvdberge)
- Removed "autocraft on redstone signal" option in the Crafter, use an External Storage in combination with an Interface
  with the Crafting Upgrade instead (raoulvdberge)
- Fixed bug where storages that are removed remain visible (raoulvdberge)
- Fixed bug where the GUI didn't close when a block is broken, causing a dupe bug with the Portable Grid (raoulvdberge)
- Added support for External Storage on Interfaces and other Refined Storage blocks, so you can keep items in stock
  easier (raoulvdberge)
- You now have to sneak to place the Portable Grid in the world (raoulvdberge)
- The Machine Casing now requires 1 piece of stone in the middle (raoulvdberge)
- Changed recipe of Disk Drive to no longer require a Solderer (raoulvdberge)
- Changed recipe of Interface to no longer require a Basic Processor, but a Machine Casing instead (raoulvdberge)

### 1.4.10

- Improved performance of network scanning (raoulvdberge)
- Fixed crash when attempting to get direction of a node (raoulvdberge)
- Fixed bug where some network parts don't want to connect to the storage system (raoulvdberge)

### 1.4.9

- Fixed bug where inventory data was lost sometimes upon opening the world (raoulvdberge)

### 1.4.8

- Fixed missing config categories in ingame config (raoulvdberge)
- Fixed Controller not working anymore after changing redstone setting (raoulvdberge)
- Fixed crash when placing or destroying network blocks (raoulvdberge)

### 1.4.7

- Fixed bug where Portable Grid would dupe in inventory (raoulvdberge)
- Worked around an autocrafting bug to prevent crashes (raoulvdberge)
- Added config option "debugLog" that logs diagnostic info to help developers to fix the inventory loss bug, please
  enable it if you are experiencing this issue (raoulvdberge)

### 1.4.6

- Performance improvement to network scanning (raoulvdberge)
- Removed debug output from 1.4.5 (raoulvdberge)
- Fixed Wrench opening GUIs while performing action (raoulvdberge)
- Fixed client Grid GUI clearing and causing crashes after starting an autocrafting request (raoulvdberge)

### 1.4.5

- Updated Forge to 2296 (raoulvdberge)
- Added Portable Grid (raoulvdberge)
- Added OpenComputers integration (thraaawn)
- Fixed Crafting Tweaks buttons positioned wrongly (blay09)
- Fixed Crafting Tweaks keybindings interfering with RS keybindings (blay09)
- Fixed crash when updating storages (raoulvdberge)
- Fixed no tooltips for fluid filter slots (raoulvdberge)
- Fixed Disk Manipulator in fluid mode not showing fluids (raoulvdberge)
- Fixed dupe bug in the Interface (raoulvdberge)
- Removed ticking tile entities, every tile entity in RS is non-ticking now (raoulvdberge)

### 1.4.4

- Updated Forge to 2284 (raoulvdberge)
- Fixed Disk Manipulator crashing due to empty stack (raoulvdberge)
- Fixed issue where empty stacks show up in the system (raoulvdberge)
- Fixed Storage Monitor not respecting security settings (raoulvdberge)

### 1.4.3

- Updated Forge to 2282 (raoulvdberge)
- Updated JEI version (raoulvdberge)
- Updated MCMultiPart version (raoulvdberge)
- Storage Monitors don't render any quantity text when no item is specified to monitor anymore (raoulvdberge)
- Fixed bug where disks in Disk Drive didn't respect access type or void excess stacks option (raoulvdberge)
- Fixed crash in Disk Manipulator (raoulvdberge)
- Fixed oredict not working (raoulvdberge)
- You can now shift click Grid Filters into a Grid instead of manually inserting them (raoulvdberge)
- The Solderer inventory isn't sided anymore (raoulvdberge)
- You can now use up and down arrows to scroll through Grid search history (raoulvdberge)
- Fixed Grid crash (raoulvdberge)
- Fixed Fluid Grid not formatting large quantities correctly (raoulvdberge)
- Small performance improvement: only sort the storages when needed (raoulvdberge)
- Display progress bar on JEI recipes for the Solderer (raoulvdberge)
- Shift clicking patterns in the Pattern Grid now puts the patterns in the pattern slot (raoulvdberge)

### 1.4.2

- Updated Forge to 2261 (raoulvdberge)
- Implemented support for the Forge update JSON system (raoulvdberge)
- Added integration for MCMultiPart, this is an optional dependency (raoulvdberge)
- You can now specify more items to export, import, filter, etc by inserting the Filter item (raoulvdberge)
- Fixed a crash with the Constructor (raoulvdberge)
- Fixed Crafting Pattern model (pauljoda)
- Fixed Quartz Enriched Iron and the block form of it not having an oredictionary entry (raoulvdberge)
- Fixed crash in storage cache (raoulvdberge)
- Fixed slow oredict comparisons causing TPS lag (raoulvdberge)
- The Detector no longer outputs a strong redstone signal (raoulvdberge)
- Made the keybinding to focus on the Grid search bar configurable (way2muchnoise)
- Autocrafting bugfixes (way2muchnoise)
- Fire event on completion of an autocrafting task (way2muchnoise)
- Fire playerCrafting event when shift clicking in the grid (way2muchnoise)
- Fixed controller model warning during launch (raoulvdberge)
- Improved memory usage of some models (raoulvdberge)
- Fixed not rendering some tooltips correctly (raoulvdberge)
- Removed support for the Deep Storage Unit API (raoulvdberge)
- Performance improvements related to storage inserting and extracting (raoulvdberge)
- Fixed crash with External Storage (raoulvdberge)
- Fixed Interface duping items on extract-only storages (raoulvdberge)
- Fixed controls menu showing unlocalized text for Refined Storage keybindings (raoulvdberge)

### 1.4.1

- Added Storage Monitor (raoulvdberge)
- Fixed Processing Pattern Encoder and Security Manager recipes not supporting oredict workbench and chest (VT-14)
- Fixed network nodes not respecting redstone mode (raoulvdberge)
- Fixed "Clear" and "Create Pattern" buttons not working correctly when using Grid Filter tabs (raoulvdberge)
- Fixed Wrench in Dismantling Mode voiding Storage Block contents (raoulvdberge)
- Fixed OPs not having global permissions on secured storage systems (raoulvdberge)
- Fixed crash when Destructor tries to break secured network block (raoulvdberge)
- Fixed Fluid Interface not dropping inventory contents (raoulvdberge)
- Fixed Disk Manipulator crash (raoulvdberge)
- Updated Forge to 2232 (raoulvdberge)

### 1.4

- Added Security Manager (raoulvdberge)
- Added Security Card (raoulvdberge)
- Added Wireless Fluid Grid (raoulvdberge)
- Added craft-only toggle to Exporter (raoulvdberge)
- Added Reader (raoulvdberge)
- Added Writer (raoulvdberge)
- Added blocking mode to patterns in autocrafting (InusualZ)
- Added Grid size toggle (stretched, small, medium, large) (raoulvdberge)
- Added dismantling mode to the Wrench (raoulvdberge)
- Added Block of Quartz Enriched Iron (raoulvdberge)
- You can now bind multiple crafters with the same pattern to machines, to spread or balance out autocrafting (
  way2muchnoise)
- Autocrafting bugfixes (way2muchnoise)
- Fixed Regulator mode not working (InusualZ)
- Fixed Fluid Interface still being sided (raoulvdberge)
- Fixed Constructor not working on Botania flowers (raoulvdberge)
- Fixed Wireless Transmitter working even if it was disabled with redstone mode (raoulvdberge)
- Fixed Solderer not accepting books created in an Anvil (raoulvdberge)
- Fixed bug where network machines stopped working on chunk borders (raoulvdberge)
- Fixed memes not working (raoulvdberge)
- Fixed External Storage crashes (raoulvdberge)
- Fixed Constructor in liquid mode being able to place fluids <1000 mB (raoulvdberge)
- Fixed Solderer recipe conflicts, allowing for easier automation (raoulvdberge)
- Fluid Grid now first tries to get buckets from your inventory instead of the storage (raoulvdberge)
- Remove sidedness from fluid interface (way2muchnoise)
- The Stack Upgrade in a Constructor in item dropping mode will drop stacks of items at a time (raoulvdberge)
- Ignore damage for damageable items when transferring into crafting grid (way2muchnoise)
- Ignore tags from given items when transferring into crafting grid (way2muchnoise)
- Add Ore Dictionary grid filter (use $ as prefix like in JEI) (way2muchnoise)
- Made sure External Storage always has the correct inventory in world (raoulvdberge)
- Using tab in a grid that isn't in autoselected mode will focus on the search box (raoulvdberge)
- Added integration for Collosal Chests for the External Storage, Importer and Exporter improving performance (
  way2muchnoise)
- Exposed the Network Card inventory of the Network Transmitter so other tiles can interact with it (raoulvdberge)
- Increased size of Detector textbox (way2muchnoise)
- Fixed stack upgrades not working in exporter when stack size is 16 (way2muchnoise)
- Fixed crash when rotating External Storage (raoulvdberge)
- Fixed disk textures not working on latest Forge (raoulvdberge)
- Handle breaking and placing blocks better for Constructor and Destructor (way2muchnoise)
- Updated Forge to 2226 (raoulvdberge)
- Updated Chinese translation (TartaricAcid)
- Added filtering slots for the Crafting Monitor (raoulvdberge)
- Added way to hide tasks created in an automated way in the Crafting Monitor (raoulvdberge)
- Added Grid sorting by ID (way2muchnoise)
- Added Solderer particles (raoulvdberge)
- Fixed crash when placing head with Constructor (raoulvdberge)
- Converting blocks instead of ingots to Printed Processors is now a little faster (raoulvdberge)
- Pressing shift while starting a crafting task will skip the crafting preview (raoulvdberge)

### 1.3.5

- Fixed TPS lag on very large crafting tasks (way2muchnoise)
- Fixed not being able to use autocrafting on some EnderIO items (way2muchnoise)
- Fixed server crash with ore dictionary checks (way2muchnoise)
- Fixed Controller not using energy (raoulvdberge)
- Fixed dupe bug when inserting bucket in Fluid Grid (raoulvdberge)
- Fixed not being able to start autocrafting for storage disks (raoulvdberge)
- Fixed oredict button not having the correct position on a small resolution (raoulvdberge)
- Fixed Constructor not using Crafting Upgrade when in item dropping mode (InusualZ)
- Updated French translation (Leventovitch)
- Added regulator mode to Exporter (InusualZ)

### 1.3.4

- Added option to check for oredict in the Grid Filter (raoulvdberge)
- Added option to use a mod filter in the Grid Filter (raoulvdberge)
- Added option to use a whitelist or blacklist in the Grid Filter (raoulvdberge)
- Added Grid tabs using Grid Filters (raoulvdberge)
- The Grid now resizes based on screen size (max rows can be configured) (raoulvdberge)
- Added configuration option to enable large fonts in Grid (raoulvdberge)
- Made Solderer tooltip less big (raoulvdberge)
- Fixed bug with opening a network item with food in offhand (raoulvdberge)
- Fixed not respecting "Extract only" option for storages (raoulvdberge)
- Made the Interface sideless, you can just insert or extract from any side (raoulvdberge)
- Fixed a few autocrafting bugs (way2muchnoise)
- Fixed a crash with the Disk Manipulator (way2muchnoise)

### 1.3.3

- Updated Forge to 2188 (raoulvdberge)
- Fixed not being able to start a crafting task (raoulvdberge)

### 1.3.2

- Fixed being able to exceed max stack size while shift clicking (raoulvdberge)
- Fixed Wrench clearing NBT data when reset causing problems with Morph O Tool (raoulvdberge)
- Fixed client crash when placing network blocks (raoulvdberge)

### 1.3.1

- Updated Forge to 2180 (raoulvdberge)
- Made Upgrades stackable (raoulvdberge)
- Fixed Disk Drive not noticing a Storage Disk being shift clicked out of the GUI (raoulvdberge)

### 1.3

- Port to Minecraft 1.11 (raoulvdberge, way2muchnoise)
- Removed RF support, use Forge Energy instead (raoulvdberge)
- Removed IC2 support (raoulvdberge)
- Removed MCMultiPart support (will be re-added as soon as MCMultiPart for MC 1.11 is available) (raoulvdberge)

### 1.2.26

- Fixed Interface duping items on extract-only storages (raoulvdberge)
- Fixed calculating crafting resources for more than 9 unique items, for addon mods (ExpensiveKoala)

### 1.2.25

- Fire event on completion of an autocrafting task (way2muchnoise)
- Fire playerCrafting event when shift clicking in the grid (way2muchnoise)
- Allow INodeNetwork instances to return an ItemStack for display in Controller GUI (bmwalter68)

### 1.2.24

- Made the keybinding to focus on the Grid search bar configurable (way2muchnoise)
- Autocrafting bugfixes (way2muchnoise)

### 1.2.23

- Implemented support for the Forge update JSON system (raoulvdberge)
- Fixed crash in storage cache (raoulvdberge)
- Fixed Crafting Pattern model (pauljoda)
- Fixed Constructor not working on Botania flowers (raoulvdberge)
- Fixed Disk Manipulator crash (raoulvdberge)
- Fixed slow oredict comparisons causing TPS lag (raoulvdberge)
- The Detector no longer outputs a strong redstone signal (raoulvdberge)

### 1.2.22

- Fixed recipe for Processing Pattern Encoder not using oredictionary for the workbench (VT-14)
- Fixed Fluid Interface not dropping inventory contents (raoulvdberge)
- Fixed glitchy upgrade recipes in the Solderer (raoulvdberge)

### 1.2.21

- Fixed crash when placing head with Constructor (raoulvdberge)

### 1.2.20

- Fixed client side crash with cables (raoulvdberge)
- Added Solderer particles (raoulvdberge)
- Added Grid sorting by ID (way2muchnoise)

### 1.2.19

- Added integration for Collosal Chests for the External Storage, Importer and Exporter improving performance (
  way2muchnoise)
- Exposed the Network Card inventory of the Network Transmitter so other tiles can interact with it (raoulvdberge)
- Increased size of Detector textbox (way2muchnoise)
- Autocrafting bugfixes (way2muchnoise)
- Fixed stack upgrades not working in exporter when stack size is 16 (way2muchnoise)
- Fixed crash when rotating External Storage (raoulvdberge)
- Handle breaking and placing blocks better for Constructor and Destructor (way2muchnoise)
- Updated cable part back texture and Construction and Destruction Core textures (CyanideX)
- Updated Forge to 2221 (raoulvdberge)
- Fixed disk textures not working on latest Forge (raoulvdberge)
- Updated Chinese translation (TartaricAcid)

### 1.2.18

- Performance improvements with oredict autocrafting (way2muchnoise)
- Fixed client side crash with cable (raoulvdberge)
- Fixed client side crash with disk drive (raoulvdberge)
- Fixed crash with external storage in fluid mode (raoulvdberge)
- Fluid Grid now first tries to get buckets from your inventory instead of the storage (raoulvdberge)

### 1.2.17

- Ignore damage for damageable items when transferring into crafting grid (way2muchnoise)
- Ignore tags from given items when transferring into crafting grid (way2muchnoise)
- Remove sidedness from fluid interface (way2muchnoise)
- Using tab in a grid that isn't in autoselected mode will focus on the search box (raoulvdberge)
- Add Ore Dictionary grid filter (use $ as prefix like in JEI) (way2muchnoise)
- The Stack Upgrade in a Constructor in item dropping mode will drop stacks of items at a time (raoulvdberge)
- Fixed Constructor in liquid mode being able to place fluids <1000 mB (raoulvdberge)
- Fixed Solderer recipe conflicts, allowing for easier automation (raoulvdberge)
- Fixed machines not connecting with cable after Controller (raoulvdberge)

### 1.2.16

- Fixed an autocrafting regression (way2muchnoise)
- Fixed crash with External Storage (raoulvdberge)

### 1.2.15

- Autocrafting bugfixes (way2muchnoise)
- Fixed Grid Filter hiding everything when 2 or more items are in it (raoulvdberge)
- Fixed External Storage crash when breaking a connected inventory (raoulvdberge)

### 1.2.14

- Fixed server crash (way2muchnoise)

### 1.2.13

- Fixed memes not working (raoulvdberge)
- Fixed controller causing network rebuild on every neighbor change (raoulvdberge)
- Autocrafting bugfixes (way2muchnoise)
- Fixed Wireless Transmitter working even if it was disabled with redstone mode (raoulvdberge)
- Fixed Solderer not accepting books created in an Anvil (raoulvdberge)
- Made sure External Storage always has the correct inventory in world (raoulvdberge)

### 1.2.12

- Fixed TPS lag on very large crafting tasks (way2muchnoise)
- Fixed not being able to use autocrafting on some EnderIO items (way2muchnoise)
- Fixed not being able to start autocrafting for storage disks (raoulvdberge)
- Fixed oredict button not having the correct position on a small resolution (raoulvdberge)
- Fixed Constructor not using Crafting Upgrade when in item dropping mode (InusualZ)
- Updated French translation (Leventovitch)

### 1.2.11

- Made the Interface sideless, you can just insert or extract from any side (raoulvdberge)
- The Grid now resizes based on screen size (max rows can be configured) (raoulvdberge, way2muchnoise)
- Added configuration option to enable large fonts in Grid (raoulvdberge, way2muchnoise)

### 1.2.10

- A few autocrafting bugfixes (way2muchnoise)
- Fixed a crash with the Disk Manipulator (way2muchnoise)
- Fixed not respecting "Extract only" option for storages (raoulvdberge)
- Fixed bug with opening a network item with food in offhand (raoulvdberge)
- Fixed other fluid storages going negative when using void excess fluids option (raoulvdberge)
- Made Solderer tooltip less big (raoulvdberge)

### 1.2.9

- Updated Forge to 2185 (raoulvdberge)
- Fixed Wrench clearing NBT data when reset causing problems with Morph O Tool (raoulvdberge)

### 1.2.8

- Fixed autocrafting bugs (way2muchnoise)

### 1.2.7

- Updated German translation (LHS_Buster)
- Fixed not being able to place disks in Disk Drives on servers (raoulvdberge)

### 1.2.6

- Processing patterns can now insert buckets (way2muchnoise)
- Fixed crash with Exporters in fluid mode (raoulvdberge)
- Removed Solderer progress percentage text (raoulvdberge)

### 1.2.5

- The Constructor can now place fireworks (raoulvdberge)
- Added "View Recipes" JEI toggle in Solderer (way2muchnoise)
- Fixed a bunch of autocrafting bugs (way2muchnoise)
- Fixed Grid search not working correctly (raoulvdberge)
- Fixed items disappearing from Solderer inventory (way2muchnoise)
- Fixed being able to take fluids that have less than 1000 millibuckets filled in Fluid Grid (way2muchnoise)
- Fixed Constructor being able to place fluids that have less than 1000 millibuckets (way2muchnoise)
- Fixed Exporter and Importer not working properly with fluids (way2muchnoise)
- Fixed inserting new stack type with right click in Grid causing a desync (raoulvdberge)
- Fixed Constructor not calling block place event (raoulvdberge)
- Fixed shift clicking non disk items in the Disk Manipulator voiding them (way2muchnoise)
- Updated Forge to 2151 (way2muchnoise)

### 1.2.4

- Added tooltip search with # (raoulvdberge)
- Mod search can now also take mod name instead of just id (raoulvdberge)
- Fixed bug where Disk Manipulator doesn't save disks (raoulvdberge)
- Fixed Disk Drive stored quantity GUI text hovering over other text (raoulvdberge)
- Fixed External Storage being in item and fluid mode at the same time (raoulvdberge)
- Fixed Wrench working when player is not sneaking (raoulvdberge)
- Fixed External Storage cache counting items up when extracting (raoulvdberge)
- Fixed External Storage cache not working properly on Compacting Drawers (raoulvdberge)
- Removed ability to put External Storages on Refined Storage network blocks (raoulvdberge)

### 1.2.3

- Fixed fluid cache updating wrongly (raoulvdberge)
- Fixed Exporter scheduling too many crafting tasks (raoulvdberge)

### 1.2.2

- Various autocrafting fixes (way2muchnoise)
- Fixed item voiding when exporting to a chest with a storage in Extract Only mode (raoulvdberge)

### 1.2.1

- Added Wireless Crafting Monitor (with temporary textures) (raoulvdberge)
- Added support for JEI R and U keys in Grids (raoulvdberge)
- Fixed crafting upgrade having weird behavior (raoulvdberge)
- Fixed external storage not updating when loading chunk (raoulvdberge)
- Fixed external storage crash (raoulvdberge)
- Fixed weird autocrafting behavior (way2muchnoise)
- Removed controller explosions when multiple controllers are connected to the same network (raoulvdberge)
- You can now decompose storage disks if the item count is below zero by any chance (raoulvdberge)

### 1.2

- Added new autocrafting system (way2muchnoise)
- Added ore dictionary autocrafting (way2muchnoise)
- Added recipe transfer handler for Processing Pattern Encoder (way2muchnoise)
- Added void excess items functionality to storage blocks (geldorn, raoulvdberge, InusualZ)
- Added config option to configure RS to EU conversion rates (InusualZ)
- Added ability to toggle between insert and extract, only insert and only extract mode in storage blocks (InusualZ,
  raoulvdberge)
- Added Silk Touch Upgrade for Destructor (InusualZ)
- Added Fortune Upgrade for Destructor (InusualZ)
- Added ore dictionary compare toggle to storage I/O blocks (raoulvdberge)
- Added disk leds to Disk Drive block that shows the disks (raoulvdberge)
- Added disk leds to Disk Manipulator block that shows the disks (raoulvdberge)
- Added Wrench, has two modes: configuration saving / reading mode, and rotation mode (raoulvdberge)
- Changed storage GUIs (raoulvdberge)
- Changed default EU conversion rate to be 1:8 with RS (raoulvdberge)
- Controller sorts by energy usage in GUI (highest to lowest) (raoulvdberge)
- The Constructor can now drop items in the world (raoulvdberge)
- The Constructor can now place skulls (modmuss50)
- The Destructor can now pick up items in the world (InusualZ)
- Stack upgrade in Importer / Exporter in fluid mode and Fluid Interface now transfers 64 buckets at once (raoulvdberge)
- Detector without any filter will detect based on total items or fluids stored (raoulvdberge)
- Storage disks and storage blocks now don't despawn anymore when dropped in the world (raoulvdberge)
- Grid item and fluid quantity now only rounds to 1 digit after comma (raoulvdberge)
- Items count can no longer overflow, and will max out at the maximum integer value (raoulvdberge)
- Limited network transmitter usage to 1000 RS/t (raoulvdberge)
- Fixed lag issues caused by External Storage (raoulvdberge)
- Fixed resetting a stack of patterns yields 1 blank pattern (raoulvdberge)
- Fixed being able to pipe items in the export slots of the Interface (InusualZ)
- Fixed Interface being stuck when item isn't accepted in storage (InusualZ)
- Fixed items with colored name being uncolored in Grid (raoulvdberge)
- Fixed fluid rendering bugging out side buttons (raoulvdberge)
- Fixed item count going negative when using the Disk Manipulator (InusualZ)
- Fixed Storage Drawer quantities not updating properly on Void Drawers (geldorn)
- Fixed Disk Manipulator blocking items transferring in some cases (InusualZ)
- Fixed External Storage crafting recipe not supporting ore dictionary chests (raoulvdberge)
- Fixed when shift clicking crafting recipe and inventory is full items are dropping on the ground instead of going in
  the system (raoulvdberge)
- Fixed glitchy rendering of cable parts in item form (raoulvdberge)
- Fixed Destructor being able to break bedrock (InusualZ)
- Fixed External Storage thinking that items are inserted in Extra Utilities Trash Cans (InusualZ)
- Fixed Grid quantities being unreadable when using unicode font (raoulvdberge)
- Fixed disconnecting when Storage Disk or Storage Block is too big (raoulvdberge)
- Updated Storage Drawers API (raoulvdberge)

### 1.1.3

- Fixed some clients not starting up due to too many Disk Drive model permutations (raoulvdberge)

### 1.1.2

- Added recipe transfer handler for Solderer (way2muchnoise)
- It is now possible to start a crafting task even if the crafting preview says you can't (raoulvdberge)
- Fixed crash with JEI when changing screens in autocrafting (raoulvdberge)
- Fixed not being able to start autocrafting in other dimensions with Network Transmitter / Network Receivers (
  raoulvdberge)
- Fixed JEI overlay disappearing now and again (raoulvdberge)
- Fixed Detector hitbox (raoulvdberge)

### 1.1.1

- Fixed crash on servers (raoulvdberge)

### 1.1

- New art by CyanideX (CyanideX)
- Added crafting preview screen (way2muchnoise)
- Added max crafting task depth (raoulvdberge)
- Added helpful tooltips to Solderer and Processing Pattern Encoder (raoulvdberge)
- Every machine now compares on damage and NBT by default (raoulvdberge)
- Updated JEI, fixes crashes (way2muchnoise)
- Fixed crash with Disk Manipulator (way2muchnoise)
- Fixed autocrafting not giving back byproducts (raoulvdberge)
- Detector amount text field doesn't autoselect anymore (raoulvdberge)

### 1.0.5

- Fixed crafting a complex item causes the process to flow off the Crafting Monitor's GUI (raoulvdberge)
- Fixed shift clicking from Grid when player inventory is full throwing items in the world (raoulvdberge)
- Importer now takes a Destruction Core, and Exporter a Construction Core (raoulvdberge)
- Added Disk Manipulator (way2muchnoise)
- Added ingame config (way2muchnoise)
- Added the ability to see the output of a Pattern by holding shift (raoulvdberge)
- When a machine is in use by a crafting pattern, inserting of items from other patterns will be avoided (raoulvdberge)
- Exporter in fluid mode and Fluid Interface no longer duplicates fluids that are less than 1 bucket (raoulvdberge)
- Changed default Grid sorting type to quantity (raoulvdberge)
- Updated Dutch translation (raoulvdberge)
- Updated Chinese translation (TartaricAcid)

### 1.0.4

- Fixed lag caused by Crafter (raoulvdberge)

### 1.0.3

- Fixed item loading issue (raoulvdberge)
- Added integration for Forge energy (raoulvdberge)
- Solderer now accepts items from any side, allowing easier automation (raoulvdberge)
- Solderer is now intelligent about items in slots, and will only accept an item if it is part of a recipe (
  raoulvdberge)
- Changed recipe for upgrades in the Solderer, they now just take 1 of the unique item instead of 2, using redstone
  instead (raoulvdberge)
- Fixed fluid autocrafting scheduling too much crafting tasks for buckets (raoulvdberge)
- Fixed blocks in hand facing wrong direction (raoulvdberge)
- Updated to Forge 2088 (raoulvdberge)

### 1.0.2

- Fixed processing patterns not handling item insertion sometimes (raoulvdberge)
- Removed crafting task limit in crafting start GUI (raoulvdberge)
- +64 in crafting start GUI now gives 64 from the first time instead of 65 (raoulvdberge)

### 1.0.1

- Fixed advanced tooltips showing in Grid when not configured to do so (raoulvdberge)
- Added "autocrafting mode" in Detector, to check if an item is being crafted. If no item is specified, it'll emit a
  signal if anything is crafting (raoulvdberge)
- Added an option for the Crafter to trigger autocrafting with a redstone signal (raoulvdberge)
- Optimized crafting pattern loading (raoulvdberge)
- Updated to Forge 2084 (raoulvdberge)

### 1.0

**NOTE:** Due to the new crafting system, all Crafting Patterns made before 1.0 have to be re-made.

- Implemented multithreaded autocrafting (raoulvdberge)
- Processing patterns now hold their items back for pushing until all the required items are gathered from the system (
  raoulvdberge)
- Fixed item and fluid storage stored count having incorrect values at times (raoulvdberge)
- Reworked Crafting Monitor GUI (raoulvdberge)
- Fixed problems relating to Crafting Upgrade (scheduling a task wrongly, blocking other tasks, etc) (raoulvdberge)
- Interface now supports Crafting Upgrade (raoulvdberge)
- When shift clicking a recipe in the Crafting Grid, the player inventory is now leveraged as well (raoulvdberge)
- Fixed machines breaking on long distances (raoulvdberge)
- Fixed Controller rebuilding network graph on energy change (raoulvdberge)
- Fixed fluids not caring about NBT tags (raoulvdberge)
- Fixed fluids that have less than 1 bucket stored render only partly in Fluid Grid (raoulvdberge)
- Fixed Fluid Interface voiding bucket when shift clicking to out slot (raoulvdberge)
- Fixed wrong machine connection logic (raoulvdberge)
- Updated to Forge 2077 (raoulvdberge)

### 0.9.4

- Little fixes in German translation (ThexXTURBOXx)
- Fixed mod not working without JEI (raoulvdberge)
- Reverted network changes that caused buggy behavior (raoulvdberge)
- Reduced explosion radius when multiple controllers are connected to the same network (raoulvdberge)

### 0.9.3

- Updated German translation for Fluid Storage (0blu)
- Updated Dutch translation for Fluid Storage (raoulvdberge)
- Added Chinese translation (TartaricAcid)
- Added Crafting Tweaks integration (blay09)
- Reworked storage network code, should fix weird machine disconnection issues (raoulvdberge)
- Fixed that the Fluid Storage Disk recipe returns an invalid disk (raoulvdberge)

### 0.9.2

**Bugfixes**

- Fixed not being able to take out items from Wireless Grid cross-dimensionally

### 0.9.1

**Bugfixes**

- Fixed server crash with Grid

### 0.9

**Bugfixes**

- Fixed crash with Grid
- Fixed Grid Filter only updating the Grid when reopening the GUI
- Fixed Wireless Grid not working cross dimensionally
- Fixed Grid not displaying items after changing redstone mode
- Fixed Wireless Transmitter crashing when it is transmitting to a removed dimension
- Fixed disassembling stacked Storage Blocks only returns 1 set of items
- Priority field and detector amount field can now display 4 digits at a time

**Features**

- Added fluid storage
- Added Russian translation by CorwinTheCat
- Energy usage of Wireless Grid is now configurable
- Wireless Transmitters can now only be placed on Cable

### 0.8.20

**Bugfixes**

- Fixed crash with Grid

### 0.8.19

**Bugfixes**

- Fixed item duplication bug with External Storage
- Fixed External Storage taking too long to update storage
- Fixed crash with Grid
- Fixed crash when shift clicking unsupported item in a slot

### 0.8.18

**Bugfixes**

- Fixed Detector mode not persisting
- Fixed bug where scrollbar didn't scroll correctly and thus hiding some items
- Fixed Network Transmitter not dropping inventory when broken

### 0.8.17

**Bugfixes**

- Fixed Grid causing sorting lag on the client

### 0.8.16

**Bugfixes**

- Fixed issue with IC2 integration causing console spam
- Slight performance increase and network efficiency improvement in all GUI's
- Slight performance increase in Grid GUI
- Fixed not being able to change some configs in blocks
- Fixed serverside configs not syncing up with clientside
- Fixed not being able to move inventory items in Grid GUI's to hotbar via the number keys
- Fixed Relays when being in "Ignore Redstone" mode using up energy
- Fixed Crafter facing bottom side on placement
- Improved collisions of Cable parts
- You now have to click the actual cable part head in order to get the GUI open

**Features**

- Added German translation by ChillUpX
- Grid Filters can now only filter 9 items, but, Grids take 4 filters now instead
- Grid Filters can now be configured to compare on NBT and/ or damage
- It is now possible to shift click items to the Storage Device filters
- Updated to Forge 2046
- Updated Tesla
- Java 8 is now a requirement
- Added MCMultiPart integration for Cable Parts

### 0.8.15

**Bugfixes**

- Fixed server startup crash

### 0.8.14

**Features**

- Added Interdimensional Upgrade so the Network Transmitter can work over different dimensions

### 0.8.13

**Bugfixes**

- Fixed rendering crash with Disk Drive
- Fixed crash when quickly toggling sorting direction in Grid
- Fixed not being able to clear exporter row in interface

**Features**

- Added config option to set the base energy usage of the Controller (default is 0)
- Added Grid Filter item to filter items in any Grid
- Added support for processing patterns with big stacksizes
- Added Network Transmitter, Network Receiver and Network Cards
- The slot where the Wireless Grid is in in the Wireless Grid GUI is now disabled, so the item can't be thrown out of
  the inventory by accident
- Changed Relay recipe to use redstone torch instead of Basic Processor
- Placed machines now face the block they are placed on, like hoppers

**NOTE:** Config change: the config options for the energy capacity of the Controller and wether the Controller uses
energy are now in a different config category called "controller", if you changed these config options, don't forget the
change it under the new category.

### 0.8.12

**Bugfixes**

- Fixed dupe bug when shift clicking output slot in grid

### 0.8.11

**Bugfixes**

- Fixed minor dupe bug with JEI transferring
- Fixed exporter crafting upgrades taking priority over other tasks
- Solderer upgrades go to upgrades slots first now when shift clicking
- Fixed NPE with incorrectly initialized disks
- Fixed not being able to take out items of Grid 2K16
- Fixed not being able to start autocrafting for certain items (most notably IC2 items)

**Features**

- Added X button to Processing Pattern Encoder to clear configuration of inputs and outputs
- Added Grid view toggle buttons (regular, craftable items only, no craftable items)
- Added ability to shift click items into Importer, Exporter, Constructor, Destructor and Detector to set up whitelist /
  blacklist configurations easier
- Re-added opposite facing on shift click functionality
- Updated to Forge 2014

### 0.8.10

**Bugfixes**

- Fixed not being able to get some items out of Grid
- Fixed slight glitch in Constructor and Destructor model

### 0.8.9

**Bugfixes**

- Fixed bug where Grid crafting doesn't handle remainder sometimes
- Fixed caching issues with External Storage
- Fixed possible crash with Disk Drives

**Features**

- Added a model for the Constructor
- Added a model for the Destructor
- Wireless Transmitters next to each other without any cable or without being connected to a machine won't work anymore,
  they need to be explictly connected to a cable or other machine
- Some models / texture tweaks

### 0.8.8

**Bugfixes**

- Use ore dictionary for recipes with glass
- Fixed solderer not working with automation anymore

**Features**

- Texture tweaks

### 0.8.7

**Bugfixes**

- Improved detector model, add a better hitbox for it
- Improved the Wireless Transmitter texture
- Wireless Transmitter is now only bright red when connected
- Fixed crash with External Storage
- Fixed Detector not unpowering when disconnected from the network
- Made the Solderer beams be bright red when they are working
- Added better hitbox for the Solderer

### 0.8.6

**Bugfixes**

- Fixed External Storage disconnecting on world reload
- Fixed External Storage not updating correctly
- Fixed wireless signal starting from Controller instead of per Wireless Transmitter individually
- Fixed Controller's redstone state not saving
- Fixed crafting tasks not saving properly
- Huge performance improvements to large storage networks

**Features**

- Re-added Controllers exploding when two of them are connected to the same network
- Limited some blocks to only have a direction on the x-axis
- Decreased amount of block updates significantly
- Added new textures
- Added model for External Storage
- Added model for Importer
- Added model for Exporter
- Added model for Detector
- Removed opposite facing on placement mechanic
- Removed Quartz Enriched Iron Block

### 0.8.5

**Bugfixes**

- Fixed crash when Tesla API is not installed

### 0.8.4

**Bugfixes**

- Removed delay until grid items are visible
- Performance improvements

**Features**

- Added a debug storage disk
- Added tooltip to solderer progress bar that shows progress percentage
- Added support for the Tesla energy system
- Added support for the IC2 (EU) energy system
- Added a Portuguese (Brazilian) translation by ChaoticTabris
- Tweaked grid GUI

### 0.8.3

**Bugfixes**

- Fixed drawer controllers not working with external storage
- Fixed right click taking 64 items instead of 32 items

### 0.8.2

**Bugfixes**

- Fixed not being able to take items sometimes

**Features**

- It is now possible to use middle click multiple times for the same item in grid
- Made the mod configurable with a config file

### 0.8.1

**Bugfixes**

- Fixed upgrades from interface not dropping
- Fixed lag caused by constantly rebuilding storage

### 0.8

**Bugfixes**

- Fixed solderer not using extra RF/t with upgrades

**Features**

- Recompile for Minecraft 1.10

### 0.7.19

**Bugfixes**

- Fixed controller being buggy with reconnecting
- Fixed controller texture updating too slow when energy changes
- Fixed not being able to take item from grid at times
- Fixed external storage on storage drawer sending an itemcount of 0 over

### 0.7.18

**Bugfixes**

- Fixed cables sending updates when not needed
- Fixed cables not connecting to foreign machines that implement the API

### 0.7.17

**Bugfixes**

- Fixed getting wrong items back in grid
- Fixed wrong item getting crafted
- Fixed server lag with exporter and importer
- Updated Forge to 1969
- Updated JEI to 3.6.x

**Features**

- New crafting settings gui
- Tweaked some textures

### 0.7.16

**Features**

- Added support for Storage Drawers void upgrade
- Added support for Deep Storage Unit API again

**Bugfixes**

- Fixed NPE in machine searching
- Fixed a bug with interface giving negative amounts of items
- Fixed crash when using scroll wheel

### 0.7.15

**Bugfixes**

- Fixed not being able to scroll with the scroll wheel using MouseTweaks
- Fixed grid search box mode only changing after reopening gui

### 0.7.14

**Bugfixes**

- Updated Forge to build 1965
- Fixed item overflow bug with storage drawers and external storage

**Features**

- Added shift clicking support to every inventory (for upgrades etc)
- Added grid filtering options: @ for searching on mod items, # for searching on tooltips
- Added a way to clear patterns (shift + right click in inventory)
- Tweaked some recipes
- Tweaked energy usage in some machines

### 0.7.13

**Features**

- Added ability to triple click in grid

### 0.7.12

**Bugfixes**

- Fixed creative storage blocks and disks not working
- Fixed interface overflowing

### 0.7.11

**Bugfixes**

- Fixed crash with wireless grid
- Fixed high RF/t usage on external storage
- Fixed that requesting crafting processing task yields too many tasks

**Features**

- Right click on grid search bar clears the search query

### 0.7.10

**Bugfixes**

- Fixed inventories not saving correctly
- Fixed that the player can't shift-click patterns into the last 3 slots of the Crafter

### 0.7.9

**Bugfixes**

- Fixed not being able to place sugar cane
- Fixed not being able to place seeds
- Fixed stacks not splitting between storages correctly
- Fixed storage not saving ItemStack capabilities
- Fixed dropping items into crafting grid with mouse won't work if your mouse is in between items
- Fixed controller still drawing power even if disabled

**Features**

- Added an API
- Added Storage Drawers integration
- Added handling for patterns that return the same item
- Added stack splitting between multiple storages
- Added handling for patterns that give back the same item
- Increased cable recipe to 12 cables

### 0.7.8

**Bugfixes**

- Updated to Forge 1951
- Fixed crash on some worlds
- Improved Grid performance when sorting on quantity

### 0.7.7

**Bugfixes**

- Fixed buggy reequip animation on wireless grid
- Fixed solderer not supporting ore dictionary
- Fixed recipes not supporting ore dictionary
- Fixed destructor not being able to destroy some blocks
- Fixed not being able to place or destroy sugar cane
- Fixed storage blocks not being dismantable
- Fixed getting more items than needed sometimes
- New items now go to the first available storage that has items in it already
- Tweak some recipes
- Performance improvements

**Features**

- Added the Stack Upgrade
- Added Quartz Enriched Iron Block
- Added French translation by Leventovitch

### 0.7.6

**Bugfixes**

- Updated to Forge 1932
- Fixed not being able to start an autocraft

### 0.7.5

**Bugfixes**

- Performance improvements
- Fixed wrong ascending / descending order in Grid
- Fixed autocrafting not giving back byproducts
- Fixed Solderer causing too many chunk updates
- Fixed Solderer slot sides being weird

### 0.7.4

**Bugfixes**

- Updated to Forge 1922
- Performance improvements

### 0.7.3

**Bugfixes**

- Fixed grid performance by not sending grid data so often
- Fixed silicon + quartz enriched iron not having oredict names
- Broke Interface block inventory compatibility: make sure to take all your items out of your Interface blocks before
  you apply the update

**Features**

- Crafting tasks are now sorted from new to old in the Crafting Monitor

### 0.7.2

**Bugfixes**

- Fixed Importer getting stuck on slot

### 0.7.1

**Bugfixes**

- Fixed NPE in some tiles
- Fixed going out of crafting GUI not restoring state (scrollbar and search term)
- Fixed not being able to create a pattern in disconnected Pattern Grid
- Fixed not being able to place cake or string
- Performance improvement to Grids

### 0.7

**Bugfixes**

- Fixed Crafting Grid / Pattern Grid not throwing items on break

**Features**

- Port to Minecraft 1.9.4

### 0.6.15

**Bugfixes**

- Fixed Solderer sides being weird
- Fixed Solderer causing too many block updates

### 0.6.14

**Bugfixes**

- Performance improvements
- Fixed wrong ascending / descending order in Grid

### 0.6.13

**Bugfixes**

- Performance improvements

### 0.6.12

This is a bugfix release containing all fixes from the 1.9.4 version.

**Bugfixes**

- Fixed Crafting Grid / Pattern Grid not throwing items on break
- Fixed NPE in some tiles
- Fixed going out of crafting GUI not restoring state (scrollbar and search term)
- Fixed not being able to place cake or string
- Performance improvement to Grids
- Fixed Importer getting stuck on slot
- Fixed silicon + quartz enriched iron not having oredict names

**Features**

- Crafting tasks are now sorted from new to old in the Crafting Monitor

### 0.6.11

**Bugfixes**

- Fixed crafting patterns crashing when item of an input or output no longer exists
- Fixed Grid letting the current held item flicker
- Fixed Importer / Exporter / External Storage not being able to push or pull out of the other side of a double chest
- Converted all inventories in the mod to Forge's item handler capability system

### 0.6.10

**Bugfixes**

- Fixed Processing Patterns not working
- Fixed not being able to request more than 1 item at once
- Fixed crash with the Solderer
- Increased max crafting request size to 500

### 0.6.9

**Bugfixes**

- Fixed bug where machines wouldn't disconnect / connect when needed outside of chunk
- Fixed not being able to toggle redstone mode in a Wireless Transmitter
- Fixed same machine being connected to the network multiple times
- Fixed External Storage not working
- Reduced network usage

**Features**

- Added automation for the Solderer: every side corresponds to a slot (see the wiki)

### 0.6.8

**Bugfixes**

- Performance improvements
- Fixed CTRL + pick block on machines crashing game

### 0.6.7

**Bugfixes**

- Performance improvements

### 0.6.6

**Bugfixes**

- Fixed being able to insert non-allowed items in inventories with hoppers
- Fixed Processing Pattern Encoder not using up a Pattern

### 0.6.5

**Bugfixes**

- Performance improvements for servers
- Performance improvements for client scrollbars

**Features**

- Updated Forge to build 1907

### 0.6.4

**Bugfixes**

- Performance improvements

### 0.6.3

**Bugfixes**

- Performance improvements

### 0.6.2

**Bugfixes**

- Fixed race condition with crafting tasks
- Fixed pressing escape in crafting settings GUI not going back to grid GUI
- Fixed losing autoselection in Grid when clicking on slot with autoselection mode
- Fixed being able to pick up from pattern result slot
- Added a max crafting quantity per request cap (hardcoded to 100)

**Features**

- Upgrades now draw extra energy

### 0.6.1

**Bugfixes**

- Fixed NPE on world load
- Fixed Destructor crashing when removing a connected machine

### 0.6

**Bugfixes**

- Fixed Destructor not playing block break sound
- Fixed Constructor not playing block place sound
- Fixed picking up from crafting result slot
- Fixed being able to use right click on crafting result slot
- Fixed item duplication issue with the Interface
- Fixed Importers and Exporters not working when changing facing with a wrench
- Fixed Crafting Grid not respecting remainder in recipes
- Fixed Crafting Grid giving back the wrong amount of items when shift clicking
- Fixed items disappearing in Grid when doing a weird combination of inputs
- Fixed Solderer not stacking items
- Fixed Importer voiding Storage Disks from the Disk Drive
- Fixed Controller not saving energy

**Features**

- Added autocrafting
- Massive performance improvements which reduces lag and lets machines connect almost instantly
- Added the Pattern Grid
- Added the Crafting Monitor
- Added the Crafter
- Added the Processing Pattern Encoder
- Added a Pattern item
- Added the Wireless Transmitter
- Added Speed Upgrades which are applicable on a bunch of machines
- Added Range Upgrades for in the Wireless Transmitter
- Added Crafting Upgrades
- Changed Grid modes to have a autoselected option
- Added recipe category -> item JEI integration
- Added Storage Housing

### 0.5.6

**Bugfixes**

- Fixed sorting crash
- Fixed autofocusing on priority field in storage GUIs
- Fixed controller causing lag when energy level changes

### 0.5.5

**Bugfixes**

- Fixed several crashes
- Updated to Forge 1859

**Features**

- Energy level on Controller is maintained

### 0.5.4

**Bugfixes**

- Fixed machines out of the Controller's chunk range only connecting after block break when rejoining the world
- Fixed scrollbar skipping some rows when scrolling with mouse wheel
- Fixed machines from a long distance not being visible in the Controller

**Features**

- Shift clicking on placing Constructor and Destructor will have opposite direction

### 0.5.3

**Bugfixes**

- Fixed not being able to open a Grid that is 256 blocks away from the Controller
- Changed block hardness levels

**Features**

- Made the mod way less network intensive
- Added a Creative Wireless Grid

### 0.5.2

**Bugfixes**

- Items that don't exist anymore, won't be added to storage again to avoid crashes
- Fixed not being able to run the mod without JEI

### 0.5.1

**Bugfixes**

- Fix Disk Drive crashing with an AbstractMethodException

### 0.5

**Bugfixes**

- Fixed clicking sound in Grid
- Fixed a bunch of crashes
- Fixed Exporter not exporting is some cases
- Fixed Importer not importing in some cases
- Fixed Controller drawing RF every 20 ticks instead of every tick
- Fixed not being able to shift click from Crafting Grid crafting slots
- Fixed new items inserted after crafting in Grid being laggy
- Fixed flickering of items in Grid
- Fixed getting a stack of unstackable items from Grid
- Fixed Cable not having a collision box
- Check if the Constructor can actually place said block in the world

**Features**

- New textures
- Updated to the latest Forge and JEI
- Renamed Drives to Disk Drives
- Renamed Storage Cells to Storage Disks
- Removed Wireless Transmitters
- Wireless Grid is now bound to a Controller instead of a Grid
- Drives have a better interface and there are now blacklist and whitelist filters for the Storage Disks in it too.
- Destructors have the ability to whitelist and blacklist certain items now
- Shift clicking stuff in the Interface
- Scrollbar in Grid and Crafting Grid
- Made the normal Grid 1 row larger
- Display of connected machines in the Controller GUI
- Deep Storage Unit integration (with this several barrel mods are now supported too!)
- Machines don't need to be connected with cables anymore, they can be next to each other too
- Made the amount text in the Grid for items smaller
- Nice formatting for items >= 1K (pressing shift while hovering over an item will still display the real item count)
- When placing Importer, Exporter or External Storage with SHIFT, it will have the opposite direction. This is for easy
  placement behind other blocks (furnaces for example)
- Added mass crafting of items with shift in Crafting Grid
- Added JEI recipe transfering in Crafting Grid
- Grid can synchronize with JEI
- Side buttons in machine GUIs are now left, not right

Special thanks to [GustoniaEagle](https://github.com/gustoniaeagle) for the new textures,
and [tomevoll](https://github.com/tomevoll) for providing a bunch of patches and helping me debug some bugs.

### 0.4.1

**Bugfixes**

- Fix ID duplication issues

### 0.4

**Bugfixes**

- Cables now have actual collision
- Fullness percentage in Creative Storage Blocks going under 0%
- The Controller shouldn't display the base usage when not working
- Fix Minecraft reporting that retrieving Grid type fails
- Check isItemValidForSlot on trying to push to inventories

**Features**

- Relays
- Interfaces

### 0.3

- Initial release for Minecraft 1.9
