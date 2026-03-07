package dev.velmax.velkoth.storage;

import java.util.UUID;

/**
 * Player statistics record.
 */
public record PlayerStats(UUID uuid, String playerName, int totalWins, long lastWinTimestamp) {

    public static PlayerStats empty(UUID uuid, String name) {
        return new PlayerStats(uuid, name, 0, 0L);
    }
}
