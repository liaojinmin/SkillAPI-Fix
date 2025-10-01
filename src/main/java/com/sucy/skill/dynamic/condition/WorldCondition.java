package com.sucy.skill.dynamic.condition;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;

public class WorldCondition extends ConditionComponent {

    private static final String USE_TARGET = "use-target";         // 是否以目标所在世界为准，否则以施法者所在世界为准
    private static final String EXCLUDE_PLAYER = "exclude-player"; // 世界条件满足时，是否禁止对玩家生效
    private static final String WORLD = "world";                   // 指定的世界名，如果为空则跳过判定
    private static final String REQUIRE_MATCH = "require-match";   // 是否要求必须匹配这个世界（true=必须是，false=必须不是）

    private boolean useTarget = true;
    private boolean excludePlayer = false;
    private String world = "";
    private boolean requireMatch = true;

    @Override
    public String getKey() {
        return "world";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        useTarget = settings.getBool(USE_TARGET, useTarget);
        excludePlayer = settings.getBool(EXCLUDE_PLAYER, excludePlayer);
        world = settings.getString(WORLD, world);
        requireMatch = settings.getBool(REQUIRE_MATCH, requireMatch);
    }

    @Override
    public boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        // 没设置 world，直接通过
        if (world == null || world.isEmpty()) {
            return true;
        }
      //  System.out.println("world >>> "+world);

        // 选择判定的实体（施法者 or 目标）
        LivingEntity entity = useTarget ? target : caster;

        // 在需要排除玩家的情况下，反取布尔值
        if (excludePlayer) {
         //   System.out.println("  excludePlayer >>> "+String.valueOf(target instanceof Player));
            boolean match = requireMatch == entity.getWorld().getName().equalsIgnoreCase(world);
        //    System.out.println("  match >>> "+match);
            if (match && target instanceof Player) {
                return false;
            }
            return true;
        } else {
          //  System.out.println("  else >>> "+String.valueOf(requireMatch == entity.getWorld().getName().equalsIgnoreCase(world)));
            return requireMatch == entity.getWorld().getName().equalsIgnoreCase(world);
        }
    }
}


