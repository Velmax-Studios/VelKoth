package dev.velmax.velkoth.api.event;

import dev.velmax.velkoth.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player stops capturing (leaves the hill or is contested).
 */
public class KothCaptureStopEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Arena arena;
    private final Player player;
    private final Reason reason;

    public KothCaptureStopEvent(Arena arena, Player player, Reason reason) {
        this.arena = arena;
        this.player = player;
        this.reason = reason;
    }

    public Arena getArena() {
        return arena;
    }

    public Player getPlayer() {
        return player;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        LEFT_HILL,
        CONTESTED,
        EVENT_STOPPED,
        GRACE_EXPIRED
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
