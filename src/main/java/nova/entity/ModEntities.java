package nova.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import nova.NovaMod;

public class ModEntities {
    public static final EntityType<BlackCubeEntity> BLACK_CUBE =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    new Identifier(NovaMod.MOD_ID, "black_cube"),
                    FabricEntityTypeBuilder
                            .create(SpawnGroup.MISC, BlackCubeEntity::new)
                            .dimensions(EntityDimensions.fixed(0.8F, 0.8F))
                            .build()
            );
    public static void registerEntities() {
    }
    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(BLACK_CUBE, BlackCubeEntity.createAttributes());
    }
}
