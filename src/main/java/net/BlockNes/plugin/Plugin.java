package net.BlockNes.plugin;

import net.BlockNes.Player;
import net.BlockNes.Server;
import net.BlockNes.command.CommandExecutor;
import net.BlockNes.command.CommandSender;
import net.BlockNes.command.Commands;
import net.BlockNes.item.Item;
import net.BlockNes.level.World;
import net.BlockNes.mob.Mob;
import net.BlockNes.entity.Entity;
import net.BlockNes.mob.Entities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.BlockNes.network.protocolo.TextPacket;

public abstract class Plugin {
    private PluginManager manager;
    private Server server;
    private PluginDescription desc;
    private Path dataFolder;
    private ClassLoader loader;
    private PluginLogger logger;
    private Config config;

    public static final int MAX_ERRORS = 100, MAX_SLOW_CALLS = 1000, MAX_TASKS = 5000;
     
    public static final long SLOW_NANOS = 100_000_000L;
     
    static final int SLOW_WARN_AT = 100;
    private int errorCount, slowCount;

    boolean noteError() {
        errorCount++;
        int max = manager == null ? 0 : manager.limits().maxErrors();
        return max > 0 && errorCount == max;
    }

    boolean noteSlow() {
        slowCount++;
        if (slowCount == SLOW_WARN_AT && logger != null)
            logger.warning("ha tenido " + SLOW_WARN_AT + " llamadas lentas (mas de 100 ms cada una): revisa su codigo");
        int max = manager == null ? 0 : manager.limits().maxSlowCalls();
        return max > 0 && slowCount == max;
    }

    public int errorCount() { return errorCount; }
    public int slowCallCount() { return slowCount; }

    final void init(PluginManager m, Server s, PluginDescription d, Path data, ClassLoader loader) {
        this.manager = m; this.server = s; this.desc = d; this.dataFolder = data; this.loader = loader;
        this.logger = new PluginLogger(d.getPrefix());
    }

    public void onLoad() {}
     
    public void onEnable() {}
     
    public void onDisable() {}

    public final Server server() { return server; }
    public Server getServer() { return server; }
     
    public PluginDescription getDescription() { return desc; }
    public final String name() { return desc.getName(); }
    public String getName() { return desc.getName(); }
    public final String version() { return desc.getVersion(); }
    public String getVersion() { return desc.getVersion(); }
    public final String author() { return desc.getAuthor(); }
    public final String description() { return desc.getDescription(); }
     
    public PluginLogger getLogger() { return logger; }
     
    public Path getFile() { return desc.jar; }
    public PluginManager pluginManager() { return manager; }
     
    public Collection<Plugin> plugins() { return manager.plugins(); }
     
    public final Path dataFolder() {
        try { Files.createDirectories(dataFolder); } catch (IOException e) { logWarn(net.BlockNes.lang.Lang.t("plg.folder.fail", dataFolder, e)); }
        return dataFolder;
    }
    public Path getDataFolder() { return dataFolder(); }

