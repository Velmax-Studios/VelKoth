package dev.velmax.velkoth.api;

import dev.velmax.velkoth.VelKothPlugin;
import dev.velmax.velkoth.arena.Arena;
import dev.velmax.velkoth.storage.PlayerStats;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Public API for external plugins to interact with VelKoth.
 */
public final class VelKothAPI {

    private VelKothAPI() {
    }

    /**
     * Get an arena by its ID.
     */
    public static @Nullable Arena getArena(@NotNull String id) {
        return VelKothPlugin.getInstance().getArenaManager().getArena(id);
    }

    /**
     * Get all registered arenas.
     */
    public static @NotNull Collection<Arena> getArenas() {
        return VelKothPlugin.getInstance().getArenaManager().getArenas();
    }

    /**
     * Get all currently active (running) arenas.
     */
    public static @NotNull Collection<Arena> getActiveArenas() {
        return VelKothPlugin.getInstance().getArenaManager().getActiveArenas();
    }

    /**
     * Check if a player is currently capturing any hill.
     */
    public static boolean isPlayerCapturing(@NotNull Player player) {
        return VelKothPlugin.getInstance().getCaptureManager().isCapturing(player);
    }

    /**
     * Get the leaderboard asynchronously.
     */
    public static @NotNull CompletableFuture<List<PlayerStats>> getLeaderboard(int limit) {
        return VelKothPlugin.getInstance().getStatsManager().getLeaderboard(limit);
    }

    /**
     * Get stats for a player asynchronously.
     */
    public static @NotNull CompletableFuture<PlayerStats> getPlayerStats(@NotNull UUID uuid) {
        return VelKothPlugin.getInstance().getStatsManager().getStats(uuid);
    }
}
