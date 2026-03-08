package dev.velmax.velkoth.team;

import dev.velmax.velkoth.VelKothPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the loaded TeamHook.
 */
public class TeamManager {

    private final VelKothPlugin plugin;
    private TeamHook activeHook;

    public TeamManager(VelKothPlugin plugin) {
        this.plugin = plugin;
        this.activeHook = new NullTeamHook();
    }

    /**
     * Attempts to find and load a supported team plugin.
     */
    public void loadHook() {
        if (Bukkit.getPluginManager().isPluginEnabled("BetterTeams")) {
            activeHook = new BetterTeamsHook();
            plugin.getLogger().info("Hooked into BetterTeams for team support!");
        } else if (Bukkit.getPluginManager().isPluginEnabled("Factions")) {
            // Need to check which Factions fork it is. SaberFactions and FactionsUUID share
            // similar APIs.
            // For safety we try one first. We will assume FactionsUUID/SaberFactions for
            // now.
            activeHook = new FactionsUUIDHook();
            plugin.getLogger().info("Hooked into Factions (UUID/Saber) for faction support!");
        } else if (Bukkit.getPluginManager().isPluginEnabled("GangsPlus")) {
            activeHook = new GangsPlusHook();
            plugin.getLogger().info("Hooked into GangsPlus for gang support!");
        } else if (Bukkit.getPluginManager().isPluginEnabled("Guilds")) {
            activeHook = new GuildsHook();
            plugin.getLogger().info("Hooked into Guilds for guild support!");
        } else if (Bukkit.getPluginManager().isPluginEnabled("Kingdoms")) {
            activeHook = new KingdomsXHook();
            plugin.getLogger().info("Hooked into KingdomsX for kingdom support!");
        } else if (Bukkit.getPluginManager().isPluginEnabled("SuperiorSkyblock2")) {
            activeHook = new SuperiorSkyblock2Hook();
            plugin.getLogger().info("Hooked into SuperiorSkyblock2 for island team support!");
        } else if (Bukkit.getPluginManager().isPluginEnabled("Towny")) {
            activeHook = new TownyHook();
            plugin.getLogger().info("Hooked into Towny Advanced for town support!");
        } else if (Bukkit.getPluginManager().isPluginEnabled("UltimateClans")) {
            activeHook = new UltimateClansHook();
            plugin.getLogger().info("Hooked into UltimateClans for clan support!");
        } else if (Bukkit.getPluginManager().isPluginEnabled("Parties")) {
            activeHook = new PartiesHook();
            plugin.getLogger().info("Hooked into Parties for party support!");
        } else if (Bukkit.getPluginManager().isPluginEnabled("AxParties")) {
            activeHook = new AxPartiesHook();
            plugin.getLogger().info("Hooked into AxParties for party support!");
        } else if (Bukkit.getPluginManager().isPluginEnabled("LandClaimPlugin")) {
            activeHook = new LandClaimPluginHook(plugin);
            plugin.getLogger().info("Hooked into LandClaimPlugin for claim team support!");
        } else {
            plugin.getLogger().info("No supported team/faction plugin found. Team capturing is disabled.");
        }
    }

    /**
     * Check if two players are on the same team.
     */
    public boolean isSameTeam(Player p1, Player p2) {
        if (p1 == null || p2 == null)
            return false;
        if (p1.getUniqueId().equals(p2.getUniqueId()))
            return true;

        try {
            return activeHook.isSameTeam(p1, p2);
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking team status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get a player's team name.
     */
    public @Nullable String getTeamName(Player player) {
        if (player == null)
            return null;
        try {
            return activeHook.getTeamName(player);
        } catch (Exception e) {
            return null;
        }
    }
}
