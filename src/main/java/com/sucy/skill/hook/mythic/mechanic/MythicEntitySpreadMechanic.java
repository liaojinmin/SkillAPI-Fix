package com.sucy.skill.hook.mythic.mechanic;

import com.sucy.skill.api.projectile.EntityProjectile2;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

@MythicMechanic(
        author = "廖爷爷",
        name = "entitySpread",
        description = "抛射生物"
)
public class MythicEntitySpreadMechanic extends SkillMechanic implements ITargetedEntitySkill, ITargetedLocationSkill {

    private static final Vector UP = new Vector(0, 1, 0);

    private final double step;
    protected final double gravity;

    protected final double angle;

    protected final double speed;

    protected final double distance;

    protected final boolean trace;

    protected final double right;

    protected final double upward;

    protected final double forward;

    public MythicEntitySpreadMechanic(String line, MythicLineConfig mlc) {
        super(line, mlc);
        step = mlc.getDouble(new String[]{"step"}, 0.5);
        gravity = mlc.getDouble(new String[]{"gravity", "g"}, 0.0);
        angle = mlc.getDouble(new String[]{"angle", "a"}, 0.0);
        speed = mlc.getDouble(new String[]{"speed", "s"}, 0.05);
        distance = mlc.getDouble(new String[]{"distance", "d"}, 6);
        trace = mlc.getBoolean(new String[]{"trace", "t"}, false);

        right = mlc.getDouble(new String[]{"right", "r"}, 0);
        upward = mlc.getDouble(new String[]{"upward", "u"}, 0);
        forward = mlc.getDouble(new String[]{"forward", "f"}, 0);
    }

    private boolean apply(SkillMetadata meta, AbstractLocation location, @Nullable Entity entity) {
        try {
            LivingEntity cast = (LivingEntity) meta.getCaster().getEntity().getBukkitEntity();
            Location targetLocation = BukkitAdapter.adapt(location);
            EntityProjectile2 ne = new EntityProjectile2(
                    cast,
                    targetLocation,
                    entity,
                    distance
            );
            ne.setStep(step);
            ne.setSpeed((int) (speed * 20));
            ne.setRotationAngle(angle);
            ne.setGravity(gravity);
            ne.setTrace(trace);

            EntityProjectile2 old = EntityProjectile2.cache.put(cast.getUniqueId(), ne);
            if (old != null) {
                old.cancel();
            }
            ne.start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean castAtLocation(SkillMetadata skillMetadata, AbstractLocation abstractLocation) {
        return apply(skillMetadata, abstractLocation, null);
    }

    @Override
    public boolean castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        return apply(skillMetadata, abstractEntity.getLocation(), abstractEntity.getBukkitEntity());
        //return castAtLocation(skillMetadata, abstractEntity.getLocation());
    }
}
