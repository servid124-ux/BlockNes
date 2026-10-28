package net.BlockNes.plugin;

import net.BlockNes.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class PluginLogger {
    private final String prefix;

    public PluginLogger(String prefix) { this.prefix = prefix; }

    public void info(String msg) { Log.plugin(prefix, msg); }
    public void notice(String msg) { info(msg); }
    public void warning(String msg) { Log.warn("[" + prefix + "] " + msg); }
    public void error(String msg) { Log.error("[" + prefix + "] " + msg); }
    public void critical(String msg) { error(msg); }
    public void alert(String msg) { error(msg); }
    public void emergency(String msg) { error(msg); }
     
    public void debug(String msg) { if (Log.debugEnabled()) Log.debug("[" + prefix + "] " + msg); }

    public void logException(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        error(sw.toString().trim());
    }
}
