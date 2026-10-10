# BlockNes 1.4.0 beta

A Minecraft: Pocket Edition **0.15.10** (protocol 84) server written in plain Java, with no external dependencies. It implements the MCPE network protocol and game logic in Java and includes a plugin API. Some mechanics may still differ from the original game.

> **Beta:** the server is already playable, but some features are still missing or unfinished (for example, skeletons do not raise their bow arm yet).

---

## Downloads

Two jars are published:

| Jar | Purpose |
| --- | --- |
| `server.jar` | The server itself (`java -jar server.jar`) |
| `plugin-api.jar` | Compile-only jar used to build your plugins. Do **not** put it in `plugins/` |

## Requirements

- Java 11 or newer (on Termux: `pkg install openjdk-17`)
- A Minecraft PE 0.15.10 client

## Starting the server

```sh
java -jar server.jar
```

Run it from the folder where you want the server files to live.

### First run

1. A short wizard asks for a language (`eng`, `spa`, `por`, `fra`, `deu`, `ita`). Press Enter for English. The choice is saved as `language=` in `server.properties`.
2. `server.properties` is created with default values.
3. The world is generated (`Preparing start region...`) and the console shows `Done`.

The console prints the name and version on startup, for example:

```
Starting BlockNes 1.4.0 beta version 0.15.10 (MCPE protocol 84)
```

Stop the server with `/stop` or Ctrl+C (both save players, worlds and items).

### Files and folders created

| Path | Purpose |
| --- | --- |
| `server.properties` | Server configuration |
| `ops.txt` | Operators (one name per line) |
| `players/` | One file per player (`<name>.dat`): position, health, inventory. Also `enderchests.dat` (ender chests) |
| `banned-players.txt` | Banned player names, one per line. Created at startup; managed with `ban` and `pardon` |
| `banned-ips.txt` | Banned IP addresses, one per line. Managed with `ban-ip` and `pardon-ip` |
| `white-list.txt` | White list names, one per line. Used when `white-list=on` |
| `worlds/world/` | Overworld: `level.dat`, `region/` (chunks), `items.dat`, `mobs.dat`, `xp.dat`, `tiles.dat`, `seed.txt`, `time.txt`. Worlds from older versions (`world/`, `world_nether/`) are moved here automatically on the first start |
| `worlds/nether/` | Nether: `level.dat`, `region/` (chunks) |
| `plugins/` | Plugin `.jar` files, plugin data folders and `permissions.properties` |
| `server.log` | Log file (rotated to `server.log.old` at about 5 MB) |

---

## Configuration (`server.properties`)

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

Invalid or out-of-range values fall back to a safe default and a warning is printed.

> The server only writes default values into a **new** `server.properties`. If you already have one, edit `motd` there yourself.

---

## Console and in-game commands

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

The API version is `1.3` (`server().getApiVersion()`). Plugins built for earlier versions of this API keep loading, because the API only adds to what was there. Version 1.2 adds `p.getPing()` and the text above the head (`p.setNameTag()`, see *Player*); a plugin that uses them declares `api=1.2`.

Version 1.3 adds the access-control and server-setting helpers listed below (`banPlayer`, `whitelistAdd`, `isPvpEnabled`, ...). A plugin that declares `api=1.3` needs this version or newer; plugins for older versions keep loading.

**Limits.** A handler that throws 100 times, or takes more than 100 ms in 1000 calls, disables its plugin; the server keeps running. A plugin can have up to 5000 pending tasks; more `runLater` or `runRepeating` calls throw an `IllegalStateException`.

### Plugin layout

Compile your plugin against `plugin-api.jar`; the finished plugin jar goes in `plugins/`. A plugin jar needs a `plugin.properties` file in its root and a main class that extends `mcpe.plugin.Plugin` and has a public no-argument constructor.

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
- `api`: the minimum API version your plugin needs. This server has API `1.2`; a plugin that asks for a newer one is not loaded and a warning is printed.
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

Events, commands, tasks, permissions and attachments registered by a plugin are removed automatically after `onDisable()`.

