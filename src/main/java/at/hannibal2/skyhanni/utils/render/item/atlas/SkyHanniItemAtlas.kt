package at.hannibal2.skyhanni.utils.render.item.atlas

import at.hannibal2.skyhanni.utils.render.atlas.SkyHanniAbstractAtlas
import at.hannibal2.skyhanni.utils.render.item.SkyHanniGuiItemRenderState
import at.hannibal2.skyhanni.utils.render.item.SkyHanniItemRenderContext
import net.minecraft.client.renderer.ProjectionMatrixBuffer
import net.minecraft.client.renderer.state.gui.GuiRenderState
import net.minecraft.resources.Identifier

internal class SkyHanniItemAtlas : SkyHanniAbstractAtlas<SkyHanniAtlasKey, SkyHanniItemAtlasEntry>() {

    override val identifier: Identifier by lazy {
        Identifier.fromNamespaceAndPath("skyhanni", "item_atlas")
    }

    private companion object {
        const val ITEM_PIXELS = 16
    }

    override val colorLabel = "SkyHanni item atlas"
    override val depthLabel = "SkyHanni item atlas depth"

    private var renderer: SkyHanniItemAtlasRenderer? = null

    override fun onAllocated() {
        @Suppress("UnsafeCallOnNullableType")
        renderer = SkyHanniItemAtlasRenderer(sizePixels, textureView!!, depthTextureView!!, texture!!, depthTexture!!)
    }

    fun pruneFrames(currentFrame: Int, olderThanLastRenderedFrames: Int = 2) {
        entries.entries.removeIf { (key, pos) ->
            key is SkyHanniAnimatedAtlasKey && pos is SkyHanniAnimatedItemAtlasEntry &&
                currentFrame - pos.lastRenderedFrame > olderThanLastRenderedFrames
        }
    }

    private fun SkyHanniAnimatedAtlasKey.clearPreviousFrame() {
        val prevEntry = entries[this.copy(frameNumber = frameNumber - 1)] ?: return
        renderer?.clearSlot(prevEntry.x, prevEntry.y, prevEntry.pixelSize)
    }

    private fun recordPosition(key: SkyHanniAtlasKey, slotX: Int, slotY: Int, pixelSize: Int) {
        val (u, v) = uvForSlot(slotX, slotY)
        entries[key] = if (key is SkyHanniAnimatedAtlasKey) {
            SkyHanniAnimatedItemAtlasEntry(slotX, slotY, u, v, pixelSize, key.frameNumber)
        } else {
            SkyHanniItemAtlasEntry(slotX, slotY, u, v, pixelSize)
        }
    }

    fun SkyHanniItemRenderContext.renderAndBlit(
        state: SkyHanniGuiItemRenderState,
        guiRenderState: GuiRenderState,
        projectionBuffer: ProjectionMatrixBuffer,
    ): Boolean {
        ensureAllocated()
        val renderer = renderer ?: return false
        val key = state.atlasKey
        val neededPixels = (ITEM_PIXELS * guiScale * state.adjustedScale).toInt()
        val existing = entries[key]
        val entry = if (existing != null && existing.pixelSize >= neededPixels) {
            if (key is SkyHanniAnimatedAtlasKey) key.clearPreviousFrame()
            existing
        } else {
            val node = tryInsert(neededPixels) ?: return false
            renderer.render(projectionBuffer) {
                renderer.renderItemToAtlas(state, node.x, node.y, neededPixels, submitNodeStorage, featureRenderDispatcher)
            }
            recordPosition(key, node.x, node.y, neededPixels)
            entries.getValue(key)
        }
        if (entry is SkyHanniAnimatedItemAtlasEntry) {
            entries[key] = SkyHanniAnimatedItemAtlasEntry(entry.x, entry.y, entry.u, entry.v, entry.pixelSize, frameNumber)
        }
        renderer.submitBlitForState(state, guiRenderState, entry)
        return true
    }

    override fun close() {
        super.close()
        renderer = null
    }
}
