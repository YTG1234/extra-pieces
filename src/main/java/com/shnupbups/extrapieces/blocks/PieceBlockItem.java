package com.shnupbups.extrapieces.blocks;

import com.shnupbups.extrapieces.register.EPItemGroups;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;

public class PieceBlockItem extends BlockItem {
	private final PieceBlock pb;

	public PieceBlockItem(PieceBlock block, Item.Settings settings) {
		super(block.getBlock(), settings);
		pb = block;
	}

	public PieceBlock getPieceBlock() {
		return pb;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Text getName(ItemStack stack) {
		if (Language.getInstance().hasTranslation(this.getTranslationKey(stack))) return super.getName(stack);
		return new TranslatableText(getPieceBlock().getType().getTranslationKey(), new TranslatableText(getPieceBlock().getSet().getTranslationKey()));
	}

	@Override
	protected boolean isIn(ItemGroup group) {
		return group.equals(EPItemGroups.getItemGroup(this.getPieceBlock().getType())) || group.equals(ItemGroup.SEARCH) || super.isIn(group);
	}
}
