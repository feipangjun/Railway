package com.railwayteam.railways.blocks;

import com.railwayteam.railways.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;

import net.minecraft.block.AbstractBlock.Properties;

public class LargeSwitchTrackBlock extends AbstractLargeTrackBlock {
  public static final String name = "large_switch";

  public static EnumProperty<LargeSwitchSide> SWITCH_SIDE = EnumProperty.create("bigswitch", LargeSwitchSide.class);

  public LargeSwitchTrackBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any()
      .setValue(SWITCH_SIDE, LargeSwitchSide.NORTH_SOUTHEAST)
      .setValue(BlockStateProperties.ENABLED, false) // tracking whether it's turning or straight
    );
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockItemUseContext context) {
    return super.getStateForPlacement(context)
      .setValue(SWITCH_SIDE, LargeSwitchSide.NORTH_SOUTHEAST)
      .setValue(BlockStateProperties.ENABLED, false);
  }

  @Override
  protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
    builder.add(SWITCH_SIDE);
    builder.add(BlockStateProperties.ENABLED);
  }

  @Override
  protected boolean canConnectFrom (BlockState state, IWorld worldIn, BlockPos pos, Util.Vector direction) {
    return state.getValue(SWITCH_SIDE).connectsTo(direction.value);
  }

  @Override
  protected BlockState checkForConnections (BlockState state, IWorld worldIn, BlockPos pos) {
    BlockPos other = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    ArrayList<Vector3i> directions = new ArrayList<>();
    for (int x=-1; x<2; x++) {
      for (int z=-1; z<2; z++) {
        if (other.offset(x,0,z).equals(pos)) continue;
        //  Railways.LOGGER.debug("  checking at " + other.add(x,0,z));
        if (worldIn.getBlockState(other.offset(x,0,z)).getBlock() instanceof AbstractLargeTrackBlock) {
          //  Railways.LOGGER.debug("  found at " + x + "," + z);
          directions.add(new Vector3i(x,0,z));
        }
      }
    }
    switch (directions.size()) {
      case 3:
        state = state.setValue(SWITCH_SIDE, LargeSwitchSide.findValidStateFrom(directions.get(0), directions.get(1), directions.get(2)));
        break;
      case 2:
        state = state.setValue(SWITCH_SIDE, LargeSwitchSide.findValidStateFrom(directions.get(0), directions.get(1)));
        break;
      case 1:
        state = state.setValue(SWITCH_SIDE, LargeSwitchSide.findValidStateFrom(directions.get(0)));
        break;
      case 0:
        // state = state; // use regular state
        break;
      default:
        boolean found = false;
        //  Railways.LOGGER.debug("Found " + directions.size() + " possible connections");
        for (Vector3i dir : directions) {
          //  Railways.LOGGER.debug("checking " + dir + " vs " + Util.opposite(dir));
          if (directions.contains(Util.opposite(dir))) {
            state = state.setValue(SWITCH_SIDE, LargeSwitchSide.findValidStateFrom(dir));
            found = true;
            //  Railways.LOGGER.debug("  found a straight connection");
          }
        }
        // else
        if (!found) state = state.setValue(SWITCH_SIDE, LargeSwitchSide.findValidStateFrom(directions.get(0),directions.get(1)));
    }
    return state;
  }

  public boolean isTurning (BlockState state) {
    return state.getValue(BlockStateProperties.ENABLED);
  }
}
