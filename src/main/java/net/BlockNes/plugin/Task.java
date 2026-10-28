package net.BlockNes.plugin;

import net.BlockNes.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

public final class Task {
    private static final AtomicInteger IDS = new AtomicInteger();
     
    static int nextId() { return IDS.incrementAndGet(); }

    private final int id = nextId();
    final Plugin owner;      
    final Runnable body;
    final long period;       
    long next;
    private volatile boolean cancelled;
    private volatile Runnable cancelHook;

    Task(Plugin owner, Runnable body, long next, long period) { this.owner = owner; this.body = body; this.next = next; this.period = period; }

    public int id() { return id; }
     
    public int getTaskId() { return id; }
    public boolean isRepeating() { return period > 0; }
     
    public long getPeriod() { return period; }
     
    public long getNextRun() { return next; }
     
    public Plugin owner() { return owner; }
    public Plugin getOwner() { return owner; }

    public void cancel() {
        if (cancelled) return;
        cancelled = true;
        Runnable hook = cancelHook;
        cancelHook = null;
        if (hook != null) {
            try { hook.run(); }
            catch (RuntimeException e) { Log.warn("error while cancelling a task: " + e); e.printStackTrace(); }
        }
    }
    public boolean isCancelled() { return cancelled; }

    public void setCancelHook(Runnable hook) { cancelHook = hook; }
}
