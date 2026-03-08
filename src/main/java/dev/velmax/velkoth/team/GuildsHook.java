package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class GuildsHook implements TeamHook {
    private Object guildsApiInstance;
    private Method getGuildMethod;
    private Method getNameMethod;

    public GuildsHook() {
        try {
            Class<?> guildsClass = Class.forName("me.glaremasters.guilds.Guilds");
            Object pluginInstance = guildsClass.getMethod("getApi").invoke(null);

            // getGuild() could be on the plugin instance or the API instance
            getGuildMethod = pluginInstance.getClass().getMethod("getGuild", Player.class);
            guildsApiInstance = pluginInstance;

            Class<?> guildClass = Class.forName("me.glaremasters.guilds.guild.Guild");
            getNameMethod = guildClass.getMethod("getName");

        } catch (Exception e) {
            // Plugin not present or unsupported version
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        try {
            if (getGuildMethod == null)
                return false;
            Object g1 = getGuildMethod.invoke(guildsApiInstance, p1);
            Object g2 = getGuildMethod.invoke(guildsApiInstance, p2);
            return g1 != null && g1.equals(g2);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        try {
            if (getGuildMethod == null || getNameMethod == null)
                return null;
            Object g = getGuildMethod.invoke(guildsApiInstance, player);
            if (g == null)
                return null;
            return (String) getNameMethod.invoke(g);
        } catch (Exception e) {
            return null;
        }
    }
}
