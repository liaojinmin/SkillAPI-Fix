package com.sucy.skill.dynamic.trigger

import com.sucy.skill.api.Settings
import me.neon.flash.api.event.AttributeChangeEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class AttributeChangeTrigger: Trigger<AttributeChangeEvent> {

    override fun getKey(): String {
        return "ATTRIBUTE_CHANGE"
    }

    override fun getEvent(): Class<AttributeChangeEvent> {
        return AttributeChangeEvent::class.java
    }

    override fun getTarget(event: AttributeChangeEvent, settings: Settings): LivingEntity {
        return event.attributePlayer.player
    }

    override fun getCaster(event: AttributeChangeEvent): LivingEntity {
        return event.attributePlayer.player
    }

    override fun setValues(event: AttributeChangeEvent, data: MutableMap<String, Any>) {
        val oldValue = event.oldData?.getSourceAttributeValue() ?: 0.0
        val newValue = event.newData?.getSourceAttributeValue() ?: 0.0
        val base = event.baseValue
        val delta = if (oldValue <= 0) newValue else newValue - oldValue
        val deltaRate = if (base != 0.0) delta / base * 100 else 0.0
        val totalRate = if (base != 0.0 && newValue > 0.0) newValue / base * 100 else 0.0
        data["nf_oldValue"] = oldValue // 旧值
        data["nf_newValue"] = newValue // 新值
        data["nf_baseValue"] = base // 基础值
        data["nf_delta"] = delta // 变化值
        data["nf_deltaRate"] = deltaRate // 本次比例
        data["nf_totalRate"] = totalRate // 累计比例
    }

    override fun shouldTrigger(event: AttributeChangeEvent, level: Int, settings: Settings): Boolean {
        val type = settings.getStringList("container")
        // 建议容器id，分别代表副本、对战，这样可以快速区分属性是在哪里触发变更的
        // 副本 >>> geekteamplus:runic
        // 对战 >>> neonarena:runic
        var accept = false
        if (type.isNotEmpty()) {
            val id = event.attributeContainer.resourceLocation.toString()

            for (t in type) {
                if (t.isEmpty()) {
                    accept = true
                    continue
                }
                if (t == id) {
                    accept = true
                    break
                } else accept = false
            }
            if (!accept) {
                return false
            }
        }

        val attr = settings.getStringList("attributes")
        if (attr.isEmpty() || (attr.size == 1 && attr.first().isEmpty())) {
            return true
        }
        return attr.contains(event.attribute.attributeName)
    }


}