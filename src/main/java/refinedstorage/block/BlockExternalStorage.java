package refinedstorage.block;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import refinedstorage.RefinedStorage;
import refinedstorage.RefinedStorageGui;
import refinedstorage.tile.TileBase;
import refinedstorage.tile.TileExternalStorage;

public class BlockExternalStorage extends BlockMachine {
    public BlockExternalStorage() {
        super("external_storage");
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileExternalStorage();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(RefinedStorage.INSTANCE, RefinedStorageGui.STORAGE, world, pos.getX(), pos.getY(), pos.getZ());
        }

        return true;
    }
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, player, itemStack);

        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileBase) {
        	EnumFacing dir = BlockPistonBase.getFacingFromEntity(pos, player);
        	if(player.isSneaking()) dir = dir.getOpposite();
        	((TileBase) tile).setDirection(dir);
        }
    }
}
