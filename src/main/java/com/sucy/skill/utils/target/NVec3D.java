package com.sucy.skill.utils.target;

import com.google.common.base.Objects;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * SkillAPI-Fix
 * com.sucy.skill.util.target
 *
 * @author 老廖
 * @since 2024/1/15 19:45
 */
public class NVec3D {
    /**
     * Point with the coordinate (1, 1, 1).
     */
    public static final NVec3D UNIT_MAX = new NVec3D(1, 1, 1);

    /** X coordinate. */
    public final double x;
    /** Y coordinate. */
    public final double y;
    /** Z coordinate. */
    public final double z;

    /**
     * Creates a new vector with the given coordinates.
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public NVec3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a new vector with the coordinates of the given vector.
     * @param v vector to copy.
     */
    public NVec3D(NVec3D v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    /**
     * Construct a vector from a Bukkit location.
     * @param loc - the Bukkit location.
     */
    public static NVec3D fromLocation(Location loc) {
        return new NVec3D(loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Construct a copy of our immutable vector from Bukkit's mutable vector.
     * @param v - Bukkit vector.
     * @return A copy of the given vector.
     */
    public static NVec3D fromVector(Vector v) {
        return new NVec3D(v.getX(), v.getY(), v.getZ());
    }

    /**
     * Add vector v and returns result as new vector.
     *
     * @param v vector to add
     * @return result as new vector
     */
    public final NVec3D add(NVec3D v) {
        return new NVec3D(x + v.x, y + v.y, z + v.z);
    }

    /**
     * Scales vector uniformly and returns result as new vector.
     *
     * @param s scale factor
     *
     * @return new vector
     */
    public NVec3D scale(double s) {
        return new NVec3D(x * s, y * s, z * s);
    }

    /**
     * Normalizes the vector so that its magnitude = 1.
     * @return The normalized vector.
     */
    public NVec3D normalize() {
        double mag = Math.sqrt(x * x + y * y + z * z);

        if (mag > 0)
            return scale(1.0 / mag);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NVec3D) {
            final NVec3D v = (NVec3D) obj;
            return x == v.x && y == v.y && z == v.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y, z);
    }

    public String toString() {
        return String.format("{x: %g, y: %g, z: %g}", x, y, z);
    }
}
