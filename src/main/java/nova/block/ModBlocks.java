package nova.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import nova.NovaMod;

public class ModBlocks {
    public static final Block SKY_BLOCK=register("sky_block",new SkyBlock(AbstractBlock.Settings.copy(Blocks.STONE)));
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
