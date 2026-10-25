package org.novamod.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * 在天空绘制结束时保存当前帧的天空颜色纹理。
 */
public final class SkyboxSnapshot {
    private static SimpleFramebuffer framebuffer;
    private static boolean available;

    private SkyboxSnapshot() {
    }

    public static void captureSky() {
        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer source = client.getFramebuffer();
        int width = source.textureWidth;
        int height = source.textureHeight;
        if (width <= 0 || height <= 0) {
            available = false;
            return;
        }

        if (framebuffer == null) {
            framebuffer = new SimpleFramebuffer(width, height, false, false);
        } else if (framebuffer.textureWidth != width || framebuffer.textureHeight != height) {
            framebuffer.resize(width, height, false);
        }

        source.beginWrite(false);
        int previousTexture = RenderSystem.getShaderTexture(0);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(framebuffer.getColorAttachment());
        GlStateManager._glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
        RenderSystem.bindTexture(previousTexture);
        available = true;
    }

    public static Framebuffer getFramebuffer() {
        return framebuffer;
    }

    public static boolean isAvailable() {
        return available;
    }
}
