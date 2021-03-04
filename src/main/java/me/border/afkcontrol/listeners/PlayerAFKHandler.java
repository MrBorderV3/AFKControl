package me.border.afkcontrol.listeners;

import me.border.afkcontrol.AFKManager;
import me.border.afkcontrol.AFKPlayer;
import me.border.afkcontrol.PermissionGroup;
import me.border.spigotutilities.baseutils.Utils;
import me.border.spigotutilities.task.TaskBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerAFKHandler implements Listener {

    // UUID = player, Long = timestamp of last time they moved
    private final Map<UUID, Long> lastMove = new LinkedHashMap<>();
    private final double xMove;
    private final double yMove;
    private final double zMove;

    public PlayerAFKHandler(){
        TaskBuilder.builder()
                .async()
                .after(10, TimeUnit.SECONDS)
                .every(45, TimeUnit.SECONDS)
                .runnable(new BukkitRunnable() {
                    @Override
                    public void run() {
                        Set<UUID> remove = new HashSet<>();
                        for (Map.Entry<UUID, Long> entry : lastMove.entrySet()){
                            double minsAFK = toMinutes(entry.getValue());
                            Player player = Bukkit.getPlayer(entry.getKey());
                            if (player == null) {
                                remove.add(entry.getKey());
                                continue;
                            }
                            if (AFKManager.isAFK(player))
                                continue;


                                PermissionGroup pg = PermissionGroup.getPermissionGroup(player);
                            if (minsAFK >= pg.getAFKTime()){
                                AFKManager.addAFK(player, true);
                                PermissionGroup.removeFromCache(player);
                            }
                        }

                        for (UUID uuid : remove){
                            lastMove.remove(uuid);
                        }
                    }
                })
                .run();

        xMove = Utils.cd("AFKMove.x");
        yMove = Utils.cd("AFKMove.y");
        zMove = Utils.cd("AFKMove.z");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        if (p.isOp())
            return;
        if (AFKManager.isAFK(p)){
            AFKManager.removeAFK(p, false);
            remove(e.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if (p.isOp())
            return;
        add(p);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.isOp())
            return;
        Location from = e.getFrom();
        Location to = e.getTo();
        double x = to.getX();
        double y = to.getY();
        double z = to.getZ();

        if (AFKManager.isAFK(p)) {
            AFKPlayer afkPlayer = AFKManager.getAFK(p.getUniqueId());
            Location location = afkPlayer.getLocation();
            if (x > location.getX() + xMove || x < location.getX() - xMove ||
                    y > location.getY() + yMove || y < location.getY() - yMove ||
                    z > location.getZ() + zMove || z < location.getZ() - zMove) {
                add(p);
                AFKManager.removeAFK(p, true);
            }
        } else {
            if (from.getX() != x || from.getY() != y || from.getZ() != z){
                add(p);
            }
        }
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent e){
        if (e.getEntity() instanceof Player){
            if (e.getDamager() instanceof  Player){
                Player p = (Player) e.getDamager();
                if (p.isOp())
                    return;
                add(p);
                if (AFKManager.isAFK(p)){
                    AFKManager.removeAFK(p, true);
                }
            }
            Player p = (Player) e.getEntity();
            if (p.isOp())
                return;
            add(p);
            if (AFKManager.isAFK(p)){
                AFKManager.removeAFK(p, true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if (e.getWhoClicked() instanceof Player){
            Player p = (Player) e.getWhoClicked();
            if (p.isOp())
                return;
            add(p);
            if (AFKManager.isAFK(p)){
                AFKManager.removeAFK(p, true);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();
        if (p.isOp())
            return;
        add(p);
        if (AFKManager.isAFK(p)){
            AFKManager.removeAFK(p, true);
        }
    }

    private void add(Player player){
        lastMove.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void remove(Player player){
        lastMove.remove(player.getUniqueId());
    }

    private double toMinutes(long then){
        return (double) (System.currentTimeMillis() - then) / 60000;
    }
}
