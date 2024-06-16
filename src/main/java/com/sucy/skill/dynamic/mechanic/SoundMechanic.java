package com.sucy.skill.dynamic.mechanic;

import com.germ.germplugin.api.GermPacketAPI;
import com.germ.germplugin.api.SoundType;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.log.Logger;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class SoundMechanic extends MechanicComponent {

    private static final String SOUND  = "sound";
    private static final String VOLUME = "volume";
    private static final String PITCH  = "pitch";

    @Override
    public String getKey() {
        return "sound";
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
        if (targets.size() == 0) {
            return false;
        }
        float volume = (float) parseValues(caster, VOLUME, level, 100.0) / 100;
        float pitch = (float) parseValues(caster, PITCH, level, 0.0);

        String type = settings.getString(SOUND, "").toUpperCase().replace(" ", "_");
        volume = Math.max(0, volume);
        pitch = Math.min(2, Math.max(0.5f, pitch));
        try {
            Sound sound = Sound.valueOf(type);
            for (LivingEntity target : targets) {
                target.getWorld().playSound(target.getLocation(), sound, volume, pitch);
            }
        } catch (Exception ex) {
            for (LivingEntity target : targets) {
                GermPacketAPI.playSound(
                        target.getLocation(),
                        type,
                        SoundType.MASTER,
                        0, volume, pitch
                );
            }
        }
        return targets.size() > 0;
    }

}
