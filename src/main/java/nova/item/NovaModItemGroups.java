package nova.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nova.NovaMod;
import nova.block.ModBlocks;

/**
 * 注册 NovaMod 方块内容使用的创造栏分组。
 */
public final class NovaModItemGroups {
    public static final ItemGroup NOVAMOD = Registry.register(
            Registries.ITEM_GROUP,
            new Identifier(NovaMod.MOD_ID, "content"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.novamod.content"))
                    .icon(() -> new ItemStack(ModBlocks.SKYBOX_ITEM))
                    .entries((context, entries) -> entries.add(ModBlocks.SKYBOX_ITEM))
                    .entries((context, entries) -> entries.add(ModBlocks.NOVA_BLOCK_ITEM))
                    .build()
    );

    private NovaModItemGroups() {
    }

    /**
     * 触发本类静态注册器的初始化。
     */
    public static void initialize() {
    }
}
