package com.sucy.skill.screen

import com.rit.sucy.config.Filter
import com.rit.sucy.config.FilterType
import com.sucy.skill.SkillAPI
import com.sucy.skill.api.player.PlayerData
import com.sucy.skill.language.GUINodes
import com.sucy.skill.language.RPGFilter
import com.sucy.skill.manager.AttributeManager
import me.neon.libs.hook.PluginHookImpl
import me.neon.libs.taboolib.ui.ClickType
import me.neon.libs.taboolib.ui.openMenu
import me.neon.libs.taboolib.ui.type.PageableChest
import me.neon.libs.util.item.buildItem
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * SkillAPI-Fix
 * com.sucy.skill.screen
 *
 * @author 老廖
 * @since 2024/5/6 20:20
 */
internal object AttributeScreen {

    val sl = listOf(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)
}

fun PlayerData.getAttributeTitle(): String {
    return SkillAPI.getLanguage().getMessage(
        GUINodes.ATTRIB_TITLE,
        true,
        FilterType.COLOR,
        RPGFilter.POINTS.setReplacement(attributePoints.toString() + ""),
        Filter.PLAYER.setReplacement(playerName)
    )[0]
}

fun Player.openAttributeScreen(data: PlayerData) {
    playSound(location, Sound.UI_BUTTON_CLICK, 1f, 2f)
    openMenu<PageableChest<AttributeManager.Attribute>>(
        data.getAttributeTitle()
    ) {

        rows(6)

        slots(AttributeScreen.sl)

        elements {
            SkillAPI.getAttributeManager().screenAttributes.sortedBy { it.iconPriority }
        }

        onGenerate { _, element, _, _ ->
            element.getIcon(data)
        }

        onClick { event, element ->
            if (event.clickType == ClickType.CLICK) {
                if (data.upAttribute(element.key)) {
                    updateTitle(data.getAttributeTitle())
                } else {
                    sendMessage(AttributeManager.notLevel)
                }
            }
        }

        set(49, buildItem(Material.BOOK) { name = "§7洗点" }) {
            PluginHookImpl.money?.let {
                if (data.hasInvestedAttributes()) {
                    if (it.hasTakeMoney(this@openAttributeScreen, AttributeManager.money)) {
                        data.refundAttributes()
                        updateTitle(data.getAttributeTitle())
                        sendMessage(AttributeManager.accept)
                    } else {
                        sendMessage(AttributeManager.deny)
                    }
                } else {
                    sendMessage(AttributeManager.notPoints)
                }
            }
        }

        setNextPage(51) { _, hasNextPage ->
            if (hasNextPage) {
                buildItem(Material.ARROW) { name = "§7下一页" }
            } else {
                buildItem(Material.ARROW) { name = "§8下一页" }
            }
        }
        setPreviousPage(47) { _, hasPreviousPage ->
            if (hasPreviousPage) {
                buildItem(Material.ARROW) { name = "§7上一页" }
            } else {
                buildItem(Material.ARROW) { name = "§8上一页" }
            }
        }
    }
}