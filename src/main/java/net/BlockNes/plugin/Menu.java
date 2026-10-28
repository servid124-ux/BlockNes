package net.BlockNes.plugin;

import net.BlockNes.Player;
import net.BlockNes.Server;
import net.BlockNes.item.Item;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.BlockNes.network.protocolo.ContainerSetContentPacket;
import net.BlockNes.network.protocolo.ContainerSetSlotPacket;

public final class Menu {
    private final Plugin owner;
    private final Server server;
    private final int rows, size;
    private final Item[] items;
    private final Map<Integer, Consumer<Events.MenuClick>> handlers = new HashMap<>();
    private final Set<Player> viewers = new LinkedHashSet<>();
    private Consumer<Player> onClose;

    Menu(Plugin owner, Server server, int rows) {
        if (rows != 3 && rows != 6) throw new IllegalArgumentException("un menu tiene 3 o 6 filas (recibido " + rows + ")");
        this.owner = owner;
        this.server = server;
        this.rows = rows;
        this.size = rows * 9;
        this.items = new Item[size];
        Arrays.fill(items, Item.AIR);
    }

    public int rows() { return rows; }

    public int size() { return size; }

    public Plugin owner() { return owner; }

    public Menu setItem(int slot, Item item) { return setItem(slot, item, null); }

    public Menu setItem(int slot, Item item, Consumer<Events.MenuClick> onClick) {
        checkSlot(slot);
        items[slot] = item == null || item.isAir() ? Item.AIR : item;
        if (onClick != null) handlers.put(slot, onClick);
        else handlers.remove(slot);
        for (Player v : viewers) server.send(v, ContainerSetSlotPacket.containerSlot(v.windowId, slot, items[slot]));
        return this;
    }

    public Item getItem(int slot) {
        checkSlot(slot);
        return items[slot];
    }

    public Menu fill(Item item) {
        for (int i = 0; i < size; i++) setItem(i, item);
        return this;
    }

    public Menu border(Item item) {
        for (int i = 0; i < size; i++) {
            int row = i / 9, col = i % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) setItem(i, item);
        }
        return this;
    }

    public Menu clear() { return fill(Item.AIR); }

    public Menu onClose(Consumer<Player> handler) {
        this.onClose = handler;
        return this;
    }

    public Set<Player> viewers() { return Collections.unmodifiableSet(new LinkedHashSet<>(viewers)); }

    public Menu refresh() {
        for (Player v : viewers) server.send(v, ContainerSetContentPacket.containerContent(v.windowId, items.clone()));
        return this;
    }

    public Item[] snapshot() { return items.clone(); }

    public void addViewer(Player p) {
        viewers.add(p);
        server.menuOpened(this);
    }

    public void notifyClosed(Player p) {
        viewers.remove(p);
        if (viewers.isEmpty()) server.menuClosed(this);
        if (onClose != null) {
            try { onClose.accept(p); }
            catch (Throwable t) { fail(t); }
        }
    }

    public void clientChanged(Player p, int slot, Item proposed) {
        if (slot >= 0 && slot < size && proposed != null && !proposed.equals(items[slot])) {
            Events.MenuClick ev = new Events.MenuClick(p, this, slot, items[slot], proposed);
            server.plugins().fire(ev);
            Consumer<Events.MenuClick> h = handlers.get(slot);
            if (h != null && !ev.isCancelled()) {
                try { h.accept(ev); }
                catch (Throwable t) { fail(t); }
            }
        }
         
        if (p.windowMenu == this && p.windowId != -1) server.send(p, ContainerSetContentPacket.containerContent(p.windowId, items.clone()));
        server.send(p, p.inv.contentPacket());
    }

    private void fail(Throwable t) {
        owner.getLogger().logException(t);
        if (owner.noteError()) server.plugins().disable(owner.name());
    }

    private void checkSlot(int slot) {
        if (slot < 0 || slot >= size) throw new IndexOutOfBoundsException("casilla " + slot + " fuera del menu (0.." + (size - 1) + ")");
    }
}
