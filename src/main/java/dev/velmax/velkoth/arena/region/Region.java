package dev.velmax.velkoth.arena.region;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Sealed interface representing a KoTH capture region.
 * Implementations must provide efficient O(1) containment checks.
 */
public sealed interface Region permits CuboidRegion, CylinderRegion {

    /**
     * Check if a location is inside this region.
     * This must be an efficient O(1) operation.
     */
    boolean contains(Location location);

    /**
     * Get the center point of this region.
     */
    Location getCenter();

    /**
     * Get the world this region belongs to.
     */
    World getWorld();

    /**
     * Get the type identifier for serialization.
     */
    String getType();
}
