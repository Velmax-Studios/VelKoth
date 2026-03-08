package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * A fallback hook that always returns false.
 */
public class NullTeamHook implements TeamHook {

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        return false;
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        return null;
    }
}
