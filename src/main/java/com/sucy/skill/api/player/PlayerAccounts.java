/**
 * SkillAPI
 * com.sucy.player.api.skill.PlayerAccounts
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
package com.sucy.skill.api.player;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.version.VersionPlayer;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.PlayerAccountChangeEvent;
import com.sucy.skill.data.io.IOManager;
import com.sucy.skill.listener.AttributeListener;
import com.sucy.skill.log.Logger;
import com.sucy.skill.manager.ClassBoardManager;
import com.sucy.skill.manager.ComboManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the collection of accounts owned by a single player.
 * Most of the time, this class won't be used by other plugins as
 * you can skip directly to a player's active data using the
 * SkillAPI.getPlayerData methods. This would be if you want
 * to extend functionality for handling the inactive accounts.
 */
public class PlayerAccounts {
    private final HashMap<Integer, PlayerData> classData = new HashMap<Integer, PlayerData>();

    private int           active;
    private OfflinePlayer player;

    /**
     * Initializes a new container for player account data.
     * This shouldn't be used by other plugins as the API
     * provides one for each player already.
     *
     * @param player player to store data for
     */
    public PlayerAccounts(OfflinePlayer player) {
        this.player = player;

        PlayerData data = new PlayerData(player, true);
        classData.put(1, data);
        active = 1;
    }

    /**
     * Retrieves the active account ID for the player
     *
     * @return active account ID
     */
    public int getActiveId() {
        return active;
    }

    /**
     * Retrieves the active account data for the player
     *
     * @return active account data
     */
    public PlayerData getActiveData() {
        return classData.get(active);
    }

    /**
     * Gets the Bukkit player object for the owner of the data
     *
     * @return Bukkit player object or null if offline/dead
     */
    public Player getPlayer() {
        return player.getPlayer();
    }

    /**
     * Gets the Bukkit offline player object for the owner of the data
     *
     * @return Bukkit offline player object
     */
    public OfflinePlayer getOfflinePlayer() {
        return player;
    }

    /**
     * Gets the name of the owner of the data
     *
     * @return owner's name
     */
    public String getPlayerName() {
        return player.getName();
    }

    /**
     * Retrieves the max amount of accounts the owner can use
     *
     * @return available account number
     */
    public int getAccountLimit() {
        return SkillAPI.getSettings().getMaxAccounts(getPlayer());
    }

    /**
     * Checks whether or not there is any data for the given account ID. If
     * the player has not switched to the account, there will be no data
     * unless the setting to initialize one account for each class is enabled.
     *
     * @param id account ID
     *
     * @return true if data exists, false otherwise
     */
    public boolean hasData(int id) {
        return classData.containsKey(id);
    }

    /**
     * Gets the account data by ID for the owner
     *
     * @param id account ID
     *
     * @return account data or null if not found
     */
    public PlayerData getData(int id) {
        return classData.get(id);
    }

    /**
     * Gets the account data by ID for the owner. If no data
     * exists under the given ID, new data is created as long
     * as the ID is a positive integer (not necessarily in
     * bounds for the player's allowed accounts).
     *
     * @param id     account ID
     * @param player offline player reference
     * @param init   whether or not the data is being initialized
     *
     * @return account data or null if invalid id or player
     */
    public PlayerData getData(int id, OfflinePlayer player, boolean init) {
        if (!hasData(id) && id > 0 && player != null) {
            classData.put(id, new PlayerData(player, init));
        }
        return classData.get(id);
    }

    /**
     * Retrieves all of the data for the owner. Modifying this map will
     * alter the player's actual data.
     *
     * @return all account data for the player
     */
    public HashMap<Integer, PlayerData> getAllData() {
        return classData;
    }

    /**
     * Switches the active account for the player by ID. This will not accept
     * IDs outside the player's account limits. If the player is offline or
     * dead, this will not do anything.
     *
     * @param id ID of the account to switch to
     */
    public void setAccount(int id) {
        setAccount(id, true);
    }

    /**
     * Switches the active account for the player by ID. This will not accept
     * IDs outside the player's account limits. If the player is offline or
     * dead, this will not do anything.
     *
     * @param id    ID of the account to switch to
     * @param apply whether or not to apply the switch
     */
    public void setAccount(int id, boolean apply) {
        Player player = getPlayer();
        if (player == null || id == active || !apply) {
            active = id;
            return;
        }
        if (id <= getAccountLimit() && id > 0 && !classData.containsKey(id)) {
            classData.put(id, new PlayerData(player, false));
        }
        if (classData.containsKey(id)) {
            PlayerAccountChangeEvent event = new PlayerAccountChangeEvent(this, active, id);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            if (SkillAPI.getSettings().isWorldEnabled(player.getWorld())) {
                ClassBoardManager.clear(new VersionPlayer(player));
                getActiveData().stopPassives(player);
                AttributeListener.clearBonuses(player);
                getActiveData().clearBonuses();
                active = event.getNewID();
                getActiveData().startPassives(player);
                getActiveData().updateScoreboard();
                getActiveData().updateHealthAndMana(player);
                AttributeListener.updatePlayer(getActiveData());
                if (getActiveData().hasClass() && SkillAPI.getSettings().isSkillBarEnabled() && !SkillAPI.getSettings()
                        .isUsingCombat()) {
                    getActiveData().getSkillBar().setup(player);
                }
                getActiveData().getEquips().update(player);
            } else {
                active = event.getNewID();
            }
        }
    }

    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put(IOManager.LIMIT, getAccountLimit());
            json.put(IOManager.ACTIVE, getActiveId());
            json.put(IOManager.HEALTH, getActiveData().getLastHealth());
            json.put(IOManager.MANA, getActiveData().getMana());

