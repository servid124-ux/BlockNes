package net.BlockNes.plugin;

public abstract class AsyncTask {
    Plugin owner;                        
    private final int taskId = Task.nextId();
    private volatile Object result;
    private volatile boolean hasResult, cancelled, crashed;

    public abstract void onRun();

    public void onCompletion() { }

    public final void setResult(Object value) { result = value; hasResult = true; }
    public final Object getResult() { return result; }
    public final boolean hasResult() { return hasResult; }

    public final int getTaskId() { return taskId; }

    public final void cancelRun() { cancelled = true; }
    public final boolean isCancelled() { return cancelled; }

    public final boolean isCrashed() { return crashed; }
    void markCrashed() { crashed = true; }
}
