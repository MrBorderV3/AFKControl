package me.border.afkcontrol.listeners;

import me.border.afkcontrol.AFKManager;
import me.border.afkcontrol.PermissionGroup;
import me.border.spigotutilities.task.TaskBuilder;
import org.bukkit.Bukkit;
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
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        if (AFKManager.isAFK(e.getPlayer())){
            AFKManager.removeAFK(e.getPlayer(), false);
            remove(e.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        add(e.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Player p = e.getPlayer();
        add(p);
        if (AFKManager.isAFK(p)){
            AFKManager.removeAFK(p, true);
        }
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent e){
        if (e.getEntity() instanceof Player){
            if (e.getDamager() instanceof  Player){
                Player p = (Player) e.getDamager();
                add(p);
                if (AFKManager.isAFK(p)){
                    AFKManager.removeAFK(p, true);
                }
            }
            Player p = (Player) e.getEntity();
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
            add(p);
            if (AFKManager.isAFK(p)){
                AFKManager.removeAFK(p, true);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player p = e.getPlayer();
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
