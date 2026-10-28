package net.BlockNes.plugin;

import net.BlockNes.Player;
import net.BlockNes.command.CommandSender;
import net.BlockNes.command.Commands;
import net.BlockNes.item.Item;
import net.BlockNes.level.Position;
import net.BlockNes.math.Vector3;
import net.BlockNes.mob.Mob;

import java.util.ArrayList;
import java.util.List;
import net.BlockNes.network.protocolo.Paquete;

public final class Events {
    private Events() {}

    public abstract static class PlayerEvent extends Event implements net.BlockNes.event.player.PlayerEvent {
        public final Player player;
        protected PlayerEvent(Player p) { player = p; }
        @Override public Player getPlayer() { return player; }
    }

    public static final class PlayerJoin extends PlayerEvent implements net.BlockNes.event.player.PlayerJoinEvent {
        public PlayerJoin(Player p) { super(p); }
    }

    public static final class PlayerQuit extends PlayerEvent implements net.BlockNes.event.player.PlayerQuitEvent {
        public PlayerQuit(Player p) { super(p); }
    }

    public static final class PlayerChat extends PlayerEvent implements net.BlockNes.event.player.PlayerChatEvent {
        public String message;
        public String format;
        public final List<Player> recipients = new ArrayList<>();
        public PlayerChat(Player p, String m) { super(p); message = m; }
        @Override public String getMessage() { return message; }
        @Override public void setMessage(String m) { message = m; }
        @Override public String getFormat() { return format; }
        @Override public void setFormat(String f) { format = f; }
        @Override public List<Player> getRecipients() { return recipients; }
    }

    public static final class PlayerCommand extends PlayerEvent implements net.BlockNes.event.player.PlayerCommandPreprocessEvent {
        public final String line;
        public PlayerCommand(Player p, String l) { super(p); line = l; }
        @Override public String getMessage() { return line; }
    }

    public static final class PlayerMove extends PlayerEvent implements net.BlockNes.event.player.PlayerMoveEvent {
        public final double fromX, fromY, fromZ, toX, toY, toZ;
        public PlayerMove(Player p, double fx, double fy, double fz, double tx, double ty, double tz) {
            super(p); fromX = fx; fromY = fy; fromZ = fz; toX = tx; toY = ty; toZ = tz;
        }
        @Override public Position getFrom() { return new Position(fromX, fromY, fromZ, player.getLevel()); }
        @Override public Position getTo() { return new Position(toX, toY, toZ, player.getLevel()); }
    }

    public static final class PlayerInteract extends PlayerEvent implements net.BlockNes.event.player.PlayerInteractEvent, net.BlockNes.event.block.BlockInteractEvent {
        public final boolean air;
        public final int x, y, z, face;
        public final Item item;
        public PlayerInteract(Player p, boolean air, int x, int y, int z, int face, Item item) { super(p); this.air = air; this.x = x; this.y = y; this.z = z; this.face = face; this.item = item; }
        @Override public Item getItem() { return item; }
        @Override public Vector3 getBlock() { return air ? null : new Vector3(x, y, z); }
        @Override public int getFace() { return face; }
        @Override public boolean isAir() { return air; }
    }

    public static final class PlayerDamage extends PlayerEvent implements net.BlockNes.event.entity.EntityDamageEvent {
        public final Player attacker;    
        public final String cause;
        public final boolean forced;
        public int amount;
        public PlayerDamage(Player p, int amount, String cause, boolean forced, Player attacker) { super(p); this.amount = amount; this.cause = cause; this.forced = forced; this.attacker = attacker; }
        @Override public Player getEntity() { return player; }
        @Override public int getDamage() { return amount; }
        @Override public void setDamage(int damage) { amount = damage; }
        @Override public String getCause() { return cause; }
        @Override public Player getDamager() { return attacker; }
    }

    public static final class PlayerDeath extends PlayerEvent implements net.BlockNes.event.player.PlayerDeathEvent {
        public String message;
        public boolean keepInventory;
        public PlayerDeath(Player p, String message) { super(p); this.message = message; }
        @Override public String getDeathMessage() { return message; }
        @Override public void setDeathMessage(String m) { message = m; }
        @Override public boolean getKeepInventory() { return keepInventory; }
        @Override public void setKeepInventory(boolean keep) { keepInventory = keep; }
    }

    public static final class PlayerRespawn extends PlayerEvent implements net.BlockNes.event.player.PlayerRespawnEvent {
        public PlayerRespawn(Player p) { super(p); }
    }

    public static final class PlayerGamemode extends PlayerEvent implements net.BlockNes.event.player.PlayerGameModeChangeEvent {
        public final int from, to;
        public PlayerGamemode(Player p, int from, int to) { super(p); this.from = from; this.to = to; }
        @Override public int getOldGamemode() { return from; }
        @Override public int getNewGamemode() { return to; }
    }

    public static final class BlockBreak extends PlayerEvent implements net.BlockNes.event.block.BlockBreakEvent {
        public final int x, y, z, id, meta;
        public BlockBreak(Player p, int x, int y, int z, int id, int meta) { super(p); this.x = x; this.y = y; this.z = z; this.id = id; this.meta = meta; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getBlockId() { return id; }
        @Override public int getBlockMeta() { return meta; }
    }

    public static final class BlockPlace extends PlayerEvent implements net.BlockNes.event.block.BlockPlaceEvent {
        public final int x, y, z, id, meta;
        public BlockPlace(Player p, int x, int y, int z, int id, int meta) { super(p); this.x = x; this.y = y; this.z = z; this.id = id; this.meta = meta; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getBlockId() { return id; }
        @Override public int getBlockMeta() { return meta; }
    }

    public static final class MobDamage extends Event implements net.BlockNes.event.entity.EntityDamageEvent {
        public final Mob mob;
        public final Player by;
        public int amount;
        public MobDamage(Mob m, int amount, Player by) { mob = m; this.amount = amount; this.by = by; }
        @Override public Mob getEntity() { return mob; }
        @Override public int getDamage() { return amount; }
        @Override public void setDamage(int damage) { amount = damage; }
        @Override public String getCause() { return by != null ? "ENTITY_ATTACK" : "CUSTOM"; }
        @Override public Player getDamager() { return by; }
    }

    public static final class MobDeath extends Event implements net.BlockNes.event.entity.EntityDeathEvent {
        public final Mob mob;
        public final Player killer;
        public final List<Item> drops;
        public MobDeath(Mob m, Player killer, List<Item> drops) { mob = m; this.killer = killer; this.drops = drops; }
        @Override public Mob getEntity() { return mob; }
        @Override public List<Item> getDrops() { return drops; }
    }

    public static final class MobSpawn extends Event implements net.BlockNes.event.entity.EntitySpawnEvent {
        public final Mob mob;
        public final boolean natural;
        public MobSpawn(Mob m, boolean natural) { mob = m; this.natural = natural; }
        @Override public Mob getEntity() { return mob; }
    }

