package dev.velmax.velkoth.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * Optional Vault economy integration.
 */
public final class VaultHook {

    private static @Nullable Economy economy;
    private static boolean available;

    private VaultHook() {
    }

    public static void setup(Logger logger) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            logger.info("Vault not found — economy rewards disabled.");
            available = false;
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
            available = true;
            logger.info("Vault economy hooked successfully.");
        } else {
            logger.warning("Vault found but no Economy provider registered.");
            available = false;
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static void deposit(Player player, double amount) {
        if (available && economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    public static double getBalance(Player player) {
        if (available && economy != null) {
            return economy.getBalance(player);
        }
        return 0;
    }
}
