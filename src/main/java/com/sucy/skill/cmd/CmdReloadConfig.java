package com.sucy.skill.cmd;

import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.screen.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CmdReloadConfig implements IFunction {


    /**
     * Runs the command
     *
     * @param cmd    command that was executed
     * @param plugin plugin reference
     * @param sender sender of the command
     * @param args   argument list
     */
    @Override
    public void execute(ConfigurableCommand cmd, Plugin plugin, CommandSender sender, String[] args) {
        ConfigManager.INSTANCE.loader();
        cmd.sendMessage(sender, "done", "&9已重写加载新版本配置文件...");
    }
}
