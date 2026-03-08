package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class SuperiorSkyblock2Hook implements TeamHook {
    private Method getPlayerMethod;
    private Method getIslandMethod;
    private Method getNameMethod;

    public SuperiorSkyblock2Hook() {
        try {
            Class<?> apiClass = Class.forName("com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI");
            getPlayerMethod = apiClass.getMethod("getPlayer", Player.class);

            Class<?> superiorPlayerClass = Class.forName("com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer");
            getIslandMethod = superiorPlayerClass.getMethod("getIsland");

            Class<?> islandClass = Class.forName("com.bgsoftware.superiorskyblock.api.island.Island");
            getNameMethod = islandClass.getMethod("getName");
        } catch (Exception e) {
            // Plugin not present or unsupported version
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        try {
            if (getPlayerMethod == null)
                return false;
            Object sp1 = getPlayerMethod.invoke(null, p1);
            Object sp2 = getPlayerMethod.invoke(null, p2);

            Object i1 = getIslandMethod.invoke(sp1);
            Object i2 = getIslandMethod.invoke(sp2);

            return i1 != null && i1.equals(i2);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        try {
            if (getPlayerMethod == null || getNameMethod == null)
                return null;
            Object sp = getPlayerMethod.invoke(null, player);
            Object island = getIslandMethod.invoke(sp);
            if (island == null)
                return null;
            return (String) getNameMethod.invoke(island);
        } catch (Exception e) {
            return null;
        }
    }
}
