package nova.client;

import net.minecraft.util.Identifier;
import nova.NovaMod;
import nova.entity.BlackCubeEntity;
import software.bernie.geckolib.model.GeoModel;

public class BlackCubeModel extends GeoModel<BlackCubeEntity> {
    @Override
    public Identifier getModelResource(BlackCubeEntity animatable) {
        return new Identifier(NovaMod.MOD_ID, "geo/black_cube.geo.json");
    }

    @Override
    public Identifier getTextureResource(BlackCubeEntity animatable) {
        return new Identifier(NovaMod.MOD_ID, "textures/entity/black_cube.png");
    }

    @Override
    public Identifier getAnimationResource(BlackCubeEntity animatable) {
        return new Identifier(NovaMod.MOD_ID, "animations/black_cube.animation.json");
    }
}
