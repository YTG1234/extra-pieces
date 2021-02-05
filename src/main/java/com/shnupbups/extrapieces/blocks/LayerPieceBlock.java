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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
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
public class LayerPieceBlock extends Block implements Waterloggable, PieceBlock {
	public static final IntProperty LAYERS;
	public static final BooleanProperty WATERLOGGED;
	public static final EnumProperty<Direction> FACING;

	static {
		LAYERS = Properties.LAYERS;
		WATERLOGGED = Properties.WATERLOGGED;
		FACING = Properties.FACING;
	}

	private final PieceSet set;

	public LayerPieceBlock(PieceSet set) {
		super(FabricBlockSettings.copyOf(set.getBase()).materialColor(set.getBase().getDefaultMaterialColor()));
		this.set = set;
		this.setDefaultState(this.stateManager.getDefaultState().with(LAYERS, 1).with(FACING, Direction.UP).with(WATERLOGGED, false));
	}

	public PieceSet getSet() {
		return set;
	}

	public Block getBlock() {
		return this;
	}

	public PieceType getType() {
		return PieceTypes.LAYER;
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		Direction dir = state.get(FACING);
		int layers = state.get(LAYERS);
		if(layers == 8) return VoxelShapes.fullCube();
		switch(dir) {
			case UP:
				return Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D*layers, 16.0D);
			case DOWN:
				return Block.createCuboidShape(0.0D, 16.0D-(layers*2.0D), 0.0D, 16.0D, 16.0D, 16.0D);
			case EAST:
				return Block.createCuboidShape(0.0D, 0.0D, 0.0D, 2.0D*layers, 16.0D, 16.0D);
			case SOUTH:
				return Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D*layers);
			case WEST:
				return Block.createCuboidShape(16.0D-(layers*2.0D), 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
			case NORTH:
				return Block.createCuboidShape(0.0D, 0.0D, 16.0D-(layers*2.0D), 16.0D, 16.0D, 16.0D);
		} return VoxelShapes.empty();
	}

	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	public boolean canReplace(BlockState state, ItemPlacementContext itemPlacementContext) {
		int layers = state.get(LAYERS);
		if (itemPlacementContext.getStack().getItem() == this.asItem() && layers < 8) {
			if (itemPlacementContext.canReplaceExisting()) {
				return itemPlacementContext.getSide() == state.get(FACING);
			} else {
				return true;
			}
		}
		return false;
	}

	public BlockState getPlacementState(ItemPlacementContext itemPlacementContext) {
		BlockPos pos = itemPlacementContext.getBlockPos();
		BlockState state = itemPlacementContext.getWorld().getBlockState(pos);
		if (state.getBlock() == this) {
			int layers = state.get(LAYERS);
			BlockState newState = state.with(LAYERS, Math.min(8, layers + 1));
			if (layers + 1 >= 8) {
				newState = newState.with(WATERLOGGED, false);
			}
			return newState;
		} else {
			FluidState fluidState = itemPlacementContext.getWorld().getFluidState(pos);
			return this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER).with(FACING, itemPlacementContext.getSide());
		}
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> stateFactory$Builder) {
		stateFactory$Builder.add(LAYERS);
		stateFactory$Builder.add(WATERLOGGED);
		stateFactory$Builder.add(FACING);
	}

	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		return state.get(LAYERS) < 8 && Waterloggable.super.tryFillWithFluid(world, pos, state, fluidState);
	}

	public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
		return state.get(LAYERS) < 8 && Waterloggable.super.canFillWithFluid(world, pos, state, fluid);
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
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState state_2, boolean notify) {
		super.onBlockAdded(state, world, pos, state_2, notify);
		if (state.getBlock() != state_2.getBlock()) {
			this.getBase().getDefaultState().neighborUpdate(world, pos, Blocks.AIR, pos, false);
			this.getBase().getDefaultState().onBlockAdded(world, pos, state_2, false);
		}
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState state_2, boolean moved) {
		super.onStateReplaced(state, world, pos, state_2, moved);
		if (state.getBlock() != state_2.getBlock()) {
			this.getBaseState().onStateReplaced(world, pos, state_2, moved);
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
	
	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return super.emitsRedstonePower(state) || this.getBaseState().emitsRedstonePower();
	}
	
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		float power = (float)this.getBaseState().getWeakRedstonePower(world, pos, direction);
		power = (power / 8) * state.get(LAYERS);
		return Math.round(power);
	}
}
