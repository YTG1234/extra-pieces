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
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.Random;

import static com.shnupbups.extrapieces.EPUtilities.*;

@SuppressWarnings("deprecation")
public class ColumnPieceBlock extends Block implements Waterloggable, PieceBlock {
	public static final EnumProperty<Direction.Axis> AXIS;
	public static final BooleanProperty WATERLOGGED;
	protected static final VoxelShape Y_SHAPE;
	protected static final VoxelShape X_SHAPE;
	protected static final VoxelShape Z_SHAPE;
	protected static final VoxelShape Y_COLLISION;

	static {
		AXIS = Properties.AXIS;
		WATERLOGGED = Properties.WATERLOGGED;
		Y_SHAPE = Block.createCuboidShape(4f, 0f, 4f, 12f, 16f, 12f);
		Y_COLLISION = Block.createCuboidShape(4f, 0f, 4f, 12f, 24f, 12f);
		X_SHAPE = Block.createCuboidShape(0f, 4f, 4f, 16f, 12f, 12f);
		Z_SHAPE = Block.createCuboidShape(4f, 4f, 0f, 12f, 12f, 16f);
	}

	private final PieceSet set;

	public ColumnPieceBlock(PieceSet set) {
		super(FabricBlockSettings.copyOf(set.getBase()).materialColor(set.getBase().getDefaultMaterialColor()));
		this.set = set;
		this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.Y).with(WATERLOGGED, false));
	}

	public Block getBlock() {
		return this;
	}

	public PieceSet getSet() {
		return set;
	}

	public PieceType getType() {
		return PieceTypes.COLUMN;
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Direction.Axis axis = state.get(AXIS);
		switch (axis) {
			case X:
				return X_SHAPE;
			case Z:
				return Z_SHAPE;
			default:
				return Y_SHAPE;
		}
	}

	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Direction.Axis axis = state.get(AXIS);
		if (axis == Direction.Axis.Y) return Y_COLLISION;
		else return super.getCollisionShape(state, world, pos, context);
	}

	public BlockState rotate(BlockState state, BlockRotation rotation) {
		switch (rotation) {
			case COUNTERCLOCKWISE_90:
			case CLOCKWISE_90:
				switch (state.get(AXIS)) {
					case X:
						return state.with(AXIS, Direction.Axis.Z);
					case Z:
						return state.with(AXIS, Direction.Axis.X);
					default:
						return state;
				}
			default:
				return state;
		}
	}

	public BlockState getPlacementState(ItemPlacementContext context) {
		BlockPos pos = context.getBlockPos();
		FluidState fluidState = context.getWorld().getFluidState(pos);
		return this.getDefaultState().with(AXIS, context.getSide().getAxis()).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
	}

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos poposFrom) {
		if (state.get(WATERLOGGED)) {
			world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		return super.getStateForNeighborUpdate(state, direction, newState, world, pos, poposFrom);
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AXIS, WATERLOGGED);
	}

	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	public boolean canPlaceAtSide(BlockState state, BlockView world, BlockPos pos, NavigationType navigationType) {
		return false;
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
		return this.getBaseState().getWeakRedstonePower(world, pos, direction);
	}
}
