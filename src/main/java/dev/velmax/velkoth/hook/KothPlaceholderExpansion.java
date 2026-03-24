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
        String[] parts = params.toLowerCase().split("_");
        if (parts.length == 0) return null;

        boolean isNext = parts[0].equals("next");
        int offset = isNext ? 1 : 0;
        
        if (parts.length <= offset) return null;
        String type = parts[offset];
        
        // Potential arenaId is the last part if there are more parts than expected for type
        String arenaId = null;
        if (parts.length > offset + 1) {
            // Check if parts[parts.length-1] is a placeholder type or an arena ID
            String last = parts[parts.length - 1];
            if (!last.equals("formatted")) {
                arenaId = last;
            }
        }

        if (isNext) {
            var scheduler = plugin.getSchedulerManager();
            var entry = (arenaId != null) ? scheduler.getNextEntry(arenaId) : scheduler.getNextEntry();
            
            if (entry == null) return "N/A";

            long delaySeconds = scheduler.calculateDelayMs(entry) / 1000;
            return switch (type) {
                case "arena" -> {
                    Arena arena = plugin.getArenaManager().getArena(entry.arenaId());
                    yield arena != null ? arena.displayName() : entry.arenaId();
                }
                case "time" -> {
                    if (params.contains("_formatted")) yield formatNextTime(delaySeconds);
                    yield String.valueOf(delaySeconds);
                }
                default -> null;
            };
        }

        // Active arena placeholders
        var activeArenas = plugin.getArenaManager().getActiveArenas();
        Arena arena = null;
        if (arenaId != null) {
            arena = plugin.getArenaManager().getArena(arenaId);
            if (arena == null || arena.state() != Arena.ArenaState.ACTIVE) return "N/A";
        } else {
            if (activeArenas.isEmpty()) return "None";
            arena = activeArenas.iterator().next();
        }

        CaptureSession session = plugin.getCaptureManager().getSession(arena.id());
        int remaining = (session != null) ? (arena.captureTime() - session.elapsedSeconds()) : arena.captureTime();
        remaining = Math.max(0, remaining);

        return switch (type) {
            case "arena" -> arena.displayName();
            case "time" -> {
                if (params.contains("_formatted")) yield formatTime(remaining);
                yield String.valueOf(remaining);
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

    private String formatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0) {
            return String.format("%02d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }

    private String formatNextTime(long seconds) {
        long d = seconds / 86400;
        long h = (seconds % 86400) / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(String.format("%02dd ", d));
        if (d > 0 || h > 0) sb.append(String.format("%02dh ", h));
        if (d > 0 || h > 0 || m > 0) sb.append(String.format("%02dm ", m));
        sb.append(String.format("%02ds", s));
        return sb.toString();
    }
}
