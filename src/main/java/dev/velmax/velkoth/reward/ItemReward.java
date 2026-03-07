package dev.velmax.velkoth.reward;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Reward that gives an ItemStack to the player.
 */
public record ItemReward(ItemStack item) implements Reward {

    @Override
    public void grant(Player player) {
        var leftover = player.getInventory().addItem(item.clone());
        // Drop any items that didn't fit
        leftover.values().forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
    }

    @Override
    public String describe() {
        return "Item: " + item.getType().name() + " x" + item.getAmount();
    }
}
