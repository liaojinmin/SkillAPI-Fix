
package com.sucy.skill.cmd;

import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.rit.sucy.version.VersionManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.player.PlayerSkill;
import com.sucy.skill.listener.AttributeListener;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CmdCooldown implements IFunction
{


    @Override
    public void execute(ConfigurableCommand cmd, Plugin plugin, CommandSender sender, String[] args) {
        // Disabled world
        if (sender instanceof Player && !SkillAPI.getSettings().isWorldEnabled(((Player) sender).getWorld()) && args.length == 0) {
            cmd.sendMessage(sender, "world-disabled", "&4You cannot use this command in this world");
        } else if (sender instanceof Player || args.length >= 1) {
            OfflinePlayer target = args.length == 0 ? (OfflinePlayer) sender : VersionManager.getOfflinePlayer(args[0], false);

            if (target == null) {
                cmd.sendMessage(sender, "not-player", ChatColor.RED + "That is not a valid player name");
                return;
            }
            Player caster = target.getPlayer();
            if (caster != null && args.length >= 2) {
                double value = Double.parseDouble(args[1]);

                PlayerData playerData = SkillAPI.getPlayerData(caster.getUniqueId());
                if (playerData == null) return;

                for (PlayerSkill data : playerData.getSkills()) {
                    data.subtractCooldown(value);
                }

            }
        }
    }
}
