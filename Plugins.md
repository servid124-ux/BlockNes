# BlockNes plugins

Reference for writing plugins for BlockNes 1.7.0 beta (plugin API `1.6`, Minecraft PE 0.15.10, protocol 84). Server settings, commands and files are in [README.md](README.md).

## Plugin API

Plugins are `.jar` files placed in `plugins/`. They are loaded on startup and on `/plugins reload`. A plugin that crashes never takes the server down.

> **Breaking change in 1.7.0:** RakNetLib is now its own library, with the package `raknetlib` (it was `net.BlockNes.raknet`). If your plugin imports `net.BlockNes.raknet.*`, change those imports to `raknetlib.*` and compile it again. The plugin API jar still includes the RakNetLib classes.

The API version is `1.6` (`server().getApiVersion()`). Plugins built for earlier versions of this API keep loading, because the API only adds to what was there. Version 1.2 adds `p.getPing()` and the text above the head (`p.setNameTag()`, see *Player*); a plugin that uses them declares `api=1.2`.

Version 1.3 adds the access-control and server-setting helpers listed below (`banPlayer`, `whitelistAdd`, `isPvpEnabled`, ...). A plugin that declares `api=1.3` needs this version or newer; plugins for older versions keep loading.

Version 1.5 adds menus with buttons, recipes added by plugins, regions, per-player data, item names and lore, and argument helpers for commands (see *API 1.5 additions*). A plugin that uses them declares `api=1.5`; plugins for older versions keep loading.

Version 1.6 adds a server facade, a scheduler with `Task`, `PluginTask` and `AsyncTask`, a command map with `CommandExecutor`, event interfaces, `Level`, `Position`, `Vector3`, metadata, `TextFormat`, `Item.get(...)` and `getPlayerInventory()` (see *API 1.6 additions*). A plugin that uses them declares `api=1.6`; plugins for earlier versions keep loading.

BlockNes 1.7.0 adds more events (see *Events*) without changing the API version. A plugin that uses them only works on 1.7.0 or newer, even if it still declares `api=1.6`.

**Limits.** There are no limits by default: a handler that throws is logged and the other handlers keep running, and a plugin can have as many pending tasks as it needs. To turn the protection on, set `plugin-max-errors`, `plugin-max-slow-calls` and/or `plugin-max-tasks` in `server.properties` (for example 100, 1000 and 5000). A plugin is then disabled after that many handler errors or slow calls (calls slower than 100 ms), and more pending tasks than the limit make `runLater`, `runRepeating` and the scheduler throw an `IllegalStateException`.

### What plugins can and can't change

Plugins can change most of the game logic. Here is what they can touch, and where the limits are.

**Plugins can change**

- **Players:** chat text, format and recipients; commands; movement; clicks on blocks and air; damage, death and respawn; game mode, health, food and XP; permissions; teleports; kicks and bans; fishing, bow shots, projectiles and achievements.
- **Blocks and the world:** block breaks and placements, sign text, the blocks an explosion destroys, `setBlock` and regions, time, weather and lightning, fire, liquid flow, leaf decay and the items a block drops.
- **Mobs:** spawning by protocol id, damage, spawn, death and drops.
- **Items and crafting:** crafting, shaped and shapeless recipes, item names and lore, and chest-style menus.
- **Packets:** read and cancel raw packets with `PacketReceive` and `PacketSend`, and send your own bytes with `server().send(player, bytes)`.
- **Server:** commands, the scheduler, your own data files, and services shared between plugins.

Cancelable events stop the action. Editable fields change its result, for example the chat message or the damage amount.

**What the protocol doesn't allow (Minecraft PE 0.15.10, protocol 84)**

- Titles, boss bars and scoreboards are not packets in this version. The API emulates them with the tip and popup channels, so the result depends on the client.
- No custom blocks, items, mobs or particles. The ids are the ones 0.15.10 knows, and `spawnMob` returns `null` for an unknown id.
- Menus are chest windows with 3 or 6 rows, and they have no title.
- Item names are at most 128 characters per line, and lore has at most 16 lines.
- Furnace recipes are not part of the recipes API.
- Your own packets must use the layouts of protocol 84. You can send them, but you cannot add new packet types.

**What the server core doesn't expose**

- The tick rate: the server runs at 20 ticks per second, and no API changes it.
- The transport: RakNet's reliability, resends and handshake are internal. Use `getPing()`, `kick()` and the packet events instead.
- World generation: there is no hook to change how new chunks are generated. `ChunkPopulate` only tells you when a chunk is ready.
- Mob AI and pathfinding, block updates, crop growth and redstone: `MobTarget`, `MobMove`, `BlockUpdate`, `BlockGrow` and `BlockRedstone` only tell you what happened. There is no API to change them.
- Furnace smelting and burning: `SmeltItem` and `FurnaceBurnFuel` only tell you what happened. There is no API to change them.
- Player data files (`players/*.dat`): use `playerData()` for your own data.
- `server.properties`: no API method writes it. Read it with `server().getConfigString(key, def)`, `getConfigInt` and `getConfigBoolean`.

### Plugin layout

Compile your plugin against `server-plugin-api.jar`; the finished plugin jar goes in `plugins/`. A plugin jar needs a `plugin.properties` file in its root and a main class that extends `net.BlockNes.plugin.Plugin` and has a public no-argument constructor.

```properties
name=HelloWorld
main=example.HelloWorld
version=1.0
author=You
description=What the plugin does
# everything below is optional
website=https://example.com
prefix=HW
api=1.1
depend=RequiredPlugin,AnotherOne
softdepend=OptionalPlugin
loadbefore=SomePlugin
```

- `author` / `authors`: `authors=You,Ana` lists several people; `author()` returns them joined with `, `.
- `depend`: these plugins must be loaded first, otherwise your plugin is not loaded.
- `softdepend`: loaded before yours if they exist.
- `loadbefore`: your plugin is loaded before these, when they are present.
- `prefix`: the text in brackets at the start of your log lines (default: the plugin name).
- `api`: the minimum API version your plugin needs. This server has API `1.6`; a plugin that asks for a newer one is not loaded and a warning is printed.
- `permissions.<node>.…` and `commands.<name>.…` declare permissions and commands (see below).
- An optional `config.properties` in the jar root is copied to `plugins/<Name>/config.properties` on first run.

### Lifecycle

```java
public class HelloWorld extends Plugin {
    @Override public void onLoad()    { }   // jar loaded, nothing enabled yet
    @Override public void onEnable()  { }   // register events, commands and tasks here
    @Override public void onDisable() { }   // server stopping or /plugins reload
}
```

Events, commands, tasks, permissions and attachments registered by a plugin are removed automatically after `onDisable()`. Its menus are closed (their `onClose` runs), its recipes are removed and its player data is saved.

Plugins can be switched individually without restarting: `/plugins disable <name>` (also disables plugins that `depend` on it) and `/plugins enable <name>` (OP only; re-reads the jar). `/plugins reload` still reloads everything.

### Events

Register with `on(Events.X.class, e -> ...)`. Handlers run with the game lock held, never two at once: on the game thread or on the thread of the player who triggered them. You can touch players and worlds without locks of your own.

```java
on(Events.PlayerJoin.class, e -> message(e.player, "Welcome " + e.player.name));
on(Events.BlockPlace.class, Priority.HIGH, true, e -> { if (e.id == 46) e.setCancelled(true); });
```

