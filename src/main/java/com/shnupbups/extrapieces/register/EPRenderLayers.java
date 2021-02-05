package com.shnupbups.extrapieces.register;

import com.shnupbups.extrapieces.core.PieceSet;
import com.shnupbups.extrapieces.core.PieceSets;
import static com.shnupbups.extrapieces.EPUtilities.*;

public class EPRenderLayers {
	public static void init() {
		for (PieceSet ps : PieceSets.registry.values()) {
			ps.addRenderLayers();
		}

		debugLog("Added render layers!");
	}
}
