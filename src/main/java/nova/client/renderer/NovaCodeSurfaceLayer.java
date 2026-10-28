package nova.client.renderer;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

/**
 * NovaBlock 表面绘制层（带深度测试，关闭背面剔除）。
 */
public final class NovaCodeSurfaceLayer extends RenderLayer {
    public static final RenderLayer INSTANCE = RenderLayer.of(
            "novamod_nova_code_surface",
            VertexFormats.POSITION,
            VertexFormat.DrawMode.QUADS,
            256,
            false,
            false,
            MultiPhaseParameters.builder()
                    .program(new ShaderProgram(NovaCodeShader::getProgram))
                    .texture(RenderPhase.NO_TEXTURE)
                    .transparency(RenderPhase.NO_TRANSPARENCY)
                    .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .writeMaskState(RenderPhase.ALL_MASK)
                    .build(false)
    );

    private NovaCodeSurfaceLayer() {
        super("novamod_nova_code_surface_access", VertexFormats.POSITION, VertexFormat.DrawMode.QUADS, 0, false, false, () -> {
        }, () -> {
        });
    }
}
