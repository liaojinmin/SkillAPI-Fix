
package com.sucy.skill.cmd;

import com.rit.sucy.commands.CommandManager;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.sucy.skill.trail.TrailManager;
import com.sucy.skill.trail.TrailEntity;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CmdTrailDemo implements IFunction {

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
        if (args.length >= 1) {
            Player player = Bukkit.getPlayerExact(args[0]);
            if (player != null) {
                TrailEntity trailEntity = TrailManager.INSTANCE.getDemoTrailEntity();
                if (trailEntity == null) {
                    trailEntity = new TrailEntity(
                            player, "生化狂人",
                            player.getWidth() / 2,
                            -1, 2700, 2700, 20
                    );
                    TrailManager.INSTANCE.setDemoTrailEntity(trailEntity);
                    sender.sendMessage("开始 TrailEntity 测试");
                } else {
                    TrailManager.INSTANCE.setDemoTrailEntity(null);
                    sender.sendMessage("关闭 TrailEntity 测试");
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
