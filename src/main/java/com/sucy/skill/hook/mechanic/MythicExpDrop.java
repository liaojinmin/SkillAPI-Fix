package com.sucy.skill.hook.mechanic;


import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.player.PlayerData;
import io.lumine.xikage.mythicmobs.adapters.AbstractPlayer;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.DropMetadata;
import io.lumine.xikage.mythicmobs.drops.IIntangibleDrop;
import io.lumine.xikage.mythicmobs.drops.IMessagingDrop;
import io.lumine.xikage.mythicmobs.io.ConfigManager;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mechanic
 *
 * @author 老廖
 * @since 2024/6/7 23:38
 */
public class MythicExpDrop extends Drop implements IIntangibleDrop, IMessagingDrop {

    private final PlaceholderString rewardMessage;

    public MythicExpDrop(String line, MythicLineConfig config) {
        super(line, config);
        this.rewardMessage = !ConfigManager.compatSkillAPIShowXPMessage
                ? null : PlaceholderString.of(ConfigManager.compatSkillAPIXPMessageFormat);
    }

    public MythicExpDrop(String line, MythicLineConfig config, double amount) {
        super(line, config, amount);
        this.rewardMessage = !ConfigManager.compatSkillAPIShowXPMessage
                ? null : PlaceholderString.of(ConfigManager.compatSkillAPIXPMessageFormat);
    }

    @Override
    public void giveDrop(AbstractPlayer target, DropMetadata metadata) {
        PlayerData playerData = SkillAPI.getPlayerData(BukkitAdapter.adapt(target));
        if (playerData != null) {
            playerData.giveExp(this.getAmount(), ExpSource.MOB);
        }
    }

    public String getRewardMessage(DropMetadata meta, double amount) {
        if (this.rewardMessage != null && ConfigManager.compatSkillAPIShowXPMessage) {
            String message = this.rewardMessage.get(meta);
            message = message.replace("<drops.skillapi>", String.format("%.2f", amount));
            message = message.replace("<drops.xp>", String.format("%.2f", amount));
            message = message.replace("<drop.amount>", String.format("%.2f", amount));
            return message;
        } else {
            return null;
        }
    }
}
