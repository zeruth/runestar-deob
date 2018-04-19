package org.runestar.client.plugins.dev

import org.runestar.client.game.api.SceneObject
import org.runestar.client.game.api.live.*
import org.runestar.client.game.raw.access.XScene
import org.runestar.client.plugins.spi.PluginSettings
import org.runestar.client.utils.DisposablePlugin
import java.awt.Color

class ObjectClickBoxDebug : DisposablePlugin<PluginSettings>() {

    override val defaultSettings = PluginSettings()

    private val objs: MutableSet<SceneObject> = LinkedHashSet()

    override fun start() {
        add(XScene.clear.exit.subscribe { objs.clear() })
        add(SceneObjects.removals.filter(SceneObject::isInteractable).subscribe { objs.remove(it) })
        add(SceneObjects.additions.filter(SceneObject::isInteractable).subscribe { objs.add(it) })

        val color = Color(0, 255, 255, 80)
        add(LiveCanvas.repaints.subscribe { g ->
            g.color = color
            val viewport = LiveViewport.shape
            val cam = LiveCamera.position.sceneTile
            objs.forEach {
                val loc = it.location
                if (loc.plane != Game.plane || !loc.isVisible(cam)) return@forEach
                val pt = loc.center.toScreen() ?: return@forEach
                if (pt !in viewport) return@forEach
                it.models.forEach {
                    g.fill(it.objectClickBoxOutline())
                }
            }
        })
    }
}