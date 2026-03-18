package dev.velmax.velkoth.reward;

import dev.velmax.velkoth.VelKothPlugin;
import org.bukkit.entity.Player;

/**
 * Sealed interface representing a reward granted on KoTH win.
 */
public sealed interface Reward permits CommandReward, ItemReward, EconomyReward {

    /**
     * Grant this reward to the given player.
     * 
     * @param player the player
     * @param plugin the plugin instance
     */
    void grant(Player player, VelKothPlugin plugin);

    /**
     * Get a human-readable description of this reward.
     */
    String describe();
}
