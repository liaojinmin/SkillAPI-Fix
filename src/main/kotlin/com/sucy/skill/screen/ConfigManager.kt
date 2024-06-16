package com.sucy.skill.screen

import com.sucy.skill.SkillAPI
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * SkillAPI-Fix
 * com.sucy.skill.screen
 *
 * @author 老廖
 * @since 2024/5/7 1:03
 */
object ConfigManager {

    private val screenFile: File by lazy {
        val file = File(SkillAPI.singleton().dataFolder, "screen")
        if (!file.exists()) {
            file.mkdirs()
        }
        file
    }

    lateinit var attributeSection: ConfigurationSection
        private set


    fun loader() {
        loadAttribute()
    }

    private fun loadAttribute() {
        val file = File(screenFile, "attribute.yml")
        if (!file.exists()) {
            SkillAPI.singleton().saveResource("screen/attribute.yml", true)
        }
        val yaml = YamlConfiguration.loadConfiguration(file)
        attributeSection = yaml.getConfigurationSection("属性加点-UI")
    }

}