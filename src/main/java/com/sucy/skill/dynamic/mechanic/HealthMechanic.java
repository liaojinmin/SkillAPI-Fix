package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.SkillContext;
import me.neon.flash.utils.BukkitAttribute;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * SkillAPI © 2017
 * com.sucy.mechanic.dynamic.skill.HealthSetMechanic
 *
 * 支持多种生命值操作：
 * - 动作：set（设置）、add（增加）、remove（扣除）
 * - 计算模式：flat（固定值）、percent（最大生命值百分比）、multiply（当前生命值乘系数）、divide（当前生命值除系数）
 */
public class HealthMechanic extends MechanicComponent {

    private static final String HEALTH = "health";
    private static final String ACTION = "action";
    private static final String MODE   = "mode";   // 新增：数值计算模式

    @Override
    public String getKey() {
        return "health";
    }

    @Override
    public boolean execute(final LivingEntity caster, SkillContext context, final int level, final List<LivingEntity> targets) {
        // 1. 读取动作类型（默认 set，兼容旧版）
        final String action = settings.getString(ACTION, "set").toLowerCase();
        // 2. 读取数值计算模式（默认 flat，兼容旧版）
        final String mode = settings.getString(MODE, "flat").toLowerCase();
        // 3. 解析配置中的基础数值（支持公式、属性等动态值）
        final double baseAmount = parseValues(caster, HEALTH, level, 1);

        for (final LivingEntity target : targets) {
            // 根据计算模式得到实际生效的数值
            final double effectiveAmount = calculateEffectiveAmount(target, mode, baseAmount);
            // 根据动作修改生命值
            Bukkit.getScheduler().runTaskLater(SkillAPI.singleton(), () -> {
                applyHealthAction(target, action, effectiveAmount);
            }, 5);
        }

        return true;
    }

    /**
     * 根据计算模式计算实际生效的数值
     *
     * @param target     目标实体
     * @param mode       模式：flat / percent / multiply / divide
     * @param baseAmount 基础数值
     * @return 实际应使用的生命值量
     */
    private double calculateEffectiveAmount(LivingEntity target, String mode, double baseAmount) {
        switch (mode) {
            case "percent":
                // 最大生命值的百分比 (例：20 → 20%)
                return BukkitAttribute.MAX_HEALTH.get(target) * (baseAmount / 100.0);
            case "multiply":
                // 当前生命值乘以系数
                return BukkitAttribute.MAX_HEALTH.get(target) * baseAmount;
            case "divide":
                // 当前生命值除以系数（避免除以零）
                if (baseAmount == 0) return target.getHealth();
                return BukkitAttribute.MAX_HEALTH.get(target) / baseAmount;
            case "flat":
            default:
                // 直接使用解析出的固定值
                return baseAmount;
        }
    }

    /**
     * 根据动作类型修改目标生命值
     *
     * @param target 目标实体
     * @param action 动作：set / add / remove
     * @param amount 要应用的生命值量（已按模式计算）
     */
    private void applyHealthAction(LivingEntity target, String action, double amount) {
        switch (action) {
            case "add":
                // 增加生命，不超过最大值
                double newHealthAdd = target.getHealth() + amount;
                target.setHealth(Math.min(newHealthAdd, BukkitAttribute.MAX_HEALTH.get(target)));
                break;

            case "remove":
                // 扣除生命，不小于0（允许致死）
                double newHealthRemove = target.getHealth() - amount;
                target.setHealth(Math.max(1, newHealthRemove));
                break;

            case "set":
            default:
                // 设置生命值，安全限制在 [1, max] 之间
                double newHealthSet = Math.max(1, amount);
                target.setHealth(Math.min(newHealthSet, BukkitAttribute.MAX_HEALTH.get(target)));
                break;
        }
    }
}