Overloads: `on(type, handler)`, `on(type, priority, handler)`, `on(type, priority, ignoreCancelled, handler)`.
`Priority` order: `LOWEST`, `LOW`, `NORMAL`, `HIGH`, `HIGHEST`, `MONITOR` (last; observe only).

<details>
<summary>Eventos principales de la API</summary>

| Event | Cancelable | Fields / notes |
| --- | --- | --- |
| `PlayerJoin` | no | `player` |
| `PlayerQuit` | no | `player` |
| `PlayerChat` | yes | `message` (editable), `format` (optional `String.format` pattern taking the name and the message, e.g. `"<%s> %s"`; `null` = normal format), `recipients` (editable list; everyone logged in by default) |
| `PlayerCommand` | yes | `line` (text starting with `/`) |
| `PlayerMove` | yes | `fromX/Y/Z`, `toX/Y/Z`; fires when the block changes |
| `PlayerInteract` | yes | `air`, `x`, `y`, `z`, `face`, `item` |
| `PlayerDamage` | yes (not forced damage) | `amount` (editable), `cause`, `forced`, `attacker` |
| `PlayerDeath` | no | `message` (null = none), `keepInventory` |
| `PlayerRespawn` | no | `player` |
| `PlayerGamemode` | yes | `from`, `to` |
| `BlockBreak` | yes | `x`, `y`, `z`, `id`, `meta` |
| `BlockPlace` | yes | `x`, `y`, `z`, `id`, `meta` |
| `MobDamage` | yes | `mob`, `by` (may be null), `amount` (editable) |
| `MobDeath` | no | `mob`, `killer`, `drops` (editable list) |
| `MobSpawn` | yes | `mob`, `natural` |
| `ItemPickup` | yes | `item` |
| `ServerTick` | no | `tick` (20 per second) |
| `PlayerPreLogin` | yes | `kickMessage` (editable); cancelling kicks the player before joining |
| `PlayerKick` | yes | `reason` (editable) |
| `EntityCombust` | yes | `eid`, `player` (`null` for mobs), `cause`, `seconds`. Cancelled = it does not catch fire |
| `PlayerDropItem` | yes | `item`; cancelling gives the item back |
| `PlayerItemConsume` | yes | `item` (eating / drinking) |
| `PlayerBedEnter` / `PlayerBedLeave` | yes / no | `x`, `y`, `z` |
| `PlayerHeal` | yes | `amount` (editable) |
| `PlayerExhaust` | yes | `amount` (editable): how much food and saturation the exertion costs |
| `PlayerAnimation` | yes | `action` (`1` = arm swing); cancelling hides it from the other players |
| `PlayerItemHeld` | no | `item`, `slot`, `hotbarSlot`; fires after the player changes the item in hand |
| `CraftItem` | yes | `input` (ingredients about to be used), `result`; cancelling uses no ingredients and gives no result |
| `PlayerToggleSneak` / `PlayerToggleSprint` | yes | `sneaking` / `sprinting` |
| `InventoryOpen` | yes | `type` (`chest`, `enderchest`, `workbench`, `furnace`), `x`, `y`, `z` |
| `InventoryClose` | no | `player` |
| `MenuClick` | yes | `menu`, `slot`, `item` (what the slot held), `proposed` (what the client tried to put there). The change is always reverted; cancelling skips the slot's handler (API 1.5) |
| `SignChange` | yes | `x`, `y`, `z`, `lines` (4, editable) |
| `Explosion` | yes | `dimension`, `x`, `y`, `z`, `size`, `blocks` (`{x,y,z,id}` list; remove entries to save blocks) |
| `ServerCommand` | yes | console only: `sender`, `line` (editable) |
| `PacketReceive` | yes | `id`, `data` (raw packet from a player, before the server handles it) |
| `PacketSend` | yes | `data` (raw packet sent through `Server.send`) |
| `LevelSave` | no | fired after every world save |
| `PluginEnable` / `PluginDisable` | no | `plugin` |
| `PlayerAttack` | yes | `targetEntityId`, `targetPlayer` o `targetMob`, `weapon`, `damage` editable; permite cancelar o ajustar el dano antes del golpe a jugadores o mobs |
| `VehicleMount` | yes | `player`, `vehicle` (`BOAT` / `MINECART`), `vehicleEntityId`, `x/y/z`; cancelar impide montar |
| `VehicleDestroy` | yes | `player`, `vehicle`, `vehicleEntityId`, `x/y/z`; cancelar conserva el bote o vagoneta golpeado |
| `BlockSpread` | yes | `sourceX/Y/Z`, `x/y/z`, `id`, `meta` (the new block). Grass, mycelium or fire spreads to a neighbouring block |
| `BlockFromTo` | yes | `sourceX/Y/Z` (the liquid), `x/y/z` (target), `liquidId` (8 water, 10 lava), `newMeta`; cancelled = the liquid does not flow there |
| `LeavesDecay` | yes | `x/y/z`, `id`; leaves with no log nearby. Cancelled = they stay |
| `BlockBurn` | yes | `x/y/z`, `id`; cancelled = the block does not burn |
| `BlockForm` | yes | `x/y/z`, `id` (the new block: 49 obsidian or 4 cobblestone); lava meets water. Cancelled = the lava stays |
| `ExplosionPrime` | yes | `x`, `y`, `z`, `fuse` (editable); TNT lit by a player, fire or redstone. Chain-reaction TNT does not fire it yet |
| `EntityShootBow` | yes | `player`, `bow`, `force` (editable, 0–2); cancelled = no arrow and nothing spent |
| `ProjectileLaunch` | yes | `type` (`ARROW`, `SNOWBALL`, `EGG`, `POTION`, `EXP_BOTTLE`, `FIREBALL`, `SMALL_FIREBALL`), `player` or `mob` (the shooter), `x/y/z`; cancelled = not launched, the item is not spent |
| `PlayerAchievementAwarded` | yes | `player`, `achievement` (`mineWood`, `diamond`); cancelled = not recorded, no message |
| `PlayerTextPreSend` | yes | `player`, `message` (editable); chat and command text only, not tips, popups or titles |
| `PlayerLatencyUpdate` | no | `player`, `ping`, `previousPing`; the RakNet ping changed (checked once per second) |
| `ThunderChange` | no | `thundering`, `weather`; a thunderstorm starts or stops (rain alone does not count) |
| `ItemPickup` / `PlayerPickupItemEvent` | yes | `item`; alias de interfaz para impedir que se recojan objetos |
| `ServerStart` | no | Fired after startup, once plugins are loaded and the world is ready, just before the network loop |
| `ServerStop` | no | Fired at the start of shutdown, before plugins are disabled |
| `WorldLoad` | no | `world`, `nether` (`true` for the Nether). Fired when each world finishes loading, after plugins load |
| `WorldUnload` | no | `world`, `nether`. Fired at the start of shutdown, before the worlds are saved |
| `ConnectionAttempt` | no | `address`. A new connection opens, before login |
| `ConnectionClose` | no | `address`, `reason`. A connection closes, with or without a player (failed logins included) |
| `TimeChange` | no | `from`, `to`. Fired when the time is set (commands, sleeping). The normal clock advance does not fire it |
| `ChunkLoad` | no | `world`, `x`, `z` (chunk coordinates). A chunk enters memory |
| `ChunkUnload` | no | `world`, `x`, `z` (chunk coordinates). A chunk leaves memory |
| `ChunkPopulate` | no | `world`, `x`, `z` (chunk coordinates). A chunk finishes generating |
| `LightningStrike` | no | `x`, `y`, `z`. Lightning strikes at a position |
| `MobDespawn` | no | `mob`. A mob leaves the world (after death, distance cleanup or shutdown). Informational |
| `MobMove` | no | `mob`, `fromX`, `fromZ`. A mob changes position on the x/z plane. Only created when a plugin listens, because it would fire every tick |
| `MobTarget` | no | `mob`, `target` (a player, or `null`). A hostile mob's target changes |
| `EntityTransform` | no | `mob`. Declared, not fired yet |
| `EntityDamageByEntity` | no | `mob`, `damager`. A player hits a mob (only this case for now) |
| `ArmorEquip` | no | `slot`, `item`. The player puts a piece of armor on from the inventory. Losing durability does not fire it |
| `InventoryClick` | no | `window`, `slot`, `item`. A slot change sent by the client |
| `FurnaceBurnFuel` | no | `x`, `y`, `z`, `fuel` (the item before it is used up). A furnace burns one unit of fuel. Informational |
| `SmeltItem` | no | `x`, `y`, `z`, `source`, `result`. A furnace finishes smelting one item. Informational |
| `PlayerEnchantItem` | no | `before`, `result` (items), `levelsSpent`. A player applies an enchantment at the enchanting table |
| `ItemUse` | no | `item`. The player uses an item (right-click, placing a block) |
| `PlayerFish` | yes | `fish`. The player catches a fish. Cancelled = nothing is given |
| `PlayerJump` | no | Heuristic: the server reads no jump packet, so it fires when a player leaves the ground moving up |
| `PlayerHungerChange` | no | `from`, `to`. Food changes from eating or from losing hunger |
| `PlayerExperienceChange` | no | `before`, `after` (total XP). Fired when XP is gained or spent at the enchanting table |
| `PlayerLevelChange` | no | `from`, `to`. The player's level changes |
| `PlayerChangeDimension` | no | `from`, `to`. The player moves between the overworld and the Nether |
| `VehicleDismount` | no | `vehicle` (`BOAT` or `MINECART`). The player gets off a boat or a minecart |
| `BlockGrow` | no | `x`, `y`, `z`, `id`, `meta` (values before growing). Crops, stems and saplings grow |
| `BlockUpdate` | no | `x`, `y`, `z`, `id`. A block is checked after a nearby change. Only created when a plugin listens |
| `BlockRedstone` | no | `x`, `y`, `z`. The redstone power around a block changes |
| `BlockFade` | no | `x`, `y`, `z`, `id`. Declared, not fired yet |
| `BlockDropItem` | yes | `player` (`null` for falling blocks and explosions), `x`, `y`, `z`, `id`, `drops` (editable list). The block is about to drop its items. Cancelled = nothing drops |

