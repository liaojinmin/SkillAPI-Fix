package com.sucy.skill.api.projectile;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.EntityProjectileExpireEvent;
import com.sucy.skill.api.event.EntityProjectileHitEvent;
import com.sucy.skill.api.event.EntityProjectileLandEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.projectile
 *
 * @author 老廖
 * @since 2024/8/1 1:28
 */
public class EntityProjectile2 extends CustomProjectile {


    public final static HashMap<UUID, EntityProjectile2> cache = new HashMap<>();

    private final static Vector defaultVector = new Vector(0.0, 0.0, 0.0);

    private final Location originLocation;
    private Location targetLocation;
    private final Entity target;
    private final Double distance;

    private Vector vector;
    private double length = 0.0;
    private double gotLength = 0.0;
    private double step = 0.5;
    private int speed = 2;
    private double rotationAngle = 0;
    private double gravity = 0;
    private boolean trace = false;

    private boolean useVelocity = false;

    private Consumer<Event> hitEventConsumer = null;

    private Consumer<Event> landEventConsumer = null;

    private Consumer<Event> expireEventConsumer = null;

    public EntityProjectile2(
            LivingEntity thrower,
            Location targetLocation,
            @Nullable Entity target,
            double distance
    ) {
        super(thrower, false);
        this.originLocation = thrower.getLocation();
        this.targetLocation = targetLocation;
        this.target = target;
        this.distance = distance;
        applyReset();
    }

    public EntityProjectile2(
            LivingEntity thrower,
            Location targetLocation,
            @Nullable Entity target,
            double distance,
            double left,
            double right,
            double upward,
            double forward
    ) {
        super(thrower, false);
        this.originLocation = thrower.getLocation();
        this.targetLocation = targetLocation;
        this.target = target;
        this.distance = distance;

        // 获取朝向向量（正前方方向）
        Vector direction = originLocation.getDirection().normalize();
        // 通过叉积获得右方向向量
        Vector rightDirection = direction.clone()
                .crossProduct(new Vector(0, 1, 0)).normalize();
        // 直接使用Y轴向量进行垂直偏移
        Vector upwardDirection = new Vector(0, 1, 0);
        // 调整 originLocation 的位置
        originLocation.add(direction.multiply(forward)); // 向前移动
        originLocation.add(rightDirection.multiply(right - left)); // 左右偏移
        originLocation.add(upwardDirection.multiply(upward)); // 向上偏移

        applyReset();
    }


    public void registerHit(Consumer<Event> hit) {
        hitEventConsumer = hit;
    }

    public void registerLand(Consumer<Event> hit) {
        landEventConsumer = hit;
    }

    public void registerExpire(Consumer<Event> hit) {
        expireEventConsumer = hit;
    }

    public void start() {
        runTaskTimer(SkillAPI.singleton(), 1, 1);
    }

