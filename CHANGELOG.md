## Unique UUID per player

- Each connected player has their own UUID. Before, if two clients sent the same `identity` (or all zeros), they shared a UUID: the player list and the signs got mixed up.
- Order: the client's UUID is used if it is valid, is not all zeros and no other connected player is using it; otherwise, the UUID derived from the name (the same as before); and if that one is taken too, a random one.
- `getUniqueId()` no longer says "or null". Anyone who changes UUID when joining will not be able to edit the signs they placed with the previous one.

## Code structure: IPlayer and BlockNes.java

- `IPlayer.java` (new): the player API for plugins (the " style API (API 1.6)" and "API for plugins" sections) is now declared in an interface. `Player` implements it; no signature changed.
- `Main.java`, `AccessLists.java`, `Achievements.java` and `Setup.java` are merged into `BlockNes.java`. The startup class is `net.BlockNes.BlockNes` (it was `net.BlockNes.Main`), and `AccessLists`, `Achievements` and `Setup` are nested classes.
- Change for plugins: `net.BlockNes.AccessLists` becomes `net.BlockNes.BlockNes.AccessLists`, and `net.BlockNes.Achievements` becomes `net.BlockNes.BlockNes.Achievements`. If a plugin used those classes directly, its import has to change. `server-plugin-api.jar` no longer includes the startup class.

## Console: UUID of each player

- When a player joins, the console shows their UUID the way Minecraft Java does: `UUID of player <name> is <uuid>`, right before the join line and in the same color as that line. The message comes in the server's six languages (key `player.uuid` in `Lang`).

## Console: log colors

- The time and the INFO text (from the server and from plugins) are cyan, like the time.
- `[Server thread/INFO]` goes back to bold white; the color goes on the text.
- The `[Name]` prefix of plugins is cyan at every level. WARNING, ERROR and DEBUG keep their color on the text.
- With `NO_COLOR` or `-Dblocknes.color=false` the console is plain text, and `logs/server.log` is always saved without colors.

## Next version:  events completed

- New  interfaces that were missing: `BlockEvent`, `ChunkEvent`, `LevelEvent`, `LevelInitEvent`, `LevelLoadEvent`, `LevelUnloadEvent`, `InventoryEvent`, `FurnaceBurnEvent`, `FurnaceSmeltEvent`, `PlayerBucketEvent`, `PlayerCreationEvent`, `PlayerGlassBottleEvent`, `ServerEvent`, `EntityCombustEvent`, `EntityLevelChangeEvent`, `EntityMotionEvent`, `EntityEatBlockEvent`, `EntityInventoryChangeEvent`, `CreeperPowerEvent`, `EntityGenerateEvent` and `ItemFrameDropItemEvent`. The last three (and `EntityGenerateEvent`) are not fired yet, because their mechanics are missing.
- Now fired: `LevelInit` (new world), `PlayerCreation`, `EntityLevelChange` (before `PlayerChangeDimension`), `EntityMotion` (pushes on players), `EntityInventoryChange` (inventory slots), `PlayerGlassBottle` (cauldron) and `EntityEatBlock` (cake).
- Furnaces: `FurnaceBurn` and `FurnaceSmelt` are now cancelable, as in  (`burnTime` and `setResult` can be changed).
- `LevelLoad` and `LevelUnload` are `WorldLoad` and `WorldUnload`, with 's name. `LevelUnload` cannot be cancelled.
- The base classes receive the events of their family (for example, listening to `BlockEvent` receives every block event).

# Changelog

## 1.2.0: plugins from source code

### Added

- Plugins without a jar: the server loads plugins straight from `.java` files and compiles them on startup, on `/plugins reload` and on `/plugins enable`. There are two ways: a folder `plugins/<Name>/` with `plugin.properties` and the `.java` files, or a single file `plugins/<Name>.java` (with an optional `<Name>.properties`). Jars keep working the same way. It requires running the server with a JDK.
- The compiled output is stored in `plugins/.cache/<Name>/` and is only rebuilt when a `.java` file changes. If it does not compile, the plugin is not loaded and the console shows the errors (file, line, message).

## 1.2.0: plugin API 1.7 and HUD

### Added

