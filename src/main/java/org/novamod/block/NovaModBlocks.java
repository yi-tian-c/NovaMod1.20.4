package org.novamod.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.novamod.NovaMod;
import org.novamod.block.entity.SkyboxBlockEntity;
import org.novamod.block.entity.SkyboxBlockEntityBlock;

/**
 * 持有并注册 NovaMod 的方块及其物品形式。
 */
public final class NovaModBlocks {
    public static final String SKYBOX_BLOCK_ID = "skybox";
    public static final SkyboxBlockEntityBlock SKYBOX = Registry.register(
            Registries.BLOCK,
            new Identifier(NovaMod.MOD_ID, SKYBOX_BLOCK_ID),
            new SkyboxBlockEntityBlock(AbstractBlock.Settings.copy(Blocks.GLASS))
    );
    public static final BlockEntityType<SkyboxBlockEntity> SKYBOX_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(NovaMod.MOD_ID, SKYBOX_BLOCK_ID),
            BlockEntityType.Builder.create(SkyboxBlockEntity::new, SKYBOX).build(null)
    );
    public static final Item SKYBOX_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(NovaMod.MOD_ID, SKYBOX_BLOCK_ID),
            new BlockItem(SKYBOX, new Item.Settings())
    );

    private NovaModBlocks() {
    }

    /**
     * 触发本类静态注册器的初始化。
     */
    public static void initialize() {
    }
}
