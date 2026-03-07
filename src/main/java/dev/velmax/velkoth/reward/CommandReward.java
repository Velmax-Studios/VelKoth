package dev.velmax.velkoth.reward;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Reward that dispatches a console command with %player% replacement.
 */
public record CommandReward(String command) implements Reward {

    @Override
    public void grant(Player player) {
        String parsed = command.replace("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
    }

    @Override
    public String describe() {
        return "Command: " + command;
    }
}
