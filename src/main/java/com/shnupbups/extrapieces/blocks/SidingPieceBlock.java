package com.shnupbups.extrapieces.blocks;

import com.shnupbups.extrapieces.ExtraPieces;
import com.shnupbups.extrapieces.core.PieceSet;
import com.shnupbups.extrapieces.core.PieceType;
import com.shnupbups.extrapieces.core.PieceTypes;
import com.shnupbups.extrapieces.register.EPProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
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
public class SidingPieceBlock extends Block implements Waterloggable, PieceBlock {
	public static final EnumProperty<EPProperties.SidingType> TYPE;
	public static final BooleanProperty WATERLOGGED;
	public static final DirectionProperty FACING_HORIZONTAL;
	protected static final VoxelShape SINGLE_SHAPE_SOUTH;
	protected static final VoxelShape SINGLE_SHAPE_NORTH;
	protected static final VoxelShape SINGLE_SHAPE_EAST;
	protected static final VoxelShape SINGLE_SHAPE_WEST;

	static {
		TYPE = EPProperties.SIDING_TYPE;
		WATERLOGGED = Properties.WATERLOGGED;
		FACING_HORIZONTAL = Properties.HORIZONTAL_FACING;
		SINGLE_SHAPE_NORTH = Block.createCuboidShape(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
		SINGLE_SHAPE_SOUTH = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
		SINGLE_SHAPE_EAST = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);
		SINGLE_SHAPE_WEST = Block.createCuboidShape(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	}

	private final PieceSet set;

	public SidingPieceBlock(PieceSet set) {
		super(FabricBlockSettings.copyOf(set.getBase()).materialColor(set.getBase().getDefaultMaterialColor()));
		this.set = set;
		this.setDefaultState(this.getDefaultState().with(TYPE, EPProperties.SidingType.SINGLE).with(FACING_HORIZONTAL, Direction.NORTH).with(WATERLOGGED, false));
	}

	public Block getBlock() {
		return this;
	}

	public PieceSet getSet() {
		return set;
	}

	public PieceType getType() {
		return PieceTypes.SIDING;
	}

	public boolean hasSidedTransparency(BlockState state) {
		return state.get(TYPE) != EPProperties.SidingType.DOUBLE;
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(TYPE, FACING_HORIZONTAL, WATERLOGGED);
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		EPProperties.SidingType slabType = state.get(TYPE);
		Direction facing = state.get(FACING_HORIZONTAL);
		if (slabType == EPProperties.SidingType.DOUBLE) {
			return VoxelShapes.fullCube();
		} else {
			switch (facing) {
				case SOUTH:
					return SINGLE_SHAPE_SOUTH;
				case EAST:
					return SINGLE_SHAPE_EAST;
				case WEST:
					return SINGLE_SHAPE_WEST;
				default:
					return SINGLE_SHAPE_NORTH;
			}
		}
	}

	public BlockState getPlacementState(ItemPlacementContext context) {
		BlockPos pos = context.getBlockPos();
		BlockState state = context.getWorld().getBlockState(pos);
		if (state.getBlock() == this) {
			return state.with(TYPE, EPProperties.SidingType.DOUBLE).with(FACING_HORIZONTAL, state.get(FACING_HORIZONTAL)).with(WATERLOGGED, false);
		} else {
			FluidState fluidState = context.getWorld().getFluidState(pos);
			Direction playerHorizontalFacing = context.getPlayerFacing();
			Direction facing = context.getSide();
			double xPos = context.getHitPos().getX() - pos.getX();
			double zPos = context.getHitPos().getZ() - pos.getZ();
			Direction direction = playerHorizontalFacing.getOpposite();
			if (facing.getAxis().isVertical()) {
				if (direction == Direction.EAST || direction == Direction.WEST) {
					if (xPos > 0.5) direction = Direction.WEST;
					else direction = Direction.EAST;
				} else {
					if (zPos > 0.5) direction = Direction.NORTH;
					else direction = Direction.SOUTH;
				}
			}
			return this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER).with(FACING_HORIZONTAL, direction);
		}
	}

	public boolean canReplace(BlockState state, ItemPlacementContext context) {
		ItemStack itemStack = context.getStack();
		EPProperties.SidingType slabType = state.get(TYPE);
		Direction facing = state.get(FACING_HORIZONTAL);
		if (slabType != EPProperties.SidingType.DOUBLE && itemStack.getItem() == this.asItem()) {
			if (context.canReplaceExisting()) {
				boolean bl;
				switch (facing) {
					case EAST:
						bl = context.getHitPos().getX() - (double) context.getBlockPos().getX() > 0.5D;
						break;
					case WEST:
						bl = context.getHitPos().getX() - (double) context.getBlockPos().getX() < 0.5D;
						break;
					case SOUTH:
						bl = context.getHitPos().getZ() - (double) context.getBlockPos().getZ() > 0.5D;
						break;
					default:
						bl = context.getHitPos().getZ() - (double) context.getBlockPos().getZ() < 0.5D;
				}
				Direction direction = context.getSide();
				return direction == facing || bl && direction.getAxis().isVertical();
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		return state.get(TYPE) != EPProperties.SidingType.DOUBLE && Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
	}

	public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
		return state.get(TYPE) != EPProperties.SidingType.DOUBLE && Waterloggable.super.canFillWithFluid(world, pos, state, fluid);
	}

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if (state.get(WATERLOGGED) && direction != state.get(FACING_HORIZONTAL).getOpposite()) {
			world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}

	public boolean canPlaceAtSide(BlockState state, BlockView world, BlockPos pos, NavigationType navigationType) {
		switch (navigationType) {
			case LAND:
				return state.get(TYPE) == EPProperties.SidingType.SINGLE;
			case WATER:
				return world.getFluidState(pos).isIn(FluidTags.WATER);
			default:
				return false;
		}
	}

	@Environment(EnvType.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return getSet().isTransparent() ? (stateFrom.getBlock() == this || super.isSideInvisible(state, stateFrom, direction)) : super.isSideInvisible(state, stateFrom, direction);
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
			debugLog("Caught an exception in onSteppedOn for " + this.getPieceString());
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
	
	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return super.emitsRedstonePower(state) || this.getBaseState().emitsRedstonePower();
	}
	
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		float power = (float)this.getBaseState().getWeakRedstonePower(world, pos, direction);
		if(state.get(TYPE).equals(EPProperties.SidingType.SINGLE)) power /= 2;
		return Math.round(power);
	}
}