</details>


Example using the new events:

```java
on(Events.PlayerAttack.class, e -> {
    if (e.targetPlayer != null && !e.player.isOp()) e.setCancelled(true); // block PvP attacks unless attacker is OP
    if (e.targetMob != null) e.damage = Math.min(e.damage, 4.0);           // cap damage to mobs
});
on(Events.VehicleMount.class, e -> { if (!e.player.isOp()) e.setCancelled(true); });
on(Events.VehicleDestroy.class, e -> { if (e.vehicle.equals("BOAT")) e.setCancelled(true); });
```

#### Event interfaces

Every event also implements an interface with a standard name (in the `net.BlockNes.event` packages). You can listen to the interface instead of the class:

```java
on(PlayerChatEvent.class, e -> e.setMessage(e.getMessage().replace("badword", "****")));

@EventHandler(priority = Priority.HIGH, ignoreCancelled = true)
public void onChat(PlayerChatEvent e) { e.setMessage(e.getMessage().replace("badword", "****")); }
```

`EntityEvent.getEntity()` returns the `Player` or the `Mob`, so check it with `instanceof`. `getDamager()` returns `null` when no entity caused the damage. The category interfaces `PlayerEvent`, `EntityEvent` and `PluginEvent` cover all the events of their kind.

<details>
<summary>All event interfaces</summary>

| Package | Interfaces |
| --- | --- |
| `net.BlockNes.event.player` | `PlayerEvent` (category), `PlayerAttackEvent`, `PlayerPickupItemEvent`, `PlayerJoinEvent`, `PlayerQuitEvent`, `PlayerChatEvent`, `PlayerCommandPreprocessEvent`, `PlayerMoveEvent`, `PlayerInteractEvent`, `PlayerDeathEvent`, `PlayerRespawnEvent`, `PlayerGameModeChangeEvent`, `PlayerKickEvent`, `PlayerPreLoginEvent`, `PlayerDropItemEvent`, `PlayerItemConsumeEvent`, `PlayerBedEnterEvent`, `PlayerBedLeaveEvent`, `PlayerToggleSneakEvent`, `PlayerToggleSprintEvent`, `PlayerExhaustEvent`, `PlayerAnimationEvent`, `PlayerItemHeldEvent`, `ItemUseEvent`, `PlayerAchievementAwardedEvent`, `PlayerAuthenticateEvent`, `PlayerChangeDimensionEvent`, `PlayerExperienceChangeEvent`, `PlayerFishEvent`, `PlayerHandshakeEvent`, `PlayerHungerChangeEvent`, `PlayerJumpEvent`, `PlayerLatencyUpdateEvent`, `PlayerLevelChangeEvent`, `PlayerTeleportEvent`, `PlayerTextPreSendEvent` |
| `net.BlockNes.event.block` | `BlockBreakEvent`, `BlockPlaceEvent`, `SignChangeEvent`, `BlockBurnEvent`, `BlockDropItemEvent`, `BlockFadeEvent`, `BlockFormEvent`, `BlockFromToEvent`, `BlockGrowEvent`, `BlockInteractEvent`, `BlockRedstoneEvent`, `BlockSpreadEvent`, `BlockUpdateEvent`, `LeavesDecayEvent` |
| `net.BlockNes.event.entity` | `VehicleMountEvent`, `VehicleDestroyEvent`, `EntityEvent` (category), `EntityDamageEvent`, `EntityDeathEvent`, `EntitySpawnEvent`, `EntityRegainHealthEvent`, `EntityTeleportEvent`, `EntityDamageByEntityEvent`, `EntityDespawnEvent`, `EntityExplodeEvent`, `EntityMoveEvent`, `EntityShootBowEvent`, `EntityTargetEvent`, `EntityTransformEvent`, `ExplosionPrimeEvent`, `ProjectileLaunchEvent`, `VehicleDismountEvent` |
| `net.BlockNes.event.inventory` | `InventoryOpenEvent`, `InventoryCloseEvent`, `CraftItemEvent`, `ArmorEquipEvent`, `FurnaceBurnFuelEvent`, `InventoryClickEvent`, `InventoryTransactionEvent`, `PlayerEnchantItemEvent`, `SmeltItemEvent` |
| `net.BlockNes.event.server` | `ServerCommandEvent`, `DataPacketReceiveEvent`, `DataPacketSendEvent`, `ConnectionAttemptEvent`, `ConnectionCloseEvent`, `PacketDecodeEvent`, `PacketEncodeEvent`, `ServerStartEvent`, `ServerStopEvent` |
| `net.BlockNes.event.level` | `LevelSaveEvent`, `ChunkLoadEvent`, `ChunkPopulateEvent`, `ChunkUnloadEvent`, `LightningStrikeEvent`, `ThunderChangeEvent`, `TimeChangeEvent`, `WeatherChangeEvent`, `WorldLoadEvent`, `WorldUnloadEvent` |
| `net.BlockNes.event.plugin` | `PluginEvent` (category), `PluginEnableEvent`, `PluginDisableEvent` |
| `net.BlockNes.event` | `Cancellable`, `Listener` |

