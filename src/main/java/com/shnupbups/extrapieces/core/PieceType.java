package com.shnupbups.extrapieces.core;

import com.shnupbups.extrapieces.EPUtilities;
import com.shnupbups.extrapieces.blocks.PieceBlock;
import com.shnupbups.extrapieces.blocks.PieceBlockItem;
import com.shnupbups.extrapieces.recipe.*;
import com.swordglowsblue.artifice.api.ArtificeResourcePack;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

public abstract class PieceType {

	private final Identifier id;

	public PieceType(String id) {
		this(EPUtilities.getID(id));
	}

	public PieceType(Identifier id) {
		this.id = id;
	}

	/**
	 * Gets the name of this {@link PieceType} with {@code baseName_} appended to the front, in all lowercase.<br>
	 * Used for registry.
	 *
	 * @return The name of this {@link PieceType}, in all lowercase.
	 */
	public String getBlockId(String baseName) {
		return baseName.toLowerCase() + "_" + getId().getPath();
	}

	/**
	 * Gets the id of this {@link PieceType}<br>
	 * Used for registry.
	 *
	 * @return The id of this {@link PieceType}
	 */
	public Identifier getId() {
		return this.id;
	}

	public String getTranslationKey() {
		return "piece." + id.getNamespace() + "." + id.getPath();
	}

	/**
	 * Gets the id of the block and item tag of this {@link PieceType}<br>
	 * Used for registry.<br>
	 * Defaults to {@link #getId()} wth an 's' appended
	 *
	 * @return The id of this {@link PieceType}'s tag
	 */
	public Identifier getTagId() {
		return new Identifier(this.id.toString() + "s");
	}

	public abstract PieceBlock getNew(PieceSet set);

	public PacketByteBuf writePieceType(PacketByteBuf buf) {
		buf.writeInt(getId().toString().length());
		buf.writeString(getId().toString());
		return buf;
	}

	public String toString() {
		return getId().toString();
	}

	@Deprecated
	/**
	 * Use {@link #getShapedRecipes()} or {@link #getCraftingRecipes()}
	 */
	public ArrayList<ShapedPieceRecipe> getRecipes() {
		return getShapedRecipes();
	}

	public ArrayList<ShapedPieceRecipe> getShapedRecipes() {
		return new ArrayList<>();
	}

	public ArrayList<ShapelessPieceRecipe> getShapelessRecipes() {
		return new ArrayList<>();
	}

	public ArrayList<PieceRecipe> getCraftingRecipes() {
		ArrayList<PieceRecipe> recipes = new ArrayList<>();
		recipes.addAll(getRecipes());
		recipes.addAll(getShapelessRecipes());
		return recipes;
	}

	public StonecuttingPieceRecipe getStonecuttingRecipe() {
		return new StonecuttingPieceRecipe(this, getStonecuttingCount(), PieceTypes.BASE);
	}

	public WoodmillingPieceRecipe getWoodmillingRecipe() {
		return new WoodmillingPieceRecipe(this, getStonecuttingCount(), PieceTypes.BASE);
	}

	public int getStonecuttingCount() {
		return 1;
	}

	public void addLootTable(ArtificeResourcePack.ServerResourcePackBuilder data, PieceBlock pb) {
		data.addLootTable(EPUtilities.prependToPath(Registry.BLOCK.getId(pb.getBlock()), "blocks/"), loot -> {
			loot.type(new Identifier("block"));
			loot.pool(pool -> {
				pool.rolls(1);
				pool.entry(entry -> {
					entry.type(new Identifier("item"));
					entry.name(Registry.BLOCK.getId(pb.getBlock()));
				});
				pool.condition(new Identifier("survives_explosion"), cond -> {
				});
			});
		});
	}

	public void addModels(ArtificeResourcePack.ClientResourcePackBuilder pack, PieceBlock pb) {
		addBlockModels(pack, pb);
		addItemModel(pack, pb);
	}

	public void addBlockModels(ArtificeResourcePack.ClientResourcePackBuilder pack, PieceBlock pb) {
		pack.addBlockModel(Registry.BLOCK.getId(pb.getBlock()), model -> {
			model.parent(EPUtilities.prependToPath(this.getId(), "block/dummy_"));
			model.texture("particle", pb.getSet().getMainTexture());
			model.texture("main", pb.getSet().getMainTexture());
			model.texture("top", pb.getSet().getTopTexture());
			model.texture("bottom", pb.getSet().getBottomTexture());
		});
	}

	public void addItemModel(ArtificeResourcePack.ClientResourcePackBuilder pack, PieceBlock pb) {
		pack.addItemModel(Registry.BLOCK.getId(pb.getBlock()), model -> {
			model.parent(EPUtilities.prependToPath(Registry.BLOCK.getId(pb.getBlock()), "block/"));
		});
	}

	public void addBlockModel(ArtificeResourcePack.ClientResourcePackBuilder pack, PieceBlock pb, String append) {
		pack.addBlockModel(EPUtilities.appendToPath(Registry.BLOCK.getId(pb.getBlock()), "_" + append), model -> {
			model.parent(EPUtilities.prependToPath(EPUtilities.appendToPath(this.getId(), "_" + append), "block/dummy_"));
			model.texture("particle", pb.getSet().getMainTexture());
			model.texture("main", pb.getSet().getMainTexture());
			model.texture("top", pb.getSet().getTopTexture());
			model.texture("bottom", pb.getSet().getBottomTexture());
		});
	}

	public void addBlockState(ArtificeResourcePack.ClientResourcePackBuilder pack, PieceBlock pb) {
		pack.addBlockState(Registry.BLOCK.getId(pb.getBlock()), state -> {
			state.variant("", var -> {
				var.model(EPUtilities.prependToPath(Registry.BLOCK.getId(pb.getBlock()), "block/"));
			});
		});
	}

	public PieceBlockItem getBlockItem(PieceBlock pb) {
		return new PieceBlockItem(pb, new Item.Settings());
	}
	
	public Identifier getModelPath(PieceBlock pb) {
		return EPUtilities.prependToPath(Registry.BLOCK.getId(pb.getBlock()), "block/");
	}
	
	public Identifier getModelPath(PieceBlock pb, String append) {
		return EPUtilities.prependToPath(EPUtilities.appendToPath(Registry.BLOCK.getId(pb.getBlock()), "_" + append), "block/");
	}
}
