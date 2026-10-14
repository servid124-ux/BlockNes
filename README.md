<div align="center">

# BlockNes

**1.7.0 beta** · A Minecraft: Pocket Edition **0.15.10** server, written in Java with Netty for networking.

![Version](https://img.shields.io/badge/version-1.7.0--beta-2ea44f?style=flat-square) ![MCPE](https://img.shields.io/badge/MCPE-0.15.10-blue?style=flat-square) ![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square) ![Dependencies](https://img.shields.io/badge/dependencies-Netty%204.2-blue?style=flat-square) ![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20Termux-lightgrey?style=flat-square)

[What's new](#whats-new) · [Quick start](#quick-start) · [Files](#files-and-folders) · [Configuration](#configuration) · [Commands](#commands) · [Plugins](#plugins)

</div>

The only external dependency is Netty 4.2, which runs the UDP transport. It is bundled inside `server.jar`, so there is nothing else to install. Without Netty the server still runs on plain Java sockets. The MCPE network protocol and game logic are implemented in Java, and there is a plugin API, documented in [Plugins.md](Plugins.md).

## What's new

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
Starting BlockNes 1.7.0 beta version 0.15.10 (MCPE protocol 84)
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
| `hud-position` | `right` | `right`, `left` or `center` (the old behaviour). Players can move the scoreboard with `/hud` |
| `hud-screen-width` | `427` | Screen width in GUI pixels used to find the edge. If the text is cut off or too far from the edge, change it (480, 640...) |
| `hud-offset-y` | `0` | Move the scoreboard up (+) or down (-) this many lines |
| `hud-max-lines` | `15` | Lines beyond this are dropped |

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
| `/plugins enable <name>` / `/plugins disable <name>` | Enable or disable one plugin without restarting. `disable` also disables the plugins that depend on it. `enable` re-reads the jar (OP only) |
| `/hud` | Hide / show the scoreboard (any player) |
| `/hud right` \| `left` \| `center` | Scoreboard position |
| `/hud width <px>` | Screen width used for the alignment (160..2000) |
| `/hud y <n>` | Move up (+) or down (-), -20..20 |
| `/hud reset` | Back to the server defaults |
| `ban <player>` / `pardon <player>` | Ban or pardon a player name (banned players are kicked) |
| `ban-ip <ip>` / `pardon-ip <ip>` | Ban or pardon an IP address |
| `banlist` | List banned players and IP addresses |
| `whitelist <on\\|off\\|add\\|remove\\|list\\|reload> [player]` | Turn the white list on or off, edit it, or reload it |
| `/stop` | Save and shut down |

---

## Plugins

Plugins are `.jar` files in `plugins/`. They load on startup and on `/plugins reload`, and a plugin that crashes never takes the server down. The plugin API (version `1.6`) is documented in **[Plugins.md](Plugins.md)**: events, commands, scheduler, menus, recipes, regions, per-player data, the HUD, and how to compile a plugin. The examples in `plugins-ejemplo/` show it in use.
