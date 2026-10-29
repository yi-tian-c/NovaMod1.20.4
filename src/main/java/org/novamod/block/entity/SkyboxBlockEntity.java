package org.novamod.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.novamod.block.NovaModBlocks;

/**
 * 标记需要在客户端绘制天空快照表面的方块位置。
 */
public final class SkyboxBlockEntity extends BlockEntity {
    public SkyboxBlockEntity(BlockPos pos, BlockState state) {
        super(NovaModBlocks.SKYBOX_ENTITY, pos, state);
    }
}
