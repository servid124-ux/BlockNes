<div align="center">

# BlockNes

**1.6.5 beta** · A Minecraft: Pocket Edition **0.15.10** server, written in plain Java.

![Version](https://img.shields.io/badge/version-1.6.5--beta-2ea44f?style=flat-square) ![MCPE](https://img.shields.io/badge/MCPE-0.15.10-blue?style=flat-square) ![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square) ![Dependencies](https://img.shields.io/badge/dependencies-none-brightgreen?style=flat-square) ![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20Termux-lightgrey?style=flat-square)

[What's new](#whats-new) · [Quick start](#quick-start) · [Files](#files-and-folders) · [Configuration](#configuration) · [Commands](#commands) · [Plugin API](#plugin-api) · [RakNetLib](#raknetlib)

</div>

No external dependencies. It implements the MCPE network protocol and game logic in Java and includes a plugin API.

## What's new

**Release 1.6.5 beta: plugins are much less limited.** The defaults no longer disable a plugin after a number of errors or slow calls, and they no longer cap how many tasks a plugin can schedule. The limits can still be turned on in `server.properties`.

The release also brings plugin API 1.6 (`api=1.6`):

- A **server facade** (`Server.getInstance()`, `getLevelByName()`, `getScheduler()`, `getCommandMap()`...) and a **scheduler** with `Task`, `PluginTask` and `AsyncTask`.
- **Event interfaces**: every event can also be listened to by its interface name, for example `on(PlayerChatEvent.class, ...)`.
- `Level`, `Position`, `Vector3`, `TextFormat`, metadata, `Item.get(...)` and `player.getPlayerInventory()`.

Plugins built for earlier versions of the API keep working. Details: [API 1.6 additions](#api-16-additions-inside-plugin).

---

## Quick start

### Requirements

- Compatible with Linux and Termux
- Java 17 or newer (for example 17, 21 or 25). On Termux: `pkg install openjdk-17`
- A Minecraft PE 0.15.10 client

### Downloads

Download the files from the release page. Nothing needs to be compiled.

| File | Purpose |
| --- | --- |
| `server.jar` | The server. Run it with `java -jar server.jar` |
| `server-plugin-api.jar` | Compile-only jar used to build your plugins. Do **not** put it in `plugins/` |
| `RakNetLib.jar` | The RakNet library the server is built on. See [RakNetLib](#raknetlib) to use it in your own project |

### Build from source

Requires JDK 11 or newer.

```sh
sh build.sh      # or: mvn package
sh test.sh       # plugin API test suite
```

`build.sh` writes `server/target/server.jar` and `server/target/server-plugin-api.jar`. The tests write `players/` and `server.log` into the project folder, so remove them before running `sh test.sh` again.

### Run

```sh
java -jar server.jar
```

Run it from the folder where you want the server files to live.

### First run

1. A short wizard asks for a language (`eng`, `spa`, `por`, `fra`, `deu`, `ita`). Press Enter for English. The choice is saved as `language=` in `server.properties`.
2. The wizard asks `Do you want the guided setup and tutorial? [Y/n]` (in the language you picked). **Y** (or Enter) walks you through 14 steps with a progress bar and an explanation for each: server name (MOTD), port, game mode, difficulty, max players, world name, world type, seed, view distance, PvP, mob spawns, whitelist, spawn protection and your OP name. Enter keeps the default shown in `[brackets]`. It ends with a summary and quick usage tips. **N** skips it and keeps the factory settings.
3. `server.properties` is created with default values.
4. The world is generated (`Preparing start region...`) and the console shows `Done`.

The console prints the name and version on startup, for example:

```
Starting BlockNes 1.6.5 beta version 0.15.10 (MCPE protocol 84)
```

Stop the server with `/stop` or Ctrl+C (both save players, worlds and items).

---

## Files and folders

| Path | Purpose |
| --- | --- |
| `server.properties` | Server configuration |
| `ops.txt` | Operators (one name per line) |
| `players/` | One file per player (`<name>.dat`): position, health, inventory and achievements. Also `enderchests.dat` (ender chests) |
| `banned-players.txt` | Banned player names, one per line. Created at startup; managed with `ban` and `pardon` |
| `banned-ips.txt` | Banned IP addresses, one per line. Managed with `ban-ip` and `pardon-ip` |
| `white-list.txt` | White list names, one per line. Used when `white-list=on` |
| `worlds/world/` | Overworld: `level.dat`, `region/` (chunks), `items.dat`, `mobs.dat`, `xp.dat`, `tiles.dat`, `seed.txt`, `time.txt`. Worlds from older versions (`world/`, `world_nether/`) are moved here automatically on the first start |
| `worlds/nether/` | Nether: `level.dat`, `region/` (chunks) |
| `plugins/` | Plugin `.jar` files, plugin data folders and `permissions.properties` |
| `server.log` | Log file (rotated to `server.log.old` at about 5 MB) |

---

## Configuration

Missing keys are added automatically with their default value the next time the server starts.

| Key | Default | Description |
| --- | --- | --- |
| `server-name` | `BlockNes Server` | Internal name, used when `motd` is empty |
| `motd` | `My Minecraft server` | Text shown in the server list |
| `server-port` | `19132` | UDP port (1-65535) |
| `gamemode` | `survival` | `survival` or `creative` (`0` / `1`) |
| `difficulty` | `1` | `0` peaceful, `1` easy, `2` normal, `3` hard (names work too) |
| `max-players` | `20` | 1-1000 |
| `spawn-protection` | `0` | Protected radius around spawn in blocks (OPs ignore it) |
| `view-distance` | `8` | Chunks sent to each player (2-24) |
| `level-name` | `world` | World folder name |
| `level-seed` | *(empty)* | Seed; empty means random. Once `seed.txt` exists in the world folder, it wins |
| `level-type` | `normal` | `normal`, `flat` or `nether` |
| `language` | *(set by wizard)* | `eng`, `spa`, `por`, `fra`, `deu`, `ita` |
| `pvp` | `on` | `off`: players cannot hit other players |
| `spawn-mobs` | `on` | `off`: no natural mob spawns (spawn eggs and `/summon` still work) |
| `white-list` | `off` | `on`: only the names in `white-list.txt` can join |
| `pregenerate-radius` | `0` | Chunks around spawn generated at startup, before players join (0-16). `0` only prepares the spawn area. New chunks may get animals on grass |
| `enable-query` | `true` | Query GS4 on the same UDP port, used by server list tools |
| `enable-rcon` | `false` | RCON over TCP (Source protocol). Uses `rcon.password` to log in |
| `rcon.password` | *(empty)* | Password for RCON |
| `rcon.port` | `19132` | TCP port for RCON (1-65535) |
| `network.upnp-forwarding` | `false` | `true`: open the UDP port on the router with UPnP at startup and close it at shutdown |
| `plugin-max-errors` | `0` | Errors in plugin handlers before the plugin is disabled. `0` = no limit |
| `plugin-max-slow-calls` | `0` | Handler calls slower than 100 ms before the plugin is disabled. `0` = no limit |
| `plugin-max-tasks` | `0` | Pending tasks per plugin; more of them throw `IllegalStateException`. `0` = no limit |

Invalid or out-of-range values fall back to a safe default and a warning is printed.

> The server only writes default values into a **new** `server.properties`. If you already have one, edit `motd` there yourself.

---

## Commands

Commands work from the console (with or without `/`) and in-game chat (starting with `/`). Most commands are OP-only; add yourself with `/op <name>` in the console or by editing `ops.txt`.

| Command | Description |
| --- | --- |
| `/help` | List commands (includes plugin commands) |
| `/version` | Server version |
| `/list` | Online players |
| `/say <message>` | Broadcast a message |
| `/me <action>` | Action message |
| `/tell <player> <message>` | Private message |
| `/kick <player> [reason]` | Kick a player |
| `/gamemode <0\|1> [player]` | Change game mode |
| `/tp [player] <x y z \| player>` | Teleport |
| `/time <set\|add\|query\|stop\|start> [value]` | Time control (`set` accepts `day`, `noon`, `sunset`, `night`, `midnight`, `sunrise`) |
| `/setblock <x> <y> <z> <id> [meta]` | Place a block |
| `/give <player> <id> [amount] [meta]` | Give items |
| `/enchant <player> <enchantment> [level]` | Enchant the item in hand, e.g. `/enchant Steve sharpness 5` |
| `/weather <clear\|rain\|thunder\|storm> [ticks]` | Change the overworld weather (`/weather rain 600`); without ticks it lasts a random time |
| `/summon <mob> [player]` | Spawn a mob next to a player |
| `/kill [player]` / `/heal [player]` | Kill / heal |
| `/op <player>` / `/deop <player>` | Manage operators |
| `/save-all` | Save everything now |
| `/status` | Server status |
| `/debug [on\|off]` | Show every damage / hunger change in your chat |
| `/plugins` | List loaded plugins |
| `/plugins reload` | Reload plugins (OP only) |
| `ban <player>` / `pardon <player>` | Ban or pardon a player name (banned players are kicked) |
| `ban-ip <ip>` / `pardon-ip <ip>` | Ban or pardon an IP address |
| `banlist` | List banned players and IP addresses |
| `whitelist <on\\|off\\|add\\|remove\\|list\\|reload> [player]` | Turn the white list on or off, edit it, or reload it |
| `/stop` | Save and shut down |

---

## Plugin API

Plugins are `.jar` files placed in `plugins/`. They are loaded on startup and on `/plugins reload`. A plugin that crashes never takes the server down.

The API version is `1.6` (`server().getApiVersion()`). Plugins built for earlier versions of this API keep loading, because the API only adds to what was there. Version 1.2 adds `p.getPing()` and the text above the head (`p.setNameTag()`, see *Player*); a plugin that uses them declares `api=1.2`.

Version 1.3 adds the access-control and server-setting helpers listed below (`banPlayer`, `whitelistAdd`, `isPvpEnabled`, ...). A plugin that declares `api=1.3` needs this version or newer; plugins for older versions keep loading.

Version 1.5 adds menus with buttons, recipes added by plugins, regions, per-player data, item names and lore, and argument helpers for commands (see *API 1.5 additions*). A plugin that uses them declares `api=1.5`; plugins for older versions keep loading.

Version 1.6 adds a server facade, a scheduler with `Task`, `PluginTask` and `AsyncTask`, a command map with `CommandExecutor`, event interfaces, `Level`, `Position`, `Vector3`, metadata, `TextFormat`, `Item.get(...)` and `getPlayerInventory()` (see *API 1.6 additions*). A plugin that uses them declares `api=1.6`; plugins for earlier versions keep loading.

**Limits.** There are no limits by default: a handler that throws is logged and the other handlers keep running, and a plugin can have as many pending tasks as it needs. To turn the protection on, set `plugin-max-errors`, `plugin-max-slow-calls` and/or `plugin-max-tasks` in `server.properties` (for example 100, 1000 and 5000). A plugin is then disabled after that many handler errors or slow calls (calls slower than 100 ms), and more pending tasks than the limit make `runLater`, `runRepeating` and the scheduler throw an `IllegalStateException`.

### What plugins can and can't change

Plugins can change most of the game logic. Here is what they can touch, and where the limits are.

**Plugins can change**

- **Players:** chat text, format and recipients; commands; movement; clicks on blocks and air; damage, death and respawn; game mode, health, food and XP; permissions; teleports; kicks and bans.
- **Blocks and the world:** block breaks and placements, sign text, the blocks an explosion destroys, `setBlock` and regions, time, weather and lightning.
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
- World generation: there is no hook to change how new chunks are generated.
- Mob AI and pathfinding, block updates and redstone: there are no events or API for them.
- Furnace smelting and burning: there are no events or API for them.
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

Register with `on(Events.X.class, e -> ...)`. Handlers run on the server thread.

```java
on(Events.PlayerJoin.class, e -> message(e.player, "Welcome " + e.player.name));
on(Events.BlockPlace.class, Priority.HIGH, true, e -> { if (e.id == 46) e.setCancelled(true); });
```

Overloads: `on(type, handler)`, `on(type, priority, handler)`, `on(type, priority, ignoreCancelled, handler)`.
`Priority` order: `LOWEST`, `LOW`, `NORMAL`, `HIGH`, `HIGHEST`, `MONITOR` (last; observe only).

<details>
<summary>All events (39 entries)</summary>

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

</details>

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
| `net.BlockNes.event.player` | `PlayerEvent` (category), `PlayerJoinEvent`, `PlayerQuitEvent`, `PlayerChatEvent`, `PlayerCommandPreprocessEvent`, `PlayerMoveEvent`, `PlayerInteractEvent`, `PlayerDeathEvent`, `PlayerRespawnEvent`, `PlayerGameModeChangeEvent`, `PlayerKickEvent`, `PlayerPreLoginEvent`, `PlayerDropItemEvent`, `PlayerItemConsumeEvent`, `PlayerBedEnterEvent`, `PlayerBedLeaveEvent`, `PlayerToggleSneakEvent`, `PlayerToggleSprintEvent`, `PlayerExhaustEvent`, `PlayerAnimationEvent`, `PlayerItemHeldEvent` |
| `net.BlockNes.event.block` | `BlockBreakEvent`, `BlockPlaceEvent`, `SignChangeEvent` |
| `net.BlockNes.event.entity` | `EntityEvent` (category), `EntityDamageEvent`, `EntityDeathEvent`, `EntitySpawnEvent`, `EntityRegainHealthEvent`, `EntityTeleportEvent` |
| `net.BlockNes.event.inventory` | `InventoryOpenEvent`, `InventoryCloseEvent`, `CraftItemEvent` |
| `net.BlockNes.event.server` | `ServerCommandEvent`, `DataPacketReceiveEvent`, `DataPacketSendEvent` |
| `net.BlockNes.event.level` | `LevelSaveEvent` |
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

| Setting (`server.properties`) | Default | Meaning |
| --- | --- | --- |
| `hud-position` | `right` | `right`, `left` or `center` (the old behaviour) |
| `hud-screen-width` | `427` | Screen width in GUI pixels used to find the edge. If the text is cut off or too far from the edge, change it (480, 640...) |
| `hud-offset-y` | `0` | Move the scoreboard up (+) or down (-) this many lines |
| `hud-max-lines` | `15` | Lines beyond this are dropped |

**Every player can tune it live with `/hud`** (no OP needed), because GUI size differs per device:

| Command | What it does |
| --- | --- |
| `/hud` | Hide / show the scoreboard |
| `/hud right` \| `left` \| `center` | Position |
| `/hud width <px>` | Screen width used for the alignment (160..2000) |
| `/hud y <n>` | Move up (+) or down (-), -20..20 |
| `/hud reset` | Back to the server defaults |

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

New events: `Events.PlayerTeleport` (cancelable, `toX/toY/toZ` editable; fired by `/tp` and `teleportChecked`, not by the server's internal teleports) and `Events.WeatherChange` (cancelable, `type`/`ticks` editable; fired by `/weather` and `setWeather`, not by random weather).

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
| `getName()` / `getVersion()` / `getMinecraftVersion()` / `getApiVersion()` | `BlockNes`, `1.6.5 beta`, `0.15.10`, `1.6` |
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
| `server().getServerVersion()` / `getGameVersion()` / `getProtocolVersion()` / `getApiVersion()` | Name and build (e.g. `BlockNes 1.6.5 beta`), `0.15.10`, `84`, `1.6` |
| `server().onlinePlayers()` / `getOps()` / `getConfigString(key, def)` / `getConfigInt` / `getConfigBoolean` | Players, operator names, values from `server.properties` |
| `name()`, `version()`, `author()`, `description()` | Plugin metadata |

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

---

## RakNetLib

RakNetLib is the RakNet implementation the server runs on: UDP sockets, the connection handshake, reliable delivery (acknowledgements, resends and ordering) and server-list pings. It has no dependencies, so you can use it in any Java project. Put `RakNetLib.jar` on your classpath. The package is `net.BlockNes.raknet`.

### Server

```java
import java.nio.charset.StandardCharsets;
import net.BlockNes.raknet.RakNet;
import net.BlockNes.raknet.RakNetServer;
import net.BlockNes.raknet.Session;

public class RakNetEchoServer {
    public static void main(String[] args) throws Exception {
        RakNetServer server = new RakNetServer(19133);
        server.setLogger(System.out::println);
        server.setErrorLogger(System.err::println);
        server.setMotd(() -> "Echo server");
        server.setListener(new RakNetServer.Listener() {
            @Override public void onOpen(Session s) {
                System.out.println("open: " + s.address);
            }
            @Override public void onPacket(Session s, byte[] buffer) {
                // buffer[0] is the packet ID (0xFE for game packets). The reply is the same data plus "pong" at the end
                byte[] pong = "pong".getBytes(StandardCharsets.UTF_8);
                byte[] reply = new byte[buffer.length + pong.length];
                System.arraycopy(buffer, 0, reply, 0, buffer.length);
                System.arraycopy(pong, 0, reply, buffer.length, pong.length);
                s.sendEncapsulated(reply, RakNet.RELIABLE_ORDERED, 0, false);
            }
            @Override public void onClose(Session s, String reason) {
                System.out.println("close: " + s.address + " " + reason);
            }
        });
        server.bind();
        System.out.println("listening on UDP port " + server.getPort());
        server.run();   // blocks until server.stop() is called
    }
}
```

It listens on UDP port 19133. Every connected client gets a `Session`, and every packet from it arrives in `onPacket`.

### Client

```java
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.BlockNes.raknet.RakNetClient;

public class RakNetEchoClient {
    public static void main(String[] args) throws Exception {
        InetSocketAddress server = new InetSocketAddress("127.0.0.1", 19133);
        System.out.println("motd: " + RakNetClient.ping(server, 2000));

        CountDownLatch done = new CountDownLatch(1);
        RakNetClient client = new RakNetClient(server);
        client.setListener(new RakNetClient.Listener() {
            @Override public void onConnected() {
                System.out.println("connected, sending hello");
                client.sendGame("hello".getBytes(StandardCharsets.UTF_8));   // sendGame adds the 0xFE
            }
            @Override public void onPacket(byte[] buffer) {
                String text = new String(buffer, 1, buffer.length - 1, StandardCharsets.UTF_8);   // skips the 0xFE
                System.out.println("reply: " + text);
                client.disconnect();
            }
            @Override public void onDisconnected(String reason) {
                System.out.println("disconnected: " + reason);
                done.countDown();
            }
        });
        client.start();
        done.await(5, TimeUnit.SECONDS);
        System.out.println("client done");
    }
}
```

Running the two programs prints:

```
motd: Echo server
connected, sending hello
reply: hellopong
disconnected: Disconnected
client done
```

### Compile and run

```sh
javac -cp RakNetLib.jar RakNetEchoServer.java RakNetEchoClient.java
java -cp .:RakNetLib.jar RakNetEchoServer     # first terminal
java -cp .:RakNetLib.jar RakNetEchoClient     # second terminal
```

On Windows, use `;` instead of `:` in `-cp`.

### How it works

- **Packet IDs.** The first byte of a packet decides who handles it. IDs below `0x80` belong to RakNet itself (pings, connection setup), so your data must start with `0x80` or higher. Minecraft PE uses `0xFE`. `client.sendGame(data)` and `session.sendGame(data, ...)` add that byte for you.
- **Reliability.** `RakNet.UNRELIABLE` (0), `UNRELIABLE_SEQUENCED` (1), `RELIABLE` (2), `RELIABLE_ORDERED` (3, use this for most data), `RELIABLE_SEQUENCED` (4), and the `_WITH_ACK_RECEIPT` versions (5, 6, 7). The receipt versions report back through `onAckReceipt` whether the packet arrived.
- **Channels.** Packets on the same ordered channel arrive in order. Use channel `0` unless you need separate streams.
- **Threads.** `server.run()` blocks its thread until `server.stop()` is called. From other threads, queue work with `server.execute(() -> ...)`. `client.send(...)` already queues the packet for you, and `client.execute(...)` works the same way.
- **Callbacks** run on the library's thread, so keep them short.
- **Logs.** `setLogger` and `setErrorLogger` receive the library's messages (`Consumer<String>`), so they can go to your own log.

### Server API

| Method | Description |
| --- | --- |
| `new RakNetServer(port)` | Server on a UDP port |
| `setMotd(Supplier<String>)` | Text answered to server-list pings |
| `setListener(...)` | `onOpen`, `onPacket`, `onClose` and `onAckReceipt` |
| `setMaxConnections(n)` / `setMaxConnectionsPerIp(n)` | Connection limits |
| `ban(ip, millis)` / `unban(ip)` / `isBanned(ip)` | Bans by IP address |
| `setAcceptedProtocols(...)` | RakNet protocol versions to accept |
| `getSessions()` / `getSession(address)` | Connected sessions |
| `send(address, bytes)` | Raw datagram to an address |
| `bind()` / `run()` / `stop()` | Open the port, run the loop, stop it |

### Session API (server side)

| Method | Description |
| --- | --- |
| `address` | The client's `InetSocketAddress` |
| `sendEncapsulated(bytes, reliability, channel, immediate)` | Send data; returns a receipt id |
| `sendGame(bytes, reliability, channel, immediate)` | Same, adding the `0xFE` ID |
| `kick(reason)` | Disconnect the client with a reason |
| `close(reason, notifyClient)` | Close the session |
| `getPing()` / `getState()` | Connection info |
| `getAttachment()` / `setAttachment(obj)` | Store your own data for the player |

### Client API

| Method | Description |
| --- | --- |
| `new RakNetClient(address)` | Client for a server address |
| `setListener(...)` | `onConnected`, `onPacket`, `onDisconnected` and `onAckReceipt` |
| `start()` / `run()` | Connect in a background thread / in the current thread |
| `send(bytes, reliability, channel)` | Send data (thread-safe) |
| `sendGame(bytes)` | Send data with the `0xFE` ID, reliable and ordered |
| `disconnect()` | Close the connection |
| `isConnected()` / `getPing()` / `getMtu()` | Connection info |
| `RakNetClient.ping(address, timeoutMs)` | Server-list ping: returns the MOTD, or `null` if nothing answers |
