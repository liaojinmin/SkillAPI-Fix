package com.sucy.skill.data.io;

import com.alibaba.fastjson2.JSONObject;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.player.*;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.log.Logger;
import com.sucy.skill.manager.ComboManager;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Map;

/**
 * SkillAPI-Fix
 * com.sucy.skill.data.io
 *
 * @author 老廖
 * @since 2023/9/28 19:48
 */
public class AccountAgent {

    private final JSONObject jsonObject;

    public AccountAgent(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public PlayerAccounts adpPlayerAccount(OfflinePlayer player) {
        PlayerAccounts data = new PlayerAccounts(player);
        JSONObject accounts = (JSONObject)jsonObject.get(IOManager.ACCOUNTS);

        if (accounts == null) {
            data.getActiveData().endInit();
            return data;
        }

        for (Map.Entry<String, Object> entry : accounts.entrySet()) {
            JSONObject account = (JSONObject) entry.getValue();

            PlayerData playerData = data.getData(Integer.parseInt(entry.getKey()), player, true);

            // 加载职业
            JSONObject classes = account.getJSONObject(IOManager.CLASSES);
            if (classes != null) {
                for (Map.Entry<String, Object> entry1 : classes.entrySet()) {
                    RPGClass rpgClass = SkillAPI.getClass(entry1.getKey());
                    if (rpgClass != null) {
                        JSONObject c1 = (JSONObject) entry1.getValue();
                        PlayerClass c2 = playerData.setClass(rpgClass);
                        int levels = c1.getIntValue(IOManager.LEVEL);
                        if (levels > 0) {
                            c2.setLevel(levels);
                        }
                        c2.setPoints(c1.getIntValue(IOManager.POINTS));
                        if (c1.containsKey("total-exp")) {
                            c2.setExp(c1.getDouble("total-exp") - c2.getTotalExp());
                        } else  {
                            c2.setExp(c1.getDouble(IOManager.EXP));
                        }
                     //   System.out.println("---=== LOAD " + player.getName() + "===---");
                     //   System.out.println("    CLASSES -> "+c2.getData().getName());
                     //   System.out.println("    LEVEL -> "+c2.getLevel());
                    //    System.out.println("    POINTS -> "+c2.getPoints());
                    //    System.out.println("    EXP -> "+c2.getExp());
                    }
                }
            }
            // 加载 技能
            JSONObject skills = account.getJSONObject(IOManager.SKILLS);
            if (skills != null) {
                for (Map.Entry<String, Object> entry1 : skills.entrySet()) {
                    PlayerSkill playerSkill = playerData.getSkill(entry1.getKey());
                    if (playerSkill != null) {
                        JSONObject s1 = (JSONObject) entry1.getValue();
                        playerSkill.setLevel(s1.getIntValue(IOManager.LEVEL));
                        playerSkill.setPoints(s1.getIntValue(IOManager.POINTS));
                        playerSkill.addCooldown(s1.getIntValue(IOManager.COOLDOWN, 0));
                    }
                }
            }

            // Load skill bar
            if (SkillAPI.getSettings().isSkillBarEnabled() || SkillAPI.getSettings().isUsingCombat()) {
                JSONObject skillBar = account.getJSONObject(IOManager.SKILL_BAR);
                PlayerSkillBar bar = playerData.getSkillBar();
                if (skillBar != null && bar != null) {
                    for (Map.Entry<String, Object> entry1 : skillBar.entrySet()) {
                        final boolean[] locked = SkillAPI.getSettings().getLockedSlots();
                        if (entry1.getKey().equals(IOManager.SLOTS)) {

                            for (int i = 0; i < 9; i++)
                                if (!bar.isWeaponSlot(i) && !locked[i])
                                    bar.getData().remove(i + 1);
                            final List<String> slots = skillBar.getList(IOManager.SLOTS, String.class);
                            if (slots != null) {
                                for (final String slot : slots) {
                                    int i = Integer.parseInt(slot);
                                    if (!locked[i - 1])
                                        bar.getData().put(i, IOManager.UNASSIGNED);
                                }
                            }
                        } else if (SkillAPI.getSkill(entry1.getKey()) != null) {
                            bar.getData().put(skillBar.getIntValue(entry1.getKey()), entry1.getKey());
                        }
                    }
                    bar.applySettings();
                }
            }

            // 加载 combos
            if (SkillAPI.getSettings().isCustomCombosAllowed()) {
                JSONObject combos = account.getJSONObject(IOManager.COMBOS);
                PlayerCombos comboData = playerData.getComboData();
                if (combos != null && comboData != null) {
                    ComboManager cm = SkillAPI.getComboManager();
                    for (Map.Entry<String, Object> entry1 : combos.entrySet()) {
                        Skill skill = SkillAPI.getSkill(entry1.getKey());

                        if (playerData.hasSkill(entry1.getKey()) && skill != null && skill.canCast()) {
                            int combo = cm.parseCombo(combos.getString(entry1.getKey()));
                            if (combo == -1) {
                                Logger.invalid("Invalid skill combo: " + combos.getString(entry1.getKey()));
                            } else {
                                comboData.setSkill(skill, combo);
                            }
                        }
                    }
                }
            }

            // 加载属性 attributes
            if (SkillAPI.getSettings().isAttributesEnabled()) {
                playerData.setAttribPoints(account.getIntValue(IOManager.ATTRIB_POINTS, 0));
                JSONObject attribs = account.getJSONObject(IOManager.ATTRIBS);
                if (attribs != null) {
                    for (String key : attribs.keySet()) {
                        playerData.getAttributeData().put(key, attribs.getIntValue(key));
                    }
                }
            }

            // 加载 cast bars
            if (SkillAPI.getSettings().isCastEnabled()) {
                playerData.getCastBars().reset();
                playerData.getCastBars().load(account.getJSONObject(IOManager.HOVER), true);
                playerData.getCastBars().load(account.getJSONObject(IOManager.INSTANT), true);
            }

            // 加载 HUNGER
            playerData.setHungerValue(account.getDoubleValue(IOManager.HUNGER));

            // 加载扩展数据 Extra data TODO
            if (account.containsKey(IOManager.EXTRA)) {
                Logger.bug("加载扩展数据 Extra data 未实现反序列化");
            }

            playerData.endInit();

            // 加载 binds
            JSONObject binds = account.getJSONObject(IOManager.BINDS);
            if (binds != null) {
                for (String bindKey : binds.keySet()) {
                    playerData.bind(Material.valueOf(bindKey), playerData.getSkill(binds.getString(bindKey)));
                }
            }
        }
        data.setAccount(jsonObject.getInteger(IOManager.ACTIVE));
        data.getActiveData().setLastHealth(jsonObject.getDouble(IOManager.HEALTH));
        data.getActiveData().setMana(jsonObject.getDouble(IOManager.MANA));
        return data;
    }
}
