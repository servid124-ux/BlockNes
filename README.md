<div align="center">

# BlockNes

**1.2.0** · A Minecraft: Pocket Edition **0.15.10** server, written in Java with Netty for networking.

![Version](https://img.shields.io/badge/version-1.2.0-2ea44f?style=flat-square) ![MCPE](https://img.shields.io/badge/MCPE-0.15.10-blue?style=flat-square) ![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square) ![Dependencies](https://img.shields.io/badge/dependencies-Netty%204.2-blue?style=flat-square) ![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20Termux-lightgrey?style=flat-square)

[Versions](#versions) · [What's new](#whats-new) · [Quick start](#quick-start) · [Files](#files-and-folders) · [Configuration](#configuration) · [Commands](#commands) · [Plugins](#plugins)

</div>

The only external dependency is Netty 4.2, which runs the UDP transport. It is bundled inside `server.jar`, so there is nothing else to install. Without Netty the server still runs on plain Java sockets. The MCPE network protocol and game logic are implemented in Java, and there is a plugin API, documented in [Plugins.md](Plugins.md).

## Versions

- **1.2.x** is the current line. The next releases are 1.2.1, 1.2.2 and 1.2.3, and the line ends at **1.2.9**: until then, every possible improvement goes into 1.2.x.
- **1.3.0** and support for **Minecraft PE 0.16.0** come after 1.2.9, when there is room for them.
- Clients must use Minecraft PE **0.15.10**, the version the server reports when it starts.

## What's new

**Release 1.2.0: plugin API 1.7 and a plugin-controlled scoreboard.** The plugin API is now version `1.7` (`api=1.7`), and it includes the new events and `Plugin.tr(...)`. The scoreboard corner, vertical offset and width are now set by the plugin that draws it (`hudPosition`, `hudOffsetY` and `hudScreenWidth`, which defaults to 427 px). The `/hud` command is gone. New scripts: `Start.sh` installs and starts the server, and `compiler.sh` builds a plugin. Details: [API 1.7 additions](Plugins.md#api-17-additions-inside-plugin) and [CHANGELOG.md](CHANGELOG.md).

**Release 1.7.0 beta: Netty transport and bug fixes.** RakNet's UDP transport runs on Netty 4.2 when it is available, and `server.jar` already carries it. Start the server with `-Draknet.transport=socket` to use the plain-socket transport instead. The release also fixes player-data keys that depended on the system language, a full player-file rewrite on every logout, a legacy migration that could drop players, and log flooding from malformed packets, getting off boats and minecarts, and the enchanting table. It also adds many plugin events (see [Events](Plugins.md#events)). Plugins that import `net.BlockNes.raknet` must switch to `raknetlib` (see [Plugins.md](Plugins.md#plugin-api)). Details: [CHANGELOG.md](CHANGELOG.md).

**Release 1.6.5 beta: plugins are much less limited.** The defaults no longer disable a plugin after a number of errors or slow calls, and they no longer cap how many tasks a plugin can schedule. The limits can still be turned on in `server.properties`.

The release also brings plugin API 1.6 (`api=1.6`):

- A **server facade** (`Server.getInstance()`, `getLevelByName()`, `getScheduler()`, `getCommandMap()`...) and a **scheduler** with `Task`, `PluginTask` and `AsyncTask`.
- **Event interfaces**: every event can also be listened to by its interface name, for example `on(PlayerChatEvent.class, ...)`.
- `Level`, `Position`, `Vector3`, `TextFormat`, metadata, `Item.get(...)` and `player.getPlayerInventory()`.

Plugins built for earlier versions of the API keep working. Details: [API 1.6 additions](Plugins.md#api-16-additions-inside-plugin).

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

### Run

Create an empty folder for the server, put `server.jar` in it, and start it from there. Worlds, plugins, logs and settings are created in that same folder.

```sh
mkdir blocknes
cd blocknes
# copy server.jar into this folder, then:
java -jar server.jar
```

To give Java more memory, put the options before `-jar`, for example `java -Xms128M -Xmx512M -jar server.jar`. To update, replace `server.jar` with the new release and start it again from the same folder.

#### Terminal session title (Termux / Linux)

The Java server updates the terminal-session title automatically; no wrapper script is needed for that. Start it normally from the server folder with `java -jar server.jar`. The Termux/Linux session label shows the configured `server-name` and the server state at a glance: `🟢 BlockNes • 👥 3/20 • 20 TPS • RAM ~184 MiB` while it is online (the icon follows the TPS: 🟢 18 and up, 🟡 12 to 17, 🔴 below 12), `⏳ BlockNes • Spawn 40%` while it starts (stage and progress), and `⚫ BlockNes • Detenido` after it stops. While the server is online the title is checked every 10 seconds, but it is rewritten only when something visible changes (players, TPS, or RAM rounded to 8 MiB), so the checks do not cause a notification each time. Use `-Dblocknes.title.refresh=30` to change the interval (minimum 5 seconds), or `-Dblocknes.title.refresh=0` to keep the title fixed at startup, when ready and at shutdown. If you switch to another terminal tab, the server keeps running in its original session; do not close that session or stop the Java process if you want it to stay online.

To choose the JVM heap limit, pass options directly to Java, for example `java -Xms128M -Xmx512M -jar server.jar`. The title shows the process's resident RAM where Linux/Termux exposes `/proc`; on other platforms it falls back to occupied JVM heap. The figure is refreshed together with the rest of the title, so it stays current while the server runs.

### First run

1. A short wizard asks for a language (`eng`, `spa`, `por`, `fra`, `deu`, `ita`). Press Enter for English. The choice is saved as `language=` in `server.properties`.
2. The wizard asks `Do you want the guided setup and tutorial? [Y/n]` (in the language you picked). **Y** (or Enter) walks you through 14 steps with a progress bar and an explanation for each: server name (MOTD), port, game mode, difficulty, max players, world name, world type, seed, view distance, PvP, mob spawns, whitelist, spawn protection and your OP name. Enter keeps the default shown in `[brackets]`. It ends with a summary and quick usage tips. **N** skips it and keeps the factory settings.
3. `server.properties` is created with default values, and `blocknes.yml` with the advanced settings.
4. The world is generated (`Preparing start region...`) and the console shows `Done`.

The console prints the name and version on startup, for example:

```
Starting BlockNes 1.2.0 version 0.15.10 (MCPE protocol 84)
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
| `worlds/world/` | Overworld: `level.dat`, `type.txt` (`flat`, `plano` or `normal`; written by the server if missing), `region/` (chunks; `region/generador.txt` is the type the saved chunks were made with), `items.dat`, `mobs.dat`, `xp.dat`, `caida.dat`, `tiles.dat`, `seed.txt`, `time.txt`. Worlds from older versions (`world/`, `world_nether/`) are moved here automatically on the first start |
| `worlds/nether/` | Nether: `level.dat`, `region/` (chunks) |
| `plugins/` | Plugin `.jar` files, source plugins (folders / `.java`), plugin data folders and `permissions.properties` |
| `logs/server.log` | Log file. When it reaches `logs.max-size-mb` (5 MB by default) it is moved to `logs/server.log.old`. An old `server.log` from an earlier version is moved into `logs/` automatically |
| `blocknes.yml` | Advanced settings (console, log size, autosave, item, XP orb and mob limits). Created on the first start. See [Advanced settings](#advanced-settings-blocknesyml) |

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
| `spawn-protection` | `0` | Protected radius around spawn in blocks (OPs ignore it). It moves with `/spawn` |
| `view-distance` | `8` | Chunks sent to each player (2-24) |
| `level-name` | `world` | Folder inside `worlds/` for the normal world. `/spawn <world>` changes it and saves it here |
| `level-seed` | *(empty)* | Seed; empty means random. Once `seed.txt` exists in the world folder, it wins |
| `level-type` | `normal` | `normal`, `flat` or `nether`. Sets the type of **new** worlds only; an existing world keeps the type in its `type.txt` |
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
| `hud-max-lines` | `15` | Lines beyond this are dropped |

Invalid or out-of-range values fall back to a safe default and a warning is printed. The scoreboard corner, vertical offset and width are set by the plugin that draws it (`hudPosition`, `hudOffsetY`, `hudScreenWidth`); there is no `/hud` command.

> The server only writes default values into a **new** `server.properties`. If you already have one, edit `motd` there yourself.

---

### Advanced settings (blocknes.yml)

The server creates `blocknes.yml` in its folder on the first start. It holds the advanced settings; `server.properties` keeps the basic ones. Restart the server after changing it. A missing setting uses its default value. A wrong value or an unknown key is reported in the console and ignored.

| Setting | Default | Range | What it does |
| --- | --- | --- | --- |
| `console.color` | `true` | `true` / `false` | Colors in the console. `NO_COLOR=1` turns them off too |
| `console.debug` | `false` | `true` / `false` | Debug messages, in the console and in `logs/server.log` |
| `logs.max-size-mb` | `5` | 1-1024 | `logs/server.log` is moved to `logs/server.log.old` when it reaches this size |
| `saves.autosave-minutes` | `5` | 1-60 | Every how many minutes the worlds, players and entities are saved |
| `items.despawn-seconds` | `300` | 10-86400 | Seconds a dropped item stays on the ground |
| `xp-orbs.lifetime-seconds` | `60` | 5-3600 | Seconds an XP orb stays on the ground |
| `mobs.max-total` | `100` | 50-1000 | Maximum mobs in each world. Hostile mobs stop spawning 40 below this number |
| `mobs.hostile-per-player` | `6` | 1-50 | Maximum hostile mobs near each player |
| `mobs.animals-max` | `40` | 0-500 | Maximum animals in each world |
| `mobs.animals-per-player` | `10` | 0-100 | Maximum animals near each player |

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
| `/spawn` | OP: sets the world spawn to the block you are standing on. New players and players without a bed appear there. Saved with the world. Not from the Nether |
| `/spawn <player>` | OP: teleports you to that player |
| `/spawn <world>` | OP: makes `worlds/<world>` the normal world, if that folder exists, without restarting. Everyone in the Overworld goes to its spawn, and it stays that way after a restart (`level-name` is saved). A player with that name is checked first. Beds from the old world are forgotten. The world type is the folder's `type.txt` (`flat`, `plano` or `normal`). A folder without one keeps the type of its saved chunks, or takes `level-type` if it has no chunks, and `type.txt` is written with that type. If the saved chunks were made by the other type, they are moved to `region-<old type>-<date>/` (not deleted) and the world is generated again. Not from the Nether |
| `/pos [player]` | Show coordinates and dimension (anyone for themselves, OP for others) |
| `/time <set\|add\|query\|stop\|start> [value]` | Time control (`set` accepts `day`, `noon`, `sunset`, `night`, `midnight`, `sunrise`) |
| `/setblock <x> <y> <z> <id> [meta]` | Place a block |
| `/give <player> <id> [amount] [meta]` | Give items |
| `/clear [player]` | Empty the inventory and armor (OP only) |
| `/xp <amount>[L] [player]` | Give XP points, or levels with `L` (e.g. `/xp 5L`) (OP only) |
| `/enchant <player> <enchantment> [level]` | Enchant the item in hand, e.g. `/enchant Steve sharpness 5` |
| `/weather <clear\|rain\|thunder\|storm> [ticks]` | Change the overworld weather (`/weather rain 600`); without ticks it lasts a random time |
| `/difficulty <peaceful\|easy\|normal\|hard>` | Change the difficulty until the server restarts (`server.properties` is not changed) |
| `/summon <mob> [player]` | Spawn a mob next to a player |
| `/kill [player]` / `/heal [player]` | Kill / heal |
| `/op <player>` / `/deop <player>` | Manage operators |
| `/save-all` | Save everything now |
| `/save-on` / `/save-off` | Turn autosave on or off (`/save-all` still saves everything) |
| `/status` | Server status |
| `/debug [on\|off]` | Show every damage / hunger change in your chat |
| `/plugins` | List loaded plugins |
| `/plugins reload` | Reload plugins (OP only) |
| `/plugins enable <name>` / `/plugins disable <name>` | Enable or disable one plugin without restarting. `disable` also disables the plugins that depend on it. `enable` re-reads the jar (OP only) |
| `ban <player>` / `pardon <player>` | Ban or pardon a player name (banned players are kicked) |
| `ban-ip <ip>` / `pardon-ip <ip>` | Ban or pardon an IP address |
| `banlist` | List banned players and IP addresses |
| `whitelist <on\\|off\\|add\\|remove\\|list\\|reload> [player]` | Turn the white list on or off, edit it, or reload it |
| `/stop` | Save and shut down |

---

## Plugins

Plugins are `.jar` files in `plugins/`, or plain source code (a folder with `plugin.properties` and `.java` files, or a single `.java` file) that the server compiles itself, so you can test without building a jar (needs a JDK; see [Source plugins](Plugins.md#source-plugins-no-jar-needed)). They load on startup and on `/plugins reload`, and a plugin that crashes never takes the server down. The plugin API (version `1.7`) is documented in **[Plugins.md](Plugins.md)**: events, commands, scheduler, menus, recipes, regions, per-player data, the HUD, and how to compile a plugin. The examples in `plugins-ejemplo/` show it in use.
