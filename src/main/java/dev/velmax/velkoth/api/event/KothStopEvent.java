package dev.velmax.velkoth.api.event;

import dev.velmax.velkoth.arena.Arena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a KoTH arena is stopped.
 */
public class KothStopEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Arena arena;

    public KothStopEvent(Arena arena) {
        this.arena = arena;
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
