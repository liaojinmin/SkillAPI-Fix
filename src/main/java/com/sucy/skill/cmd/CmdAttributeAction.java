
package com.sucy.skill.cmd;

import com.rit.sucy.commands.CommandManager;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.utils.AttributeParseUtils;
import com.sucy.skill.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public class CmdAttributeAction implements IFunction {


    private static final String ATTRIBUTE_NAME = "临时属性";

    /**
     * Executes the command
     *
     * @param command owning command
     * @param plugin  plugin reference
     * @param sender  sender of the command
     * @param args    arguments
     */
    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args) {
        /*
            class attAction give <player> 勇斗:100
            class attAction clear <player>
         */
        if (args.length >= 2) {
            Player player = Bukkit.getPlayer(args[1]);
            if (args[0].equalsIgnoreCase("give")) {
                if (player != null) {
                    Pair<String, Integer> pair = AttributeParseUtils.getAttribute(args[2]);
                    if (pair != null) {
                        AttributeAPI.addAttribute(player, ATTRIBUTE_NAME, pair.key, pair.value);
                    } else {
                        sender.sendMessage("属性不存在 -> " + args[2]);
                    }
                } else {
                    sender.sendMessage("玩家不存在 -> " + args[1]);
                }
            } else {
                if (player != null) {
                    AttributeAPI.clearSource(player, ATTRIBUTE_NAME);
                }
            }
        }

        // Invalid arguments
        else
            CommandManager.displayUsage(command, sender, 1);
    }
}
