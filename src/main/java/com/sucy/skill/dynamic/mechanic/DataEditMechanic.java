package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.data.CustomDataStack;
import com.sucy.skill.dynamic.data.DataSkills;
import org.bukkit.entity.LivingEntity;

import java.util.List;


public class DataEditMechanic extends MechanicComponent {
    private static final String KEY = "key";

    private static final String ACTION = "action";

    private static final String VALUE = "value";

    @Override
    public String getKey() {
        return "data edit";
    }

    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        if (targets.size() == 0 || !settings.has(KEY)) {
            return false;
        }

        String key = settings.getString(KEY).replace("{uuid}", caster.getUniqueId().toString());
        String action = settings.getString(ACTION);
        double value = parseValues(caster, VALUE, level, 1);
        for (LivingEntity target : targets) {
            CustomDataStack data = DataSkills.getMetaStack(target.getUniqueId(), true);
            if (data != null) {
                data.putMeta(key, value, -1, action);
            }
        }
        return true;
    }
}
