package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class FactionsUUIDHook implements TeamHook {
    private Object fplayersInstance;
    private Method getByPlayerMethod;
    private Method getFactionMethod;
    private Method getTagMethod;

    public FactionsUUIDHook() {
        try {
            Class<?> fplayersClass = Class.forName("com.massivecraft.factions.FPlayers");
            fplayersInstance = fplayersClass.getMethod("getInstance").invoke(null);
            getByPlayerMethod = fplayersClass.getMethod("getByPlayer", Player.class);

            Class<?> fplayerClass = Class.forName("com.massivecraft.factions.FPlayer");
            getFactionMethod = fplayerClass.getMethod("getFaction");

            Class<?> factionClass = Class.forName("com.massivecraft.factions.Faction");
            getTagMethod = factionClass.getMethod("getTag");
        } catch (Exception e) {
            // Plugin not present or unsupported version
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        try {
            if (getByPlayerMethod == null)
                return false;
            Object fp1 = getByPlayerMethod.invoke(fplayersInstance, p1);
            Object fp2 = getByPlayerMethod.invoke(fplayersInstance, p2);

            Object f1 = getFactionMethod.invoke(fp1);
            Object f2 = getFactionMethod.invoke(fp2);

            // In factions, wildcard / wilderness might be technically equal,
            // but generally we just match object equality for standard factions.
            return f1 != null && f1.equals(f2);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        try {
            if (getByPlayerMethod == null || getTagMethod == null)
                return null;
            Object fp = getByPlayerMethod.invoke(fplayersInstance, player);
            Object f = getFactionMethod.invoke(fp);
            if (f == null)
                return null;
            return (String) getTagMethod.invoke(f);
        } catch (Exception e) {
            return null;
        }
    }
}
