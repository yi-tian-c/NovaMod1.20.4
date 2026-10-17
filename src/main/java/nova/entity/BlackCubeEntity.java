package nova.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BlackCubeEntity extends MobEntity implements GeoEntity {

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    public BlackCubeEntity(
            EntityType<? extends MobEntity> entityType,
            World world
    ) {
        super(entityType, world);
        // 不使用重力
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    public void tick() {
        super.tick();

        // 持续关闭重力，确保实体能够悬浮
        this.setNoGravity(true);

    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    private PlayState predicate(AnimationState<BlackCubeEntity> state) {
        AnimationController<BlackCubeEntity> controller = state.getController();
        controller.setAnimation(
                RawAnimation.begin().then("idle", Animation.LoopType.LOOP)
        );
        controller.setAnimationSpeed(1.0F);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}