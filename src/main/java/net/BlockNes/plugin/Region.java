package net.BlockNes.plugin;

import net.BlockNes.Server;
import net.BlockNes.level.World;

public final class Region {
     
    public static final int MAX_BLOCKS = 1_000_000;

    private final Server server;
    private final int dimension, minX, minY, minZ, maxX, maxY, maxZ;

    Region(Server server, int dimension, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.server = server;
        this.dimension = dimension;
        this.minX = Math.min(x1, x2); this.maxX = Math.max(x1, x2);
        this.minY = Math.min(y1, y2); this.maxY = Math.max(y1, y2);
        this.minZ = Math.min(z1, z2); this.maxZ = Math.max(z1, z2);
    }

    public int dimension() { return dimension; }
    public int minX() { return minX; }
    public int minY() { return minY; }
    public int minZ() { return minZ; }
    public int maxX() { return maxX; }
    public int maxY() { return maxY; }
    public int maxZ() { return maxZ; }
    public int width() { return maxX - minX + 1; }
    public int height() { return maxY - minY + 1; }
    public int depth() { return maxZ - minZ + 1; }
    public long volume() { return (long) width() * height() * depth(); }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public int count(int id) {
        World w = server.worldOf(dimension);
        int n = 0;
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++)
            if (w.getId(x, y, z) == id) n++;
        return n;
    }

    public int fill(int id, int meta) {
        int n = 0;
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++)
            if (server.setBlockIn(dimension, x, y, z, id, meta)) n++;
        return n;
    }

    public int replace(int fromId, int toId, int toMeta) {
        World w = server.worldOf(dimension);
        int n = 0;
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++)
            if (w.getId(x, y, z) == fromId && server.setBlockIn(dimension, x, y, z, toId, toMeta)) n++;
        return n;
    }

    public Snapshot copy() {
        if (volume() > MAX_BLOCKS) throw new IllegalArgumentException("copia de " + volume() + " bloques: el maximo para copiar es " + MAX_BLOCKS + " (memoria)");
        World w = server.worldOf(dimension);
        int total = (int) volume();
        int[] ids = new int[total], metas = new int[total];
        int i = 0;
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++) {
            ids[i] = w.getId(x, y, z);
            metas[i] = w.getMeta(x, y, z);
            i++;
        }
        return new Snapshot(width(), height(), depth(), ids, metas);
    }

    public int paste(Snapshot s, int x, int y, int z, boolean skipAir) {
        int n = 0;
        for (int dx = 0; dx < s.width; dx++) for (int dy = 0; dy < s.height; dy++) for (int dz = 0; dz < s.depth; dz++) {
            int idx = s.index(dx, dy, dz);
            int id = s.ids[idx];
            if (skipAir && id == 0) continue;
            if (server.setBlockIn(dimension, x + dx, y + dy, z + dz, id, s.metas[idx])) n++;
        }
        return n;
    }

    public int paste(Snapshot s, int x, int y, int z) { return paste(s, x, y, z, false); }

    public static final class Snapshot {
        private final int width, height, depth;
        private final int[] ids, metas;

        Snapshot(int width, int height, int depth, int[] ids, int[] metas) {
            this.width = width; this.height = height; this.depth = depth;
            this.ids = ids; this.metas = metas;
        }

        public int width() { return width; }
        public int height() { return height; }
        public int depth() { return depth; }

        public int idAt(int dx, int dy, int dz) { return ids[index(dx, dy, dz)]; }

        public int metaAt(int dx, int dy, int dz) { return metas[index(dx, dy, dz)]; }

        private int index(int dx, int dy, int dz) { return (dx * height + dy) * depth + dz; }
    }
}
