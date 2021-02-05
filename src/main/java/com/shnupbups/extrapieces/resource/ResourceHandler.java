package com.shnupbups.extrapieces.resource;

import com.shnupbups.extrapieces.EPUtilities;
import com.shnupbups.extrapieces.register.EPBlocks;
import com.shnupbups.extrapieces.register.EPModels;
import com.swordglowsblue.artifice.api.Artifice;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public final class ResourceHandler {
	public void server() {
		Artifice.registerDataPack(EPUtilities.getID("ep_data"), pack -> {
			EPBlocks.init(pack);
			pack.setDescription("Various data that is necessary in order for Extra Pieces to function.");
		});
	}

	@Environment(EnvType.CLIENT)
	public void client() {
		Artifice.registerAssetPack(EPUtilities.getID("ep_assets"), pack -> {
			EPModels.init(pack);
			pack.setDescription("Assets necessary for Extra Pieces.");
		});
	}
}