    public static final class MobEatBlock extends Event implements net.BlockNes.event.entity.MobEatBlockEvent {
        public final Mob mob;
        public final int x, y, z, id, meta;
        public int residueId = 3, residueMeta = 0;
        public MobEatBlock(Mob mob, int x, int y, int z, int id, int meta) { this.mob = mob; this.x=x; this.y=y; this.z=z; this.id=id; this.meta=meta; }
        @Override public Object getEntity() { return mob; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getBlockId() { return id; }
        @Override public int getBlockMeta() { return meta; }
        @Override public int getResidueId() { return residueId; }
        @Override public int getResidueMeta() { return residueMeta; }
        @Override public void setResidue(int id, int meta) { this.residueId = Math.max(0, Math.min(255,id)); this.residueMeta = Math.max(0,Math.min(15,meta)); }
    }

    public static final class GolemConstruct extends PlayerEvent implements net.BlockNes.event.player.PlayerGolemConstructEvent {
        public final int golemType, x, y, z;
        public GolemConstruct(Player p, int golemType, int x, int y, int z) { super(p); this.golemType=golemType; this.x=x; this.y=y; this.z=z; }
        @Override public int getGolemType() { return golemType; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
    }

    public static final class ItemPickup extends PlayerEvent implements net.BlockNes.event.player.PlayerPickupItemEvent, net.BlockNes.event.inventory.InventoryPickupItemEvent {
        public final Item item;
        public ItemPickup(Player p, Item item) { super(p); this.item = item; }
        @Override public Item getItem() { return item; }
    }

    public static final class ServerStart extends Event implements net.BlockNes.event.server.ServerStartEvent {
        public ServerStart() { }
    }

    public static final class ServerStop extends Event implements net.BlockNes.event.server.ServerStopEvent {
        public ServerStop() { }
    }

    public static final class MobDespawn extends Event implements net.BlockNes.event.entity.EntityDespawnEvent {
        public final Mob mob;
        public MobDespawn(Mob m) { mob = m; }
        @Override public Mob getEntity() { return mob; }
    }

    public static final class SmeltItem extends Event implements net.BlockNes.event.inventory.SmeltItemEvent {
        public final int x, y, z;
        public final Item source;
        public Item result;
        public SmeltItem(int x, int y, int z, Item source, Item result) { this.x = x; this.y = y; this.z = z; this.source = source; this.result = result; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public void setResult(Item r) { result = r; }
        @Override public Item getSource() { return source; }
        @Override public Item getResult() { return result; }
    }

    public static final class ConnectionAttempt extends Event implements net.BlockNes.event.server.ConnectionAttemptEvent {
        public final String address;
        public ConnectionAttempt(String address) { this.address = address; }
    }

    public static final class ConnectionClose extends Event implements net.BlockNes.event.server.ConnectionCloseEvent {
        public final String address;
        public final String reason;
        public ConnectionClose(String address, String reason) { this.address = address; this.reason = reason; }
    }

    public static final class WorldLoad extends Event implements net.BlockNes.event.level.WorldLoadEvent {
        public final net.BlockNes.level.World world;
        public final boolean nether;
        public WorldLoad(net.BlockNes.level.World world, boolean nether) { this.world = world; this.nether = nether; }
    }

    public static final class WorldUnload extends Event implements net.BlockNes.event.level.WorldUnloadEvent {
        public final net.BlockNes.level.World world;
        public final boolean nether;
        public WorldUnload(net.BlockNes.level.World world, boolean nether) { this.world = world; this.nether = nether; }
    }

    public static final class TimeChange extends Event implements net.BlockNes.event.level.TimeChangeEvent {
        public final int from;
        public final int to;
        public TimeChange(int from, int to) { this.from = from; this.to = to; }
    }

    public static final class InventoryClick extends PlayerEvent implements net.BlockNes.event.inventory.InventoryClickEvent, net.BlockNes.event.inventory.InventoryTransactionEvent {
        public final int window;
        public final int slot;
        public final Item item;
        public InventoryClick(Player p, int window, int slot, Item item) { super(p); this.window = window; this.slot = slot; this.item = item; }
    }

    public static final class ItemUse extends PlayerEvent implements net.BlockNes.event.player.ItemUseEvent {
        public final Item item;
        public ItemUse(Player p, Item item) { super(p); this.item = item; }
    }

    public static final class ArmorEquip extends PlayerEvent implements net.BlockNes.event.inventory.ArmorEquipEvent {
        public final int slot;
        public final Item item;
        public ArmorEquip(Player p, int slot, Item item) { super(p); this.slot = slot; this.item = item; }
    }

    public static final class PlayerExperienceChange extends PlayerEvent implements net.BlockNes.event.player.PlayerExperienceChangeEvent {
        public final int before;
        public final int after;
        public PlayerExperienceChange(Player p, int before, int after) { super(p); this.before = before; this.after = after; }
    }

    public static final class PlayerLevelChange extends PlayerEvent implements net.BlockNes.event.player.PlayerLevelChangeEvent {
        public final int from;
        public final int to;
        public PlayerLevelChange(Player p, int from, int to) { super(p); this.from = from; this.to = to; }
    }

    public static final class PlayerJump extends PlayerEvent implements net.BlockNes.event.player.PlayerJumpEvent {
        public PlayerJump(Player p) { super(p); }
    }

    public static void xpChanged(PluginManager pm, Player p, int before, int after) {
        if (before == after) return;
        pm.fire(new PlayerExperienceChange(p, before, after));
        int lo = net.BlockNes.level.Experience.levelOf(before);
        int ln = net.BlockNes.level.Experience.levelOf(after);
        if (lo != ln) pm.fire(new PlayerLevelChange(p, lo, ln));
    }

    public static final class EntityDamageByEntity extends Event implements net.BlockNes.event.entity.EntityDamageByEntityEvent {
        public final Mob mob;
        public final Player damager;
        public EntityDamageByEntity(Mob m, Player damager) { mob = m; this.damager = damager; }
        public Mob getEntity() { return mob; }
    }

    public static final class PlayerAttack extends PlayerEvent implements net.BlockNes.event.player.PlayerAttackEvent {
        public final long targetEntityId;
        public final Mob targetMob;
        public final Player targetPlayer;
        public final Item weapon;
        public double damage;
        public PlayerAttack(Player attacker, Mob targetMob, Player targetPlayer, long targetEntityId, Item weapon, double damage) {
            super(attacker); this.targetMob = targetMob; this.targetPlayer = targetPlayer; this.targetEntityId = targetEntityId; this.weapon = weapon; this.damage = damage;
        }
        @Override public long getTargetEntityId() { return targetEntityId; }
        @Override public Player getTargetPlayer() { return targetPlayer; }
        @Override public Mob getTargetMob() { return targetMob; }
        @Override public Item getWeapon() { return weapon; }
        @Override public double getDamage() { return damage; }
        @Override public void setDamage(double value) { damage = value; }
    }

    public static final class VehicleMount extends PlayerEvent implements net.BlockNes.event.entity.VehicleMountEvent {
        public final String vehicle;
        public final long vehicleEntityId;
        public final double x, y, z;
        public VehicleMount(Player player, String vehicle, long vehicleEntityId, double x, double y, double z) {
            super(player); this.vehicle = vehicle; this.vehicleEntityId = vehicleEntityId; this.x = x; this.y = y; this.z = z;
        }
        @Override public String getVehicleType() { return vehicle; }
        @Override public long getVehicleEntityId() { return vehicleEntityId; }
    }

