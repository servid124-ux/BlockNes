<div align="center">

<img src="logo.svg" alt="BlockNes logo" width="150">

# 🧱 BlockNes

**A Minecraft: Pocket Edition 0.15.10 server, written in Java**

![Release](https://img.shields.io/github/v/release/servid124-ux/BlockNes?style=flat-square&color=2ea44f) ![Downloads](https://img.shields.io/github/downloads/servid124-ux/BlockNes/total?style=flat-square) ![Last commit](https://img.shields.io/github/last-commit/servid124-ux/BlockNes?style=flat-square) ![MCPE](https://img.shields.io/badge/MCPE-0.15.10-blue?style=flat-square) ![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square) ![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20Termux-lightgrey?style=flat-square) ![Plugin API](https://img.shields.io/badge/plugin%20API-1.8-8250df?style=flat-square)

📥 [Download](https://github.com/servid124-ux/BlockNes/releases/latest) · 📝 [Changelog](CHANGELOG.md) · 🔌 [Plugin API](Plugins.md) · 🐛 [Report a bug](https://github.com/servid124-ux/BlockNes/issues)

</div>

> ⚠️ **NOT an official or vanilla server.** BlockNes is a custom, independent project. It is not affiliated with Mojang Studios or Microsoft.

BlockNes implements the Minecraft PE network protocol and the game logic in Java. Networking comes from **RakNetLib**, bundled inside `server.jar` and running on plain Java sockets, so there are no external dependencies to install.

## ✨ Features

- 🔌 **Plugin API**: write plugins as `.jar` files or as plain `.java` source that the server compiles itself. Reload them with `/plugins reload`, or enable and disable one without restarting.
- 🌍 **Survival and creative** in the overworld (`normal` or `flat` worlds) and in the Nether.
- 🧱 **OneBlock mini-game** built in: set `level-type=oneblock` and break the single block to progress through phases (see `/oneblock`).
- 🌐 **Guided setup** in six languages: English, Spanish, Portuguese, French, German and Italian.
- 🛡️ **Admin tools**: operators, white list, bans by name and by IP, RCON and Query (GS4).
- 🖥️ **Live terminal title** on Linux and Termux, with players, TPS and RAM.
- 💾 **Autosave**, log rotation and UPnP port forwarding.

## 📦 Quick start

### Requirements

- 🐧 Linux or Termux
- ☕ Java 17 or newer (for example 17, 21 or 25). On Termux: `pkg install openjdk-17`
- 📱 A Minecraft PE **0.15.10** client. The server prints this version when it starts.

### Download

Get the files from the [latest release](https://github.com/servid124-ux/BlockNes/releases/latest). Nothing needs to be compiled.

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

To give Java more memory, put the options before `-jar`, for example `java -Xms128M -Xmx512M -jar server.jar`.

### Update

Replace `server.jar` with the new release and start it again from the same folder. Your worlds, players and settings stay where they are. Check the [CHANGELOG](CHANGELOG.md) to see what changed.

### First run

1. A short wizard asks for a language (`eng`, `spa`, `por`, `fra`, `deu`, `ita`). Press Enter for English. The choice is saved as `language=` in `server.properties`.
2. The wizard asks `Do you want the guided setup and tutorial? [Y/n]`, in the language you picked. **Y** (or Enter) walks you through 14 steps with a progress bar and an explanation for each: server name (MOTD), port, game mode, difficulty, max players, world name, world type, seed, view distance, PvP, mob spawns, whitelist, spawn protection and your OP name. Enter keeps the default shown in `[brackets]`. It ends with a summary and quick usage tips. **N** skips it and keeps the factory settings.
3. `server.properties` is created with default values, and `blocknes.yml` with the advanced settings.
4. The world is generated (`Preparing start region...`) until the console shows `Done`.

On startup the console prints the name, the server version and the MCPE version:

```
Starting BlockNes <version> version 0.15.10 (MCPE protocol 84)
```

Stop the server with `/stop` or Ctrl+C. Both save players, worlds and items.

#### 🖥️ Terminal title (Termux / Linux)

The server sets the terminal-session title itself, so no wrapper script is needed. It shows the `server-name` and the server state:

- 🟢 `BlockNes • 👥 3/20 • 20 TPS • RAM ~184 MiB` while online. The icon follows the TPS: 🟢 18 and up, 🟡 12 to 17, 🔴 below 12.
- ⏳ `BlockNes • Spawn 40%` while starting (stage and progress).
- ⚫ `BlockNes • Detenido` after it stops. (This one word is always Spanish, whatever `language` is set to.)

While online, the title is checked every 10 seconds, but it is rewritten only when something visible changes (players, TPS, or RAM rounded to 8 MiB). Use `-Dblocknes.title.refresh=30` to change the interval (minimum 5 seconds), or `-Dblocknes.title.refresh=0` to keep the title fixed at startup, when ready and at shutdown. The RAM figure is the process's resident memory where Linux/Termux exposes `/proc`, and the occupied JVM heap elsewhere.

> 💡 Switching to another terminal tab is fine: the server keeps running in its original session. Do not close that session if you want the server to stay online.

## 🗂️ Files and folders

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

## ⚙️ Configuration

Missing keys are added with their default value the next time the server starts. Existing values are kept, so if you already have a `server.properties`, change `motd` there yourself.

| Key | Default | Description |
| --- | --- | --- |
| `server-name` | `BlockNes Server` | Internal name, used when `motd` is empty |
| `motd` | `§aWelcome to BlockNes!` | Text shown in the server list |
| `server-port` | `19132` | UDP port (1-65535) |
| `gamemode` | `survival` | `survival` or `creative` (`0` / `1`) |
| `difficulty` | `1` | `0` peaceful, `1` easy, `2` normal, `3` hard (names work too) |
| `max-players` | `20` | 1-1000 |
| `spawn-protection` | `0` | Protected radius around spawn in blocks (OPs ignore it). It moves with `/spawn` |
| `view-distance` | `8` | Chunks sent to each player (2-24) |
| `level-name` | `world` | Folder inside `worlds/` for the normal world. `/spawn <world>` changes it and saves it here |
| `level-seed` | *(empty)* | Seed; empty means random. Once `seed.txt` exists in the world folder, it wins |
| `level-type` | `normal` | `normal`, `flat`, `nether` or `oneblock` (one block in the void: breaking it drops its items and another block appears from the current phase). Sets the type of **new** worlds only; an existing world keeps the type in its `type.txt` |
| `language` | *(set by wizard)* | `eng`, `spa`, `por`, `fra`, `deu`, `ita` |
| `pvp` | `on` | `off`: players cannot hit other players |
| `spawn-mobs` | `on` | `off`: no natural mob spawns (spawn eggs and `/summon` still work) |
| `white-list` | `off` | `on`: only the names in `white-list.txt` can join |
| `pregenerate-radius` | `0` | Chunks around spawn generated at startup, before players join (0-16). `0` only prepares the spawn area. New chunks may get animals on grass |
| `enable-query` | `true` | Query GS4 on the same UDP port, used by server list tools |
| `enable-rcon` | `false` | RCON over TCP (Source protocol). Uses `rcon.password` to log in |
| `rcon.password` | *(empty)* | Password for RCON |
| `rcon.port` | `19132` | TCP port for RCON (1-65535). The game uses UDP, so both can share the number; change it if another program already uses that TCP port |
| `network.upnp-forwarding` | `false` | `true`: open the UDP port on the router with UPnP at startup and close it at shutdown |
| `plugin-max-errors` | `0` | Errors in plugin handlers before the plugin is disabled. `0` = no limit |
| `plugin-max-slow-calls` | `0` | Handler calls slower than 100 ms before the plugin is disabled. `0` = no limit |
| `plugin-max-tasks` | `0` | Pending tasks per plugin; more of them throw `IllegalStateException`. `0` = no limit |
| `hud-max-lines` | `15` | Maximum scoreboard lines shown per player (1-40). Lines beyond this are dropped |

Invalid or out-of-range values fall back to a safe default and a warning is printed. The scoreboard position, vertical offset and width are set by the plugin that draws it (`hudPosition`, `hudOffsetY`, `hudScreenWidth`). There is no `/hud` command.

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

## ⌨️ Commands

Commands work from the console (with or without `/`) and in-game chat (starting with `/`). Most commands are OP-only; add yourself with `/op <name>` in the console or by editing `ops.txt`.

| Command | Description |
| --- | --- |
| `/help` | List commands (includes plugin commands) |
| `/version` | Server version |
| `/list` | Online players |
| `/say <message>` | Broadcast a message |
| `/me <action>` | Action message |
| `/tell <player> <message>` | Private message |
| `/oneblock` | OneBlock progress: current phase, breaks, breaks left until the next phase, your own breaks and the top 5 breakers |
| `/kick <player> [reason]` | Kick a player |
| `/gamemode <survival\|creative\|0\|1\|s\|c> [player]` | Change game mode |
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
| `/weather <clear\|rain\|thunder\|storm> [ticks]` | Change the overworld weather (`/weather rain 600`); without ticks it lasts a random time. `storm` is rain with thunder, `thunder` is thunder without rain. `sunny` and `fine` also mean `clear` |
| `/difficulty <peaceful\|easy\|normal\|hard>` | Change the difficulty until the server restarts (`server.properties` is not changed) |
| `/summon <mob> [player]` | Spawn a mob next to a player |
| `/kill [player]` / `/heal [player]` | Kill / heal |
| `/op <player>` / `/deop <player>` | Manage operators |
| `/save-all` | Save everything now |
| `/save-on` / `/save-off` | Turn autosave on or off (`/save-all` still saves everything) |
| `/status` | Server status |
| `/debug [on\|off]` | Show every damage / hunger change in your chat |
| `/plugins` (or `/pl`) | List loaded plugins |
| `/plugins reload` | Reload plugins (OP only) |
| `/plugins enable <name>` / `/plugins disable <name>` | Enable or disable one plugin without restarting. `disable` also disables the plugins that depend on it. `enable` re-reads the jar (OP only) |
| `ban <player>` / `pardon <player>` | Ban or pardon a player name (banned players are kicked) |
| `ban-ip <ip>` / `pardon-ip <ip>` | Ban or pardon an IP address |
| `banlist` | List banned players and IP addresses |
| `whitelist <on\|off\|add\|remove\|list\|reload> [player]` | Turn the white list on or off, edit it, or reload it |
| `/stop` | Save and shut down |

## 🔌 Plugins

Plugins are `.jar` files in `plugins/`, or plain source code (a folder with `plugin.properties` and `.java` files, or a single `.java` file) that the server compiles itself, so you can test without building a jar (needs a JDK; see [Source plugins](Plugins.md#source-plugins-no-jar-needed)). They load on startup and on `/plugins reload`, and a plugin that crashes never takes the server down.

The plugin API is documented in **[Plugins.md](Plugins.md)**: events, commands, scheduler, menus, recipes, regions, per-player data, the HUD, and how to compile a plugin. The examples in `plugins-ejemplo/` show it in use.


## 🆘 Troubleshooting

| Problem | Solution |
| --- | --- |
| The client can't connect | The client must be **0.15.10**. Open the UDP port (`server-port`, `19132` by default) in your firewall, or set `network.upnp-forwarding` to `true` |
| Most commands don't work | They are OP-only. Add your name to `ops.txt`, or run `/op <name>` in the console |
| A plugin doesn't load | Run `/plugins` to see what is loaded, then check `logs/server.log` |
| `java: command not found` on Termux | `pkg install openjdk-17` |
| The client says the server or the client is outdated | The protocol must match exactly: use a Minecraft PE **0.15.10** client |
| The console colors look broken | Run with `NO_COLOR=1`, or set `console.color: false` in `blocknes.yml` |
| The server stops when I close Termux | Keep the session open (see the note about terminal tabs above), or run `termux-wake-lock` so Android does not suspend it |
| Where are the logs? | `logs/server.log`. The previous one is `logs/server.log.old` |

## 🙏 Credits

- [**RakNetLib**](https://github.com/servid124-ux/RakNetLib): the UDP transport, bundled inside `server.jar`.
- 
