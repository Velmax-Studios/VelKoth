package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class UltimateClansHook implements TeamHook {
    private Object clansApiInstance;
    private Method getClanPlayerMethod;
    private Method getClanMethod;
    private Method getNameMethod;

    public UltimateClansHook() {
        try {
            Class<?> apiClass = Class.forName("me.ulrich.clans.api.ClansAPI");
            clansApiInstance = apiClass.getMethod("getInstance").invoke(null);
            getClanPlayerMethod = apiClass.getMethod("getClanPlayer", Player.class);

            Class<?> clanPlayerClass = Class.forName("me.ulrich.clans.data.ClanPlayer");
            getClanMethod = clanPlayerClass.getMethod("getClan");

            Class<?> clanClass = Class.forName("me.ulrich.clans.data.Clan");
            getNameMethod = clanClass.getMethod("getTag"); // usually getTag() or getName()
        } catch (Exception e) {
            // Plugin not present or unsupported version
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        try {
            if (getClanPlayerMethod == null)
                return false;
            Object cp1 = getClanPlayerMethod.invoke(clansApiInstance, p1);
            Object cp2 = getClanPlayerMethod.invoke(clansApiInstance, p2);

            if (cp1 == null || cp2 == null)
                return false;

            Object c1 = getClanMethod.invoke(cp1);
            Object c2 = getClanMethod.invoke(cp2);

            return c1 != null && c1.equals(c2);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        try {
            if (getClanPlayerMethod == null || getNameMethod == null)
                return null;
            Object cp = getClanPlayerMethod.invoke(clansApiInstance, player);
            if (cp == null)
                return null;
            Object c = getClanMethod.invoke(cp);
            if (c == null)
                return null;
            return (String) getNameMethod.invoke(c);
        } catch (Exception e) {
            return null;
        }
    }
}