Plugins can be switched individually without restarting: `/plugins disable <name>` (also disables plugins that `depend` on it) and `/plugins enable <name>` (OP only; re-reads the jar). `/plugins reload` still reloads everything.

### Events

Register with `on(Events.X.class, e -> ...)`. Handlers run on the server thread.

```java
on(Events.PlayerJoin.class, e -> message(e.player, "Welcome " + e.player.name));
on(Events.BlockPlace.class, Priority.HIGH, true, e -> { if (e.id == 46) e.setCancelled(true); });
```

Overloads: `on(type, handler)`, `on(type, priority, handler)`, `on(type, priority, ignoreCancelled, handler)`.
`Priority` order: `LOWEST`, `LOW`, `NORMAL`, `HIGH`, `HIGHEST`, `MONITOR` (last; observe only).

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
| `SignChange` | yes | `x`, `y`, `z`, `lines` (4, editable) |
| `Explosion` | yes | `dimension`, `x`, `y`, `z`, `size`, `blocks` (`{x,y,z,id}` list; remove entries to save blocks) |
| `ServerCommand` | yes | console only: `sender`, `line` (editable) |
| `PacketReceive` | yes | `id`, `data` (raw packet from a player, before the server handles it) |
| `PacketSend` | yes | `data` (raw packet sent through `Server.send`) |
| `LevelSave` | no | fired after every world save |
| `PluginEnable` / `PluginDisable` | no | `plugin` |

#### `@EventHandler` listeners (PocketMine / Bukkit style)

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
import mcpe.command.CommandSender;
import mcpe.plugin.Command;

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
**tip** (center of the screen) and **popup** (above the hotbar). It redraws every 10 ticks, only resends when something changed, and removes everything a plugin registered when that plugin is disabled.

```java
// scoreboard: one line per key, sorted by order; a global line is evaluated for every player
hudLine("hp", 1, p -> "\u00a7cHP: " + p.health);
hudLine(player, "money", 2, "\u00a76$" + balance);       // per-player line; text = null removes it
hudRemove("hp");  hudRemove(player, "money");  hudClear(player);

title(player, "\u00a7aWelcome", "\u00a77enjoy", 60);      // center, 60 ticks (covers the scoreboard while it lasts)
actionBar(player, "\u00a7eSaved!", 40);                   // above the hotbar (covers boss bars while it lasts)
bossBar(player, "dragon", "\u00a7cEnder Dragon", 0.65);   // text + 24-segment progress bar; same key = update
removeBossBar(player, "dragon");

String[] onScreen = hudView(player);                      // {popup, tip} currently shown
```

`Events.PlayerHud` fires per player on every redraw (only if someone listens): edit `lines` (the scoreboard) or set `popup`, so several plugins can add to the same HUD without knowing each other.
It is an emulation: the "scoreboard" is centered text, not a side panel, and the boss bar is text, not the real bar.

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
| `server().getServerVersion()` / `getGameVersion()` / `getProtocolVersion()` / `getApiVersion()` | Name and build (e.g. `BlockNes 1.4.0 beta`), `0.15.10`, `84`, `1.3` |
| `server().onlinePlayers()` / `getOps()` / `getConfigString(key, def)` / `getConfigInt` / `getConfigBoolean` | Players, operator names, values from `server.properties` |
| `name()`, `version()`, `author()`, `description()` | Plugin metadata |

Colors use the Minecraft `§` codes (`\u00a7a` in Java strings).

### Player

`Player` objects come from events, commands and `players()`:

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

Compile against `plugin-api.jar`, add `plugin.properties` (and an optional `config.properties`) to the jar, and put the result in `plugins/`:

```sh
mkdir -p out plugins
javac -source 11 -target 11 -encoding UTF-8 -cp plugin-api.jar -d out $(find src -name '*.java')
cp plugin.properties config.properties out/
jar --create --file plugins/MyPlugin.jar -C out .
```

Restart the server or run `/plugins reload`. If `javac` / `jar` are not available, use `java -m jdk.compiler/com.sun.tools.javac.Main` and `java -m jdk.jartool/sun.tools.jar.Main` instead.

### Minimal example

```java
package example;

import mcpe.plugin.Events;
import mcpe.plugin.Plugin;

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
