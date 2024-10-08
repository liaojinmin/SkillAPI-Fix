package com.sucy.skill.api.attribute;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.mob.MobAttribute;
import com.sucy.skill.api.attribute.mob.MobAttributeData;
import com.sucy.skill.api.event.AttributeEntityAddEvent;
import com.sucy.skill.api.event.TempAttributeAddEvent;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.dynamic.EffectComponent;
import com.sucy.skill.manager.AttributeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeAPI {

    public static final String FX_SKILL_API_MASTER = "FX_SKILL_API_MASTER";


    /**
     * 获取实体属性 所有实体获取属性环节都会走这个方法
     * 来实现给MM怪物等获取属性或者是兼容AP等插件
     *
     * @param entity 获取属性的实体
     * @param key    属性名
     * @return 属性值
     */
    public static double getAttribute(LivingEntity entity, String key) {
        if (entity == null || entity.isDead()) {
            return 0.0;
        }
        // 去色
        key = ChatColor.stripColor(key).toLowerCase();
        if (entity instanceof Player) {
            PlayerData data = SkillAPI.getPlayerData(entity.getUniqueId());
            if (data != null) {
                return Math.max(0, data.getAttribute(key));
            } else {
                return 0;
            }
        }
        MobAttributeData mobAttributeData = MobAttribute.getData(entity.getUniqueId(), true);
        return mobAttributeData.getAttribute(key);
    }

    /**
     * 给实体增加属性 会自动判断是玩家还是Mob
     *
     * @param entity    施法者
     * @param source    源
     * @param attribute 属性
     * @param value     值
     */
    public static void addAttribute(LivingEntity entity, String source, String attribute, Integer value) {
        if (entity instanceof Player) {
            PlayerData playerData = SkillAPI.getPlayerData(entity.getUniqueId());
            if (playerData == null) {
                return;
            }
            playerData.addAttrib.computeIfAbsent(source, key -> new ConcurrentHashMap<>()).put(attribute, value);
            return;
        }
        MobAttributeData mobAttributeData = MobAttribute.getData(entity.getUniqueId(), true);
        mobAttributeData.tempAddAttribute(source, attribute, value);
    }

    /**
     * 删除属性源
     *
     * @param entity 目标
     * @param source 源
     */
    public static void clearSource(LivingEntity entity, String source) {
        if (entity instanceof Player) {
            PlayerData playerData = SkillAPI.getPlayerData(entity.getUniqueId());
            if (playerData == null) {
                return;
            }
            playerData.addAttrib.remove(source);
            return;
        }
        MobAttributeData mobAttributeData = MobAttribute.getData(entity.getUniqueId(), true);
        mobAttributeData.tempRemove(source);
    }

    /**
     * Scales a dynamic skill's value using global modifiers
     *
     * @param component component holding the value
     * @param key       key of the value
     * @param value     unmodified value
     * @return the modified value
     */
    public static double scaleDynamic(LivingEntity entity, EffectComponent component, String key, double value) {
        final AttributeManager manager = SkillAPI.getAttributeManager();
        if (manager == null) {
            return value;
        }

        final List<AttributeManager.Attribute> matches = manager.forComponent(component, key);
        if (matches == null) {
            return value;
        }

        for (final AttributeManager.Attribute attribute : matches) {
            // value
            double amount = getAttribute(entity, attribute.getKey());
            if (amount > 0) {
                value = attribute.modify(component, key, value, amount);
            }
        }
        return value;
    }

    /**
     * Scales a stat value using the player's attributes
     *
     * @param stat  stat key
     * @param value base value
     * @return modified value
     */
    public static double scaleStat(LivingEntity entity, final String stat, final double value) {
        final AttributeManager manager = SkillAPI.getAttributeManager();
        if (manager == null) {
            return value;
        }

        final List<AttributeManager.Attribute> matches = manager.forStat(stat);
        if (matches == null) {
            return value;
        }

        double modified = value;
        for (final AttributeManager.Attribute attribute : matches) {
            double amount = getAttribute(entity, attribute.getKey());
            if (amount > 0) {
                modified = attribute.modifyStat(stat, modified, amount);
            }
        }
        return modified;
    }


    /**吗，
     * 广播属性增加事件 让拓展器也获得监听。
     * proSkillapi 也从event执行代码
     *
     * @param entity    目标
     * @param attribute 属性ID
     * @param value     增加的值 仅作为bus
     */
    public static TempAttributeAddEvent tempAttribute(LivingEntity entity, String attribute, double value, long tick) {
        TempAttributeAddEvent event = new TempAttributeAddEvent(entity, attribute, value, tick);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * 广播非玩家增加属性事件
     */
    public static AttributeEntityAddEvent attributeEntityAdd(LivingEntity entity, String attribute, double value) {
        AttributeEntityAddEvent event = new AttributeEntityAddEvent(entity, attribute, (int) value);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

}