</details>

#### `@EventHandler` listeners

Instead of one `on(...)` per event you can group handlers in a class and register them all at once:

```java
public class MyListener {
    @EventHandler(priority = Priority.HIGH, ignoreCancelled = true)
    public void onBreak(Events.BlockBreak e) { e.setCancelled(true); }

    @EventHandler
    public void onJoin(Events.PlayerJoin e) { /* ... */ }
}
// in onEnable():
registerEvents(new MyListener());   // returns how many handlers were registered
```

Each handler method takes exactly one parameter (an `Event`). Methods that don't are skipped with a warning.

`unregisterEvents(listener)` removes the handlers of that object and returns how many were removed; `unregisterAllEvents()` removes every handler this plugin registered.

You can listen to a base class (`Events.PlayerEvent` or `Event`) to receive every event of that kind, and fire your own events with `fire(new MyEvent())` (any subclass of `Event`).

### Commands and permissions

```java
// opOnly = true: only OPs and the console
command("hello", "greets you", "", false, (sender, args) -> sender.send("Hello " + sender.name()));

// protected by a permission node (OPs always pass)
command("announce", "message to all", "<text ...>", "hello.announce", (s, a) -> broadcast(String.join(" ", a)));
```

`sender` has `name()`, `isOp()` and `send(String)`. Check a node manually with `hasPermission(sender, "node")`.

Aliases: `alias("hello", "hi", "hey")` makes `/hi` and `/hey` run the same command (`/help` lists it once).
Run commands from code: `dispatchCommand(sender, "give Steve 1 1")`; `console()` returns an OP sender that writes to the log.

`plugins/permissions.properties`:

```properties
# player=groups and/or permissions
Steve=vip,hello.use
# group.<name>=permissions; "-x" denies; "@group" includes another group
group.vip=pos.others,-tnt.place,@default
# the "default" group applies to everybody
group.default=hello.use
```

Wildcards: `*` and `a.*`. OPs have every permission.

#### Command classes

For a command with its own state, extend `Command`. It handles the usage line, the aliases and the permission:

```java
import net.BlockNes.command.CommandSender;
import net.BlockNes.plugin.Command;

// in onEnable():
registerCommand(new Command("greet", "greets someone", "<name>", "hi") {
    @Override public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) return false;          // false = show the usage line
        sender.send("Hello " + args[0]);
        return true;
    }
});
```

`setPermission("hello.use")` (`""` = everyone, `"op"` = operators only), `setPermissionMessage(...)`, `setDescription(...)`, `setUsage(...)` and `setAliases(...)` change the command after it is created. `getCommand("greet")` returns it and `unregisterCommand("greet")` removes it.

#### Declared commands and permissions

Commands, aliases and permissions can also be declared in `plugin.properties`. Declared descriptions, usages and permissions are used for whatever the code leaves empty, and declared aliases are added to the ones the code gives:

```properties
commands.greet.description=greets someone
commands.greet.usage=<name>
commands.greet.aliases=hi,hey
commands.greet.permission=hello.use
commands.greet.permission-message=You cannot use that
permissions.hello.use.description=use /greet
permissions.hello.use.default=true
permissions.vip.default=op
permissions.vip.children=hello.use,-hello.admin
```

- `permissions.<node>.default`: `op` (only operators, the default), `notop` (everyone except operators), `true` (everyone) or `false` (nobody).
- `children` apply to whoever has the parent permission. Above, anyone with `vip` gets `hello.use` and is denied `hello.admin` (`-` denies).

The same thing in code:

```java
registerPermission("hello.use", "use /greet", PermissionDefault.TRUE);
registerPermission("vip", "VIP perks", PermissionDefault.OP)
    .addChild("hello.use", true)
    .addChild("hello.admin", false);
```

#### Who has which permission

Players who are not operators get a permission from the first of these that says something about it:

