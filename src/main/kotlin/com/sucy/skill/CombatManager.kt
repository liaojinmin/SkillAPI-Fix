package com.sucy.skill

import me.neon.libs.event.SubscribeEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.scheduler.BukkitTask
import java.util.*

/**
 * SkillAPI-Fix
 * com.sucy.skill
 *
 * @author 老廖
 * @since 2026/1/28 17:42
 */
object CombatManager: Listener{

    data class CombatState(
        val uuid: UUID,
        var lastCombatTime: Long
    )

    class PlayerEnterCombatEvent(
        val player: Player
    ) : Event() {

        override fun getHandlers(): HandlerList = handlerList

        companion object {
            @JvmStatic
            val handlerList = HandlerList()
        }
    }

    class PlayerLeaveCombatEvent(
        val player: Player
    ) : Event() {

        override fun getHandlers(): HandlerList = handlerList

        companion object {
            @JvmStatic
            val handlerList = HandlerList()
        }
    }

    /** 脱战时间（毫秒） */
    private const val COMBAT_TIMEOUT = 3_000L

    private var bukkitTask: BukkitTask? = null

    private val combatMap = HashMap<UUID, CombatState>()

    /** 玩家是否在战斗中 */
    fun isInCombat(player: Player): Boolean {
        return combatMap.containsKey(player.uniqueId)
    }

    fun getRemainTime(player: Player): Long {
        val state = combatMap[player.uniqueId] ?: return 0
        return (COMBAT_TIMEOUT - (System.currentTimeMillis() - state.lastCombatTime))
            .coerceAtLeast(0)
    }

    /** 进入 / 刷新战斗 */
    fun markCombat(player: Player) {
        val now = System.currentTimeMillis()
        val uuid = player.uniqueId

        val existed = combatMap.containsKey(uuid)
        combatMap[uuid] = CombatState(uuid, now)

        if (!existed) {
            Bukkit.getPluginManager().callEvent(
                PlayerEnterCombatEvent(player)
            )
        }
    }

    /** 强制脱战 */
    fun leaveCombat(player: Player) {
        if (combatMap.remove(player.uniqueId) != null) {
            Bukkit.getPluginManager().callEvent(
                PlayerLeaveCombatEvent(player)
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDamage(e: EntityDamageByEntityEvent) {
        val damager = e.damager
        val world = damager.world.name
        if (world == "spawn" || world == "world") return
        val victim = e.entity

        if (damager is Player) {
            markCombat(damager)
        }
        if (victim is Player) {
            markCombat(victim)
        }
    }

    fun start() {
        bukkitTask?.cancel()
        bukkitTask = Bukkit.getScheduler().runTaskTimer(SkillAPI.singleton(), ::tick, 20, 20)
    }

    fun close() {
        bukkitTask?.cancel()
    }

    /** 定时检查脱战 */
    private fun tick() {
        val now = System.currentTimeMillis()
        val it = combatMap.iterator()

        while (it.hasNext()) {
            val entry = it.next()
            if (now - entry.value.lastCombatTime >= COMBAT_TIMEOUT) {
                val player = Bukkit.getPlayer(entry.key)
                it.remove()
                if (player != null && player.isOnline) {
                    Bukkit.getPluginManager().callEvent(
                        PlayerLeaveCombatEvent(player)
                    )
                }
            }
        }
    }

}