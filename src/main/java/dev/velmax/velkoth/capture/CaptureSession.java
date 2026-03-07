package dev.velmax.velkoth.capture;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds the active capture state for a single arena.
 * One CaptureSession exists per active arena event.
 */
public final class CaptureSession {

    private @Nullable UUID capturingPlayer;
    private int elapsedSeconds;
    private boolean contested;
    private int graceTimer;
    private @Nullable UUID lastCapturer;

    /** For SCORE mode: playerUUID -> accumulated points */
    private final Map<UUID, Integer> scores = new HashMap<>();

    public CaptureSession() {
        reset();
    }

    public void reset() {
        this.capturingPlayer = null;
        this.elapsedSeconds = 0;
        this.contested = false;
        this.graceTimer = 0;
        this.lastCapturer = null;
    }

    public @Nullable UUID capturingPlayer() {
        return capturingPlayer;
    }

    public void setCapturingPlayer(@Nullable UUID uuid) {
        this.capturingPlayer = uuid;
    }

    public int elapsedSeconds() {
        return elapsedSeconds;
    }

    public void incrementElapsed() {
        this.elapsedSeconds++;
    }

    public void setElapsedSeconds(int elapsed) {
        this.elapsedSeconds = elapsed;
    }

    public boolean isContested() {
        return contested;
    }

    public void setContested(boolean contested) {
        this.contested = contested;
    }

    public int graceTimer() {
        return graceTimer;
    }

    public void incrementGraceTimer() {
        this.graceTimer++;
    }

    public void resetGraceTimer() {
        this.graceTimer = 0;
    }

    public @Nullable UUID lastCapturer() {
        return lastCapturer;
    }

    public void setLastCapturer(@Nullable UUID lastCapturer) {
        this.lastCapturer = lastCapturer;
    }

    public Map<UUID, Integer> scores() {
        return scores;
    }

    public int addScore(UUID player, int points) {
        return scores.merge(player, points, Integer::sum);
    }

    public int getScore(UUID player) {
        return scores.getOrDefault(player, 0);
    }
}
