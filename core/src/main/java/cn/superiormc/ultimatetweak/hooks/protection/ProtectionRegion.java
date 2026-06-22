package cn.superiormc.ultimatetweak.hooks.protection;

import org.bukkit.Location;
import org.bukkit.World;

public record ProtectionRegion(String id, World world, int minX, int minY, int minZ,
                               int maxX, int maxY, int maxZ) {

    public static ProtectionRegion of(String id, Location min, Location max) {
        return new ProtectionRegion(id,
                min.getWorld(),
                Math.min(min.getBlockX(), max.getBlockX()),
                Math.min(min.getBlockY(), max.getBlockY()),
                Math.min(min.getBlockZ(), max.getBlockZ()),
                Math.max(min.getBlockX(), max.getBlockX()),
                Math.max(min.getBlockY(), max.getBlockY()),
                Math.max(min.getBlockZ(), max.getBlockZ()));
    }

    public Location minLocation() {
        return new Location(world, minX, minY, minZ);
    }

    public Location maxLocation() {
        return new Location(world, maxX, maxY, maxZ);
    }

    public Location centerLocation() {
        return new Location(world,
                (minX + maxX) / 2.0,
                (minY + maxY) / 2.0,
                (minZ + maxZ) / 2.0);
    }

    public int minChunkX() {
        return minX >> 4;
    }

    public int maxChunkX() {
        return maxX >> 4;
    }

    public int minChunkZ() {
        return minZ >> 4;
    }

    public int maxChunkZ() {
        return maxZ >> 4;
    }

    public int maxHorizontalRadius() {
        return Math.max(maxX - minX, maxZ - minZ) / 2 + 1;
    }
}
