package com.sucy.skill.gui;

import com.sucy.skill.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IconHolder
{
    ItemStack getIcon(PlayerData player);

    boolean isAllowed(Player player);
}
