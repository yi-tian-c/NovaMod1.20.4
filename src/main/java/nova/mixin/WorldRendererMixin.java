package nova.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import nova.client.renderer.SkyboxSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.joml.Matrix4f;

/**
 * 在原版天空绘制结束时捕获当前渲染目标。
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At("TAIL")
    )
    private void novamod$captureSky(
            MatrixStack matrices,
            Matrix4f projectionMatrix,
            float tickDelta,
            Camera camera,
            boolean thickFog,
            Runnable fogCallback,
            CallbackInfo callbackInfo
    ) {
        SkyboxSnapshot.captureSky();
    }
}
