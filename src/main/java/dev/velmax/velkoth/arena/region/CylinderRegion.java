package dev.velmax.velkoth.arena.region;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Cylindrical region defined by a center, radius, and vertical bounds.
 * Containment check is O(1) using 2D distance squared comparison.
 */
public record CylinderRegion(World world, double centerX, double centerZ,
        double radius, double minY, double maxY) implements Region {

    public CylinderRegion {
        if (radius <= 0)
            throw new IllegalArgumentException("Radius must be positive");
        if (minY > maxY) {
            double t = minY;
            minY = maxY;
            maxY = t;
        }
    }

    @Override
    public boolean contains(Location location) {
        if (!location.getWorld().equals(world))
            return false;
        double y = location.getY();
        // Use exclusive upper bounds (+1) to include the full volume of blocks at maxY
        if (y < minY || y >= (maxY + 1.0))
            return false;
        double dx = location.getX() - centerX;
        double dz = location.getZ() - centerZ;
        return (dx * dx + dz * dz) <= (radius * radius);
    }

    @Override
    public Location getCenter() {
        return new Location(world, centerX, (minY + maxY) / 2.0, centerZ);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public String getType() {
        return "CYLINDER";
    }
}
