package me.odinmain.features.impl.render

import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.clickgui.animations.impl.EaseInOut
import me.odinmain.ui.clickgui.util.ColorUtil.brighter
import me.odinmain.ui.hud.HudElement
import me.odinmain.ui.util.TextAlign
import me.odinmain.ui.util.dropShadow
import me.odinmain.ui.util.roundedRectangle
import me.odinmain.ui.util.text
import me.odinmain.utils.render.Color
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CPSDisplay : Module(
    "Cps Display",
    description = "Displays your CPS.",
    category = Category.RENDER
) {
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 2f, false) {
        leftClicks.removeAll { System.currentTimeMillis() - it > 1000 }
        rightClicks.removeAll { System.currentTimeMillis() - it > 1000 }

        val value = if (button == 0) "${leftClicks.size}" else "${rightClicks.size}"
        val anim = if (button == 0) leftAnim else rightAnim
        val color = color.brighter(leftAnim.get(1f, 1.5f, leftAnim.getPercent() >= 50))
        if (button == 2) {
            roundedRectangle(0f, 0f, 50f, 38f, color, color, color, 0f , 9f, 0f, 9f, 0f, 0f)
            roundedRectangle(50f, 0f, 50f, 38f, color, color, color, 0f , 0f, 9f, 0f, 9f, 0f)

            if (outline) dropShadow(0f, 0f, 100f, 36f, 10f)
        } else {
            roundedRectangle(0f, 0f, 50f, 36f, color.brighter(anim.get(1f, 1.5f, anim.getPercent() >= 50)), 9f)
            if (outline) dropShadow(0f, 0f, 50f, 36f, 10f)
        }

        if (mouseText) {
            if (button == 2) {
                text("LMB", 25f, 9f, textColor, 10f, OdinFont.BOLD, TextAlign.Middle)
                text(leftClicks.size.toString(), 25f, 30f, textColor, 18.5f, OdinFont.BOLD, TextAlign.Middle)

                text("RMB", 75f, 9f, textColor, 10f, OdinFont.BOLD, TextAlign.Middle)
                text(rightClicks.size.toString(), 75f, 30f, textColor, 18.5f, OdinFont.BOLD, TextAlign.Middle)
            } else {
                val text = if (button == 0) "LMB" else "RMB"
                text(text, 25f, 9f, textColor, 10f, OdinFont.BOLD, TextAlign.Middle)
                text(value, 25f, 20f, textColor, 28f, OdinFont.BOLD, TextAlign.Middle)
            }
        } else {
            if (button == 2) {
                text(leftClicks.size.toString(), 25f, 13f, textColor, 24f, OdinFont.BOLD, TextAlign.Middle)
                text(rightClicks.size.toString(), 75f, 13f, textColor, 24f, OdinFont.BOLD, TextAlign.Middle)
            } else text(value, 25f, 19f, textColor, 24f, OdinFont.BOLD, TextAlign.Middle)
        }
        if (button == 2) 100f to 38f else 50f to 38f

    }

    private val countPackets: Boolean by BooleanSetting("Count Packets", false, description = "Counts packets sent outside of the rightclickmouse method, this will be better at detecting other mods' autoclickers, but might show innacurate values.")

    private val advanced: Boolean by BooleanSetting("Settings", false)

    private val button: Int by SelectorSetting("Button", "Both", arrayListOf("Left", "Right", "Both"))
        .withDependency { advanced }

    private val mouseText: Boolean by BooleanSetting("Show Button", true)
        .withDependency { advanced }

    private val color: Color by ColorSetting("Color", Color(21, 22, 23, 0.25f), allowAlpha = true)
        .withDependency { advanced }

    private val textColor: Color by ColorSetting("Text Color", Color(239, 239, 239, 1f), allowAlpha = true)
        .withDependency { advanced }

    private val outline: Boolean by BooleanSetting("Outline", true)
        .withDependency { advanced }

    private val leftAnim = EaseInOut(300)
    private val rightAnim = EaseInOut(300)

    private val leftClicks = mutableListOf<Long>()
    private val rightClicks = mutableListOf<Long>()

    fun onLeftClick() {
        leftClicks.add(System.currentTimeMillis())
        leftAnim.start(true)
    }

    fun onRightClick() {
        rightClicks.add(System.currentTimeMillis())
        rightAnim.start(true)
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketSentEvent) { // This is for any block placement packet that gets sent outside the rightclickmouse method :eyes:
        if (event.packet !is C08PacketPlayerBlockPlacement || !countPackets) return
        if (rightClicks.any { System.currentTimeMillis() - it < 5 }) return
        onRightClick()
    }
}