    public static final class VehicleDestroy extends PlayerEvent implements net.BlockNes.event.entity.VehicleDestroyEvent {
        public final String vehicle;
        public final long vehicleEntityId;
        public final double x, y, z;
        public VehicleDestroy(Player player, String vehicle, long vehicleEntityId, double x, double y, double z) {
            super(player); this.vehicle = vehicle; this.vehicleEntityId = vehicleEntityId; this.x = x; this.y = y; this.z = z;
        }
        @Override public String getVehicleType() { return vehicle; }
        @Override public long getVehicleEntityId() { return vehicleEntityId; }
        @Override public double getX() { return x; }
        @Override public double getY() { return y; }
        @Override public double getZ() { return z; }
    }

    public static final class MobMove extends Event implements net.BlockNes.event.entity.EntityMoveEvent {
        public final Mob mob;
        public final double fromX;
        public final double fromZ;
        public MobMove(Mob m, double fromX, double fromZ) { mob = m; this.fromX = fromX; this.fromZ = fromZ; }
        public Mob getEntity() { return mob; }
    }

    public static final class MobTarget extends Event implements net.BlockNes.event.entity.EntityTargetEvent {
        public final Mob mob;
        public final Player target;
        public MobTarget(Mob m, Player target) { mob = m; this.target = target; }
        public Mob getEntity() { return mob; }
    }

    public static final class BlockGrow extends Event implements net.BlockNes.event.block.BlockGrowEvent {
        public final int x, y, z, id, meta;
        public int newId, newMeta;
        public BlockGrow(int x, int y, int z, int id, int meta) { this(x, y, z, id, meta, id, meta + 1); }
        public BlockGrow(int x, int y, int z, int id, int meta, int newId, int newMeta) {
            this.x = x; this.y = y; this.z = z; this.id = id; this.meta = meta; this.newId = newId; this.newMeta = newMeta;
        }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getBlockId() { return id; }
        @Override public int getBlockMeta() { return meta; }
        @Override public int getNewBlockId() { return newId; }
        @Override public int getNewBlockMeta() { return newMeta; }
        @Override public void setNewBlock(int id, int meta) { this.newId = Math.max(0, Math.min(255, id)); this.newMeta = Math.max(0, Math.min(15, meta)); }
    }

    public static final class BlockUpdate extends Event implements net.BlockNes.event.block.BlockUpdateEvent {
        public final int x, y, z, id;
        public BlockUpdate(int x, int y, int z, int id) { this.x = x; this.y = y; this.z = z; this.id = id; }
    }

    public static final class BlockRedstone extends Event implements net.BlockNes.event.block.BlockRedstoneEvent {
        public final int x, y, z;
        public BlockRedstone(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    }

    public static final class PlayerHungerChange extends PlayerEvent implements net.BlockNes.event.player.PlayerHungerChangeEvent {
        public final float from;
        public final float to;
        public PlayerHungerChange(Player p, float from, float to) { super(p); this.from = from; this.to = to; }
    }

    public static final class ChunkLoad extends Event implements net.BlockNes.event.level.ChunkLoadEvent {
        public final net.BlockNes.level.World world;
        public final int x;
        public final int z;
        public ChunkLoad(net.BlockNes.level.World world, int x, int z) { this.world = world; this.x = x; this.z = z; }
    }

    public static final class ChunkUnload extends Event implements net.BlockNes.event.level.ChunkUnloadEvent {
        public final net.BlockNes.level.World world;
        public final int x;
        public final int z;
        public ChunkUnload(net.BlockNes.level.World world, int x, int z) { this.world = world; this.x = x; this.z = z; }
    }

    public static final class ChunkPopulate extends Event implements net.BlockNes.event.level.ChunkPopulateEvent {
        public final net.BlockNes.level.World world;
        public final int x;
        public final int z;
        public ChunkPopulate(net.BlockNes.level.World world, int x, int z) { this.world = world; this.x = x; this.z = z; }
    }

    public static final class BlockBurn extends Event implements net.BlockNes.event.block.BlockBurnEvent {
        public final int x, y, z, id;
        public BlockBurn(int x, int y, int z, int id) { this.x = x; this.y = y; this.z = z; this.id = id; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getBlockId() { return id; }
    }

    public static final class BlockFade extends Event implements net.BlockNes.event.block.BlockFadeEvent {
        public final int x, y, z, id;
        public BlockFade(int x, int y, int z, int id) { this.x = x; this.y = y; this.z = z; this.id = id; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getBlockId() { return id; }
    }

    public static final class BlockForm extends Event implements net.BlockNes.event.block.BlockFormEvent {
        public final int x, y, z, id;
        public BlockForm(int x, int y, int z, int id) { this.x = x; this.y = y; this.z = z; this.id = id; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getNewBlockId() { return id; }
    }

    public static final class EntityTransform extends Event implements net.BlockNes.event.entity.EntityTransformEvent {
        public final Mob mob;
        public final int targetType;
        public EntityTransform(Mob m) { this(m, -1); }
        public EntityTransform(Mob m, int targetType) { mob = m; this.targetType = targetType; }
        @Override public Mob getEntity() { return mob; }
        @Override public int getTargetType() { return targetType; }
    }

    public static final class CreeperPower extends Event implements net.BlockNes.event.entity.CreeperPowerEvent {
        public final Mob mob;
        public final int cause;
        public CreeperPower(Mob mob, int cause) { this.mob = mob; this.cause = cause; }
        @Override public Mob getEntity() { return mob; }
        @Override public int getCause() { return cause; }
    }

    public static final class PlayerLatencyUpdate extends PlayerEvent implements net.BlockNes.event.player.PlayerLatencyUpdateEvent {
        public final int ping, previousPing;
        public PlayerLatencyUpdate(Player p, int ping, int previousPing) { super(p); this.ping = ping; this.previousPing = previousPing; }
        @Override public int getPing() { return ping; }
        @Override public int getPreviousPing() { return previousPing; }
    }

    public static void hungerChanged(PluginManager pm, Player p, float before, float after) {
        if (before != after) pm.fire(new PlayerHungerChange(p, before, after));
    }

    public static final class BlockDropItem extends Event implements net.BlockNes.event.block.BlockDropItemEvent {
        public final Player player;
        public final int x, y, z, id;
        public final List<Item> drops;
        public BlockDropItem(Player player, int x, int y, int z, int id, List<Item> drops) { this.player = player; this.x = x; this.y = y; this.z = z; this.id = id; this.drops = drops; }
    }

    public static final class PlayerChangeDimension extends PlayerEvent implements net.BlockNes.event.player.PlayerChangeDimensionEvent {
        public final int from;
        public final int to;
        public PlayerChangeDimension(Player p, int from, int to) { super(p); this.from = from; this.to = to; }
    }

    public static final class LightningStrike extends Event implements net.BlockNes.event.level.LightningStrikeEvent {
        public final double x, y, z;
        public LightningStrike(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
    }

