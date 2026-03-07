package dev.velmax.velkoth.manager;

import dev.velmax.velkoth.storage.DatabaseManager;
import dev.velmax.velkoth.storage.PlayerStats;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages player statistics and leaderboards.
 * All database calls are asynchronous.
 */
public final class StatsManager {

    private final DatabaseManager database;

    public StatsManager(DatabaseManager database) {
        this.database = database;
    }

    /**
     * Record a win for a player.
     */
    public CompletableFuture<Void> recordWin(UUID uuid, String playerName, String arenaId) {
        return database.recordWin(uuid, playerName, arenaId);
    }

    /**
     * Get a player's overall stats.
     */
    public CompletableFuture<PlayerStats> getStats(UUID uuid) {
        return database.getStats(uuid);
    }

    /**
     * Get the top N players by total wins.
     */
    public CompletableFuture<List<PlayerStats>> getLeaderboard(int limit) {
        return database.getLeaderboard(limit);
    }

    /**
     * Get daily wins (since midnight today).
     */
    public CompletableFuture<Integer> getDailyWins(UUID uuid) {
        long startOfDay = java.time.LocalDate.now()
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        return database.getWinsSince(uuid, startOfDay);
    }

    /**
     * Get weekly wins (since start of this week).
     */
    public CompletableFuture<Integer> getWeeklyWins(UUID uuid) {
        long startOfWeek = java.time.LocalDate.now()
                .with(java.time.DayOfWeek.MONDAY)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        return database.getWinsSince(uuid, startOfWeek);
    }
}
