package com.sucy.skill.utils.target;

import org.bukkit.Location;

/**
 * SkillAPI-Fix
 * com.sucy.skill.utils.target
 *
 * @author 老廖
 * @since 2024/1/15 19:46
 */
public class NRay3D extends NVec3D {

    public final NVec3D dir;

    public NRay3D(NVec3D origin, NVec3D direction) {
        super(origin);
        dir = direction.normalize();
    }

    /**
     * Construct a 3D ray from a location.
     * @param loc - the Bukkit location.
     */
    public NRay3D(Location loc) {

        this(NVec3D.fromLocation(loc), NVec3D.fromVector(loc.getDirection()));
    }

    public NVec3D getDirection() {
        return dir;
    }

    public NVec3D getPointAtDistance(double dist) {
        return add(dir.scale(dist));
    }

    public String toString() {
        return "origin: " + super.toString() + " dir: " + dir;
    }
}
