package nova.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import nova.NovaMod;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 对 HUD 之前的场景执行重影、镜头拉扯和世界表面黑化，最终过渡到纯黑。
 *
 * <p>由 {@link BlackCubeEffect} 在渲染线程调用，保存并恢复调用前的 OpenGL 状态。
 */
public final class BlackCubePostProcessor {
    /** 全屏后处理 shader 程序。 */
    private static int program;

    /** 全屏四边形顶点数组。 */
    private static int vertexArray;

    /** 全屏四边形顶点缓冲区。 */
    private static int vertexBuffer;

    /** 保存原始世界颜色画面的纹理。 */
    private static int sceneTexture;

    /** 保存原始画面的中间 framebuffer。 */
    private static int sceneFramebuffer;

    /** 中间颜色纹理当前宽度。 */
    private static int sceneWidth;

    /** 中间颜色纹理当前高度。 */
    private static int sceneHeight;

    /** 屏幕尺寸 uniform 位置。 */
    private static int screenSizeUniform;

    /** 重影偏移方向 uniform 位置。 */
    private static int directionUniform;

    /** 重影分层平移进度 uniform 位置。 */
    private static int ghostProgressUniform;

    /** 黑烟遮罩闭合进度 uniform 位置。 */
    private static int obscurationUniform;

    /** 烟雾边界动画时间 uniform 位置，单位为秒。 */
    private static int effectTimeUniform;

    /** 是否具备当前帧场景深度的 uniform 位置。 */
    private static int terrainMappingUniform;
    /** 场景逆矩阵 uniform 位置。 */
    private static int inverseWorldUniform;
    /** 摄像机相对回忆原点的偏移 uniform 位置。 */
    private static int cameraOffsetUniform;
    /** 回忆拉扯焦点 uniform 位置。 */
    private static int focusUniform;

    private BlackCubePostProcessor() {
    }

    /**
     * 在当前颜色目标上执行回忆过渡；有深度时沿世界表面黑化，否则回退为屏幕过渡。
     *
     * <p>示例：{@code BlackCubePostProcessor.render(1F, 0F, 4F, 0.5F, 5F)}。
     *
     * @param directionX 屏幕空间水平方向
     * @param directionY 屏幕空间垂直方向
     * @param ghostProgress 重影分层平移进度，零至四分别对应四层重影的连续出现
     * @param obscuration 视野闭合进度，零表示无遮挡，一表示完全黑屏
     * @param effectTime 烟雾动画运行时间，单位为秒
     */
    public static void render(float directionX, float directionY, float ghostProgress,
                              float obscuration, float effectTime) {
        RenderSystem.assertOnRenderThread();

        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        if (viewport[2] <= 0 || viewport[3] <= 0) {
            return;
        }

        int sourceDrawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int sourceReadFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int sourceDrawBuffer = GL11.glGetInteger(GL20.GL_DRAW_BUFFER0);
        int sourceReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        int previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int previousVertexArray = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.activeTexture(GL13.GL_TEXTURE1);
        int previousDepthTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        int previousReadFramebufferBinding = sourceReadFramebuffer;
        int previousDrawFramebufferBinding = sourceDrawFramebuffer;
        boolean blendWasEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depthWasEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cullWasEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean scissorWasEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        boolean framebufferSrgbWasEnabled = GL11.glIsEnabled(GL30.GL_FRAMEBUFFER_SRGB);
        ByteBuffer previousColorMask = BufferUtils.createByteBuffer(4);
        GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, previousColorMask);

