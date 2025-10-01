package com.sucy.skill.dynamic.mechanic;

import com.germ.germplugin.api.GermPacketAPI;
import com.sucy.skill.api.projectile.EntityProjectile;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.TempEntity;
import me.neon.core.listener.KeyListener;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EntitySpreadMechanic extends MechanicComponent {

    @Override
    public String getKey() {
        return "entity spread";
    }

    private SkillContext skillContext = null;

    private Integer Level = 0;

    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) return false;
        skillContext = context;
        this.Level = level;
        LivingEntity tar = targets.get(0);
        if (tar instanceof TempEntity) {
            return false;
        }
        //System.out.println("caster: "+caster.getName());
        //System.out.println("target: "+tar.getName() + " type: "+tar.getType());
        final double step = parseValues(caster, "step", level, 0.5);
        final double gravity = parseValues(caster, "gravity", level, 0.0);
        final double angle = parseValues(caster, "angle", level, 0.0);
        final double speed = parseValues(caster, "speed", level, 0.05);
        final boolean trace = settings.getBool("trace", false);
        final boolean lock = settings.getBool("lock", true);
        final double left = parseValues(caster, "left", level, 0.0);
        final double right = parseValues(caster, "right", level, 0.0);
        final double upward = parseValues(caster, "upward", level, 0.0);
        final double forward = parseValues(caster, "forward", level, 0.0);
        final double distance = caster.getLocation().distance(tar.getLocation());
        try {
            EntityProjectile ne = new EntityProjectile(
                    caster,
                    tar.getLocation(),
                    tar,
                    distance,
                    left,
                    right,
                    upward,
                    forward
            );
            ne.setStep(step);
            ne.setSpeed((int) (speed * 20));
            ne.setRotationAngle(angle);
            ne.setGravity(gravity);
            ne.setTrace(trace);
            EntityProjectile old = EntityProjectile.cache.put(caster.getUniqueId(), ne);
            if (old != null) {
                old.cancel();
            }

            if (lock) {
                Consumer<Event> runnable;
                if (tar.hasAI()) {
                     runnable = it -> {

                        if (caster instanceof Player) {
                          //  KeyListener.keyModifierUseDisable.remove(caster.getUniqueId());
                            KeyListener.delSpecialGlobalDisable((Player) caster);
                            caster.setGravity(true);
                            GermPacketAPI.sendUnlockPlayerMove((Player) caster);
                        }
                        if (tar instanceof Player) {
                        //    KeyListener.keyModifierUseDisable.remove(tar.getUniqueId());
                            KeyListener.delSpecialGlobalDisable((Player) tar);
                            GermPacketAPI.sendUnlockPlayerMove((Player) tar);
                        } else {
                            tar.setAI(true);
                        }
                        callback(caster, tar);
                    };
                } else {
                    runnable = it -> {

                        if (caster instanceof Player) {
                            //KeyListener.keyModifierUseDisable.remove(caster.getUniqueId());
                            KeyListener.delSpecialGlobalDisable((Player) caster);
                            caster.setGravity(true);
                            GermPacketAPI.sendUnlockPlayerMove((Player) caster);
                        }
                        if (tar instanceof Player) {

                            //KeyListener.keyModifierUseDisable.remove(tar.getUniqueId());
                            KeyListener.delSpecialGlobalDisable((Player) tar);
                            GermPacketAPI.sendUnlockPlayerMove((Player) tar);
                        }
                        callback(caster, tar);
                    };
                }
                ne.registerHit(runnable);
                ne.registerLand(runnable);
                ne.registerExpire(runnable);
                if (caster instanceof Player) {
                    caster.setGravity(false);
                   // KeyListener.keyModifierUseDisable.put(caster.getUniqueId(), true);
                    KeyListener.addSpecialGlobalDisable((Player) caster, 99999);
                    GermPacketAPI.sendLockPlayerMove((Player) caster, 99999);
                }
                if (tar instanceof Player) {
                    //KeyListener.keyModifierUseDisable.put(tar.getUniqueId(), true);

                    KeyListener.addSpecialGlobalDisable((Player) tar, 99999);
                    GermPacketAPI.sendLockPlayerMove((Player) tar, 99999);
                } else {
                    tar.setAI(false);
                }
            }
            //System.out.println("start");
            ne.useVelocity();
            ne.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public void callback(LivingEntity projectile, LivingEntity hit) {
        if (hit == null) {
            hit = new TempEntity(projectile.getLocation());
        }
        ArrayList<LivingEntity> targets = new ArrayList<>();
        targets.add(hit);
        if (skillContext == null) skillContext = new SkillContext();
        executeChildren(projectile, skillContext, Level, targets);
    }
}
