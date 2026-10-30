package nova.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import nova.block.entity.SkyboxBlockEntity;
import org.joml.Matrix4f;

/**
 * 将本帧天空快照投影到天空盒方块的六个表面。
 */
public final class SkyboxBlockEntityRenderer implements BlockEntityRenderer<SkyboxBlockEntity> {
    public SkyboxBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(
            SkyboxBlockEntity blockEntity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        if (!SkyboxSnapshot.isAvailable()) {
            return;
        }

        var shader = SkyboxShader.getProgram();
        if (shader == null) {
            return;
        }
        shader.addSampler("Sampler0", SkyboxSnapshot.getFramebuffer());
        RenderSystem.setShaderTexture(0, SkyboxSnapshot.getFramebuffer().getColorAttachment());

        VertexConsumer vertices = vertexConsumers.getBuffer(SkyboxSurfaceLayer.INSTANCE);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        addFace(vertices, matrix, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1);
        addFace(vertices, matrix, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0);
        addFace(vertices, matrix, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0);
        addFace(vertices, matrix, 0, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1);
        addFace(vertices, matrix, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0);
        addFace(vertices, matrix, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1);
    }

    private static void addFace(
            VertexConsumer vertices,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4
    ) {
        vertices.vertex(matrix, x1, y1, z1).next();
        vertices.vertex(matrix, x2, y2, z2).next();
        vertices.vertex(matrix, x3, y3, z3).next();
        vertices.vertex(matrix, x4, y4, z4).next();
    }
}
