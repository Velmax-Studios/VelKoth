package dev.velmax.velkoth.reward;

import dev.velmax.velkoth.hook.VaultHook;
import org.bukkit.entity.Player;

/**
 * Reward that deposits money via Vault economy.
 */
public record EconomyReward(double amount) implements Reward {

    @Override
    public void grant(Player player) {
        VaultHook.deposit(player, amount);
    }

    @Override
    public String describe() {
        return "Economy: $" + String.format("%.2f", amount);
    }
}
