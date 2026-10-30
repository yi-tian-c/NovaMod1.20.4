package nova.client.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import nova.client.BlackCubeModel;
import nova.entity.BlackCubeEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BlackCubeEntityRenderer
        extends GeoEntityRenderer<BlackCubeEntity> {

    public BlackCubeEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new BlackCubeModel());
        this.shadowRadius = 0.0F;
    }
}