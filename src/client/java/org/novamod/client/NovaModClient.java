package org.novamod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import org.novamod.block.NovaModBlocks;

/**
 * 初始化仅客户端使用的天空盒方块渲染绑定。
 */
public final class NovaModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkyboxShader.register();
        BlockEntityRendererRegistry.register(NovaModBlocks.SKYBOX_ENTITY, SkyboxBlockEntityRenderer::new);
    }
}
