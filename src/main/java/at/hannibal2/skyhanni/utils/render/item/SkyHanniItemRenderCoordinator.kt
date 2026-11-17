package at.hannibal2.skyhanni.utils.render.item

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.VectorUtils.isEqualTo
import at.hannibal2.skyhanni.utils.render.item.atlas.SkyHanniItemAtlas
import net.minecraft.client.renderer.ProjectionMatrixBuffer
import net.minecraft.client.renderer.SubmitNodeStorage
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher
import net.minecraft.client.renderer.state.gui.GuiRenderState
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

@SkyHanniModule
internal object SkyHanniItemRenderCoordinator {
    @HandleEvent
    private fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Item Atlas")
        event.addIrrelevant {
            atlas.atlasDebugInfo().onEach(::add)
            add("Realtime slot pools: ${slotPools.size} sizes, ${slotPools.values.sumOf { it.slots.size }} slots")
            add("Settle tracker entries: ${settleTracker.size}")
        }
    }

    private const val SETTLE_FRAMES = 4
    private const val IDLE_EVICT_FRAMES = 100
    private const val SCALE_EPSILON = 0.01f
    private const val ITEM_PIXELS = 16

    private val submitNodeStorage = SubmitNodeStorage()
    private val projectionBuffer by lazy { ProjectionMatrixBuffer("SkyHanni items") }
    private val atlas by lazy { SkyHanniItemAtlas() }

    private class RealtimeSlotPool {
        val slots = ArrayList<SkyHanniRealtimeItemSlot>()
        var cursor = 0
    }

    private val slotPools = HashMap<Int, RealtimeSlotPool>()
    private val settleTracker = HashMap<Int, SettleEntry>()
    private var frameNumber = 0
    private var preparedThisFrame = false

    fun invalidateAtlas() {
        atlas.invalidate()
        settleTracker.clear()
        closeRealtimeSlots()
    }

    fun closeAtlas() {
        atlas.close()
        projectionBuffer.close()
        closeRealtimeSlots()
    }

    fun prepare(
        state: SkyHanniGuiItemRenderState,
        guiRenderState: GuiRenderState,
        featureRenderDispatcher: FeatureRenderDispatcher,
        guiScale: Int,
    ) {
        if (!preparedThisFrame) {
            preparedThisFrame = true
            slotPools.values.forEach { it.cursor = 0 }
        }
        val context = SkyHanniItemRenderContext(submitNodeStorage, featureRenderDispatcher, frameNumber, guiScale)
        if (isSettled(state)) {
            val hit = with(atlas) { context.renderAndBlit(state, guiRenderState, projectionBuffer) }
            if (hit) return
        }
        val slotSize = (ITEM_PIXELS * guiScale * state.adjustedScale).toInt()
        acquireRealtimeSlot(slotSize).render(context, state, guiRenderState, projectionBuffer)
    }

    @JvmStatic
    fun endFrame() {
        if (frameNumber % IDLE_EVICT_FRAMES == 0) {
            settleTracker.values.removeIf { frameNumber - it.lastSeenFrame > IDLE_EVICT_FRAMES }
        }
        slotPools.values.removeIf { pool ->
            while (pool.slots.isNotEmpty() && frameNumber - pool.slots.last().lastUsedFrame > IDLE_EVICT_FRAMES) {
                pool.slots.removeLast().close()
            }
            pool.slots.isEmpty()
        }
        atlas.pruneFrames(frameNumber)
        preparedThisFrame = false
        frameNumber++
    }

    private fun isSettled(state: SkyHanniGuiItemRenderState): Boolean {
        val settle = settleTracker.getOrPut(state.stableId) {
            SettleEntry(state.rotationVector, state.adjustedScale, 0, frameNumber)
        }
        settle.lastSeenFrame = frameNumber
        val stable = settle.rotationVec.isEqualTo(state.rotationVector) &&
            abs(settle.lastScale - state.adjustedScale) < SCALE_EPSILON
        if (stable) {
            settle.framesStable++
        } else {
            settle.rotationVec = state.rotationVector
            settle.lastScale = state.adjustedScale
            settle.framesStable = 0
        }
        return settle.framesStable >= SETTLE_FRAMES || (stable && !state.isAnimated())
    }

    private fun acquireRealtimeSlot(slotSize: Int): SkyHanniRealtimeItemSlot {
        val pool = slotPools.getOrPut(slotSize) { RealtimeSlotPool() }
        val slot = pool.slots.getOrNull(pool.cursor) ?: SkyHanniRealtimeItemSlot(slotSize).also(pool.slots::add)
        pool.cursor++
        slot.lastUsedFrame = frameNumber
        return slot
    }

    private fun closeRealtimeSlots() {
        slotPools.values.forEach { pool -> pool.slots.forEach { it.close() } }
        slotPools.clear()
    }

    private data class SettleEntry(
        var rotationVec: Vec3,
        var lastScale: Float,
        var framesStable: Int,
        var lastSeenFrame: Int,
    )
}
