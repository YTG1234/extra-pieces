package com.shnupbups.extrapieces.blocks;

import com.shnupbups.extrapieces.ExtraPieces;
import com.shnupbups.extrapieces.core.PieceSet;
import com.shnupbups.extrapieces.core.PieceType;
import com.shnupbups.extrapieces.core.PieceTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.Random;

import static com.shnupbups.extrapieces.EPUtilities.*;

@SuppressWarnings("deprecation")
public class CornerPieceBlock extends Block implements Waterloggable, PieceBlock {
	public static final DirectionProperty FACING;
	public static final BooleanProperty WATERLOGGED;
	protected static final VoxelShape NORTH_SIDING_SHAPE;
	protected static final VoxelShape EAST_SIDING_SHAPE;
	protected static final VoxelShape SOUTH_SIDING_SHAPE;
	protected static final VoxelShape WEST_SIDING_SHAPE;
	protected static final VoxelShape NORTH_EXTRA_SHAPE;
	protected static final VoxelShape EAST_EXTRA_SHAPE;
	protected static final VoxelShape SOUTH_EXTRA_SHAPE;
	protected static final VoxelShape WEST_EXTRA_SHAPE;
	protected static final VoxelShape NORTH_SHAPE;
	protected static final VoxelShape EAST_SHAPE;
	protected static final VoxelShape SOUTH_SHAPE;
	protected static final VoxelShape WEST_SHAPE;

	static {
		FACING = Properties.HORIZONTAL_FACING;
		WATERLOGGED = Properties.WATERLOGGED;
		NORTH_SIDING_SHAPE = SidingPieceBlock.SINGLE_SHAPE_NORTH;
		EAST_SIDING_SHAPE = SidingPieceBlock.SINGLE_SHAPE_EAST;
		SOUTH_SIDING_SHAPE = SidingPieceBlock.SINGLE_SHAPE_SOUTH;
		WEST_SIDING_SHAPE = SidingPieceBlock.SINGLE_SHAPE_WEST;
		NORTH_EXTRA_SHAPE = Block.createCuboidShape(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
		EAST_EXTRA_SHAPE = Block.createCuboidShape(8.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
		SOUTH_EXTRA_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 8.0D, 8.0D, 16.0D, 16.0D);
		WEST_EXTRA_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 8.0D);
		NORTH_SHAPE = VoxelShapes.union(NORTH_SIDING_SHAPE, NORTH_EXTRA_SHAPE);
		EAST_SHAPE = VoxelShapes.union(EAST_SIDING_SHAPE, EAST_EXTRA_SHAPE);
		SOUTH_SHAPE = VoxelShapes.union(SOUTH_SIDING_SHAPE, SOUTH_EXTRA_SHAPE);
		WEST_SHAPE = VoxelShapes.union(WEST_SIDING_SHAPE, WEST_EXTRA_SHAPE);
	}

	private final PieceSet set;

	public CornerPieceBlock(PieceSet set) {
		super(FabricBlockSettings.copyOf(set.getBase()).materialColor(set.getBase().getDefaultMaterialColor()));
		this.set = set;
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(WATERLOGGED, false));
	}

	public PieceSet getSet() {
		return set;
	}

	public Block getBlock() {
		return this;
	}

	public PieceType getType() {
		return PieceTypes.CORNER;
	}

	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		switch (state.get(FACING)) {
			case EAST:
				return EAST_SHAPE;
			case SOUTH:
				return SOUTH_SHAPE;
			case WEST:
				return WEST_SHAPE;
			default:
				return NORTH_SHAPE;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		super.randomDisplayTick(state, world, pos, random);
		this.getBase().randomDisplayTick(this.getBaseState(), world, pos, random);
	}

	@Override
	public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		super.onBlockBreakStart(state, world, pos, player);
		this.getBaseState().onBlockBreakStart(world, pos, player);
	}

	@Override
	public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
		super.onBroken(world, pos, state);
		this.getBase().onBroken(world, pos, state);
	}

	@Override
	public float getBlastResistance() {
		return this.getBase().getBlastResistance();
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		super.onBlockAdded(state, world, pos, oldState, notify);
		if (state.getBlock() != oldState.getBlock()) {
			this.getBase().getDefaultState().neighborUpdate(world, pos, Blocks.AIR, pos, false);
			this.getBase().getDefaultState().onBlockAdded(world, pos, oldState, false);
		}
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		super.onStateReplaced(state, world, pos, newState, moved);
		if (state.getBlock() != newState.getBlock()) {
			this.getBaseState().onStateReplaced(world, pos, newState, moved);
		}
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, Entity entity) {
		super.onSteppedOn(world, pos, entity);
		try {
			this.getBase().onSteppedOn(world, pos, entity);
		} catch (IllegalArgumentException ignored) {
			debugLog("Caught an exception in onSteppedOn for "+this.getPieceString());
		}
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		super.scheduledTick(state, world, pos, random);
		this.getBase().scheduledTick(this.getBaseState(), world, pos, random);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ActionResult a = super.onUse(state, world, pos, player, hand, hit);
		if(a.isAccepted() || this.getBaseState().onUse(world, player, hand, hit).isAccepted()) {
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.PASS;
		}
	}

	@Override
	public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
		super.onDestroyedByExplosion(world, pos, explosion);
		this.getBase().onDestroyedByExplosion(world, pos, explosion);
	}

	public BlockState getPlacementState(ItemPlacementContext context) {
		BlockPos pos = context.getBlockPos();
		FluidState fluidState = context.getWorld().getFluidState(pos);
		double xPos = context.getHitPos().getX() - pos.getX();
		double zPos = context.getHitPos().getZ() - pos.getZ();
		Direction direction = context.getPlayerFacing().getOpposite();
		if (direction == Direction.EAST) {
			if (zPos < 0.5) direction = direction.rotateYClockwise();
		} else if (direction == Direction.WEST) {
			if (zPos > 0.5) direction = direction.rotateYClockwise();
		} else if (direction == Direction.SOUTH) {
			if (xPos > 0.5) direction = direction.rotateYClockwise();
		} else {
			if (xPos < 0.5) direction = direction.rotateYClockwise();
		}
		return this.getDefaultState().with(FACING, direction).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
	}

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if (state.get(WATERLOGGED)) {
			world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	public boolean canPlaceAtSide(BlockState state, BlockView world, BlockPos pos, NavigationType navigationType) {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return getSet().isTransparent() ? (stateFrom.getBlock() == this || super.isSideInvisible(state, stateFrom, direction)) : super.isSideInvisible(state, stateFrom, direction);
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
