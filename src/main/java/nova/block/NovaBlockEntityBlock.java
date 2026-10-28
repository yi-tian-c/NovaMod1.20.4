package nova.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import nova.block.entity.NovaBlockEntity;

/**
 * NovaBlock：走普通模型管线，方便 Iris 通过 block.properties 识别并绘制 Code Sky。
 * 保留 BlockEntity 以便后续扩展。
 */
public final class NovaBlockEntityBlock extends Block implements BlockEntityProvider {
    public NovaBlockEntityBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return Block.createCodec(NovaBlockEntityBlock::new);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NovaBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // 关键：改成 MODEL，让 Iris 能正常处理
        return BlockRenderType.MODEL;
    }
}
