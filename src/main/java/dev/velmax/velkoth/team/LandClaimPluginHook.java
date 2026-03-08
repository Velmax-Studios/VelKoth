package dev.velmax.velkoth.team;

import dev.velmax.velkoth.VelKothPlugin;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.managers.ClaimManager;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LandClaimPluginHook implements TeamHook {

    private final VelKothPlugin plugin;
    private boolean isAvailable = false;

    public LandClaimPluginHook(VelKothPlugin plugin) {
        this.plugin = plugin;
        try {
            Class.forName("org.ayosynk.landClaimPlugin.LandClaimPlugin");
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
            LandClaimPlugin lcp = LandClaimPlugin.getInstance();
            ClaimManager cm = lcp.getClaimManager();

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
            LandClaimPlugin lcp = LandClaimPlugin.getInstance();
            ClaimManager cm = lcp.getClaimManager();

            UUID ownerId = getEffectiveProfileOwner(player.getUniqueId(), cm);
            if (ownerId != null) {
                ClaimProfile profile = cm.getProfile(ownerId);
                if (profile != null) {
                    return profile.getName();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private @Nullable UUID getEffectiveProfileOwner(UUID playerUuid, ClaimManager cm) {
        // First check if they own a profile
        ClaimProfile ownProfile = cm.getProfile(playerUuid);
        if (ownProfile != null) {
            return ownProfile.getOwnerId();
        }

        // They might be a member/trusted in another active profile.
        // We iterate loaded profiles in ClaimManager (since they are cached).
        try {
            // Since there's no direct API to get a profile by member, we just check all
            // cached profiles
            // This is O(N) but the cache is typically small for active players.
            java.lang.reflect.Field profilesField = cm.getClass().getDeclaredField("profilesCache");
            profilesField.setAccessible(true);
            java.util.Map<UUID, ClaimProfile> cache = (java.util.Map<UUID, ClaimProfile>) profilesField.get(cm);

            for (ClaimProfile p : cache.values()) {
                if (p.isMember(playerUuid) || p.isTrusted(playerUuid)) {
                    return p.getOwnerId();
                }
            }

            // Note: If they are offline, they might not be cached, but participants are
            // online.
        } catch (Exception e) {
            // Ignore reflection errors on private maps if version changed
        }

        return null; // No profile found
    }
}
