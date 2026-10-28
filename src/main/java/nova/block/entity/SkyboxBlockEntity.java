package nova.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import nova.block.ModBlocks;

public final class SkyboxBlockEntity extends BlockEntity {
    public SkyboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SKYBOX_ENTITY, pos, state);
    }
}
