package dev.velmax.velkoth.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages wand selections for KoTH region creation.
 */
public final class WandManager {

    private final Map<UUID, Selection> selections = new ConcurrentHashMap<>();

    public void setPos1(Player player, Location location) {
        selections.computeIfAbsent(player.getUniqueId(), k -> new Selection())
                .setPos1(location);
    }

    public void setPos2(Player player, Location location) {
        selections.computeIfAbsent(player.getUniqueId(), k -> new Selection())
                .setPos2(location);
    }

    public @Nullable Selection getSelection(Player player) {
        return selections.get(player.getUniqueId());
    }

    public void clearSelection(Player player) {
        selections.remove(player.getUniqueId());
    }

    /**
     * Represents a player's two-point selection.
     */
    public static final class Selection {
        private @Nullable Location pos1;
        private @Nullable Location pos2;

        public @Nullable Location getPos1() {
            return pos1;
        }

        public void setPos1(@Nullable Location pos1) {
            this.pos1 = pos1;
        }

        public @Nullable Location getPos2() {
            return pos2;
        }

        public void setPos2(@Nullable Location pos2) {
            this.pos2 = pos2;
        }

        public boolean isComplete() {
            return pos1 != null && pos2 != null;
        }
    }

    /**
     * The custom model data for the wand item (blaze rod with enchant glow).
     */
    public static final String WAND_TAG = "velkoth_wand";
}
