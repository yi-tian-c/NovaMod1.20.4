package nova;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import nova.client.renderer.BlackCubeEntityRenderer;
import nova.entity.ModEntities;

public class NovaModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.BLACK_CUBE, BlackCubeEntityRenderer::new);
    }
}
