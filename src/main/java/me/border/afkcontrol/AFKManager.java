package me.border.afkcontrol;

import me.border.afkcontrol.util.TPSCounter;
import me.border.spigotutilities.baseutils.ChatUtils;
import me.border.spigotutilities.baseutils.Utils;
import me.border.spigotutilities.task.TaskBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class AFKManager {

    private static final Map<UUID, AFKPlayer> afkPlayers = new HashMap<>();
    private static final Map<Integer, LinkedList<AFKPlayer>> pioAfkMap = new HashMap<>();

    public static void initKickTasks() {
        // AUTO AFK KICK TASK
        TaskBuilder.builder()
                .async()
                .after(45, TimeUnit.SECONDS)
                .every(1, TimeUnit.MINUTES)
                .runnable(new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!Utils.cb("AutoKick.enabled"))
                            return;

                        for (AFKPlayer afkPlayer : afkPlayers.values()) {
                            double neededMins = afkPlayer.getPermissionGroup().getKickTime();
                            double mins = afkPlayer.getTimeAFKMinutes();

                            if (mins >= neededMins) {
                                Bukkit.getScheduler().runTask(AFKControl.getInstance(), () -> {
                                    Player player = Bukkit.getPlayer(afkPlayer.getUUID());
                                    if (player == null) {
                                        removeAFK(afkPlayer);
                                        return;
                                    }

                                    player.kickPlayer(Utils.ucs("AutoKick.KickMessage"));
                                });
                            }
                        }
                    }
                })
                .run();

        TaskBuilder.builder()
                .async()
                .after(30, TimeUnit.SECONDS)
                .every(Utils.ci("TPSKick.Time"), TimeUnit.SECONDS)
                .runnable(new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!Utils.cb("TPSKick.enabled"))
                            return;

                        double tps = TPSCounter.getInstance().getTPS();
                        if (tps < Utils.ci("TPSKick.TPS")){
                            kickWorstPio("TPSKick");
                        }
                    }
                })
                .run();

    }

    public static void kickWorstPioWhile(Callable<Boolean> forCase, String reason){
        List<Integer> pios = new LinkedList<>(pioAfkMap.keySet());
        Collections.sort(pios);

        for (int i : pios){
            if (pioAfkMap.containsKey(i)){
                LinkedList<AFKPlayer> players = pioAfkMap.get(i);
                for (int j = 0; j < players.size(); j++) {
                    try {
                        if (!forCase.call())
                            return;
                    } catch (Exception e){
                        e.printStackTrace();
                        return;
                    }
                    kickFirst(reason, players, i);
                }
            }
        }
    }

    public static void kickWorstPio(String reason) {
        List<Integer> pios = new LinkedList<>(pioAfkMap.keySet());
        Collections.sort(pios);

        for (int i : pios) {
            if (pioAfkMap.containsKey(i)) {
                LinkedList<AFKPlayer> players = pioAfkMap.get(i);
                kickFirst(reason, players, i);
                return;
            }
        }
    }

    private static void kickFirst(String reason, LinkedList<AFKPlayer> players, int key) {
        if (players.isEmpty()) {
            pioAfkMap.remove(key);
            kickWorstPio(reason);
            return;
        }
        AFKPlayer pickedPlayer = players.removeFirst();
        if (players.isEmpty()) {
            pioAfkMap.remove(key);
        }


        Bukkit.getScheduler().runTask(AFKControl.getInstance(), () -> {
            Player player = Bukkit.getPlayer(pickedPlayer.getUUID());
            if (player == null) {
                removeAFK(pickedPlayer);
                return;
            }

            player.kickPlayer(Utils.ucs(reason + ".KickMessage"));
        });
    }

    public static void addAFK(Player player, boolean afk){
        AFKPlayer afkPlayer = new AFKPlayer(player);
        afkPlayers.put(player.getUniqueId(), afkPlayer);

        int pio = afkPlayer.getPermissionGroup().getPriority();
        LinkedList<AFKPlayer> afkPlayersList;
        if (pioAfkMap.containsKey(pio)){
            afkPlayersList = pioAfkMap.get(pio);
            afkPlayersList.add(afkPlayer);
        } else {
            afkPlayersList = new LinkedList<>();
            afkPlayersList.add(afkPlayer);
            pioAfkMap.put(pio, afkPlayersList);

        }

        if (afk) {
            if (Utils.cb("AFKBroadcast.enabled")){
                ChatUtils.broadcastMessage("AFKBroadcast.AFKEnabled", "%player%", player.getName());
            }
        }
    }

    public static void removeAFK(Player player, boolean afk){
        AFKPlayer afkPlayer = afkPlayers.remove(player.getUniqueId());
        int pio = afkPlayer.getPermissionGroup().getPriority();
        try {
            pioAfkMap.get(pio).remove(afkPlayer);
        } catch (NullPointerException ignored){
            // ignored
        }
        if (afk) {
            if (Utils.cb("AFKBroadcast.enabled")){
                ChatUtils.broadcastMessage("AFKBroadcast.AFKDisabled", "%player%", player.getName());
            }
        }
    }

    private static void removeAFK(AFKPlayer player){
        afkPlayers.remove(player.getUUID());
        int pio = player.getPermissionGroup().getPriority();
        pioAfkMap.get(pio).remove(player);
    }

    public static AFKPlayer getAFK(UUID uuid){
        return afkPlayers.get(uuid);
    }

    public static boolean isAFK(Player player){
        return afkPlayers.containsKey(player.getUniqueId());
    }
}
