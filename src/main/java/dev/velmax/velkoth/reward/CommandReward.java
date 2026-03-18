package dev.velmax.velkoth.reward;

import dev.velmax.velkoth.VelKothPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Reward that dispatches a console command with %player% replacement.
 */
public record CommandReward(String command) implements Reward {

    @Override
    public void grant(Player player, VelKothPlugin plugin) {
        String parsed = command.replace("%player%", player.getName());
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> 
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed));
    }

    @Override
    public String describe() {
        return "Command: " + command;
    }
}
