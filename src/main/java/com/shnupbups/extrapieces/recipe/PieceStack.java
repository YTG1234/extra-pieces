package com.shnupbups.extrapieces.recipe;

import com.shnupbups.extrapieces.PieceSet;
import com.shnupbups.extrapieces.PieceType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;

public class PieceStack {
	public static final PieceStack EMPTY = new PieceStack(null,0);

	public PieceType type;
	public int count;

	public PieceStack(PieceType type) {
		this(type,1);
	}

	public PieceStack(PieceType type, int count) {
		this.type=type;
		this.count=count;
		System.out.println("EXTRA PIECES DEBUG! New PieceStack! "+this.toString());
	}

	public boolean isEmpty() {
		return type==null;
	}

	public boolean equals(PieceStack stack) {
		return typeEquals(stack)&&stack.getCount()==this.getCount();
	}

	public boolean typeEquals(PieceStack stack) {
		return stack.getType().equals(this.getType());
	}

	public PieceType getType() {
		return type;
	}

	public int getCount() {
		return count;
	}

	public ItemStack toItemStack(Block base) {
		//System.out.println("Getting itemstack for piecestack "+this.toString()+" with base "+base.getTranslationKey());
		ItemStack is =  new ItemStack(PieceSet.getPiece(base, type),count);
		//System.out.println("Result: "+is.toString());
		return is;
	}

	public String toString() {
		return "PieceStack{type: "+this.getType()+" count: "+this.getCount()+"}";
	}

	public static PieceStack read(PacketByteBuf buf) {
		PieceType type = PieceType.readPieceType(buf);
		int count = buf.readInt();
		return new PieceStack(type, count);
	}

	public static void write(PacketByteBuf buf, PieceStack stack) {
		stack.type.writePieceType(buf);
		buf.writeVarInt(stack.count);
	}

	public void write(PacketByteBuf buf) {
		write(buf, this);
	}
}