    public void setStep(double step) {
        this.step = step;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public void useVelocity() {
        useVelocity = true;
    }

    public void applyReset() {
        if (target != null) {
            targetLocation = target.getLocation();
        }
        vector = targetLocation.clone().subtract(originLocation).toVector();
        length = vector.length();
        // 修正使用设定的距离
        length = length > distance ? distance : length;
        if (vector.length() == 0) {
            // 防止长度为0的向量产生 NaN
            vector = new Vector(1, 0, 0); // 或者其他默认值
        } else {
            vector.normalize();
        }
        // applyRotation
        double radians = Math.toRadians(rotationAngle);
        if (Double.isFinite(radians)) {
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double newX = vector.getX() * cos - vector.getZ() * sin;
            double newZ = vector.getX() * sin + vector.getZ() * cos;
            vector.setX(newX);
            vector.setZ(newZ);
        } else {
            vector.setX(1);
            vector.setZ(0);
        }
    }

    @Override
    public Location getLocation() {
        return getShooter().getLocation();
    }

    @Override
    protected Event expire() {
        EntityProjectileExpireEvent event = new EntityProjectileExpireEvent(this);
        if (expireEventConsumer != null) {
            expireEventConsumer.accept(event);
        }
        return event;
    }

    @Override
    protected Event land() {
        EntityProjectileLandEvent event = new EntityProjectileLandEvent(this);
        if (landEventConsumer != null) {
            landEventConsumer.accept(event);
        }
        return event;
    }

    @Override
    protected Event hit(LivingEntity entity) {
        EntityProjectileHitEvent event = new EntityProjectileHitEvent(this, entity);
        if (hitEventConsumer != null) {
            hitEventConsumer.accept(event);
        }
        return event;
    }

    @Override
    protected boolean landed() {
        return !NON_SOLID_BLOCKS.contains(
                getShooter().getLocation().add(0.0, 0.5, 0.0).getBlock().getType()
        );
    }

    @Override
    protected double getCollisionRadius() {
        return getShooter().getVelocity().length() / 2;
    }

    @Override
    protected Vector getVelocity() {
        return getShooter().getVelocity();
    }

    @Override
    protected void setVelocity(Vector vel) {
        getShooter().setVelocity(vel);
    }

    private int count;

    private void moveVelocity() {
        try {
            count++;
            if (count >= speed) {
                count = 0;
                if (gotLength < length) {
                    Location currLocation = getShooter().getLocation();
                   // System.out.println("currLocation: "+currLocation);
                    // 计算方向向量
                    Vector direction = targetLocation.clone().subtract(currLocation).toVector();
                    direction.normalize();
                    Vector moveVector = direction.multiply(step);

                    // 计算预计的新位置
                   // System.out.println("moveVector: "+moveVector);
                    Vector distanceVector = moveVector.clone().multiply(step / 0.8);
                   // System.out.println("distanceVector: "+distanceVector);
                    Location newLocation = currLocation.add(distanceVector);

                    // 如果预计的新位置超过目标位置，则直接传送
                    double dis = newLocation.distance(targetLocation);
                   // System.out.println("dis: "+dis);
                    if (dis < 2.0) {
                        getShooter().setVelocity(defaultVector);
                        cancel();
                        Bukkit.getPluginManager().callEvent(land());
                        return;
                    }

                    // 检查方块是否阻挡
                    if (!NON_SOLID_BLOCKS.contains(newLocation.getBlock().getType())) {
                        cancel();
                        Bukkit.getPluginManager().callEvent(land());
                        return;
                    }

                    // 检查其他移动条件
                    if (!isTraveling()) {
                        cancel();
                        Bukkit.getPluginManager().callEvent(land());
                        return;
                    }

                    if (!checkCollision(false)) {
                        return;
                    }

                    // 计算出向量
                    gotLength += step;

                    // 应用速度
                    getShooter().setVelocity(moveVector);

                } else {
                    cancel();
                    Bukkit.getPluginManager().callEvent(expire());
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            cancel();
            Bukkit.getPluginManager().callEvent(expire());
        }
    }
    private void moveLocation() {
        try {
            count++;
            if (count >= speed) {
                count = 0;
                // System.out.println("tick ");
                if (gotLength < length) {
                    if (trace && target != null) {
                        applyReset();
                    }
                    Vector temp = vector.clone().multiply(gotLength);
                    Location location = originLocation.clone();
                    double x = location.getX();
                    double z = location.getZ();
                    location = getShooter().getLocation(location);
                    location.setX(x + temp.getX());
                    location.setY(location.getY() - gravity * gotLength);
                    location.setZ(z + temp.getZ());
                    if (!NON_SOLID_BLOCKS.contains(location.getBlock().getType())) {
                       // System.out.println("loc != AIR");
                        cancel();
                        Bukkit.getPluginManager().callEvent(land());
                        return;
                    }
                    if (!isTraveling()) {
                       // System.out.println("!isTraveling()");
                        cancel();
                        Bukkit.getPluginManager().callEvent(land());
                        return;
                    }
                    if (!checkCollision(false)) {
                      //  System.out.println("checkCollision");
                        return;
                    }
                    gotLength += step;
                    // System.out.println("teleport x:" + location.getX() + " y:" + location.getY() + " z:" + location.getZ());
                    getShooter().teleport(location);
                } else {
                    cancel();
                    Bukkit.getPluginManager().callEvent(expire());
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            cancel();
            Bukkit.getPluginManager().callEvent(expire());
        }
    }

    @Override
    public void run() {
        if (useVelocity) {
            moveVelocity();
        } else {
            moveLocation();
        }
    }

    private static final Set<Material> NON_SOLID_BLOCKS = new HashSet<Material>() {{
        add(Material.AIR);
       // add(Material.GRASS);
        add(Material.GRASS_PATH);
        add(Material.LONG_GRASS);
        add(Material.SUGAR_CANE);
        // 花
        add(Material.YELLOW_FLOWER);
        add(Material.RED_ROSE);
        add(Material.DOUBLE_PLANT);
        add(Material.SUGAR_CANE_BLOCK);
        add(Material.RED_MUSHROOM);
        add(Material.BROWN_MUSHROOM);
        add(Material.VINE);
        add(Material.COCOA);
        add(Material.WHEAT);
        add(Material.MELON_STEM);
        add(Material.PUMPKIN_STEM);
        add(Material.TORCH);
        add(Material.LADDER);
        add(Material.TRIPWIRE);
        add(Material.BANNER);
        add(Material.END_ROD);
        add(Material.FLOWER_POT);
        add(Material.LEVER);
        add(Material.TRIPWIRE_HOOK);
        add(Material.DAYLIGHT_DETECTOR);
        add(Material.REDSTONE_WIRE);
        add(Material.SLIME_BLOCK);
    }};

}
