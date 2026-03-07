package dev.velmax.velkoth.scheduler;

import dev.velmax.velkoth.VelKothPlugin;
import dev.velmax.velkoth.arena.Arena;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * Manages automatic KoTH event scheduling.
 * Uses a single-threaded ScheduledExecutorService for efficiency.
 */
public final class SchedulerManager {

    private final VelKothPlugin plugin;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "VelKoth-Scheduler");
        t.setDaemon(true);
        return t;
    });
    private final List<ScheduleEntry> entries = new ArrayList<>();

    public SchedulerManager(VelKothPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load schedule entries from config and schedule them.
     */
    public void loadSchedule() {
        entries.clear();
        for (String line : plugin.getPluginConfig().getSchedule()) {
            try {
                ScheduleEntry entry = ScheduleEntry.parse(line);
                entries.add(entry);
                scheduleEntry(entry);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Invalid schedule entry: " + line, e);
            }
        }
        plugin.getLogger().info("Loaded " + entries.size() + " scheduled events.");
    }

    private void scheduleEntry(ScheduleEntry entry) {
        long delayMs = calculateDelayMs(entry);
        long weekMs = TimeUnit.DAYS.toMillis(7);

        executor.scheduleAtFixedRate(() -> {
            // Must run on main thread
            plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
                String arenaId = entry.arenaId();
                if ("random".equalsIgnoreCase(arenaId)) {
                    startRandomArena();
                } else {
                    Arena arena = plugin.getArenaManager().getArena(arenaId);
                    if (arena != null && arena.state() == Arena.ArenaState.IDLE) {
                        plugin.getCaptureManager().startArena(arena);
                        plugin.getLogger().info("Scheduled event started: " + arenaId);
                    }
                }
            });
        }, delayMs, weekMs, TimeUnit.MILLISECONDS);
    }

    private void startRandomArena() {
        var arenas = plugin.getArenaManager().getArenas().stream()
                .filter(a -> a.state() == Arena.ArenaState.IDLE)
                .toList();
        if (!arenas.isEmpty()) {
            Arena random = arenas.get(ThreadLocalRandom.current().nextInt(arenas.size()));
            plugin.getCaptureManager().startArena(random);
            plugin.getLogger().info("Scheduled random event started: " + random.id());
        }
    }

    private long calculateDelayMs(ScheduleEntry entry) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.with(TemporalAdjusters.nextOrSame(entry.dayOfWeek()))
                .withHour(entry.hour())
                .withMinute(entry.minute())
                .withSecond(0)
                .withNano(0);

        if (!target.isAfter(now)) {
            target = target.plusWeeks(1);
        }

        long targetMs = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return targetMs - System.currentTimeMillis();
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public List<ScheduleEntry> getEntries() {
        return List.copyOf(entries);
    }
}
