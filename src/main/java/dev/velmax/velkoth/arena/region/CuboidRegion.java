package dev.velmax.velkoth.arena.region;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Axis-aligned bounding box region.
 * Containment check is O(1) using min/max coordinate comparison.
 */
public record CuboidRegion(World world, double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ) implements Region {

    public CuboidRegion {
        // Normalize so min <= max
        if (minX > maxX) {
            double t = minX;
            minX = maxX;
            maxX = t;
        }
        if (minY > maxY) {
            double t = minY;
            minY = maxY;
            maxY = t;
        }
        if (minZ > maxZ) {
            double t = minZ;
            minZ = maxZ;
            maxZ = t;
        }
    }

    @Override
    public boolean contains(Location location) {
        if (!location.getWorld().equals(world))
            return false;
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        // Use exclusive upper bounds (+1) to include the full volume of blocks at maxX/maxY/maxZ
        return x >= minX && x < (maxX + 1.0)
                && y >= minY && y < (maxY + 1.0)
                && z >= minZ && z < (maxZ + 1.0);
    }

    @Override
    public Location getCenter() {
        return new Location(world,
                (minX + maxX) / 2.0,
                (minY + maxY) / 2.0,
                (minZ + maxZ) / 2.0);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public String getType() {
        return "CUBOID";
    }
}
