package dev.velmax.velkoth.reward;

import org.bukkit.entity.Player;

/**
 * Sealed interface representing a reward granted on KoTH win.
 */
public sealed interface Reward permits CommandReward, ItemReward, EconomyReward {

    /**
     * Grant this reward to the given player.
     */
    void grant(Player player);

    /**
     * Get a human-readable description of this reward.
     */
    String describe();
}
