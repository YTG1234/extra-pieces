package com.shnupbups.extrapieces.register;

import com.shnupbups.extrapieces.blocks.PieceBlock;
import com.shnupbups.extrapieces.core.PieceSet;
import com.shnupbups.extrapieces.core.PieceSets;
import com.swordglowsblue.artifice.api.ArtificeResourcePack;
import static com.shnupbups.extrapieces.EPUtilities.*;

public class EPModels {
	public static void init(ArtificeResourcePack.ClientResourcePackBuilder pack) {
		int m = 0;
		for (PieceSet set : PieceSets.registry.values()) {
			for (PieceBlock pb : set.getPieceBlocks()) {
				if (!set.isVanillaPiece(pb.getType())) {
					pb.getType().addModels(pack, pb);
					pb.getType().addBlockState(pack, pb);
					m++;
				}
			}
		}
		debugLog("Added models and blockstates for " + m + " blocks!");
		dump(pack);
	}
}
