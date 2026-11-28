package at.hannibal2.skyhanni.utils.render

import com.mojang.renderpearl.api.pipeline.RenderPipeline
import net.minecraft.client.renderer.rendertype.LayeringTransform
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.resources.Identifier
import net.minecraft.util.Util

//? if < 26.2 {
/*import at.hannibal2.skyhanni.utils.render.layers.ChromaRenderLayer
*///?}

object SkyHanniRenderLayers {
    private val FILLED = SkyHanniRenderPipeline.FILLED.createRenderType()

    private val FILLED_XRAY = SkyHanniRenderPipeline.FILLED_XRAY.createRenderType()

    private val TRIANGLES = SkyHanniRenderPipeline.TRIANGLES.createRenderType()

    private val TRIANGLES_XRAY = SkyHanniRenderPipeline.TRIANGLES_XRAY.createRenderType()

    private val TRIANGLE_FAN = SkyHanniRenderPipeline.TRIANGLE_FAN.createRenderType()

    private val TRIANGLE_FAN_XRAY = SkyHanniRenderPipeline.TRIANGLE_FAN_XRAY.createRenderType()

    private val QUADS = SkyHanniRenderPipeline.QUADS.createRenderType()

    private val QUADS_XRAY = SkyHanniRenderPipeline.QUADS_XRAY.createRenderType()

    private val CHROMA_TEXTURED: java.util.function.Function<Identifier, RenderType> = Util.memoize { texture ->
        //~ if < 26.2 'RenderType.create' -> 'ChromaRenderLayer'
        RenderType.create(
            "skyhanni_text_chroma",
            //? if >= 26.2 {
            RenderSetup.builder(SkyHanniRenderPipeline.CHROMA_TEXT())
                .withTexture("Sampler0", texture)
                .createRenderSetup(),
            //?} else
            //texture = texture,
        )
    }

    private val LINES = SkyHanniRenderPipeline.LINES.createRenderType {
        setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
    }

    private val LINES_XRAY = SkyHanniRenderPipeline.LINES_XRAY.createRenderType {
        setLayeringTransform(LayeringTransform.NO_LAYERING)
    }

    private inline fun SkyHanniRenderPipeline.createRenderType(
        crossinline configure: RenderSetup.RenderSetupBuilder.() -> Unit = {},
    ): RenderType {
        val setup = RenderSetup.builder(invoke()).apply {
            //? if >= 26.3
            oitPipelines()?.let(::setOitPipelines)
            configure()
        }.createRenderSetup()

        return RenderType.create(
            "skyhanni_${name.lowercase()}",
            setup,
        )
    }

    fun getFilled(throughWalls: Boolean): RenderType {
        return if (throughWalls) FILLED_XRAY else FILLED
    }

    fun getTriangles(throughWalls: Boolean): RenderType {
        return if (throughWalls) TRIANGLES_XRAY else TRIANGLES
    }

    fun getTriangleFan(throughWalls: Boolean): RenderType {
        return if (throughWalls) TRIANGLE_FAN_XRAY else TRIANGLE_FAN
    }

    fun getQuads(throughWalls: Boolean): RenderType {
        return if (throughWalls) QUADS_XRAY else QUADS
    }

    fun getLines(throughWalls: Boolean): RenderType {
        return if (throughWalls) LINES_XRAY else LINES
    }

    fun getChromaTexturedWithIdentifier(identifier: Identifier) = CHROMA_TEXTURED.apply(identifier)

    fun getChromaStandard(): RenderPipeline = SkyHanniRenderPipeline.CHROMA_STANDARD()
    fun getChromaTextured(): RenderPipeline = SkyHanniRenderPipeline.CHROMA_TEXT()
}
