/**
 * SkillAPI
 * com.sucy.mechanic.dynamic.skill.ItemMechanic
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
package com.sucy.skill.dynamic.mechanic;

import com.rit.sucy.text.TextFormatter;
import com.sucy.skill.api.enums.ArmorType;
import com.sucy.skill.api.skills.SkillContext;
import me.neon.core.listener.BasicListener;
import me.neon.core.service.Pair;
import me.neon.core.utils.NeonNMSUtils;
import me.neon.flash.api.NeonFlashAPI;
import me.neon.flash.api.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;

/**
 * Gives an item to each player target
 */
public class ItemPacketMechanic extends MechanicComponent {

    private static final String NeonFlash = "neonFlash";
    private static final String SLOT = "slot";
    private static final String SECONDS = "delay";
    private static final ItemStack AIR = new ItemStack(Material.AIR);

    private static final String MATERIAL = "material";
    private static final String NAME     = "name";
    private static final String AMOUNT = "amount";
    private static final String DATA     = "data";
    private static final String BYTE     = "byte";


    @Override
    public String getKey() {
        return "item packet";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {

        String neonFlash = settings.getString(NeonFlash, "").toUpperCase();
        ArmorType slot = ArmorType.valueOf(settings.getString(SLOT).toUpperCase());
        double seconds = parseValues(caster, SECONDS, level, 2.0);

        Material material;
        try {
            String mat = settings.getString(MATERIAL, "").toUpperCase();
            if (mat.isEmpty()) {
                material = null;
            } else material = Material.valueOf(mat);
        } catch (Exception ex) {
            material = null;
        }
        if (material == null && neonFlash.isEmpty()) return false;

        String name = TextFormatter.colorString(settings.getString(NAME, ""));
        int amount = settings.getInt(AMOUNT, 1);
        int durability = settings.getInt(DATA, 0);
        int data = settings.getInt(BYTE, 0);


        ItemStack item = null;
        if (material != null) {
            item = new ItemStack(material, amount, (short) durability, (byte) data);
            ItemMeta meta = item.getItemMeta();
            if (!name.isEmpty()) {
                meta.setDisplayName(name);
            }
            item.setItemMeta(meta);
        } else {
            Item i = NeonFlashAPI.INSTANCE.getItemHandler().getItem(neonFlash);
            if (i != null) {
                item = i.buildItemStack(null);
            }
        }
        if (item == null) return false;

        for (LivingEntity target : targets) {
            if (target instanceof Player) {
                NeonNMSUtils.sendEquipment(
                        BasicListener.players,
                        target.getEntityId(),
                        new Pair<>(ArmorType.matchSlot(slot), item)
                );
            }
        }

        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("SkillAPI"),
                () -> {
                    for (LivingEntity target : targets) {
                        if (target instanceof Player) {
                            NeonNMSUtils.sendEquipment(
                                    BasicListener.players,
                                    target.getEntityId(),
                                    new Pair<>(ArmorType.matchSlot(slot), ((Player) target).getInventory().getItem(ArmorType.matchSlot(slot)))
                            );
                        }
                    }
                },
                (long) (seconds * 20)
        );
        return !targets.isEmpty();
    }
}
