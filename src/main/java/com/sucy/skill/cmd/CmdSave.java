package com.sucy.skill.cmd;

import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.data.io.IOManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * SkillAPI-Fix
 * com.sucy.skill.cmd
 *
 * @author 老廖
 * @since 2023/10/9 1:11
 */
public class CmdSave implements IFunction {

    @Override
    public void execute(ConfigurableCommand configurableCommand, Plugin plugin, CommandSender commandSender, String[] strings) {
        commandSender.sendMessage("正在执行数据保存...");
        SkillAPI.getIoManager().saveAll();
    }

}


