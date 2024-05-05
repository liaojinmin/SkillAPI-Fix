/**
 * SkillAPI
 * com.sucy.cmd.skill.CmdForceCast
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.cmd;

import com.rit.sucy.commands.CommandManager;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.rit.sucy.version.VersionManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillShot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.regex.Pattern;

/**
 * A command that makes a player cast a skill regardless
 * of them owning it or not and also ignores cooldown/mana costs.
 */
public class CmdConsoleCast implements IFunction {
    private static final String NOT_SKILL    = "not-skill";
    private static final String NOT_AVAILABLE = "not-available";
    private static final String NOT_UNLOCKED = "not-unlocked";
    private static final String DISABLED     = "world-disabled";


    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args) {

        if (args.length >= 2) {
            Player targer = VersionManager.getPlayer(args[0]);
            if (!SkillAPI.getSettings().isWorldEnabled(targer.getWorld())) {
                command.sendMessage(sender, DISABLED, "&4此世界已禁用...");
            }

            PlayerData player = SkillAPI.getPlayerData(targer.getUniqueId());
            if (player == null) {
                sender.sendMessage("玩家数据未加载...");
                return;
            }
            // Get the skill name
            StringBuilder skill = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++) {
                skill.append(" ").append(args[i]);
            }

            // Invalid skill
            if (!SkillAPI.isSkillRegistered(skill.toString())) {
                command.sendMessage(sender, NOT_SKILL, ChatColor.RED + "未知技能名称 "+skill+"...");
            }
            // Class mismatch
            else if (!player.hasSkill(skill.toString())) {
                command.sendMessage(sender, NOT_AVAILABLE, ChatColor.RED + "这个可能不适用你的职业...");
            } else if (!player.hasSkill(skill.toString()) || player.getSkillLevel(skill.toString()) == 0) {
                command.sendMessage(sender, NOT_UNLOCKED, ChatColor.RED + "等级不足...");

            } else {
                player.cast(skill.toString());
            }
        } else {
            CommandManager.displayUsage(command, sender, 1);
        }
    }
}
