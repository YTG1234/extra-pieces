package com.shnupbups.extrapieces.blocks;

import com.shnupbups.extrapieces.core.PieceSet;
import com.shnupbups.extrapieces.core.PieceType;
import com.shnupbups.extrapieces.core.PieceTypes;
import static com.shnupbups.extrapieces.EPUtilities.*;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

@SuppressWarnings("deprecation")
public class StairsPieceBlock extends StairsBlock implements PieceBlock {
	private final PieceSet set;

	public StairsPieceBlock(PieceSet set) {
		super(set.getBase().getDefaultState(), FabricBlockSettings.copyOf(set.getBase()).materialColor(set.getBase().getDefaultMaterialColor()));
		this.set = set;
	}

	public Block getBlock() {
		return this;
	}

	public PieceSet getSet() {
		return set;
	}

	public PieceType getType() {
		return PieceTypes.STAIRS;
	}

	@Environment(EnvType.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return getSet().isTransparent() ? (stateFrom.getBlock() == this || super.isSideInvisible(state, stateFrom, direction)) : super.isSideInvisible(state, stateFrom, direction);
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, Entity entity) {
		try {
			this.getBase().onSteppedOn(world, pos, entity);
		} catch (IllegalArgumentException ignored) {
			debugLog("Caught an exception in onSteppedOn for "+this.getPieceString());
		}
	}
	
	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return super.emitsRedstonePower(state) || this.getBaseState().emitsRedstonePower();
	}
	
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		float power = (float)this.getBaseState().getWeakRedstonePower(world, pos, direction);
		power = (power / 4) * 3;
		return Math.round(power);
	}
}
