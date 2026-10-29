package org.novamod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import org.novamod.block.NovaModBlocks;

/**
 * 生成 NovaMod 方块的掉落表。
 */
public final class NovaModBlockLootProvider extends FabricBlockLootTableProvider {
    public NovaModBlockLootProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate() {
        addDrop(NovaModBlocks.SKYBOX);
    }
}
