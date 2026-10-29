package org.novamod.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * 注册 NovaMod 的基础资源和简体中文语言数据生成器。
 */
public final class NovaModDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(NovaModBlockModelProvider::new);
        pack.addProvider(NovaModBlockLootProvider::new);
        pack.addProvider(NovaModChineseLanguageProvider::new);
    }
}
