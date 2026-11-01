package nova.mixin;

import net.minecraft.client.render.GameRenderer;
import nova.client.BlackCubeEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在最终场景及手持物完成后、HUD 开始前绘制黑方块屏幕效果；隐藏 HUD 时仍会执行。
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    /** 使用原版 GUI 光照准备点，避免捕获局部变量或替换世界渲染。 */
    @Inject(
            method = "render(FJZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/DiffuseLighting;enableGuiDepthLighting()V",
                    shift = At.Shift.AFTER
            ),
            require = 1,
            allow = 1
    )
    private void renderBlackCubeEffect(float tickDelta, long startTime, boolean tick, CallbackInfo callbackInfo) {
        if (tick) {
            BlackCubeEffect.render(tickDelta);
        }
    }
}