    public static final class FurnaceBurnFuel extends Event implements net.BlockNes.event.inventory.FurnaceBurnFuelEvent {
        public final int x, y, z;
        public final Item fuel;
         
        public int burnTime;
        public FurnaceBurnFuel(int x, int y, int z, Item fuel) { this(x, y, z, fuel, 0); }
        public FurnaceBurnFuel(int x, int y, int z, Item fuel, int burnTime) { this.x = x; this.y = y; this.z = z; this.fuel = fuel; this.burnTime = burnTime; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public Item getFuel() { return fuel; }
        @Override public int getBurnTime() { return burnTime; }
        @Override public void setBurnTime(int ticks) { burnTime = ticks; }
    }

    public static final class PlayerFish extends PlayerEvent implements net.BlockNes.event.player.PlayerFishEvent {
        public final Item fish;
        public PlayerFish(Player p, Item fish) { super(p); this.fish = fish; }
    }

    public static final class VehicleDismount extends PlayerEvent implements net.BlockNes.event.entity.VehicleDismountEvent {
        public final String vehicle;
        public VehicleDismount(Player p, String vehicle) { super(p); this.vehicle = vehicle; }
    }

    public static final class PlayerEnchantItem extends PlayerEvent implements net.BlockNes.event.inventory.PlayerEnchantItemEvent {
        public final Item before;
        public final Item result;
        public final int levelsSpent;
        public PlayerEnchantItem(Player p, Item before, Item result, int levelsSpent) { super(p); this.before = before; this.result = result; this.levelsSpent = levelsSpent; }
    }

    public static final class ServerTick extends Event {
        public final long tick;
        public ServerTick(long t) { tick = t; }
    }

    public static final class PlayerKick extends PlayerEvent implements net.BlockNes.event.player.PlayerKickEvent {
        public String reason;
        public PlayerKick(Player p, String reason) { super(p); this.reason = reason; }
        @Override public String getReason() { return reason; }
        @Override public void setReason(String r) { reason = r; }
    }

    public static final class PlayerPreLogin extends PlayerEvent implements net.BlockNes.event.player.PlayerPreLoginEvent, net.BlockNes.event.player.PlayerHandshakeEvent, net.BlockNes.event.player.PlayerAuthenticateEvent {
        public String kickMessage = "Server closed";
        public PlayerPreLogin(Player p) { super(p); }
        @Override public String getKickMessage() { return kickMessage; }
        @Override public void setKickMessage(String m) { kickMessage = m; }
    }

    public static final class PlayerDropItem extends PlayerEvent implements net.BlockNes.event.player.PlayerDropItemEvent {
        public final Item item;
        public PlayerDropItem(Player p, Item item) { super(p); this.item = item; }
        @Override public Item getItem() { return item; }
    }

    public static final class PlayerItemConsume extends PlayerEvent implements net.BlockNes.event.player.PlayerItemConsumeEvent {
        public final Item item;
        public PlayerItemConsume(Player p, Item item) { super(p); this.item = item; }
        @Override public Item getItem() { return item; }
    }

    public static final class PlayerBedEnter extends PlayerEvent implements net.BlockNes.event.player.PlayerBedEnterEvent {
        public final int x, y, z;
        public PlayerBedEnter(Player p, int x, int y, int z) { super(p); this.x = x; this.y = y; this.z = z; }
    }

    public static final class PlayerBedLeave extends PlayerEvent implements net.BlockNes.event.player.PlayerBedLeaveEvent {
        public final int x, y, z;
        public PlayerBedLeave(Player p, int x, int y, int z) { super(p); this.x = x; this.y = y; this.z = z; }
    }

    public static final class PlayerHeal extends PlayerEvent implements net.BlockNes.event.entity.EntityRegainHealthEvent {
        public int amount;
        public PlayerHeal(Player p, int amount) { super(p); this.amount = amount; }
        @Override public Player getEntity() { return player; }
        @Override public int getAmount() { return amount; }
        @Override public void setAmount(int a) { amount = a; }
    }

    public static final class PlayerToggleSneak extends PlayerEvent implements net.BlockNes.event.player.PlayerToggleSneakEvent {
        public final boolean sneaking;
        public PlayerToggleSneak(Player p, boolean on) { super(p); sneaking = on; }
        @Override public boolean isSneaking() { return sneaking; }
    }

    public static final class EntityCombust extends Event {
        public final long eid;
        public final Player player;    
        public final String cause;
        public final int seconds;
        public EntityCombust(long eid, Player player, String cause, int seconds) { this.eid = eid; this.player = player; this.cause = cause; this.seconds = seconds; }
    }

    public static final class PlayerToggleSprint extends PlayerEvent implements net.BlockNes.event.player.PlayerToggleSprintEvent {
        public final boolean sprinting;
        public PlayerToggleSprint(Player p, boolean on) { super(p); sprinting = on; }
        @Override public boolean isSprinting() { return sprinting; }
    }

    public static final class InventoryOpen extends PlayerEvent implements net.BlockNes.event.inventory.InventoryOpenEvent {
        public final String type;
        public final int x, y, z;
        public InventoryOpen(Player p, String type, int x, int y, int z) { super(p); this.type = type; this.x = x; this.y = y; this.z = z; }
        @Override public String getType() { return type; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
    }

    public static final class InventoryClose extends PlayerEvent implements net.BlockNes.event.inventory.InventoryCloseEvent {
        public InventoryClose(Player p) { super(p); }
    }

    public static final class SignChange extends PlayerEvent implements net.BlockNes.event.block.SignChangeEvent {
        public final int x, y, z;
        public final String[] lines;
        public SignChange(Player p, int x, int y, int z, String[] lines) { super(p); this.x = x; this.y = y; this.z = z; this.lines = lines; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public String getLine(int index) { return lines[index]; }
        @Override public void setLine(int index, String text) { lines[index] = text; }
        @Override public String[] getLines() { return lines; }
    }

    public static final class Explosion extends Event implements net.BlockNes.event.entity.EntityExplodeEvent {
        public final int dimension;
        public final double x, y, z, size;
        public final List<int[]> blocks;
        public Explosion(int dimension, double x, double y, double z, double size, List<int[]> blocks) { this.dimension = dimension; this.x = x; this.y = y; this.z = z; this.size = size; this.blocks = blocks; }
        @Override public int getDimension() { return dimension; }
        @Override public double getX() { return x; }
        @Override public double getY() { return y; }
        @Override public double getZ() { return z; }
        @Override public double getSize() { return size; }
        @Override public List<int[]> getBlocks() { return blocks; }
    }

    public static final class ServerCommand extends Event implements net.BlockNes.event.server.ServerCommandEvent {
        public final Commands.Sender sender;
        public String line;
        public ServerCommand(Commands.Sender sender, String line) { this.sender = sender; this.line = line; }
        @Override public CommandSender getSender() { return sender; }
        @Override public String getCommand() { return line; }
        @Override public void setCommand(String command) { line = command; }
    }

