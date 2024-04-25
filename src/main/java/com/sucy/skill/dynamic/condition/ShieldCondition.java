package com.sucy.skill.dynamic.condition;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.condition
 *
 * @author 老廖
 * @since 2024/4/20 17:08
 */
public class ShieldCondition  extends ConditionComponent {

    private static final String TARGET = "target";

    public static boolean checkBlocking(Player player) {
        if (player == null) return false;
        if (player.isBlocking()) {
            ItemStack itemStack = player.getInventory().getItemInOffHand();
            if (itemStack == null || itemStack.getType() != Material.SHIELD) {
                itemStack = player.getInventory().getItemInMainHand();
            }
            return itemStack != null && itemStack.getType() == Material.SHIELD;
        }
        return false;
    }

    @Override
    public String getKey() {
        return "shield";
    }

    @Override
    boolean test(LivingEntity caster, int level, LivingEntity target) {
        final boolean tar = settings.getBool(TARGET, false);
        Player player;
        if (tar) {
            if (target instanceof Player) {
                player = (Player) target;
            } else player = null;
        } else {
            if (caster instanceof Player) {
                player = (Player) caster;
            } else player = null;
        }
        if (player != null && player.isBlocking()) {
            ItemStack itemStack = player.getInventory().getItemInOffHand();
            if (itemStack == null || itemStack.getType() != Material.SHIELD) {
                itemStack = player.getInventory().getItemInMainHand();
            }
            return itemStack != null && itemStack.getType() == Material.SHIELD;
        }
        return false;
    }
}
