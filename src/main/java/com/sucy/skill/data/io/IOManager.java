
package com.sucy.skill.data.io;

import com.alibaba.fastjson2.JSONObject;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.player.*;
import com.sucy.skill.log.Logger;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for managers that handle saving and loading player data
 */
public abstract class IOManager {
    public static final String
        ACCOUNT       = "account",

        HEALTH         = "health",

        MANA           = "mana",

        CLASSES        = "classes",

        SKILLS         = "skills",

        LEVEL          = "level",

        EXP            = "exp",

        POINTS         = "points",

        ATTRIBS        = "attribs",

        COOLDOWN       = "cd",

        HUNGER         = "hunger",

        ATTRIB_POINTS  = "attrib-points";

    /**
     * API reference
     */
    protected final SkillAPI api;

    /**
     * Initializes a new IO manager
     *
     * @param api SkillAPI reference
     */
    IOManager(SkillAPI api) {
        this.api = api;
    }

    /**
     * Loads data for the player
     *
     * @param player player to load for
     *
     * @return loaded player data
     */
    public abstract PlayerData loadData(OfflinePlayer player);

    /**
     * Saves the player's data
     *
     * @param data data to save
     */
    public abstract void saveData(PlayerData data);


    /**
     * Saves all player data
     */
    public abstract void saveAll();


    public PlayerData loadOfJson(OfflinePlayer player, JSONObject json) {
        PlayerData playerData = new PlayerData(player);
        JSONObject account = json.getJSONObject(ACCOUNT);
        if (account == null) {
            playerData.endInit();
            System.out.println("玩家 "+player.getName()+" 的数据为空, 正在返回默认值...");
            return playerData;
        }
        playerData.setLastHealth(json.getDouble(HEALTH));
        playerData.setMana(json.getDouble(MANA));
        // 加载职业
        JSONObject classes = account.getJSONObject(CLASSES);
        if (classes != null) {
            for (Map.Entry<String, Object> entry1 : classes.entrySet()) {
                RPGClass rpgClass = SkillAPI.getClass(entry1.getKey());
                if (rpgClass != null) {
                    JSONObject c1 = (JSONObject) entry1.getValue();
                    PlayerClass playerClass = playerData.setClass(rpgClass);
                    playerClass.setLevel(c1.getIntValue(LEVEL));
                    playerClass.setPoints(c1.getIntValue(POINTS));
                    playerClass.setExp(c1.getDouble(EXP));
                }
            }
        }
        // 加载技能
        JSONObject skills = account.getJSONObject(SKILLS);
        if (skills != null) {
            for (Map.Entry<String, Object> entry1 : skills.entrySet()) {
                PlayerSkill playerSkill = playerData.getSkill(entry1.getKey());
                if (playerSkill != null) {
                    JSONObject s1 = (JSONObject) entry1.getValue();
                    playerSkill.setLevel(s1.getIntValue(LEVEL));
                    playerSkill.setPoints(s1.getIntValue(POINTS));
                    playerSkill.addCooldown(s1.getIntValue(COOLDOWN, 0));
                }
            }
        }
        // 加载属性
        playerData.setAttribPoints(account.getIntValue(ATTRIB_POINTS, 0));
        JSONObject attribs = account.getJSONObject(ATTRIBS);
        if (attribs != null) {
            for (String key : attribs.keySet()) {
                playerData.getAttributeData().put(key, attribs.getIntValue(key));
            }
        }
        // 加载 HUNGER
        playerData.setHungerValue(account.getDoubleValue(HUNGER));
        playerData.endInit();
        return playerData;
    }


    public JSONObject saveOfJson(PlayerData playerData) {
        try {
            JSONObject json = new JSONObject();
            json.put(IOManager.HEALTH, playerData.getLastHealth());
            json.put(IOManager.MANA, playerData.getMana());

            JSONObject account = new JSONObject();

            // save HUNGER
            account.put(IOManager.HUNGER, playerData.getHungerValue());

            // 保存职业
            account.put(IOManager.CLASSES, new JSONObject() {{
                for (PlayerClass c : playerData.getClasses()) {
                    put(c.getData().getName(), new JSONObject() {{
                        put(IOManager.LEVEL, c.getLevel());
                        put(IOManager.POINTS, c.getPoints());
                        put(IOManager.EXP, c.getExp());
                    }});
                }
            }});

            // 保存技能
            account.put(IOManager.SKILLS, new JSONObject() {{
                for (PlayerSkill s : playerData.getSkills()) {
                    put(s.getData().getName(), new JSONObject() {{
                        put(IOManager.LEVEL, s.getLevel());
                        put(IOManager.POINTS, s.getPoints());
                        if (s.isOnCooldown()) {
                            put(IOManager.COOLDOWN, s.getCooldown());
                        }
                    }});
                }
            }});

            // 保存属性
            account.put(IOManager.ATTRIB_POINTS, playerData.getAttributePoints());
            account.put(IOManager.ATTRIBS, new JSONObject() {{
                putAll(playerData.getAttributeData());
            }});
            json.put(IOManager.ACCOUNT, account);
            return json;
        } catch (Exception ex) {
            Logger.bug("Failed to save player data for " + playerData.getPlayerName());
            ex.printStackTrace();
            return null;
        }
    }
}
