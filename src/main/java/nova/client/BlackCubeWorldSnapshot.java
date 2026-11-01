package nova.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import nova.entity.BlackCubeEntity;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

/**
 * 在世界渲染期间保存实际目标的深度和逆矩阵，供 HUD 前的地形黑化使用。
 *
 * <p>客户端渲染线程通过 {@link #capture} 捕获、通过 {@link #bind} 读取，
 * 每帧开始调用 {@link #invalidate()}，避免使用上一帧或上一世界的深度。
 * 不假设原版或 Iris 的 framebuffer 编号；没有可用深度时回退至屏幕过渡。
 */
public final class BlackCubeWorldSnapshot {
    /** 独立深度副本，禁止一边采样一边写入同一深度附件。 */
    private static int depthTexture;
    /** 深度副本的像素宽度。 */
    private static int width;
    /** 深度副本的像素高度。 */
    private static int height;
    /** 当前帧是否已捕获匹配的场景。 */
    private static boolean ready;
    /** 将裁剪坐标还原为相对摄像机世界坐标的矩阵。 */
    private static Matrix4f inverseWorld;
    /** 摄像机相对本次回忆原点的偏移，单位为格。 */
    private static Vec3d cameraOffset;
    /** 本体在屏幕上的水平焦点，范围零至一。 */
    private static float focusX = 0.5F;
    /** 本体在屏幕上的垂直焦点，范围零至一。 */
    private static float focusY = 0.5F;

    private BlackCubeWorldSnapshot() {
    }

    /** 使当前帧的捕获失效；例如在 WorldRenderEvents.START 中调用。 */
    public static void invalidate() {
        ready = false;
    }

    /**
     * 在原版清除手持物及 GUI 深度之前复制实际场景深度，并保存对应矩阵。
     *
     * <p>示例：在 WorldRenderEvents.LAST 中调用
     * {@code BlackCubeWorldSnapshot.capture(context, memoryOrigin, cube)}。
     *
     * @param context 当前世界帧的渲染上下文
     * @param origin 本次激活时固定的玩家位置，不随镜头移动
     * @param cube 镜头拉扯的焦点实体
     */
    public static void capture(WorldRenderContext context, Vec3d origin, BlackCubeEntity cube) {
        RenderSystem.assertOnRenderThread();
        ready = false;
        int source = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        if (source == 0 || GL11.glGetInteger(GL13.GL_SAMPLES) > 0
                || GL30.glGetFramebufferAttachmentParameteri(GL30.GL_DRAW_FRAMEBUFFER,
                GL30.GL_DEPTH_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE) == GL11.GL_NONE) {
            return;
        }
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        if (viewport[2] <= 0 || viewport[3] <= 0) {
            return;
        }
        int previousRead = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int previousActive = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        try {
            if (depthTexture == 0) {
                depthTexture = GL11.glGenTextures();
            }
            RenderSystem.bindTexture(depthTexture);
            if (width != viewport[2] || height != viewport[3]) {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT32F,
                        viewport[2], viewport[3], 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, 0L);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                width = viewport[2];
                height = viewport[3];
            }
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source);
            GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0,
                    viewport[0], viewport[1], width, height);
            Matrix4f world = new Matrix4f(context.projectionMatrix())
                    .mul(context.matrixStack().peek().getPositionMatrix());
            inverseWorld = new Matrix4f(world).invert();
            cameraOffset = context.camera().getPos().subtract(origin);

            // 焦点投影使用同一世界矩阵，镜头旋转时仍指向上浮的黑方块。
            Vec3d relative = cube.getPos().add(0.0, cube.getEffectScale(context.tickDelta()) * 0.4, 0.0)
                    .subtract(context.camera().getPos());
            Vector4f focus = world.transform(new Vector4f(
                    (float) relative.x, (float) relative.y, (float) relative.z, 1F));
            focusX = focus.w > 0.001F ? MathHelper.clamp(focus.x / focus.w * 0.5F + 0.5F, 0.1F, 0.9F) : 0.5F;
            focusY = focus.w > 0.001F ? MathHelper.clamp(focus.y / focus.w * 0.5F + 0.5F, 0.1F, 0.9F) : 0.5F;
            ready = true;
        } finally {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousRead);
            RenderSystem.bindTexture(previousTexture);
            RenderSystem.activeTexture(previousActive);
        }
    }

    /**
     * 将本帧深度副本绑定到一号纹理槽。
     *
     * <p>调用者负责恢复纹理槽，例如 {@code boolean mapping = BlackCubeWorldSnapshot.bind(w, h)}。
     *
     * @param targetWidth 最终场景宽度，单位为像素
     * @param targetHeight 最终场景高度，单位为像素
     * @return 仅在捕获有效且尺寸匹配时返回 true
     */
    public static boolean bind(int targetWidth, int targetHeight) {
        boolean valid = ready && targetWidth == width && targetHeight == height;
        RenderSystem.activeTexture(GL13.GL_TEXTURE1);
        RenderSystem.bindTexture(valid ? depthTexture : 0);
        return valid;
    }

    /** @return 当前帧的逆世界矩阵，只允许读取；未捕获时可能为 null */
    public static Matrix4f getInverseWorld() {
        return inverseWorld;
    }

    /** @return 当前摄像机相对回忆原点的位置；未捕获时可能为 null */
    public static Vec3d getCameraOffset() {
        return cameraOffset;
    }

    /** @return 水平拉扯焦点；捕获无效时回到屏幕中心 */
    public static float getFocusX() {
        return ready ? focusX : 0.5F;
    }

    /** @return 垂直拉扯焦点；捕获无效时回到屏幕中心 */
    public static float getFocusY() {
        return ready ? focusY : 0.5F;
    }
}
