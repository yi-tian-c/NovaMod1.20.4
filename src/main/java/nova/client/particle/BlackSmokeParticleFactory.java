package nova.client.particle;

import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

/** 将粒子图集提供的动画精灵绑定到黑烟实例。 */
public final class BlackSmokeParticleFactory implements ParticleFactory<DefaultParticleType> {
    /** 资源重载后由原版粒子图集更新的精灵提供者。 */
    private final SpriteProvider sprites;

    /** 保存黑烟动画对应的精灵提供者。 */
    public BlackSmokeParticleFactory(SpriteProvider sprites) {
        this.sprites = sprites;
    }

    @Override
    public BlackSmokeParticle createParticle(DefaultParticleType type, ClientWorld world,
                                            double x, double y, double z,
                                            double velocityX, double velocityY, double velocityZ) {
        return new BlackSmokeParticle(world, x, y, z, velocityX, velocityY, velocityZ, sprites);
    }
}
