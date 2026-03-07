package dev.velmax.velkoth.api.event;

import dev.velmax.velkoth.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player wins a KoTH event.
 */
public class KothWinEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Arena arena;
    private final Player winner;
    private final int captureTimeSeconds;

    public KothWinEvent(Arena arena, Player winner, int captureTimeSeconds) {
        this.arena = arena;
        this.winner = winner;
        this.captureTimeSeconds = captureTimeSeconds;
    }

    public Arena getArena() {
        return arena;
    }

    public Player getWinner() {
        return winner;
    }

    public int getCaptureTimeSeconds() {
        return captureTimeSeconds;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
