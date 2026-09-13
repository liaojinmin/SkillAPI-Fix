package com.sucy.skill.hook.neonflash

import me.neon.flash.attribute.AttributeManager
import me.neon.libs.core.LifeCycle
import me.neon.libs.core.inject.Awake

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.neonflash
 *
 * @author 老廖
 * @since 2026/8/28 14:03
 */
object NeonFlashManager {

    @Awake(LifeCycle.ENABLE)
    fun register() {
        AttributeManager.register(ManaAttribute(), BearingAttribute(), LossAttribute())
    }
}