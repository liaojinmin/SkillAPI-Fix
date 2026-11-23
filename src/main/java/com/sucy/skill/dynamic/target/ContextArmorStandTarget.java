package com.sucy.skill.dynamic.target;

import com.sucy.skill.api.armorstand.ArmorStandInstance;
import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;

import java.util.*;


public class ContextArmorStandTarget extends TargetComponent {

    private static final String TARGET = "target";

    private static final String LOCK = "lock";

    private List<LivingEntity> get(LivingEntity entity, List<Integer> list, boolean lock) {
        List<LivingEntity> armorStands = new ArrayList<>();
        for (int a : list) {
            ArmorStandInstance armorStandInstance = ArmorStandManager.getArmorStand(entity, a);
            if (armorStandInstance != null) {
                if (lock) {
                    // 锁定
                    armorStandInstance.stopRunnable();
                }
                armorStands.add(armorStandInstance.getArmorStand());
            }
        }
        return armorStands;
    }

    @Override
    List<LivingEntity> getTargets(
            SkillContext context,
            LivingEntity caster,
            int level,
            List<LivingEntity> targets
    ) {
        boolean target = settings.getBool(TARGET, false);
        boolean lock = settings.getBool(LOCK, true);
        List<Integer> list = context.getIntegerList("armor stand");
        if (target) {
            List<LivingEntity> armorStands = new ArrayList<>();
            for (LivingEntity entity : targets) {
                armorStands.addAll(get(entity, list, lock));
            }
            return armorStands;
        } else {
            return get(caster, list, lock);
        }
    }

    @Override
    public String getKey() {
        return "ContextArmorStand";
    }

}
