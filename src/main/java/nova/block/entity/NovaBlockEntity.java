package nova.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import nova.block.ModBlocks;

/**
 * 标记需要在客户端绘制 Code Sky 投影表面的方块位置。
 */
public final class NovaBlockEntity extends BlockEntity {
    public NovaBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.NOVA_BLOCK_ENTITY, pos, state);
    }
}
