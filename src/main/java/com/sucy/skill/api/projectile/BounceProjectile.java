package com.sucy.skill.api.projectile;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.BounceProjectileCollisionEvent;
import com.sucy.skill.api.event.BounceProjectileEndEvent;
import com.sucy.skill.api.event.BounceProjectileSplitEvent;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A carrier projectile with swept collision detection, physical block bouncing
 * and generation-based splitting.
 *
 * <p>Velocity is stored in blocks per tick. Gravity is the vertical velocity
 * change per tick. Split children inherit the visual settings, callbacks and
 * ally/enemy filters of their parent.</p>
 *
 * <p>The initial projectile is generation zero. Generation zero uses
 * {@code max-bounces}, {@code split-enabled} and {@code split-count}. Later
 * generations can override those values with {@code split-child-max-bounces},
 * {@code split-child-enabled} and {@code split-child-count}.</p>
 *
 * <p>Each projectile splits at most once, on its first real block impact, unless
 * {@code split-repeat} is enabled. Positions are validated after every bounce and
 * split so a projectile is never left inside solid geometry, which would otherwise
 * register a zero-length collision every tick and split in place repeatedly.</p>
 */
public class BounceProjectile extends CarrierProjectile {

    /** 每 tick 的垂直速度变化量；0 为无重力，负数向下加速，默认 0。 */
    private static final String KEY_GRAVITY = "gravity";

    /** 载体位置同步间隔，单位秒；0.05 约等于每 tick 更新，默认 0.05。 */
    private static final String KEY_CARRIER_FREQUENCY = "carrier-frequency";

    /** 初始抛射物的最大反弹次数；-1 表示无限，默认 3。 */
    private static final String KEY_MAX_BOUNCES = "max-bounces";

    /** 反弹后的法向速度保留比例，范围 0-1；越小反弹越弱，默认 0.85。 */
    private static final String KEY_RESTITUTION = "restitution";

    /** 反弹后的切向速度保留比例，范围 0-1；越小越容易“贴墙减速”，默认 0.98。 */
    private static final String KEY_TANGENT_RETENTION = "tangent-retention";

    /** 可反弹方块材质列表，英文逗号分隔；all 或 * 表示所有实体方块。 */
    private static final String KEY_BOUNCE_BLOCKS = "bounce-blocks";

    /** 单个碰撞采样段的最大长度，单位格；越小越精确但开销越高，默认 0.25。 */
    private static final String KEY_MAX_STEP_DISTANCE = "max-step-distance";

    /** 每 tick 最多执行的碰撞采样次数，防止高速弹体卡顿，默认 128。 */
    private static final String KEY_MAX_STEPS = "max-steps-per-tick";

    /** 反弹后沿法线推出碰撞面的距离，避免同一方块重复碰撞，默认 0.001。 */
    private static final String KEY_COLLISION_EPSILON = "collision-epsilon";

    /** 初始抛射物成功反弹后是否分裂，默认 false。 */
    private static final String KEY_SPLIT_ENABLED = "split-enabled";

    /** 初始抛射物每次分裂额外生成的子体数量，默认 2。 */
    private static final String KEY_SPLIT_COUNT = "split-count";

    /** 最大分裂代数：0 禁止分裂，1 仅初始体分裂，2 允许第一代继续分裂，默认 1。 */
    private static final String KEY_SPLIT_MAX_DEPTH = "split-max-depth";

    /** 水平模式为扇形半角，圆锥模式为 3D 圆锥角；单位度，范围 0-180，默认 35。 */
    private static final String KEY_SPLIT_ANGLE = "split-angle";

    /**
     * 分裂方向模式：horizontal 保留父体 Y 分量并只做水平扇形偏转；
     * flat 强制子代 Y=0；cone 使用原来的 3D 圆锥分布，默认 horizontal。
     */
    private static final String KEY_SPLIT_MODE = "split-mode";

    /** 子体继承父体速度的比例；0 为静止，大于 1 表示加速，默认 1。 */
    private static final String KEY_SPLIT_SPEED_RETENTION = "split-speed-retention";

    /** 分裂后父体是否继续运动；false 时父体结束，仅保留子体，默认 true。 */
    private static final String KEY_SPLIT_PARENT_CONTINUE = "split-parent-continue";

    /** 子代及后续代数是否允许继续分裂，默认 false；仍受 split-max-depth 限制。 */
    private static final String KEY_SPLIT_CHILD_ENABLED = "split-child-enabled";

    /** 子代及后续代数每次分裂的数量，默认沿用 split-count。 */
    private static final String KEY_SPLIT_CHILD_COUNT = "split-child-count";

