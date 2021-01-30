package me.border.afkcontrol.papi;

import me.border.afkcontrol.AFKControl;
import me.border.afkcontrol.AFKManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class AFKControlExpansion extends PlaceholderExpansion {

    public AFKControlExpansion(){
        register();
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getIdentifier() {
        return "afkcontrol";
    }

    @Override
    public String getAuthor() {
        return "MrBorder";
    }

    @Override
    public String getVersion() {
        return AFKControl.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null)
            return null;
        if (!player.isOnline())
            return null;
        Player p = (Player) player;
        if (params.equals("afk")){
            return AFKManager.isAFK(p) ? "[AFK] " : "";
        }

        return null;
    }
}
