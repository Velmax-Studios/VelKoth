package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for integrating with various team, faction, and party plugins.
 */
public interface TeamHook {

    /**
     * Checks if two players are on the same team/faction/party.
     *
     * @param p1 the first player
     * @param p2 the second player
     * @return true if both players are on the same team
     */
    boolean isSameTeam(Player p1, Player p2);

    /**
     * Gets the name of the player's team/faction/party.
     *
     * @param player the player
     * @return the team name, or null if they don't have one
     */
    @Nullable
    String getTeamName(Player player);
}
