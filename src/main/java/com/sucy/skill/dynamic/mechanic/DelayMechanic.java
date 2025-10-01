
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;
import java.util.List;

/**
 * Executes child components after a delay
 * 可以使用 {@link com.sucy.skill.dynamic.mechanic.ReturnMechanic} 设置中断
 */
public class DelayMechanic extends MechanicComponent {

    private static final String SECONDS = "delay";

    @Override
    public String getKey() {
        return "delay";
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
    public boolean execute(final LivingEntity caster, SkillContext context, final int level, final List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return false;
        }
        final double seconds = parseValues(caster, SECONDS, level, 2.0);
        final String mark = settings.getString(ReturnMechanic.MARK, "标记名称");
        //germAction = settings.getString(ReturnMechanic.DENY_GERM_ACTION, "");
        //commands = settings.getStringList(ReturnMechanic.DENY_COMMANDS);
        // 添加标记
        ReturnMechanic.addMark(caster, new ReturnMechanic.MarkTask(
                () -> executeChildren(caster, context, level, targets),
                (long) (seconds * 20)
        ), mark);

        return true;
    }


}