    public final void log(String msg) { logger.info(msg); }
    public final void logWarn(String msg) { logger.warning(msg); }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            if (!isEnabled()) {
                String err = manager.enable(name());
                if (err != null) logWarn(net.BlockNes.lang.Lang.t("plg.activate.fail", err));
            }
        } else if (isEnabled()) {
            manager.disable(name());
        }
    }

    public boolean isDisabled() { return !isEnabled(); }

    public final <T> void on(Class<T> type, Consumer<T> handler) { manager.addHandler(this, type, Priority.NORMAL, false, handler); }
     
    public final <T> void on(Class<T> type, Priority priority, Consumer<T> handler) { manager.addHandler(this, type, priority, false, handler); }
     
    public final <T> void on(Class<T> type, Priority priority, boolean ignoreCancelled, Consumer<T> handler) { manager.addHandler(this, type, priority, ignoreCancelled, handler); }
     
    public final <T extends Event> T fire(T event) { return manager.fire(event); }

    public final int registerEvents(Object listener) { return manager.registerListener(this, listener); }
     
    public int unregisterEvents(Object listener) { return listener == null ? 0 : manager.removeHandlers(this, listener); }
     
    public int unregisterAllEvents() { return manager.removeHandlers(this, null); }

    public final void command(String name, String description, String usage, boolean opOnly, PluginCommand exec) {
        manager.addCommand(this, name, description, usage, opOnly ? "op" : "", exec);
    }
     
    public final void command(String name, String description, String usage, String permission, PluginCommand exec) {
        manager.addCommand(this, name, description, usage, permission, exec);
    }

    public final void command(String name, String description, String usage, String permission, CommandExecutor executor) {
        if (executor == null) throw new IllegalArgumentException("command: the executor is null");
        Command c = new Command(name, description, usage) {
            @Override public boolean execute(CommandSender sender, String label, String[] args) {
                return executor.onCommand(sender, this, label, args);
            }
        };
        c.setPermission(permission);
        registerCommand(c);
    }
     
    public void registerCommand(Command command) { manager.addCommandObject(this, command); }
     
    public boolean unregisterCommand(String name) { return manager.removeCommand(this, name); }
     
    public Command getCommand(String name) { return manager.commandOf(this, name); }
     
    public final void alias(String command, String... aliases) { manager.addAliases(this, command, aliases); }

    public final CommandBuilder command(String name) { return new CommandBuilder(this, name); }
     
    public final Player playerOf(CommandSender sender) {
        return sender != null && sender.isPlayer() ? playerExact(sender.name()) : null;
    }
    public final boolean hasPermission(Commands.Sender s, String node) { return manager.hasPermission(s, node); }
     
    public boolean hasPermission(CommandSender s, String node) { return manager.hasPermission(s, node); }
     
    public boolean hasPermission(Player p, String node) { return p != null && manager.hasPermission(server.senderOf(p), node); }
     
    public final void dispatchCommand(Commands.Sender sender, String line) { server.commands().dispatch(sender, line); }
     
    public final Commands.Sender console() { return new net.BlockNes.command.Console(server); }

    public Permission registerPermission(String node, String description, PermissionDefault def) {
        Permission p = new Permission(node, description, def);
        manager.addPermissionOf(this, p);
        return p;
    }
     
    public Permission registerPermission(String node, String description) { return registerPermission(node, description, PermissionDefault.OP); }
     
    public Permission getPermission(String node) { return manager.getPermission(node); }
     
    public PermissionAttachment addAttachment(Player p) { return manager.addAttachment(this, p.name); }
     
    public PermissionAttachment addAttachment(Player p, String node, boolean value) { return addAttachment(p).setPermission(node, value); }
     
    public void removeAttachment(PermissionAttachment a) { if (a != null) a.remove(); }

    public final Task runLater(long delayTicks, Runnable body) { return manager.schedule(this, body, delayTicks, 0); }
     
    public final Task runRepeating(long delayTicks, long periodTicks, Runnable body) { return manager.schedule(this, body, delayTicks, Math.max(1, periodTicks)); }
     
    public final void cancelTasks() { manager.cancelTasks(this); }
     
    public boolean cancelTask(int taskId) { return manager.cancelTask(this, taskId); }
     
    public boolean isTaskQueued(int taskId) { return manager.isQueued(this, taskId); }
     
    public Task getTask(int taskId) { return manager.getTask(this, taskId); }
     
    public final void runSync(Runnable body) { manager.runSync(this, body); }
    public final boolean isEnabled() { return manager.isEnabled(this); }
     
    public final void runAsync(Runnable job) { manager.async(this, job); }
     
    public final <T> void runAsync(Supplier<T> work, Consumer<T> done) { manager.async(this, work, done); }
     
    public int runAsync(AsyncTask task) { return manager.runAsyncTask(this, task); }

    public final <T> void registerService(Class<T> type, T impl) { manager.addService(this, type, impl); }
     
    public final <T> T service(Class<T> type) { return manager.service(type); }
     
    public final Plugin plugin(String name) { return manager.get(name); }

    public final List<Player> players() { return server.onlinePlayers(); }
    public final Player player(String name) { return server.findPlayer(name); }
     
    public Player playerExact(String name) { return server.playerExact(name); }
     
    public List<Player> playersIn(int dimension) { return server.playersIn(dimension); }
    public final int maxPlayers() { return server.maxPlayers(); }
    public final void kick(Player p, String reason) { server.kick(p, reason); }
     
    public final void teleport(Player p, double x, double y, double z) { server.teleport(p, x, y, z); }
     
    public final void heal(Player p, int amount) { server.heal(p, amount); }
    public final void kill(Player p) { server.kill(p); }
     
    public final void damage(Player p, int amount, String cause) { server.damage(p, amount, cause); }
     
    public final void ignite(Player p, int seconds) { server.combust(p, seconds, "plugin"); }
     
    public final void extinguish(Player p) { server.extinguish(p); }
     
    public final boolean isOnFire(Player p) { return p.fireTicks > 0; }

    public final boolean banPlayer(String name) {
        boolean added = server.access().banName(name);
        Player p = server.playerExact(name);
        if (p != null) server.kick(p, net.BlockNes.lang.Lang.t("acc.kick.banned"));
        return added;
    }
     
    public final boolean pardonPlayer(String name) { return server.access().pardonName(name); }
    public final boolean isBanned(String name) { return server.access().isBannedName(name); }
     
    public final boolean banIp(String ip) {
        boolean added = server.access().banIp(ip);
        for (Player p : server.onlinePlayers()) if (ip.equals(net.BlockNes.BlockNes.AccessLists.ipOf(p))) server.kick(p, net.BlockNes.lang.Lang.t("acc.kick.ip"));
        return added;
    }
    public final boolean pardonIp(String ip) { return server.access().pardonIp(ip); }
    public final boolean isBannedIp(String ip) { return server.access().isBannedIp(ip); }
     
    public final boolean whitelistAdd(String name) { return server.access().whitelistAdd(name); }
    public final boolean whitelistRemove(String name) { return server.access().whitelistRemove(name); }
    public final boolean isWhitelisted(String name) { return server.access().isWhitelisted(name); }
     
    public final void setWhitelist(boolean on) { server.access().setWhitelistOn(on); }
    public final boolean isWhitelistOn() { return server.access().whitelistOn(); }
     
    public final boolean isPvpEnabled() { return server.pvpEnabled(); }
    public final boolean isSpawnMobsEnabled() { return server.spawnMobsEnabled(); }
     
    public final void setGamemode(Player p, int gm) { server.setGamemode(p, gm); }
     
    public final net.BlockNes.item.Item give(Player p, net.BlockNes.item.Item item) { return server.give(p, item); }
    public final boolean isOp(Player p) { return server.isOp(p); }
    public final void setOp(String name, boolean op) { server.setOp(name, op); }
    public final int time() { return server.getTime(); }
    public final void setTime(int t) { server.setTime(t); }
     
    public final int saveAll() { return server.saveAll(); }
    public final void broadcast(String msg) { server.broadcastMessage(msg); }
     
    public void broadcast(String msg, String permission) { server.broadcastMessage(msg, permission); }
     
    public void broadcastTip(String msg) { server.broadcastTip(msg); }
     
    public void broadcastPopup(String msg) { server.broadcastPopup(msg); }
    public final void message(Player p, String msg) { server.sendMessage(p, msg); }
     
    public final void popup(Player p, String msg) { if (p.loggedIn) server.send(p, TextPacket.text(TextPacket.POPUP, "", msg)); }
     
    public final void tip(Player p, String msg) { if (p.loggedIn) server.send(p, TextPacket.text(TextPacket.TIP, "", msg)); }

    public final void hudLine(Player p, String key, int order, String text) { manager.hud().line(this, p, key, order, text); }
    public final void hudLine(Player p, String key, String text) { manager.hud().line(this, p, key, 0, text); }
     
    public final void hudLine(String key, int order, Function<Player, String> fn) { manager.hud().global(this, key, order, fn); }
    public final void hudLine(String key, Function<Player, String> fn) { manager.hud().global(this, key, 0, fn); }
    public final void hudRemove(Player p, String key) { manager.hud().removeLine(this, p, key); }
     
    public final void hudRemove(String key) { manager.hud().removeGlobal(this, key); }
     
    public final void hudClear(Player p) { manager.hud().clear(this, p); }
     
    public final void title(Player p, String title, String subtitle, long stayTicks) { manager.hud().title(p, title, subtitle, stayTicks); }
     
    public final void actionBar(Player p, String text, long ticks) { manager.hud().actionBar(p, text, ticks); }
     
    public final void bossBar(Player p, String key, String text, double progress) { manager.hud().boss(this, p, key, text, progress); }
    public final void removeBossBar(Player p, String key) { manager.hud().removeBoss(this, p, key); }
     
    public final String[] hudView(Player p) { return manager.hud().view(p); }
     
    public final void hudLine(Player p, String key, int order, String text, long ttlTicks) { manager.hud().line(this, p, key, order, text, ttlTicks); }
     
    public final List<String> hudLines(Player p) { return manager.hud().linesOf(p); }
     
    public final void hudPosition(Player p, HudPosition pos) { manager.hud().setPosition(p, pos); }
    public final HudPosition hudPosition(Player p) { return manager.hud().position(p); }
     
    public final void hudVisible(Player p, boolean visible) { manager.hud().setHidden(p, !visible); }
    public final boolean isHudVisible(Player p) { return !manager.hud().hidden(p); }
     
    public final void hudScreenWidth(Player p, int pixels) { manager.hud().setScreenWidth(p, pixels); }
     
    public final int hudScreenWidth(Player p) { return manager.hud().screenWidth(p); }
     
    public final void hudOffsetY(Player p, int lines) { manager.hud().setOffsetY(p, lines); }
     
    public final int hudOffsetY(Player p) { return manager.hud().offsetY(p); }
     
    public final void broadcastTitle(String title, String subtitle, long stayTicks) { for (Player p : server.onlinePlayers()) manager.hud().title(p, title, subtitle, stayTicks); }
     
    public final void broadcastActionBar(String text, long ticks) { for (Player p : server.onlinePlayers()) manager.hud().actionBar(p, text, ticks); }
     
    public final void clearTitle(Player p) { manager.hud().title(p, "", "", 1); manager.hud().actionBar(p, "", 1); }

    public static String color(String s) { return Text.color(s); }
     
    public static String stripColor(String s) { return Text.strip(s); }
     
    public final void messagef(Player p, String format, Object... args) { server.sendMessage(p, Text.color(String.format(format, args))); }
     
    public final void broadcastf(String format, Object... args) { server.broadcastMessage(Text.color(String.format(format, args))); }

    public final Task runRepeating(long delayTicks, long periodTicks, Consumer<Task> body) {
        final Task[] ref = new Task[1];
        ref[0] = manager.schedule(this, () -> body.accept(ref[0]), delayTicks, Math.max(1, periodTicks));
        return ref[0];
    }
     
    public final Task runTimes(long delayTicks, long periodTicks, int times, java.util.function.IntConsumer body) {
        final int[] i = {0};
        return runRepeating(delayTicks, periodTicks, t -> {
            if (i[0] >= times) { t.cancel(); return; }
            int n = i[0]++;
            if (i[0] >= times) t.cancel();
            body.accept(n);
        });
    }
     
    public final Task countdown(int seconds, java.util.function.IntConsumer onSecond, Runnable done) {
        final int total = Math.max(0, seconds);
        if (total == 0) return runLater(1, done == null ? () -> {} : done);
        return runTimes(0, 20, total + 1, n -> {
            int left = total - n;
            if (left > 0) onSecond.accept(left); else if (done != null) done.run();
        });
    }
     
    public final <T> void once(Class<T> type, Consumer<T> handler) {
        final Object token = new Object();
        manager.addHandler(this, token, type, Priority.NORMAL, false, (T ev) -> { manager.removeHandlers(this, token); handler.accept(ev); });
    }
     
    public final <T> void once(Class<T> type, Priority priority, Consumer<T> handler) {
        final Object token = new Object();
        manager.addHandler(this, token, type, priority, false, (T ev) -> { manager.removeHandlers(this, token); handler.accept(ev); });
    }

    public final int onlineCount() { return server.onlinePlayers().size(); }
     
    public final List<String> playerNames() { return server.onlineNames(); }
    public final boolean isOnline(String name) { return server.playerExact(name) != null; }
     
    public final List<Player> playersNear(int dimension, double x, double y, double z, double radius) {
        List<Player> out = new ArrayList<>();
        double r2 = radius * radius;
        for (Player p : server.playersIn(dimension)) if (dist2(p.x, p.y, p.z, x, y, z) <= r2) out.add(p);
        out.sort((a, b) -> Double.compare(dist2(a.x, a.y, a.z, x, y, z), dist2(b.x, b.y, b.z, x, y, z)));
        return out;
    }
     
    public final Player nearestPlayer(int dimension, double x, double y, double z, double maxDistance) {
        List<Player> l = playersNear(dimension, x, y, z, maxDistance);
        return l.isEmpty() ? null : l.get(0);
    }
     
    public final List<Mob> mobsNear(int dimension, double x, double y, double z, double radius) {
        List<Mob> out = new ArrayList<>();
        double r2 = radius * radius;
        for (Mob m : server.mobsIn(dimension)) if (!m.dead && dist2(m.x, m.y, m.z, x, y, z) <= r2) out.add(m);
        out.sort((a, b) -> Double.compare(dist2(a.x, a.y, a.z, x, y, z), dist2(b.x, b.y, b.z, x, y, z)));
        return out;
    }
    private static double dist2(double ax, double ay, double az, double bx, double by, double bz) { double dx = ax - bx, dy = ay - by, dz = az - bz; return dx * dx + dy * dy + dz * dz; }

    public final boolean teleportChecked(Player p, double x, double y, double z) {
        Events.PlayerTeleport ev = manager.fire(new Events.PlayerTeleport(p, x, y, z));
        if (ev.isCancelled()) return false;
        server.teleport(p, ev.toX, ev.toY, ev.toZ);
        return true;
    }
     
    public final void setWeather(int type, int ticks) { server.setWeather(type, ticks); }
     
    public final void strikeLightning(double x, double y, double z) { server.spawnLightning(x, y, z); }
     
    public final void giveXp(Player p, int amount) { server.addXp(p, amount); }
     
    public final void setTimeStopped(boolean stopped) { server.setTimeStopped(stopped); }
     
    public final int kickAll(String reason, String exceptPermission) {
        int n = 0;
        for (Player p : server.onlinePlayers()) {
            if (exceptPermission != null && !exceptPermission.isEmpty() && hasPermission(p, exceptPermission)) continue;
            server.kick(p, reason); n++;
        }
        return n;
    }

    public final World world(int dimension) { return server.worldOf(dimension); }
    public final int getBlock(int dimension, int x, int y, int z) { return server.worldOf(dimension).getId(x, y, z); }
     
    public final boolean setBlock(int dimension, int x, int y, int z, int id, int meta) { return server.setBlockIn(dimension, x, y, z, id, meta); }
     
    public final Mob spawnMob(int dimension, int netId, double x, double y, double z) { return server.spawnMob(dimension, netId, x, y, z); }
     
    public List<Mob> mobsIn(int dimension) { return server.mobsIn(dimension); }
     
    public final Entity entityOf(Mob mob) { return Entities.of(mob); }
     
    public int highestBlockAt(int dimension, int x, int z) { return world(dimension).getHighestBlockAt(x, z); }
     
    public void dropItem(int dimension, double x, double y, double z, Item item) { server.dropItemIn(dimension, x, y, z, item); }
     
    public void explode(int dimension, double x, double y, double z, double size) { server.explodeIn(dimension, x, y, z, size); }
     
    public void playLevelEvent(int dimension, double x, double y, double z, int eventId, int data) { server.levelEventIn(dimension, x, y, z, eventId, data); }

    public InputStream getResource(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            URL u = loader instanceof PluginManager.PluginLoader ? ((PluginManager.PluginLoader) loader).own(name) : loader.getResource(name);
            return u == null ? null : u.openStream();
        } catch (IOException e) {
            return null;
        }
    }
     
    public final Path saveResource(String resource) { return saveResource(resource, false); }
     
    public Path saveResource(String resource, boolean replace) {
        Path out = dataFolder().resolve(resource);
        if (!replace && Files.exists(out)) return out;
        try (InputStream in = getResource(resource)) {
            if (in == null) { logWarn(net.BlockNes.lang.Lang.t("plg.resource.missing", resource)); return out; }
            if (out.getParent() != null) Files.createDirectories(out.getParent());
            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { logWarn(net.BlockNes.lang.Lang.t("plg.resource.save.fail", resource, e)); }
        return out;
    }

    public final Config config() {
        if (config == null) config = new Config(saveResource("config.properties"));
        return config;
    }
     
    public Config getConfig() { return config(); }
     
    public void saveConfig() { config().save(); }
     
    public void saveDefaultConfig() { saveResource("config.properties", false); }
     
    public final Config reloadConfig() { config = null; return config(); }
     
    public JsonConfig jsonConfig(String fileName) { return new JsonConfig(dataFolder().resolve(fileName)); }

    private PlayerStore playerStore;

    public final Menu menu(int rows) { return new Menu(this, server, rows); }

    public final void openMenu(Player p, Menu menu) { server.openMenu(p, menu); }

    public final void closeMenu(Player p) { server.closeMenu(p); }

    public final boolean isMenuOpen(Player p) { return p != null && p.windowMenu != null && p.windowId != -1; }

    public final net.BlockNes.item.Recipes.Recipe addShapedRecipe(net.BlockNes.item.Item result, String[] rows, Map<Character, net.BlockNes.item.Item> ingredients) {
        return server.addRecipe(net.BlockNes.item.Recipes.shaped(result, rows, ingredients), name());
    }

    public final net.BlockNes.item.Recipes.Recipe addShapelessRecipe(net.BlockNes.item.Item result, List<net.BlockNes.item.Item> ingredients) {
        return server.addRecipe(net.BlockNes.item.Recipes.shapeless(result, ingredients), name());
    }

    public final boolean removeRecipe(net.BlockNes.item.Recipes.Recipe recipe) { return server.removeRecipe(recipe); }

    public final Region region(int dimension, int x1, int y1, int z1, int x2, int y2, int z2) {
        return new Region(server, dimension, x1, y1, z1, x2, y2, z2);
    }

    public final PlayerStore playerData() {
        if (playerStore == null) {
            playerStore = new PlayerStore(dataFolder().resolve("players"));
            on(Events.PlayerQuit.class, Priority.MONITOR, e -> playerStore.unload(e.player.name));
        }
        return playerStore;
    }

    final void releaseFeatures() {
        if (server == null) return;
        try { server.closeMenusOf(this); }
        catch (RuntimeException e) { logger.logException(e); }
        try { server.removeRecipesOf(name()); }
        catch (RuntimeException e) { logger.logException(e); }
        if (playerStore != null) {
            try { playerStore.close(); }
            catch (RuntimeException e) { logger.logException(e); }
        }
        try { server.forgetMetadataOf(this); }
        catch (RuntimeException e) { logger.logException(e); }
    }

    private final java.util.Map<String, java.util.Properties> langFiles = new java.util.concurrent.ConcurrentHashMap<>();

    public final String tr(String key, Object... args) {
        String s = langLookup(net.BlockNes.lang.Lang.code(), key);
        if (s == null) s = langLookup("eng", key);
        if (s == null) s = key;
        return args.length == 0 ? s : String.format(java.util.Locale.ROOT, s, args);
    }

    private String langLookup(String code, String key) {
        return langFiles.computeIfAbsent(code, this::loadLangFile).getProperty(key);
    }

    private java.util.Properties loadLangFile(String code) {
        java.util.Properties p = new java.util.Properties();
        ClassLoader cl = getClass().getClassLoader();
        if (cl == null) return p;
        try (java.io.InputStream in = cl.getResourceAsStream("lang/" + code + ".properties")) {
            if (in != null) p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
             
        }
        return p;
    }
}
