package dev.velmax.velkoth.team;

import dev.velmax.velkoth.VelKothPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.UUID;

public class LandClaimPluginHook implements TeamHook {

    private final VelKothPlugin plugin;
    private boolean isAvailable = false;
    private Class<?> landClaimPluginClass;

    public LandClaimPluginHook(VelKothPlugin plugin) {
        this.plugin = plugin;
        try {
            landClaimPluginClass = Class.forName("org.ayosynk.landClaimPlugin.LandClaimPlugin");
            isAvailable = true;
        } catch (ClassNotFoundException e) {
            isAvailable = false;
        }
    }

    @Override
    public boolean isSameTeam(Player p1, Player p2) {
        if (!isAvailable)
            return false;

        try {
            Method getInstanceMethod = landClaimPluginClass.getMethod("getInstance");
            Object lcp = getInstanceMethod.invoke(null);
            
            Method getClaimManagerMethod = landClaimPluginClass.getMethod("getClaimManager");
            Object cm = getClaimManagerMethod.invoke(lcp);

            UUID id1 = getEffectiveProfileOwner(p1.getUniqueId(), cm);
            UUID id2 = getEffectiveProfileOwner(p2.getUniqueId(), cm);

            return id1 != null && id1.equals(id2);
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking LandClaimTeam: " + e.getMessage());
            return false;
        }
    }

    @Override
    public @Nullable String getTeamName(Player player) {
        if (!isAvailable)
            return null;

        try {
            Method getInstanceMethod = landClaimPluginClass.getMethod("getInstance");
            Object lcp = getInstanceMethod.invoke(null);
            
            Method getClaimManagerMethod = landClaimPluginClass.getMethod("getClaimManager");
            Object cm = getClaimManagerMethod.invoke(lcp);

            UUID ownerId = getEffectiveProfileOwner(player.getUniqueId(), cm);
            if (ownerId != null) {
                Method getProfileMethod = cm.getClass().getMethod("getProfile", UUID.class);
                Object profile = getProfileMethod.invoke(cm, ownerId);
                if (profile != null) {
                    Method getNameMethod = profile.getClass().getMethod("getName");
                    return (String) getNameMethod.invoke(profile);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private @Nullable UUID getEffectiveProfileOwner(UUID playerUuid, Object cm) {
        try {
            // First check if they own a profile
            Method getProfileMethod = cm.getClass().getMethod("getProfile", UUID.class);
            Object ownProfile = getProfileMethod.invoke(cm, playerUuid);
            
            if (ownProfile != null) {
                Method getOwnerIdMethod = ownProfile.getClass().getMethod("getOwnerId");
                return (UUID) getOwnerIdMethod.invoke(ownProfile);
            }

            // They might be a member/trusted in another active profile.
            // We iterate loaded profiles in ClaimManager (since they are cached).
            java.lang.reflect.Field profilesField = cm.getClass().getDeclaredField("profilesCache");
            profilesField.setAccessible(true);
            java.util.Map<UUID, Object> cache = (java.util.Map<UUID, Object>) profilesField.get(cm);

            for (Object p : cache.values()) {
                Method isMemberMethod = p.getClass().getMethod("isMember", UUID.class);
                Method isTrustedMethod = p.getClass().getMethod("isTrusted", UUID.class);
                
                boolean isMember = (boolean) isMemberMethod.invoke(p, playerUuid);
                boolean isTrusted = (boolean) isTrustedMethod.invoke(p, playerUuid);
                
                if (isMember || isTrusted) {
                    Method getOwnerIdMethod = p.getClass().getMethod("getOwnerId");
                    return (UUID) getOwnerIdMethod.invoke(p);
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors on private maps if version changed
        }

        return null; // No profile found
    }
}
