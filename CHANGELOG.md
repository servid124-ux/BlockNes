# Changelog

## Unreleased: startup

### Changed

- Startup opens with a header: the BlockNes version, the client version and the RakNetLib and BlockNes links.
- Startup prepares the spawn in two passes with the same progress lines: the spawn area (`Preparing spawn area: X%`) and then the spawn chunks (`Preparing spawn chunks: X%`). The chunk pass used to run silently for a few seconds.
- The progress counter is shared (`net.BlockNes.util.PassProgress`) by the spawn, chunk and pregeneration passes.

## Unreleased: rain and fire

### Changed

- Rain (overworld only) keeps mobs from catching fire. Zombies and skeletons don't burn in the sunlight while it rains and burn again once it stops. Skeletons used to burn in the rain too. Lava, fire blocks and Fire Aspect don't set mobs on fire during rain either.
- Rain puts out any mob that is already on fire, animals included.
- Flint and steel does nothing while it rains in the overworld: the player gets a message and the item keeps its durability. In the Nether it works as before.

## Unreleased: more cancellable plugin events

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
