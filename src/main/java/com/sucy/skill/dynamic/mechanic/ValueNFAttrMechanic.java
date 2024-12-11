package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.enums.ArmorType;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.DynamicSkill;
import me.neon.flash.api.NeonFlashAPI;
import me.neon.flash.api.item.ReadItemFactory;
import me.neon.flash.feature.data.DataParser;
import me.neon.flash.feature.data.JsonDataParser;
import me.neon.flash.feature.data.StringDataParse;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ValueNFAttrMechanic extends MechanicComponent {
    private static final String KEY  = "key";
    private static final String NBT_KEY  = "nbt";
    private static final String SLOT = "slot";

    @Override
    public String getKey() {
        return "value neonflash attr";
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
            if (itemStack == null || itemStack.getType() == Material.AIR) return false;
            final String key = settings.getString(KEY);
            final String nbtKey = settings.getString(NBT_KEY);
            final ReadItemFactory factory = NeonFlashAPI.INSTANCE.getItemHandler().readSimple(itemStack);
            if (factory != null) {
                DataParser data = factory.getData(nbtKey);
                if (data != null) {
                    if (data instanceof StringDataParse) {
                        DynamicSkill.getCastData(caster).put(key, ((StringDataParse) data).asDouble());
                    } else if (data instanceof JsonDataParser) {
                        DynamicSkill.getCastData(caster).put(key, ((JsonDataParser) data).getDouble("base"));
                    }
                    return true;
                }
            }
            DynamicSkill.getCastData(caster).put(key, 0.0);
            return false;
        }
        return false;
    }
}
