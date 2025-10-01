
package com.sucy.skill.api.event;

import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.player.PlayerSkill;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerCastSkillEndEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private PlayerData playerData;

    private PlayerSkill skill;

    private Player      player;

    public PlayerCastSkillEndEvent(PlayerData playerData, PlayerSkill skill, Player player) {
        this.playerData = playerData;
        this.skill = skill;
        this.player = player;
    }

    public Player getPlayer()
    {
        return player;
    }

    public PlayerData getPlayerData()
    {
        return playerData;
    }

    public PlayerSkill getSkill()
    {
        return skill;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
