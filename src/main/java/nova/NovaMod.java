package nova;

import net.fabricmc.api.ModInitializer;

import nova.block.ModBlocks;
import nova.entity.ModEntities;
import nova.item.NovaModItemGroups;
import nova.particle.ModParticles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 注册模组的方块、实体、粒子和物品分组。 */
public class NovaMod implements ModInitializer {
	/** 所有模组资源及注册项共用的命名空间。 */
	public static final String MOD_ID = "novamod";

	/** 输出模组初始化及运行日志。 */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();
		ModParticles.initialize();
		ModEntities.registerEntities();
		ModEntities.registerAttributes();
		NovaModItemGroups.initialize();
		LOGGER.info("Hello Fabric world!");
	}
}
