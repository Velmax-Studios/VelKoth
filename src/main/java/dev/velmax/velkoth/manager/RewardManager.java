package dev.velmax.velkoth.manager;

import dev.velmax.velkoth.VelKothPlugin;
import dev.velmax.velkoth.reward.Reward;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Handles granting rewards to KoTH winners.
 */
public final class RewardManager {

    private final VelKothPlugin plugin;

    public RewardManager(VelKothPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Grant all rewards for an arena to the winning player.
     * Executed on the main thread since some rewards (commands, items) require it.
     */
    public void grantRewards(Player player, List<Reward> rewards) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Reward reward : rewards) {
                try {
                    reward.grant(player);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to grant reward '" + reward.describe()
                            + "' to " + player.getName() + ": " + e.getMessage());
                }
            }
        });
    }
}
