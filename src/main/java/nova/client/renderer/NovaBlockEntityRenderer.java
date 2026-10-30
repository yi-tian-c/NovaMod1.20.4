package nova.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import nova.NovaMod;
import nova.block.entity.NovaBlockEntity;
import org.joml.Matrix4f;

/**
 * 将当前视角的 Code Sky（与 Craftmine 光影包一致）投影到 NovaBlock 的六个表面。
 */
public final class NovaBlockEntityRenderer implements BlockEntityRenderer<NovaBlockEntity> {

    private static final Identifier CODE_TEXTURE = new Identifier(NovaMod.MOD_ID, "textures/effect/code.png");

    public NovaBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(
            NovaBlockEntity blockEntity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        ShaderProgram shader = NovaCodeShader.getProgram();
        if (shader == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null) {
            return;
        }

        // Bind code font texture
        RenderSystem.setShaderTexture(0, CODE_TEXTURE);

        // Compute inverse matrices from current view
        Matrix4f proj = new Matrix4f(RenderSystem.getProjectionMatrix());
        Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix());

        Matrix4f invProj = new Matrix4f(proj).invert();
        Matrix4f invModelView = new Matrix4f(modelView).invert();

        // Set custom uniforms
        if (shader.getUniform("InvProjMat") != null) {
            shader.getUniform("InvProjMat").set(invProj);
        }
        if (shader.getUniform("InvModelViewMat") != null) {
            shader.getUniform("InvModelViewMat").set(invModelView);
        }
        if (shader.getUniform("ScreenSize") != null) {
            shader.getUniform("ScreenSize").set(
                    (float) client.getWindow().getFramebufferWidth(),
                    (float) client.getWindow().getFramebufferHeight()
            );
        }
        if (shader.getUniform("Time") != null) {
            // Match original: (worldTime + frameTimeCounter * 20) / 24000
            long worldTime = client.world != null ? client.world.getTimeOfDay() : 0L;
            float frameTime = client.getTickDelta(); // approximate
            // Better approximation using system nano if needed, but world time + partial is ok for visual
            float time = (worldTime % 24000L + tickDelta) / 24000.0f;
            // To better match frameTimeCounter style animation, add a continuous component
            // Original uses frameTimeCounter * 20 / 24000 for smooth
            // We use a simple continuous time based on system for smoothness
            float continuous = (System.nanoTime() / 1_000_000_000.0f) * (20.0f / 24000.0f);
            shader.getUniform("Time").set(time + continuous);
        }

        VertexConsumer vertices = vertexConsumers.getBuffer(NovaCodeSurfaceLayer.INSTANCE);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Six faces of the unit cube (same as original Skybox)
        addFace(vertices, matrix, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1); // -X
        addFace(vertices, matrix, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0); // +X
        addFace(vertices, matrix, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0); // -Y
        addFace(vertices, matrix, 0, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1); // +Y
        addFace(vertices, matrix, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0); // -Z
        addFace(vertices, matrix, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1); // +Z
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
