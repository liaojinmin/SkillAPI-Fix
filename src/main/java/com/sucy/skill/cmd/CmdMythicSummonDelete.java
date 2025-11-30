
package com.sucy.skill.cmd;

import com.rit.sucy.commands.CommandManager;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.hook.mythic.MythicManager;
import com.sucy.skill.hook.mythic.Summon;
import com.sucy.skill.hook.mythic.SummonData;
import com.sucy.skill.utils.AttributeParseUtils;
import com.sucy.skill.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public class CmdMythicSummonDelete implements IFunction {

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

        if (args.length >= 2) {
            Player player = Bukkit.getPlayerExact(args[0]);
            if (player != null) {
                String type = args[1];
                SummonData summonData = MythicManager.INSTANCE.getSummonMap().get(player.getUniqueId());
                if (summonData != null) {
                    Collection<Summon> list = summonData.getAllSummon((it) ->
                        it.getActiveMob().getType().getEntityType().equalsIgnoreCase(type)
                    );
                    for (Summon summon : list) {
                        summon.setDeath(true);
                    }
                } else  {
                    sender.sendMessage("summonData is null -> " + args[0]);
                }
            } else {
                sender.sendMessage("玩家不存在 -> " + args[1]);
            }
        }

        // Invalid arguments
        else
            CommandManager.displayUsage(command, sender, 1);
    }
}
