package com.sucy.skill.hook.mythic.mechanic

import com.sucy.skill.hook.mythic.MythicManager
import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.io.ConfigManager
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.logging.MythicLogger
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.damage.DamageMetadata
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderDouble
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic
import me.geek.team.common.TeamManager
import me.neon.arena.utils.getGameTeamManager
import org.bukkit.Bukkit
import org.bukkit.EntityEffect
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic.summon
 *
 * @author 老廖
 * @since 2025/10/23 21:52
 */
@MythicMechanic(author = "SkillAPI",
    name = "SkSummonDamage",
    aliases = ["ssd"],
    description = "召唤攻击方法"
)
class SummonDamageMechanic(
    line: String,
    mlc: MythicLineConfig
): SkillMechanic(line, mlc), ITargetedEntitySkill {

    private val amount: PlaceholderDouble = PlaceholderDouble.of(mlc.getString(arrayOf("amount", "a"), "1", *arrayOfNulls(0)))
    private val ignoresArmor: Boolean = mlc.getBoolean(arrayOf("ignorearmor", "ia", "i"), false)
    private val preventImmunity: Boolean = mlc.getBoolean(arrayOf("preventimmunity", "pi"), false)
    private val preventKnockback: Boolean = mlc.getBoolean(arrayOf("preventknockback", "pkb", "pk"), false)
    private var element: String?

    init {
        element = mlc.getString(arrayOf("element", "e", "damagetype", "type"), null, *arrayOfNulls(0))
        if (element != null && !MythicMobs.isVolatile()) {
            MythicLogger.errorMechanicConfig(this, mlc, "Custom damage types require MythicMobs Premium to use.")
            element = null
        }
    }

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): Boolean {
        return if (!target.isDead && !data.caster.isUsingDamageSkill && (!target.isLiving || target.health > 0.0)) {
            val damage: Double = amount[data, target] * data.power.toDouble()
            val meta = DamageMetadata(data.caster, damage, element, ignoresArmor, preventImmunity, preventKnockback)
            doDamage(meta, target)
            true
        } else {
            false
        }
    }

    private fun doDamage(meta: DamageMetadata, aTarget: AbstractEntity) {
        val am = meta.damager
        val damage = meta.amount
        if (!aTarget.isDamageable) {
            return
        } else {
            val source = BukkitAdapter.adapt(am.entity) as LivingEntity
            val target = BukkitAdapter.adapt(aTarget) as LivingEntity
            val owner: LivingEntity = MythicManager.summonAscription[am.entity.uniqueId] ?: return

            am.isUsingDamageSkill = true
            am.entity.setMetadata("doing-skill-damage", true)
            aTarget.setMetadata("skill-damage", meta)

            if (am is ActiveMob) {
                am.lastDamageSkillAmount = damage
            }

            if (owner.uniqueId == target.uniqueId) {
                am.entity.removeMetadata("doing-skill-damage")
                am.isUsingDamageSkill = false
                aTarget.removeMetadata("skill-damage")
                return
            }

            if (target is Player) {
                val temp = TeamManager.getTeamByTeamID(owner.uniqueId)
                if (temp != null && temp.part.containPlayer(target.uniqueId)) {
                    am.entity.removeMetadata("doing-skill-damage")
                    am.isUsingDamageSkill = false
                    aTarget.removeMetadata("skill-damage")
                    return
                }
                if (owner is Player) {
                    if (target.getGameTeamManager()?.contains(owner) == true) {
                        am.entity.removeMetadata("doing-skill-damage")
                        am.isUsingDamageSkill = false
                        aTarget.removeMetadata("skill-damage")
                        return
                    }
                }
            }

            try {
                if (meta.ignoresArmor) {
                    val event = EntityDamageByEntityEvent(
                        owner ?: source,
                        target,
                        DamageCause.ENTITY_ATTACK,
                        damage
                    )
                    Bukkit.getServer().pluginManager.callEvent(event)

                    if (event.isCancelled) {
                        am.isUsingDamageSkill = false
                        return
                    }
                    if (target.health - damage < 1.0) {
                        target.lastDamageCause = event
                        target.health = 1.0E-4
                        if (meta.preventsKnockback) {
                            target.damage(10.0)
                        } else {
                            target.damage(10.0, owner ?: source)
                        }
                    } else {
                        target.health -= damage
                        target.playEffect(EntityEffect.HURT)
                    }
                } else if (meta.preventsKnockback) {
                    target.damage(damage)
                } else {
                    target.damage(damage, owner ?: source)
                }
            } catch (var12: Exception) {
                if (ConfigManager.debugLevel > 0) {
                    var12.printStackTrace()
                }
            } finally {
                am.entity.removeMetadata("doing-skill-damage")
                am.isUsingDamageSkill = false
                aTarget.removeMetadata("skill-damage")
            }
            if (meta.preventsImmunity) {
                target.noDamageTicks = 0
            }
        }
    }

}