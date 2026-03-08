package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class KingdomsXHook implements TeamHook {
    private Method getKingdomPlayerMethod;
    private Method getKingdomMethod;
    private Method getNameMethod;

    public KingdomsXHook() {
        try {
            Class<?> kpClass = Class.forName("org.kingdoms.constants.player.KingdomPlayer");
            getKingdomPlayerMethod = kpClass.getMethod("getKingdomPlayer", Player.class);
            getKingdomMethod = kpClass.getMethod("getKingdom");

            Class<?> kingdomClass = Class.forName("org.kingdoms.constants.group.Kingdom");
            getNameMethod = kingdomClass.getMethod("getName");
        } catch (Exception e) {
            // Plugin not present or unsupported version
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        try {
            if (getKingdomPlayerMethod == null)
                return false;
            Object kp1 = getKingdomPlayerMethod.invoke(null, p1);
            Object kp2 = getKingdomPlayerMethod.invoke(null, p2);

            Object k1 = getKingdomMethod.invoke(kp1);
            Object k2 = getKingdomMethod.invoke(kp2);

            return k1 != null && k1.equals(k2);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        try {
            if (getKingdomPlayerMethod == null || getNameMethod == null)
                return null;
            Object kp = getKingdomPlayerMethod.invoke(null, player);
            Object k = getKingdomMethod.invoke(kp);
            if (k == null)
                return null;
            return (String) getNameMethod.invoke(k);
        } catch (Exception e) {
            return null;
        }
    }
}
