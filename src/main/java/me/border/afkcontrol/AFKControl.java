package me.border.afkcontrol;

import me.border.afkcontrol.commands.AFKCommand;
import me.border.afkcontrol.listeners.PlayerAFKHandler;
import me.border.afkcontrol.listeners.PlayerOverloadHandler;
import me.border.afkcontrol.util.TPSCounter;
import me.border.spigotutilities.baseutils.Utils;
import me.border.spigotutilities.plugin.SpigotPlugin;

public class AFKControl extends SpigotPlugin {

    private static TPSCounter tpsCounter;

    @Override
    protected void enable() {
        new AFKCommand();
        tpsCounter.start();
        AFKManager.initKickTasks();
        registerListener(new PlayerAFKHandler());
        registerListener(new PlayerOverloadHandler());

        for (String key : getConfig().getConfigurationSection("PermissionGroups").getKeys(false)){
            new PermissionGroup(key.toUpperCase(), Utils.ci("PermissionGroups." + key + ".priority"),
                    Utils.cd("PermissionGroups." + key + ".kickTime"), Utils.cd("PermissionGroups." + key + ".afkTime"));
        }
    }

    @Override
    protected void load() {
        tpsCounter = TPSCounter.create();
    }
;
    @Override
    protected void disable() {
        tpsCounter.stop();
    }

}
