package org.novamod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import org.novamod.block.NovaModBlocks;

/**
 * 生成天空盒方块状态、方块模型和物品模型。
 */
public final class NovaModBlockModelProvider extends FabricModelProvider {
    public NovaModBlockModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        generator.registerSimpleCubeAll(NovaModBlocks.SKYBOX);
    }

    @Override
    public void generateItemModels(ItemModelGenerator generator) {
    }
}
