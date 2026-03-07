package dev.velmax.velkoth.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Main plugin configuration loaded from config.yml.
 */
@Header("VelKoth Configuration")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class PluginConfig extends OkaeriConfig {

    @Comment("Database configuration")
    private DatabaseConfig database = new DatabaseConfig();

    @Comment("Default capture settings for new arenas")
    private CaptureDefaults captureDefaults = new CaptureDefaults();

    @Comment("Display settings")
    private DisplaySettings display = new DisplaySettings();

    @Comment({ "Scheduled events", "Format: day:HH:mm:arenaId or a cron expression" })
    private List<String> schedule = List.of(
            "SATURDAY:14:00:spawn_koth",
            "SUNDAY:18:00:random");

    public DatabaseConfig getDatabase() {
        return database;
    }

    public CaptureDefaults getCaptureDefaults() {
        return captureDefaults;
    }

    public DisplaySettings getDisplay() {
        return display;
    }

    public List<String> getSchedule() {
        return schedule;
    }

    public static class DatabaseConfig extends OkaeriConfig {
        @Comment("Database type: SQLITE or MYSQL")
        private String type = "SQLITE";

        @Comment("MySQL connection details (ignored for SQLite)")
        private String host = "localhost";
        private int port = 3306;
        private String database = "velkoth";
        private String username = "root";
        private String password = "";

        @Comment("Connection pool size")
        private int poolSize = 5;

        public String getType() {
            return type;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getDatabase() {
            return database;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public int getPoolSize() {
            return poolSize;
        }
    }

    public static class CaptureDefaults extends OkaeriConfig {
        @Comment("Default capture time in seconds")
        private int captureTime = 120;

        @Comment("Grace period in seconds before capture resets when player leaves")
        private int gracePeriod = 5;

        @Comment("Default capture mode: CAPTURE or SCORE")
        private String captureMode = "CAPTURE";

        @Comment("Default max score for SCORE mode")
        private int maxScore = 300;

        @Comment("Whether the hill becomes contested when multiple players are on it")
        private boolean contestEnabled = true;

        public int getCaptureTime() {
            return captureTime;
        }

        public int getGracePeriod() {
            return gracePeriod;
        }

        public String getCaptureMode() {
            return captureMode;
        }

        public int getMaxScore() {
            return maxScore;
        }

        public boolean isContestEnabled() {
            return contestEnabled;
        }
    }

    public static class DisplaySettings extends OkaeriConfig {
        @Comment("Enable BossBar showing capture progress")
        private boolean bossBarEnabled = true;

        @Comment("Enable ActionBar timer")
        private boolean actionBarEnabled = true;

        @Comment("Enable title notifications")
        private boolean titlesEnabled = true;

        @Comment("Enable capture sounds")
        private boolean soundsEnabled = true;

        @Comment("Enable particle effects on the hill")
        private boolean particlesEnabled = true;

        public boolean isBossBarEnabled() {
            return bossBarEnabled;
        }

        public boolean isActionBarEnabled() {
            return actionBarEnabled;
        }

        public boolean isTitlesEnabled() {
            return titlesEnabled;
        }

        public boolean isSoundsEnabled() {
            return soundsEnabled;
        }

        public boolean isParticlesEnabled() {
            return particlesEnabled;
        }
    }
}
