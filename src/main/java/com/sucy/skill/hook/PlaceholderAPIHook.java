package com.sucy.skill.hook;

import com.rit.sucy.mobs.MobManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.dynamic.DynamicSkill;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * SkillAPI © 2018
 * com.sucy.hook.skill.PlaceholderAPIHook
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {
    private static final Map<String, BiFunction<PlayerData, String, String>> PLACEHOLDERS = new HashMap<>();

    public static void init() {
        PLACEHOLDERS.put("attrib_points", (p, u) -> Integer.toString(p.getAttributePoints()));
        PLACEHOLDERS.put("attrib_spent:", (p, attribute) -> Integer.toString(p.getInvestedAttribute(attribute)));
        PLACEHOLDERS.put("attrib_total:", (p, attribute) -> Integer.toString(p.getAttribute(attribute)));
        PLACEHOLDERS.put("exp", (p, u) -> getExp(p.getMainClass()));
        PLACEHOLDERS.put("exp:", (p, group) -> getExp(p.getClass(group)));
        PLACEHOLDERS.put("exp_total", (p, u) -> getTotalExp(p.getMainClass()));
        PLACEHOLDERS.put("exp_total:", (p, group) -> getTotalExp(p.getClass(group)));
        PLACEHOLDERS.put("exp_left", (p, u) -> getRemainingExp(p.getMainClass()));
        PLACEHOLDERS.put("exp_left:", (p, group) -> getRemainingExp(p.getClass(group)));
        PLACEHOLDERS.put("exp_req", (p, u) -> getRequiredExp(p.getMainClass()));
        PLACEHOLDERS.put("exp_req:", (p, group) -> getRequiredExp(p.getClass(group)));
        PLACEHOLDERS.put("level", (p, u) -> getLevel(p.getMainClass()));
        PLACEHOLDERS.put("level:", (p, group) -> getLevel(p.getClass(group)));
        PLACEHOLDERS.put("mana", (p, u) -> Integer.toString((int)p.getMana()));
        PLACEHOLDERS.put("mana_max", (p, u) -> Integer.toString((int)p.getMaxMana()));
        PLACEHOLDERS.put("mana_name", (p, u) -> getManaName(p.getMainClass()));
        PLACEHOLDERS.put("mana_name:", (p, group) -> getManaName(p.getClass(group)));
        PLACEHOLDERS.put("prefix", (p, u) -> getPrefix(p.getMainClass()));
        PLACEHOLDERS.put("prefix:", (p, className) -> getPrefix(p.getClass(className)));
        PLACEHOLDERS.put("skill_level:", (p, skill) -> Integer.toString(p.getSkill(skill).getLevel()));
        PLACEHOLDERS.put("skill_points", (p, u) -> getSkillPoints(p.getMainClass()));
        PLACEHOLDERS.put("skill_points:", (p, group) -> getSkillPoints(p.getClass(group)));
        PLACEHOLDERS.put("value:", (p, key) -> formats(DynamicSkill.getCastData(p.getPlayer())));
        new PlaceholderAPIHook().register();
    }
    // %sapi_mana%
    public String getIdentifier() {
        return "sapi";
    }

    public String getPlugin() {
        return "SkillAPI";
    }

    public String getAuthor() {
        return "Eniripsa96";
    }

    public String getVersion() {
        return "1.0";
    }

    public String onPlaceholderRequest(Player player, String id) {
        int paramIndex = id.indexOf(58) + 1;
        String param;
        String key;
        if (paramIndex > 0) {
            param = id.substring(paramIndex);
            key = id.substring(0, paramIndex);
        } else {
            key = id;
            param = null;
        }

        BiFunction<PlayerData, String, String> placeholder = PLACEHOLDERS.get(key);
        PlayerData playerData = SkillAPI.getPlayerData(player.getUniqueId());
        if (playerData == null) {
            return "";
        }
        return placeholder == null ? "" : placeholder.apply(playerData, param);
    }

    private static String getExp(PlayerClass playerClass) {
        return Integer.toString((int)(playerClass == null ? 0.0 : playerClass.getExp()));
    }

    private static String getTotalExp(PlayerClass playerClass) {
        return Integer.toString((int)(playerClass == null ? 0.0 : playerClass.getTotalExp()));
    }

    private static String getRequiredExp(PlayerClass playerClass) {
        return Integer.toString(playerClass == null ? SkillAPI.getSettings().getRequiredExp(1) : playerClass.getRequiredExp());
    }

    private static String getRemainingExp(PlayerClass playerClass) {
        return Integer.toString((int)(playerClass == null ? (double)SkillAPI.getSettings().getRequiredExp(1) : (double)playerClass.getRequiredExp() - playerClass.getExp()));
    }

    private static String getLevel(PlayerClass playerClass) {
        return Integer.toString(playerClass == null ? 0 : playerClass.getLevel());
    }

    private static String getSkillPoints(PlayerClass playerClass) {
        return Integer.toString(playerClass == null ? 0 : playerClass.getPoints());
    }

    private static String getPrefix(PlayerClass playerClass) {
        return playerClass == null ? "" : playerClass.getData().getPrefix();
    }

    private static String getManaName(PlayerClass playerClass) {
        return playerClass == null ? "" : playerClass.getData().getManaName();
    }

    private static String formats(Object value) {
        if (value instanceof Player) {
            return ((Player)value).getName();
        } else if (value instanceof LivingEntity) {
            return ((LivingEntity)value).isCustomNameVisible() ? ((LivingEntity)value).getCustomName() : MobManager.getName((LivingEntity)value);
        } else {
            return value instanceof Collection ? ((Collection)value).stream().map(PlaceholderAPIHook::formats).reduce((a, b) -> a + ", " + b).toString() : value.toString();
        }
    }

    public static String format(final String message, final Player player) {
        return PlaceholderAPI.setPlaceholders(player, message);
    }
}
