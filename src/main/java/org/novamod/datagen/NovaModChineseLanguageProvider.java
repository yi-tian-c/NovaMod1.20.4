package org.novamod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import org.novamod.block.NovaModBlocks;

/**
 * 生成 NovaMod 的简体中文语言条目。
 */
public final class NovaModChineseLanguageProvider extends FabricLanguageProvider {
    public NovaModChineseLanguageProvider(FabricDataOutput output) {
        super(output, "zh_cn");
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        translationBuilder.add(NovaModBlocks.SKYBOX, "天空盒方块");
        translationBuilder.add("itemGroup.novamod.content", "NovaMod");
    }
}
