package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.enums.ArmorType;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.hook.PlaceholderAPIHook;
import com.sucy.skill.hook.PluginChecker;
import com.sucy.skill.log.Logger;
import me.neon.libs.libraries.nbt.NBT;
import me.neon.libs.libraries.nbt.iface.ReadableNBT;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;

/**
 * SkillAPI © 2018
 * com.sucy.mechanic.dynamic.skill.ValuePlaceholderMechanic
 */
public class ValueMMoItemAttrMechanic extends MechanicComponent {
    private static final String KEY  = "key";
    private static final String NBT_KEY  = "nbt";
    private static final String SLOT = "slot";

    @Override
    public String getKey() {
        return "value mmoItem attr";
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
        if (targets.isEmpty()) return false;
        if (targets.get(0) instanceof Player) {
            final ItemStack itemStack = ArmorType.matchItemStack(((Player) targets.get(0)).getInventory(), ArmorType.valueOf(settings.getString(SLOT).toUpperCase()));
            if (itemStack == null) return false;
            final String key = settings.getString(KEY);
            final String nbtKey = settings.getString(NBT_KEY);
            Double value = NBT.readNbt(itemStack).getOrNull("MMOITEMS_" + nbtKey, Double.class);
            if (value != null) {
                DynamicSkill.getCastData(caster).put(key, value);
                return true;
            }
            DynamicSkill.getCastData(caster).put(key, 0.0);
            return false;
        }
        return false;
    }
}
