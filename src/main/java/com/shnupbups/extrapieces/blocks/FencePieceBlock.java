package com.shnupbups.extrapieces.blocks;

import java.util.Random;

import com.shnupbups.extrapieces.core.PieceSet;
import com.shnupbups.extrapieces.core.PieceType;
import com.shnupbups.extrapieces.core.PieceTypes;
import static com.shnupbups.extrapieces.EPUtilities.*;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

@SuppressWarnings("deprecation")
public class FencePieceBlock extends FenceBlock implements PieceBlock {
	private final PieceSet set;

	public FencePieceBlock(PieceSet set) {
		super(FabricBlockSettings.copyOf(set.getBase()).materialColor(set.getBase().getDefaultMaterialColor()));
		this.set = set;
	}

	@Override
	public PieceType getType() {
		return PieceTypes.FENCE;
	}

	@Override
	public Block getBlock() {
		return this;
	}

	@Override
	public PieceSet getSet() {
		return set;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		super.randomDisplayTick(state, world, pos, random);
		this.getBase().randomDisplayTick(this.getBaseState(), world, pos, random);
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
	public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
		super.onDestroyedByExplosion(world, pos, explosion);
		this.getBase().onDestroyedByExplosion(world, pos, explosion);
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
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ActionResult a = super.onUse(state, world, pos, player, hand, hit);
		if (a.isAccepted() || this.getBaseState().onUse(world, player, hand, hit).isAccepted()) {
			return ActionResult.SUCCESS;
		} else {
			return ActionResult.PASS;
		}
	}

	@Environment(EnvType.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
		return getSet().isTransparent() ?
			   (stateFrom.getBlock() == this || super.isSideInvisible(state, stateFrom, direction)) :
			   super.isSideInvisible(state, stateFrom, direction);
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
	public boolean emitsRedstonePower(BlockState state) {
		return super.emitsRedstonePower(state) || this.getBaseState().emitsRedstonePower();
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		super.scheduledTick(state, world, pos, random);
		this.getBase().scheduledTick(this.getBaseState(), world, pos, random);
	}

	@Override
	public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		super.onBlockBreakStart(state, world, pos, player);
		this.getBaseState().onBlockBreakStart(world, pos, player);
	}

	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return this.getBaseState().getWeakRedstonePower(world, pos, direction);
	}
}
