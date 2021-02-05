package com.shnupbups.extrapieces;

import com.shnupbups.extrapieces.register.EPRenderLayers;

import net.fabricmc.api.ClientModInitializer;

public class ExtraPiecesClient implements ClientModInitializer {
	public void onInitializeClient() {
		ExtraPieces.HANDLER.client();
		EPRenderLayers.init();
	}
}
