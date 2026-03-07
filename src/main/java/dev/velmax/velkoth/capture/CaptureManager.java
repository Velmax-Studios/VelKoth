package dev.velmax.velkoth.capture;

import dev.velmax.velkoth.VelKothPlugin;
import dev.velmax.velkoth.api.event.*;
import dev.velmax.velkoth.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core capture logic engine. Runs a tick loop every second for each active
 * arena,
 * managing capture state, contesting, grace periods, and win conditions.
 */
public final class CaptureManager {

    private final VelKothPlugin plugin;
    private final Map<String, CaptureSession> sessions = new ConcurrentHashMap<>();
    private @Nullable BukkitTask tickTask;

    public CaptureManager(VelKothPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Start the global tick loop (1 tick = 1 second = 20 game ticks).
     */
    public void startTickLoop() {
        if (tickTask != null)
            return;
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    /**
     * Stop the global tick loop.
     */
    public void stopTickLoop() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    /**
     * Initialize a capture session for an arena.
     */
    public void createSession(Arena arena) {
        sessions.put(arena.id(), new CaptureSession());
    }

    /**
     * Remove a capture session for an arena.
     */
    public void removeSession(String arenaId) {
        sessions.remove(arenaId);
    }

    /**
     * Get the session for an arena.
     */
    public @Nullable CaptureSession getSession(String arenaId) {
        return sessions.get(arenaId);
    }

    /**
     * Check if a player is currently capturing any hill.
     */
    public boolean isCapturing(Player player) {
        UUID uuid = player.getUniqueId();
        return sessions.values().stream()
                .anyMatch(s -> uuid.equals(s.capturingPlayer()));
    }

    /**
     * Get all online players currently inside an arena's region.
     */
    public List<Player> getPlayersOnHill(Arena arena) {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (arena.region().contains(player.getLocation())) {
                players.add(player);
            }
        }
        return players;
    }

    /**
     * Main tick — runs once per second.
     */
    private void tick() {
        for (Arena arena : plugin.getArenaManager().getActiveArenas()) {
            CaptureSession session = sessions.get(arena.id());
            if (session == null)
                continue;
            tickArena(arena, session);
        }
    }

    private void tickArena(Arena arena, CaptureSession session) {
        List<Player> playersOnHill = getPlayersOnHill(arena);

        // Spawn hill particles
        plugin.getDisplayManager().spawnHillParticles(arena);

        if (playersOnHill.isEmpty()) {
            handleEmptyHill(arena, session);
        } else if (playersOnHill.size() == 1) {
            handleSingleCapture(arena, session, playersOnHill.getFirst());
        } else {
            handleContested(arena, session, playersOnHill);
        }

        // Update BossBar
        plugin.getDisplayManager().updateBossBar(arena, session);
    }

    private void handleEmptyHill(Arena arena, CaptureSession session) {
        if (session.capturingPlayer() != null) {
            // Player left the hill — start grace period
            session.setLastCapturer(session.capturingPlayer());
            session.incrementGraceTimer();

            if (session.graceTimer() >= arena.gracePeriod()) {
                // Grace expired — reset capture
                Player lastPlayer = Bukkit.getPlayer(session.capturingPlayer());
                if (lastPlayer != null) {
                    Bukkit.getPluginManager().callEvent(
                            new KothCaptureStopEvent(arena, lastPlayer, KothCaptureStopEvent.Reason.GRACE_EXPIRED));
                }
                session.setCapturingPlayer(null);
                session.setElapsedSeconds(0);
                session.resetGraceTimer();
            }
        }
        session.setContested(false);
    }

    private void handleSingleCapture(Arena arena, CaptureSession session, Player player) {
        UUID playerUuid = player.getUniqueId();
        session.setContested(false);
        session.resetGraceTimer();

        if (!playerUuid.equals(session.capturingPlayer())) {
            // New player on the hill
            if (session.capturingPlayer() != null) {
                Player prev = Bukkit.getPlayer(session.capturingPlayer());
                if (prev != null) {
                    Bukkit.getPluginManager().callEvent(
                            new KothCaptureStopEvent(arena, prev, KothCaptureStopEvent.Reason.LEFT_HILL));
                }
            }

            // Fire capture start event
            KothCaptureStartEvent startEvent = new KothCaptureStartEvent(arena, player);
            Bukkit.getPluginManager().callEvent(startEvent);
            if (startEvent.isCancelled())
                return;

            session.setCapturingPlayer(playerUuid);
            session.setElapsedSeconds(0);

            plugin.getDisplayManager().playCaptureStartSound(player);
            plugin.getDisplayManager().broadcast(
                    plugin.getMessages().getCaptureStart(), arena, player);
        }

        // Increment capture
        switch (arena.captureMode()) {
            case CAPTURE -> {
                session.incrementElapsed();
                plugin.getDisplayManager().sendActionBar(player, arena, session);
                plugin.getDisplayManager().playTickSound(player);

                if (session.elapsedSeconds() >= arena.captureTime()) {
                    handleWin(arena, session, player);
                }
            }
            case SCORE -> {
                int score = session.addScore(playerUuid, 1);
                session.incrementElapsed();
                plugin.getDisplayManager().sendActionBar(player, arena, session);

                if (score >= arena.maxScore()) {
                    handleWin(arena, session, player);
                }
            }
        }
    }

    private void handleContested(Arena arena, CaptureSession session, List<Player> players) {
        boolean wasContested = session.isContested();
        session.setContested(true);

        if (!wasContested) {
            // Just became contested
            plugin.getDisplayManager().broadcast(
                    plugin.getMessages().getCaptureContested(), arena, null);

            if (session.capturingPlayer() != null) {
                Player capturer = Bukkit.getPlayer(session.capturingPlayer());
                if (capturer != null) {
                    Bukkit.getPluginManager().callEvent(
                            new KothCaptureStopEvent(arena, capturer, KothCaptureStopEvent.Reason.CONTESTED));
                }
            }
        }

        // Show contested feedback to all players on hill
        for (Player p : players) {
            plugin.getDisplayManager().showContestedTitle(p, arena);
            plugin.getDisplayManager().playContestedSound(p);
        }

        // Capture timer pauses while contested — do not increment
    }

    private void handleWin(Arena arena, CaptureSession session, Player winner) {
        // Fire win event
        KothWinEvent winEvent = new KothWinEvent(arena, winner, session.elapsedSeconds());
        Bukkit.getPluginManager().callEvent(winEvent);

        // Broadcast win
        plugin.getDisplayManager().broadcast(
                plugin.getMessages().getCaptureWin(), arena, winner);
        plugin.getDisplayManager().showWinTitle(winner, arena);
        plugin.getDisplayManager().playWinSound();

        // Grant rewards
        plugin.getRewardManager().grantRewards(winner, arena.rewards());

        // Record stats
        plugin.getStatsManager().recordWin(winner.getUniqueId(), winner.getName(), arena.id());

        // Stop the arena
        plugin.getArenaManager().getArena(arena.id());
        stopArena(arena);
    }

    /**
     * Start an arena event.
     */
    public boolean startArena(Arena arena) {
        if (arena.state() != Arena.ArenaState.IDLE)
            return false;

        KothStartEvent startEvent = new KothStartEvent(arena);
        Bukkit.getPluginManager().callEvent(startEvent);
        if (startEvent.isCancelled())
            return false;

        arena.setState(Arena.ArenaState.ACTIVE);
        createSession(arena);
        plugin.getDisplayManager().createBossBar(arena);
        plugin.getDisplayManager().showStartTitle(arena);
        plugin.getDisplayManager().broadcast(
                plugin.getMessages().getEventStart(), arena, null);

        // Ensure tick loop is running
        startTickLoop();
        return true;
    }

    /**
     * Stop an arena event.
     */
    public void stopArena(Arena arena) {
        arena.setState(Arena.ArenaState.IDLE);

        CaptureSession session = sessions.get(arena.id());
        if (session != null && session.capturingPlayer() != null) {
            Player player = Bukkit.getPlayer(session.capturingPlayer());
            if (player != null) {
                Bukkit.getPluginManager().callEvent(
                        new KothCaptureStopEvent(arena, player, KothCaptureStopEvent.Reason.EVENT_STOPPED));
            }
        }

        removeSession(arena.id());
        plugin.getDisplayManager().removeBossBar(arena);

        Bukkit.getPluginManager().callEvent(new KothStopEvent(arena));
        plugin.getDisplayManager().broadcast(
                plugin.getMessages().getEventStop(), arena, null);

        // Stop tick loop if no more active arenas
        if (plugin.getArenaManager().getActiveArenas().isEmpty()) {
            stopTickLoop();
        }
    }

    /**
     * Pause an arena event.
     */
    public void pauseArena(Arena arena) {
        arena.setState(Arena.ArenaState.PAUSED);
        plugin.getDisplayManager().broadcast(
                plugin.getMessages().getEventPaused(), arena, null);
    }

    /**
     * Resume a paused arena.
     */
    public void resumeArena(Arena arena) {
        arena.setState(Arena.ArenaState.ACTIVE);
        plugin.getDisplayManager().broadcast(
                plugin.getMessages().getEventResumed(), arena, null);
    }

    /**
     * Cleanup all sessions and stop tick loop.
     */
    public void shutdown() {
        stopTickLoop();
        // Stop all active arenas gracefully
        for (Arena arena : plugin.getArenaManager().getActiveArenas()) {
            arena.setState(Arena.ArenaState.IDLE);
            removeSession(arena.id());
            plugin.getDisplayManager().removeBossBar(arena);
        }
        sessions.clear();
    }
}
