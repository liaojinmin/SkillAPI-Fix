package com.sucy.skill.api.particle.direction;

import com.sucy.skill.data.Point2D;
import com.sucy.skill.data.Point3D;

/**
 * Handles the XY direction
 */
public class XYHandler implements DirectionHandler {
    public static XYHandler instance = new XYHandler();

    /**
     * Applies the two results from the polar calculation to a point
     *
     * @param point the point to apply it to
     * @param n1    first value
     * @param n2    second value
     */
    public void apply(Point3D point, double n1, double n2) {
        point.x = n1;
        point.y = n2;
        point.z = 0;
    }

    /**
     * Calculates the X value of a point after rotation
     *
     * @param p    original point
     * @param trig trig data
     *
     * @return rotation
     */
    public double rotateX(Point3D p, Point2D trig)
    {
        return p.x * trig.x - p.y * trig.y;
    }

    /**
     * Calculates the Y value of a point after rotation
     *
     * @param p    original point
     * @param trig trig data
     *
     * @return rotation
     */
    public double rotateY(Point3D p, Point2D trig)
    {
        return p.x * trig.y + p.y * trig.x;
    }

    /**
     * Calculates the Z value of a point after rotation
     *
     * @param p    original point
     * @param trig trig data
     *
     * @return rotation
     */
    public double rotateZ(Point3D p, Point2D trig)
    {
        return p.z;
    }
}
