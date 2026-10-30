package nova.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import nova.NovaMod;
import nova.block.entity.NovaBlockEntity;
import nova.block.entity.SkyboxBlockEntity;

public class ModBlocks {
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
    public static final String NOVA_BLOCK_ID = "nova_block";
    public static final NovaBlockEntityBlock NOVA_BLOCK = Registry.register(
            Registries.BLOCK,
            new Identifier(NovaMod.MOD_ID, NOVA_BLOCK_ID),
            new NovaBlockEntityBlock(AbstractBlock.Settings.copy(Blocks.GLASS))
    );
    public static final BlockEntityType<NovaBlockEntity> NOVA_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(NovaMod.MOD_ID, NOVA_BLOCK_ID),
            BlockEntityType.Builder.create(NovaBlockEntity::new, NOVA_BLOCK).build(null)
    );
    public static final Item NOVA_BLOCK_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(NovaMod.MOD_ID, NOVA_BLOCK_ID),
            new BlockItem(NOVA_BLOCK, new Item.Settings())
    );
    public static Block register(String id, Block block) {
        registerBlockItem(id, block);
        return Registry.register(Registries.BLOCK, new Identifier(NovaMod.MOD_ID,id) , block);
    }
    public  static void registerBlockItem(String id,Block block){
        Registry.register(Registries.ITEM,new Identifier(NovaMod.MOD_ID,id),new BlockItem(block,new Item.Settings()));
    }
    public static void registerModBlocks(){

    }
}