- Plugin API 1.7 (`api=1.7`). The new events, `Plugin.tr(...)` and listening to events by interface are part of API 1.7: a plugin that uses them declares `api=1.7`, and plugins for earlier versions keep loading.
- `Plugin.hudScreenWidth(p)` and `Plugin.hudOffsetY(p)`: read the width and the vertical offset of a player's scoreboard.
- `Start.sh`: installs the server (if needed) and starts it from its folder. `./Start.sh --reinstalar` (or `--reinstall`) builds it again; `JAVA_OPTS` passes options to Java. Requires Java 17 or newer.
- `compiler.sh <plugin folder>`: compiles the plugin against `server/target/server-plugin-api.jar` and writes `plugins/<name>.jar`, with `plugin.properties`, `config.properties` and `lang/` inside.

### Changed

- Version 1.2.0 (it was 1.7.0 beta). The plugin API goes from 1.6 to 1.7.
- HUD: the plugin decides the scoreboard: the corner (`hudPosition`), the vertical offset (`hudOffsetY`) and the width (`hudScreenWidth`, 427 px by default). The `/hud` command was removed. `hud-position`, `hud-offset-y` and `hud-screen-width` were removed from `server.properties`: if you had them, they no longer have any effect.
- `hud-max-lines` stays in `server.properties`: the maximum number of scoreboard lines per player.
- `.gitignore` ignores `worlds/` and `plugins/`, which are server data.

## 1.2.0: Plugin events and messages (second batch)

- New events (from ): `PlayerDamageByMob`, `EntityDamageByChildEntity`, `EntityDamageByBlock`, `EntityCombustByBlock`, `EntityCombustByEntity`, `EntityEffectAdd`, `EntityEffectRemove`, `InventoryPickupArrow`, `EntityArmorChange`, `EntityEatItem`, `EntityDrinkPotion`, `EntityBlockChange`, `SpawnChange`, `LowMemory` and `QueryRegenerate`. `ItemPickup` is also `InventoryPickupItemEvent`.
- The plugin system messages (loading, errors, commands, `/plugins`) follow the server language, in the six languages. The exceptions thrown by the API are in English.
- `tr(key, args...)` in `Plugin`: plugins translate their own messages with `lang/<code>.properties` inside the jar.
- Plugin tests that compared Spanish texts now set the language to Spanish.

## 1.2.0: Plugin events: more events

- New plugin events: `ProjectileHit`, `PlayerBucketFill`, `PlayerBucketEmpty`, `PlayerPickupExpOrb`, `PlayerLogin`, `PlayerUseFishingRod`, `ItemSpawn`, `ItemDespawn` and `RemoteServerCommand` (RCON). Each one has its interface in `net.BlockNes.event.*`.
- Chain TNT now also goes through `ExplosionPrime`: if a plugin cancels it, that TNT does not light and stays where it is.
- `Plugins.md` documents the new events and the ones that are still missing.

## 1.2.0: `/spawn <world>`

### Added

- New command `/spawn <world>` (OP only): if the folder `worlds/<world>` exists, it becomes the normal world without restarting. Everyone in the Overworld goes to its spawn, new players spawn there, and `level-name` in `server.properties` is saved, so it stays after a restart. The old world is saved and kept on disk with its items, mobs, XP, falling blocks and portals. Players in the Nether stay where they are. `/spawn <player>` is still checked first when a player has that name. Not from the Nether.

### Changed

- When the normal world changes, players in the Overworld make a short trip to the Nether and back, so their game clears the old world. That fires the dimension-change event twice.
- Fix: the trip back to the Overworld waits until the client has arrived in the Nether (`PLAYER_SPAWN`). Before, both changes went out in the same tick and the client could keep the Nether sky. If the client does not arrive within 5 seconds, the player goes back anyway. A player who leaves during the trip is saved in the Overworld, at the new spawn.
- New: a `type.txt` file inside `worlds/<name>` sets that world's generator (`flat` or `plano`, `normal`, `nether` or `hell`). The server writes it the first time it uses the world, with the type the world already has (the type of its saved chunks), or `level-type` for a new world. A flat lobby stays flat even when the server's `level-type` is `normal`. Changing `type.txt` to another type moves the saved chunks to `region-<old type>-<date>/` (not deleted) and generates the world again.
- Fix: `level-type` no longer changes a world that already has chunks. Before, a world without `type.txt` took `level-type`, so a normal world could be moved aside and generated again as flat. The server now writes the type of its saved chunks to `region/generador.txt` and no longer guesses it from the terrain on each start.
- Plugins get `WorldUnload` for the old world and `WorldLoad` for the new one when `/spawn <world>` changes it.
- When the normal world changes, beds from the old world are forgotten and anyone asleep wakes up. Players without a bed in the new world appear at its spawn.

## 1.2.0: logs folder and blocknes.yml

