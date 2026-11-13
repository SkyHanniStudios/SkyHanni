package at.hannibal2.skyhanni.utils.render.item

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher
import net.minecraft.client.renderer.state.gui.GuiRenderState

@Suppress("EmptyDefaultConstructor")
class SkyHanniPipCoordinatorRenderer : PictureInPictureRenderer<SkyHanniGuiItemRenderState>() {

    override fun prepare(
        renderState: SkyHanniGuiItemRenderState,
        guiRenderState: GuiRenderState,
        featureRenderDispatcher: FeatureRenderDispatcher,
        guiScale: Int,
    ) = SkyHanniItemRenderCoordinator.prepare(renderState, guiRenderState, featureRenderDispatcher, guiScale)

    override fun renderToTexture(
        state: SkyHanniGuiItemRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
    ) = Unit

    override fun getRenderStateClass(): Class<SkyHanniGuiItemRenderState> = SkyHanniGuiItemRenderState::class.java
    override fun getTextureLabel(): String = "skyhanni_item_coordinator"
}
