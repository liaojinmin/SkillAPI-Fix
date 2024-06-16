package com.sucy.skill.screen

import com.germ.germplugin.api.dynamic.gui.GermGuiButton
import com.germ.germplugin.api.dynamic.gui.GermGuiCanvas
import com.germ.germplugin.api.dynamic.gui.GermGuiLabel
import com.germ.germplugin.api.dynamic.gui.GermGuiScroll
import com.sucy.skill.SkillAPI
import com.sucy.skill.api.player.PlayerData
import com.sucy.skill.manager.AttributeManager
import com.sucy.skill.manager.AttributeManager.Attribute
import me.neon.libs.hook.PluginHookImpl
import org.bukkit.entity.Player

/**
 * SkillAPI-Fix
 * com.sucy.skill.screen
 *
 * @author 老廖
 * @since 2024/5/7 0:22
 */
class AttributeGermScreen(
    private val viewer: Player,
    private val data: PlayerData
): AbstractScreen(viewer.name + "-属性加点-UI", ConfigManager.attributeSection) {

    private val economyText: List<String> = getGuiPart("金币组件", GermGuiLabel::class.java).texts
    private val topText: List<String> = getGuiPart("头部文本", GermGuiLabel::class.java).texts
    private val attributeCanvas: GermGuiCanvas

    init {
        initButton()
        updateInfo()
        attributeCanvas = getGuiPart("列表画布", GermGuiScroll::class.java)
            .getGuiPart("属性画布", GermGuiCanvas::class.java)!!.clone()
        getGuiPart("列表画布", GermGuiScroll::class.java).clearGuiPart()
        initCanvasList()
    }

    private fun initButton() {
        getGuiPart("洗点按钮", GermGuiButton::class.java)?.apply {
            this.registerCallbackHandler({ _, _ ->
                PluginHookImpl.money?.let {
                    if (data.hasInvestedAttributes()) {
                        if (it.hasTakeMoney(viewer, AttributeManager.money)) {
                            data.refundAttributes()
                            updateInfo()
                            initCanvasList()
                            setMessage(AttributeManager.accept)
                        } else {
                            setMessage(AttributeManager.deny)
                        }
                    } else {
                        setMessage(AttributeManager.notPoints)
                    }
                }
            }, GermGuiButton.EventType.LEFT_CLICK)
        }
    }

    private fun updateInfo() {
        getGuiPart("金币组件", GermGuiLabel::class.java)?.apply {
            texts = economyText.map { it.replace("{0}", (PluginHookImpl.money?.getMoney(viewer)?.toInt() ?: 0).toString()) }
        }
        getGuiPart("头部文本", GermGuiLabel::class.java)?.apply {
            texts = topText.map { it.replace("{player}", viewer.name).replace("{points}", data.attributePoints.toString()) }
        }
    }

    private fun initCanvasList() {
        getGuiPart("列表画布", GermGuiScroll::class.java)?.apply {
            SkillAPI.getAttributeManager().screenAttributes.forEach {
               // println("初始化 ${it.key}")
                val canvas = attributeCanvas.clone()
                updateAttribute(it, canvas)
                addGuiPart(canvas)
            }
        }
    }

    private fun updateAttribute(attribute: Attribute, canvas: GermGuiCanvas) {
        canvas.indexName = attribute.key
        // texture TODO
        // text
        canvas.getGuiPart("图标标题", GermGuiLabel::class.java).setText(attribute.getIconDisplay(data))
        canvas.getGuiPart("图标描述", GermGuiLabel::class.java).texts = attribute.getIconLore(data)
        canvas.getGuiPart("加点按钮", GermGuiButton::class.java)?.apply {
            this.registerCallbackHandler({ _, _ ->
                if (!data.upAttribute(attribute.key)) {
                    setMessage(AttributeManager.notLevel)
                } else {
                    setMessage(AttributeManager.hasLevel)
                    updateAttribute(attribute, canvas)
                    updateInfo()
                }
            }, GermGuiButton.EventType.LEFT_CLICK)
        }
    }
}