### Added

- `blocknes.yml`: advanced settings, created with comments in the server folder on the first start. Restart the server after changing it. A missing setting uses its default value; a wrong value or an unknown key is reported in the console and ignored. Settings: `console.color`, `console.debug`, `logs.max-size-mb`, `saves.autosave-minutes`, `items.despawn-seconds`, `xp-orbs.lifetime-seconds`, `mobs.max-total`, `mobs.hostile-per-player`, `mobs.animals-max` and `mobs.animals-per-player`. The defaults are the values the server already used.

- New command `/clear [player]`: empties the inventory and the armor (OP only).
- New command `/xp <amount>[L] [player]`: gives XP points, or levels with `L` (for example `/xp 5L`). OP only.
- New command `/spawn` (OP only): without a name, it sets the world spawn to the block you are standing on. New players and players without a bed appear there, the spawn is saved with the world, and the spawn protection moves with it. For a lobby, set the spawn on the lobby. `/spawn <player>` takes you to that player. It does not work from the Nether.
- New command `/pos [player]`: shows the coordinates and the dimension. Anyone can see their own; seeing another player's needs OP.
- New command `/difficulty <peaceful|easy|normal|hard>`: changes the difficulty until the next restart. `server.properties` is not changed.
- New commands `/save-on` and `/save-off`: turn the automatic save on or off. `/save-all` still saves everything.

### Changed

- `/help` now lists `/enchant`, `/weather` and `/debug`, which already worked but were missing from the list.
- The log file moves to `logs/server.log`, and the `logs/` folder is created on the first start. A `server.log` and `server.log.old` from earlier versions are moved into `logs/` automatically.
- Autosave, item despawn, XP orb lifetime and the mob limits read their values from `blocknes.yml` instead of fixed numbers in the code.
- `Mobs.MAX` and `ItemEntity.DESPAWN_TICKS` are removed. Their values are the defaults of `mobs.max-total` and `items.despawn-seconds`; plugins that read them should read the settings instead.

## 1.2.0: console

### Changed

- The startup header lines up its values in one column, with a single space after the longest label and no tabs or extra spaces. It is written to `server.log` too. With colors on, the labels are bold and the links are underlined in cyan.
- Errors from `World` (corrupt chunks or `tiles.dat`, failed saves of chunks, tiles and time, and failed folder creation) and from the ender chests went straight to `System.err`, with no time and no `server.log` entry. They now go through the console log like the other errors.

## 1.2.0: falling sand, gravel and anvils

### Changed

- Sand, gravel and anvils fall as entities, following  2.0 (`block/Fallable.php`, `entity/FallingSand.php`). Before, a block without support teleported down in one step. Now it becomes a falling entity that moves with gravity (0.04 per tick) and drag (0.02), so players see it fall.
- The block leaves the world on the entity's first tick, so the sand above starts falling right after it. A column without support collapses from the bottom up, one block per tick.
- Falling blocks pass through air, water and blocks without a collision box (grass, flowers, torches). Landing on water replaces the water. Landing on a non-solid block such as tall grass drops the block as an item and leaves that block in place. Before, sand stopped on top of tall grass.
- Anvils play the fall sound (event 1022) when they land and hurt the players and mobs under them: `(height - 1) * 2` damage, up to 40, as in .
- Falling blocks in the air are saved in `caida.dat` in each world folder and keep falling after a restart.
- One difference from : if the origin block is already gone on the entity's first tick, the entity is removed.  keeps it alive and places an extra block when it lands.
- Blocks with a collision box that is not a full cube (slabs, stairs, fences...) count as full cubes here.  uses their real box, so sand landing on a bottom slab stops at half height there and a full block higher here.

## 1.2.0: startup

### Changed

- Startup opens with a header: the BlockNes version, the client version and the RakNetLib and BlockNes links.
- Startup prepares the spawn in two passes with the same progress lines: the spawn area (`Preparing spawn area: X%`) and then the spawn chunks (`Preparing spawn chunks: X%`). The chunk pass used to run silently for a few seconds.
- The progress counter is shared (`net.BlockNes.util.PassProgress`) by the spawn, chunk and pregeneration passes.

## 1.2.0: rain and fire

### Changed

- Rain (overworld only) keeps mobs from catching fire. Zombies and skeletons don't burn in the sunlight while it rains and burn again once it stops. Skeletons used to burn in the rain too. Lava, fire blocks and Fire Aspect don't set mobs on fire during rain either.
- Rain puts out any mob that is already on fire, animals included.
- Flint and steel does nothing while it rains in the overworld: the player gets a message and the item keeps its durability. In the Nether it works as before.

