/**
 * SkillAPI
 * com.sucy.cmd.skill.CmdInfo
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

import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.rit.sucy.config.Filter;
import com.rit.sucy.text.TextFormatter;
import com.rit.sucy.version.VersionManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.mob.MobAttribute;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.language.RPGFilter;
import me.neon.flash.attribute.AttributePlayer;
import me.neon.flash.attribute.comp.SuitData;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A command that displays a player's current class information
 */
public class CmdInfo implements IFunction
{
    private static final String NEEDS_ARGS = "needs-player";
    private static final String TITLE      = "title";
    private static final String CATEGORY   = "category";
    private static final String PROFESSION = "profession";
    private static final String EXP        = "exp";
    private static final String SEPARATOR  = "separator";
    private static final String END        = "end";
    private static final String NO_CLASS   = "no-class";
    private static final String NOT_PLAYER = "not-player";
    private static final String DISABLED   = "world-disabled";

    /**
     * Runs the command
     *
     * @param cmd    command that was executed
     * @param plugin plugin reference
     * @param sender sender of the command
     * @param args   argument list
     */
    @Override
    public void execute(ConfigurableCommand cmd, Plugin plugin, CommandSender sender, String[] args)
    {
        // Disabled world
        if (sender instanceof Player && !SkillAPI.getSettings().isWorldEnabled(((Player) sender).getWorld()) && args.length == 0)
        {
            cmd.sendMessage(sender, DISABLED, "&4You cannot use this command in this world");
        }

        // Only can show info of a player so console needs to provide a name
        else if (sender instanceof Player || args.length >= 1) {
            AtomicReference<PlayerData> parseOwner = new AtomicReference<>();
            OfflinePlayer target = args.length == 0 ? (OfflinePlayer) sender : VersionManager.getOfflinePlayer(args[0], false);
            if (target == null) {
                MobAttribute.getData(args[0]).forEach(it -> {
                    parseOwner.set(it.owner);
                    sender.sendMessage("怪物 "+it.getDisplay() + " UUID: "+it.getUuid());
                    sender.sendMessage(ChatColor.GOLD + "基本属性:");
                    for (Map.Entry<String, Double> a : it.map.entrySet()) {
                        sender.sendMessage("    key: " + a.getKey() + " value: " + a.getValue());
                    }
                    sender.sendMessage(ChatColor.GOLD + "限时属性:");
                    for (Map.Entry<String, Double> a : it.timerMap.entrySet()) {
                        sender.sendMessage("    key: " + a.getKey() + " value: " + a.getValue());
                    }
                    sender.sendMessage(ChatColor.GOLD + "源属性:");
                    for (Map.Entry<String, HashMap<String, Double>> a : it.temp.entrySet()) {
                        sender.sendMessage("  属性源: "+a.getKey());
                        for (Map.Entry<String, Double> it2 : a.getValue().entrySet()) {
                            sender.sendMessage("    key: " + it2.getKey() + " value: " + it2.getValue());
                        }
                    }
                });
                if (parseOwner.get() == null) {
                    return;
                }
            }
            PlayerData data;
            if (parseOwner.get() != null) {
                data = parseOwner.get();
                target = data.getPlayer();
            } else {
                data = SkillAPI.getPlayerData(target.getUniqueId());
            }
            if (data == null) {
                sender.sendMessage("玩家数据未加载...");
                return;
            }
            cmd.sendMessage(sender, TITLE, ChatColor.DARK_GRAY + "--" + ChatColor.DARK_GREEN + " {player} " + ChatColor.DARK_GRAY + "-----------", Filter.PLAYER.setReplacement(target.getName()));
            String separator = cmd.getMessage(SEPARATOR, ChatColor.DARK_GRAY + "----------------------------");
            boolean first = true;
            sender.sendMessage(ChatColor.GOLD + "基本属性:");
            for (Map.Entry<String, Integer> it : data.getAttributeData().entrySet()) {
                sender.sendMessage("    key: " + it.getKey() + " value: " + it.getValue());
            }
            sender.sendMessage(ChatColor.GOLD + "额外属性:");
            for (Map.Entry<String, Double> it : data.bonusAttrib.entrySet()) {
                sender.sendMessage("    key: " + it.getKey() + " value: " + it.getValue());
            }
            sender.sendMessage(ChatColor.GOLD + "临时属性:");
            for (Map.Entry<String, ConcurrentHashMap<String, Integer>> it : data.addAttrib.entrySet()) {
                sender.sendMessage("  属性源: "+it.getKey());
                for (Map.Entry<String, Integer> it2 : it.getValue().entrySet()) {
                    sender.sendMessage("    key: " + it2.getKey() + " value: " + it2.getValue());
                }
            }
            // nf start
            sender.sendMessage(ChatColor.GOLD + "NeonFlash:");
            AttributePlayer attributePlayer = me.neon.flash.attribute.AttributeManager.INSTANCE.getAttributePlayer().get(target.getUniqueId());
            if (attributePlayer != null) {
                sender.sendMessage("  前台属性: ");
                for (Map.Entry<String, Double> entry : attributePlayer.getAttributes().entrySet()) {
                    sender.sendMessage("    key: " + entry.getKey() + " value: " + entry.getValue());
                }
            }

            // nf end
            for (String group : SkillAPI.getGroups()) {
                PlayerClass c = data.getClass(group);
                // Separator message if not the first group
                if (first) {
                    first = false;
                } else {
                    sender.sendMessage(separator);
                }

                // Compose the message
                cmd.sendMessage(sender, CATEGORY, ChatColor.GOLD + "{group}" + ChatColor.GRAY + ": ", RPGFilter.GROUP.setReplacement(TextFormatter.format(group)));
                PlayerClass profession = data.getClass(group);
                if (profession == null) {
                    cmd.sendMessage(sender, NO_CLASS, ChatColor.GRAY + "Not Professed");
                } else {
                    cmd.sendMessage(sender, PROFESSION, ChatColor.AQUA + "Lv{level} " + ChatColor.DARK_GREEN + "{profession}", RPGFilter.LEVEL.setReplacement(profession.getLevel() + ""), RPGFilter.PROFESSION.setReplacement(profession.getData().getName()));
                    cmd.sendMessage(sender, EXP, ChatColor.AQUA + "Exp " + ChatColor.DARK_GREEN + "{exp}", RPGFilter.EXP.setReplacement((int) profession.getExp() + "/" + profession.getRequiredExp()));
                }
            }
            cmd.sendMessage(sender, END, ChatColor.DARK_GRAY + "----------------------------");
        }

        // Console doesn't have profession options
        else
        {
            cmd.sendMessage(sender, NEEDS_ARGS, ChatColor.RED + "A player name is required from the console");
        }
    }
}
