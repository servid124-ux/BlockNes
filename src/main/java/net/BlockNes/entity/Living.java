package net.BlockNes.entity;

import net.BlockNes.mob.Mob;

public abstract class Living extends Entity implements Damageable {
    protected Living(Object handle) { super(handle); }

    public float getHealth() { return handle instanceof Mob ? ((Mob) handle).health : 0f; }

    public int getMaxHealth() { return handle instanceof Mob ? ((Mob) handle).type.health : 0; }

    public boolean isAlive() { return handle instanceof Mob && !((Mob) handle).dead && ((Mob) handle).health > 0; }
}
