package net.BlockNes.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PluginDescription {

    public static final class CommandInfo {
        final String name;
        String description = "", usage = "", permission = "", permissionMessage = "";
        final List<String> aliases = new ArrayList<>();

        CommandInfo(String name) { this.name = name; }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getUsage() { return usage; }
        public String getPermission() { return permission; }
        public String getPermissionMessage() { return permissionMessage; }
        public List<String> getAliases() { return Collections.unmodifiableList(aliases); }
    }

    Path jar;
     
    boolean source;
    String name, version = "1.0", main, description = "", website = "", prefix, api, author = "";
    final List<String> authors = new ArrayList<>(), depend = new ArrayList<>(), softDepend = new ArrayList<>(), loadBefore = new ArrayList<>();
    final List<Permission> permissions = new ArrayList<>();
    final Map<String, CommandInfo> commands = new LinkedHashMap<>();

    public String getName() { return name; }
     
    public String getFullName() { return name + " v" + version; }
    public String getVersion() { return version; }
    public String getMain() { return main; }
     
    public String getAuthor() { return author; }
    public List<String> getAuthors() { return Collections.unmodifiableList(authors); }
    public String getDescription() { return description; }
    public String getWebsite() { return website; }
     
    public String getPrefix() { return prefix == null ? name : prefix; }
     
    public String getApi() { return api; }
    public List<String> getDepend() { return Collections.unmodifiableList(depend); }
    public List<String> getSoftDepend() { return Collections.unmodifiableList(softDepend); }
    public List<String> getLoadBefore() { return Collections.unmodifiableList(loadBefore); }
     
    public List<Permission> getPermissions() { return Collections.unmodifiableList(permissions); }
     
    public Map<String, CommandInfo> getCommands() { return Collections.unmodifiableMap(commands); }
}
