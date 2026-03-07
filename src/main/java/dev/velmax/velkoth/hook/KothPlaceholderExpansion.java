package dev.velmax.velkoth.hook;

import dev.velmax.velkoth.VelKothPlugin;
import dev.velmax.velkoth.arena.Arena;
import dev.velmax.velkoth.capture.CaptureSession;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for VelKoth.
 * Provides: %koth_arena%, %koth_time%, %koth_owner%, %koth_players%
 */
public class KothPlaceholderExpansion extends PlaceholderExpansion {

    private final VelKothPlugin plugin;

    public KothPlaceholderExpansion(VelKothPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "koth";
    }

    @Override
    public @NotNull String getAuthor() {
        return "VelMax Studios";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        var activeArenas = plugin.getArenaManager().getActiveArenas();
        if (activeArenas.isEmpty()) {
            return switch (params.toLowerCase()) {
                case "arena" -> "None";
                case "time" -> "0";
                case "owner" -> "None";
                case "players" -> "0";
                default -> null;
            };
        }

        // Use the first active arena by default
        Arena arena = activeArenas.iterator().next();
        CaptureSession session = plugin.getCaptureManager().getSession(arena.id());

        return switch (params.toLowerCase()) {
            case "arena" -> arena.displayName();
            case "time" -> {
                if (session != null) {
                    int remaining = arena.captureTime() - session.elapsedSeconds();
                    yield String.valueOf(Math.max(0, remaining));
                }
                yield String.valueOf(arena.captureTime());
            }
            case "owner" -> {
                if (session != null && session.capturingPlayer() != null) {
                    var p = Bukkit.getPlayer(session.capturingPlayer());
                    yield p != null ? p.getName() : "None";
                }
                yield "None";
            }
            case "players" -> {
                if (session != null) {
                    yield String.valueOf(plugin.getCaptureManager().getPlayersOnHill(arena).size());
                }
                yield "0";
            }
            default -> null;
        };
    }
}
