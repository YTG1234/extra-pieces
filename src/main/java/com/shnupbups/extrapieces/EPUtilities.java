package com.shnupbups.extrapieces;

import java.io.File;

import com.shnupbups.extrapieces.register.EPConfig;
import com.swordglowsblue.artifice.api.ArtificeResourcePack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

public final class EPUtilities {
	public static final Logger logger = LogManager.getFormatterLogger(ExtraPieces.MOD_NAME);

	private EPUtilities() {
	}

	public static void moreDebugLog(String out) {
		if (EPConfig.moreDebugOutput) debugLog(out);
	}

	public static void debugLog(String out) {
		if (EPConfig.debugOutput) log("[" + ExtraPieces.MOD_NAME + "/DEBUG] " + out);
	}

	public static void log(String out) {
		logger.info("[" + ExtraPieces.MOD_NAME + "] " + out);
	}

	public static void warn(String out) {
		logger.warn("[" + ExtraPieces.MOD_NAME + "] " + out);
	}

	public static Identifier getID(String path) {
		return new Identifier(ExtraPieces.MOD_ID, path);
	}

	public static Identifier prependToPath(Identifier id, String prep) {
		return new Identifier(id.getNamespace(), prep + id.getPath());
	}

	public static Identifier appendToPath(Identifier id, String app) {
		return new Identifier(id.getNamespace(), id.getPath() + app);
	}

	public static File getPiecePackDirectory() {
		if (ExtraPieces.ppDir == null) {
			ExtraPieces.ppDir = new File(getConfigDirectory(), "piecepacks");
			ExtraPieces.ppDir.mkdirs();
		}
		return ExtraPieces.ppDir;
	}

	public static File getConfigDirectory() {
		if (ExtraPieces.configDir == null) {
			ExtraPieces.configDir = FabricLoader.getInstance().getConfigDir().resolve(ExtraPieces.MOD_ID).toFile();
			ExtraPieces.configDir.mkdirs();
		}
		return ExtraPieces.configDir;
	}

	public static boolean isWoodmillInstalled() {
		return FabricLoader.getInstance().isModLoaded("woodmill");
	}

	public static void dump(ArtificeResourcePack.ServerResourcePackBuilder pack) {
		if (EPConfig.dumpData) {
			try {
				pack.dumpResources(
						FabricLoader.getInstance().getGameDir().resolve("dump").toAbsolutePath().toString(),
						ResourceType.SERVER_DATA.getDirectory()
				);
			} catch (Exception e) {
				debugLog("BIG OOF: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static void dump(ArtificeResourcePack.ClientResourcePackBuilder pack) {
		if (EPConfig.dumpAssets) {
			try {
				pack.dumpResources(FabricLoader.getInstance().getGameDir().resolve("dump").toAbsolutePath().toString(),
						ResourceType.CLIENT_RESOURCES.getDirectory());
			} catch (Exception e) {
				debugLog("BIG OOF: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
