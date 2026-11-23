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

    @Override
    public boolean execute(final SkillContext context, LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return false;
        }
        float volume = (float) parseValues(context, caster, VOLUME, level, 100.0) / 100;
        float pitch = (float) parseValues(context, caster, PITCH, level, 0.0);

        String type = settings.getString(SOUND, "");
        volume = Math.max(0, volume);
        pitch = Math.min(2, Math.max(0.5f, pitch));
        try {
            Sound sound = Sound.valueOf(type.toUpperCase().replace(" ", "_"));
            for (LivingEntity target : targets) {
                target.getWorld().playSound(target.getLocation(), sound, volume, pitch);
            }
        } catch (Exception ex) {
            //System.out.println("GermPacketAPI "+type + " "+volume);
            for (LivingEntity target : targets) {
                GermPacketAPI.playSound(
                        target.getLocation(),
                        type,
                        SoundType.MASTER,
                        0, volume, pitch
                );
            }
        }
        return !targets.isEmpty();
    }

}