## 1.2.0: more cancellable plugin events

### Added

- `PlayerAttackEvent` (`Events.PlayerAttack`): fires before a player melee attack hits a player or mob. Plugins can cancel it or modify `damage`; includes the target, entity ID and weapon.
- `VehicleMountEvent` (`Events.VehicleMount`): fires before mounting a boat or minecart; cancelling prevents the mount and leaves any current vehicle state unchanged.
- `VehicleDestroyEvent` (`Events.VehicleDestroy`): fires when a player is about to break a boat or minecart; cancelling preserves the vehicle and its drop.
- `PlayerPickupItemEvent`: standard interface alias for the existing cancellable `Events.ItemPickup` event.


- `ServerStartEvent` (`Events.ServerStart`): fired after startup, once plugins are loaded and the world is ready, just before the network loop.
- `ServerStopEvent` (`Events.ServerStop`): fired at the start of the shutdown sequence, before plugins are disabled.
- `EntityDespawnEvent` (`Events.MobDespawn`): fired when a mob leaves the world (after death, distance cleanup, or shutdown). Informational.
- `SmeltItemEvent` (`Events.SmeltItem`): fired when a furnace finishes smelting one item. Carries the block position, the input and the result. Informational.
- `ConnectionAttemptEvent` (`Events.ConnectionAttempt`): a new session opens, before login.
- `ConnectionCloseEvent` (`Events.ConnectionClose`): a session closes, with or without a player (failed logins included).
- `WorldLoadEvent` (`Events.WorldLoad`): the overworld and the nether finish loading, after plugins are loaded.
- `WorldUnloadEvent` (`Events.WorldUnload`): fired at the start of shutdown, before the worlds are saved.
- `TimeChangeEvent` (`Events.TimeChange`): fired when the time is set with `setTime` (commands, sleeping). The normal clock advance does not fire it.
- `InventoryClickEvent` (`Events.InventoryClick`): a slot change sent by the client.
- `ItemUseEvent` (`Events.ItemUse`): the player uses an item (right-click, placing a block).
- `ArmorEquipEvent` (`Events.ArmorEquip`): the player puts a piece of armor on from the inventory. Armor losing durability does not fire it.
- `PlayerExperienceChangeEvent` (`Events.PlayerExperienceChange`): total XP changes, from gaining XP or spending it at the enchanting table.
- `PlayerLevelChangeEvent` (`Events.PlayerLevelChange`): the player's level changes.
- `PlayerJumpEvent` (`Events.PlayerJump`): heuristic. The server reads no jump packet, so it fires when a player leaves the ground moving up.
- `EntityDamageByEntityEvent` (`Events.EntityDamageByEntity`): a player hits a mob. Only that case for now.
- `EntityMoveEvent` (`Events.MobMove`): a mob changes position on the x/z plane. Only created when a plugin listens, because it would fire every tick.
- `EntityTargetEvent` (`Events.MobTarget`): a hostile mob's target changes (a player, or none).
- `BlockGrowEvent` (`Events.BlockGrow`): crops, stems and saplings grow. `id` and `meta` are the values before growing.
- `BlockUpdateEvent` (`Events.BlockUpdate`): a block is checked after a nearby change. Only created when a plugin listens.
- `BlockRedstoneEvent` (`Events.BlockRedstone`): the redstone power around a block changes.
- `PlayerHungerChangeEvent` (`Events.PlayerHungerChange`): food changes from eating or from losing hunger.
- `ChunkLoadEvent`, `ChunkUnloadEvent`, `ChunkPopulateEvent` (`Events.ChunkLoad`, `Events.ChunkUnload`, `Events.ChunkPopulate`): a chunk enters memory, leaves memory, or finishes generating.
- Aliases, no new firing: `InventoryTransactionEvent` is implemented by `InventoryClick`, `BlockInteractEvent` by `PlayerInteract`, `PacketDecodeEvent` by `PacketReceive`, `PacketEncodeEvent` by `PacketSend`, and `PlayerHandshakeEvent` and `PlayerAuthenticateEvent` by `PlayerPreLogin`.
- `BlockDropItemEvent` (`Events.BlockDropItem`): a block is about to drop its items. The `drops` list is editable: a plugin can remove, replace or add items (for example, dirt drops stone). Fired when a player breaks a block, when a block falls alone, and on explosions (`player` is null in the last two). Cancelable: cancelling it drops nothing.
- `PlayerChangeDimensionEvent` (`Events.PlayerChangeDimension`): a player changes between the overworld and the Nether.
- `LightningStrikeEvent` (`Events.LightningStrike`): lightning strikes at a position.
- `FurnaceBurnFuelEvent` (`Events.FurnaceBurnFuel`): a furnace burns one unit of fuel. `fuel` is the item before it is consumed.
- `PlayerFishEvent` (`Events.PlayerFish`): a player catches a fish. Cancelable: cancelling it gives nothing.
- `VehicleDismountEvent` (`Events.VehicleDismount`): a player gets off a boat or a minecart. `vehicle` is `BOAT` or `MINECART`.
- `EnchantItemEvent` (`Events.PlayerEnchantItem`): a player applies an enchantment at the enchanting table. Reports the item before and after, and the levels spent.
- `BlockSpreadEvent` (`Events.BlockSpread`): grass or mycelium spreads onto dirt, or fire jumps to a neighbouring block. Cancelable: the block does not change. Fields: `sourceX/Y/Z`, `x/y/z`, and `id`/`meta` of the new block.
- `BlockFromToEvent` (`Events.BlockFromTo`): water or lava flows into a neighbouring block or down. Cancelable: the liquid does not flow there. Fields: `sourceX/Y/Z` (the liquid), `x/y/z` (the target), `liquidId` (8 water, 10 lava) and `newMeta`.
- `LeavesDecayEvent` (`Events.LeavesDecay`): leaves with no log nearby decay. Cancelable: they stay.
- `ExplosionPrimeEvent` (`Events.ExplosionPrime`): a TNT block is lit by a player, by fire or by redstone. Cancelable: it stays TNT. `fuse` (ticks) is editable. TNT lit by another explosion (chain reaction) does not fire it yet.
- `EntityShootBowEvent` (`Events.EntityShootBow`, a `PlayerEvent`): a player releases a loaded bow. `force` (0 to 2) is editable. Cancelable: no arrow is shot and neither the arrow nor the bow durability is spent.
- `ProjectileLaunchEvent` (`Events.ProjectileLaunch`): an arrow, snowball, egg, splash potion, experience bottle, fireball or small fireball is launched by a player or a mob. `getProjectileType()` gives the name (`ARROW`, `SNOWBALL`, `EGG`, `POTION`, `EXP_BOTTLE`, `FIREBALL`, `SMALL_FIREBALL`). Cancelable: it is not launched and the player's item is not spent.
- `PlayerAchievementAwardedEvent` (`Events.PlayerAchievementAwarded`): a player earns an achievement for the first time. The server has two so far: `mineWood` (first log) and `diamond` (first diamond ore). Cancelable: it is not recorded and no message is shown.
- `PlayerTextPreSendEvent` (`Events.PlayerTextPreSend`): a chat or command text is about to go to a player (`sendMessage` and `broadcastMessage`). `message` is editable. Cancelable: it is not sent. Tips, popups and titles do not fire it.
- `ThunderChangeEvent` (`Events.ThunderChange`): a thunderstorm starts or stops. Rain alone does not fire it. Informational. Fired both by `/weather` and by the natural weather cycle.
- Aliases, no new firing: `EntityExplodeEvent` is implemented by `Explosion`, `PlayerTeleportEvent` by `PlayerTeleport`, and `WeatherChangeEvent` by `WeatherChange`.
- Now fired, declared in the previous patch: `BlockBurnEvent` (`Events.BlockBurn`): fire burns a block. Cancelable: the block does not burn, and the fire on its side goes out. Fields: `x/y/z`, `id`.
- `BlockFormEvent` (`Events.BlockForm`): lava meets water and forms obsidian (a source) or cobblestone (flowing lava). Cancelable: the lava stays. `newBlockId`.
- `PlayerLatencyUpdateEvent` (`Events.PlayerLatencyUpdate`): a player's real network ping (RakNet) changes; checked once per second. `ping` and `previousPing`. Informational.
- Declared but NOT fired yet, because the server has no such logic: `BlockFadeEvent` (`Events.BlockFade`), `EntityTransformEvent` (`Events.EntityTransform`). Plugins can listen to them without errors; they will simply not be called yet.

