package com.sucy.skill.data.io;

import com.sucy.skill.api.player.PlayerAccounts;
import org.bukkit.OfflinePlayer;

/**
 * SkillAPI-Fix
 * com.sucy.skill.data.io
 *
 * @author 老廖
 * @since 2023/9/27 4:18
 */
public class FakePlayer extends PlayerAccounts {
    public FakePlayer(OfflinePlayer player) {
        super(player);
    }
}
