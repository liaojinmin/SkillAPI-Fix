package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.BounceProjectileSplitEvent;
import com.sucy.skill.api.particle.EffectPlayer;
import com.sucy.skill.api.particle.target.FollowTarget;
import com.sucy.skill.api.projectile.BounceProjectile;
import com.sucy.skill.api.projectile.BounceProjectileCallback;
import com.sucy.skill.api.projectile.CustomProjectile;
import com.sucy.skill.api.projectile.ParticleProjectile;
import com.sucy.skill.api.projectile.ProjectileCallback;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.TempEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Launches carrier-based projectiles with physical block bouncing and optional
 * recursive splitting. Entity and block collisions execute child components.
 */
public class BounceProjectileMechanic extends MechanicComponent implements ProjectileCallback {

    private static final Vector UP = new Vector(0, 1, 0);
    private static final Vector DOWN = new Vector(0, -1, 0);

    /** 锥形散射角，单位度；仅 spread 为 cone / horizontal cone 时使用，默认 30。 */
    private static final String ANGLE = "angle";

    /** 每个目标发射的抛射物数量，默认 1；小于等于 0 时不发射。 */
    private static final String AMOUNT = "amount";

    /** 抛射物元数据中的技能等级键，供碰撞子组件读取，不是 YAML 配置项。 */
    private static final String LEVEL = "skill_level";

    /** rain 模式下抛射物的生成高度，默认 8 格。 */
    private static final String HEIGHT = "height";

    /** rain 模式下生成区域的水平半径，默认 2 格。 */
    private static final String RADIUS = "rain-radius";

    /** 发射形态：cone（锥形）、horizontal cone（水平锥形）或 rain（雨幕），默认 cone。 */
    private static final String SPREAD = "spread";

    /** 阵营过滤：enemy 只命中敌方，ally 只命中友方，默认 enemy。 */
    private static final String ALLY = "group";

    /** 生成点相对目标朝向的左右偏移，单位格，默认 0。 */
    private static final String RIGHT = "right";

    /** 生成点相对目标的向上偏移，单位格，默认 0；不会改变抛射方向。 */
    private static final String UPWARD = "upward";

    /** 生成点相对目标朝向的前后偏移，单位格，默认 0。 */
    private static final String FORWARD = "forward";

    /** 是否启用 EffectPlayer 跟随特效，默认 false；关闭时不创建特效实例。 */
    private static final String USE_EFFECT = "use-effect";

    /** 跟随特效使用的效果键，默认使用技能名称；仅 use-effect 为 true 时读取。 */
    private static final String EFFECT_KEY = "effect-key";

    /** 初始抛射物碰撞时调用的子技能名称；留空不触发。 */
    private static final String COLLISION_SKILL = "collision-skill";

    /** 分裂体碰撞时调用的子技能名称；留空不触发。 */
    private static final String SPLIT_COLLISION_SKILL = "split-collision-skill";

    @Override
    public String getKey() {
        return "bounce projectile";
    }

    @Override
    public boolean execute(
            final SkillContext context,
            LivingEntity caster,
            int level,
            List<LivingEntity> targets
    ) {
        if (targets == null || targets.isEmpty()) {
            return false;
        }

        int amount = (int) parseValues(context, caster, AMOUNT, level, 1.0);
        if (amount <= 0) {
            return false;
        }

        String spread = settings.getString(SPREAD, "cone");
        if (spread == null) {
            spread = "cone";
        }
        spread = spread.toLowerCase(Locale.ROOT);
        boolean ally = "ally".equalsIgnoreCase(settings.getString(ALLY, "enemy"));

        // Use a copy so execution-specific values do not pollute the component.
        final Settings projectileSettings = new Settings(settings);
        projectileSettings.set("level", level);
        projectileSettings.set(
                ParticleProjectile.SPEED,
                parseValues(context, caster, ParticleProjectile.SPEED, level, 1.0),
                0
        );

        final EffectPlayer effectPlayer = settings.getBool(USE_EFFECT, false)
                ? new EffectPlayer(settings)
                : null;
        final String effectKey = effectPlayer == null
                ? null
                : settings.getString(EFFECT_KEY, skill.getName());

        for (LivingEntity target : targets) {
            Location origin = target.getLocation().clone();
            if ("rain".equals(spread)) {
                launchRain(context, caster, level, origin, projectileSettings, amount, ally,
                        effectPlayer, effectKey);
            } else {
                launchSpread(context, caster, level, origin, projectileSettings, amount, spread, ally,
                        effectPlayer, effectKey);
            }
        }

        return true;
    }