    public static final class PacketReceive extends PlayerEvent implements net.BlockNes.event.server.DataPacketReceiveEvent, net.BlockNes.event.server.PacketDecodeEvent {
        public final int id; public final byte[] data;
        public PacketReceive(Player p, int id, byte[] data) { super(p); this.id = id; this.data = data; }
        @Override public int getPacketId() { return id; }
        @Override public byte[] getPacket() { return data; }
    }

    public static final class PacketSend extends PlayerEvent implements net.BlockNes.event.server.DataPacketSendEvent, net.BlockNes.event.server.PacketEncodeEvent {
        public final byte[] data;
        public PacketSend(Player p, byte[] data) { super(p); this.data = data; }
        @Override public byte[] getPacket() { return data; }
    }

    public static final class PlayerHud extends PlayerEvent {
        public final List<String> lines;
        public String popup = "";
        public PlayerHud(Player p, List<String> lines) { super(p); this.lines = lines; }
    }

    public static final class PlayerExhaust extends PlayerEvent implements net.BlockNes.event.player.PlayerExhaustEvent {
        public float amount;
        public PlayerExhaust(Player p, float amount) { super(p); this.amount = amount; }
        @Override public float getAmount() { return amount; }
        @Override public void setAmount(float a) { amount = a; }
    }

    public static final class PlayerAnimation extends PlayerEvent implements net.BlockNes.event.player.PlayerAnimationEvent {
        public final int action;
        public PlayerAnimation(Player p, int action) { super(p); this.action = action; }
        @Override public int getAnimationType() { return action; }
    }

    public static final class PlayerItemHeld extends PlayerEvent implements net.BlockNes.event.player.PlayerItemHeldEvent {
        public final Item item;
        public final int slot, hotbarSlot;
        public PlayerItemHeld(Player p, Item item, int slot, int hotbarSlot) { super(p); this.item = item; this.slot = slot; this.hotbarSlot = hotbarSlot; }
        @Override public Item getItem() { return item; }
        @Override public int getSlot() { return slot; }
        @Override public int getHotbarSlot() { return hotbarSlot; }
    }

    public static final class CraftItem extends PlayerEvent implements net.BlockNes.event.inventory.CraftItemEvent {
        public final List<Item> input;
        public final Item result;
        public CraftItem(Player p, List<Item> input, Item result) { super(p); this.input = input; this.result = result; }
        @Override public Item getResult() { return result; }
        @Override public List<Item> getInput() { return input; }
    }

    public static final class MenuClick extends PlayerEvent {
        public final Menu menu;
        public final int slot;
         
        public final Item item;
         
        public final Item proposed;
        public MenuClick(Player p, Menu menu, int slot, Item item, Item proposed) {
            super(p); this.menu = menu; this.slot = slot; this.item = item; this.proposed = proposed;
        }
    }

    public static final class LevelSave extends Event implements net.BlockNes.event.level.LevelSaveEvent { }

    public static final class PluginEnable extends Event implements net.BlockNes.event.plugin.PluginEnableEvent {
        public final Plugin plugin;
        public PluginEnable(Plugin p) { plugin = p; }
        @Override public Plugin getPlugin() { return plugin; }
    }

    public static final class PluginDisable extends Event implements net.BlockNes.event.plugin.PluginDisableEvent {
        public final Plugin plugin;
        public PluginDisable(Plugin p) { plugin = p; }
        @Override public Plugin getPlugin() { return plugin; }
    }

    public static final class PlayerTeleport extends PlayerEvent implements net.BlockNes.event.entity.EntityTeleportEvent, net.BlockNes.event.player.PlayerTeleportEvent {
        public final double fromX, fromY, fromZ;
        public double toX, toY, toZ;
        public PlayerTeleport(Player p, double toX, double toY, double toZ) {
            super(p); fromX = p.x; fromY = p.y; fromZ = p.z; this.toX = toX; this.toY = toY; this.toZ = toZ;
        }
        @Override public Player getEntity() { return player; }
        @Override public Position getFrom() { return new Position(fromX, fromY, fromZ, player.getLevel()); }
        @Override public Position getTo() { return new Position(toX, toY, toZ, player.getLevel()); }
    }

    public static final class WeatherChange extends Event implements net.BlockNes.event.level.WeatherChangeEvent {
        public int type, ticks;
        public WeatherChange(int type, int ticks) { this.type = type; this.ticks = ticks; }
        @Override public int getType() { return type; }
        @Override public void setType(int t) { type = t; }
        @Override public int getTicks() { return ticks; }
        @Override public void setTicks(int t) { ticks = t; }
    }

    public static final class BlockSpread extends Event implements net.BlockNes.event.block.BlockSpreadEvent {
        public final int sourceX, sourceY, sourceZ, x, y, z, id, meta;
        public BlockSpread(int sourceX, int sourceY, int sourceZ, int x, int y, int z, int id, int meta) {
            this.sourceX = sourceX; this.sourceY = sourceY; this.sourceZ = sourceZ; this.x = x; this.y = y; this.z = z; this.id = id; this.meta = meta;
        }
        @Override public int getSourceX() { return sourceX; }
        @Override public int getSourceY() { return sourceY; }
        @Override public int getSourceZ() { return sourceZ; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getNewBlockId() { return id; }
        @Override public int getNewBlockMeta() { return meta; }
    }

    public static final class BlockFromTo extends Event implements net.BlockNes.event.block.BlockFromToEvent {
        public final int sourceX, sourceY, sourceZ, x, y, z, liquidId, newMeta;
        public BlockFromTo(int sourceX, int sourceY, int sourceZ, int x, int y, int z, int liquidId, int newMeta) {
            this.sourceX = sourceX; this.sourceY = sourceY; this.sourceZ = sourceZ; this.x = x; this.y = y; this.z = z; this.liquidId = liquidId; this.newMeta = newMeta;
        }
        @Override public int getSourceX() { return sourceX; }
        @Override public int getSourceY() { return sourceY; }
        @Override public int getSourceZ() { return sourceZ; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getLiquidId() { return liquidId; }
        @Override public int getNewMeta() { return newMeta; }
    }

    public static final class LeavesDecay extends Event implements net.BlockNes.event.block.LeavesDecayEvent {
        public final int x, y, z, id;
        public LeavesDecay(int x, int y, int z, int id) { this.x = x; this.y = y; this.z = z; this.id = id; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getBlockId() { return id; }
    }

    public static final class ExplosionPrime extends Event implements net.BlockNes.event.entity.ExplosionPrimeEvent {
        public final double x, y, z;
        public int fuse;
        public ExplosionPrime(double x, double y, double z, int fuse) { this.x = x; this.y = y; this.z = z; this.fuse = fuse; }
        @Override public double getX() { return x; }
        @Override public double getY() { return y; }
        @Override public double getZ() { return z; }
        @Override public int getFuse() { return fuse; }
        @Override public void setFuse(int f) { fuse = f; }
    }

    public static final class EntityShootBow extends PlayerEvent implements net.BlockNes.event.entity.EntityShootBowEvent {
        public final Item bow;
        public double force;
        public EntityShootBow(Player p, Item bow, double force) { super(p); this.bow = bow; this.force = force; }
        @Override public Item getBow() { return bow; }
        @Override public double getForce() { return force; }
        @Override public void setForce(double f) { force = f; }
    }

