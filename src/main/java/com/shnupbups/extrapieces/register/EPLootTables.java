package com.shnupbups.extrapieces.register;

import com.shnupbups.extrapieces.core.PieceSet;
import com.shnupbups.extrapieces.core.PieceSets;
import com.swordglowsblue.artifice.api.ArtificeResourcePack;
import static com.shnupbups.extrapieces.EPUtilities.*;

public class EPLootTables {
	static int l;

	public static void init(ArtificeResourcePack.ServerResourcePackBuilder data) {
		for (PieceSet ps : PieceSets.registry.values()) {
			ps.addLootTables(data);
		}
		debugLog("Registered " + l + " loot tables!");
	}

	public static void incrementLootTables() {
		l++;
	}
}
