
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ValueManaMechanic extends MechanicComponent
{
    private static final String KEY  = "key";
    private static final String TYPE = "type";

    @Override
    public String getKey() {
        return "value mana";
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
        if (!(targets.get(0) instanceof Player)) return false;

        final PlayerData player = SkillAPI.getPlayerData((targets.get(0)).getUniqueId());
        if (player == null) return false;
        final String key = settings.getString(KEY);
        final String type = settings.getString(TYPE, "current").toLowerCase();
        final Map<String, Object> data = DynamicSkill.getCastData(caster);
        switch (type) {
            case "max":
                data.put(key, player.getMaxMana());
                break;
            case "percent":
                data.put(key, player.getMana() / player.getMaxMana());
                break;
            case "missing":
                data.put(key, player.getMaxMana() - player.getMana());
                break;
            default: // current
                data.put(key, player.getMana());
                break;
        }
        return true;
    }
}