    public static final class ProjectileLaunch extends Event implements net.BlockNes.event.entity.ProjectileLaunchEvent {
        public final String type;
        public final Player player;
        public final Mob mob;
        public final double x, y, z;
        public ProjectileLaunch(String type, Player player, Mob mob, double x, double y, double z) {
            this.type = type; this.player = player; this.mob = mob; this.x = x; this.y = y; this.z = z;
        }
        @Override public String getProjectileType() { return type; }
        @Override public Player getShooterPlayer() { return player; }
        @Override public Mob getShooterMob() { return mob; }
        @Override public double getX() { return x; }
        @Override public double getY() { return y; }
        @Override public double getZ() { return z; }
    }

    public static final class PlayerAchievementAwarded extends PlayerEvent implements net.BlockNes.event.player.PlayerAchievementAwardedEvent {
        public final String achievement;
        public PlayerAchievementAwarded(Player p, String achievement) { super(p); this.achievement = achievement; }
        @Override public String getAchievement() { return achievement; }
    }

    public static final class PlayerTextPreSend extends PlayerEvent implements net.BlockNes.event.player.PlayerTextPreSendEvent {
        public String message;
        public PlayerTextPreSend(Player p, String message) { super(p); this.message = message; }
        @Override public String getMessage() { return message; }
        @Override public void setMessage(String m) { message = m; }
    }

    public static final class ThunderChange extends Event implements net.BlockNes.event.level.ThunderChangeEvent {
        public final boolean thundering;
        public final int weather;
        public ThunderChange(boolean thundering, int weather) { this.thundering = thundering; this.weather = weather; }
        @Override public boolean isThundering() { return thundering; }
        @Override public int getWeather() { return weather; }
    }

    public static final class ProjectileHit extends Event implements net.BlockNes.event.entity.ProjectileHitEvent {
        public final String type;
        public final Player shooterPlayer;
        public final Mob shooterMob;
        public final double x, y, z;
        public final Player hitPlayer;
        public final Mob hitMob;
        public final int blockId;
        public ProjectileHit(String type, Player shooterPlayer, Mob shooterMob, double x, double y, double z,
                             Player hitPlayer, Mob hitMob, int blockId) {
            this.type = type; this.shooterPlayer = shooterPlayer; this.shooterMob = shooterMob;
            this.x = x; this.y = y; this.z = z; this.hitPlayer = hitPlayer; this.hitMob = hitMob; this.blockId = blockId;
        }
        @Override public String getProjectileType() { return type; }
        @Override public Player getShooterPlayer() { return shooterPlayer; }
        @Override public Mob getShooterMob() { return shooterMob; }
        @Override public double getX() { return x; }
        @Override public double getY() { return y; }
        @Override public double getZ() { return z; }
        @Override public Player getHitPlayer() { return hitPlayer; }
        @Override public Mob getHitMob() { return hitMob; }
        @Override public int getBlockId() { return blockId; }
    }

    public static final class PlayerBucketFill extends PlayerEvent implements net.BlockNes.event.player.PlayerBucketFillEvent {
        public final int x, y, z, face, liquidId;
        public final Item bucket;
        public Item item;
        public PlayerBucketFill(Player p, int x, int y, int z, int face, int liquidId, Item bucket, Item item) {
            super(p);
            this.x = x; this.y = y; this.z = z; this.face = face; this.liquidId = liquidId; this.bucket = bucket; this.item = item;
        }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getFace() { return face; }
        @Override public int getLiquidId() { return liquidId; }
        @Override public Item getBucket() { return bucket; }
        @Override public Item getItem() { return item; }
        @Override public void setItem(Item i) { item = i; }
    }

    public static final class PlayerBucketEmpty extends PlayerEvent implements net.BlockNes.event.player.PlayerBucketEmptyEvent {
        public final int x, y, z, face, liquidId;
        public final Item bucket;
        public Item item;
        public PlayerBucketEmpty(Player p, int x, int y, int z, int face, int liquidId, Item bucket, Item item) {
            super(p);
            this.x = x; this.y = y; this.z = z; this.face = face; this.liquidId = liquidId; this.bucket = bucket; this.item = item;
        }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getFace() { return face; }
        @Override public int getLiquidId() { return liquidId; }
        @Override public Item getBucket() { return bucket; }
        @Override public Item getItem() { return item; }
        @Override public void setItem(Item i) { item = i; }
    }

    public static final class PlayerPickupExpOrb extends PlayerEvent implements net.BlockNes.event.player.PlayerPickupExpOrbEvent {
        public final double x, y, z;
        public int exp;
        public PlayerPickupExpOrb(Player p, int exp, double x, double y, double z) {
            super(p); this.exp = exp; this.x = x; this.y = y; this.z = z;
        }
        @Override public double getX() { return x; }
        @Override public double getY() { return y; }
        @Override public double getZ() { return z; }
        @Override public int getExp() { return exp; }
        @Override public void setExp(int e) { exp = e; }
    }

    public static final class PlayerLogin extends PlayerEvent implements net.BlockNes.event.player.PlayerLoginEvent {
        public String kickMessage = "Login cancelled";
        public PlayerLogin(Player p) { super(p); }
        @Override public String getKickMessage() { return kickMessage; }
        @Override public void setKickMessage(String m) { kickMessage = m; }
    }

    public static final class PlayerUseFishingRod extends PlayerEvent implements net.BlockNes.event.player.PlayerUseFishingRodEvent {
        public static final int START_FISHING = 0, STOP_FISHING = 1;
        public final int action;
        public PlayerUseFishingRod(Player p, int action) { super(p); this.action = action; }
        @Override public int getAction() { return action; }
    }

    public static final class ItemSpawn extends Event implements net.BlockNes.event.entity.ItemSpawnEvent {
        public final net.BlockNes.item.ItemEntity entity;
        public ItemSpawn(net.BlockNes.item.ItemEntity entity) { this.entity = entity; }
        @Override public net.BlockNes.item.ItemEntity getEntity() { return entity; }
    }

    public static final class ItemDespawn extends Event implements net.BlockNes.event.entity.ItemDespawnEvent {
        public final net.BlockNes.item.ItemEntity entity;
        public ItemDespawn(net.BlockNes.item.ItemEntity entity) { this.entity = entity; }
        @Override public net.BlockNes.item.ItemEntity getEntity() { return entity; }
    }

    public static final class RemoteServerCommand extends Event implements net.BlockNes.event.server.RemoteServerCommandEvent {
        public String line;
        public RemoteServerCommand(String line) { this.line = line; }
        @Override public String getLine() { return line; }
        @Override public void setLine(String l) { line = l; }
    }

    public static final class PlayerDamageByMob extends PlayerEvent implements net.BlockNes.event.entity.EntityDamageByEntityEvent {
        public final Mob mob;
        public int damage;
        public PlayerDamageByMob(Player p, Mob mob, int damage) { super(p); this.mob = mob; this.damage = damage; }
        @Override public Player getEntity() { return getPlayer(); }
    }

