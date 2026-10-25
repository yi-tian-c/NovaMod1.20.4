package org.novamod.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

/**
 * 为天空快照表面提供带深度测试的实体绘制层。
 */
public final class SkyboxSurfaceLayer extends RenderLayer {
    public static final RenderLayer INSTANCE = RenderLayer.of(
            "novamod_skybox_surface",
            VertexFormats.POSITION,
            VertexFormat.DrawMode.QUADS,
            256,
            false,
            false,
            MultiPhaseParameters.builder()
                    .program(new RenderPhase.ShaderProgram(SkyboxShader::getProgram))
                    .texture(RenderPhase.NO_TEXTURE)
                    .transparency(RenderPhase.NO_TRANSPARENCY)
                    .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .writeMaskState(RenderPhase.ALL_MASK)
                    .build(false)
    );

    private SkyboxSurfaceLayer() {
        super("novamod_skybox_surface_access", VertexFormats.POSITION, VertexFormat.DrawMode.QUADS, 0, false, false, () -> {
        }, () -> {
        });
    }
}
