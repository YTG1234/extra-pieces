package com.shnupbups.extrapieces.pieces;

import com.shnupbups.extrapieces.EPUtilities;
import com.shnupbups.extrapieces.blocks.FencePieceBlock;
import com.shnupbups.extrapieces.blocks.PieceBlock;
import com.shnupbups.extrapieces.core.PieceSet;
import com.shnupbups.extrapieces.core.PieceType;
import com.shnupbups.extrapieces.core.PieceTypes;
import com.shnupbups.extrapieces.recipe.ShapedPieceRecipe;
import com.swordglowsblue.artifice.api.ArtificeResourcePack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

public class FencePiece extends PieceType {
	public FencePiece() {
		super("fence");
	}

	public FencePieceBlock getNew(PieceSet set) {
		return new FencePieceBlock(set);
	}

	public Identifier getTagId() {
		return new Identifier("minecraft", "fences");
	}

	public ArrayList<ShapedPieceRecipe> getShapedRecipes() {
		ArrayList<ShapedPieceRecipe> recipes = super.getShapedRecipes();
		recipes.add(new ShapedPieceRecipe(this, 3, "bsb", "bsb").addToKey('b', PieceTypes.BASE).addToKey('s', Items.STICK));
		return recipes;
	}

	public void addBlockModels(ArtificeResourcePack.ClientResourcePackBuilder pack, PieceBlock pb) {
		super.addBlockModels(pack, pb);
		addBlockModel(pack, pb, "side");
		addBlockModel(pack, pb, "inventory");
	}

	public void addItemModel(ArtificeResourcePack.ClientResourcePackBuilder pack, PieceBlock pb) {
		pack.addItemModel(Registry.BLOCK.getId(pb.getBlock()), model -> {
			model.parent(EPUtilities.prependToPath(EPUtilities.appendToPath(Registry.BLOCK.getId(pb.getBlock()), "_inventory"), "block/"));
		});
	}

	public void addBlockState(ArtificeResourcePack.ClientResourcePackBuilder pack, PieceBlock pb) {
		pack.addBlockState(Registry.BLOCK.getId(pb.getBlock()), state -> {
			state.multipartCase(caze -> {
				caze.apply(var -> {
					var.model(getModelPath(pb));
				});
			});
			for (Direction d : Direction.values()) {
				if (d != Direction.UP && d != Direction.DOWN) {
					state.multipartCase(caze -> {
						caze.when(d.asString(), "true");
						caze.apply(var -> {
							var.model(getModelPath(pb, "side"));
							var.uvlock(true);
							switch (d) {
								case EAST:
									var.rotationY(90);
									break;
								case WEST:
									var.rotationY(270);
									break;
								case SOUTH:
									var.rotationY(180);
									break;
							}
						});
					});
				}
			}
		});
	}
}