            JSONObject accounts = new JSONObject();
            for (Map.Entry<Integer, PlayerData> entry : getAllData().entrySet()) {
                JSONObject account = new JSONObject();
                PlayerData playerData = entry.getValue();

                // 保存职业
                JSONObject classes = new JSONObject();
                for (PlayerClass c : playerData.getClasses()) {
                    JSONObject c1 = new JSONObject();
                  //  System.out.println("---=== UNLOAD "+getPlayerName()+"===---");
                 //   System.out.println("    CLASSES -> "+c.getData().getName());
                    c1.put(IOManager.LEVEL, c.getLevel());
                  //  System.out.println("    LEVEL -> "+c.getLevel());
                    c1.put(IOManager.POINTS, c.getPoints());
                //    System.out.println("    POINTS -> "+c.getPoints());
                    c1.put(IOManager.EXP, c.getExp());
                //    System.out.println("    EXP -> "+c.getExp());
                    classes.put(c.getData().getName(), c1);
                }
                account.put(IOManager.CLASSES, classes);


                // 保存技能
                JSONObject skill = new JSONObject();
                for (PlayerSkill s : playerData.getSkills()) {
                    JSONObject s1 = new JSONObject();
                    s1.put(IOManager.LEVEL, s.getLevel());
                    s1.put(IOManager.POINTS, s.getPoints());
                    if (s.isOnCooldown()) {
                        s1.put(IOManager.COOLDOWN, s.getCooldown());
                    }
                    skill.put(s.getData().getName(), s1);
                }
                account.put(IOManager.SKILLS, skill);

                // Save binds
                JSONObject bind = new JSONObject();
                for (Map.Entry<Material, PlayerSkill> b : playerData.getBinds().entrySet()) {
                    if (b.getKey() == null || b.getValue() == null) continue;
                    bind.put(b.getKey().name(), b.getValue().getData().getName());
                }
                account.put(IOManager.BINDS, bind);

                // Save skill bar
                if ((SkillAPI.getSettings().isSkillBarEnabled() || SkillAPI.getSettings().isUsingCombat()) && playerData.getSkillBar() != null) {
                    JSONObject skillBar = new JSONObject();
                    PlayerSkillBar bar = playerData.getSkillBar();
                    skillBar.put(IOManager.ENABLED, bar.isEnabled());
                    skillBar.put(IOManager.SLOTS, new ArrayList<>(bar.getData().keySet()));
                    for (Map.Entry<Integer, String> slotEntry : bar.getData().entrySet()) {
                        if (slotEntry.getValue().equals(IOManager.UNASSIGNED)) {
                            continue;
                        }
                        skillBar.put(slotEntry.getValue(), slotEntry.getKey());
                    }
                    account.put(IOManager.SKILL_BAR, skillBar);
                }

                // Save combos
                if (SkillAPI.getSettings().isCustomCombosAllowed()) {
                    JSONObject combos = new JSONObject();
                    PlayerCombos comboData = playerData.getComboData();
                    if (comboData != null) {
                        ComboManager cm = SkillAPI.getComboManager();
                        HashMap<Integer, String> comboMap = comboData.getSkillMap();
                        for (Map.Entry<Integer, String> combo : comboMap.entrySet()) {
                            combos.put(combo.getValue(), cm.getSaveString(combo.getKey()));
                        }
                        account.put(IOManager.COMBOS, combos);
                    }
                }

                // Save attributes
                if (SkillAPI.getSettings().isAttributesEnabled()) {
                    account.put(IOManager.ATTRIB_POINTS, playerData.getAttributePoints());
                    if (!playerData.getAttributeData().isEmpty()) {
                        JSONObject attribs = new JSONObject();
                        attribs.putAll(playerData.getAttributeData());
                        account.put(IOManager.ATTRIBS, attribs);
                    }
                }

                // Save cast bars
                if (SkillAPI.getSettings().isCastEnabled()) {
                    JSONObject ho = new JSONObject();
                    playerData.getCastBars().save(ho, true);
                    account.put(IOManager.HOVER, ho);
                    JSONObject in = new JSONObject();
                    playerData.getCastBars().save(in, false);
                    account.put(IOManager.INSTANT, in);
                }

                // save HUNGER
                account.put(IOManager.HUNGER, playerData.getHungerValue());

                // 扩展数据
                /*
                if (playerData.getExtraData().size() > 0) {
                    account.put(IOManager.EXTRA, playerData.getExtraData());
                }
                 */

                // 添加职业
                accounts.put(String.valueOf(entry.getKey()), account);
            }
            json.put(IOManager.ACCOUNTS, accounts);
            return json.toJSONString();
        } catch (Exception ex) {
            Logger.bug("Failed to save player data for " + getPlayer().getName());
            ex.printStackTrace();
            return null;
        }
    }
}