1. attachments made with `addAttachment` (the last matching one wins);
2. `plugins/permissions.properties` (the player's own line and their groups);
3. the children of parent permissions the player has;
4. the default of the registered permission. Unregistered nodes are denied.

Operators and the console have every permission. `hasPermission(player, "node")` checks all of the above.

```java
PermissionAttachment a = addAttachment(player);     // or addAttachment(player, "hello.admin", true)
a.setPermission("hello.admin", true).setPermission("hello.use", false);
a.unsetPermission("hello.admin");                   // back to the lower levels
removeAttachment(a);                                // or a.remove()
```

Attachments are removed when the player leaves and when the plugin is disabled or reloaded.

### Tasks

```java
Task t = runLater(100, () -> broadcast("5 seconds later"));          // 20 ticks = 1 second
runRepeating(0, 6000, () -> broadcast("Every 5 minutes"));
t.cancel();

runAsync(() -> { /* HTTP, files... never touch players or worlds here */ });
runAsync(() -> fetchSomething(), result -> message(player, result)); // result arrives on the server thread

runAsync(() -> { String data = download(); runSync(() -> broadcast(data)); }); // runSync: back to the server thread from ANY thread
cancelTasks();                                   // cancels every task of this plugin
t.id(); t.isRepeating(); t.owner();              // Task info
```

#### AsyncTask

For work that must not block the server, extend `AsyncTask`. `onRun()` runs on another thread (never touch players or worlds there), then `onCompletion()` runs on the server thread:

```java
int id = runAsync(new AsyncTask() {
    @Override public void onRun() {
        setResult(download("https://example.com/data.txt"));   // your own code, off the server thread
    }
    @Override public void onCompletion() {
        broadcast("Got: " + getResult());                       // back on the server thread
    }
});
isTaskQueued(id);    // still pending?
cancelTask(id);      // see the rules below
```

- A cancelled task never runs `onCompletion()`. If it is cancelled before it starts, `onRun()` does not run either.
- If `onRun()` throws, the error is logged, `isCrashed()` becomes `true` and `onCompletion()` still runs.
- `cancelTask(id)` and `isTaskQueued(id)` work for both kinds of task; `getTask(id)` returns a scheduled `Task`. Pending tasks of a plugin are cancelled when it is disabled.

### Config

`config()` returns `plugins/<Name>/config.properties` (a `java.util.Properties` subclass):

```java
Config cfg = config();
String s = cfg.getString("welcome", "Hi");
int n = cfg.getInt("limit", 10);                // also getLong, getDouble, getBoolean
cfg.set("limit", 20).save();
reloadConfig();                                  // re-read from disk
Path file = saveResource("data.txt");           // copy a resource from the jar if missing
Path folder = dataFolder();                     // plugins/<Name>/

cfg.setDefault("limit", 10).save();             // only writes the key if it doesn't exist yet
cfg.has("limit");
cfg.setList("worlds", List.of("a", "b"));       // stored as "a,b"
List<String> w = cfg.getStringList("worlds");   // [a, b]
List<String> kits = cfg.keys("kits.");          // kits.vip, kits.basic ... (sorted)
```

`Config` also has `remove(key)`, `getAll()` and `getKeys()`. `saveDefaultConfig()` copies the jar's `config.properties` if none exists, `getConfig()` is the same as `config()`, and `getResource("name")` opens a file from the plugin's own jar (close it when you are done).

For structured data, `jsonConfig("data.json")` gives a `JsonConfig` stored in the plugin folder, addressed with dotted paths:

```java
JsonConfig json = jsonConfig("data.json");
json.set("homes.steve.x", 10).set("homes.steve.z", -4).save();   // creates the nested objects
int x = json.getInt("homes.steve.x", 0);                          // also getString, getLong, getDouble, getBoolean, getList
json.setDefault("maxHomes", 3).save();                            // only if the key is missing
json.remove("homes.steve");
json.reload();                                                    // read the file from disk again
```

### HUD: titles, action bar, boss bars and scoreboard

MCPE 0.15.10 has no title, boss bar or scoreboard packets, so the API emulates them on the two text channels the client does have:
**tip** (scoreboard and titles) and **popup** (above the hotbar). It redraws every 10 ticks, only resends when something changed, and removes everything a plugin registered when that plugin is disabled.

```java
// scoreboard: one line per key, sorted by order; a global line is evaluated for every player
hudLine("hp", 1, p -> "\u00a7cHP: " + p.health);
hudLine(player, "money", 2, "\u00a76$" + balance);       // per-player line; text = null removes it
hudLine(player, "combat", 3, "\u00a7cIn combat", 200);   // NEW: disappears by itself after 200 ticks
hudRemove("hp");  hudRemove(player, "money");  hudClear(player);
List<String> shown = hudLines(player);                    // NEW: scoreboard lines the player sees now (no padding)

title(player, "\u00a7aWelcome", "\u00a77enjoy", 60);      // center, 60 ticks (covers the scoreboard while it lasts)
actionBar(player, "\u00a7eSaved!", 40);                   // above the hotbar (covers boss bars while it lasts)
bossBar(player, "dragon", "\u00a7cEnder Dragon", 0.65);   // text + 24-segment progress bar; same key = update
removeBossBar(player, "dragon");
broadcastTitle("\u00a76Round 2", "fight!", 40);            // NEW: to everybody
broadcastActionBar("\u00a7eServer restarts in 5 min", 100);
clearTitle(player);                                       // NEW: remove the active title and action bar

String[] onScreen = hudView(player);                      // {popup, tip} currently shown (logical text, no padding)
```

`Events.PlayerHud` fires per player on every redraw (only if someone listens): edit `lines` (the scoreboard) or set `popup`, so several plugins can add to the same HUD without knowing each other.

#### Scoreboard position (it no longer sits in the middle of the screen)

The client centers every line of the tip channel, so the scoreboard used to be in the middle of the screen. Now each line is padded with spaces (4 px each) so the whole block sits at the **right** edge by default; all lines start at the same x.
The width of each line is computed with the default Minecraft font widths (color codes ignored, bold counts +1 px per letter).

The server settings (`hud-position`, `hud-screen-width`, `hud-offset-y`, `hud-max-lines`) are in [README.md](README.md#configuration). Every player can tune the scoreboard live with `/hud` (no OP needed), because GUI size differs per device; the command is in [README.md](README.md#commands).

For plugins: `hudPosition(p, HudPosition.LEFT)`, `hudVisible(p, false)`, `hudScreenWidth(p, 640)`, `hudOffsetY(p, 2)` (and the matching getters). Titles are always centered. These per-player settings are not saved to disk (they reset when the player leaves).

It is still an emulation: the tests check the text the server sends (including the padding math), **not how a real 0.15.10 client draws it**. Padding is based on assumed font widths and screen size, so check it on a real client and adjust `hud-screen-width` / `/hud y` once. The `left` position relies on trailing spaces being kept by the client (they are followed by `\u00a7r` just in case); `right` only uses leading spaces. The boss bar is text, not the real bar.

### API 1.4 additions (inside `Plugin`)

`api=1.4` in `plugin.properties` is only needed if you use these. Everything is additive: plugins compiled against 1.3 keep working.

| Method | Description |
| --- | --- |
| `color("&aHi &lthere")` / `stripColor(s)` | `&` codes to `\u00a7` codes / remove them (also `Text.color`, `Text.strip`) |
| `messagef(p, "&aYou have %d coins", n)` / `broadcastf(...)` | `String.format` + colors, to one player / everybody |
| `runRepeating(delay, period, task -> ...)` | Like `runRepeating` but the body receives its `Task` so it can `task.cancel()` itself |
| `runTimes(delay, period, times, i -> ...)` | Runs `times` times (index 0..times-1) and cancels itself |
| `countdown(5, s -> broadcast("Starting in " + s), () -> start())` | One call per second with the remaining seconds, then `done` |
| `once(Events.X.class, e -> ...)` | Listen to an event only once (also with a `Priority`) |
| `onlineCount()` / `playerNames()` / `isOnline(name)` | Quick player queries |
| `playersNear(dim, x, y, z, r)` / `nearestPlayer(dim, x, y, z, max)` / `mobsNear(dim, x, y, z, r)` | Entities in a radius, nearest first |
| `teleportChecked(p, x, y, z)` | Teleport through `Events.PlayerTeleport` (returns false if another plugin cancelled it) |
| `setWeather(type, ticks)` | `Weather.SUNNY / RAINY / RAINY_THUNDER / THUNDER`; goes through `Events.WeatherChange` |
| `strikeLightning(x, y, z)` / `giveXp(p, n)` / `setTimeStopped(b)` | Small server helpers |
| `kickAll(reason, exceptPermission)` | Kick everyone except players with that permission |
| `hudLine(p, key, order, text, ttlTicks)`, `hudLines`, `hudPosition`, `hudVisible`, `hudScreenWidth`, `hudOffsetY`, `broadcastTitle`, `broadcastActionBar`, `clearTitle` | See the HUD section |

New events: `Events.PlayerTeleport` (cancelable, `toX/toY/toZ` editable; fired by `/tp` and `teleportChecked`, not by the server's internal teleports) and `Events.WeatherChange` (cancelable, `type`/`ticks` editable; fired by `/weather` and `setWeather`, not by random weather). `EntityExplodeEvent`, `PlayerTeleportEvent` and `WeatherChangeEvent` are the PocketMine names for `Explosion`, `PlayerTeleport` and `WeatherChange`. Also new in this patch: the table rows above for `BlockSpread`, `BlockFromTo`, `LeavesDecay`, `BlockBurn`, `BlockForm`, `ExplosionPrime`, `EntityShootBow`, `ProjectileLaunch`, `PlayerAchievementAwarded`, `PlayerTextPreSend`, `PlayerLatencyUpdate` and `ThunderChange`.

New helper classes: `Text` (`color`, `strip`, `width`, `bar(progress, length, "&a", "&8")`, `ticks`, `seconds`, `number`, `truncate`) and `Cooldowns` (`tryUse(key, ms)`, `remainingSeconds(key)`, `reset`).

### API 1.5 additions (inside `Plugin`)

`api=1.5` in `plugin.properties` is only needed if you use these. Everything is additive, so plugins compiled against 1.4 keep working. `plugins-ejemplo/Tienda` uses most of them.

#### Menus with buttons

A menu is a chest window with 3 or 6 rows (27 or 54 slots). Each slot holds an item and, optionally, a click handler. Items in a menu are buttons: when a player tries to take or move an item, the change is reverted and the handler runs.

```java
Menu shop = menu(3);                                          // 3 rows (27 slots); menu(6) gives 54
shop.border(new Item(160, 7, 1));                             // glass around the edge
shop.setItem(11, new Item(297, 0, 1).withName(color("&ePan")), e -> {
    give(e.player, new Item(297, 0, 1));                      // the event has player, menu, slot, item and proposed
});
shop.setItem(15, new Item(35, 14, 1).withName(color("&cClose")), e -> closeMenu(e.player));
shop.onClose(p -> message(p, "Thanks for visiting"));
openMenu(player, shop);
```

- Every change a client makes to a menu slot is reverted, so an item never ends up in the wrong place. A slot without a handler just reverts.
- `Events.MenuClick` can be cancelled. Cancelling it skips the slot's handler; the change is still reverted.
- While a menu is open the player's inventory is frozen: the client cannot move, drop or craft items. That is what makes it impossible to lose or duplicate an item through a menu. Closing the menu unfreezes it.
- `onClose` runs when the player closes the menu, when `closeMenu` is called, when the player disconnects, and when the plugin that created the menu is disabled.
- Opening a menu (or a chest) closes the window the player had open before.
- `setItem` and the other changes update the screen of every player who has the menu open. Call them from the server thread.
- The client shows no title for these menus.
- To show the window, the client needs a chest where the server says it is. So while a menu is open, that player alone sees a chest two blocks above their head; it is removed when the menu closes. The world on the server is not changed.

| Method | Description |
| --- | --- |
| `menu(rows)` | New menu with 3 or 6 rows (other values throw `IllegalArgumentException`) |
| `openMenu(p, menu)` / `closeMenu(p)` / `isMenuOpen(p)` | Open it for a connected player / close it (runs `onClose`) / check |
| `setItem(slot, item)` / `setItem(slot, item, handler)` | Put an item (air empties the slot), with an optional click handler |
| `getItem(slot)` / `rows()` / `size()` / `owner()` | Read the menu |
| `fill(item)` / `border(item)` / `clear()` | Bulk changes; the slots they change lose their handlers |
| `onClose(handler)` / `viewers()` / `refresh()` | Close callback, players who have it open, resend the whole content |

#### Items with a name and lore

```java
Item sword = new Item(276, 0, 1).withName(color("&bSword of the cave"))
        .withLore(Arrays.asList(color("&7Found deep underground"), color("&7Sharp")));
sword.name();   // the name with its § codes, or null when the item has no name
sword.lore();   // the lines, or an empty list
```

Names and lore are saved with the item and sent to the client as NBT (`display`). Limits: 128 characters per name or line, and 16 lore lines. They are part of the item's identity: items with different names do not stack, and the duplicate protection counts them as different items.

#### Recipes

```java
Map<Character, Item> ing = new HashMap<>();
ing.put('T', new Item(5, 0, 1));                                       // planks
ing.put('P', new Item(280, 0, 1));                                     // stick
addShapedRecipe(new Item(278, 0, 1).withName(color("&bShop pickaxe")), new String[]{"TTT", " P ", " P "}, ing);
addShapelessRecipe(new Item(1, 0, 4), Arrays.asList(new Item(3, 0, 2), new Item(4, 0, 2)));
```

- Shaped rows have up to 3 characters, and `' '` is an empty cell. Empty borders are trimmed, so `{"AA ", "A  "}` is a 2x2 recipe. Every character must be in the ingredients map.
- Shapeless recipes use up to 9 units in total.
- An ingredient with damage `-1` matches any damage value, for example every kind of log.
- Recipes added by a plugin are removed when it is disabled. `removeRecipe(recipe)` removes any recipe, built-in ones included (`server().recipes.all` lists them). When a built-in recipe and a plugin recipe give the same result, the built-in one is tried first.
- Furnace recipes are not part of this API yet.
- Each recipe added while players are online sends the whole recipe list to each of them again. When you add many, do it in `onEnable`, before players join.

#### Regions

```java
Region r = region(0, 10, 60, 10, 12, 62, 12);   // corners in any order, inclusive; dimension 0 = overworld, 1 = nether
r.fill(1, 0);                                   // stone; returns how many blocks were written
r.replace(1, 4, 0);                             // stone -> cobblestone; returns how many were replaced
int cobble = r.count(4);
Region.Snapshot copy = r.copy();                // ids and metas kept in memory
region(0, 40, 60, 10, 42, 62, 12).paste(copy, 40, 60, 10, true);   // true = skip air: keep what is there where the copy is air
```

Creating a region has no size limit. `copy()` refuses regions of more than 1,000,000 blocks with `IllegalArgumentException`, because the copy is kept in memory. Blocks outside the world height are skipped. Other methods: `contains(x, y, z)`, `volume()`, `width()`, `height()`, `depth()`, and `minX()` to `maxZ()`. Snapshots have `width()`, `height()`, `depth()`, `idAt(dx, dy, dz)` and `metaAt(dx, dy, dz)`.

#### Per-player data

```java
JsonConfig stats = playerData().of(player);     // or of("Steve")
stats.set("kills", stats.getInt("kills", 0) + 1);
```

Each player gets a JSON file at `plugins/<Name>/players/<name>.json`, with the same methods as `jsonConfig` (dotted paths, `getInt`, `setDefault`, ...). Names are lowercased, so "Steve" and "steve" share one file. The data is saved when the player disconnects and when the plugin is disabled. `saveAll()` saves now, `exists(name)` checks for the file, and `unload(name)` saves the player's data and drops it from memory.

#### Argument helpers

`net.BlockNes.plugin.Args` reads command arguments. A missing or invalid value returns the default, so a bad number never breaks a command:

| Method | Description |
| --- | --- |
| `Args.getInt(args, i, def)` / `Args.getInt(args, i, def, min, max)` | Integer; with a range, values outside it return `def` |
| `Args.getDouble(args, i, def)` / `Args.getBoolean(args, i, def)` | Finite decimal / `true`, `on`, `si`, `yes`, `1` and `false`, `off`, `no`, `0` |
| `Args.get(args, i, def)` / `Args.has(args, i)` / `Args.join(args, from)` | Text / whether it exists / the words from a position, joined with spaces |

### API 1.6 additions (inside `Plugin`)

`api=1.6` in `plugin.properties` is only needed if you use these. Everything is additive: plugins compiled against 1.5 keep working.

#### Base class and state

- `extends PluginBase` works the same as `extends Plugin`.
- `setEnabled(true)` / `setEnabled(false)` enable or disable the plugin. Enabling a disabled plugin reads its jar again, so the old object stops working.
- `isEnabled()` is `true` only after `onEnable()` has finished. Tasks and listeners can still be registered inside `onEnable()`.

#### Server facade

Get it with `getServer()` (or `Server.getInstance()` from anywhere):

| Method | Description |
| --- | --- |
| `getName()` / `getVersion()` / `getMinecraftVersion()` / `getApiVersion()` | `BlockNes`, `1.7.0 beta`, `0.15.10`, `1.6` |
| `getPort()` / `getIp()` / `getMaxPlayers()` / `getMotd()` | Server settings |
| `getOnlinePlayers()` / `getPlayer(name)` / `getPlayerExact(name)` | `getPlayer` also accepts the start of a name |
| `getLevelByName(name)` / `getDefaultLevel()` / `getLevels()` | Worlds as `Level` objects (`world`, `nether`) |
| `getPluginManager()` / `getScheduler()` / `getCommandMap()` / `getLogger()` | Managers and logger |
| `getDataPath()` / `getPluginPath()` | Server and plugin folders, with a trailing separator |
| `isOp(name)` / `getWhitelisted()` / `isWhitelisted(name)` / `getNameBans()` / `getIPBans()` | Access lists |
| `broadcastMessage(msg, recipients)` | Message to a list of players |
| `dispatchCommand(sender, line)` | Run a command line as `sender`; returns `true` (it does not report whether the command exists) |
| `getConsoleSender()` / `shutdown()` | Console sender / stop the server |

#### Scheduler

Get it with `getScheduler()`. Delays and periods are in ticks (20 ticks = 1 second).

| Method | Description |
| --- | --- |
| `scheduleTask(task)` | Run on the next tick |
| `scheduleDelayedTask(task, delay)` / `scheduleRepeatingTask(task, period)` / `scheduleDelayedRepeatingTask(task, delay, period)` | Run later / repeat / both. Each returns a `TaskHandler` |
| `scheduleAsyncTask(asyncTask)` | Run `onRun()` on another thread; returns the task id |
| `cancelTask(id)` / `cancelTasks(plugin)` / `cancelAllTasks()` / `isQueued(id)` | Cancel and check |

```java
getServer().getScheduler().scheduleRepeatingTask(new Task() {
    @Override public void onRun(long currentTick) { broadcast("Every 5 minutes"); }
}, 6000);
```

- `Task`: implement `onRun(long currentTick)`. `onCancel()` runs when the task is cancelled.
- `PluginTask(plugin)`: a task that belongs to a plugin. It is cancelled automatically when the plugin is disabled, and `getOwner()` returns the plugin.
- `AsyncTask`: `onRun()` runs on another thread (never touch players or worlds there); `onCompletion(Server)` runs on the server thread. Use `setResult(...)` and `getResult()` to pass data between them.

#### Commands

| Method | Description |
| --- | --- |
| `command(name, description, usage, permission, (sender, command, label, args) -> ...)` | Command with a `CommandExecutor`. Return `false` to show the usage line |
| `getCommandMap()` → `getCommand(name)` / `getCommands()` / `register(prefix, command)` / `unregister(name)` / `dispatch(sender, line)` | Find, add and remove commands |
| `ConsoleCommandSender` | The console as a command sender |

```java
command("gift", "gives 16 stone", "", "", (sender, command, label, args) -> {
    Player p = getServer().getPlayerExact(sender.getName());
    if (p == null) { sender.sendMessage("Players only"); return true; }
    p.getPlayerInventory().addItem(Item.get(1, 0, 16));
    return true;
});
```

#### Events

| Method | Description |
| --- | --- |
| `getPluginManager().registerEvents(listener, plugin)` | Register every `@EventHandler` method of a `net.BlockNes.event.Listener` |
| `getPluginManager().unregisterEvents(listener, plugin)` | Remove them |
| `getPluginManager().callEvent(event)` | Send an event to the listeners (same as `fire`) |

#### Worlds, positions and vectors

Get a world with `getServer().getLevelByName("world")`, `getDefaultLevel()` or `player.getLevel()`.

| Method | Description |
| --- | --- |
| `getName()` / `getDimension()` | Folder name and dimension (0 overworld, 1 nether) |
| `getBlockIdAt(x, y, z)` / `getBlockDataAt(x, y, z)` | Read a block id and its data |
| `setBlockIdAt(x, y, z, id)` / `setBlockDataAt(x, y, z, meta)` / `setBlockAt(x, y, z, id, meta)` | Change blocks (clients are updated) |
| `getHighestBlockAt(x, z)` / `getSpawnLocation()` | Column height and world spawn |
| `getTime()` / `setTime(t)` / `stopTime()` / `startTime()` | World time |
| `setWeather(type, ticks)` | Weather in the overworld |
| `getPlayers()` / `getMobs()` | Players and mobs in that world |
| `dropItem(vector, item)` / `explode(vector, size)` / `addLevelEvent(vector, eventId, data)` / `save()` | Items, explosions, sounds and particles, saving |

Time and weather are global to the server, not per world. A block is an id and its data; there is no `Block` object yet.

`Position` is a `Vector3` with a world (`getLevel()`). `Vector3` is immutable: `add`, `subtract`, `multiply`, `divide`, `distance`, `distanceSquared`, `length`, `normalize`, `floor`, `ceil` and `dot` return new vectors.

#### Players

| Method | Description |
| --- | --- |
| `getLevel()` / `getPosition()` | World and position |
| `getIp()` | IP address without the port (`getAddress()` still returns the `InetSocketAddress`) |
| `getFood()` / `getXpLevel()` / `getXpProgress()` | Food level and experience |
| `hasPermission(node)` | Permission check (OPs have everything) |
| `getPlayerInventory()` | Inventory that updates the client: `addItem`, `removeItem`, `setItem`, `getItem`, `contains`, `getContents`, `clear`. `getInventory()` is the raw inventory and does not update the client |
| `sendTitle(title, subtitle)` / `addTitle(title, subtitle, fadeIn, stay, fadeOut)` | Title in the middle of the screen. The times are in ticks, and the title is shown for their sum (no animation) |
| `setMetadata(key, value)` / `getMetadata(key)` / `hasMetadata(key)` / `removeMetadata(key, plugin)` | Plugin data stored on the player (see below) |

#### Metadata

Metadata is stored per plugin on players and levels, and it is removed when the plugin is disabled:

```java
p.setMetadata("vip", new FixedMetadataValue(this, true));
boolean vip = p.hasMetadata("vip") && p.getMetadata("vip").get(0).asBoolean();
p.removeMetadata("vip", this);
```

`MetadataValue` has `value()`, `asInt()`, `asLong()`, `asDouble()`, `asString()` and `asBoolean()`. `FixedMetadataValue(plugin, value)` keeps a fixed value. Metadata on blocks is not available yet.

#### Items and text

| Method | Description |
| --- | --- |
| `Item.get(id)` / `Item.get(id, damage)` / `Item.get(id, damage, count)` | Create an item |
| `getId()` / `getDamage()` / `getCount()` / `getMaxStackSize()` / `isNull()` | Item data |
| `getCustomName()` / `getLore()` / `getEnchantments()` / `hasEnchantment(id)` | Name, description and enchantments |

`Item` is immutable, so change it with `withName(...)`, `withCount(...)` and the other `with` methods. `TextFormat` has the color and style codes (`TextFormat.RED`, `TextFormat.GREEN`, `TextFormat.BOLD`, `TextFormat.RESET`...), and `TextFormat.clean(text)` removes them.

### Server and world helpers (inside `Plugin`)

| Method | Description |
| --- | --- |
| `server()` | The `Server` instance (`server().isOp(player)` etc.) |
| `players()` / `player(name)` | Connected players / find one by name |
| `broadcast(msg)` / `message(player, msg)` | Chat messages |
| `popup(player, msg)` / `tip(player, msg)` | Text above the hotbar / small text at screen center |
| `world(dim)` | World by dimension (`0` overworld, `1` nether) |
| `getBlock(dim, x, y, z)` / `setBlock(dim, x, y, z, id, meta)` | Read / place blocks (clients are updated) |
| `spawnMob(dim, netId, x, y, z)` | Spawn a mob (`Mob.ZOMBIE`, `Mob.COW`, ...); `null` if unknown |
| `registerService(Type.class, impl)` / `service(Type.class)` | Share an API between plugins |
| `plugin(name)` / `isEnabled()` | Get another loaded plugin / is this plugin still active |
| `kick(p, reason)` / `teleport(p, x, y, z)` | Kick (goes through `PlayerKick`) / move a player |
| `heal(p, n)` / `damage(p, n, cause)` / `kill(p)` | Health (go through `PlayerHeal` / `PlayerDamage`) |
| `setGamemode(p, gm)` / `give(p, item)` | Game mode (`PlayerGamemode`) / give an item (returns what didn't fit) |
| `isOp(p)` / `setOp(name, bool)` | OP list |
| `time()` / `setTime(t)` / `saveAll()` / `maxPlayers()` | World time, save to disk, slot count |
| `getLogger()` | `info`, `notice`, `warning`, `error`, `critical`, `alert`, `emergency`, `debug` (only with `-Dblocknes.debug=true`) and `logException(t)`; lines start with `[prefix]` |
| `log(msg)` / `logWarn(msg)` | Console output prefixed with your plugin name |
| `playerExact(name)` / `playersIn(dim)` | Exact-name lookup / players in one dimension |
| `broadcast(msg, permission)` / `broadcastTip(msg)` / `broadcastPopup(msg)` | Chat message only to players with the permission / tip or popup to everyone |
| `hasPermission(player, node)` | Permission check (see *Who has which permission*) |
| `banPlayer(name)` / `pardonPlayer(name)` / `isBanned(name)` | Ban (kicks the player) / pardon / check a name (`banned-players.txt`) |
| `banIp(ip)` / `pardonIp(ip)` / `isBannedIp(ip)` | Ban (kicks players from that IP) / pardon / check an IP (`banned-ips.txt`) |
| `whitelistAdd(name)` / `whitelistRemove(name)` / `isWhitelisted(name)` | Edit / check the white list (`white-list.txt`) |
| `setWhitelist(on)` / `isWhitelistOn()` | Turn the white list on or off / check it |
| `isPvpEnabled()` / `isSpawnMobsEnabled()` | The `pvp` and `spawn-mobs` settings from `server.properties` |
| `mobsIn(dim)` / `highestBlockAt(dim, x, z)` | Mobs in a dimension / top block at x, z |
| `dropItem(dim, x, y, z, item)` / `explode(dim, x, y, z, size)` / `playLevelEvent(dim, x, y, z, id, data)` | Drop an item / explosion / a level event (sound or particle effect) |
| `getFile()` / `dataFolder()` / `pluginManager()` / `plugins()` | Your jar / your folder / the plugin manager / loaded plugins |
| `server().getServerVersion()` / `getGameVersion()` / `getProtocolVersion()` / `getApiVersion()` | Name and build (e.g. `BlockNes 1.7.0 beta`), `0.15.10`, `84`, `1.6` |
| `server().onlinePlayers()` / `getOps()` / `getConfigString(key, def)` / `getConfigInt` / `getConfigBoolean` | Players, operator names, values from `server.properties` |
| `name()`, `version()`, `author()`, `description()` | Plugin metadata |
| `ignite(p, seconds)` / `extinguish(p)` / `isOnFire(p)` | Set a player on fire (goes through `EntityCombust`, which a plugin can cancel) / put the fire out / check it |
| `errorCount()` / `slowCallCount()` | Handler errors and slow calls so far: the counts that `plugin-max-errors` and `plugin-max-slow-calls` look at |
| `saveConfig()` / `isDisabled()` / `isHudVisible(p)` / `getPermission(node)` | Save `config()` now / the opposite of `isEnabled()` / whether the scoreboard is shown to a player / the registered `Permission` for a node |

Colors use the Minecraft `§` codes (`\u00a7a` in Java strings).

### Player

`Player` objects come from events, commands and `players()`:

<details>
<summary>Player methods (15 entries)</summary>

| Call | Description |
| --- | --- |
| `p.getName()` / `getDisplayName()` / `setDisplayName(n)` | Login name / name shown in chat |
| `p.getHealth()` / `getMaxHealth()` / `getFoodLevel()` / `getSaturation()` | Health (0-20), hunger |
| `p.getX()` / `getY()` / `getZ()` / `getYaw()` / `getPitch()` | Position and look direction |
| `p.getDimension()` / `getWorld()` / `getGamemode()` | Dimension, world, game mode |
| `p.getItemInHand()` / `getInventory()` | Item in hand, inventory |
| `p.isOnline()` / `isDead()` / `isSleeping()` / `isOp()` / `setOp(true)` | State and operator flag |
| `p.getAddress()` / `getUniqueId()` | IP address and UUID |
| `p.sendMessage(m)` / `sendPopup(m)` / `sendTip(m)` | Messages |
| `p.kick(reason)` / `teleport(x, y, z)` / `setGamemode(gm)` / `heal(n)` / `damage(n, cause)` / `kill()` / `setHealth(v)` | Same as the helpers above. Health changes go through `PlayerHeal` / `PlayerDamage`, so they can be cancelled |
| `p.getPing()` | Round-trip latency in ms (RakNet ping every second). `-1` until the first answer, and again after 5 s without one |
| `p.getNameTag()` / `setNameTag(text)` | Text above the head, seen by the players who see `p`. `null` = the real name; at most 256 characters |
| `p.isNameTagVisible()` / `setNameTagVisible(bool)` | Show or hide that text |
| `p.getLevel()` / `getPosition()` / `getIp()` | World, position and IP without the port (API 1.6) |
| `p.getPlayerInventory()` | Inventory that updates the client (API 1.6) |
| `p.sendTitle(title, subtitle)` / `addTitle(...)` / `hasPermission(node)` | Titles and permission check (API 1.6) |

</details>

#### Latency and text above the head

`p.getPing()` is measured with RakNet ping/pong: the server sends a ping every second and times the answer. It stays `-1` until an answer arrives, and goes back to `-1` if nothing comes back for 5 seconds.

The text above the head is the nametag. This example shows each player's name and latency, refreshed every second:

```java
runRepeating(0, 20, () -> {
    for (Player p : server().onlinePlayers()) {
        p.setNameTag(p.getName() + " - " + (p.getPing() < 0 ? "?" : p.getPing() + " ms"));
    }
});
```

`setNameTag` sends the change only to the players who see `p`, and only when the text really changes. The tests of this release check what the server sends, not how a real client draws it; check the nametag on a real 0.15.10 client before relying on its layout.

### Compiling your plugin

Compile against `server-plugin-api.jar`, add `plugin.properties` (and an optional `config.properties`) to the jar, and put the result in `plugins/`:

```sh
mkdir -p out plugins
javac -source 11 -target 11 -encoding UTF-8 -cp server-plugin-api.jar -d out $(find src -name '*.java')
cp plugin.properties config.properties out/
jar --create --file plugins/MyPlugin.jar -C out .
```

Restart the server or run `/plugins reload`. If `javac` / `jar` are not available, use `java -m jdk.compiler/com.sun.tools.javac.Main` and `java -m jdk.jartool/sun.tools.jar.Main` instead.

### Minimal example

```java
package example;

import net.BlockNes.plugin.Events;
import net.BlockNes.plugin.Plugin;

public class HelloWorld extends Plugin {
    @Override public void onEnable() {
        on(Events.PlayerJoin.class, e -> message(e.player, "Welcome, " + e.player.name + "!"));
        command("hello", "greets you", "", false, (s, a) -> s.send("Hello " + s.name() + "!"));
        log("ready");
    }
}
```

`plugin.properties`:

```properties
name=HelloWorld
main=example.HelloWorld
version=1.0
author=You
description=Greets players
```
