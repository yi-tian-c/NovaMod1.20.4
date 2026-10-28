package nova;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import nova.block.ModBlocks;
import nova.client.renderer.BlackCubeEntityRenderer;
import nova.client.renderer.NovaCodeShader;
import nova.client.renderer.SkyboxBlockEntityRenderer;
import nova.client.renderer.SkyboxShader;
import nova.entity.ModEntities;

public class NovaModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.BLACK_CUBE, BlackCubeEntityRenderer::new);
        SkyboxShader.register();
        NovaCodeShader.register();
        // nova_block 不再注册自定义 BER，让它走 MODEL + Iris gbuffers_terrain
        // BlockEntityRendererRegistry.register(NovaModBlocks.NOVA_BLOCK_ENTITY, NovaBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(ModBlocks.SKYBOX_ENTITY, SkyboxBlockEntityRenderer::new);
    }
}