        try {
            ensureResources(viewport[2], viewport[3]);

            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, sourceDrawFramebuffer);
            GL11.glReadBuffer(sourceDrawBuffer);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, sceneFramebuffer);
            GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
            GL30.glBlitFramebuffer(
                    viewport[0],
                    viewport[1],
                    viewport[0] + viewport[2],
                    viewport[1] + viewport[3],
                    0,
                    0,
                    viewport[2],
                    viewport[3],
                    GL11.GL_COLOR_BUFFER_BIT,
                    GL11.GL_NEAREST
            );

            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, sourceDrawFramebuffer);
            GL11.glDrawBuffer(sourceDrawBuffer);
            GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.activeTexture(GL13.GL_TEXTURE0);
            RenderSystem.bindTexture(sceneTexture);
            GL20.glUseProgram(program);
            GL20.glUniform2f(screenSizeUniform, viewport[2], viewport[3]);
            GL20.glUniform2f(directionUniform, directionX, directionY);
            GL20.glUniform1f(ghostProgressUniform, ghostProgress);
            GL20.glUniform1f(obscurationUniform, obscuration);
            GL20.glUniform1f(effectTimeUniform, effectTime);
            boolean terrainMapping = BlackCubeWorldSnapshot.bind(viewport[2], viewport[3]);
            GL20.glUniform1i(terrainMappingUniform, terrainMapping ? 1 : 0);
            if (terrainMapping) {
                GL20.glUniformMatrix4fv(inverseWorldUniform, false,
                        BlackCubeWorldSnapshot.getInverseWorld().get(new float[16]));
                Vec3d offset = BlackCubeWorldSnapshot.getCameraOffset();
                GL20.glUniform3f(cameraOffsetUniform, (float) offset.x, (float) offset.y, (float) offset.z);
            }
            GL20.glUniform2f(focusUniform, BlackCubeWorldSnapshot.getFocusX(), BlackCubeWorldSnapshot.getFocusY());
            GL30.glBindVertexArray(vertexArray);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        } finally {
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDrawFramebufferBinding);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousReadFramebufferBinding);
            GL11.glDrawBuffer(sourceDrawBuffer);
            GL11.glReadBuffer(sourceReadBuffer);
            GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            GL20.glUseProgram(previousProgram);
            GL30.glBindVertexArray(previousVertexArray);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
            RenderSystem.activeTexture(GL13.GL_TEXTURE0);
            RenderSystem.bindTexture(previousTexture);
            RenderSystem.activeTexture(GL13.GL_TEXTURE1);
            RenderSystem.bindTexture(previousDepthTexture);
            RenderSystem.activeTexture(previousActiveTexture);
            RenderSystem.colorMask(
                    previousColorMask.get(0) != 0,
                    previousColorMask.get(1) != 0,
                    previousColorMask.get(2) != 0,
                    previousColorMask.get(3) != 0
            );
            setBlendEnabled(blendWasEnabled);
            setDepthEnabled(depthWasEnabled);
            setCullEnabled(cullWasEnabled);
            setEnabled(GL11.GL_SCISSOR_TEST, scissorWasEnabled);
            setEnabled(GL30.GL_FRAMEBUFFER_SRGB, framebufferSrgbWasEnabled);
        }
    }

    private static void ensureResources(int width, int height) {
        if (program == 0) {
            createProgram();
            createQuad();
        }
        if (sceneTexture == 0) {
            sceneTexture = GL11.glGenTextures();
        }
        if (sceneFramebuffer == 0) {
            sceneFramebuffer = GL30.glGenFramebuffers();
        }
        if (sceneWidth == width && sceneHeight == height) {
            return;
        }

        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(sceneTexture);
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL30.GL_RGBA16F,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL30.GL_HALF_FLOAT,
                0L
        );
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, sceneFramebuffer);
        GL30.glFramebufferTexture2D(
                GL30.GL_DRAW_FRAMEBUFFER,
                GL30.GL_COLOR_ATTACHMENT0,
                GL11.GL_TEXTURE_2D,
                sceneTexture,
                0
        );
        if (GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("全屏重影颜色缓存创建失败");
        }
        sceneWidth = width;
        sceneHeight = height;
    }

    /** 首次绘制时编译当前资源包提供的程序并绑定所有效果参数。 */
    private static void createProgram() {
        int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, readShader("screen_ghost.vsh"));
        int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, readShader("screen_ghost.fsh"));
        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("全屏重影 shader 链接失败：" + GL20.glGetProgramInfoLog(program));
        }
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
        screenSizeUniform = uniform("ScreenSize");
        directionUniform = uniform("Direction");
        ghostProgressUniform = uniform("GhostProgress");
        obscurationUniform = uniform("Obscuration");
        effectTimeUniform = uniform("EffectTime");
        terrainMappingUniform = uniform("TerrainMapping");
        inverseWorldUniform = uniform("InverseWorld");
        cameraOffsetUniform = uniform("CameraOffset");
        focusUniform = uniform("Focus");
        GL20.glUseProgram(program);
        GL20.glUniform1i(uniform("Scene"), 0);
        GL20.glUniform1i(uniform("SceneDepth"), 1);
    }

    private static void createQuad() {
        float[] vertices = {
                -1.0F, -1.0F, 0.0F, 0.0F,
                1.0F, -1.0F, 1.0F, 0.0F,
                1.0F, 1.0F, 1.0F, 1.0F,
                -1.0F, -1.0F, 0.0F, 0.0F,
                1.0F, 1.0F, 1.0F, 1.0F,
                -1.0F, 1.0F, 0.0F, 1.0F
        };
        vertexArray = GL30.glGenVertexArrays();
        vertexBuffer = GL15.glGenBuffers();
        GL30.glBindVertexArray(vertexArray);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        int position = GL20.glGetAttribLocation(program, "Position");
        int texture = GL20.glGetAttribLocation(program, "Texture");
        GL20.glEnableVertexAttribArray(position);
        GL20.glVertexAttribPointer(position, 2, GL11.GL_FLOAT, false, 16, 0L);
        GL20.glEnableVertexAttribArray(texture);
        GL20.glVertexAttribPointer(texture, 2, GL11.GL_FLOAT, false, 16, 8L);
        GL30.glBindVertexArray(0);
    }

    private static int compileShader(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("全屏重影 shader 编译失败：" + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private static String readShader(String path) {
        Identifier identifier = new Identifier(NovaMod.MOD_ID, "shaders/post/" + path);
        Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(identifier)
                .orElseThrow(() -> new IllegalStateException("缺少全屏重影 shader 资源：" + identifier));
        try (InputStream input = resource.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("读取全屏重影 shader 失败：" + identifier, exception);
        }
    }

    private static int uniform(String name) {
        int location = GL20.glGetUniformLocation(program, name);
        if (location < 0) {
            throw new IllegalStateException("全屏重影 shader 缺少 uniform：" + name);
        }
        return location;
    }

    private static void setEnabled(int capability, boolean enabled) {
        if (enabled) {
            GL11.glEnable(capability);
        } else {
            GL11.glDisable(capability);
        }
    }

    private static void setBlendEnabled(boolean enabled) {
        if (enabled) {
            RenderSystem.enableBlend();
        } else {
            RenderSystem.disableBlend();
        }
    }

    private static void setDepthEnabled(boolean enabled) {
        if (enabled) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
    }

    private static void setCullEnabled(boolean enabled) {
        if (enabled) {
            RenderSystem.enableCull();
        } else {
            RenderSystem.disableCull();
        }
    }
}
