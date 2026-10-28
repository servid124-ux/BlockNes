package net.BlockNes.event;

public abstract class Event {
     
    public String getEventName() { return getClass().getSimpleName(); }
}
