package nova.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.TextureManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 * 黑烟专用的原版粒子批次，透明叠加时关闭深度测试与写入。
 *
 * <p>继续使用粒子管理器提供的程序、图集和目标，不持有或替换光影 framebuffer。
 * 绘制结束恢复本批次修改的状态，其他粒子保持原有深度行为。
 */
public final class BlackSmokeTextureSheet implements ParticleTextureSheet {
    /** 粒子分组和渲染列表必须共用的批次身份。 */
    public static final BlackSmokeTextureSheet INSTANCE = new BlackSmokeTextureSheet();
    /** 进入批次前是否启用深度测试。 */
    private boolean depthTest;
    /** 进入批次前是否写入深度。 */
    private boolean depthWrite;
    /** 进入批次前是否启用混合。 */
    private boolean blend;
    /** 进入批次前的颜色源混合因子。 */
    private int sourceRgb;
    /** 进入批次前的颜色目标混合因子。 */
    private int destinationRgb;
    /** 进入批次前的透明度源混合因子。 */
    private int sourceAlpha;
    /** 进入批次前的透明度目标混合因子。 */
    private int destinationAlpha;
    /** 进入批次前原版程序的零号纹理槽。 */
    private int shaderTexture;

    /** 仅创建一个与粒子队列对应的渲染批次。 */
    private BlackSmokeTextureSheet() {
    }

    /** 沿用原版粒子图集及缓冲格式，独立关闭黑烟批次的深度遮挡。 */
    @Override
    public void begin(BufferBuilder builder, TextureManager textureManager) {
        depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        blend = GL11.glIsEnabled(GL11.GL_BLEND);
        sourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        destinationRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        sourceAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        destinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        shaderTexture = RenderSystem.getShaderTexture(0);
        ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT.begin(builder, textureManager);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
    }

    /** 提交整批烟雾后恢复深度、混合和程序纹理槽，避免影响地形及其他粒子。 */
    @Override
    public void draw(Tessellator tessellator) {
        try {
            ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT.draw(tessellator);
        } finally {
            RenderSystem.depthMask(depthWrite);
            if (depthTest) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
            RenderSystem.blendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
            if (blend) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }
            RenderSystem.setShaderTexture(0, shaderTexture);
        }
    }
}