    private void launchSpread(
            SkillContext context,
            LivingEntity caster,
            int level,
            Location origin,
            Settings projectileSettings,
            int amount,
            String spread,
            boolean ally,
            EffectPlayer effectPlayer,
            String effectKey
    ) {
        Vector direction = origin.getDirection();

        double right = parseValues(context, caster, RIGHT, level, 0.0);
        double upward = parseValues(context, caster, UPWARD, level, 0.0);
        double forward = parseValues(context, caster, FORWARD, level, 0.0);

        Vector horizontal = direction.clone().setY(0);
        if (horizontal.lengthSquared() > 1.0E-10) {
            horizontal.normalize();
        }
        Vector rightDirection = horizontal.clone().crossProduct(UP);
        horizontal.multiply(forward).add(rightDirection.multiply(right));

        if ("horizontal cone".equals(spread)) {
            direction.setY(0);
            if (direction.lengthSquared() > 1.0E-10) {
                direction.normalize();
            }
        }

        double angle = parseValues(context, caster, ANGLE, level, 30.0);
        Location spawnLocation = origin.clone()
                .add(horizontal)
                .add(0, upward + 0.5, 0);

        ArrayList<Vector> directions = CustomProjectile.calcSpread(direction, angle, amount);
        launchProjectiles(
                context, caster, level, spawnLocation, projectileSettings,
                directions, ally, effectPlayer, effectKey
        );
    }

    private void launchRain(
            SkillContext context,
            LivingEntity caster,
            int level,
            Location origin,
            Settings projectileSettings,
            int amount,
            boolean ally,
            EffectPlayer effectPlayer,
            String effectKey
    ) {
        double radius = parseValues(context, caster, RADIUS, level, 2.0);
        double height = parseValues(context, caster, HEIGHT, level, 8.0);

        ArrayList<Location> spawnLocations = ParticleProjectile.calcRain(
                origin.clone(),
                radius,
                height,
                amount
        );

        for (Location spawnLocation : spawnLocations) {
            spawnLocation.setDirection(DOWN);
            launchProjectile(
                    context, caster, level, spawnLocation, projectileSettings,
                    ally, effectPlayer, effectKey
            );
        }
    }

    private void launchProjectiles(
            SkillContext context,
            LivingEntity caster,
            int level,
            Location spawnLocation,
            Settings projectileSettings,
            List<Vector> directions,
            boolean ally,
            EffectPlayer effectPlayer,
            String effectKey
    ) {
        for (Vector direction : directions) {
            Location projectileLocation = spawnLocation.clone();
            projectileLocation.setDirection(direction);
            launchProjectile(
                    context, caster, level, projectileLocation, projectileSettings,
                    ally, effectPlayer, effectKey
            );
        }
    }

    private void launchProjectile(
            SkillContext context,
            LivingEntity caster,
            final int level,
            Location location,
            Settings projectileSettings,
            final boolean ally,
            final EffectPlayer effectPlayer,
            final String effectKey
    ) {
        BounceProjectile projectile = new BounceProjectile(
                context,
                caster,
                level,
                location,
                projectileSettings
        );

        // The callback object is inherited by every generation. It initializes
        // metadata and follow effects for children created inside BounceProjectile.
        projectile.setBounceCallback(new BounceProjectileCallback() {
            @Override
            public void onSplit(BounceProjectileSplitEvent event) {
                for (BounceProjectile child : event.getChildren()) {
                    configureProjectile(child, level, ally, effectPlayer, effectKey);
                }
            }
        });
        configureProjectile(projectile, level, ally, effectPlayer, effectKey);
    }

    private void configureProjectile(
            BounceProjectile projectile,
            int level,
            boolean ally,
            EffectPlayer effectPlayer,
            String effectKey
    ) {
        SkillAPI.setMeta(projectile, LEVEL, level);
        projectile.setAllyEnemy(ally, !ally);
        projectile.setCallback(this);

        if (effectPlayer != null) {
            effectPlayer.start(
                    new FollowTarget(projectile),
                    effectKey,
                    9999,
                    level,
                    true
            );
        }
    }

    /** Entity hits use the entity; block hits use a temporary location target. */
    @Override
    public void callback(SkillContext context, CustomProjectile projectile, LivingEntity hit) {
        LivingEntity collisionTarget = hit;
        if (collisionTarget == null) {
            collisionTarget = new TempEntity(projectile.getLocation());
        }

        int level = SkillAPI.getMetaInt(projectile, LEVEL);
        castCollisionSkill(context, projectile, collisionTarget, level);

        ArrayList<LivingEntity> targets = new ArrayList<>();
        targets.add(collisionTarget);
        executeChildren(
                context,
                projectile.getShooter(),
                level,
                targets
        );
    }

    /**
     * Casts the configured collision skill through SkillCastAPI with the original
     * SkillContext. The projectile shooter is the caster and the collision target
     * is passed through; block collisions use a TempEntity at the exact contact
     * point so location-based child components do not fall back to the caster.
     */
    private void castCollisionSkill(
            SkillContext context,
            CustomProjectile projectile,
            LivingEntity target,
            int level
    ) {
        if (!(projectile instanceof BounceProjectile)) {
            return;
        }

        BounceProjectile bounceProjectile = (BounceProjectile) projectile;
        String skillName = bounceProjectile.getGeneration() == 0
                ? settings.getString(COLLISION_SKILL, "")
                : settings.getString(SPLIT_COLLISION_SKILL, "");
        if (skillName == null || skillName.trim().isEmpty()) {
            return;
        }

        Skill skill = SkillAPI.getSkill(skillName.trim());
        if (skill == null) {
            return;
        }

        SkillCastAPI.cast(
                context,
                projectile.getShooter(),
                skill,
                level,
                target
        );
    }
}
