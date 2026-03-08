package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class BetterTeamsHook implements TeamHook {
    private Method getTeamMethod;
    private Method getNameMethod;

    public BetterTeamsHook() {
        try {
            Class<?> teamClass = Class.forName("com.booksaw.betterTeams.Team");
            getTeamMethod = teamClass.getMethod("getTeam", Player.class);
            getNameMethod = teamClass.getMethod("getName");
        } catch (Exception e) {
            // Plugin not present or unsupported version
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        try {
            if (getTeamMethod == null)
                return false;
            Object t1 = getTeamMethod.invoke(null, p1);
            Object t2 = getTeamMethod.invoke(null, p2);
            return t1 != null && t1.equals(t2);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        try {
            if (getTeamMethod == null || getNameMethod == null)
                return null;
            Object team = getTeamMethod.invoke(null, player);
            if (team == null)
                return null;
            return (String) getNameMethod.invoke(team);
        } catch (Exception e) {
            return null;
        }
    }
}
