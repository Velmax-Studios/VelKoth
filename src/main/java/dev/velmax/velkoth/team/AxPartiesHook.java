package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class AxPartiesHook implements TeamHook {
    private Method getPartyMethod;
    private Method getNameMethod;

    public AxPartiesHook() {
        try {
            Class<?> apiClass = Class.forName("com.artillexstudios.axparties.api.AxPartiesAPI");
            getPartyMethod = apiClass.getMethod("getParty", Player.class);

            Class<?> partyClass = Class.forName("com.artillexstudios.axparties.party.Party");
            getNameMethod = partyClass.getMethod("getName");
        } catch (Exception e) {
            // Plugin not present or unsupported version
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        try {
            if (getPartyMethod == null)
                return false;
            Object prty1 = getPartyMethod.invoke(null, p1);
            Object prty2 = getPartyMethod.invoke(null, p2);

            return prty1 != null && prty1.equals(prty2);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        try {
            if (getPartyMethod == null || getNameMethod == null)
                return null;
            Object party = getPartyMethod.invoke(null, player);
            if (party == null)
                return null;
            return (String) getNameMethod.invoke(party);
        } catch (Exception e) {
            return null;
        }
    }
}