Validation note for this patch: built with `NO_NETTY=1 sh build.sh` (socket transport; Maven Central was blocked where this was built, so the Netty transport was not compiled). The new `TestMoreEvents` passes 75 of 75 checks, and the full suite shows the same 17 failing checks as before these changes (TestPluginApi3, TestTrack, TestRun, TestPingNameTag and TestConfig). Run `sh build.sh` and `sh test.sh` with Netty before relying on it.

### Fixed

- Ground mobs now really jump over one-block obstacles. The old step-up moved them up instantly and gravity pulled them back before they could move forward, so zombies bounced in place next to the block. The jump now applies a real impulse (`Mob.JUMP_VELOCITY`).
- Zombies run in a straight line to their target at a constant 2.28 m/s (`MobAI.ZOMBIE_SPEED`), and their head turns toward the target at most 30 degrees per tick (`MobAI.ZOMBIE_TURN`). The old direction calculation (`MobAI.toFloat`) bent the path and changed the speed, and the head snapped to the movement direction.
- Zombies stop next to their target (`MobAI.ZOMBIE_STOP`) instead of walking through it.

## 1.7.0 beta (Minecraft PE 0.15.10, protocol 84)

### Added

- RakNet's UDP transport runs on **Netty 4.2.18.Final**, bundled inside `server.jar`. Plain Java sockets remain as the
  fallback: start with `-Draknet.transport=socket`, or build with `NO_NETTY=1`.