    public static final class EntityDamageByChildEntity extends PlayerEvent implements net.BlockNes.event.entity.EntityDamageByChildEntityEvent {
        public final String child;
        public final Player shooterPlayer;
        public final Mob shooterMob;
        public int damage;
        public EntityDamageByChildEntity(Player victim, String child, Player shooterPlayer, Mob shooterMob, int damage) {
            super(victim); this.child = child; this.shooterPlayer = shooterPlayer; this.shooterMob = shooterMob; this.damage = damage;
        }
        @Override public Player getEntity() { return getPlayer(); }
        @Override public String getChildType() { return child; }
        @Override public int getDamage() { return damage; }
        @Override public void setDamage(int d) { damage = d; }
    }

    public static final class EntityDamageByBlock extends PlayerEvent implements net.BlockNes.event.entity.EntityDamageByBlockEvent {
        public final int blockId;
        public final String cause;
        public int damage;
        public EntityDamageByBlock(Player p, int blockId, String cause, int damage) { super(p); this.blockId = blockId; this.cause = cause; this.damage = damage; }
        @Override public Player getEntity() { return getPlayer(); }
        @Override public int getBlockId() { return blockId; }
        @Override public String getCause() { return cause; }
        @Override public int getDamage() { return damage; }
        @Override public void setDamage(int d) { damage = d; }
    }

    public static final class EntityCombustByBlock extends PlayerEvent implements net.BlockNes.event.entity.EntityCombustByBlockEvent {
        @Override public int getDuration() { return seconds; }
        @Override public void setDuration(int s) { seconds = s; }
        public final int blockId;
        public int seconds;
        public EntityCombustByBlock(Player p, int blockId, int seconds) { super(p); this.blockId = blockId; this.seconds = seconds; }
        @Override public Player getEntity() { return getPlayer(); }
        @Override public int getBlockId() { return blockId; }
        @Override public int getSeconds() { return seconds; }
        @Override public void setSeconds(int s) { seconds = s; }
    }

    public static final class EntityCombustByEntity extends PlayerEvent implements net.BlockNes.event.entity.EntityCombustByEntityEvent {
        @Override public int getDuration() { return seconds; }
        @Override public void setDuration(int s) { seconds = s; }
        public final Player combusterPlayer;
        public final Mob combusterMob;
        public int seconds;
        public EntityCombustByEntity(Player combustee, Player combusterPlayer, Mob combusterMob, int seconds) {
            super(combustee); this.combusterPlayer = combusterPlayer; this.combusterMob = combusterMob; this.seconds = seconds;
        }
        @Override public Player getEntity() { return getPlayer(); }
        @Override public Player getCombusterPlayer() { return combusterPlayer; }
        @Override public Mob getCombusterMob() { return combusterMob; }
        @Override public int getSeconds() { return seconds; }
        @Override public void setSeconds(int s) { seconds = s; }
    }

    public static final class EntityEffectAdd extends PlayerEvent implements net.BlockNes.event.entity.EntityEffectAddEvent {
        public final int effect, amplifier, duration;
        public EntityEffectAdd(Player p, int effect, int amplifier, int duration) { super(p); this.effect = effect; this.amplifier = amplifier; this.duration = duration; }
        @Override public Player getEntity() { return getPlayer(); }
        @Override public int getEffect() { return effect; }
        @Override public int getAmplifier() { return amplifier; }
        @Override public int getDuration() { return duration; }
    }

    public static final class EntityEffectRemove extends PlayerEvent implements net.BlockNes.event.entity.EntityEffectRemoveEvent {
        public final int effect;
        public EntityEffectRemove(Player p, int effect) { super(p); this.effect = effect; }
        @Override public Player getEntity() { return getPlayer(); }
        @Override public int getEffect() { return effect; }
    }

    public static final class InventoryPickupArrow extends PlayerEvent implements net.BlockNes.event.inventory.InventoryPickupArrowEvent {
        public final Item item;
        public final double x, y, z;
        public InventoryPickupArrow(Player p, Item item, double x, double y, double z) { super(p); this.item = item; this.x = x; this.y = y; this.z = z; }
        @Override public Item getItem() { return item; }
        @Override public double getX() { return x; }
        @Override public double getY() { return y; }
        @Override public double getZ() { return z; }
    }

    public static final class EntityArmorChange extends PlayerEvent implements net.BlockNes.event.entity.EntityArmorChangeEvent {
        public final int slot;
        public final Item oldItem;
        public Item newItem;
        public EntityArmorChange(Player p, int slot, Item oldItem, Item newItem) { super(p); this.slot = slot; this.oldItem = oldItem; this.newItem = newItem; }
        @Override public Player getEntity() { return getPlayer(); }
        @Override public int getSlot() { return slot; }
        @Override public Item getOldItem() { return oldItem; }
        @Override public Item getNewItem() { return newItem; }
        @Override public void setNewItem(Item i) { newItem = i; }
    }

    public static final class EntityEatItem extends PlayerEvent implements net.BlockNes.event.entity.EntityEatItemEvent {
        public final Item food;
        public EntityEatItem(Player p, Item food) { super(p); this.food = food; }
        @Override public Player getEntity() { return getPlayer(); }
        @Override public Item getFood() { return food; }
    }

    public static final class EntityDrinkPotion extends PlayerEvent implements net.BlockNes.event.entity.EntityDrinkPotionEvent {
        public final Item potion;
        public EntityDrinkPotion(Player p, Item potion) { super(p); this.potion = potion; }
        @Override public Player getEntity() { return getPlayer(); }
        @Override public Item getPotion() { return potion; }
    }

    public static final class EntityBlockChange extends Event implements net.BlockNes.event.entity.EntityBlockChangeEvent {
        public final int x, y, z, fromId, toId, toMeta;
        public EntityBlockChange(int x, int y, int z, int fromId, int toId, int toMeta) {
            this.x = x; this.y = y; this.z = z; this.fromId = fromId; this.toId = toId; this.toMeta = toMeta;
        }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getFromId() { return fromId; }
        @Override public int getToId() { return toId; }
        @Override public int getToMeta() { return toMeta; }
    }

    public static final class SpawnChange extends Event implements net.BlockNes.event.level.SpawnChangeEvent {
        public final String level;
        public final int previousX, previousY, previousZ, x, y, z;
        public SpawnChange(String level, int px, int py, int pz, int x, int y, int z) {
            this.level = level; this.previousX = px; this.previousY = py; this.previousZ = pz; this.x = x; this.y = y; this.z = z;
        }
        @Override public String getLevel() { return level; }
        @Override public int getPreviousX() { return previousX; }
        @Override public int getPreviousY() { return previousY; }
        @Override public int getPreviousZ() { return previousZ; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
    }

    public static final class QueryRegenerate extends Event implements net.BlockNes.event.server.QueryRegenerateEvent {
        public final String motd;
        public final int players, maxPlayers;
        public final String version, level;
        public QueryRegenerate(String motd, int players, int maxPlayers, String version, String level) {
            this.motd = motd; this.players = players; this.maxPlayers = maxPlayers; this.version = version; this.level = level;
        }
        @Override public String getMotd() { return motd; }
        @Override public int getPlayers() { return players; }
        @Override public int getMaxPlayers() { return maxPlayers; }
        @Override public String getVersion() { return version; }
        @Override public String getLevel() { return level; }
    }

