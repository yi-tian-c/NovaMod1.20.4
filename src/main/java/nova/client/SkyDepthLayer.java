package nova.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public final class SkyDepthLayer extends RenderLayer {

    public static final RenderLayer INSTANCE = RenderLayer.of(
            "novamod_sky_depth",
            VertexFormats.POSITION,
            VertexFormat.DrawMode.QUADS,
            256,
            false,
            false,
            MultiPhaseParameters.builder()
                    .program(RenderPhase.POSITION_PROGRAM)   // 最简单的位置着色器
                    .texture(RenderPhase.NO_TEXTURE)
                    .transparency(RenderPhase.NO_TRANSPARENCY)
                    .depthTest(RenderPhase.ALWAYS_DEPTH_TEST) // 强制写入
                    .cull(RenderPhase.DISABLE_CULLING)
                    .writeMaskState(new RenderPhase.WriteMaskState(false, true)) // 不写颜色，只写深度
                    .build(false)
    );

    private SkyDepthLayer() {
        super("novamod_sky_depth_access", VertexFormats.POSITION, VertexFormat.DrawMode.QUADS, 0, false, false, () -> {}, () -> {});
    }
}