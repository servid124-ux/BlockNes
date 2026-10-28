# BlockNes Plugin API

**API version 1.8** · BlockNes **1.2.3** · Minecraft: Pocket Edition **0.15.10** (protocol 84)

This guide explains how plugins work in BlockNes: how to build one, how the server loads it, and everything the API gives you (events, commands, permissions, tasks, config files, menus, recipes, regions, the HUD and more).

Plugins are written in **Java** and shipped as a `.jar` file in the server's `plugins/` folder. A plugin that crashes never takes the server down.

> If you know PocketMine-MP or Bukkit, you will feel at home: many classes and method names are the same on purpose (`Plugin`, `PluginBase`, `Listener`, `@EventHandler`, `Server.getInstance()`, `ServerScheduler`, `Level`, `Vector3`, `PlayerChatEvent`...). BlockNes also has a shorter "native" style (`on(Events.PlayerChat.class, e -> ...)`). Both work and you can mix them.

## Table of contents

1. [How it works](#how-it-works)
2. [Quick start: your first plugin](#quick-start-your-first-plugin)
3. [Cheat sheet](#cheat-sheet)
4. [Building a plugin](#building-a-plugin) (including [source plugins, no jar](#source-plugins-no-jar-needed))
5. [plugin.properties reference](#pluginproperties-reference)
6. [Lifecycle and load order](#lifecycle-and-load-order)
7. [Plugin API](#plugin-api)
   - [Information and logging](#information-and-logging)
   - [Listening to events](#listening-to-events)
   - [Commands](#commands)
   - [Permissions](#permissions)
   - [Tasks and threads](#tasks-and-threads)
   - [Config and data files](#config-and-data-files)
   - [Players and server](#players-and-server)
   - [World, mobs and items](#world-mobs-and-items)
   - [Menus](#menus)
   - [Recipes](#recipes)
   - [Regions](#regions)
   - [HUD: scoreboard, titles, boss bars](#hud-scoreboard-titles-boss-bars)
   - [Services: talking to other plugins](#services-talking-to-other-plugins)
   - [Text, arguments and cooldowns](#text-arguments-and-cooldowns)
   - [Translations](#translations)
8. [Events](#events)
9. [Entity classes](#entity-classes)
10. [API 1.6 additions (inside `Plugin`)](#api-16-additions-inside-plugin)
11. [API 1.7 additions (inside `Plugin`)](#api-17-additions-inside-plugin)
12. [API 1.8 additions (inside `Plugin`)](#api-18-additions-inside-plugin)
13. [Limits, errors and performance](#limits-errors-and-performance)
14. [Full example: an economy and a shop](#full-example-an-economy-and-a-shop)
15. [Things to know (limitations)](#things-to-know-limitations)
16. [Troubleshooting](#troubleshooting)

---

## How it works

- A plugin is a `.jar` in `plugins/`. Inside the jar there is a `plugin.properties` file at the root and a **main class** that extends `Plugin` (or `PluginBase`, which is the same thing).
- On startup (and on `/plugins reload`) the server reads every jar, sorts the plugins by their dependencies, creates one instance of each main class and calls `onLoad()`, then `onEnable()`.
- Everything a plugin registers (event handlers, commands, tasks, permissions, services, HUD lines, menus, recipes, metadata) belongs to that plugin. **When the plugin is disabled, the server removes all of it for you.**
- All handlers, commands and tasks run on the **server thread**, once per tick (20 ticks = 1 second). Do not block it: use [`runAsync`](#tasks-and-threads) for slow work such as HTTP or databases.
- Each plugin gets its own class loader. A plugin sees the server classes, its own classes, and the classes of the plugins it lists in `depend` / `softdepend`.
- Errors are isolated. An exception in an event handler, a command or a task is logged (with the plugin's name) and the server keeps going. The other handlers still run.

Folders involved:

| Path | What it is |
| --- | --- |
| `plugins/*.jar` | The plugins |
| `plugins/<Name>/` | The plugin's data folder (`config.properties`, JSON files, `players/`...). Created when the plugin asks for it |
| `plugins/permissions.properties` | Optional file that grants permissions to players and groups (see [Permissions](#permissions)) |
| `server-plugin-api.jar` | Compile-only jar that you build against. **Never put it in `plugins/`** |

---

## Quick start: your first plugin

Create this folder structure:

```
HolaMundo/
├── plugin.properties
└── src/ejemplo/HolaMundo.java
```

`plugin.properties`:

```properties
name=HolaMundo
main=ejemplo.HolaMundo
version=1.0
author=You
description=Says hello
api=1.8
```

`src/ejemplo/HolaMundo.java`:

```java
package ejemplo;

import net.BlockNes.plugin.Events;
import net.BlockNes.plugin.Plugin;

public class HolaMundo extends Plugin {
    @Override
    public void onEnable() {
        log("HolaMundo " + version() + " enabled");

        // Listen to an event (runs on the server thread)
        on(Events.PlayerJoin.class, e -> {
            broadcast(color("&e" + e.player.name + " joined the game"));
            message(e.player, color("&aWelcome, " + e.player.name + "!"));
        });

        // Register a command: /hello [name]
        command("hello").describe("Say hello").usage("[name]").run((sender, args) -> {
            String who = args.length > 0 ? args[0] : sender.name();
            sender.send("Hello, " + who + "!");
        });
    }

    @Override
    public void onDisable() {
        log("HolaMundo disabled");
    }
}
```

Build it and drop the jar in `plugins/` (details in [Building a plugin](#building-a-plugin)):

```sh
mkdir -p build
javac -source 11 -target 11 -encoding UTF-8 \
      -cp /path/to/server-plugin-api.jar -d build src/ejemplo/HolaMundo.java
cp plugin.properties build/
(cd build && jar cf ../HolaMundo.jar .)
cp HolaMundo.jar /path/to/server/plugins/
```

Start the server (or run `/plugins reload`). With the server in English you should see:

```
Enabled plugin HolaMundo v1.0
```

Typing `/hello` in game answers `Hello, <your name>!`.

---

## Cheat sheet

The calls you will use most. Each one is explained in its section.

| I want to... | Write |
| --- | --- |
| react to something that happens | `on(Events.PlayerJoin.class, e -> ...)` |
| add a command | `command("kit").describe("Get a kit").permission("tour.kit").run((sender, args) -> ...)` |
| know who ran a command | `Player p = playerOf(sender);` (`null` for the console) |
| send a message | `message(player, color("&aHello"))`, or `broadcast(color("&eHello all"))` |
| run something later, or every N ticks | `runLater(40, () -> ...)`, `runRepeating(0, 20, () -> ...)` (20 ticks = 1 second) |
| keep a setting | `config().setDefault("kit-seconds", 60).save();` |
| keep data for a player | `playerData().of(player).set("stats.kills", 3);` |
| show a scoreboard line | `hudLine(player, "kills", 1, color("&cKills: &f3"));` |
| get the class of a mob | `Entity e = entityOf(mob);` |

---

## Building a plugin

### What you need

- A JDK. The server itself needs **Java 17 or newer**. Your plugin can be compiled with `-source 11 -target 11` (that is what the server build uses) or any newer level that the server's Java can run.
- `server-plugin-api.jar`. It is produced by the server build (`server/target/server-plugin-api.jar`) and published with each release. It contains the `net.BlockNes.*` classes (without the entry class `net.BlockNes.BlockNes`; `AccessLists` is now `net.BlockNes.BlockNes.AccessLists`) and the `raknetlib.*` classes (for example `BinaryStream`). RakNetLib 2.x keeps `BinaryStream` and `RakNet` in `raknetlib`, and puts `RakNetServer` and `Session` in `raknetlib.server`, `RakNetClient` in `raknetlib.client`, `GameBatch` and `Zlib` in `raknetlib.batch`, and `EncapsulatedPacket` in `raknetlib.packet`.

### Compile and package

1. Compile your sources against `server-plugin-api.jar`. It is **compile-only**: do not bundle it inside your jar.
2. Put `plugin.properties` (and optional `config.properties` and `lang/*.properties`) at the **root** of the jar, next to your compiled classes.
3. Copy the jar to `plugins/`.

```sh
javac -source 11 -target 11 -encoding UTF-8 -cp server-plugin-api.jar -d build $(find src -name '*.java')
cp plugin.properties build/            # also config.properties and lang/ if you have them
(cd build && jar cf ../MyPlugin.jar .)
```

To avoid retyping this, save it as `pack.sh` next to your plugin folder and run `sh pack.sh MyPlugin /path/to/server-plugin-api.jar`. It expects `src/`, `plugin.properties` and, optionally, `config.properties` and `lang/` inside the plugin folder:

```sh
#!/bin/sh
# usage: sh pack.sh <plugin folder> <server-plugin-api.jar>
set -e
dir="$1"; api="$2"; name="$(basename "$dir")"
rm -rf "$dir/build" && mkdir -p "$dir/build"
javac -source 11 -target 11 -encoding UTF-8 -cp "$api" -d "$dir/build" $(find "$dir/src" -name '*.java')
cp "$dir/plugin.properties" "$dir/build/"
[ -f "$dir/config.properties" ] && cp "$dir/config.properties" "$dir/build/"
[ -d "$dir/lang" ] && cp -r "$dir/lang" "$dir/build/"
(cd "$dir/build" && jar cf "../../$name.jar" .)
echo "OK -> $name.jar (copy it to plugins/)"
```

### Important notes

- The **main class** must extend `Plugin` (or `PluginBase`) and have a public **no-argument constructor** (the default one is fine). Do your setup in `onEnable()`, not in the constructor.
- The plugin `name` may only contain letters, digits, `_` and `-`. Names are not case sensitive when you look a plugin up (`plugin("eco")` finds `Eco`).
- Two plugins (jar, folder or `.java`) with the same `name`: only the first one is loaded (the second one is skipped with a warning).
- Packages: the old `net.BlockNes.raknet` package no longer exists. If your plugin imported it, change the imports to **`raknetlib`**.
- Do not copy server classes into your jar. The server already provides them.
- If your plugin only uses the classes of another plugin (an API jar, for example), compile against that plugin's jar too, and list it in `depend` or `softdepend`.

### Source plugins: no jar needed

For testing you do not have to build a jar every time. The server can also load a plugin **straight from its `.java` files** and compile it itself when it starts. Jars keep working exactly as before, and you can mix both in the same `plugins/` folder.

**Requirement:** the server must run on a **JDK** (it needs `javac`), not a plain JRE. If it runs on a JRE, the plugin is skipped with a warning that says so.

There are two layouts:

**1. A folder** (recommended; several files, config and translations):

```
plugins/
  MyPlugin/
    plugin.properties        <- same file as in a jar
    config.properties        <- optional, same as in a jar
    lang/en.properties       <- optional
    mypkg/MyPlugin.java      <- any number of .java files, in any subfolder
```

The folder needs a `plugin.properties`. `name` may be left out (the folder name is used). The folder is also the plugin's **data folder**, so `config.properties` and `lang/` are read from it, and files the plugin saves go there too.

**2. A single `.java` file:**

```
plugins/
  Solo.java
  Solo.properties            <- optional: version, depend, api, commands...
```

The plugin name is the file name (`Solo`). The main class is taken from the file: its `package` line plus the file name, so the class must be called like the file (`public class Solo extends Plugin`).

How it behaves:

- The code is compiled when the server starts, on `/plugins reload`, and on `/plugins enable <name>`. So the loop is: edit the `.java`, run `/plugins reload`, test.
- Compiled classes are kept in `plugins/.cache/<name>/`. If no `.java` file changed (size and modification time), nothing is recompiled. You can delete `.cache` at any time.
- The compile classpath is the server plus the plugins you list in `depend` / `softdepend` (jar or source). Load order works as for jars.
- If the code does not compile, the plugin is **not loaded** and the console lists the errors (file, line, message; up to 20). The rest of the server keeps going. Fix the file and `/plugins reload`.
- Folders whose name starts with a dot, and folders without `plugin.properties`, are ignored.
- When the plugin is ready, you can still package it as a jar (see above) to distribute it.

---

## plugin.properties reference

Every plugin jar needs a `plugin.properties` at its root. Only `name` and `main` are required.

```properties
# required: letters, digits, _ and -
name=MyPlugin
# required: the class that extends Plugin
main=com.me.MyPlugin
# default 1.0
version=1.2
# one author, or several (they are merged)
author=Me
authors=Ana,Luis
website=https://example.com
# text shown in [brackets] in log lines (default: the name)
prefix=MP
description=What it does
# minimum API version the plugin needs
api=1.8

# must be loaded before this plugin, or this plugin does not load
depend=Eco,Other
# loaded first when present, but not required
softdepend=Optional
# this plugin loads before "Another"
loadbefore=Another

# permissions registered for you (default: op | notop | true | false)
permissions.myplugin.use.description=Use the plugin
permissions.myplugin.use.default=true
permissions.myplugin.admin.default=op
# children: "-x" means the child is denied
permissions.myplugin.admin.children=myplugin.use,-myplugin.secret

# commands: default description / usage / aliases / permission for commands you register in code
commands.home.description=Go home
commands.home.usage=[name]
commands.home.aliases=h,casa
commands.home.permission=myplugin.use
commands.home.permission-message=You cannot use /home
```

Notes:

- `api` is the **minimum** version. A plugin that asks for an API newer than the server's (`1.8`) is **not loaded**; the console says so. `api=1.5` loads fine on a 1.8 server. A plugin without `api` loads without the check.
- Declaring a command in `plugin.properties` does not register it. You still register it in code (see [Commands](#commands)); the properties only supply defaults for what you leave empty. If you declare a command and never register it, the server warns in the log when the plugin enables.
- Permissions listed here are registered for you when the plugin loads.
- The file is a Java `.properties` file read as UTF-8. A `#` only starts a comment at the **start of a line**; if you write `name=Foo # note`, the note becomes part of the value.

---

## Lifecycle and load order

```
read plugin.properties of every jar ──► sort by depend / softdepend / loadbefore
        │
        ▼
create instance ──► onLoad()          (for every plugin, before any onEnable)
        │
        ▼
onEnable()  ──► PluginEnable event    (plugin is now enabled)
        │
        ▼   ... server runs ...
        │
PluginDisable event ──► onDisable() ──► everything the plugin registered is removed
```

- **Order.** `depend` is always respected: a plugin whose hard dependency is missing is not loaded (the console lists what is missing). `softdepend` and `loadbefore` are respected when possible; if they form a cycle they are ignored. Plugins are otherwise taken in file-name order.
- **`onLoad()`** runs for all plugins before any plugin's `onEnable()`. Use it for light setup only.
- **`onEnable()`**: register events, commands, tasks, permissions here. If it throws an exception, that plugin is unloaded and the server continues with the others.
- **`onDisable()`**: save your data. You do **not** have to unregister anything: after `onDisable()` the server removes the plugin's event handlers, commands (and their aliases), tasks (including pending async tasks), HUD lines and boss bars, services, permission attachments, permissions, open menus (their `onClose` is called), recipes and metadata. Player data opened with `playerData()` is saved.
- **Disabling** a plugin also disables the plugins that `depend` on it. The same happens when you run `/plugins disable <name>`.
- Plugins are disabled in reverse load order on shutdown.

Server commands for plugins:

| Command | What it does |
| --- | --- |
| `/plugins` (or `/pl`) | Lists the loaded plugins with their versions. Everyone can use it |
| `/plugins reload` | Disables every plugin, reads the folder again and enables them. OP only |
| `/plugins enable <name>` | Enables one plugin without restarting. It reads the jar again, so you can replace the jar first. OP only |
| `/plugins disable <name>` | Disables one plugin (and its dependents). OP only |

Reloading creates a **new class loader**, so static fields of the old plugin are gone. Keep important state in files.

---

## Plugin API

Everything below is a method of `Plugin` (`net.BlockNes.plugin.Plugin`) unless it says otherwise. `PluginBase` is an empty subclass of `Plugin`; extend whichever you like.

Common imports:

```java
import net.BlockNes.Player;                    // a connected player
import net.BlockNes.Server;                    // the server
import net.BlockNes.item.Item;                 // items
import net.BlockNes.mob.Mob;                   // mobs
import net.BlockNes.plugin.*;                  // Plugin, Events, Priority, Config, Menu, Region, Args...
import net.BlockNes.command.CommandSender;     // who ran a command (player or console)
```

> `net.BlockNes.plugin.*` also contains `Task` and `AsyncTask`. The PocketMine-style scheduler has classes with the same names in `net.BlockNes.scheduler`. Import the ones you mean explicitly (see [Tasks and threads](#tasks-and-threads)).

### Information and logging

| Method | Returns |
| --- | --- |
| `name()` / `getName()` | The plugin name |
| `version()` / `getVersion()` | The version from `plugin.properties` |
| `author()`, `description()` | Authors (joined with commas) and description |
| `getDescription()` | The whole `PluginDescription` (authors, website, depend, declared commands and permissions...) |
| `getFile()` | The path of the jar |
| `dataFolder()` / `getDataFolder()` | `plugins/<Name>/` (created if missing) |
| `server()` / `getServer()` | The `Server` |
| `pluginManager()` | The `PluginManager` |
| `plugins()` | Every loaded plugin |
| `plugin("Name")` | One plugin by name, or `null` |
| `isEnabled()` / `isDisabled()` | Whether `onEnable()` finished |
| `setEnabled(true/false)` | Enable or disable this plugin. After disabling, this object is no longer valid; enabling again reads the jar again |

Logging:

```java
log("message");                      // [Prefix] message, at INFO level
logWarn("careful");                  // WARNING
getLogger().info("...");             // also notice(), warning(), error(), critical(), alert(), emergency()
getLogger().debug("...");            // only printed when debugging is on (blocknes.yml console.debug or -Dblocknes.debug=true)
getLogger().logException(exception); // error with the stack trace
```

Every log line gets the plugin's `prefix` in brackets.

### Listening to events

There are several ways. All of them run on the server thread, and all of them are removed when the plugin is disabled.

```java
// 1) Lambda (the shortest). Default priority NORMAL.
on(Events.PlayerJoin.class, e -> message(e.player, "Welcome!"));

// 2) With a priority.
on(Events.PlayerChat.class, Priority.HIGH, e -> e.message = e.message.toUpperCase());

// 3) With a priority, skipping events that another plugin already cancelled.
on(Events.BlockBreak.class, Priority.HIGH, true, e -> { /* only if not cancelled */ });

// 4) Only once: the handler removes itself after the first call.
once(Events.PlayerQuit.class, e -> log("first quit: " + e.player.name));

// 5) @EventHandler methods in any object (PocketMine / Bukkit style).
public class Guard {
    @EventHandler(priority = Priority.HIGH, ignoreCancelled = true)
    public void onBreak(Events.BlockBreak e) {
        if (e.id == 7) {                          // bedrock
            e.setCancelled(true);
            message(e.player, color("&cYou cannot break bedrock"));
        }
    }
}
// in onEnable():
registerEvents(new Guard());                      // returns how many handlers it registered
```

Removing handlers early:

```java
unregisterEvents(guard);       // the handlers of one registerEvents(guard); returns how many
unregisterAllEvents();         // every handler of this plugin
```

**What you can listen to.** The type you pass to `on(...)` can be:

- the event class: `Events.PlayerChat.class`;
- the PocketMine-named interface that the event implements: `net.BlockNes.event.player.PlayerChatEvent.class`;
- a base class or interface to receive a whole family: `Events.PlayerEvent.class` (every player event), `net.BlockNes.event.player.PlayerEvent.class`, `Event.class` (every event), `net.BlockNes.event.entity.EntityDamageEvent.class` (player and mob damage)...

A method handler (`@EventHandler`) must have **exactly one parameter**, the event type. Otherwise it is skipped with a warning.

**Priorities.** `LOWEST` runs first, then `LOW`, `NORMAL`, `HIGH`, `HIGHEST`, and `MONITOR` runs **last**. Use `MONITOR` only to observe the final result (do not change the event there). Handlers with the same priority run in the order they were registered. A later handler sees the changes (and the cancellation) made by earlier ones.

**Cancelling.** Events that say "cancelable" in the [event list](#events) respect `e.setCancelled(true)`. On informational events, `setCancelled` does nothing. `ignoreCancelled = true` makes your handler skip events that are already cancelled.

**Editing.** Many events have public fields you can change before the server uses them (`e.message`, `e.amount`, `e.drops`, `e.toX`...). The event list says which.

**Your own events.** Extend `Event`, create it and pass it to `fire`. Other plugins listen to it like any other event:

```java
public static class ArenaJoinEvent extends Event {
    public final Player player;
    public ArenaJoinEvent(Player p) { player = p; }
}

// listening:
on(ArenaJoinEvent.class, e -> broadcast(color("&6" + e.player.name + " entered the arena")));

// firing (returns the event, so you can check isCancelled() afterwards):
ArenaJoinEvent ev = fire(new ArenaJoinEvent(player));
if (ev.isCancelled()) return;
```

**Every event has two ways to read its data**: public fields (`e.player`, `e.x`, `e.id`) and the PocketMine-style getters of its interface (`e.getPlayer()`, `e.getX()`, `e.getBlockId()`). Use whichever you prefer.

### Commands

The shortest way is the builder, `command(name)` (API 1.8). The four older forms, A to D below, still work. All of them are removed automatically when the plugin is disabled.

Plugin commands are checked **before** the built-in server commands, so a plugin command overrides a built-in command with the same name (`/help`, `/tp`...). If two plugins register the same name, the last one wins and the log prints a warning.

Command names are lower-case; a leading `/` is ignored. Players can type them in chat; the console can type them without the `/`.

```java
// Builder (API 1.8): only the name and run() are needed
command("kit")
    .describe("Get a starter kit")                // shown in /help
    .usage("[name]")                              // shown in /help, after the command name
    .aliases("starter", "pack")                   // more names for the same command
    .permission("tour.kit")                       // a permission node; use .opOnly() for OPs only; leave it out for everyone
    .run((sender, args) -> sender.send("Kit for " + (args.length > 0 ? args[0] : sender.name())));

command("reset").describe("Reset the arena").opOnly().executor((sender, cmd, label, args) -> {
    if (args.length == 0) return false;          // false = print the usage
    sender.send("Arena " + args[0] + " reset");
    return true;
});
```

`run(...)` takes the same `(sender, args)` lambda as form A below, and `executor(...)` takes the `CommandExecutor` of form C. The builder maps to the older arguments like this: `.opOnly()` is `true`, `.permission("x")` is `"x"`, and leaving it out is `""` (everyone). Anything you do not set (description, usage, permission) is taken from `plugin.properties`, as in the other forms.

The older forms:

```java
// A) Lambda: (sender, args). 4th argument = true -> OPs and console only; false -> everybody.
command("hello", "Say hello", "[name]", false, (sender, args) -> {
    sender.send("Hello, " + (args.length > 0 ? args[0] : sender.name()) + "!");
});

// B) Lambda protected by a permission node (OPs always pass).
command("kit", "Get a starter kit", "", "tour.kit", (sender, args) -> { ... });

// C) PocketMine-style CommandExecutor: (sender, command, label, args) -> boolean.
//    Return false to print "Usage: /give2 <player> [count]".
command("give2", "Give items", "<player> [count]", "tour.admin", (sender, cmd, label, args) -> {
    if (args.length < 1) return false;
    Player target = player(args[0]);
    if (target == null) { sender.send("Player not found"); return true; }
    int n = Args.getInt(args, 1, 1, 1, 64);
    give(target, Item.get(264, 0, n));
    sender.send("Gave " + n + " diamonds to " + target.name);
    return true;
});

// D) A command as a class.
registerCommand(new Command("arena", "Join the arena", "", "a") {   // name, description, usage, aliases...
    @Override public boolean execute(CommandSender s, String label, String[] args) {
        Player p = playerExact(s.name());
        if (p == null) return false;          // not a player: prints the usage
        fire(new ArenaJoinEvent(p));
        return true;
    }
});

alias("kit", "starter", "pack");              // more names for a command of this plugin
```

The 4th argument of the lambda forms (`command(name, description, usage, X, ...)`) decides who can use it:

| `X` | Meaning |
| --- | --- |
| `false` or `""` | Everyone |
| `true` or `"op"` | OPs and the console |
| `"some.node"` | Players that have the [permission](#permissions) (OPs and the console always do) |

Useful details:

- **The sender.** Forms A and B give a `Commands.Sender`; C and D give a `CommandSender`. Both have `name()`, `isOp()`, `send(String)` and `isPlayer()`. The console is named `CONSOLE`. To get the `Player` of a sender, use `playerOf(sender)` (API 1.8). It is `null` for the console: `Player p = playerOf(sender);`.
- **Usage.** In forms C and D returning `false` prints `Usage: /name <usage>`.
- **Defaults from `plugin.properties`.** Anything you leave empty (description, usage, permission) is taken from `commands.<name>.*`; aliases and `permission-message` come from there too.
- **No permission.** The sender sees "You do not have permission to use this command" (or the `permission-message` you set).
- **Errors.** If the command code throws, the sender is told something went wrong and the error goes to the log.
- **Help.** Plugin commands are listed in `/help`.
- **Other plugins' commands.** `getCommand("name")` returns one of *your* commands; `unregisterCommand("name")` removes it. `getServer().getCommandMap()` can see every plugin command.
- **Run a command from code.** `dispatchCommand(sender, "give Steve 1 1")`, with `console()` as a ready-made console sender: `dispatchCommand(console(), "say hi")`.

The `Args` helper reads arguments safely. Each method returns the default value when the argument is missing or invalid:

```java
Args.has(args, 1)                       // is there an argument 1?
Args.get(args, 0, "default")            // String
Args.getInt(args, 1, 1)                 // int, default 1
Args.getInt(args, 1, 1, 1, 64)          // int in 1..64, otherwise the default
Args.getDouble(args, 2, 0.0)            // finite decimal number
Args.getBoolean(args, 1, false)         // true/on/yes/si/1 and false/off/no/0
Args.join(args, 1)                      // arguments 1.. joined with spaces (a chat message)
```

### Permissions

A permission is a text node such as `tour.kit`. Console and **OPs have every permission**. For everyone else the server decides in this order, and the **first rule that says something wins**:

1. **Attachments** that plugins added to the player (temporary; see below). If several attachments mention the node, the last one wins.
2. **`plugins/permissions.properties`**, the file the server owner edits.
3. **Children** of a permission the player has: if the player has `tour.admin` and `tour.admin` lists `tour.kit` as a child, they get `tour.kit` (or are denied it, if the child is `-tour.kit`).
4. The **default** of the registered permission: `TRUE` and `NOT_OP` grant it to non-OPs; `OP` and `FALSE` do not.
5. A node nobody registered, with no rule: **denied**.

Wildcards work in attachments and in the file: `tour.*` matches everything under `tour.`, and `*` matches everything. A `-` in front of a name in the file denies it.

**Registering permissions** (so they have a default and show up in the tools). Either in `plugin.properties` (see above) or in code:

```java
registerPermission("tour.kit",   "Use /kit",   PermissionDefault.TRUE);   // everyone
registerPermission("tour.admin", "Administer", PermissionDefault.OP);     // only OPs (the default)
registerPermission("tour.vip",   "VIP perks");                            // same as OP
Permission p = getPermission("tour.kit");                                 // any registered permission
```

`PermissionDefault` values: `OP`, `NOT_OP`, `TRUE`, `FALSE`.

**Checking a permission:**

```java
if (hasPermission(player, "tour.kit")) { ... }
if (player.hasPermission("tour.kit")) { ... }          // same, from the Player
if (hasPermission(sender, "tour.kit")) { ... }         // a CommandSender
```

**`plugins/permissions.properties`.** Optional file owned by the server admin. Keys are player names or `group.<name>`:

```properties
# player = groups and/or permissions
Steve=vip,tour.extra
# group = permissions; "-x" denies; "@other" includes another group
group.vip=tour.admin,-tour.secret,@default
# the group "default" applies to every player
group.default=tour.kit
```

Names are not case sensitive. The file is read when plugins load (and on `/plugins reload`).

**Attachments** are temporary permissions a plugin gives to a player. They disappear when the plugin is disabled or the player leaves, so give them again in your `PlayerJoin` handler:

```java
PermissionAttachment a = addAttachment(player);
a.setPermission("arena.enter", true);     // grant
a.setPermission("arena.shop", false);     // deny
a.unsetPermission("arena.shop");          // forget that entry
a.remove();                               // remove the whole attachment

addAttachment(player, "tour.vip", true);  // shortcut: one permission
removeAttachment(a);
```

### Tasks and threads

Time is counted in **ticks**: 20 ticks = 1 second. A delay of `0` or `1` means "next tick".

```java
Task t = runLater(40, () -> broadcast("2 seconds later"));          // once, after 40 ticks
Task r = runRepeating(0, 20 * 60, () -> broadcast("every minute")); // delay, period

// the body gets its Task, so it can cancel itself
runRepeating(0, 20, task -> {
    if (done()) task.cancel();
});

runTimes(0, 20, 5, i -> broadcast("run #" + i));                    // exactly 5 times (i = 0..4)

countdown(5, n -> broadcast("Starting in " + n), () -> broadcast("Go!"));  // 5,4,3,2,1 then "Go!"
```

Managing tasks:

```java
t.cancel();                    // one task
cancelTasks();                 // every task of this plugin
cancelTask(id);                // by id (Task.getTaskId()); true if it was still pending
isTaskQueued(id);              // is it still pending?
getTask(id);                   // the scheduled Task or null
```

**Threads.** Everything above runs on the **server thread**. For work that can be slow (HTTP, files, databases, heavy calculations) use async methods, and only **touch players and the world from the server thread**:

```java
// Work in another thread, result delivered on the server thread:
runAsync(() -> downloadSomething(),                       // other thread
         result -> message(player, "Got: " + result));    // server thread

// Fire-and-forget in another thread. Do NOT call players/world/Plugin API in here.
runAsync(() -> writeBigFile());

// AsyncTask: onRun() in another thread, onCompletion() on the server thread.
int id = runAsync(new AsyncTask() {
    @Override public void onRun() { setResult(heavy()); }
    @Override public void onCompletion() { broadcast("Result: " + getResult()); }
});
// cancelRun() before it starts means nothing runs. If onRun() throws, isCrashed() is true.

// From any thread, hand something back to the server thread (runs next tick):
runSync(() -> player.health = 20);
```

If the plugin is disabled while an async job is running, its `done` callback / `onCompletion()` is **not** called.

There are two `AsyncTask` classes: `net.BlockNes.plugin.AsyncTask` (`onCompletion()` has no parameters; use it with `runAsync(...)`) and `net.BlockNes.scheduler.AsyncTask` (`onCompletion(Server)`; use it with `getScheduler().scheduleAsyncTask(...)`). The same goes for `Task`. See [API 1.6 additions](#api-16-additions-inside-plugin).

`Cooldowns`, `PlayerStore` and most plugin objects are meant for the server thread only.

### Config and data files

Each plugin owns the folder `plugins/<Name>/`. The API offers four ways to use it.

**1. `config.properties`: simple settings.**

```java
Config cfg = config();                                   // plugins/<Name>/config.properties
cfg.setDefault("welcome", "&aWelcome, %s!")              // only written if the key does not exist
   .setDefault("kit-seconds", 60)
   .save();

String msg = cfg.getString("welcome", "Hi %s");
int secs   = cfg.getInt("kit-seconds", 60);              // also getLong, getDouble, getBoolean
List<String> worlds = cfg.getStringList("worlds");       // "a, b, c"
cfg.set("kit-seconds", 30).save();
cfg.has("key");  cfg.remove("key");  cfg.keys("kits.");  cfg.getKeys();  cfg.getAll();
reloadConfig();                                          // read it again from disk
```

`Config` is a `java.util.Properties`, so all its methods work too. All getters return the default you give when the key is missing or the value is not valid. **`save()` rewrites the file and drops its comments.**

If the jar contains a `config.properties` at its root, it is copied to the data folder the first time the config is used. If your jar has none, the server prints a warning (and the config simply starts empty), so ship one, even a nearly empty file.

`saveDefaultConfig()` copies the jar's `config.properties` if it is not on disk yet. `saveConfig()` is `config().save()`.

**2. JSON files: structured data.** Paths use dots (`"kits.vip.price"`), so keys themselves must not contain dots.

```java
JsonConfig data = jsonConfig("arenas.json");             // plugins/<Name>/arenas.json (starts empty)
data.setDefault("arenas.1.spawn", 5);
int spawn = data.getInt("arenas.1.spawn", 0);            // getString, getLong, getDouble, getBoolean, getList
data.set("arenas.2.spawn", 7).save();                    // creates the missing parents, then writes the file
data.has("arenas.2");  data.remove("arenas.2");  data.getKeys();  data.getAll();  data.reload();
```

`jsonConfig(...)` always opens the file again; keep the object in a field if you use it often. Nothing is written until you call `save()`.

**3. Per-player data.** One JSON file per player in `plugins/<Name>/players/`. It uses the same API as `JsonConfig`:

```java
JsonConfig d = playerData().of(player);                  // or playerData().of("Steve")
d.set("stats.kills", d.getInt("stats.kills", 0) + 1);
if (playerData().exists("Steve")) { ... }
playerData().saveAll();                                  // optional
```

Player names are not case sensitive (`Steve` and `steve` are the same file). The data of a player is **saved automatically when that player disconnects and when the plugin is disabled**, so you do not need to call `save()` after every change. The data stays in memory while the player is online. If the server crashes, changes that were not saved yet are lost: call `d.save()` right after an important change (a purchase, for example).

**4. Files inside your jar.**

```java
InputStream in = getResource("lang/eng.properties");     // a file from your own jar, or null (close it when done)
Path out = saveResource("kits.json");                    // copy it to the data folder if it is not there yet
saveResource("kits.json", true);                         // true = overwrite
```

### Players and server

Most methods take a `Player`. Useful fields of `Player`: `name`, `x`, `y`, `z`, `yaw`, `pitch`, `dimension` (`0` overworld, `1` nether), `health`, `food`, `gamemode` (`0` survival, `1` creative), `eid`, `loggedIn`, `spawned`, `totalXp`. Useful methods: `getPing()`, `getIp()`, `isCreative()`, `inHand()`, `getXpLevel()`, `getPosition()`, `getLevel()`, `getPlayerInventory()`, `hasPermission(node)`, `sendTitle(title, subtitle)`.

Finding players:

```java
players();                          // every connected player (a copy of the list)
player("ste");                      // by name; a name that starts with the text also matches
playerExact("Steve");               // exact name (not case sensitive), or null
isOnline("Steve");  onlineCount();  playerNames();  maxPlayers();
playersIn(0);                       // players in the overworld (1 = nether)
playersNear(0, x, y, z, 20);        // within 20 blocks, nearest first
nearestPlayer(0, x, y, z, 50);      // the closest one within 50 blocks, or null
mobsNear(0, x, y, z, 20);           // living mobs within 20 blocks
```

Messages:

```java
message(p, "text");                 // chat message to one player
messagef(p, "&aYou have %d coins", n);   // String.format + & colors
broadcast("text");                  // chat to everyone
broadcastf("&e%s won!", name);
broadcast("text", "perm.node");     // only players with that permission (OPs always)
popup(p, "text");                   // text above the hotbar
tip(p, "text");                     // small text in the middle of the screen
broadcastPopup("text");  broadcastTip("text");
```

For titles, action bars and the scoreboard see [HUD](#hud-scoreboard-titles-boss-bars). Colors: write `&a`, `&l`... and convert with `color("&aText")` (see [Text](#text-arguments-and-cooldowns)). The server does **not** convert `&` automatically in `message`/`broadcast`, except in the `...f` variants.

Doing things to players:

```java
teleport(p, x, y, z);               // same dimension, no event
teleportChecked(p, x, y, z);        // passes through Events.PlayerTeleport; false if cancelled
heal(p, 4);                         // passes through PlayerHeal (can be cancelled by other plugins)
damage(p, 3, "died to a trap");        // normal damage (passes through PlayerDamage); see the note below
kill(p);
ignite(p, 5);  extinguish(p);  isOnFire(p);
kick(p, "reason");  kickAll("Maintenance", "admin.bypass");   // everyone except players with that permission
setGamemode(p, 1);                  // 0 survival, 1 creative
giveXp(p, 10);
Item left = give(p, Item.get(264, 0, 3));     // returns what did not fit (Item.AIR if everything fit)
isOp(p);  setOp("Steve", true);
```

About `damage(p, amount, cause)`: `cause` is the text of the death message. If the player dies the server broadcasts `"<name> <cause>"` (for example `Steve died to a trap`). The server's own causes are written in Spanish (`se ahogo`, `cayo al vacio`, `murio de hambre`...), and it looks for some of those Spanish words (`cayo`, `vacio`, `ahogo`, `hambre`, `veneno`, `exploto`...) to decide how armor and potion effects reduce the damage. Any other text is treated as generic damage.

Server settings and lists:

```java
banPlayer("Steve");  pardonPlayer("Steve");  isBanned("Steve");
banIp("1.2.3.4");    pardonIp("1.2.3.4");    isBannedIp("1.2.3.4");
whitelistAdd("Steve");  whitelistRemove("Steve");  isWhitelisted("Steve");
setWhitelist(true);  isWhitelistOn();
isPvpEnabled();  isSpawnMobsEnabled();
time();  setTime(6000);  setTimeStopped(true);
setWeather(Weather.RAINY, 6000);    // net.BlockNes.level.Weather: SUNNY, RAINY, RAINY_THUNDER, THUNDER. ticks <= 0 = random length
strikeLightning(x, y, z);
saveAll();                          // saves worlds, players and items; returns the number of chunks saved
```

### World, mobs and items

`dimension` is always `0` (overworld) or `1` (nether). Call these from the server thread.

```java
int id = getBlock(0, x, y, z);                     // block id
setBlock(0, x, y, z, 20, 0);                       // dimension, x, y, z, id, meta; players see it
int top = highestBlockAt(0, x, z);                 // highest non-air block in the column, or -1
World w = world(0);                                // the raw world

Mob zombie = spawnMob(0, Mob.ZOMBIE, x, y, z);     // Mob.ZOMBIE, Mob.COW, ... or null if the id is unknown
List<Mob> mobs = mobsIn(0);                        // living mobs (a copy)

dropItem(0, x, y, z, Item.get(264, 0, 3));         // item on the ground
explode(0, x, y, z, 3.0);                          // like TNT: fires Events.Explosion and breaks blocks
playLevelEvent(0, x, y, z, 2001, 1);               // particles/sounds for nearby players (2001 = block break)
```

Block, item and effect ids are the numeric ids of Minecraft PE 0.15: stone `1`, diamond `264`, bread `297`... Effects use constants from `net.BlockNes.item.Food` (`EFFECT_POISON`, `EFFECT_REGENERATION`...).

**Items** are immutable objects. Create them with `Item.get(id)`, `Item.get(id, damage)` or `Item.get(id, damage, count)` and change them with the `with...` methods, which return a new item:

```java
Item sword = Item.get(272)                                // wooden sword
        .withName(color("&6Starter Sword"))               // custom name (colors allowed)
        .withLore(Arrays.asList("line 1", "line 2"))
        .withEnchantment(9, 2)                            // enchantment id, level
        .withUnbreakable(true)
        .withCount(1);
sword.getId();  sword.getDamage();  sword.getCount();  sword.getCustomName();  sword.isAir();
```

`Item.AIR` means "nothing". Names and lore are limited in length (the server throws `IllegalArgumentException` if they are too long).

Giving items: `give(player, item)` returns what did not fit. The inventory also works: `player.getPlayerInventory().addItem(item)`, `removeItem`, `setItem`, `getItemInHand()`, `contains`, `clear()`.

### Menus

A `Menu` is a chest GUI (3 or 6 rows) whose slots are **buttons**: players cannot take or move the items, and while the menu is open their inventory is frozen, so nothing can be lost or duplicated.

```java
Menu m = menu(3);                                        // 3 rows = 27 slots, or 6 rows = 54
m.border(Item.get(160, 7, 1).withName(" "));             // decorate the edge
m.setItem(13, Item.get(297, 0, 1).withName("Bread"), click -> {
    give(click.player, Item.get(297, 0, 1));             // click is an Events.MenuClick
    closeMenu(click.player);
});
m.onClose(p -> message(p, "Menu closed"));               // closed by the player, closeMenu, disconnect or plugin disable
openMenu(player, m);
```

- `setItem(slot, item)` is a decoration; `setItem(slot, item, handler)` is a button. Calling `setItem` again updates the screen of everyone looking at it. `fill(item)`, `border(item)` and `clear()` fill, frame and empty it. `refresh()` resends everything after many changes.
- `click.player`, `click.slot`, `click.item` (what is in the slot), `click.proposed` (what the client tried to put there) and `click.menu` are available. You can also listen to `Events.MenuClick` globally (it is cancellable: cancelling it stops the slot's handler).
- The menu is always reverted on the client after a click, whatever your handler does.
- Useful: `m.viewers()`, `isMenuOpen(player)`, `closeMenu(player)`.
- The Minecraft PE 0.15 client does **not** show a title for these windows.

### Recipes

Recipes belong to your plugin and are removed when it is disabled.

```java
// Shaped (like the crafting table): rows of up to 3 characters (a space = empty slot)
Map<Character, Item> ing = new HashMap<>();
ing.put('D', Item.get(264));     // diamond
ing.put('S', Item.get(280));     // stick
addShapedRecipe(Item.get(278), new String[]{"DDD", " S ", " S "}, ing);      // diamond pickaxe

// Shapeless: ingredients in any slot (at most 9 units)
addShapelessRecipe(Item.get(297, 0, 2), Arrays.asList(Item.get(296), Item.get(296)));

Recipe r = addShapedRecipe(...);          // both return the recipe (net.BlockNes.item.Recipes.Recipe)
removeRecipe(r);                          // remove one (yours or another plugin's)
```

You can also react to crafting with `Events.CraftItem` (cancellable) and `Events.SmeltItem`.

### Regions

A `Region` is a box of blocks between two corners (both inclusive; they are sorted for you).

```java
Region r = region(0, 0, 60, 0, 9, 70, 9);       // dimension, x1, y1, z1, x2, y2, z2
r.volume();  r.width();  r.height();  r.depth();  r.contains(x, y, z);
r.count(1);                                     // how many blocks of id 1
r.fill(1, 0);                                   // fill with stone; returns how many blocks were written
r.replace(1, 4, 0);                             // replace id 1 with id 4 meta 0; returns how many
Region.Snapshot snap = r.copy();                // copy ids + metas to memory (max 1,000,000 blocks)
r.paste(snap, 20, 60, 20, true);                // paste with its minimum corner at (20,60,20); true = skip air
```

Filling, replacing and counting have no size limit, but a huge region on the server thread takes time: split it over several ticks with `runRepeating` if it is big. Only `copy()` is limited (`Region.MAX_BLOCKS`) and throws `IllegalArgumentException` above it.

### HUD: scoreboard, titles, boss bars

Minecraft PE 0.15.10 has no packets for titles, boss bars or scoreboards. BlockNes **emulates** them with the two text channels the client does have: *tip* (the middle of the screen) and *popup* (above the hotbar). Everything you add is removed when your plugin is disabled.

**Scoreboard** (a list of lines):

```java
hudLine(player, "kills", 1, color("&cKills: &f" + kills));   // key, order, text. Same key = update. text null = remove
hudLine(player, "tip", color("&7hello"));                    // order 0
hudLine(player, "timer", 5, color("&eTime: 0:30"), 200);     // disappears by itself after 200 ticks

// A line for EVERY player: the function runs per player every 10 ticks ("" or null = hidden)
hudLine("online", 0, p -> color("&7Online: &f" + onlineCount()));

hudRemove(player, "kills");   // remove one line
hudRemove("online");          // remove a global line
hudClear(player);             // remove all lines and bars of this plugin for that player
List<String> shown = hudLines(player);                        // what the player sees now
```

Lines are sorted by `order` (lowest first; with the same order, global lines go before per-player lines, then the order they were added). Keys are per plugin, so two plugins can both use `"timer"`. The server shows at most `hud-max-lines` lines per player (default 15, from 1 to 40; set in `server.properties`).

**Where it is drawn.** The plugin decides, per player:

```java
hudPosition(player, HudPosition.RIGHT);    // RIGHT (default), LEFT or CENTER
hudOffsetY(player, 2);                     // move it up (+) or down (-) by that many lines (limit -20..20)
hudScreenWidth(player, 427);               // screen width in GUI pixels used to push it to a side (default 427; allowed 160..2000)
hudVisible(player, false);                 // hide/show the scoreboard (titles and the action bar still appear)
```

The getters `hudPosition(p)`, `hudOffsetY(p)`, `hudScreenWidth(p)` and `isHudVisible(p)` return the current values. There is no `/hud` command: the plugin that draws the scoreboard is in charge of these values.

**Titles, action bar and boss bars:**

```java
title(player, color("&bRound 1"), "Go!", 60);        // title + subtitle in the middle for 60 ticks (hides the scoreboard meanwhile)
broadcastTitle("Round 1", "Go!", 60);
actionBar(player, color("&eHold on"), 40);           // text above the hotbar for 40 ticks (hides boss bars meanwhile)
broadcastActionBar("text", 40);
bossBar(player, "dragon", color("&cDragon"), 0.75);  // text + progress bar 0..1; same key = update
removeBossBar(player, "dragon");
clearTitle(player);
String[] now = hudView(player);                      // {popup, tip} as the player sees them
```

`player.sendTitle(title, subtitle)` and `player.addTitle(title, subtitle, fadeIn, stay, fadeOut)` also exist (PocketMine style).

`Events.PlayerHud` fires for each player every 10 ticks (only if someone listens) with the scoreboard `lines` (editable) and the `popup` text, so other plugins can add or change lines.

### Services: talking to other plugins

A plugin can publish an interface and other plugins can use it.

```java
// plugin A (Eco): publish
registerService(Economy.class, this);

// plugin B (Shop): use it. A is listed in `depend` so it is already enabled here.
Economy eco = service(Economy.class);          // null if nobody published it
```

Rules:

- Put the shared interface (`Economy`) in plugin A's jar. Plugin B must compile against A's jar and list `depend=Eco` (hard) or `softdepend=Eco` (optional) in its `plugin.properties`. That is also what makes A's classes visible to B at run time.
- With `softdepend`, A may be missing: check `plugin("Eco") != null` or `service(Economy.class) != null` **before** touching any class of A, otherwise you get `NoClassDefFoundError`.
- A service is removed when its plugin is disabled.
- You can also read another plugin directly: `Plugin p = plugin("Eco");`.
- To use events between plugins, define an `Event` subclass in the shared jar (see [Listening to events](#listening-to-events)).

There is a complete example in [Full example: an economy and a shop](#full-example-an-economy-and-a-shop).

### Text, arguments and cooldowns

**Colors.** Minecraft uses `§` followed by a code. In your code write `&` and convert:

```java
color("&aGreen &lbold &rreset")     // static: Plugin.color(...) / Text.color(...)
color("&&")                         // a literal &
stripColor("§aHello")               // remove the codes
TextFormat.GOLD + "text"            // constants: net.BlockNes.util.TextFormat (BLACK, DARK_BLUE ... GOLD, GRAY ...)
```

`color` converts `&` followed by `0-9`, `a-f`, `k-o` or `r`. Players cannot type `§` in chat (the server strips it).

**`Text` helpers** (`net.BlockNes.plugin.Text`, all static):

```java
Text.bar(0.4, 10, "&a", "&8")      // progress bar of 10 segments, 40% filled
Text.ticks(1200)                   // "1:00"   (20 ticks = 1 s)
Text.seconds(3725)                 // "1:02:05"
Text.number(1234567)               // "1,234,567"
Text.truncate(text, 16)            // shorten to 16 visible letters adding "..."
Text.width(text)                   // approximate width in GUI pixels
```

**`Cooldowns`**: a timer per key (normally the player name). Server thread only.

```java
private final Cooldowns kit = new Cooldowns();

if (!kit.tryUse(p.name, 60_000)) {                       // true = allowed (and starts the 60 s wait)
    message(p, "Wait " + kit.remainingSeconds(p.name) + " s");
    return;
}
// also: remaining(key) in ms, isActive(key), reset(key), clear()
```

**`Args`** was shown in [Commands](#commands).

### Translations

`tr(key, args...)` gives a text in the **server's language** (`eng`, `spa`, `por`, `fra`, `deu`, `ita`; the `language=` value of `server.properties`). Put the files inside your jar:

```
lang/eng.properties      welcome=Welcome, %s!
lang/spa.properties      welcome=Bienvenido, %s!
```

```java
message(player, tr("welcome", player.name));
```

If the key is missing in the current language it uses `lang/eng.properties`; if it is missing there too, you get the key itself. The values go through `String.format`. The files are read as UTF-8.

---

## Events

All events live in `net.BlockNes.plugin.Events` (one nested class each) and extend `Event`. Each one also implements the interface with its PocketMine name, in `net.BlockNes.event.*` (`player`, `block`, `entity`, `inventory`, `level`, `server`, `plugin`), so you can listen with either.

How to read the tables:

- **Cancel** = `yes` means `e.setCancelled(true)` stops the action. `no` means it is informational (or already happened) and cancelling does nothing.
- **Data** lists the public fields. Fields marked *(edit)* can be changed in your handler and the server uses the new value.
- Every player event has `e.player` (and `e.getPlayer()`).
- Coordinates `x, y, z` of blocks are integers; for entities and impacts they are `double`. `dimension`/`from`/`to` are `0` (overworld) or `1` (nether).
- A few events are only created when somebody listens to them, because they would fire very often (`MobMove`, `BlockUpdate`, `PlayerHud`, `ServerTick`).

```java
on(Events.BlockBreak.class, e -> {
    if (e.id == 7) e.setCancelled(true);                 // fields
});
on(net.BlockNes.event.block.BlockBreakEvent.class, e -> {
    if (e.getBlockId() == 7) e.setCancelled(true);       // PocketMine-style getters
});
```

### Players

| Event | Cancel | Data and notes |
| --- | --- | --- |
| `PlayerPreLogin` | yes | Right after the name is validated, before joining. Cancelling kicks with `kickMessage` *(edit)*. Also implements `PlayerHandshakeEvent` and `PlayerAuthenticateEvent` |
| `PlayerLogin` | yes | The player passed pre-login and is about to enter. Cancelling kicks with `kickMessage` *(edit)* |
| `PlayerJoin` | no | The player finished joining (already in the world) |
| `PlayerQuit` | no | The player disconnected |
| `PlayerKick` | yes | About to be kicked. `reason` *(edit)* |
| `PlayerChat` | yes | `message` *(edit)*, `format` *(edit; a `String.format` pattern with two arguments: name and text, for example `"<%s> %s"`; `null` = normal)*, `recipients` *(edit; add or remove players)* |
| `PlayerCommand` | yes | A `/command` typed in chat, before it runs. `line` |
| `PlayerTextPreSend` | yes | A chat/command text is about to reach a player. `message` *(edit)*. Fired by `message`/`broadcast`; not for tips, popups or titles |
| `PlayerMove` | yes | Only when the player changes **block**. `fromX/Y/Z`, `toX/Y/Z`. Cancelling puts the player back |
| `PlayerTeleport` | yes | A `/tp` or `teleportChecked(...)`. `fromX/Y/Z`, `toX/Y/Z` *(edit)*. Internal teleports of the server do not fire it |
| `PlayerChangeDimension` | no | `from`, `to` |
| `PlayerInteract` | yes | Right click on a block (`air=false`, with `x, y, z, face`) or in the air (`air=true`). `item` |
| `PlayerDamage` | yes* | The player is about to be hurt. `amount` *(edit)*, `cause`, `forced`, `attacker` (a `Player` or `null`). *Forced damage (void, fire, hunger, poison...) ignores cancelling. `cause` is the death-message text, in Spanish for built-in causes |
| `PlayerDamageByMob` | yes | A mob hits a player. `mob`, `damage` *(edit)* |
| `EntityDamageByChildEntity` | yes | A projectile hits a player. `child` (`ARROW`, `SNOWBALL`...), `shooterPlayer`, `shooterMob`, `damage` *(edit)* |
| `EntityDamageByBlock` | yes | Lava, fire or cactus hurts a player. `blockId`, `cause`, `damage` *(edit)* |
| `PlayerDeath` | no | `message` *(edit; `null` = no message)*, `keepInventory` *(edit)* |
| `PlayerRespawn` | no | The player respawned |
| `PlayerHeal` | yes | About to regain health. `amount` *(edit)*. (`EntityRegainHealthEvent`) |
| `PlayerAttack` | yes | A player is about to hit a player or mob. `targetPlayer`, `targetMob`, `targetEntityId`, `weapon`, `damage` *(edit)* |
| `GolemConstruct` | yes | A player builds an iron golem or a snow golem. `golemType` (`Mob.IRON_GOLEM` = 20 or `Mob.SNOW_GOLEM` = 21), `x, y, z` of the head block. Cancelled = no golem, and the blocks stay |
| `PlayerMilkCow` | yes | A player milks a cow or a mooshroom with an empty bucket in hand. `cow`, `bucket` (the bucket in hand). Cancelled = no milk, and the bucket is not used |
| `PlayerShearEntity` | yes | A player shears a sheep with shears. `entity`, `drops` *(edit the list; the wool by default)*. Cancelled = the sheep keeps its wool, and the shears are not used |
| `PlayerGamemode` | yes | `from`, `to` (`0` survival, `1` creative) |
| `PlayerHungerChange` | no | `from`, `to` (food level) |
| `PlayerExhaust` | yes | Hunger from tiredness. `amount` *(edit)* |
| `PlayerExperienceChange` | no | `before`, `after` (total XP) |
| `PlayerLevelChange` | no | `from`, `to` |
| `PlayerPickupExpOrb` | yes | `exp` *(edit)*, `x, y, z` |
| `PlayerAchievementAwarded` | yes | `achievement` (`"mineWood"` or `"diamond"`) |
| `PlayerToggleSneak` / `PlayerToggleSprint` | yes | `sneaking` / `sprinting` |
| `PlayerJump` | no | A guess: the player was on the ground and now goes up. The server does not read a jump packet |
| `PlayerAnimation` | yes | `action` (`1` = arm swing). Cancelling hides it from the others |
| `PlayerBedEnter` / `PlayerBedLeave` | yes / no | `x, y, z` of the bed |
| `PlayerLatencyUpdate` | no | `ping`, `previousPing` (checked every second) |
| `PlayerFish` | yes | A fish is caught. `fish` |
| `PlayerUseFishingRod` | yes | `action`: `START_FISHING` (0) or `STOP_FISHING` (1) |
| `PlayerHud` | no | Every 10 ticks per player (only if listened to). `lines` *(edit)*, `popup` *(edit)* |
| `MenuClick` | yes | A click on a [menu](#menus). `menu`, `slot`, `item`, `proposed` |

### Blocks

| Event | Cancel | Data and notes |
| --- | --- | --- |
| `BlockBreak` | yes | `x, y, z, id, meta`. The client is re-synced when cancelled |
| `BlockPlace` | yes | `x, y, z, id, meta` of the block being placed |
| `BlockDropItem` | no | Drops of a block that is broken, falls or explodes. `drops` *(edit the list)*, `player` (`null` if not a player), `x, y, z, id` |
| `BlockBurn` | yes | A flammable block burns. `x, y, z, id` |
| `BlockForm` | yes | Lava touching water becomes stone (4) or obsidian (49). `id` = the block that will form |
| `BlockSpread` | yes | Grass/mycelium spreading, fire spreading. `sourceX/Y/Z`, `x, y, z`, `id`, `meta` |
| `BlockFromTo` | yes | A liquid flows. `sourceX/Y/Z`, `x, y, z`, `liquidId` (8 water, 10 lava), `newMeta` |
| `BlockGrow` | yes | A crop, stem, sapling or plant grows. `x, y, z`, old `id/meta`, proposed `newId/newMeta` *(edit)*. Cancel to stop growth. |
| `LeavesDecay` | yes | `x, y, z, id` |
| `BlockUpdate` | yes | A block checks its neighbours. Only created if listened to. `x, y, z, id`. Cancelled = that block does not update and does not spread the check to its neighbours |
| `BlockRedstone` | no | `x, y, z` |
| `SignChange` | yes | `x, y, z`, `lines` *(edit; 4 strings)* |
| `ItemFrameDropItem` | yes | A player takes the item out of an item frame. Fired after `BlockBreak`. `player`, `x, y, z`, `blockId` (199), `getItem()`. Cancelled = the item stays in the frame. Otherwise it drops with the frame's drop chance |
| `ItemFrameRotate` | yes | A player right-clicks an item frame that already has an item. `item`, `x, y, z`, `oldRotation`, `newRotation` *(edit; 0-7)*. Cancelled = the frame does not turn |
| `MobSpawnerChange` | yes | A spawn egg is used on a spawner. `x, y, z`, `oldType`, `newType` *(edit; mob network id)*. Cancelled = the spawner keeps its mob and the egg is not used |
| `BlockFade` | yes | Exposed ice melts to a water source or a snow layer disappears. `x, y, z, blockId`. Cancel to preserve the original block. |
| `EntityBlockChange` | yes | A falling block (sand, gravel, anvil) lands. `x, y, z, fromId, toId, toMeta` |

### Mobs, projectiles and explosions

| Event | Cancel | Data and notes |
| --- | --- | --- |
| `MobSpawn` | yes | `mob`, `natural` (`false` = egg or `/summon`) |
| `EntityGenerate` | yes | A mob comes out of a spawner (a mob spawner). `type` (mob network id, for example `Mob.ZOMBIE`), `x, y, z` (block), `cause` (`EntityGenerate.CAUSE_MOB_SPAWNER` = 1). Fired before the mob exists, so `getEntity()` is `null`. Cancelled = no mob. Otherwise a `MobSpawn` follows with `natural` = `false` |
| `MobEatBlock` | yes | A sheep eats grass. `mob`, `x, y, z`, `id` (`2`, grass), `meta`, `residueId`, `residueMeta` *(edit, or `setResidue(id, meta)`; by default `3` and `0`, dirt)*. Cancelled = the grass stays |
| `MobDamage` | yes | `mob`, `by` (player or `null`), `amount` *(edit)* |
| `MobDeath` | no | `mob`, `killer`, `drops` *(edit the list)* |
| `MobDespawn` | no | A mob leaves the world. `mob` |
| `MobMove` | no | Each tick it moves on x/z. Only created if listened to. `mob`, `fromX`, `fromZ` |
| `MobTarget` | no | A hostile mob changes target. `mob`, `target` (player or `null`) |
| `EntityDamageByEntity` | no | A player hit a mob. `mob`, `damager` |
| `EntityTransform` | yes | An entity changes type; currently a pig struck by lightning may become a zombie pigman. `mob`, `targetType` (network type ID). Cancel to preserve the original. |
| `CreeperPower` | yes | A creeper is charged by lightning. `mob`, `cause` (`CreeperPowerEvent.CAUSE_LIGHTNING`, which is `2`). Cancel to keep it unpowered. PocketMine name: `CreeperPowerEvent`. |
| `EntityCombust` | yes | An entity is about to catch fire. `eid`, `player` (`null` if a mob), `cause`, `seconds` |
| `EntityCombustByBlock` | yes | Lava or fire sets a player on fire. `blockId`, `seconds` *(edit)* |
| `EntityCombustByEntity` | yes | A flaming arrow. `combusterPlayer`, `combusterMob`, `seconds` *(edit)* |
| `EntityEffectAdd` | yes | A player gets an effect. `effect` (`Food.EFFECT_*`), `amplifier`, `duration` (ticks) |
| `EntityEffectRemove` | yes | An effect ends. `effect`. Cancelling keeps it |
| `EntityShootBow` | yes | A player releases a bow. `bow`, `force` *(edit, 0..2)*. Cancelling spends neither arrow nor durability |
| `ProjectileLaunch` | yes | `type` (`ARROW`, `SNOWBALL`, `EGG`, `POTION`, `EXP_BOTTLE`, `FIREBALL`, `SMALL_FIREBALL`), `player`, `mob`, `x, y, z` |
| `ProjectileHit` | no | `type`, `shooterPlayer`, `shooterMob`, `x, y, z`, `hitPlayer`, `hitMob`, `blockId` (`-1` if none). Fired before the projectile applies its effect |
| `ExplosionPrime` | yes | A TNT is about to light (chain TNT too). `x, y, z`, `fuse` *(edit, ticks)* |
| `Explosion` | yes | `dimension`, `x, y, z`, `size`, `blocks` *(edit; each entry is `{x, y, z, id}`; remove entries to save blocks)* |
| `EntityEatItem` / `EntityDrinkPotion` | yes | `food` / `potion` |
| `VehicleMount` | yes | A player mounts a boat or minecart. `vehicle` (`"BOAT"`/`"MINECART"`), `vehicleEntityId`, `x, y, z` |
| `VehicleDismount` | no | `vehicle` |
| `VehicleDestroy` | yes | A player breaks a boat or minecart. Same fields as `VehicleMount` |
| `LightningStrike` | no | `x, y, z` |
| `ItemSpawn` | no | `entity` (an item on the ground) |
| `ItemDespawn` | yes | `entity`. Fires only when the item expires by age, as in PocketMine. Cancelled = the item stays and its age restarts. It does not fire for fire, for the void, or when a player picks the item up |

### Items and inventories

| Event | Cancel | Data and notes |
| --- | --- | --- |
| `ItemPickup` | yes | A player picks an item from the ground. `item`. Also `PlayerPickupItemEvent` and `InventoryPickupItemEvent` |
| `InventoryPickupArrow` | yes | `item`, `x, y, z` |
| `PlayerDropItem` | yes | `item`. Cancelling returns it to the inventory |
| `PlayerItemConsume` | yes | Eating or drinking. `item` |
| `PlayerItemHeld` | yes | `item`, `slot`, `hotbarSlot`. Cancelled = the previous item stays in hand and the client is resynced |
| `ItemUse` | no | `item` |
| `InventoryClick` | no | A slot change sent by the client. `window`, `slot`, `item`. Also `InventoryTransactionEvent` |
| `InventoryOpen` | yes | `type` (`"chest"`, `"enderchest"`, `"workbench"`, `"furnace"`), `x, y, z` |
| `InventoryClose` | no | |
| `ArmorEquip` | no | `slot`, `item` |
| `EntityArmorChange` | yes | `slot`, `oldItem`, `newItem` *(edit)* |
| `CraftItem` | yes | `input` (list of items spent), `result` |
| `SmeltItem` | yes | `x, y, z`, `source`, `result` *(edit)*. Cancelled = the item does not change (the cooking time is spent anyway). PocketMine name: `SmeltItemEvent`. |
| `FurnaceBurnFuel` | yes | `x, y, z`, `fuel`, `burnTime` *(edit; the ticks the fuel lasts)*. Cancelled = nothing burns and no fuel is used. PocketMine name: `FurnaceBurnFuelEvent`. |
| `PlayerEnchantItem` | no | `before`, `result`, `levelsSpent` |
| `PlayerBucketFill` | yes | `x, y, z, face`, `liquidId`, `bucket`, `item` *(edit; what the player gets)* |
| `PlayerBucketEmpty` | yes | Same fields; `item` is the empty bucket they get |

### World and level

| Event | Cancel | Data and notes |
| --- | --- | --- |
| `WorldLoad` | no | `world`, `nether`. Fired after plugins load, and when `/spawn <world>` changes the world |
| `WorldUnload` | yes, except at shutdown | `world`, `nether`. Fired before `/spawn <world>` switches the normal world: cancelled = nothing changes. At shutdown it fires before saving and cannot be cancelled |
| `LevelSave` | no | The world was saved |
| `ChunkLoad` / `ChunkPopulate` | no | `world`, `x`, `z` (chunk coordinates). `ChunkPopulate` = a new chunk finished generating |
| `ChunkUnload` | yes | `world`, `x`, `z`. Fired before the chunk is saved. Cancelled = the chunk stays in memory |
| `TimeChange` | no | `from`, `to`. Only when time is *set* (commands, sleeping), not the normal clock |
| `WeatherChange` | yes | From `/weather` or `setWeather(...)`; random weather does not fire it. `type` *(edit)*, `ticks` *(edit)* |
| `ThunderChange` | no | `thundering`, `weather` |
| `SpawnChange` | no | `level`, `previousX/Y/Z`, `x, y, z` |

### Server, connections and plugins

| Event | Cancel | Data and notes |
| --- | --- | --- |
| `ServerStart` | no | Plugins loaded, world ready, just before the first tick |
| `ServerStop` | no | Start of shutdown, before plugins are disabled |
| `ServerTick` | no | Every tick (20 per second). `tick`. Keep it very light |
| `ServerCommand` | yes | A command typed in the console. `sender`, `line` *(edit)* |
| `RemoteServerCommand` | yes | A command that came by RCON. `line` *(edit)* |
| `ConnectionAttempt` | no | A session opened, before login. `address` (`ip:port`) |
| `ConnectionClose` | no | `address`, `reason`. Also for failed logins |
| `PacketReceive` | yes | A packet from a player. `id`, `data` (`data[0]` = packet id). Also `DataPacketReceiveEvent` / `PacketDecodeEvent` |
| `PacketSend` | yes | A packet the server will send. `data`. Also `DataPacketSendEvent` / `PacketEncodeEvent` |
| `QueryRegenerate` | no | Every 5 s, the data that the query protocol shows. `motd`, `players`, `maxPlayers`, `version`, `level` |
| `LowMemory` | no | Every 5 s if more than 90 % of the maximum memory is used. `memory`, `memoryLimit`, `triggerCount` |
| `PluginEnable` / `PluginDisable` | no | `plugin`. `PluginDisable` fires just before `onDisable()` |

### Events with PocketMine names

These events also exist under their PocketMine names. Listen with the interface in `net.BlockNes.event.*` or with the class in `Events`:

| Event | Fired when | Cancel |
| --- | --- | --- |
| `LevelInitEvent` (`Events.LevelInit`) | a world is created for the first time (its folder was not on disk). Overworld and Nether, at startup | no |
| `LevelLoadEvent` / `LevelUnloadEvent` | same moment as `WorldLoad` / `WorldUnload` | `LevelUnloadEvent`: yes, except at shutdown |
| `FurnaceBurnEvent` (`Events.FurnaceBurnFuel`) | a furnace is about to burn fuel. `burnTime` *(edit)*. Cancelled = no fuel is used | yes |
| `FurnaceSmeltEvent` (`Events.SmeltItem`) | a furnace finishes an item. `result` *(edit)*. Cancelled = the item stays in the furnace | yes |
| `PlayerCreationEvent` (`Events.PlayerCreation`) | a new session opens, before the player object is created. `address`, `port`, `clientId` | no |
| `PlayerGlassBottleEvent` (`Events.PlayerGlassBottle`) | a player fills a glass bottle from a water block or cauldron | yes |
| `EntityEatBlockEvent` (`Events.EntityEatBlock`) | a player eats a cake slice | yes |
| `EntityEatEvent` | base of `EntityEatBlockEvent` and `EntityEatItemEvent`: listen to it to get both. Cancelled = nothing is eaten | yes |
| `EntityLevelChangeEvent` (`Events.EntityLevelChange`) | a player changes world or dimension (`origin`, `target`). Fired before `PlayerChangeDimension` | yes |
| `EntityMotionEvent` (`Events.EntityMotion`) | a push (knockback from mobs, projectiles or explosions) is about to be sent to a player. `getVector()` | yes |
| `EntityInventoryChangeEvent` (`Events.EntityInventoryChange`) | a player's slot is about to change. `newItem` *(edit)* | yes |
| `BlockEvent`, `ChunkEvent`, `LevelEvent`, `InventoryEvent`, `PlayerBucketEvent`, `ServerEvent`, `EntityCombustEvent` | base interfaces: listen to them to get the events of that family. `EntityCombustEvent` covers `Events.EntityCombustByBlock` and `Events.EntityCombustByEntity`, but not `Events.EntityCombust` | - |

`EntityGenerate` (mobs that come out of a spawner) and `ItemFrameDropItem` (items taken out of item frames) are in the Mobs and Blocks tables. `EntityEatEvent` is the base of `EntityEatItem` and `EntityEatBlock`, so listen to it to get both. For sheep eating grass, use `MobEatBlock`.

### Notes about events

- **Order of creation.** Events that need work to build (`MobMove`, `BlockUpdate`, `PlayerHud`, `ServerTick`, `PlayerLatencyUpdate`, `QueryRegenerate`, `LowMemory`) are skipped completely when no plugin listens to them, so listening has a cost: do it only if you need it.
- **`PlayerEvent` and `EntityEvent`.** `net.BlockNes.event.player.PlayerEvent` has `getPlayer()`. `net.BlockNes.event.entity.EntityEvent` has `getEntity()`, which returns a `Player` or a `Mob` (use `instanceof`). `PluginEvent` has `getPlugin()`.
- **Several events for one thing.** One action can fire more than one event: a player killing a mob fires `PlayerAttack` (before), `MobDamage` and `EntityDamageByEntity`; a mob hurting a player fires `PlayerDamageByMob` and `PlayerDamage`. Pick the one that has the data you need.
- **Reading packets.** `PacketReceive.data` and `PacketSend.data` are raw bytes (protocol 84). You can parse them with `raknetlib.BinaryStream`.

---

## Entity classes

Each entity has a class in `net.BlockNes.entity`, named like its entity (`Zombie`, `Cow`, `Arrow`...). `NETWORK_ID` is the id the client uses: `Zombie.NETWORK_ID` is `32`.

Get the class of a mob in the world with `entityOf(mob)` (API 1.8). It returns `null` if the mob has no class:

```java
import net.BlockNes.entity.*;

for (Mob m : mobsIn(0)) {
    Entity e = entityOf(m);
    if (e instanceof Living) {
        float health = ((Living) e).getHealth();     // the real health of the mob
        if (e instanceof Zombie) broadcast(color("&2A zombie with " + health + " HP"));
    }
}
```

| Member | Defined in | What it does |
| --- | --- | --- |
| `getNetworkId()` | `Entity` | The network id of the class, or `-1` if it has none |
| `getName()` | `Entity` | A readable name, for example `Zombie Villager` for `ZombieVillager` |
| `getHandle()` | `Entity` | The server object the class wraps: the `Mob` for `entityOf` |
| `getHealth()`, `getMaxHealth()`, `isAlive()` | `Living` | The real health of a mob in the world (`0` for types that are not mobs) |

These are the classes in `Entities`, with their network ids. `entityOf(mob)` returns the class of a mob whose type has that id (the `yes` rows). The `no` rows have no mob type, so only `Entities.create(id, handle)` makes them:

| Id | Class | Extends | From `entityOf(mob)` |
| --- | --- | --- | --- |
| 10 | `Chicken` | `Animal` | yes |
| 11 | `Cow` | `Animal` | yes |
| 12 | `Pig` | `Animal` | yes |
| 13 | `Sheep` | `Animal` | yes |
| 14 | `Wolf` | `Animal` | yes |
| 15 | `Villager` | `Creature` | yes |
| 16 | `Mooshroom` | `Animal` | yes |
| 17 | `Squid` | `WaterAnimal` | yes |
| 18 | `Rabbit` | `Animal` | yes |
| 19 | `Bat` | `FlyingAnimal` | yes |
| 20 | `IronGolem` | `Animal` | yes |
| 21 | `SnowGolem` | `Animal` | yes |
| 22 | `Ocelot` | `Animal` | yes |
| 32 | `Zombie` | `Monster` | yes |
| 33 | `Creeper` | `Monster` | yes |
| 34 | `Skeleton` | `Monster` | yes |
| 35 | `Spider` | `Monster` | yes |
| 36 | `PigZombie` | `Monster` | yes |
| 37 | `Slime` | `Living` | yes |
| 38 | `Enderman` | `Monster` | yes |
| 39 | `Silverfish` | `Monster` | yes |
| 40 | `CaveSpider` | `Monster` | yes |
| 41 | `Ghast` | `FlyingAnimal` | yes |
| 42 | `LavaSlime` | `Living` | yes |
| 43 | `Blaze` | `Monster` | yes |
| 44 | `ZombieVillager` | `Zombie` | yes |
| 45 | `Witch` | `Monster` | yes |
| 46 | `Stray` | `Skeleton` | yes |
| 47 | `Husk` | `Zombie` | yes |
| 64 | `Item` | `Entity` | no |
| 65 | `PrimedTNT` | `Entity` | no |
| 66 | `FallingSand` | `Entity` | no |
| 68 | `ThrownExpBottle` | `Projectile` | no |
| 69 | `XPOrb` | `Entity` | no |
| 77 | `FishingHook` | `Projectile` | no |
| 80 | `Arrow` | `Projectile` | no |
| 81 | `Snowball` | `Projectile` | no |
| 82 | `Egg` | `Projectile` | no |
| 83 | `Painting` | `Hanging` | no |
| 84 | `Minecart` | `Vehicle` | no |
| 86 | `ThrownPotion` | `Projectile` | no |
| 90 | `Boat` | `Vehicle` | no |
| 93 | `Lightning` | `Entity` | no |
| 96 | `MinecartHopper` | `Minecart` | no |
| 97 | `MinecartTNT` | `Minecart` | no |
| 98 | `MinecartChest` | `Minecart` | no |

The `no` rows are not mobs (for example `Arrow`, `Boat` or `Minecart`): their class only gives you the id and the name, and their behaviour lives in the server's own code. `Human` and the abstract classes (`Entity`, `Living`, `Creature`, `Monster`, `Animal`...) are types too, and `NPC` is an interface.

`Attribute`, `AttributeMap`, `Effect` and `InstantEffect` are PocketMine-style types for attributes and status effects. The server does not use them yet.

---

## API 1.6 additions (inside `Plugin`)

API 1.6 adds a **PocketMine-style facade** so that code written for PocketMine is easy to port. None of it is required: the shorter methods of `Plugin` do the same things. A plugin that uses these classes should declare `api=1.6` or higher.

| Class | Package | What it is |
| --- | --- | --- |
| `Server` | `net.BlockNes` | `Server.getInstance()` gives the running server (it is `null` before the server is created) |
| `Level` | `net.BlockNes.level` | A world as plugins see it |
| `Position`, `Vector3` | `net.BlockNes.level`, `net.BlockNes.math` | Coordinates (`Position` = `Vector3` + `Level`) |
| `TextFormat` | `net.BlockNes.util` | Color constants (`TextFormat.GOLD`) |
| `Listener`, `Cancellable` | `net.BlockNes.event` | Marker interface for listener classes, and the cancel interface |
| `CommandExecutor`, `CommandMap`, `CommandSender` | `net.BlockNes.command` | Commands, PocketMine style |
| `ServerScheduler`, `Task`, `PluginTask`, `AsyncTask`, `TaskHandler` | `net.BlockNes.scheduler` | The scheduler |
| `Metadatable`, `MetadataValue`, `FixedMetadataValue`, `MetadataStore` | `net.BlockNes.metadata` | Metadata on players and levels |
| `PlayerInventory` | `net.BlockNes.item` | `player.getPlayerInventory()` |

**The `Server` object** (`Server.getInstance()` or `getServer()`):

```java
Server s = Server.getInstance();
s.getName();  s.getVersion();  s.getMinecraftVersion();  s.getPort();  s.getMaxPlayers();  s.getViewDistance();
s.getMotd();  s.getDefaultGamemode();  s.getSpawnRadius();
s.getOnlinePlayers();  s.getPlayer("ste");  s.getPlayerExact("Steve");
s.getDefaultLevel();               // the overworld
s.getLevelByName("world");         // by folder name ("world", "nether"), or null
s.getLevels();  s.levelOf(1);      // levelOf(dimension)
s.getPluginManager();  s.getScheduler();  s.getCommandMap();  s.getLogger();
s.getDataPath();  s.getPluginPath();
s.isOp("Steve");  s.getWhitelisted();  s.hasWhitelist();  s.getNameBans();  s.getIPBans();
s.broadcastMessage("text", listOfPlayers);
s.dispatchCommand(sender, "say hi");  s.getConsoleSender();
s.shutdown();
```

**`Level`** is a world: `getName()`, `getDimension()`, `isNether()`, `getBlockIdAt(x,y,z)`, `getBlockDataAt(x,y,z)`, `setBlockAt(x,y,z,id,meta)`, `setBlockIdAt`, `setBlockDataAt`, `getHighestBlockAt(x,z)`, `getSpawnLocation()`, `getTime()`/`setTime(t)`, `stopTime()`/`startTime()`, `setWeather(type, ticks)`, `getPlayers()`, `getMobs()`, `dropItem(Vector3, Item)`, `explode(Vector3, size)`, `addLevelEvent(Vector3, eventId, data)`, `save()`. It is also `Metadatable`. For a player: `player.getLevel()` and `player.getPosition()`.

**`Vector3`** is an immutable vector: `x, y, z`, `getFloorX()`, `add`, `subtract`, `multiply`, `divide`, `floor`, `ceil`, `dot`, `length`, `distance(v)`, `distanceSquared(v)`, `normalize`. `Position` adds `getLevel()`.

**Listeners the PocketMine way.** A class that implements `Listener` and has `@EventHandler` methods, registered with the plugin manager (the plugin's own `registerEvents(obj)` does the same and also works with classes that do not implement `Listener`):

```java
public class MyPlugin extends PluginBase implements Listener {
    @Override public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);       // (listener, plugin)
        on(PlayerChatEvent.class, e -> e.setMessage(e.getMessage().toUpperCase()));   // by interface name
    }
    @EventHandler public void onJoin(Events.PlayerJoin e) { }
}
```

**The scheduler** (`getServer().getScheduler()`):

```java
ServerScheduler sch = getServer().getScheduler();
sch.scheduleTask(task);                          // next tick
sch.scheduleDelayedTask(task, 20);               // after 20 ticks
sch.scheduleRepeatingTask(task, 20 * 30);        // every 30 s
sch.scheduleDelayedRepeatingTask(task, 40, 20);  // delay, then period
sch.scheduleAsyncTask(asyncTask);                // returns the id
sch.cancelTask(id);  sch.cancelTasks(plugin);  sch.cancelAllTasks();  sch.isQueued(id);

// a task that belongs to the plugin (cancelled for you when it is disabled)
sch.scheduleRepeatingTask(new PluginTask(this) {
    @Override public void onRun(long currentTick) { getOwner().getLogger().info("tick " + currentTick); }
}, 20 * 30);
```

`net.BlockNes.scheduler.Task` has `onRun(long currentTick)` and an optional `onCancel()`. Methods return a `TaskHandler` (`getTaskId()`, `cancel()`, `isCancelled()`, `isRepeating()`...). `scheduleAsyncTaskToWorker(task, worker)` ignores the worker number. A `PluginTask` cannot be scheduled from a disabled plugin (`IllegalStateException`).

**Metadata.** Attach values to players and levels and remove them with the plugin:

```java
player.setMetadata("tour.seen", new FixedMetadataValue(this, true));
player.hasMetadata("tour.seen");  player.getMetadata("tour.seen");  player.removeMetadata("tour.seen", this);
```

When your plugin is disabled, the metadata it set is removed.

**Items and inventory.** `Item.get(id, damage, count)`, `getId()`, `getDamage()`, `getCount()`, `getCustomName()`, `getEnchantments()`; `player.getPlayerInventory()` with `addItem`, `removeItem`, `setItem`, `getItem`, `getItemInHand`, `contains`, `getContents`, `clear`, `sendContents`.

**Player extras.** `getPing()`, `getIp()`, `getFood()`, `getXpLevel()`, `getXpProgress()`, `hasPermission(node)`, `sendTitle(title, subtitle)`, `addTitle(title, subtitle, fadeIn, stay, fadeOut)`.

**Other PocketMine-style methods** exist on `PluginManager` (`getPlugin`, `getPlugins`, `isPluginEnabled`, `enablePlugin`, `disablePlugin`, `callEvent`, `registerEvents`, `unregisterEvents`, `addPermission`, `removePermission`, `getPermission`, `getPermissions`) and on `CommandMap` (`getCommand`, `getCommands`, `register`, `unregister`, `dispatch`).

---

## API 1.7 additions (inside `Plugin`)

API 1.7 (`api=1.7`) added the items below. Plugins built for older versions keep working.

- **New events.** The events added with API 1.7 (BlockNes 1.2.0 and the releases just before it) are part of it, for example `ProjectileHit`, `PlayerBucketFill`, `PlayerBucketEmpty`, `PlayerPickupExpOrb`, `PlayerLogin`, `PlayerUseFishingRod`, `ItemSpawn`, `ItemDespawn`, `RemoteServerCommand`, `PlayerDamageByMob`, `EntityDamageByChildEntity`, `EntityDamageByBlock`, `EntityCombustByBlock`, `EntityCombustByEntity`, `EntityEffectAdd`, `EntityEffectRemove`, `InventoryPickupArrow`, `EntityArmorChange`, `EntityEatItem`, `EntityDrinkPotion`, `EntityBlockChange`, `SpawnChange`, `LowMemory` and `QueryRegenerate`, plus the cancellable `PlayerAttack`, `VehicleMount` and `VehicleDestroy`, and `ServerStart`, `ServerStop`, `ConnectionAttempt`, `ConnectionClose`, `WorldLoad`, `WorldUnload`, `TimeChange`, `MobDespawn` and `SmeltItem`. See [Events](#events).
- **`Plugin.tr(key, args...)`**: translate your own messages with `lang/<code>.properties` inside the jar (see [Translations](#translations)).
- **Listening by interface name** (`on(PlayerChatEvent.class, ...)`) is counted as part of 1.7, so a plugin that relies on it should say `api=1.7`.
- **Plugin-controlled scoreboard**: the plugin that draws it decides the corner (`hudPosition`), the vertical offset (`hudOffsetY`) and the width (`hudScreenWidth`, 427 by default). The new getters `hudOffsetY(p)` and `hudScreenWidth(p)` return the current values. The `/hud` command and the `hud-position`, `hud-offset-y` and `hud-screen-width` settings of `server.properties` are gone. `hud-max-lines` is still a server setting.
- **Package rename**: `raknetlib` replaces `net.BlockNes.raknet` in plugin imports.

---

## API 1.8 additions (inside `Plugin`)

API 1.8 makes the common calls shorter, and adds the entity classes and the events of [Entity classes](#entity-classes). Plugins built for older versions keep working. A plugin that uses the calls below should declare `api=1.8`.

| Call | What it does |
| --- | --- |
| `command("name")` | Starts the chained builder for a command: `describe`, `usage`, `aliases`, `permission`, `opOnly`, then `run` or `executor`. See [Commands](#commands) |
| `playerOf(sender)` | The `Player` behind a command sender, or `null` for the console |
| `entityOf(mob)` | The entity class of a mob (`Zombie`, `Cow`...), or `null`. See [Entity classes](#entity-classes) |

New events, in the tables of [Events](#events): `EntityGenerate` (a mob that comes out of a spawner), `ItemFrameDropItem`, `ItemFrameRotate` and `MobSpawnerChange`.

New types, in `net.BlockNes.entity`: `Entity`, `Living`, one class per entity, and `Attribute`, `AttributeMap`, `Effect` and `InstantEffect`.

---

## Limits, errors and performance

**Errors never stop the server.** If a handler, command, task or callback throws, the error is logged with the plugin's name and the stack trace; the other handlers keep running. For a command, the sender is told that an error happened.

**Optional limits.** By default there are no limits (like PocketMine). The server owner can turn them on in `server.properties`:

| Key | Default | Meaning |
| --- | --- | --- |
| `plugin-max-errors` | `0` | Errors in handlers before the plugin is disabled. `0` = no limit |
| `plugin-max-slow-calls` | `0` | Handler calls slower than 100 ms before the plugin is disabled. `0` = no limit |
| `plugin-max-tasks` | `0` | Pending tasks per plugin. More of them throw `IllegalStateException`. `0` = no limit |
| `hud-max-lines` | `15` | Scoreboard lines per player (1 to 40) |

Even with no limit, a plugin that has 100 slow calls (more than 100 ms each) gets one warning in the log.

**Performance tips**

- Everything runs on the server thread: a slow handler makes **the whole server lag**. A tick has 50 ms.
- Do HTTP, file and database work with `runAsync` and come back with `runSync` or the `done` callback.
- `PlayerMove` fires whenever a player changes block, and `ServerTick` fires 20 times a second. Do the cheap check first and return early.
- Do not hold a `Player` object after the player leaves; look players up by name when you need them.
- Big `Region` operations touch every block: split them over several ticks.

---

## Full example: an economy and a shop

Two plugins that talk to each other through a **service**. This is real, tested code.

**Plugin 1: `Eco`** publishes an `Economy` API.

`plugin.properties`:

```properties
name=Eco
main=eco.EcoPlugin
version=1.0
api=1.8
```

`eco/Economy.java` (the shared interface, in Eco's jar):

```java
package eco;

/** The API other plugins use. Put it in your jar; dependents compile against it. */
public interface Economy {
    int balance(String player);
    void add(String player, int amount);
    boolean take(String player, int amount);
}
```

`eco/EcoPlugin.java`:

```java
package eco;

import net.BlockNes.plugin.JsonConfig;
import net.BlockNes.plugin.Plugin;

public class EcoPlugin extends Plugin implements Economy {
    private JsonConfig data;

    @Override
    public void onEnable() {
        data = jsonConfig("balances.json");
        registerService(Economy.class, this);      // publish the API
        command("money", "Show your money", "", false, (s, a) -> s.send("Money: " + balance(s.name())));
    }

    @Override public void onDisable() { data.save(); }

    @Override public int balance(String p) { return data.getInt("balances." + p.toLowerCase(), 0); }
    @Override public void add(String p, int n) { data.set("balances." + p.toLowerCase(), balance(p) + n).save(); }
    @Override public boolean take(String p, int n) {
        if (balance(p) < n) return false;
        data.set("balances." + p.toLowerCase(), balance(p) - n).save();
        return true;
    }
}
```

**Plugin 2: `Shop`** uses it. It needs `Eco`, so it says `depend=Eco` and is compiled with Eco's jar on the class path.

`plugin.properties`:

```properties
name=Shop
main=shop.ShopPlugin
version=1.0
depend=Eco
api=1.8
```

`shop/ShopPlugin.java`:

```java
package shop;

import eco.Economy;
import net.BlockNes.item.Item;
import net.BlockNes.plugin.Plugin;

public class ShopPlugin extends Plugin {
    @Override
    public void onEnable() {
        // Eco is a hard dependency, so it is already enabled here.
        Economy eco = service(Economy.class);
        command("buybread", "Buy bread for 5 coins", "", false, (s, a) -> {
            if (!s.isPlayer()) { s.send("Players only"); return; }
            if (!eco.take(s.name(), 5)) { s.send("Not enough money"); return; }
            give(playerExact(s.name()), Item.get(297, 0, 1));
            s.send("Bought 1 bread");
        });
    }
}
```

Compile Eco first, then Shop with `-cp server-plugin-api.jar:Eco.jar` (use `;` instead of `:` on Windows). Put both jars in `plugins/`. The server loads Eco first (because of `depend`). `/plugins disable Eco` also disables Shop.

**A bigger "tour"** that uses many features at once (config, `@EventHandler`, a custom event, permissions, a cooldown, a menu, recipes, per-player data, countdowns, async and the HUD) is a good starting point for your own plugin:

```java
package ejemplo;

import net.BlockNes.Player;
import net.BlockNes.command.CommandSender;
import net.BlockNes.item.Item;
import net.BlockNes.plugin.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Tour extends Plugin {
    private final Cooldowns kitCooldown = new Cooldowns();

    /** A custom event other plugins can listen to. */
    public static class ArenaJoinEvent extends Event {
        public final Player player;
        public ArenaJoinEvent(Player p) { player = p; }
    }

    /** @EventHandler style (PocketMine/Bukkit). */
    public class Guard {
        @EventHandler(priority = Priority.HIGH, ignoreCancelled = true)
        public void onBreak(Events.BlockBreak e) {
            if (e.id == 7) {                         // bedrock
                e.setCancelled(true);
                message(e.player, color("&cYou cannot break bedrock"));
            }
        }
    }

    @Override
    public void onEnable() {
        // ---- config.properties ----
        Config cfg = config();
        cfg.setDefault("welcome", "&aWelcome, %s!").setDefault("kit-seconds", 60).save();

        // ---- events ----
        on(Events.PlayerJoin.class, e -> message(e.player, color(String.format(cfg.getString("welcome", "Hi %s"), e.player.name))));
        on(Events.PlayerChat.class, Priority.HIGH, true, e -> e.message = e.message.replace("badword", "***"));
        on(Events.PlayerDamage.class, e -> { if (!e.forced) e.setCancelled(true); });   // forced damage (void, fire...) cannot be cancelled
        once(Events.PlayerQuit.class, e -> log("First quit seen: " + e.player.name));
        registerEvents(new Guard());
        on(ArenaJoinEvent.class, e -> broadcast(color("&6" + e.player.name + " entered the arena")));

        // ---- permissions ----
        registerPermission("tour.kit", "Use /kit", PermissionDefault.TRUE);
        registerPermission("tour.admin", "Use /give2", PermissionDefault.OP);

        // ---- commands (lambda) ----
        command("kit").describe("Get a starter kit").permission("tour.kit").run((sender, args) -> {
            Player p = playerExact(sender.name());
            if (p == null) { sender.send("Players only"); return; }
            if (!kitCooldown.tryUse(p.name, cfg.getInt("kit-seconds", 60) * 1000L)) {
                sender.send("Wait " + kitCooldown.remainingSeconds(p.name) + "s");
                return;
            }
            give(p, Item.get(297, 0, 8));
            p.getPlayerInventory().addItem(Item.get(272, 0, 1).withName(color("&6Starter Sword")));
            sender.send("Kit given");
        });

        // ---- commands (CommandExecutor, PocketMine style) ----
        command("give2", "Give items", "<player> [count]", "tour.admin", (sender, cmd, label, args) -> {
            if (args.length < 1) return false;                // false = print the usage
            Player target = player(args[0]);
            if (target == null) { sender.send("Player not found"); return true; }
            int n = Args.getInt(args, 1, 1, 1, 64);
            give(target, Item.get(264, 0, n));
            sender.send("Gave " + n + " diamonds to " + target.name);
            return true;
        });

        // ---- commands (class) ----
        registerCommand(new Command("arena", "Join the arena", "", "a") {
            @Override public boolean execute(CommandSender s, String label, String[] args) {
                Player p = playerExact(s.name());
                if (p == null) return false;
                fire(new ArenaJoinEvent(p));
                return true;
            }
        });
        alias("kit", "starter");

        // ---- per-player data ----
        command("stats", "Show your stats", "", false, (s, a) -> {
            JsonConfig d = playerData().of(s.name());
            d.set("stats.commands", d.getInt("stats.commands", 0) + 1);
            s.send("Commands run: " + d.getInt("stats.commands", 0));
        });

        // ---- menu ----
        command("shopmenu", "Open a menu", "", false, (s, a) -> {
            Player p = playerExact(s.name());
            if (p == null) return;
            Menu m = menu(3);
            m.border(Item.get(160, 7, 1).withName(" "));
            m.setItem(13, Item.get(297, 0, 1).withName("Bread"), click -> {
                give(click.player, Item.get(297, 0, 1));
                closeMenu(click.player);
            });
            m.onClose(pl -> message(pl, "Menu closed"));
            openMenu(p, m);
        });

        // ---- recipes ----
        Map<Character, Item> ing = new HashMap<>();
        ing.put('D', Item.get(264));
        ing.put('S', Item.get(280));
        addShapedRecipe(Item.get(278), new String[]{"DDD", " S ", " S "}, ing);
        addShapelessRecipe(Item.get(297, 0, 2), Arrays.asList(Item.get(296), Item.get(296)));

        // ---- tasks ----
        runRepeating(0, 20 * 60, () -> broadcast("One minute passed"));
        command("countdown", "Start a countdown", "", true, (s, a) ->
            countdown(5, n -> broadcast("Starting in " + n), () -> broadcast("Go!")));
        command("slow", "Async example", "", false, (s, a) ->
            runAsync(() -> { try { Thread.sleep(500); } catch (InterruptedException ignored) { } return 42; },
                     result -> s.send("Async result: " + result)));

        // ---- HUD ----
        hudLine("online", 0, p -> color("&7Online: &f" + onlineCount()));
        on(Events.PlayerJoin.class, e -> { hudPosition(e.player, HudPosition.RIGHT); hudOffsetY(e.player, 2); });
    }

    @Override
    public void onDisable() {
        log("bye");
    }
}
```

---

## Things to know (limitations)

- **Client version.** The server speaks Minecraft PE **0.15.10**. Block and item ids are those of that version. The client has no title, boss bar or scoreboard packets, so the [HUD](#hud-scoreboard-titles-boss-bars) is emulated with text channels, and menus show no title.
- **`BlockFade`**, **`CreeperPower`**, and **`EntityTransform`** are now dispatched by their implemented mechanics. `EntityGenerate` (spawners) and `ItemFrameDropItem` (item frames) are dispatched too, now that BlockNes has spawners and item frames.
- **`PlayerMove`** fires only when the player changes block. **`PlayerJump`** is a guess from the position.
- **`PlayerTeleport`** is fired by `/tp` and `teleportChecked(...)` only. `teleport(...)` and the server's own teleports (portals, respawn) do not fire it.
- **`WeatherChange`** is not fired by random weather, only by `/weather` and `setWeather(...)`. **`TimeChange`** is not fired by the normal clock.
- **Death causes** (`PlayerDamage.cause`) are text in Spanish for the server's own causes; there is no cause enum.
- **Chat.** Players cannot send `§` color codes: the server removes them from their messages.
- **Worlds.** There are two dimensions (`0` overworld and `1` nether). `/spawn <world>` can change which folder is the overworld; plugins get `WorldUnload` for the old one and `WorldLoad` for the new one.
- **Reloading.** `/plugins reload` creates new class loaders; static state of the old plugin is lost.
- **Entities.** `entityOf(mob)` gives the class of a mob, but only the health of a `Living` mob is read from the server (`getHealth()`). The `Attribute`, `AttributeMap` and `Effect` types are not used by the server yet.
- **Threads.** Only the server thread may touch players, the world, menus or tasks. Inside `runAsync(...)` and `AsyncTask.onRun()` use only your own plain Java objects.

---

## Troubleshooting

| Problem | What to check |
| --- | --- |
| A source plugin (folder or `.java`) does not load | `needs a JDK` in the console: the server runs on a JRE, start it with a JDK. `has compile errors`: the console lists file, line and message. Nothing at all: the folder has no `plugin.properties`, or its name starts with a dot |
| The jar is ignored and the console says the jar has no `plugin.properties` | The file must be at the **root** of the jar, not inside a folder. Check with `jar tf MyPlugin.jar` |
| "needs name and main" | `name` and `main` are both required in `plugin.properties` |
| The name is rejected | Only letters, digits, `_` and `-` are allowed in `name` |
| "api ... newer than the server" | The plugin asks for an API newer than `1.8`. Lower `api=` or update the server |
| `ClassNotFoundException` for your main class | `main=` must be the full class name (`package.Class`) and the class must be in the jar |
| The class is found but the plugin does not start | The main class must extend `Plugin` (or `PluginBase`) and have a public no-argument constructor |
| The plugin is not loaded because of its dependencies | The plugins in `depend` are missing, or failed to load. Check the log above it |
| `NoClassDefFoundError` for a class of another plugin | Add that plugin to `depend` or `softdepend`, and do not use its classes when it is not installed |
| `NoClassDefFoundError` for a server class (`net/BlockNes/...`) | You bundled an old copy of the API, or compiled against a different version. Compile against the `server-plugin-api.jar` of your server and do not include it in your jar |
| `package net.BlockNes.raknet does not exist` | Change the import to `raknetlib` |
| A command does nothing | Registering is done in code (`command(...)`). The `commands.x.*` lines in `plugin.properties` are only defaults. The log warns about declared commands that were never registered |
| "You do not have permission" for a command | OPs always pass. Others need the node granted: in `plugins/permissions.properties`, by an attachment, or with a registered `PermissionDefault.TRUE` |
| Your handler runs but nothing changes | The event may be informational (see the **Cancel** column), or a handler with higher priority already changed it. Use `MONITOR` to see the final state |
| Config is empty and the log warns about `config.properties` | Put a `config.properties` at the root of your jar |
| The server lags | A handler or task is slow. The log warns after 100 calls slower than 100 ms. Move slow work to `runAsync` |
| An async callback never runs | The plugin was disabled before the work finished, or the work threw an exception (check the log) |
| Two plugins with the same name, only one loads | Names must be unique across jars, folders and `.java` files. The second one is skipped with a warning. Rename one of them |
| A source plugin keeps using old code | Delete `plugins/.cache/<name>/` and run `/plugins reload`. The cache is only rebuilt when a `.java` file changes (size or modification time) |
| Colors show as `&a` in chat | `message` and `broadcast` do not convert `&`. Wrap the text in `color(...)`, or use the `...f` variants (`messagef`, `broadcastf`) |
| The scoreboard is on the wrong side | The plugin decides: `hudPosition(player, ...)`, `hudScreenWidth(player, ...)` and `hudOffsetY(player, ...)` |

---

*This guide describes API 1.8 of BlockNes 1.2.3. The classes mentioned live in `server/src/main/java/net/BlockNes/plugin/` (`Plugin`, `Events`, `PluginManager`...), `event/`, `scheduler/`, `command/` and `level/`; read them for the exact signatures.*
