package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class GangsPlusHook implements TeamHook {
    private Method getPlayersGangMethod;
    private Method getNameMethod;

    public GangsPlusHook() {
        try {
            Class<?> apiClass = Class.forName("net.brcdev.gangs.GangsPlusApi");
            getPlayersGangMethod = apiClass.getMethod("getPlayersGang", Player.class);
            Class<?> gangClass = Class.forName("net.brcdev.gangs.gang.Gang");
            getNameMethod = gangClass.getMethod("getName");
        } catch (Exception e) {
            // Plugin not present or unsupported version
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        try {
            if (getPlayersGangMethod == null)
                return false;
            Object g1 = getPlayersGangMethod.invoke(null, p1);
            Object g2 = getPlayersGangMethod.invoke(null, p2);
            return g1 != null && g1.equals(g2);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        try {
            if (getPlayersGangMethod == null || getNameMethod == null)
                return null;
            Object g = getPlayersGangMethod.invoke(null, player);
            if (g == null)
                return null;
            return (String) getNameMethod.invoke(g);
        } catch (Exception e) {
            return null;
        }
    }
}
