package com.sucy.skill.api.enums;

import me.neon.libs.util.item.ItemUtilsKt;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.enums
 *
 * @author 老廖
 * @since 2024/5/30 13:19
 */
public enum ArmorType {

    HAND(-1),

    OFF_HAND(-1),

    HELMET(5),

    CHEST_PLATE(6),

    LEGGINGS(7),

    BOOTS(8);


    private final int slot;


    ArmorType(int slot) {
        this.slot = slot;
    }

    @Nullable
    public static ItemStack matchItemStack(PlayerInventory inventory, ArmorType type) {
        switch (type) {
            case HAND: {
                return inventory.getItemInMainHand();
            }
            case OFF_HAND: {
                return inventory.getItemInOffHand();
            }
            case HELMET: {
                return inventory.getHelmet();
            }
            case CHEST_PLATE: {
                return inventory.getChestplate();
            }
            case LEGGINGS: {
                return inventory.getLeggings();
            }
            default: {
                return inventory.getBoots();
            }
        }
    }

    @NotNull
    public static EquipmentSlot matchSlot(ArmorType type) {
        switch (type) {
            case HAND: {
                return EquipmentSlot.HAND;
            }
            case OFF_HAND: {
                return EquipmentSlot.OFF_HAND;
            }
            case HELMET: {
                return EquipmentSlot.HEAD;
            }
            case CHEST_PLATE: {
                return EquipmentSlot.CHEST;
            }
            case LEGGINGS: {
                return EquipmentSlot.LEGS;
            }
            default: {
                return EquipmentSlot.FEET;
            }
        }
    }

    @Nullable
    public static ArmorType matchType(ItemStack itemStack) {
        if (ItemUtilsKt.isAir(itemStack)) {
            return null;
        } else {
            String type = itemStack.getType().name();
            if (!type.endsWith("_HELMET") && !type.endsWith("_SKULL") && !type.endsWith("PLAYER_HEAD")) {
                if (!type.endsWith("_CHESTPLATE") && !type.endsWith("ELYTRA")) {
                    if (type.endsWith("_LEGGINGS")) {
                        return LEGGINGS;
                    } else {
                        return type.endsWith("_BOOTS") ? BOOTS : null;
                    }
                } else {
                    return CHEST_PLATE;
                }
            } else {
                return HELMET;
            }
        }
    }

    public int getSlot() {
        return this.slot;
    }

}
