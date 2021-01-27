package me.border.afkcontrol;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.UUID;

public class AFKPlayer {

    private final UUID uuid;
    private final long stamp;
    private final PermissionGroup permissionGroup;

    public AFKPlayer(Player player){
        this.uuid = player.getUniqueId();
        this.stamp = System.currentTimeMillis();
        this.permissionGroup = getPermissionGroup();
    }

    public UUID getUUID() {
        return uuid;
    }

    public long getStamp() {
        return stamp;
    }

    public PermissionGroup getPermissionGroup(){
        if (permissionGroup == null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                return null;

            for (PermissionAttachmentInfo pei : player.getEffectivePermissions()) {
                String perm = pei.getPermission();
                if (perm.startsWith("afkcontrol.group.")) {
                    return PermissionGroup.getPermissionGroups().get(perm.substring(perm.lastIndexOf('.')).replace(".", "").toUpperCase());
                }
            }

            return PermissionGroup.getPermissionGroups().get("DEFAULT");
        }

        return permissionGroup;
    }

    public long getTimeAFK(){
        return System.currentTimeMillis() - getStamp();
    }

    public double getTimeAFKMinutes(){
        return (double) getTimeAFK() / 60000;
    }
}
