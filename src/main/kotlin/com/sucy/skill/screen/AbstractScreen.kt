package com.sucy.skill.screen

import com.germ.germplugin.api.dynamic.gui.GermGuiLabel
import com.germ.germplugin.api.dynamic.gui.GermGuiScreen
import com.sucy.skill.SkillAPI
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.scheduler.BukkitTask

/**
 * SkillAPI-Fix
 * com.sucy.skill.screen
 *
 * @author 老廖
 * @since 2024/5/7 0:23
 */
abstract class AbstractScreen(
    name: String,
    section: ConfigurationSection
): GermGuiScreen(name, section) {

    private var messageTask: BukkitTask? = null

    fun setMessage(text: String) {
        val label = getGuiPart("消息组件", GermGuiLabel::class.java) ?: return
        if (label.isEnable) {
            messageTask?.cancel()
            label.setText(text)
        } else {
            label.enable = true
            label.setText(text)
        }
        messageTask = Bukkit.getScheduler().runTaskLater(SkillAPI.singleton(),  {
            getGuiPart("消息组件", GermGuiLabel::class.java).enable = false
        }, 30)
    }
}