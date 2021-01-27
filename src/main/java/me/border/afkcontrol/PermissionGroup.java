package me.border.afkcontrol;

import me.border.utilities.cache.Cache;
import me.border.utilities.cache.Cacheable;
import me.border.utilities.cache.CachedObject;
import me.border.utilities.cache.ExpiringCache;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PermissionGroup {

    private static final Map<String, PermissionGroup> permissionGroups = new HashMap<>();
    private static final Cache<UUID> permissionGroupCache = new ExpiringCache<>(1, TimeUnit.MINUTES);

    private final String name;
    private final int priority;
    private final double kickTime;
    private final double afkTime;

    public PermissionGroup(String name, int priority, double kickTime, double afkTime){
        this.name = name.toUpperCase();
        this.priority = priority;
        this.kickTime = kickTime;
        this.afkTime = afkTime;
        permissionGroups.put(name, this);
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public double getKickTime() {
        return kickTime;
    }

    public double getAFKTime(){
        return afkTime;
    }

    public static Map<String, PermissionGroup> getPermissionGroups() {
        return permissionGroups;
    }

    public static PermissionGroup getPermissionGroup(Player player) {
        UUID uuid = player.getUniqueId();
        Cacheable cacheable = permissionGroupCache.get(uuid);
        if (cacheable != null)
            return (PermissionGroup) cacheable.getObject();

        for (PermissionAttachmentInfo pei : player.getEffectivePermissions()) {
            String perm = pei.getPermission();
            if (perm.startsWith("afkcontrol.group.")) {
                PermissionGroup pg = PermissionGroup.getPermissionGroups().get(perm.substring(perm.lastIndexOf('.')).replace(".", "").toUpperCase());
                permissionGroupCache.cache(uuid, new CachedObject(pg, 5));
                return pg;
            }
        }

        PermissionGroup pg = PermissionGroup.getPermissionGroups().get("DEFAULT");
        permissionGroupCache.cache(uuid, new CachedObject(pg, 5));
        return pg;
    }

    public static void removeFromCache(Player player){
        permissionGroupCache.remove(player.getUniqueId());
    }
}
