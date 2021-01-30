package me.border.afkcontrol.listeners;

import me.border.afkcontrol.AFKManager;
import me.border.spigotutilities.baseutils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerOverloadHandler implements Listener {

    private final int overloadMax;

    public PlayerOverloadHandler(){
        overloadMax = Utils.ci("PlayerOverload.Amount");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if (Bukkit.getOnlinePlayers().size() > overloadMax){
            AFKManager.kickWorstPioWhile(() -> {
                if (!Utils.cb("PlayerOverload.enabled"))
                    return false;

                return Bukkit.getOnlinePlayers().size() >= overloadMax;
            }, "PlayerOverload");
        }
    }
}
