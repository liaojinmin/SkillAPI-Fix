package com.sucy.skill.listener;

import com.google.common.collect.ImmutableSet;
import com.rit.sucy.config.FilterType;
import com.rit.sucy.version.VersionManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.PlayerClassChangeEvent;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.language.ErrorNodes;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

/**
 * Listener that handles weapon item lore requirements
 */
public class ItemListener extends SkillAPIListener
{

    private boolean isMainhand(final ItemStack bow, final LivingEntity entity) {
        return !VersionManager.isVersionAtLeast(VersionManager.V1_9_0)
                || bow == entity.getEquipment().getItemInMainHand();
    }

    public static final Set<Material> ARMOR_TYPES = getArmorMaterials();

    private static Set<Material> getArmorMaterials() {
        final Set<String> armorSuffixes = ImmutableSet.of("BOOTS", "LEGGINGS", "CHESTPLATE", "HELMET");
        final ImmutableSet.Builder<Material> builder = ImmutableSet.builder();
        for (Material material : Material.values()) {
            final int index = material.name().lastIndexOf('_') + 1;
            final String suffix = material.name().substring(index);
            if (armorSuffixes.contains(suffix)) {
                builder.add(material);
            }
        }
        return builder.build();
    }

}
