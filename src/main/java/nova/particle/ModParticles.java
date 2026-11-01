package nova.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import nova.NovaMod;

/** 注册黑方块仪式使用的烟雾粒子类型。 */
public final class ModParticles {
    /** 黑烟的共享注册实例，客户端将其绑定到动画纹理工厂。 */
    public static final DefaultParticleType BLACK_SMOKE = Registry.register(
            Registries.PARTICLE_TYPE, new Identifier(NovaMod.MOD_ID, "black_smoke"),
            FabricParticleTypes.simple(true));

    private ModParticles() {
    }

    /** 在模组初始化时加载粒子注册。 */
    public static void initialize() {
    }
}