- `TestRakNetTransports` (ping, full handshake, batch echo, clean shutdown), `TestBugs17` and `TestBugs171` (regression
  tests for the fixes below). `test.sh` now also runs the vehicle and enchanting tests, which were not in the suite before.
- `TestDismountAndTable` (regression tests for the vehicle and enchanting table fixes below, including the book flow).

### Changed

- RakNetLib lives in its own repository now, https://github.com/servid124-ux/RakNetLib, and its package is `raknetlib`
  (it was `net.BlockNes.raknet`). The old copy inside the project is gone, and `build.sh` installs and compiles the new
  one by itself. **Plugins that import `net.BlockNes.raknet.*` must change those imports to `raknetlib.*` and be compiled
  again.** The plugin API jar still includes the RakNetLib classes.

### Fixed

- Player data keys depended on the system language. `toLowerCase()` without a locale turns `I` into `ı` under Turkish or
  Azerbaijani settings, so a player could stop finding their saved inventory after a language change. All 36
  `toLowerCase()` calls in the server now use `Locale.ROOT`.
- Every logout rewrote the data file of every saved player on the game thread. Now only the leaving player's file (and
  the ender chests) is written. Autosave and shutdown still save everyone.
- The legacy `players.dat` migration silently dropped players after the 10,000th entry. The limit is gone; a corrupted
  count is rejected by checking it against the bytes that are actually there.
- A malformed packet from a client wrote one ERROR line, with the exception, for every packet. Each player now gets five
  error lines per 10 seconds, followed by a notice.
- `MobAI.toFloat` could loop forever on an infinite value. Non-finite values now return 0. Not reachable from a client
  today (movement packets are validated), but it was a hang waiting to happen.
- On the socket transport, a `stop()` that arrived before `run()` was lost, so the server kept running after being asked
  to stop.
- Plugin `config.properties` errors were ignored silently, and a malformed `\uXXXX` escape escaped the constructor. They
  are now logged as warnings.
- Errors while disconnecting players during shutdown were swallowed. They are now logged.

- Getting off a boat or a minecart by sneaking did nothing on the server: the rider stayed linked, other players kept seeing
  them inside, and nobody else could board. Sneaking now dismounts, and the other players are told.
- Items taken out of the enchanting table vanished. The table did not go through the item-credit check that chests use, so
  the inventory rejected them. It now does.
- The enchanting table offered enchantments for shears, which are not enchantable (same rule as `/enchant`). Only
  enchantable items get options now.
- The bow never received enchantment options. When one of the three levels had nothing for the item, the whole table came
  up empty. That level now drops to one that has options.
- Getting off a boat or a minecart left the rider where they were, inside the vehicle, so other players kept seeing them
  there. Now the rider lands beside the vehicle (the side they face first) and everyone sees the move. If there is no free
  space around the vehicle, the rider stays where they were.
- The enchanting table accepted any item (dirt, sticks...). Now only enchantable items can be placed; anything else goes back
  to the inventory. Shears are no longer in the tool list used for enchantment options.
- Closing the enchanting table dropped its contents on the ground. They now go back to the inventory; whatever does not fit
  is dropped next to the table. Disconnecting and creative mode still drop them.

### Changed

- Version 1.6.5 beta becomes 1.7.0 beta. The Minecraft version sent to clients stays 0.15.10, and the plugin API version
  stays 1.6.
