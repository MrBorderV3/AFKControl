package me.border.afkcontrol.commands;

import me.border.afkcontrol.AFKManager;
import me.border.spigotutilities.command.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.border.spigotutilities.baseutils.CommandUtils.*;
import static me.border.spigotutilities.baseutils.ChatUtils.*;

public class AFKCommand extends ICommand {
    public AFKCommand() {
        super("afk", true, "afkcontrol.afk");
    }

    @Override
    public boolean commandUsed(CommandSender sender, String[] args) {
        if (!argsCheck(sender, 0, args)) return false;

        Player player = (Player) sender;
        if (player.isOp()){
            sendMsg(player, "AFKCommand.OP");
            return true;
        }

        if (AFKManager.isAFK(player)) {
            AFKManager.removeAFK(player, true);
            sendMsg(player, "AFKCommand.AFKDisabled");
        } else {
            AFKManager.addAFK(player, true);
            sendMsg(player, "AFKCommand.AFKEnabled");
        }

        return true;
    }
}