    /** 子代及后续代数允许的最大反弹次数；-1 无限，默认沿用 max-bounces。 */
    private static final String KEY_SPLIT_CHILD_MAX_BOUNCES = "split-child-max-bounces";

    /** 子代及后续代数使用的载体名称；留空时继承父体的 name。 */
    private static final String KEY_SPLIT_CHILD_NAME = "split-child-name";
    private static final String KEY_SPLIT_CHILD_GERM_EFFECT = "split-child-germ-effect";

    /**
     * 是否允许同一个抛射物在后续反弹时继续分裂；
     * 默认 false，表示每个抛射物只在其第一次有效碰撞时分裂一次。
     */
    private static final String KEY_SPLIT_REPEAT = "split-repeat";

    private static final String DEFAULT_BOUNCE_BLOCKS =
            "STONE,COBBLESTONE,OBSIDIAN,IRON_BLOCK,DIAMOND_BLOCK,GOLD_BLOCK,EMERALD_BLOCK";

    private static final double DEFAULT_MAX_STEP_DISTANCE = 0.25;
    private static final int DEFAULT_MAX_STEPS = 128;
    private static final double DEFAULT_COLLISION_EPSILON = 0.001;
    private static final double VECTOR_EPSILON = 1.0E-10;

    /** 从方块内部寻找出口时的采样步长，单位格。 */
    private static final double EXIT_STEP = 0.1;

    /** 从方块内部寻找出口时的最大搜索距离，单位格。 */
    private static final double MAX_EXIT_DISTANCE = 3.0;

    /** 分裂子体沿自身方向散开的最大距离，避免所有子体重叠在同一点。 */
    private static final double SPLIT_SPAWN_OFFSET = 0.2;

    private static final Vector SPLIT_UP = new Vector(0, 1, 0);
    private static final Vector SPLIT_X = new Vector(1, 0, 0);

    /** 穿模脱离时尝试的轴方向。 */
    private static final Vector[] EXIT_AXES = {
            new Vector(1, 0, 0), new Vector(-1, 0, 0),
            new Vector(0, 1, 0), new Vector(0, -1, 0),
            new Vector(0, 0, 1), new Vector(0, 0, -1)
    };

    /** Full movement velocity, in blocks per tick. */
    private Vector motionVelocity;

    /** Velocity change per tick. */
    private final Vector acceleration;

    private final SkillContext bounceContext;
    private ProjectileCallback projectileCallback;
    private BounceProjectileCallback lifecycleCallback;

    private final int projectileLevel;
    private final int generation;
    private final int maxBounces;
    private final double restitution;
    private final double tangentRetention;
    private final double maxStepDistance;
    private final int maxStepsPerTick;
    private final double collisionEpsilon;
    private final boolean bounceAllSolid;
    private final Set<Material> bounceMaterials = new HashSet<>();

    private final boolean splitEnabled;
    private final int splitCount;
    private final int maxSplitDepth;
    private final double splitAngle;
    private final boolean horizontalSplit;
    private final boolean flatSplit;
    private final double splitSpeedRetention;
    private final boolean splitParentContinues;
    private final String splitChildName;
    private final String splitChildGermEffect;
    private final boolean splitRepeat;

    private boolean hitAllies;
    private boolean hitEnemies = true;
    private int bounceCount;
    private boolean splitConsumed;

    private Block lastHitBlock;
    private Location lastCollisionLocation;
    private Vector lastCollisionNormal;

    private boolean constructed;
    private boolean endDispatched;
    private boolean endCallbackDispatched;
    private BounceProjectileEndEvent endEvent;
    private BounceProjectileEndEvent.EndReason pendingEndReason;
    private BounceProjectileEndEvent.EndReason nextEndReason;

    public BounceProjectile(
            final SkillContext context,
            LivingEntity shooter,
            int level,
            Location loc,
            Settings settings
    ) {
        this(context, shooter, level, loc, settings, 0);
    }

