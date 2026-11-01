package nova.client.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import nova.client.BlackCubeModel;
import nova.entity.BlackCubeEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 绘制单个 BlackCubeEntity 本体。
 */
public class BlackCubeEntityRenderer extends GeoEntityRenderer<BlackCubeEntity> {
    /**
     * 创建实体渲染器并绑定 GeckoLib 模型。
     *
     * @param context 客户端实体渲染上下文
     */
    public BlackCubeEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new BlackCubeModel());
        this.shadowRadius = 0.0F;
    }

    /** 按同步进度缩放本体，矩阵栈由本次绘制独立管理。 */
    @Override
    public void render(BlackCubeEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        try {
            float scale = entity.getEffectScale(tickDelta);
            matrices.scale(scale, scale, scale);
            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        } finally {
            matrices.pop();
        }
    }
}
