package nova.mixin;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import nova.client.particle.BlackSmokeTextureSheet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/** 将黑烟批次加入原版粒子管理器的绘制顺序，保持原有批次的先后关系。 */
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    /** 原版按顺序绘制的批次列表，仅在静态初始化结束时追加黑烟批次。 */
    @Shadow
    @Final
    @Mutable
    private static List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS;

    /** 原版没有公开的批次注册入口，因此在列表完成初始化后追加唯一批次。 */
    @Inject(method = "<clinit>()V", at = @At("TAIL"), require = 1, allow = 1)
    private static void registerBlackSmokeSheet(CallbackInfo callbackInfo) {
        List<ParticleTextureSheet> sheets = new ArrayList<>(PARTICLE_TEXTURE_SHEETS);
        sheets.add(BlackSmokeTextureSheet.INSTANCE);
        PARTICLE_TEXTURE_SHEETS = List.copyOf(sheets);
    }
}
