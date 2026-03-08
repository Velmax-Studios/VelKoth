package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class TownyHook implements TeamHook {
    private Object townyApiInstance;
    private Method getTownMethod;
    private Method getNameMethod;

    public TownyHook() {
        try {
            Class<?> apiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            townyApiInstance = apiClass.getMethod("getInstance").invoke(null);
            getTownMethod = apiClass.getMethod("getTown", Player.class);

            Class<?> townClass = Class.forName("com.palmergames.bukkit.towny.object.Town");
            getNameMethod = townClass.getMethod("getName");
        } catch (Exception e) {
            // Plugin not present or unsupported version
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        try {
            if (getTownMethod == null)
                return false;
            Object t1 = getTownMethod.invoke(townyApiInstance, p1);
            Object t2 = getTownMethod.invoke(townyApiInstance, p2);
            return t1 != null && t1.equals(t2);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        try {
            if (getTownMethod == null || getNameMethod == null)
                return null;
            Object t = getTownMethod.invoke(townyApiInstance, player);
            if (t == null)
                return null;
            return (String) getNameMethod.invoke(t);
        } catch (Exception e) {
            return null;
        }
    }
}