    private BounceProjectile(
            final SkillContext context,
            LivingEntity shooter,
            int level,
            Location loc,
            Settings settings,
            int generation
    ) {
        super(context, shooter, level, loc, prepareSettings(settings));

        this.bounceContext = context;
        this.projectileLevel = level;
        this.generation = Math.max(0, generation);

        // ParticleProjectile divides velocity by its original step count. Recover
        // the full per-tick velocity, including launch-event modifications.
        Vector inheritedVelocity = super.getVelocity().clone();
        int inheritedSteps = Math.max(1, this.steps);
        inheritedVelocity.multiply(inheritedSteps);
        if (isFinite(inheritedVelocity)) {
            this.motionVelocity = inheritedVelocity;
        } else {
            double speed = this.settings.getAttr(ParticleProjectile.SPEED, level, 1.0);
            this.motionVelocity = loc.getDirection().clone().multiply(speed);
        }

        this.acceleration = new Vector(0, this.settings.getDouble(KEY_GRAVITY, 0.0), 0);

        int rootMaxBounces = (int) this.settings.getDouble(KEY_MAX_BOUNCES, 3.0);
        this.maxBounces = this.generation == 0
                ? rootMaxBounces
                : (int) this.settings.getDouble(KEY_SPLIT_CHILD_MAX_BOUNCES, rootMaxBounces);
        this.restitution = clamp(this.settings.getDouble(KEY_RESTITUTION, 0.85), 0.0, 1.0);
        this.tangentRetention = clamp(this.settings.getDouble(KEY_TANGENT_RETENTION, 0.98), 0.0, 1.0);
        this.maxStepDistance = Math.max(0.01,
                this.settings.getDouble(KEY_MAX_STEP_DISTANCE, DEFAULT_MAX_STEP_DISTANCE));
        this.maxStepsPerTick = Math.max(1,
                (int) this.settings.getDouble(KEY_MAX_STEPS, DEFAULT_MAX_STEPS));
        this.collisionEpsilon = Math.max(0.0001,
                this.settings.getDouble(KEY_COLLISION_EPSILON, DEFAULT_COLLISION_EPSILON));

        String blockSetting = this.settings.getString(KEY_BOUNCE_BLOCKS, DEFAULT_BOUNCE_BLOCKS);
        if (blockSetting == null || blockSetting.trim().isEmpty()) {
            blockSetting = DEFAULT_BOUNCE_BLOCKS;
        }
        this.bounceAllSolid = "*".equals(blockSetting.trim()) || "all".equalsIgnoreCase(blockSetting.trim());
        if (!bounceAllSolid) {
            parseBounceMaterials(blockSetting);
        }

        int rootSplitCount = Math.max(0, (int) this.settings.getDouble(KEY_SPLIT_COUNT, 2.0));
        this.splitEnabled = this.generation == 0
                ? this.settings.getBool(KEY_SPLIT_ENABLED, false)
                : this.settings.getBool(KEY_SPLIT_CHILD_ENABLED, false);
        this.splitCount = this.generation == 0
                ? rootSplitCount
                : Math.max(0, (int) this.settings.getDouble(KEY_SPLIT_CHILD_COUNT, rootSplitCount));
        this.maxSplitDepth = Math.max(0, (int) this.settings.getDouble(KEY_SPLIT_MAX_DEPTH, 1.0));
        this.splitAngle = clamp(this.settings.getDouble(KEY_SPLIT_ANGLE, 35.0), 0.0, 180.0);
        String splitMode = this.settings.getString(KEY_SPLIT_MODE, "horizontal");
        this.horizontalSplit = !"cone".equalsIgnoreCase(splitMode);
        this.flatSplit = "flat".equalsIgnoreCase(splitMode)
                || "horizontal flat".equalsIgnoreCase(splitMode);
        this.splitSpeedRetention = Math.max(0.0,
                this.settings.getDouble(KEY_SPLIT_SPEED_RETENTION, 1.0));
        this.splitParentContinues = this.settings.getBool(KEY_SPLIT_PARENT_CONTINUE, true);
        this.splitRepeat = this.settings.getBool(KEY_SPLIT_REPEAT, false);
        String childName = this.settings.getString(KEY_SPLIT_CHILD_NAME, "");
        String childGermEffect = this.settings.getString(KEY_SPLIT_CHILD_GERM_EFFECT, "");
        this.splitChildName = childName == null || childName.isEmpty() ? null : childName;
        this.splitChildGermEffect = childGermEffect == null || childGermEffect.isEmpty() ? null : childGermEffect;

        this.constructed = true;
        if (this.pendingEndReason != null) {
            BounceProjectileEndEvent.EndReason reason = this.pendingEndReason;
            this.pendingEndReason = null;
            finish(reason);
        }
    }

    /**
     * CarrierProjectile uses ParticleProjectile's frequency for carrier updates.
     * Give this projectile its own default of one carrier update per tick.
     */
    private static Settings prepareSettings(Settings source) {
        Settings copy = new Settings(source);
        double carrierFrequency = Math.max(0.0,
                copy.getDouble(KEY_CARRIER_FREQUENCY, 0.05));
        copy.set("frequency", carrierFrequency);
        return copy;
    }

    private void parseBounceMaterials(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            raw = DEFAULT_BOUNCE_BLOCKS;
        }

