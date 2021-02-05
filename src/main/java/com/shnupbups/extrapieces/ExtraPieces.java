package com.shnupbups.extrapieces;

import com.shnupbups.extrapieces.api.EPInitializer;
import com.shnupbups.extrapieces.core.PieceSet;
import com.shnupbups.extrapieces.core.PieceSets;
import com.shnupbups.extrapieces.core.PieceTypes;
import com.shnupbups.extrapieces.debug.DebugItem;
import com.shnupbups.extrapieces.register.EPBlocks;
import com.shnupbups.extrapieces.register.EPConfig;
import com.shnupbups.extrapieces.resource.ResourceHandler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.util.registry.Registry;

import java.io.File;

import static com.shnupbups.extrapieces.EPUtilities.*;

public class ExtraPieces implements ModInitializer {
	public static final String MOD_ID = "extrapieces";
	public static final String MOD_NAME = "Extra Pieces";
	public static final String PIECE_PACK_VERSION = "2.9.0";
	public static final ResourceHandler HANDLER = new ResourceHandler();
	public static File configDir;
	public static File ppDir;

	@Override
	public void onInitialize() {
		EPConfig.init();
		PieceTypes.init();
		FabricLoader.getInstance().getEntrypoints("extrapieces", EPInitializer.class).forEach(api -> {
			debugLog("EPInitializer " + api.toString());
			api.onInitialize();
		});
		EPConfig.initPiecePacks();
		HANDLER.server();
		Registry.register(Registry.ITEM, getID("debug_item"), new DebugItem());

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (EPBlocks.setBuilders.size() != PieceSets.registry.size()) {
				for (PieceSet.Builder psb : EPBlocks.setBuilders.values()) {
					if (!psb.isBuilt())
						logger.warn("Piece Set " + psb + " could not be built, make sure the base and any vanilla pieces actually exist!");
				}
			}
		});
	}
}
