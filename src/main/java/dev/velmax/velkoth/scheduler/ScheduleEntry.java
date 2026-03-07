package dev.velmax.velkoth.scheduler;

/**
 * Represents a scheduled KoTH event entry.
 */
public record ScheduleEntry(java.time.DayOfWeek dayOfWeek, int hour, int minute, String arenaId) {

    /**
     * Parse a schedule string in format "DAY:HH:mm:arenaId".
     */
    public static ScheduleEntry parse(String input) {
        String[] parts = input.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid schedule format: " + input + " (expected DAY:HH:mm:arenaId)");
        }
        java.time.DayOfWeek day = java.time.DayOfWeek.valueOf(parts[0].toUpperCase());
        int hour = Integer.parseInt(parts[1]);
        int minute = Integer.parseInt(parts[2]);
        String arenaId = parts[3];
        return new ScheduleEntry(day, hour, minute, arenaId);
    }
}