    public static final class LowMemory extends Event implements net.BlockNes.event.server.LowMemoryEvent {
        public final long memory, memoryLimit;
        public final int triggerCount;
        public LowMemory(long memory, long memoryLimit, int triggerCount) { this.memory = memory; this.memoryLimit = memoryLimit; this.triggerCount = triggerCount; }
        @Override public long getMemory() { return memory; }
        @Override public long getMemoryLimit() { return memoryLimit; }
        @Override public int getTriggerCount() { return triggerCount; }
    }

    public static final class LevelInit extends Event implements net.BlockNes.event.level.LevelInitEvent {
        public final net.BlockNes.level.World world;
        public final boolean nether;
        public LevelInit(net.BlockNes.level.World world, boolean nether) { this.world = world; this.nether = nether; }
    }

    public static final class EntityLevelChange extends PlayerEvent implements net.BlockNes.event.entity.EntityLevelChangeEvent {
        public final net.BlockNes.level.World origin, target;
        public EntityLevelChange(Player p, net.BlockNes.level.World origin, net.BlockNes.level.World target) { super(p); this.origin = origin; this.target = target; }
        @Override public Object getEntity() { return player; }
        @Override public net.BlockNes.level.World getOrigin() { return origin; }
        @Override public net.BlockNes.level.World getTarget() { return target; }
    }

    public static final class PlayerCreation extends Event implements net.BlockNes.event.player.PlayerCreationEvent {
        public final String address, clientId;
        public final int port;
        public PlayerCreation(String address, int port, String clientId) { this.address = address; this.port = port; this.clientId = clientId; }
        @Override public String getAddress() { return address; }
        @Override public int getPort() { return port; }
        @Override public String getClientId() { return clientId; }
    }

    public static final class PlayerMilkCow extends PlayerEvent implements net.BlockNes.event.player.PlayerMilkCowEvent {
        public final Mob cow;
        public final Item bucket;
        public PlayerMilkCow(Player p, Mob cow, Item bucket) { super(p); this.cow=cow; this.bucket=bucket; }
        @Override public Mob getCow() { return cow; }
        @Override public Item getBucket() { return bucket; }
    }

    public static final class PlayerShearEntity extends PlayerEvent implements net.BlockNes.event.player.PlayerShearEntityEvent {
        public final Mob entity;
        public final List<Item> drops = new ArrayList<>();
        public PlayerShearEntity(Player p, Mob entity, List<Item> drops) { super(p); this.entity=entity; if(drops!=null) this.drops.addAll(drops); }
        @Override public Mob getEntity() { return entity; }
        @Override public List<Item> getDrops() { return drops; }
    }

    public static final class PlayerGlassBottle extends PlayerEvent implements net.BlockNes.event.player.PlayerGlassBottleEvent {
        public final int x, y, z, id;
        public final Item item;
        public PlayerGlassBottle(Player p, int x, int y, int z, int id, Item item) { super(p); this.x = x; this.y = y; this.z = z; this.id = id; this.item = item; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getBlockId() { return id; }
        @Override public Item getItem() { return item; }
    }

    public static final class EntityEatBlock extends PlayerEvent implements net.BlockNes.event.entity.EntityEatBlockEvent {
        public final int x, y, z, id, meta;
        public EntityEatBlock(Player p, int x, int y, int z, int id, int meta) { super(p); this.x = x; this.y = y; this.z = z; this.id = id; this.meta = meta; }
        @Override public Object getEntity() { return player; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getBlockId() { return id; }
        @Override public int getBlockMeta() { return meta; }
    }

    public static final class EntityMotion extends PlayerEvent implements net.BlockNes.event.entity.EntityMotionEvent {
        public final float mx, my, mz;
        public EntityMotion(Player p, float mx, float my, float mz) { super(p); this.mx = mx; this.my = my; this.mz = mz; }
        @Override public Object getEntity() { return player; }
        @Override public Vector3 getVector() { return new Vector3(mx, my, mz); }
    }

    public static final class EntityInventoryChange extends PlayerEvent implements net.BlockNes.event.entity.EntityInventoryChangeEvent {
        public final int slot;
        public final Item oldItem;
        public Item newItem;
        public EntityInventoryChange(Player p, int slot, Item oldItem, Item newItem) { super(p); this.slot = slot; this.oldItem = oldItem; this.newItem = newItem; }
        @Override public Object getEntity() { return player; }
        @Override public int getSlot() { return slot; }
        @Override public Item getOldItem() { return oldItem; }
        @Override public Item getNewItem() { return newItem; }
        @Override public void setNewItem(Item item) { newItem = item; }
    }

    public static final class EntityGenerate extends Event implements net.BlockNes.event.entity.EntityGenerateEvent {
        public static final int CAUSE_AI_HOLDER = 0, CAUSE_MOB_SPAWNER = 1;
        public final int type, x, y, z, cause;
        public EntityGenerate(int type, int x, int y, int z, int cause) {
            this.type = type; this.x = x; this.y = y; this.z = z; this.cause = cause;
        }
        @Override public Object getEntity() { return null; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public int getZ() { return z; }
        @Override public int getType() { return type; }
        @Override public int getCause() { return cause; }
    }

    public static final class ItemFrameDropItem extends PlayerEvent implements net.BlockNes.event.block.ItemFrameDropItemEvent {
        public final int x, y, z, blockId;
        public final Item item;
        public ItemFrameDropItem(Player p, int x, int y, int z, int blockId, Item item) {
            super(p); this.x = x; this.y = y; this.z = z; this.blockId = blockId; this.item = item;
        }
        @Override public Item getItem() { return item; }
    }

    public static final class ItemFrameRotate extends PlayerEvent implements net.BlockNes.event.block.ItemFrameRotateEvent {
        public final int x, y, z;
        public final Item item;
        public final int oldRotation;
        public int newRotation;
        public ItemFrameRotate(Player p, int x, int y, int z, Item item, int oldRotation, int newRotation) {
            super(p); this.x = x; this.y = y; this.z = z; this.item = item; this.oldRotation = oldRotation; this.newRotation = newRotation;
        }
        @Override public Item getItem() { return item; }
        @Override public int getOldRotation() { return oldRotation; }
        @Override public int getNewRotation() { return newRotation; }
        @Override public void setNewRotation(int r) { newRotation = r & 7; }
    }

    public static final class MobSpawnerChange extends PlayerEvent implements net.BlockNes.event.block.MobSpawnerChangeEvent {
        public final int x, y, z;
        public final int oldType;
        public int newType;
        public MobSpawnerChange(Player p, int x, int y, int z, int oldType, int newType) {
            super(p); this.x = x; this.y = y; this.z = z; this.oldType = oldType; this.newType = newType;
        }
        @Override public int getOldType() { return oldType; }
        @Override public int getNewType() { return newType; }
        @Override public void setNewType(int networkId) { newType = networkId; }
    }
}
