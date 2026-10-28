package net.BlockNes.plugin;

import net.BlockNes.Player;
import net.BlockNes.Server;
import net.BlockNes.command.CommandSender;
import net.BlockNes.command.Commands;
import net.BlockNes.event.Listener;
import net.BlockNes.lang.Lang;
import net.BlockNes.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PluginManager {
     
    public static final String API_VERSION = "1.8";

    public static final class Limits {
        private volatile int maxErrors, maxSlowCalls, maxTasks;
         
        public int maxErrors() { return maxErrors; }
         
        public int maxSlowCalls() { return maxSlowCalls; }
         
        public int maxTasks() { return maxTasks; }
        public void setMaxErrors(int n) { maxErrors = Math.max(0, n); }
        public void setMaxSlowCalls(int n) { maxSlowCalls = Math.max(0, n); }
        public void setMaxTasks(int n) { maxTasks = Math.max(0, n); }
    }

    private static final class Handler {
        final Plugin owner; final Object source; final Consumer<Event> fn; final Priority priority; final boolean ignoreCancelled;
        @SuppressWarnings("unchecked")
        Handler(Plugin o, Object src, Consumer<?> f, Priority p, boolean ic) { owner = o; source = src; fn = (Consumer<Event>) f; priority = p; ignoreCancelled = ic; }
    }

    private static final class Cmd {
        final Plugin owner; final String name, description, usage, perm, permMessage; final PluginCommand exec; final Command command;
        Cmd(Plugin o, String n, String d, String u, String perm, String permMessage, PluginCommand e, Command c) {
            owner = o; name = n; description = d; usage = u; this.perm = perm; this.permMessage = permMessage; exec = e; command = c;
        }
    }

    static final class PluginLoader extends URLClassLoader {
        final List<ClassLoader> deps = new ArrayList<>();
        PluginLoader(URL[] urls, ClassLoader parent) { super(urls, parent); }
         
        URL own(String name) { return findResource(name); }
        @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            try { return super.loadClass(name, resolve); }
            catch (ClassNotFoundException e) {
                for (ClassLoader d : deps) try { return d.loadClass(name); } catch (ClassNotFoundException ignored) { }
                throw e;
            }
        }
    }

    private final Server server;
    private final Map<Class<?>, List<Handler>> handlers = new ConcurrentHashMap<>();
    private final Map<String, Cmd> commands = new ConcurrentHashMap<>();
    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private final Map<Integer, AsyncTask> asyncPending = new ConcurrentHashMap<>();
    private final Map<String, Plugin> plugins = new LinkedHashMap<>();
     
    private final Set<Plugin> enabledPlugins = ConcurrentHashMap.newKeySet();
    private final Map<Plugin, URLClassLoader> loaders = new HashMap<>();
    private final Map<String, Path> classPaths = new HashMap<>();                 
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    private final Map<Class<?>, Plugin> serviceOwner = new ConcurrentHashMap<>();
    private final Queue<Runnable> syncQueue = new ConcurrentLinkedQueue<>();
    private final Hud hud = new Hud();
    private final Map<String, PluginDescription> known = new LinkedHashMap<>();    
    private final Permissions permissions = new Permissions();                    
    private final Map<String, Permission> registeredPermissions = new LinkedHashMap<>();
    private final Map<String, Plugin> permissionOwner = new HashMap<>();           
    private final List<PermissionAttachment> attachments = new CopyOnWriteArrayList<>();
    private final Limits limits = new Limits();
     
    private static final Map<Class<?>, Class<?>[]> TYPES = new ConcurrentHashMap<>();
    private ExecutorService pool;
    private Path dir;
    private long now;

    public PluginManager(Server server) { this.server = server; }

    public Limits limits() { return limits; }
    public Collection<Plugin> plugins() { return Collections.unmodifiableCollection(plugins.values()); }
     
    public Collection<Plugin> getPlugins() { return plugins(); }
    public Plugin get(String name) { return name == null ? null : plugins.get(name.toLowerCase(Locale.ROOT)); }
     
    public Plugin getPlugin(String name) { return get(name); }
     
    public boolean isEnabled(Plugin plugin) { return plugin != null && enabledPlugins.contains(plugin); }
     
    public boolean isLoaded(Plugin plugin) { return plugin != null && plugins.containsValue(plugin); }
     
    public boolean isPluginEnabled(Plugin plugin) { return isEnabled(plugin); }
     
    public boolean enablePlugin(Plugin plugin) { return plugin != null && !isEnabled(plugin) && enable(plugin.name()) == null; }
     
    public boolean disablePlugin(Plugin plugin) { return plugin != null && disable(plugin.name()); }

    public void loadAll(Path dir) {
        this.dir = dir;
        readLimits();
        List<PluginDescription> found = new ArrayList<>();
        try {
            Files.createDirectories(dir);
            permissions.load(dir.resolve("permissions.properties"));
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                List<Path> candidates = new ArrayList<>();
                for (Path j : ds) if (isPluginCandidate(j)) candidates.add(j);
                Collections.sort(candidates);
                for (Path j : candidates) { PluginDescription d = describe(j); if (d != null) found.add(d); }
            }
        } catch (IOException e) { Log.warn(Lang.t("plg.dir.read", dir, e)); return; }
        known.clear();
        for (PluginDescription d : found) known.put(d.getName().toLowerCase(Locale.ROOT), d);
        if (found.isEmpty()) return;

        Log.info(Lang.t("plg.loading", found.size()));
         
        List<Plugin> loaded = new ArrayList<>();
        List<PluginDescription> pending = new ArrayList<>(found);
        while (!pending.isEmpty()) {
            PluginDescription pick = null;
            for (PluginDescription d : pending) if (hardReady(d) && softReady(d, pending)) { pick = d; break; }
            if (pick == null) for (PluginDescription d : pending) if (hardReady(d)) { pick = d; break; }
            if (pick == null) break;                                     
            pending.remove(pick);
            Plugin p = instantiate(pick);
            if (p != null) loaded.add(p);
        }
        for (PluginDescription d : pending) Log.warn(Lang.t("plg.nodeps", d.getName(), d.depend));

        for (Plugin p : loaded) {

            String missing = null;
            for (String dep : p.getDescription().getDepend()) {
                if (get(dep) == null) { missing = dep; break; }
            }
            if (missing != null) {
                Log.warn(Lang.t("plg.depfail", p.name(), missing));
                unload(p);
                continue;
            }
            enableOne(p);
        }
    }

    private void readLimits() {
        limits.setMaxErrors(server.getConfigInt("plugin-max-errors", 0));
        limits.setMaxSlowCalls(server.getConfigInt("plugin-max-slow-calls", 0));
        limits.setMaxTasks(server.getConfigInt("plugin-max-tasks", 0));
    }

    private boolean enableOne(Plugin p) {
        try {
            p.onEnable();
            enabledPlugins.add(p);
            Log.info(Lang.t("plg.enabled", p.name(), p.version()));
            for (String cn : p.getDescription().getCommands().keySet())
                if (!isCommandOf(p, cn)) p.getLogger().warning(Lang.t("plg.cmd.unregistered", cn));
            fire(new Events.PluginEnable(p));
            return true;
        } catch (Throwable t) {
            Log.warn(Lang.t("plg.enable.fail", p.name(), t));
            t.printStackTrace();
            unload(p);
            return false;
        }
    }

    private void disableOne(Plugin p) {
        try { fire(new Events.PluginDisable(p)); } catch (Throwable ignored) { }
        try { p.onDisable(); Log.info(Lang.t("plg.disabled", p.name())); }
        catch (Throwable t) { Log.warn(Lang.t("plg.disable.fail", p.name(), t)); }
        unload(p);
    }

    public boolean disable(String name) {
        Plugin p = get(name);
        if (p == null) return false;
        for (PluginDescription d : known.values()) {                        
            Plugin dep = plugins.get(d.getName().toLowerCase(Locale.ROOT));
            if (dep != null && dep != p && d.depend.stream().anyMatch(x -> x.equalsIgnoreCase(p.name()))) disable(dep.name());
        }
        disableOne(p);
        return true;
    }

    public String enable(String name) {
        if (get(name) != null) return Lang.t("plg.err.alreadyon");
        PluginDescription d = known.get(name.toLowerCase(Locale.ROOT));
        if (d == null) return Lang.t("plg.err.nojar");
        for (String dep : d.depend) if (!plugins.containsKey(dep.toLowerCase(Locale.ROOT))) return Lang.t("plg.err.depmissing", dep);
        Plugin p = instantiate(d);
        if (p == null) return Lang.t("plg.err.loadfail");
        return enableOne(p) ? null : Lang.t("plg.err.enablefail");
    }

    public Collection<String> knownNames() {
        List<String> out = new ArrayList<>();
        for (PluginDescription d : known.values()) out.add(d.getName());
        return out;
    }

    public void reload() {
        disableAll();
        if (dir != null) loadAll(dir);
    }

    private boolean hardReady(PluginDescription d) {
        for (String dep : d.depend) if (!plugins.containsKey(dep.toLowerCase(Locale.ROOT))) return false;
        return true;
    }

    private static boolean softReady(PluginDescription d, List<PluginDescription> pending) {
        for (PluginDescription o : pending) {
            if (o == d) continue;
            for (String sd : d.softDepend) if (o.getName().equalsIgnoreCase(sd)) return false;
            if (containsIgnoreCase(o.loadBefore, d.getName())) return false;
        }
        return true;
    }

    private PluginDescription describe(Path jar) {
        if (isSourcePlugin(jar)) return describeSource(jar);
        try (URLClassLoader probe = new URLClassLoader(new URL[]{jar.toUri().toURL()}, null);
             InputStream in = probe.getResourceAsStream("plugin.properties")) {
            if (in == null) { Log.warn(Lang.t("plg.jar.noprops", jar.getFileName())); return null; }
            Properties pr = new Properties();
            pr.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return describeProps(jar, pr, false);
        } catch (Exception e) {
            Log.warn(Lang.t("plg.jar.readfail", jar.getFileName(), e));
            return null;
        }
    }

    private PluginDescription describeProps(Path jar, Properties pr, boolean source) {
        try {
            PluginDescription d = new PluginDescription();
            d.jar = jar;
            d.source = source;
            d.name = pr.getProperty("name", "").trim();
            d.main = pr.getProperty("main", "").trim();
            if (d.name.isEmpty() || d.main.isEmpty()) { Log.warn(Lang.t("plg.jar.needsnamemain", jar.getFileName())); return null; }
            if (!d.name.matches("[A-Za-z0-9_\\-]+")) { Log.warn(Lang.t("plg.jar.badname", jar.getFileName())); return null; }
            d.version = pr.getProperty("version", "1.0").trim();
            d.description = pr.getProperty("description", "").trim();
            d.website = pr.getProperty("website", "").trim();
            String prefix = pr.getProperty("prefix", "").trim();
            d.prefix = prefix.isEmpty() ? d.name : prefix;
            String api = pr.getProperty("api", "").trim();
            d.api = api.isEmpty() ? null : api;
            d.authors.addAll(list(pr.getProperty("author", "")));
            for (String a : list(pr.getProperty("authors", ""))) if (!d.authors.contains(a)) d.authors.add(a);
            d.author = String.join(", ", d.authors);
            d.depend.addAll(list(pr.getProperty("depend", "")));
            d.softDepend.addAll(list(pr.getProperty("softdepend", "")));
            d.loadBefore.addAll(list(pr.getProperty("loadbefore", "")));
            readDeclarations(pr, d);
            if (d.api != null) {
                int res = apiCheck(d.api);
                if (res == API_INVALID) { Log.warn(Lang.t("plg.api.invalid", d.getName(), d.api)); return null; }
                if (res == API_NEWER) { Log.warn(Lang.t("plg.api.mismatch", d.getName(), d.api, API_VERSION)); return null; }
            }
            return d;
        } catch (Exception e) {
            Log.warn(Lang.t("plg.jar.readfail", jar.getFileName(), e));
            return null;
        }
    }

    private static void readDeclarations(Properties pr, PluginDescription d) {
        Map<String, Permission> perms = new LinkedHashMap<>();
        for (String key : new TreeSet<>(pr.stringPropertyNames())) {
            String value = pr.getProperty(key).trim();
            if (key.startsWith("permissions.")) {
                String rest = key.substring("permissions.".length());
                int dot = rest.lastIndexOf('.');
                if (dot <= 0) continue;
                String node = rest.substring(0, dot).trim().toLowerCase(Locale.ROOT);
                if (node.isEmpty()) continue;
                Permission p = perms.computeIfAbsent(node, Permission::new);
                switch (rest.substring(dot + 1).trim()) {
                    case "description": p.setDescription(value); break;
                    case "default": p.setDefault(PermissionDefault.parse(value)); break;
                    case "children":
                        for (String c : list(value)) {
                            if (c.startsWith("-")) p.addChild(c.substring(1), false);
                            else p.addChild(c, true);
                        }
                        break;
                    default: break;
                }
            } else if (key.startsWith("commands.")) {
                String rest = key.substring("commands.".length());
                int dot = rest.lastIndexOf('.');
                if (dot <= 0) continue;
                String cmd = rest.substring(0, dot).trim().toLowerCase(Locale.ROOT).replaceFirst("^/", "");
                if (cmd.isEmpty()) continue;
                PluginDescription.CommandInfo ci = d.commands.computeIfAbsent(cmd, PluginDescription.CommandInfo::new);
                switch (rest.substring(dot + 1).trim()) {
                    case "description": ci.description = value; break;
                    case "usage": ci.usage = value; break;
                    case "aliases": ci.aliases.clear(); ci.aliases.addAll(list(value)); break;
                    case "permission": ci.permission = value; break;
                    case "permission-message": ci.permissionMessage = value; break;
                    default: break;
                }
            }
        }
        d.permissions.addAll(perms.values());
    }

    static final int API_OK = 0, API_NEWER = 1, API_INVALID = 2;

    static int apiCheck(String declared) {
        String[] a = declared.trim().split("\\.", -1), b = API_VERSION.split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            if (i < a.length && a[i].trim().matches("[xX*]")) {
                if (i == 0 || i != a.length - 1) return API_INVALID;    
                return API_OK;
            }
            int x = i < a.length ? part(a[i]) : 0, y = i < b.length ? part(b[i]) : 0;
            if (x < 0 || y < 0) return API_INVALID;
            if (x != y) return x < y ? API_OK : API_NEWER;
        }
        return API_OK;
    }

    private static int part(String s) {
        try { int v = Integer.parseInt(s.trim()); return v >= 0 ? v : -1; }
        catch (NumberFormatException e) { return -1; }
    }

    static boolean apiCompatible(String declared) { return apiCheck(declared) == API_OK; }

    private Plugin instantiate(PluginDescription d) {
        if (plugins.containsKey(d.getName().toLowerCase(Locale.ROOT))) { Log.warn(Lang.t("plg.dup", d.getName(), d.jar.getFileName())); return null; }
        URLClassLoader cl = null;
        try {
            List<URL> urls = new ArrayList<>();
            if (d.source) {
                Path classes = compileSource(d);
                if (classes == null) return null;
                urls.add(classes.toUri().toURL());
                if (Files.isDirectory(d.jar)) urls.add(d.jar.toUri().toURL());    
                classPaths.put(d.getName().toLowerCase(Locale.ROOT), classes);
            } else {
                urls.add(d.jar.toUri().toURL());
                classPaths.put(d.getName().toLowerCase(Locale.ROOT), d.jar);
            }
            PluginLoader pl = new PluginLoader(urls.toArray(new URL[0]), PluginManager.class.getClassLoader());
            cl = pl;
            List<String> need = new ArrayList<>(d.depend); need.addAll(d.softDepend);
            for (String dn : need) { Plugin dp = plugins.get(dn.toLowerCase(Locale.ROOT)); if (dp != null && loaders.get(dp) != null) pl.deps.add(loaders.get(dp)); }
            Class<?> c = Class.forName(d.main, true, cl);
            if (!Plugin.class.isAssignableFrom(c)) throw new IllegalStateException(Lang.t("plg.main.notplugin", d.main));
            Plugin p = (Plugin) c.getDeclaredConstructor().newInstance();
            p.init(this, server, d, d.source && Files.isDirectory(d.jar) ? d.jar : d.jar.resolveSibling(d.getName()), cl);
            plugins.put(d.getName().toLowerCase(Locale.ROOT), p);
            loaders.put(p, cl);
            for (Permission perm : d.permissions) addPermissionOf(p, perm);    
            p.onLoad();
            return p;
        } catch (Throwable t) {
            Log.warn(Lang.t("plg.load.fail", d.getName(), t));
            Plugin bad = plugins.get(d.getName().toLowerCase(Locale.ROOT));
            if (bad != null) unload(bad);
            if (cl != null) try { cl.close(); } catch (IOException ignored) { }
            return null;
        }
    }

    private static boolean isPluginCandidate(Path p) {
        String n = p.getFileName().toString();
        if (n.startsWith(".")) return false;
        if (Files.isDirectory(p)) return Files.isRegularFile(p.resolve("plugin.properties"));
        return n.endsWith(".jar") || n.endsWith(".java");
    }

    private static boolean isSourcePlugin(Path p) {
        return Files.isDirectory(p) || p.getFileName().toString().endsWith(".java");
    }

    private PluginDescription describeSource(Path src) {
        try {
            Properties pr = new Properties();
            if (Files.isDirectory(src)) {
                try (Reader r = Files.newBufferedReader(src.resolve("plugin.properties"), StandardCharsets.UTF_8)) { pr.load(r); }
                if (pr.getProperty("name", "").trim().isEmpty()) pr.setProperty("name", src.getFileName().toString());
            } else {
                String file = src.getFileName().toString();
                String name = file.substring(0, file.length() - ".java".length());
                Path side = src.resolveSibling(name + ".properties");
                if (Files.isRegularFile(side)) try (Reader r = Files.newBufferedReader(side, StandardCharsets.UTF_8)) { pr.load(r); }
                if (pr.getProperty("name", "").trim().isEmpty()) pr.setProperty("name", name);
                if (pr.getProperty("main", "").trim().isEmpty()) {
                    String text = new String(Files.readAllBytes(src), StandardCharsets.UTF_8);
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;").matcher(text);
                    pr.setProperty("main", (m.find() ? m.group(1) + "." : "") + name);
                }
            }
            return describeProps(src, pr, true);
        } catch (Exception e) {
            Log.warn(Lang.t("plg.jar.readfail", src.getFileName(), e));
            return null;
        }
    }

    private static List<Path> sourceFiles(Path src) throws IOException {
        List<Path> out = new ArrayList<>();
        if (!Files.isDirectory(src)) { out.add(src); return out; }
        try (java.util.stream.Stream<Path> st = Files.walk(src)) {
            st.filter(f -> Files.isRegularFile(f) && f.getFileName().toString().endsWith(".java")).sorted().forEach(out::add);
        }
        return out;
    }

    private Path compileSource(PluginDescription d) {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) { Log.warn(Lang.t("plg.src.nojdk", d.getName())); return null; }
        try {
            List<Path> files = sourceFiles(d.jar);
            if (files.isEmpty()) { Log.warn(Lang.t("plg.src.nofiles", d.getName())); return null; }
            StringBuilder cp = new StringBuilder(System.getProperty("java.class.path", ""));
            List<String> need = new ArrayList<>(d.depend); need.addAll(d.softDepend);
            for (String dn : need) {
                Path dp = classPaths.get(dn.toLowerCase(Locale.ROOT));
                if (dp != null) cp.append(java.io.File.pathSeparator).append(dp.toAbsolutePath());
            }
             
            StringBuilder fp = new StringBuilder(cp).append('|').append(System.getProperty("java.version"));
            for (Path f : files) fp.append('|').append(d.jar.toAbsolutePath().relativize(f.toAbsolutePath())).append(':').append(Files.getLastModifiedTime(f).toMillis()).append(':').append(Files.size(f));
            String print = Integer.toHexString(fp.toString().hashCode()) + "-" + fp.length();
            Path base = dir.resolve(".cache").resolve(d.getName());
            Path classes = base.resolve("classes"), stamp = base.resolve("fingerprint");
            if (Files.isDirectory(classes) && Files.isRegularFile(stamp)
                && print.equals(new String(Files.readAllBytes(stamp), StandardCharsets.UTF_8).trim())) return classes;

            Log.info(Lang.t("plg.src.compiling", d.getName(), files.size()));
            deleteTree(classes);
            Files.createDirectories(classes);
            javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diag = new javax.tools.DiagnosticCollector<>();
            try (javax.tools.StandardJavaFileManager fm = jc.getStandardFileManager(diag, null, StandardCharsets.UTF_8)) {
                List<String> opts = Arrays.asList("-encoding", "UTF-8", "-proc:none", "-Xlint:-options", "-cp", cp.toString(), "-d", classes.toString());
                boolean ok = jc.getTask(null, fm, diag, opts, null, fm.getJavaFileObjectsFromPaths(files)).call();
                if (!ok) {
                    Log.warn(Lang.t("plg.src.failed", d.getName()));
                    int shown = 0;
                    for (javax.tools.Diagnostic<? extends javax.tools.JavaFileObject> x : diag.getDiagnostics()) {
                        if (x.getKind() != javax.tools.Diagnostic.Kind.ERROR) continue;
                        if (shown++ >= 20) { Log.warn("  ..."); break; }
                        String where = x.getSource() == null ? "" : Paths.get(x.getSource().toUri()).getFileName() + ":" + x.getLineNumber() + ": ";
                        Log.warn("  " + where + x.getMessage(Locale.ENGLISH));
                    }
                    deleteTree(classes);
                    return null;
                }
            }
            Files.write(stamp, print.getBytes(StandardCharsets.UTF_8));
            return classes;
        } catch (Throwable t) {
            Log.warn(Lang.t("plg.load.fail", d.getName(), t));
            return null;
        }
    }

    private static void deleteTree(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (java.util.stream.Stream<Path> st = Files.walk(p)) {
            List<Path> all = new ArrayList<>();
            st.forEach(all::add);
            Collections.reverse(all);
            for (Path x : all) Files.deleteIfExists(x);
        }
    }

    public void disableAll() {
        List<Plugin> list = new ArrayList<>(plugins.values());
        Collections.reverse(list);
        for (Plugin p : list) disableOne(p);
        if (pool != null) { pool.shutdownNow(); pool = null; }
        syncQueue.clear();
    }

    private void unload(Plugin p) {
        enabledPlugins.remove(p);
        for (List<Handler> l : handlers.values()) l.removeIf(h -> h.owner == p);
        commands.values().removeIf(c -> c.owner == p);
        for (Task t : tasks) if (t.owner == p) t.cancel();
        tasks.removeIf(t -> t.owner == p);
        asyncPending.values().removeIf(a -> {
            boolean mine = a.owner == p;
            if (mine) a.cancelRun();
            return mine;
        });
        hud.removeAll(p);
        for (Iterator<Map.Entry<Class<?>, Plugin>> it = serviceOwner.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Class<?>, Plugin> e = it.next();
            if (e.getValue() == p) { services.remove(e.getKey()); it.remove(); }
        }
        attachments.removeIf(a -> a.getPlugin() == p);
        registeredPermissions.keySet().removeIf(k -> permissionOwner.get(k) == p);
        permissionOwner.values().removeIf(v -> v == p);
        plugins.remove(p.name().toLowerCase(Locale.ROOT));
        URLClassLoader cl = loaders.remove(p);
        if (cl != null) try { cl.close(); } catch (IOException ignored) { }
        p.releaseFeatures();                        
    }

    <T> void addHandler(Plugin owner, Class<T> type, Priority pr, boolean ignoreCancelled, Consumer<T> fn) {
        register(owner, null, type, pr, ignoreCancelled, fn);
    }

    <T> void addHandler(Plugin owner, Object source, Class<T> type, Priority pr, boolean ignoreCancelled, Consumer<T> fn) {
        register(owner, source, type, pr, ignoreCancelled, fn);
    }

    private void register(Plugin owner, Object source, Class<?> type, Priority pr, boolean ignoreCancelled, Consumer<?> fn) {
        if (!isEventType(type)) throw new IllegalArgumentException("not an event type: " + type);
        List<Handler> l = handlers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>());
        Handler h = new Handler(owner, source, fn, pr, ignoreCancelled);
        int i = l.size();
        while (i > 0 && l.get(i - 1).priority.ordinal() > pr.ordinal()) i--;       
        l.add(i, h);
    }

    int removeHandlers(Plugin owner, Object source) {
        int[] removed = new int[1];
        for (List<Handler> l : handlers.values()) l.removeIf(h -> {
            boolean mine = h.owner == owner && (source == null || h.source == source);
            if (mine) removed[0]++;
            return mine;
        });
        return removed[0];
    }

    public boolean hasHandlers(Class<?> type) {
        for (Class<?> c : typesOf(type)) {
            List<Handler> l = handlers.get(c);
            if (l != null && !l.isEmpty()) return true;
        }
        return false;
    }

    public <T extends Event> T fire(T e) {
        List<Handler> all = null; int lists = 0;
        for (Class<?> c : typesOf(e.getClass())) {
            List<Handler> l = handlers.get(c);
            if (l == null || l.isEmpty()) continue;
            lists++;
            if (all == null) all = l; else { if (lists == 2) all = new ArrayList<>(all); all.addAll(l); }
        }
        if (all == null) return e;
        if (lists > 1) all.sort(Comparator.comparingInt(h -> h.priority.ordinal()));
        Plugin tooBad = null;
        for (Handler h : all) {
            if (h.ignoreCancelled && e.isCancelled()) continue;
            long t0 = System.nanoTime();
            try { h.fn.accept(e); }
            catch (Throwable t) { Log.warn(Lang.t("plg.event.fail", h.owner.name(), e.getClass().getSimpleName(), t)); t.printStackTrace(); if (h.owner.noteError()) tooBad = h.owner; }
            if (System.nanoTime() - t0 > Plugin.SLOW_NANOS && h.owner.noteSlow()) tooBad = h.owner;
        }
        if (tooBad != null) {
            Log.warn(Lang.t("plg.limits.exceeded", tooBad.name(), tooBad.errorCount(), tooBad.slowCallCount()));
            disable(tooBad.name());
        }
        return e;
    }

    public <T extends Event> T callEvent(T event) { return fire(event); }

    public int registerEvents(Listener listener, Plugin plugin) { return registerListener(plugin, listener); }

    public int unregisterEvents(Listener listener, Plugin plugin) { return listener == null || plugin == null ? 0 : removeHandlers(plugin, listener); }

    int registerListener(Plugin owner, Object listener) {
        if (listener == null) throw new IllegalArgumentException("registerEvents: the listener is null");
        if (owner == null) throw new IllegalArgumentException("registerEvents: the plugin is missing");
        int n = 0;
        for (Class<?> c = listener.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                EventHandler eh = m.getAnnotation(EventHandler.class);
                if (eh == null) continue;
                if (m.getParameterCount() != 1 || !isEventType(m.getParameterTypes()[0])) {
                    owner.getLogger().warning(Lang.t("plg.handler.badparam", m.getName()));
                    continue;
                }
                m.setAccessible(true);
                final java.lang.reflect.Method fm = m;
                register(owner, listener, m.getParameterTypes()[0], eh.priority(), eh.ignoreCancelled(), (Consumer<Event>) ev -> {
                    try { fm.invoke(listener, ev); }
                    catch (java.lang.reflect.InvocationTargetException ex) { throw ex.getCause() instanceof RuntimeException ? (RuntimeException) ex.getCause() : new RuntimeException(ex.getCause()); }
                    catch (IllegalAccessException ex) { throw new RuntimeException(ex); }
                });
                n++;
            }
        }
        return n;
    }

    static boolean isEventType(Class<?> c) { return c != null && (Event.class.isAssignableFrom(c) || c.isInterface()); }

    static Class<?>[] typesOf(Class<?> c) { return TYPES.computeIfAbsent(c, PluginManager::computeTypes); }

    private static Class<?>[] computeTypes(Class<?> c) {
        LinkedHashSet<Class<?>> out = new LinkedHashSet<>();
        for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) out.add(k);
        Deque<Class<?>> pending = new ArrayDeque<>(out);
        while (!pending.isEmpty()) {
            for (Class<?> i : pending.poll().getInterfaces()) if (out.add(i)) pending.add(i);
        }
        return out.toArray(new Class<?>[0]);
    }

    public boolean hasPermission(Commands.Sender s, String node) { return checkPermission(s, node); }

    public boolean hasPermission(CommandSender s, String node) { return checkPermission(s, node); }

    private boolean checkPermission(CommandSender s, String node) {
        if (node == null || node.isEmpty()) return true;
        if (s.isOp()) return true;
        return permit(s.name(), node, 0);
    }

    private boolean permit(String player, String node, int depth) {
        Boolean v = explicit(player, node, depth);
        if (v != null) return v;
        Permission reg = registeredPermissions.get(node.toLowerCase(Locale.ROOT));
        if (reg == null) return false;
        return reg.getDefault() == PermissionDefault.TRUE || reg.getDefault() == PermissionDefault.NOT_OP;    
    }

    private Boolean explicit(String player, String node, int depth) {
        if (depth > 8) return null;
        String n = node.toLowerCase(Locale.ROOT);
        Boolean r = null;
        for (PermissionAttachment a : attachments) {
            if (!a.getPlayer().equalsIgnoreCase(player)) continue;
            Boolean v = matchEntries(a.entries(), n);
            if (v != null) r = v;                                    
        }
        if (r != null) return r;
        r = permissions.lookup(player, n);
        if (r != null) return r;
        for (Permission parent : registeredPermissions.values()) {
            if (parent.getChildren().isEmpty()) continue;
            Boolean child = matchEntries(parent.getChildren(), n);
            if (child != null && permit(player, parent.getName(), depth + 1)) return child;
        }
        return null;
    }

    static Boolean matchEntries(Map<String, Boolean> entries, String node) {
        Boolean exact = entries.get(node);
        if (exact != null) return exact;
        Boolean r = null;
        for (Map.Entry<String, Boolean> e : entries.entrySet()) {
            String pat = e.getKey();
            if (!pat.endsWith("*")) continue;
            if (pat.equals("*") || (pat.endsWith(".*") && node.startsWith(pat.substring(0, pat.length() - 1)))) r = e.getValue();
        }
        return r;
    }

    public void addPermission(Permission p) { addPermissionOf(null, p); }

    public void removePermission(String node) {
        String k = node.toLowerCase(Locale.ROOT);
        registeredPermissions.remove(k);
        permissionOwner.remove(k);
    }

    public Permission getPermission(String node) { return node == null ? null : registeredPermissions.get(node.toLowerCase(Locale.ROOT)); }

    public Collection<Permission> getPermissions() { return Collections.unmodifiableCollection(registeredPermissions.values()); }

    void addPermissionOf(Plugin owner, Permission p) {
        String k = p.getName();
        Plugin old = permissionOwner.get(k);
        if (registeredPermissions.containsKey(k) && old != owner && old != null)
            Log.warn(Lang.t("plg.perm.replace", owner == null ? Lang.t("plg.owner.server") : owner.name(), k, old.name()));
        registeredPermissions.put(k, p);
        permissionOwner.put(k, owner);
    }

    PermissionAttachment addAttachment(Plugin owner, String player) {
        PermissionAttachment a = new PermissionAttachment(this, owner, player);
        attachments.add(a);
        return a;
    }

    void removeAttachment(PermissionAttachment a) { attachments.remove(a); }

    public void clearAttachments(String player) { attachments.removeIf(a -> a.getPlayer().equalsIgnoreCase(player)); }

    private static String norm(String name) { return name.toLowerCase(Locale.ROOT).replaceFirst("^/", ""); }

    void addCommand(Plugin owner, String name, String desc, String usage, String perm, PluginCommand exec) {
        String n = norm(name);
        PluginDescription.CommandInfo info = declared(owner, n);
        if (info != null) {                                           
            if (desc == null || desc.isEmpty()) desc = info.getDescription();
            if (usage == null || usage.isEmpty()) usage = info.getUsage();
            if (perm == null || perm.isEmpty()) perm = info.getPermission();
        }
        put(owner, n, desc, usage, perm, info == null ? null : info.getPermissionMessage(), exec, null);
        if (info != null && !info.getAliases().isEmpty()) addAliases(owner, n, info.getAliases());
    }

    void addCommandObject(Plugin owner, Command c) {
        String n = c.getName();
        PluginDescription.CommandInfo info = declared(owner, n);
        if (info != null) {
            if (c.getDescription().isEmpty()) c.setDescription(info.getDescription());
            if (c.getUsage().isEmpty()) c.setUsage(info.getUsage());
            if (c.getPermission().isEmpty()) c.setPermission(info.getPermission());
            if (c.getPermissionMessage() == null && !info.getPermissionMessage().isEmpty()) c.setPermissionMessage(info.getPermissionMessage());
        }
        c.owner = owner;
        PluginCommand exec = (s, a) -> {
            if (!c.execute(s, n, a)) s.send(Lang.t("cmd.usage") + "/" + n + (c.getUsage().isEmpty() ? "" : " " + c.getUsage()));
        };
        put(owner, n, c.getDescription(), c.getUsage(), c.getPermission(), c.getPermissionMessage(), exec, c);
        if (!c.getAliases().isEmpty()) addAliases(owner, n, c.getAliases());
    }

    public void registerCommand(Plugin owner, Command c) { addCommandObject(owner, c); }

    private void put(Plugin owner, String n, String desc, String usage, String perm, String permMessage, PluginCommand exec, Command obj) {
        Cmd old = commands.get(n);
        if (old != null && old.owner != owner) Log.warn(Lang.t("plg.cmd.replace", nameOf(owner), n, nameOf(old.owner)));
        commands.put(n, new Cmd(owner, n, desc == null ? "" : desc, usage == null ? "" : usage, perm == null ? "" : perm,
            permMessage == null || permMessage.isEmpty() ? null : permMessage, exec, obj));
    }

    private static PluginDescription.CommandInfo declared(Plugin owner, String n) {
        return owner == null ? null : owner.getDescription().getCommands().get(n);
    }

    boolean isCommandOf(Plugin owner, String name) {
        Cmd c = commands.get(norm(name));
        return c != null && c.owner == owner;
    }

    boolean removeCommand(Plugin owner, String name) {
        Cmd c = commands.get(norm(name));
        if (c == null || c.owner != owner) return false;
        commands.entrySet().removeIf(e -> e.getValue() == c);
        return true;
    }

    public boolean unregisterCommand(String name) {
        Cmd c = commands.get(norm(name));
        if (c == null) return false;
        commands.entrySet().removeIf(e -> e.getValue() == c);
        return true;
    }

    Command commandOf(Plugin owner, String name) {
        Cmd c = commands.get(norm(name));
        if (c == null || c.owner != owner) return null;
        return wrapped(c);
    }

    public Command findCommand(String name) {
        Cmd c = commands.get(norm(name));
        return c == null ? null : wrapped(c);
    }

    public Map<String, Command> commandsSnapshot() {
        Map<String, Command> out = new TreeMap<>();
        for (Map.Entry<String, Cmd> e : commands.entrySet()) out.put(e.getKey(), wrapped(e.getValue()));
        return out;
    }

    private Command wrapped(Cmd c) {
        if (c.command != null) return c.command;
        Command w = new Command(c.name, c.description, c.usage) {
            @Override public boolean execute(CommandSender s, String label, String[] args) {
                if (!(s instanceof Commands.Sender)) return false;
                c.exec.run((Commands.Sender) s, args);
                return true;
            }
        };
        w.owner = c.owner;
        w.setPermission(c.perm);
        w.setPermissionMessage(c.permMessage);
        return w;
    }

    void addAliases(Plugin owner, String name, String[] aliases) { addAliases(owner, name, Arrays.asList(aliases)); }

    void addAliases(Plugin owner, String name, Collection<String> aliases) {
        Cmd c = commands.get(norm(name));
        if (c == null || c.owner != owner) throw new IllegalArgumentException("alias: /" + name + " is not a command of " + nameOf(owner));
        for (String a : aliases) {
            String n = norm(a);
            if (n.isEmpty()) continue;
            Cmd old = commands.get(n);
            if (old != null && old.owner != owner) Log.warn(Lang.t("plg.cmd.replace", nameOf(owner), n, nameOf(old.owner)));
            commands.put(n, c);
        }
    }

    public boolean dispatch(Commands.Sender s, String cmd, String[] args) {
        Cmd c = commands.get(cmd);
        if (c == null) return false;
        boolean ok = c.perm.isEmpty() || (c.perm.equals("op") ? s.isOp() : hasPermission(s, c.perm));
        if (!ok) { s.send(c.permMessage != null ? c.permMessage : Lang.t("cmd.noperm")); return true; }
        try { c.exec.run(s, args); }
        catch (Throwable t) { s.send(Lang.t("plg.cmd.error", c.name, nameOf(c.owner))); Log.warn(Lang.t("plg.cmd.fail", nameOf(c.owner), c.name, t)); t.printStackTrace(); }
        return true;
    }

    public List<String> helpLines() {
        List<String> out = new ArrayList<>();
        List<Cmd> l = new ArrayList<>(new LinkedHashSet<>(commands.values()));    
        l.sort(Comparator.comparing(c -> c.name));
        for (Cmd c : l) out.add("/" + c.name + (c.usage.isEmpty() ? "" : " " + c.usage) + (c.description.isEmpty() ? "" : " - " + c.description));
        return out;
    }

    <T> void addService(Plugin owner, Class<T> type, T impl) { services.put(type, impl); serviceOwner.put(type, owner); }
    @SuppressWarnings("unchecked") public <T> T service(Class<T> type) { return (T) services.get(type); }

    Hud hud() { return hud; }

    public void title(Player p, String title, String subtitle, long stayTicks) { hud.title(p, title, subtitle, stayTicks); }

    public void actionBar(Player p, String text, long ticks) { hud.actionBar(p, text, ticks); }

    public Task schedule(Plugin owner, Runnable body, long delay, long period) {
        long max = limits.maxTasks();
        if (max > 0 && tasks.stream().filter(t -> t.owner == owner).count() >= max)
            throw new IllegalStateException("too many pending tasks (limit " + max + ")");
        Task t = new Task(owner, body, now + Math.max(1, delay), period);
        tasks.add(t);
        return t;
    }

    public void cancelTasks(Plugin owner) { for (Task t : tasks) if (t.owner == owner) t.cancel(); tasks.removeIf(t -> t.owner == owner); }

    public void cancelAllTasks() {
        for (Task t : tasks) t.cancel();
        tasks.clear();
        for (AsyncTask a : asyncPending.values()) a.cancelRun();
        asyncPending.clear();
    }

    public boolean cancelTask(Plugin owner, int id) {
        for (Task t : tasks) if (t.id() == id && t.owner == owner && !t.isCancelled()) { t.cancel(); return true; }
        AsyncTask a = asyncPending.get(id);
        if (a != null && a.owner == owner && !a.isCancelled()) { a.cancelRun(); return true; }
        return false;
    }

    public boolean cancelTaskById(int id) {
        for (Task t : tasks) if (t.id() == id && !t.isCancelled()) { t.cancel(); return true; }
        AsyncTask a = asyncPending.get(id);
        if (a != null && !a.isCancelled()) { a.cancelRun(); return true; }
        return false;
    }

    public Task getTask(Plugin owner, int id) {
        for (Task t : tasks) if (t.id() == id && t.owner == owner && !t.isCancelled()) return t;
        return null;
    }

    public boolean isQueued(Plugin owner, int id) {
        if (getTask(owner, id) != null) return true;
        AsyncTask a = asyncPending.get(id);
        return a != null && a.owner == owner && !a.isCancelled();
    }

    public boolean isTaskQueued(int id) {
        for (Task t : tasks) if (t.id() == id && !t.isCancelled()) return true;
        AsyncTask a = asyncPending.get(id);
        return a != null && !a.isCancelled();
    }

    public int pendingAsyncTasks() { return asyncPending.size(); }

    public long currentTick() { return now; }

    public void runSync(Plugin owner, Runnable body) {
        syncQueue.add(() -> {
            if (!alive(owner)) return;
            try { body.run(); } catch (Throwable ex) { Log.warn(Lang.t("plg.task.runsync", nameOf(owner), ex)); ex.printStackTrace(); }
        });
    }

    private ExecutorService executor() {
        if (pool == null) pool = Executors.newCachedThreadPool(r -> { Thread t = new Thread(r, "plugin-async"); t.setDaemon(true); return t; });
        return pool;
    }

    public void async(Plugin owner, Runnable job) {
        executor().execute(() -> {
            try { job.run(); } catch (Throwable ex) { Log.warn(Lang.t("plg.task.async", nameOf(owner), ex)); ex.printStackTrace(); }
        });
    }

    public <T> void async(Plugin owner, Supplier<T> work, Consumer<T> done) {
        async(owner, () -> {
            T v = work.get();
            syncQueue.add(() -> {
                if (!alive(owner)) return;       
                try { done.accept(v); } catch (Throwable ex) { Log.warn(Lang.t("plg.task.callback", nameOf(owner), ex)); ex.printStackTrace(); }
            });
        });
    }

    public int runAsyncTask(Plugin owner, AsyncTask task) {
        task.owner = owner;
        asyncPending.put(task.getTaskId(), task);
        executor().execute(() -> {
            if (!task.isCancelled()) {
                try { task.onRun(); }
                catch (Throwable ex) { task.markCrashed(); Log.warn(Lang.t("plg.task.asynctask", nameOf(owner), ex)); ex.printStackTrace(); }
            }
            syncQueue.add(() -> {
                asyncPending.remove(task.getTaskId());
                if (!alive(owner) || task.isCancelled()) return;
                try { task.onCompletion(); }
                catch (Throwable ex) { Log.warn(Lang.t("plg.task.completion", nameOf(owner), ex)); ex.printStackTrace(); }
            });
        });
        return task.getTaskId();
    }

    public void tick(long tick) {
        now = tick;
        for (Runnable r; (r = syncQueue.poll()) != null; ) r.run();
        for (Task t : tasks) {
            if (t.isCancelled()) { tasks.remove(t); continue; }
            if (t.next > tick) continue;
            if (t.period > 0) t.next = tick + t.period; else tasks.remove(t);
            try { t.body.run(); }
            catch (Throwable ex) { Log.warn(Lang.t("plg.task.fail", nameOf(t.owner), ex)); ex.printStackTrace(); }
        }
        if (tick % Hud.REFRESH == 0) { try { hud.render(server, this, tick); } catch (Throwable ex) { Log.warn(Lang.t("plg.hud.fail", ex)); } }
        if (hasHandlers(Events.ServerTick.class)) fire(new Events.ServerTick(tick));
    }

    private boolean alive(Plugin owner) { return owner == null || plugins.containsValue(owner); }

    private static String nameOf(Plugin owner) { return owner == null ? Lang.t("plg.owner.server") : owner.name(); }

    private static List<String> list(String csv) {
        List<String> out = new ArrayList<>();
        for (String s : csv.split(",")) if (!s.trim().isEmpty()) out.add(s.trim());
        return out;
    }

    private static boolean containsIgnoreCase(List<String> l, String s) {
        for (String x : l) if (x.equalsIgnoreCase(s)) return true;
        return false;
    }
}
