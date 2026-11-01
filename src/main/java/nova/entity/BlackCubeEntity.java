package nova.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 悬浮黑方块，由服务端同步右键仪式的目标和进度。
 */
public class BlackCubeEntity extends MobEntity implements GeoEntity {

    /** 仪式总时长，单位为 tick，供实体、粒子和屏幕效果共用。 */
    public static final int EFFECT_DURATION = 352;
    /** 本体放大并停留后，开始喷烟和环绕玩家的 tick。 */
    public static final int ENCLOSURE_START_TICK = 40;
    /** 烟雾环绕完成后，开始收紧烟层和黑化地形的 tick。 */
    public static final int BLACKENING_START_TICK = 90;
    /** 烟雾完全包围玩家、屏幕完全变黑的 tick。 */
    public static final int FULL_BLACK_TICK = 182;
    /** 黑屏结束、烟雾和实体开始恢复的 tick。 */
    public static final int RECOVERY_START_TICK = 312;
    /** 本体达到最高位置和最大尺寸所需的 tick，供位移与缩放共用。 */
    private static final int RISE_TICKS = 20;
    /** 服务端同步的仪式进度；负数表示未激活。 */
    private static final TrackedData<Integer> EFFECT_TICKS =
            DataTracker.registerData(BlackCubeEntity.class, TrackedDataHandlerRegistry.INTEGER);
    /** 本次仪式目标的实体 ID；负数表示没有目标。 */
    private static final TrackedData<Integer> EFFECT_TARGET =
            DataTracker.registerData(BlackCubeEntity.class, TrackedDataHandlerRegistry.INTEGER);
    /** GeckoLib 持有的本实体动画缓存。 */
    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);
    /** 激活前的高度，用于仪式结束或目标离开时恢复位置。 */
    private double restingY;
    /** 客户端上一模拟 tick 的显示进度，负数表示尚未激活。 */
    private float previousClientEffectTime = -1F;
    /** 客户端当前模拟 tick 的连续显示进度，不直接跟随网络包跳变。 */
    private float clientEffectTime = -1F;
    /** 上次用于显示校准的服务端进度，避免对重复的旧数据反复校准。 */
    private int lastSyncedEffectTicks = -1;

    /** 创建无重力的黑方块。 */
    public BlackCubeEntity(
            EntityType<? extends MobEntity> entityType,
            World world
    ) {
        super(entityType, world);
        // 不使用重力
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(EFFECT_TICKS, -1);
        dataTracker.startTracking(EFFECT_TARGET, -1);
    }

    /** 主手右键在服务端启动仪式；激活期间重复交互不会切换目标或重置计时。 */
    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND || player.isSpectator()) {
            return ActionResult.PASS;
        }
        if (!getWorld().isClient && getEffectTicks() < 0) {
            restingY = getY();
            dataTracker.set(EFFECT_TARGET, player.getId());
            dataTracker.set(EFFECT_TICKS, 0);
        }
        return ActionResult.success(getWorld().isClient);
    }

    /** @return 当前仪式进度，负数表示空闲 */
    public int getEffectTicks() {
        return dataTracker.get(EFFECT_TICKS);
    }

    /** @return 当前目标玩家；目标卸载、离开或仪式空闲时返回 null */
    public PlayerEntity getEffectTarget() {
        Entity entity = getWorld().getEntityById(dataTracker.get(EFFECT_TARGET));
        return entity instanceof PlayerEntity player && player.isAlive() ? player : null;
    }

    /**
     * 以客户端前后模拟 tick 的显示进度插值缩放，网络校准只改变推进速度。
     * 仪式结束时恢复原始大小；不改变实体碰撞盒。
     *
     * @param tickDelta 帧内插值，范围零至一
     * @return 本体相对于原模型的缩放倍数
     */
    public float getEffectScale(float tickDelta) {
        int ticks = getEffectTicks();
        if (ticks < 0) {
            return 1F;
        }
        float time = getWorld().isClient && clientEffectTime >= 0F
                ? MathHelper.lerp(MathHelper.clamp(tickDelta, 0F, 1F), previousClientEffectTime, clientEffectTime)
                : ticks;
        float rise = MathHelper.clamp(time / RISE_TICKS, 0F, 1F);
        float recovery = MathHelper.clamp((EFFECT_DURATION - time)
                / (EFFECT_DURATION - RECOVERY_START_TICK), 0F, 1F);
        return 1F + 1.4F * rise * rise * (3F - 2F * rise) * recovery;
    }

    /** 每个客户端 tick 推进一次显示时钟，批量到达的网络状态不能直接跳改当前帧。 */
    private void tickClientEffectAnimation() {
        int syncedTicks = getEffectTicks();
        if (syncedTicks < 0) {
            previousClientEffectTime = -1F;
            clientEffectTime = -1F;
            lastSyncedEffectTicks = -1;
            return;
        }
        if (clientEffectTime < 0F) {
            previousClientEffectTime = syncedTicks;
            clientEffectTime = syncedTicks;
            lastSyncedEffectTicks = syncedTicks;
            return;
        }
        previousClientEffectTime = clientEffectTime;
        float advance = 1F;
        if (syncedTicks != lastSyncedEffectTicks) {
            // 只通过有限的速度修正追随新包，保持显示时间单调且 tick 边界两侧连续。
            advance += MathHelper.clamp((syncedTicks - clientEffectTime - 1F) * 0.15F, -0.25F, 0.25F);
            lastSyncedEffectTicks = syncedTicks;
        }
        clientEffectTime = Math.min(clientEffectTime + advance, EFFECT_DURATION);
    }

    /** 保存激活前的高度，使区块卸载或退出游戏不会永久抬高实体。 */
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (getEffectTicks() >= 0) {
            nbt.putDouble("BlackCubeRestingY", restingY);
        }
    }

    /** 加载时结束未完成的仪式并恢复原高度，不持久化瞬时的玩家实体 ID。 */
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("BlackCubeRestingY")) {
            setPosition(getX(), nbt.getDouble("BlackCubeRestingY"), getZ());
        }
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    /** 服务端驱动上浮及恢复；客户端独立推进缩放显示时钟，不修改同步状态或实体位置。 */
    @Override
    public void tick() {
        super.tick();

        // 持续关闭重力，确保实体能够悬浮
        this.setNoGravity(true);
        this.setVelocity(Vec3d.ZERO);
        if (getWorld().isClient) {
            tickClientEffectAnimation();
        }
        if (!getWorld().isClient && getEffectTicks() >= 0) {
            int ticks = getEffectTicks() + 1;
            if (ticks >= EFFECT_DURATION || getEffectTarget() == null) {
                setPosition(getX(), restingY, getZ());
                dataTracker.set(EFFECT_TICKS, -1);
                dataTracker.set(EFFECT_TARGET, -1);
                return;
            }
            dataTracker.set(EFFECT_TICKS, ticks);
            double rise = MathHelper.clamp(ticks / (double) RISE_TICKS, 0.0, 1.0);
            double recovery = MathHelper.clamp((EFFECT_DURATION - ticks)
                    / (double) (EFFECT_DURATION - RECOVERY_START_TICK), 0.0, 1.0);
            setPosition(getX(), restingY + 1.8 * rise * rise * (3.0 - 2.0 * rise) * recovery, getZ());
        }
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
    @Override
    public void takeKnockback(double strength, double x, double z) {
        // 忽略所有击退
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        // 不被其他实体推挤
    }
}
