package org.novamod;

import net.fabricmc.api.ModInitializer;
import org.novamod.block.NovaModBlocks;
import org.novamod.item.NovaModItemGroups;

/**
 * 初始化 NovaMod 的公共内容注册。
 */
public final class NovaMod implements ModInitializer {
    public static final String MOD_ID = "novamod";

    @Override
    public void onInitialize() {
        NovaModBlocks.initialize();
        NovaModItemGroups.initialize();
    }
}
