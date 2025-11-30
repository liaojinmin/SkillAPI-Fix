package com.sucy.skill.hook.mythic

import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitTriggerMetadata
import io.lumine.xikage.mythicmobs.skills.SkillTrigger
import io.lumine.xikage.mythicmobs.skills.TriggeredSkill
import io.lumine.xikage.mythicmobs.utils.Events
import io.lumine.xikage.mythicmobs.utils.Schedulers
import io.lumine.xikage.mythicmobs.utils.plugin.PluginModule
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Projectile
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import kotlin.math.pow

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mythic
 *
 * @author 老廖
 * @since 2025/11/12 13:11
 */
class CombatTriggerListener: PluginModule<MythicMobs>(MythicMobs.inst()) {

    override fun load(p0: MythicMobs?) {
        Events.subscribe(EntityDamageByEntityEvent::class.java, EventPriority.HIGHEST)
            .filter { event: EntityDamageByEntityEvent -> !event.isCancelled }
            .handler { event: EntityDamageByEntityEvent ->
                this.onCombatTrigger(event)
            }.bindWith(this)
    }

    override fun unload() {

    }

    fun onCombatTrigger(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return

        if (event.entity is LivingEntity) {
            val damager: AbstractEntity? = when (val d = event.damager) {
                is LivingEntity -> BukkitAdapter.adapt(d)
                is Projectile -> {
                    val shooter = d.shooter
                    if (shooter is LivingEntity) BukkitAdapter.adapt(shooter) else null
                }
                else -> null
            }

            val damaged: AbstractEntity? =
                if (event.entity is LivingEntity) BukkitAdapter.adapt(event.entity as LivingEntity) else null

            damaged?.let { dmg ->
                val mobManager = MythicMobs.inst().mobManager
                if (mobManager.isActiveMob(dmg.uniqueId)) {
                    val am = mobManager.getMythicMobInstance(dmg)

                    damager?.let { dam ->
                        if (mobManager.isActiveMob(dam.uniqueId)) {
                            val am2 = mobManager.getMythicMobInstance(dam)

                            if (am == am2 && am.isUsingDamageSkill) {
                                return
                            }

                            if (am.faction != null && am2.faction != null && am.faction == am2.faction) {
                                if (!am.entity.hasMetadata("MythicFactionMechanic") && !am2.entity.hasMetadata("MythicFactionMechanic"))
                                    event.isCancelled = true
                                return
                            }
                        }
                    }

                    if (am.type.maxAttackableRange > 0 && damager != null) {
                        if (damaged.location.distanceSquared(damager.location) > am.type.maxAttackableRange.toDouble()
                                .pow(2.0)
                        ) {
                            event.isCancelled = true
                            return
                        }
                    } else if (am.type.maxAttackableRange == 0) {
                        event.isCancelled = true
                        return
                    }

                    if (am.hasImmunityTable()) {
                        if (am.immunityTable.onCooldown(damager)) {
                            event.isCancelled = true
                            return
                        }
                        am.immunityTable.setCooldown(damager)
                        Schedulers.sync().runLater({ dmg.setNoDamageTicks(0) }, 1L)
                    }

                    val ts = TriggeredSkill(
                        SkillTrigger.DAMAGED,
                        am,
                        damager,
                        false,
                        { meta -> BukkitTriggerMetadata.apply(meta, event) }
                    )
                    if (ts.cancelled) {
                        event.isCancelled = true
                    }

                    if (am.type.showNameOnDamaged) {
                        event.entity.isCustomNameVisible = true
                    }

                    if (am.type.usesThreatTable() &&
                        damager != null &&
                        am.entity.uniqueId != damager.uniqueId &&
                        am.type.threatTableUseDamageTaken
                    ) {
                        am.threatTable.threatGain(damager, event.damage)
                    }

                    am.type.entityDamageModifiers?.let { modifiers ->
                        var damage = event.damage
                        val mod = modifiers.getOrDefault(event.damager.type.toString(), 1.0)
                        if (mod != 1.0) {
                            damage *= mod
                            when {
                                damage > 0.0 -> event.damage = damage
                                damage == 0.0 -> event.damage = 0.0
                                damage < 0.0 -> {
                                    event.damage = 0.0
                                    event.isCancelled = true
                                    val newHealth = damaged.health - damage
                                    damaged.health = if (newHealth > damaged.maxHealth)
                                        damaged.maxHealth else newHealth
                                }
                            }
                        }
                    }
                }
            }

            damager?.let { dam ->
                val mobManager = MythicMobs.inst().mobManager
                if (mobManager.isActiveMob(dam.uniqueId)) {
                    val am = mobManager.getMythicMobInstance(dam)

                    if (am.owner.isPresent && am.owner.get() == (event.entity.uniqueId)) {
                        event.isCancelled = true
                        return
                    }

                    if (am.isUsingDamageSkill) {
                        event.damage = am.lastDamageSkillAmount
                    } else if (event.damager is org.bukkit.entity.Creeper &&
                        event.cause == DamageCause.ENTITY_EXPLOSION
                    ) {
                        if (am.damage != -1.0) {
                            event.damage = am.damage
                        }
                    }

                    if (!am.isUsingDamageSkill) {
                        val ts = TriggeredSkill(SkillTrigger.ATTACK, am, damaged)
                        if (ts.cancelled) {
                            event.isCancelled = true
                        }
                    }
                }
            }
        }
    }

}