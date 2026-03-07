package dev.velmax.velkoth.listener;

import dev.velmax.velkoth.VelKothPlugin;
import dev.velmax.velkoth.manager.WandManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Listens for wand interactions to set KoTH region positions.
 */
public final class WandListener implements Listener {

    private final VelKothPlugin plugin;
    private final NamespacedKey wandKey;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public WandListener(VelKothPlugin plugin) {
        this.plugin = plugin;
        this.wandKey = new NamespacedKey(plugin, WandManager.WAND_TAG);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BLAZE_ROD)
            return;

        // Check if this is a KoTH wand
        if (!item.getItemMeta().getPersistentDataContainer().has(wandKey, PersistentDataType.BYTE))
            return;
        if (!player.hasPermission("velkoth.admin"))
            return;

        event.setCancelled(true);

        var loc = event.getClickedBlock() != null
                ? event.getClickedBlock().getLocation()
                : player.getLocation();

        var wand = plugin.getWandManager();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            wand.setPos1(player, loc);
            sendPositionMessage(player, "1", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            wand.setPos2(player, loc);
            sendPositionMessage(player, "2", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    private void sendPositionMessage(Player player, String pos, int x, int y, int z) {
        String template = plugin.getMessages().getPositionSet()
                .replace("<pos>", pos)
                .replace("<x>", String.valueOf(x))
                .replace("<y>", String.valueOf(y))
                .replace("<z>", String.valueOf(z));
        var prefix = miniMessage.deserialize(plugin.getMessages().getPrefix());
        player.sendMessage(prefix.append(miniMessage.deserialize(template)));
    }
}
