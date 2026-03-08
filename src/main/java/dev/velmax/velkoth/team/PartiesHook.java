package dev.velmax.velkoth.team;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.UUID;

public class PartiesHook implements TeamHook {
    private Object partiesApiInstance;
    private Method getPartyPlayerMethod;
    private Method getPartyNameMethod;

    public PartiesHook() {
        try {
            Class<?> partiesClass = Class.forName("com.alessiodp.parties.api.Parties");
            partiesApiInstance = partiesClass.getMethod("getApi").invoke(null);

            Class<?> apiClass = Class.forName("com.alessiodp.parties.api.interfaces.PartiesAPI");
            getPartyPlayerMethod = apiClass.getMethod("getPartyPlayer", UUID.class);

            Class<?> partyPlayerClass = Class.forName("com.alessiodp.parties.api.interfaces.PartyPlayer");
            getPartyNameMethod = partyPlayerClass.getMethod("getPartyName");

        } catch (Exception e) {
            // Plugin not present or unsupported version
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        try {
            if (getPartyPlayerMethod == null)
                return false;
            Object pp1 = getPartyPlayerMethod.invoke(partiesApiInstance, p1.getUniqueId());
            Object pp2 = getPartyPlayerMethod.invoke(partiesApiInstance, p2.getUniqueId());

            if (pp1 == null || pp2 == null)
                return false;

            Object pn1 = getPartyNameMethod.invoke(pp1);
            Object pn2 = getPartyNameMethod.invoke(pp2);

            // Party names are usually strings, compare if not empty
            return pn1 != null && !((String) pn1).isEmpty() && pn1.equals(pn2);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        try {
            if (getPartyPlayerMethod == null || getPartyNameMethod == null)
                return null;
            Object pp = getPartyPlayerMethod.invoke(partiesApiInstance, player.getUniqueId());
            if (pp == null)
                return null;
            String pn = (String) getPartyNameMethod.invoke(pp);
            if (pn != null && pn.isEmpty())
                return null;
            return pn;
        } catch (Exception e) {
            return null;
        }
    }
}