        String[] names = raw.split(",");
        for (String name : names) {
            String normalized = name.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            try {
                bounceMaterials.add(Material.valueOf(normalized.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // Ignore unknown materials for cross-version compatibility.
            }
        }
    }

    @Override
    public void setCallback(ProjectileCallback callback) {
        super.setCallback(callback);
        this.projectileCallback = callback;
    }

    private void dispatchEndCallback() {
        if (endEvent != null && lifecycleCallback != null && !endCallbackDispatched) {
            endCallbackDispatched = true;
            lifecycleCallback.onEnd(endEvent);
        }
    }

    /** Sets the optional collision/split/end code callbacks. */
    public void setBounceCallback(BounceProjectileCallback callback) {
        this.lifecycleCallback = callback;
        dispatchEndCallback();
    }

    public BounceProjectileCallback getBounceCallback() {
        return lifecycleCallback;
    }

    @Override
    public void setAllyEnemy(boolean ally, boolean enemy) {
        super.setAllyEnemy(ally, enemy);
        this.hitAllies = ally;
        this.hitEnemies = enemy;
    }

    /**
     * During the parent's constructor, launch events may call getVelocity before
     * this subclass has initialized motionVelocity. Fall back to the parent value.
     */
    @Override
    public Vector getVelocity() {
        return motionVelocity == null ? super.getVelocity() : motionVelocity.clone();
    }

    /**
     * After construction, setVelocity accepts a full per-tick velocity vector.
     * During superclass construction, retain the parent's original semantics.
     */
    @Override
    public void setVelocity(Vector velocity) {
        if (velocity == null) {
            return;
        }
        if (motionVelocity == null) {
            super.setVelocity(velocity);
        } else {
            motionVelocity = isFinite(velocity) ? velocity.clone() : new Vector();
        }
    }

    public int getBounceCount() {
        return bounceCount;
    }

    public int getMaxBounces() {
        return maxBounces;
    }

    public int getGeneration() {
        return generation;
    }

    public int getProjectileLevel() {
        return projectileLevel;
    }

    public boolean isSplitEnabled() {
        return splitEnabled;
    }

    public int getSplitCount() {
        return splitCount;
    }

    public int getMaxSplitDepth() {
        return maxSplitDepth;
    }

    /** The most recent solid block touched by this projectile, or null before a block hit. */
    public Block getLastHitBlock() {
        return lastHitBlock;
    }

    /** Returns a copy of the most recent block contact position. */
    public Location getLastCollisionLocation() {
        return lastCollisionLocation == null ? null : lastCollisionLocation.clone();
    }

    /** Returns a copy of the most recent block contact normal. */
    public Vector getLastCollisionNormal() {
        return lastCollisionNormal == null ? null : lastCollisionNormal.clone();
    }

    @Override
    protected boolean landed() {
        // Solid blocks are handled by this class as collisions, not as landings.
        return false;
    }

    @Override
    protected boolean isTraveling() {
        Location location = getLocation();
        World world = location.getWorld();
        if (world == null || !location.isChunkLoaded()) {
            finish(BounceProjectileEndEvent.EndReason.UNLOADED_CHUNK);
            Bukkit.getPluginManager().callEvent(expire());
            return false;
        }
        return super.isTraveling();
    }

    @Override
    protected Event hit(LivingEntity entity) {
        dispatchCollision(new BounceProjectileCollisionEvent(
                this,
                entity,
                loc,
                motionVelocity
        ));
        return super.hit(entity);
    }

    @Override
    protected Event expire() {
        nextEndReason = BounceProjectileEndEvent.EndReason.EXPIRED;
        return super.expire();
    }

    /** Ensures that every cancellation path emits exactly one end event. */
    @Override
    public void cancel() {
        BounceProjectileEndEvent.EndReason reason = nextEndReason == null
                ? BounceProjectileEndEvent.EndReason.CANCELLED
                : nextEndReason;
        nextEndReason = null;
        finish(reason);
    }

    private void finish(BounceProjectileEndEvent.EndReason reason) {
        if (endDispatched) {
            return;
        }

        nextEndReason = null;
        if (!constructed) {
            if (pendingEndReason == null) {
                pendingEndReason = reason;
            }
            super.cancel();
            return;
        }

        endDispatched = true;
        super.cancel();

        endEvent = new BounceProjectileEndEvent(this, reason);
        Bukkit.getPluginManager().callEvent(endEvent);
        dispatchEndCallback();
    }

    @Override
    public void run() {
        if (!constructed || motionVelocity == null || acceleration == null) {
            finish(BounceProjectileEndEvent.EndReason.CANCELLED);
            return;
        }
        super.run();
    }

    /**
     * Independent movement loop. The step count is recalculated every tick from
     * the current speed, so a rebound can increase or decrease the sampling rate.
     */
    @Override
    protected void applySteps() {
        // The task is scheduled by the superclass constructor. If construction was
        // interrupted (for example by a launch-event listener), cancel instead of
        // dereferencing uninitialized subclass fields on every scheduler tick.
        if (!constructed || motionVelocity == null || acceleration == null) {
            finish(BounceProjectileEndEvent.EndReason.CANCELLED);
            return;
        }

        if (!isTraveling()) {
            return;
        }

        int stepCount = calculateStepCount();
        double dt = 1.0 / stepCount;
        int penetrationAttempts = 0;

        for (int step = 0; step < stepCount; step++) {
            Vector displacement = motionVelocity.clone().multiply(dt)
                    .add(acceleration.clone().multiply(0.5 * dt * dt));

            Location next = loc.clone().add(displacement);
            BlockHit blockHit = traceBlockCollision(loc, next);

            if (!isValid()) {
                return;
            }

            if (blockHit != null) {
                // A projectile already inside a solid block (spawned in geometry,
                // or pushed into it by collision-epsilon) is not a real impact.
                // Eject it silently instead of bouncing and splitting every tick.
                if (blockHit.penetration) {
                    if (++penetrationAttempts > 4 || !resolvePenetration(blockHit)) {
                        return;
                    }
                    continue;
                }

                double elapsed = dt * blockHit.segmentT;
                Vector velocityAtImpact = motionVelocity.clone()
                        .add(acceleration.clone().multiply(elapsed));
                handleBlockCollision(blockHit, velocityAtImpact);
                return;
            }

            loc = next;
            motionVelocity.add(acceleration.clone().multiply(dt));

            if (!isTraveling()) {
                return;
            }

            if (!checkCollision(pierce)) {
                return;
            }
        }
    }

    private int calculateStepCount() {
        Vector endVelocity = motionVelocity.clone().add(acceleration);
        double maxSpeed = Math.max(motionVelocity.length(), endVelocity.length());
        int requested = Math.max(1, (int) Math.ceil(maxSpeed / maxStepDistance));
        return Math.min(requested, maxStepsPerTick);
    }

    /**
     * Moves a projectile that is embedded in solid geometry back into free space
     * without registering a bounce, a collision callback or a split. Otherwise
     * every tick would report a zero-length collision and split in place again.
     */
    private boolean resolvePenetration(BlockHit hit) {
        Location free = findNearestFreeLocation(hit.location);
        if (free == null) {
            finish(BounceProjectileEndEvent.EndReason.NO_REFLECTION);
            return false;
        }
        loc = free;
        return true;
    }

    /**
     * Returns the closest non-solid position around the given point, preferring
     * the direction the projectile came from. Returns null when no exit exists
     * within {@link #MAX_EXIT_DISTANCE}.
     */
    private Location findNearestFreeLocation(Location origin) {
        List<Vector> axes = new ArrayList<>(EXIT_AXES.length + 1);
        if (lastCollisionNormal != null && lastCollisionNormal.lengthSquared() > VECTOR_EPSILON) {
            axes.add(lastCollisionNormal.clone().normalize());
        }
        for (Vector axis : EXIT_AXES) {
            axes.add(axis);
        }

        Location best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Vector axis : axes) {
            for (double distance = EXIT_STEP;
                 distance <= MAX_EXIT_DISTANCE + VECTOR_EPSILON;
                 distance += EXIT_STEP) {
                Location candidate = origin.clone().add(axis.clone().multiply(distance));
                if (isSolidAt(candidate)) {
                    continue;
                }
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = candidate;
                }
                break;
            }
        }
        return best;
    }

    /**
     * Pushes the projectile out of the hit surface and verifies the result is not
     * inside another block. A large collision-epsilon (or a corner/one-block wall)
     * could otherwise leave the projectile embedded and cause repeated in-place
     * collisions and splits.
     */
    private Location resolveExitLocation(Location contact, Vector normal, double distance) {
        double step = Math.max(EXIT_STEP, Math.min(distance, 0.25));
        double start = Math.max(distance, EXIT_STEP);
        for (double d = start; d <= start + MAX_EXIT_DISTANCE + VECTOR_EPSILON; d += step) {
            Location candidate = contact.clone().add(normal.clone().multiply(d));
            if (!isSolidAt(candidate)) {
                return candidate;
            }
        }
        Location fallback = findNearestFreeLocation(contact);
        return fallback == null ? contact.clone() : fallback;
    }

    private boolean isSolidAt(Location location) {
        World world = location.getWorld();
        if (world == null || !location.isChunkLoaded()) {
            return false;
        }
        return world.getBlockAt(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        ).getType().isSolid();
    }

    private void handleBlockCollision(BlockHit hit, Vector velocityAtImpact) {
        loc = hit.location.clone();
        motionVelocity = velocityAtImpact;
        lastHitBlock = hit.block;
        lastCollisionLocation = hit.location.clone();
        lastCollisionNormal = hit.normal.clone();

        dispatchCollision(new BounceProjectileCollisionEvent(
                this,
                hit.block,
                hit.location,
                hit.normal,
                velocityAtImpact
        ));
        if (!isValid()) {
            return;
        }

        // Preserve the existing SkillAPI mechanic callback. A null hit represents
        // a block collision and is converted to a TempEntity by the mechanic.
        if (projectileCallback != null) {
            projectileCallback.callback(bounceContext, this, null);
        }
        if (!isValid()) {
            return;
        }

        if (!isBounceMaterial(hit.block.getType())) {
            finish(BounceProjectileEndEvent.EndReason.UNSUPPORTED_BLOCK);
            return;
        }
        if (maxBounces >= 0 && bounceCount >= maxBounces) {
            finish(BounceProjectileEndEvent.EndReason.BOUNCE_LIMIT);
            return;
        }

        Vector normal = hit.normal.clone().normalize();
        double normalSpeed = motionVelocity.dot(normal);

        // A valid entry collision points into the surface. Flip a numerically
        // inverted normal defensively before applying the reflection formula.
        if (normalSpeed > 0.0) {
            normal.multiply(-1.0);
            normalSpeed = -normalSpeed;
        }
        if (Math.abs(normalSpeed) <= VECTOR_EPSILON) {
            finish(BounceProjectileEndEvent.EndReason.NO_REFLECTION);
            return;
        }

        Vector normalVelocity = normal.clone().multiply(normalSpeed);
        Vector tangentVelocity = motionVelocity.clone().subtract(normalVelocity);
        motionVelocity = tangentVelocity.multiply(tangentRetention)
                .subtract(normalVelocity.multiply(restitution));
        bounceCount++;

        // Move out of the surface before the next swept collision query. The exit
        // position is validated so the projectile never ends a bounce inside
        // another block (which would re-collide and re-split at the same spot).
        loc = resolveExitLocation(hit.location, normal, collisionEpsilon);

        if (motionVelocity.lengthSquared() <= VECTOR_EPSILON) {
            finish(BounceProjectileEndEvent.EndReason.NO_REFLECTION);
            return;
        }

        boolean split = splitProjectiles();
        if (split && !splitParentContinues) {
            finish(BounceProjectileEndEvent.EndReason.SPLIT);
        }
    }

    private boolean splitProjectiles() {
        if (splitConsumed && !splitRepeat) {
            return false;
        }
        if (!splitEnabled || generation >= maxSplitDepth || splitCount <= 0) {
            return false;
        }
        if (motionVelocity.lengthSquared() <= VECTOR_EPSILON) {
            return false;
        }

        Vector reflectedDirection = motionVelocity.clone().normalize();
        List<Vector> directions = calculateSplitDirections(reflectedDirection);
        double childSpeed = motionVelocity.length() * splitSpeedRetention;
        Location splitOrigin = loc.clone();
        List<BounceProjectile> children = new ArrayList<>(directions.size());

        for (Vector direction : directions) {
            Settings childSettings = new Settings(this.settings);
            childSettings.set(ParticleProjectile.SPEED, childSpeed, 0);
            if (splitChildName != null) {
                childSettings.set(CarrierProjectile.NAME, splitChildName);
                childSettings.set(CarrierProjectile.GERM_EFFECT, splitChildGermEffect);
            }

            Location childLocation = resolveSplitSpawnLocation(splitOrigin, direction);
            childLocation.setDirection(direction);
            BounceProjectile child = new BounceProjectile(
                    bounceContext,
                    getShooter(),
                    projectileLevel,
                    childLocation,
                    childSettings,
                    generation + 1
            );
            child.setAllyEnemy(hitAllies, hitEnemies);
            child.setCallback(projectileCallback);
            child.setBounceCallback(lifecycleCallback);
            child.setVelocity(direction.clone().multiply(childSpeed));
            if (child.isValid()) {
                children.add(child);
            }
        }

        if (children.isEmpty()) {
            return false;
        }

        splitConsumed = true;

        BounceProjectileSplitEvent event = new BounceProjectileSplitEvent(
                this,
                children,
                splitOrigin,
                generation + 1
        );
        Bukkit.getPluginManager().callEvent(event);
        if (lifecycleCallback != null) {
            lifecycleCallback.onSplit(event);
        }
        return true;
    }

    /**
     * Offset each child slightly along its own heading so siblings do not all
     * start on the exact same point. Falls back to the split origin when the
     * offset position is blocked.
     */
    private Location resolveSplitSpawnLocation(Location origin, Vector direction) {
        Location candidate = origin.clone().add(direction.clone().multiply(SPLIT_SPAWN_OFFSET));
        return isSolidAt(candidate) ? origin.clone() : candidate;
    }

    /** Distributes children according to the configured split mode. */
    private List<Vector> calculateSplitDirections(Vector direction) {
        if (horizontalSplit) {
            return calculateHorizontalSplitDirections(direction, !flatSplit);
        }

        List<Vector> directions = new ArrayList<>(splitCount);
        if (splitCount == 1 || splitAngle <= VECTOR_EPSILON) {
            for (int i = 0; i < splitCount; i++) {
                directions.add(direction.clone());
            }
            return directions;
        }

        Vector reference = Math.abs(direction.getY()) < 0.99 ? SPLIT_UP : SPLIT_X;
        Vector right = direction.clone().crossProduct(reference);
        if (right.lengthSquared() <= VECTOR_EPSILON) {
            right = direction.clone().crossProduct(SPLIT_X);
        }
        right.normalize();
        Vector up = right.clone().crossProduct(direction).normalize();

        double coneAngle = Math.toRadians(splitAngle);
        double axialScale = Math.cos(coneAngle);
        double radialScale = Math.sin(coneAngle);
        double angleOffset = (bounceCount % 2) * Math.PI / splitCount;

        for (int i = 0; i < splitCount; i++) {
            double ringAngle = Math.PI * 2.0 * i / splitCount + angleOffset;
            Vector radial = right.clone().multiply(Math.cos(ringAngle))
                    .add(up.clone().multiply(Math.sin(ringAngle)));
            Vector splitDirection = direction.clone().multiply(axialScale)
                    .add(radial.multiply(radialScale));
            if (splitDirection.lengthSquared() > VECTOR_EPSILON) {
                splitDirection.normalize();
            }
            directions.add(splitDirection);
        }
        return directions;
    }

    /**
     * Keeps the parent's vertical component and fans children around the
     * horizontal projection of the reflected direction. This prevents extra
     * Y-axis spread from being introduced at every split generation.
     */
    private List<Vector> calculateHorizontalSplitDirections(Vector direction, boolean preserveY) {
        List<Vector> directions = new ArrayList<>(splitCount);
        double y = preserveY ? clamp(direction.getY(), -1.0, 1.0) : 0.0;
        double horizontalScale = preserveY
                ? Math.sqrt(Math.max(0.0, 1.0 - y * y))
                : 1.0;
        Vector horizontal = new Vector(direction.getX(), 0, direction.getZ());

        if (horizontal.lengthSquared() <= VECTOR_EPSILON) {
            if (preserveY || horizontalScale <= VECTOR_EPSILON) {
                for (int i = 0; i < splitCount; i++) {
                    directions.add(direction.clone());
                }
                return directions;
            }
            // A perfectly vertical reflection has no horizontal heading. Flat
            // mode still needs a horizontal axis to fan the children around.
            horizontal = new Vector(1, 0, 0);
        }

        if (splitCount == 1 || splitAngle <= VECTOR_EPSILON) {
            for (int i = 0; i < splitCount; i++) {
                Vector splitDirection = new Vector(
                        horizontal.getX(),
                        y,
                        horizontal.getZ()
                );
                if (splitDirection.lengthSquared() > VECTOR_EPSILON) {
                    splitDirection.normalize();
                }
                directions.add(splitDirection);
            }
            return directions;
        }

        horizontal.normalize();
        double halfAngle = Math.toRadians(splitAngle);

        for (int i = 0; i < splitCount; i++) {
            double ratio = (2.0 * i / (splitCount - 1)) - 1.0;
            double yaw = halfAngle * ratio;
            double cos = Math.cos(yaw);
            double sin = Math.sin(yaw);

            double x = horizontal.getX() * cos - horizontal.getZ() * sin;
            double z = horizontal.getX() * sin + horizontal.getZ() * cos;
            Vector splitDirection = new Vector(x * horizontalScale, y, z * horizontalScale);
            if (splitDirection.lengthSquared() > VECTOR_EPSILON) {
                splitDirection.normalize();
            }
            directions.add(splitDirection);
        }
        return directions;
    }

    private void dispatchCollision(BounceProjectileCollisionEvent event) {
        Bukkit.getPluginManager().callEvent(event);
        if (lifecycleCallback != null) {
            lifecycleCallback.onCollision(event);
        }
    }

    private boolean isBounceMaterial(Material material) {
        if (bounceAllSolid) {
            return material.isSolid();
        }
        return bounceMaterials.contains(material);
    }

    /**
     * Traverses every voxel crossed by the segment using a 3D DDA. This detects
     * fast projectiles that would otherwise skip over a solid block between ticks.
     */
    private BlockHit traceBlockCollision(Location from, Location to) {
        World world = from.getWorld();
        if (world == null || world != to.getWorld()) {
            return null;
        }

        double x0 = from.getX();
        double y0 = from.getY();
        double z0 = from.getZ();
        double dx = to.getX() - x0;
        double dy = to.getY() - y0;
        double dz = to.getZ() - z0;

        int x = floor(x0);
        int y = floor(y0);
        int z = floor(z0);

        if (!isBlockChunkLoaded(world, x, z, from)) {
            return null;
        }
        Block startBlock = world.getBlockAt(x, y, z);
        if (startBlock.getType().isSolid()) {
            Vector normal = startingBlockNormal(dx, dy, dz);
            return new BlockHit(startBlock, from.clone(), normal, 0.0, true);
        }

        int stepX = sign(dx);
        int stepY = sign(dy);
        int stepZ = sign(dz);

        double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
        double tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
        double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);

        double nextX = stepX > 0 ? x + 1.0 : x;
        double nextY = stepY > 0 ? y + 1.0 : y;
        double nextZ = stepZ > 0 ? z + 1.0 : z;

        double tMaxX = stepX == 0 ? Double.POSITIVE_INFINITY : (nextX - x0) / dx;
        double tMaxY = stepY == 0 ? Double.POSITIVE_INFINITY : (nextY - y0) / dy;
        double tMaxZ = stepZ == 0 ? Double.POSITIVE_INFINITY : (nextZ - z0) / dz;

        int crossings = Math.abs(floor(to.getX()) - x)
                + Math.abs(floor(to.getY()) - y)
                + Math.abs(floor(to.getZ()) - z) + 4;

        for (int i = 0; i < crossings; i++) {
            double t = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));
            if (Double.isInfinite(t) || t > 1.0 + VECTOR_EPSILON) {
                return null;
            }

            double nx = 0.0;
            double ny = 0.0;
            double nz = 0.0;

            if (Math.abs(tMaxX - t) <= VECTOR_EPSILON) {
                x += stepX;
                tMaxX += tDeltaX;
                nx = -stepX;
            }
            if (Math.abs(tMaxY - t) <= VECTOR_EPSILON) {
                y += stepY;
                tMaxY += tDeltaY;
                ny = -stepY;
            }
            if (Math.abs(tMaxZ - t) <= VECTOR_EPSILON) {
                z += stepZ;
                tMaxZ += tDeltaZ;
                nz = -stepZ;
            }

            Location point = from.clone().add(dx * t, dy * t, dz * t);
            if (!isBlockChunkLoaded(world, x, z, point)) {
                return null;
            }

            Block block = world.getBlockAt(x, y, z);
            if (block.getType().isSolid()) {
                Vector normal = new Vector(nx, ny, nz);
                if (normal.lengthSquared() <= VECTOR_EPSILON) {
                    normal = startingBlockNormal(dx, dy, dz);
                } else {
                    normal.normalize();
                }
                return new BlockHit(block, point, normal, clamp(t, 0.0, 1.0));
            }
        }

        return null;
    }

    private boolean isBlockChunkLoaded(World world, int blockX, int blockZ, Location point) {
        if (world.isChunkLoaded(blockX >> 4, blockZ >> 4)) {
            return true;
        }

        loc = point.clone();
        isTraveling();
        return false;
    }

    private static Vector startingBlockNormal(double dx, double dy, double dz) {
        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);

        if (ax >= ay && ax >= az && ax > VECTOR_EPSILON) {
            return new Vector(-Math.signum(dx), 0, 0);
        }
        if (ay >= ax && ay >= az && ay > VECTOR_EPSILON) {
            return new Vector(0, -Math.signum(dy), 0);
        }
        if (az > VECTOR_EPSILON) {
            return new Vector(0, 0, -Math.signum(dz));
        }
        return new Vector(0, 1, 0);
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    private static int sign(double value) {
        if (value > 0.0) return 1;
        if (value < 0.0) return -1;
        return 0;
    }

    private static boolean isFinite(Vector vector) {
        return Double.isFinite(vector.getX())
                && Double.isFinite(vector.getY())
                && Double.isFinite(vector.getZ());
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class BlockHit {
        private final Block block;
        private final Location location;
        private final Vector normal;
        private final double segmentT;
        /** True when the segment started inside the block instead of entering it. */
        private final boolean penetration;

        private BlockHit(Block block, Location location, Vector normal, double segmentT) {
            this(block, location, normal, segmentT, false);
        }

        private BlockHit(
                Block block,
                Location location,
                Vector normal,
                double segmentT,
                boolean penetration
        ) {
            this.block = block;
            this.location = location;
            this.normal = normal;
            this.segmentT = segmentT;
            this.penetration = penetration;
        }
    }
}
