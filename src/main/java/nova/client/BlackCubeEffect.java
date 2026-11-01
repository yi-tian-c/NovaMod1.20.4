package nova.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import nova.client.particle.BlackSmokeParticle;
import nova.entity.BlackCubeEntity;
import nova.particle.ModParticles;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 根据黑方块的服务端同步状态生成烟团，并管理本地目标玩家的眩晕和视野收缩。
 *
 * <p>由客户端入口调用 {@link #register()}，由 HUD 前的渲染注入调用 {@link #render(float)}。
 */
public final class BlackCubeEffect {
    /** 当前触发效果的目标实体。 */
    private static BlackCubeEntity target;

    /** 点击时从实体指向玩家的单位方向。 */
    private static Vec3d ghostDirection;

    /** 本次回忆黑化的世界坐标原点，激活后不随玩家或镜头移动。 */
    private static Vec3d memoryOrigin;

    private BlackCubeEffect() {
    }

    /**
     * 注册客户端烟雾更新；交互和仪式计时由服务端实体负责。
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(BlackCubeEffect::tick);
        WorldRenderEvents.START.register(context -> BlackCubeWorldSnapshot.invalidate());
        WorldRenderEvents.LAST.register(context -> {
            if (target != null && memoryOrigin != null && target.isAlive()
                    && target.getWorld() == context.world() && target.getEffectTicks() >= 0) {
                BlackCubeWorldSnapshot.capture(context, memoryOrigin, target);
            }
        });
    }

    /** 切换屏幕效果来源，并保存实体指向点击玩家的重影方向。 */
    private static void start(BlackCubeEntity entity, PlayerEntity player) {
        target = entity;
        memoryOrigin = player.getPos();
        ghostDirection = player.getPos().subtract(entity.getPos()).normalize();
        if (ghostDirection.lengthSquared() < 0.0001) {
            ghostDirection = new Vec3d(1.0, 0.0, 0.0);
        }
    }

    /** 为所有已加载的激活实体生成烟雾，只将点击本地玩家的实体选为屏幕效果来源。 */
    private static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null || !client.player.isAlive()) {
            clear();
            return;
        }
        if (client.isPaused()) {
            return;
        }
        BlackCubeEntity selected = null;
        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof BlackCubeEntity cube) || !cube.isAlive() || cube.getEffectTicks() < 0) {
                continue;
            }
            PlayerEntity player = cube.getEffectTarget();
            if (player == null) {
                continue;
            }
            emitSmoke(client, cube, player);
            if (player == client.player && (selected == null || cube.getEffectTicks() < selected.getEffectTicks())) {
                selected = cube;
            }
        }
        if (selected == null) {
            clear();
        } else if (selected != target) {
            start(selected, client.player);
        }
    }

    /** 每隔两 tick 喷出四簇气团，并以连续烟层勾出收紧的旋涡。 */
    private static void emitSmoke(MinecraftClient client, BlackCubeEntity cube, PlayerEntity player) {
        int ticks = cube.getEffectTicks();
        if (ticks < BlackCubeEntity.ENCLOSURE_START_TICK || ticks >= BlackCubeEntity.RECOVERY_START_TICK) {
            return;
        }
        if (ticks % 2 == 0 && ticks < BlackCubeEntity.FULL_BLACK_TICK) {
            Vec3d origin = cube.getPos().add(0.0, cube.getEffectScale(0F) * 0.4, 0.0);
            for (int cluster = 0; cluster < 4; cluster++) {
                double phase = ticks * 0.31 + cluster * Math.PI * 2.0 / 4.0;
                for (int puff = 0; puff < 16; puff++) {
                    spawnSmoke(client, cube, player, origin, phase, false);
                }
            }
        }
        if (ticks >= BlackCubeEntity.ENCLOSURE_START_TICK) {
            for (int puff = 0; puff < 32; puff++) {
                double phase = ticks * 0.23 + puff * Math.PI * 2.0 / 32.0;
                spawnSmoke(client, cube, player, player.getPos(), phase, true);
            }
        }
    }

    /** 直接加入粒子管理器，使仪式烟层不受原版随机删减粒子的影响。 */
    private static void spawnSmoke(MinecraftClient client, BlackCubeEntity cube, PlayerEntity player,
                                   Vec3d origin, double phase, boolean enclosing) {
        Particle particle = client.particleManager.addParticle(ModParticles.BLACK_SMOKE,
                origin.x, origin.y, origin.z, 0.0, 0.0, 0.0);
        if (particle instanceof BlackSmokeParticle smoke) {
            smoke.bind(cube, player, phase, enclosing);
        }
    }

    /**
     * 在世界、手持物及光影完成后、HUD 之前绘制眩晕与全黑遮罩。
     *
     * <p>仅在渲染线程调用，例如 {@code BlackCubeEffect.render(tickDelta)}。
     *
     * @param tickDelta 帧内插值，范围零至一；暂停时不推进动画
     */
    public static void render(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (target == null || ghostDirection == null || client.world == null || client.player == null
                || !client.player.isAlive() || !target.isAlive() || target.getWorld() != client.world
                || target.getEffectTicks() < 0 || target.getEffectTarget() != client.player) {
            return;
        }
        float time = target.getEffectTicks() + (client.isPaused() ? 0F : tickDelta);
        Quaternionf inverseCameraRotation = client.gameRenderer.getCamera().getRotation().conjugate(new Quaternionf());
        Vector3f viewDirection = new Vector3f(
                (float) ghostDirection.x,
                (float) ghostDirection.y,
                (float) ghostDirection.z
        ).rotate(inverseCameraRotation);
        float screenX = viewDirection.x;
        float screenY = -viewDirection.y;
        float lengthSquared = screenX * screenX + screenY * screenY;
        if (lengthSquared < 0.0001F) {
            screenX = 1.0F;
            screenY = 0.0F;
        } else {
            float inverseLength = (float) (1.0 / Math.sqrt(lengthSquared));
            screenX *= inverseLength;
            screenY *= inverseLength;
        }

        float recovery = MathHelper.clamp((BlackCubeEntity.EFFECT_DURATION - time)
                / (BlackCubeEntity.EFFECT_DURATION - BlackCubeEntity.RECOVERY_START_TICK), 0F, 1F);
        float ghostProgress = Math.min(time / 5F, 4F) * recovery;
        float obscuration = MathHelper.clamp((time - BlackCubeEntity.BLACKENING_START_TICK)
                / (BlackCubeEntity.FULL_BLACK_TICK - BlackCubeEntity.BLACKENING_START_TICK), 0F, 1F);
        obscuration *= recovery;
        BlackCubePostProcessor.render(screenX, screenY, ghostProgress, obscuration, time / 20F);
    }

    private static void clear() {
        target = null;
        ghostDirection = null;
        memoryOrigin = null;
        BlackCubeWorldSnapshot.invalidate();
    }
}
