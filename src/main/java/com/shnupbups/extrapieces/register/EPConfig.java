package com.shnupbups.extrapieces.register;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;
import com.shnupbups.extrapieces.ExtraPieces;
import com.shnupbups.extrapieces.core.PieceSet;
import com.shnupbups.extrapieces.core.PieceSets;
import static com.shnupbups.extrapieces.EPUtilities.*;

import net.minecraft.util.Identifier;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;

public class EPConfig {
	public static boolean generateDefaultPack = true;
	public static boolean forceUpdateDefaultPack = false;
	public static boolean everythingStonecuttable = false;
	public static boolean debugOutput = false;
	public static boolean moreDebugOutput = false;
	public static boolean dumpData = false;
	public static boolean dumpAssets = false;

	public static boolean slabs = true;
	public static boolean stairs = true;
	public static boolean sidings = true;
	public static boolean corners = true;
	public static boolean walls = true;
	public static boolean fences = true;
	public static boolean fenceGates = true;
	public static boolean columns = true;
	public static boolean posts = true;
	public static boolean layers = true;

	private static int setsNum = 0;
	private static int ppSetsNum = 0;
	private static int ppVpNum = 0;

	public static void init() {
		File config = new File(getConfigDirectory(), "config.json");
		try (FileReader reader = new FileReader(config)) {
			log("Loading config");
			JsonObject cfg = Jankson.builder().build().load(config);
			if (isConfigOutdated(cfg)) {
				updateConfig(cfg, config);
			}

			generateDefaultPack = cfg.get("generateDefaultPack").equals(JsonPrimitive.TRUE);
			forceUpdateDefaultPack = cfg.get("forceUpdateDefaultPack").equals(JsonPrimitive.TRUE);
			everythingStonecuttable = cfg.get("everythingStonecuttable").equals(JsonPrimitive.TRUE);
			debugOutput = cfg.get("debugOutput").equals(JsonPrimitive.TRUE);
			moreDebugOutput = cfg.get("moreDebugOutput").equals(JsonPrimitive.TRUE);
			dumpData = cfg.get("dumpData").equals(JsonPrimitive.TRUE);
			dumpAssets = cfg.get("dumpAssets").equals(JsonPrimitive.TRUE);

			JsonObject types = cfg.getObject("pieceTypes");
			slabs = types.get("slabs").equals(JsonPrimitive.TRUE);
			stairs = types.get("stairs").equals(JsonPrimitive.TRUE);
			sidings = types.get("sidings").equals(JsonPrimitive.TRUE);
			corners = types.get("corners").equals(JsonPrimitive.TRUE);
			walls = types.get("walls").equals(JsonPrimitive.TRUE);
			fences = types.get("fences").equals(JsonPrimitive.TRUE);
			fenceGates = types.get("fenceGates").equals(JsonPrimitive.TRUE);
			columns = types.get("columns").equals(JsonPrimitive.TRUE);
			posts = types.get("posts").equals(JsonPrimitive.TRUE);
			layers = types.get("layers").equals(JsonPrimitive.TRUE);
		} catch (IOException e) {
			generateConfig(config);
		} catch (SyntaxError e) {
			log("SyntaxError loading config");
			log(e.getMessage());
		}
		findAndCopyPiecePacks();
	}

	public static boolean isConfigOutdated(JsonObject cfg) {
		if (!cfg.containsKey("generateDefaultPack") ||
			!cfg.containsKey("forceUpdateDefaultPack") ||
			!cfg.containsKey("everythingStonecuttable") ||
			!cfg.containsKey("debugOutput") ||
			!cfg.containsKey("moreDebugOutput") ||
			!cfg.containsKey("dumpData") ||
			!cfg.containsKey("pieceTypes") ||
			!cfg.containsKey("dumpAssets")) {
			return true;
		} else {
			JsonObject types = cfg.getObject("pieceTypes");
			return !types.containsKey("slabs") ||
				   !types.containsKey("stairs") ||
				   !types.containsKey("sidings") ||
				   !types.containsKey("corners") ||
				   !types.containsKey("walls") ||
				   !types.containsKey("fences") ||
				   !types.containsKey("fenceGates") ||
				   !types.containsKey("columns") ||
				   !types.containsKey("posts") ||
				   !types.containsKey("layers");
		}
	}

	public static void updateConfig(JsonObject cfg, File config) {
		if (!cfg.containsKey("generateDefaultPack")) cfg.put("generateDefaultPack", new JsonPrimitive(generateDefaultPack));
		if (!cfg.containsKey("forceUpdateDefaultPack")) cfg.put("forceUpdateDefaultPack", new JsonPrimitive(forceUpdateDefaultPack));
		if (!cfg.containsKey("everythingStonecuttable")) cfg.put("everythingStonecuttable", new JsonPrimitive(everythingStonecuttable));
		if (!cfg.containsKey("debugOutput")) cfg.put("debugOutput", new JsonPrimitive(debugOutput));
		if (!cfg.containsKey("moreDebugOutput")) cfg.put("moreDebugOutput", new JsonPrimitive(moreDebugOutput));
		if (!cfg.containsKey("dumpData")) cfg.put("dumpData", new JsonPrimitive(dumpData));
		if (!cfg.containsKey("dumpAssets")) cfg.put("dumpAssets", new JsonPrimitive(dumpAssets));
		if (!cfg.containsKey("pieceTypes")) cfg.put("pieceTypes", new JsonObject());
		JsonObject types = cfg.getObject("pieceTypes");
		if (!types.containsKey("slabs")) types.put("slabs", new JsonPrimitive(slabs));
		if (!types.containsKey("stairs")) types.put("stairs", new JsonPrimitive(stairs));
		if (!types.containsKey("sidings")) types.put("sidings", new JsonPrimitive(sidings));
		if (!types.containsKey("corners")) types.put("corners", new JsonPrimitive(corners));
		if (!types.containsKey("walls")) types.put("walls", new JsonPrimitive(walls));
		if (!types.containsKey("fences")) types.put("fences", new JsonPrimitive(fences));
		if (!types.containsKey("fenceGates")) types.put("fenceGates", new JsonPrimitive(fenceGates));
		if (!types.containsKey("columns")) types.put("columns", new JsonPrimitive(columns));
		if (!types.containsKey("posts")) types.put("posts", new JsonPrimitive(posts));
		if (!types.containsKey("layers")) types.put("layers", new JsonPrimitive(layers));
		cfg.put("pieceTypes", types);
		if (config.exists()) config.delete();
		try (FileWriter writer = new FileWriter(config)) {
			writer.write(cfg.toJson(false, true));
		} catch (IOException e) {
			log("Failed to write to config!");
			log(e.getMessage());
		}
	}

	public static void generateConfig(File config) {
		updateConfig(new JsonObject(), config);
	}

	public static void findAndCopyPiecePacks() {
		FabricLoader.getInstance().getAllMods().stream().map(modContainer -> {
			Path path = null;

			if (modContainer.getMetadata().containsCustomValue(ExtraPieces.MOD_ID + ":piecepack")) {
				debugLog("Found Piece Pack in " + modContainer.getMetadata().getName() + " (" + modContainer.getMetadata().getId() + ")");
				CustomValue pp = modContainer.getMetadata().getCustomValue(ExtraPieces.MOD_ID + ":piecepack");
				path = modContainer.getPath(pp.getAsString());
			}

			return path;
		}).forEach(path -> {
			if (path == null) {
				return;
			}

			if (Files.exists(path)) {
				if (path.toString().endsWith(".json")) {
					Path destination = getPiecePackDirectory().toPath().resolve(path.getFileName().toString());

					if (Files.notExists(destination)) {
						try {
							Files.createDirectories(destination.getParent());
							Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);

							debugLog("Successfully copied PiecePack " + path.getFileName() + " from a mod jar!");
						} catch (IOException e) {
							debugLog("IOException copying PiecePack " + path.getFileName() + " from a mod jar!");
						}
					} else if (isNewer(path, destination)) {
						try {
							Files.createDirectories(destination.getParent());
							Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);

							debugLog("Successfully updated PiecePack " + path.getFileName() + " from a mod jar!");
						} catch (IOException e) {
							debugLog("IOException updating PiecePack " + path.getFileName() + " from a mod jar!");
						}
					} else {
						debugLog("Piece Pack " + path.getFileName() + " already present.");
					}
				} else {
					debugLog("A mod specified a Piece Pack named " +
							 path.getFileName() +
							 ", but it is not a .json file! (You must include the '.json' in the name!)");
				}
			} else {
				debugLog("A mod specified a Piece Pack named " + path.getFileName() + ", but no such file existed in its jar!");
			}
		});
	}

	public static boolean isNewer(Path toCheck, Path toReplace) {
		try {
			Path tempDir = Files.createTempDirectory("piecepacks");
			File toCheckFile = Files.copy(toCheck, tempDir.resolve(toCheck.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING).toFile();
			File toReplaceFile = toReplace.toFile();
			JsonObject ppC = Jankson.builder().build().load(toCheckFile);
			JsonObject ppR = Jankson.builder().build().load(toReplaceFile);
			Version cVer;
			Version rVer;
			if (ppC.containsKey("version")) {
				cVer = new Version(ppC.get(String.class, "version"));
			} else {
				cVer = new Version();
			}
			if (ppR.containsKey("version")) {
				rVer = new Version(ppR.get(String.class, "version"));
			} else {
				rVer = new Version();
			}
			return cVer.isNewerThan(rVer);
		} catch (Exception e) {
			log("Failed to check if Piece Pack " + toCheck.getFileName() + " needed updating:");
			e.printStackTrace();

			return false;
		}
	}

	public static void initPiecePacks() {
		File ppDir = getPiecePackDirectory();
		File defaultPack = new File(ppDir, "default.json");
		File[] packs = ppDir.listFiles();
		assert packs != null;
		if (packs.length == 0) {
			if (generateDefaultPack) {
				log("No piece packs found, generating default!");
				generateDefaultPack(defaultPack);
				ArrayList<File> packslist = new ArrayList<>(Arrays.asList(packs));
				packslist.add(defaultPack);
				packs = packslist.toArray(packs);
			} else {
				log("No piece packs found! Why bother having Extra Pieces installed then?");
			}
		} else {
			if (generateDefaultPack && (!defaultPack.exists() || forceUpdateDefaultPack || isDefaultPackOutdated(defaultPack))) {
				log("Generating default piece pack as it either did not exist or needed updating...");
				generateDefaultPack(defaultPack);
				ArrayList<File> packslist = new ArrayList<>(Arrays.asList(packs));
				packslist.add(defaultPack);
				packs = packslist.toArray(packs);
			}
		}
		for (File f : packs) {
			try (FileReader reader = new FileReader(f)) {
				JsonObject pp = Jankson.builder().build().load(f);
				JsonObject sets = null;
				JsonObject vanillaPieces = null;
				String ppVer;
				if (pp.containsKey("required_mod")) {
					String requiredMod = pp.get(String.class, "required_mod");
					if (!FabricLoader.getInstance().isModLoaded(requiredMod)) {
						continue;
					}
				}
				if (!pp.containsKey("version")) {
					sets = pp;
					ppVer = "0.0.0";
					debugLog("Piece pack " + f.getName() + " doesn't specify a version! Please update it! Defaulting to 0.0.0");
				} else {
					if (pp.containsKey("sets")) sets = pp.getObject("sets");
					if (pp.containsKey("vanilla_pieces")) vanillaPieces = pp.getObject("vanilla_pieces");
					ppVer = pp.get(String.class, "version");
				}
				debugLog("Loading piece pack " + f.getName() + " version " + ppVer);
				if (sets != null) {
					for (Map.Entry<String, JsonElement> entry : sets.entrySet()) {
						JsonObject jsonSet = (JsonObject) entry.getValue();
						PieceSet.Builder psb = new PieceSet.Builder(entry.getKey(), jsonSet, f.getName());
						if (psb.shouldLoad()) {
							setsNum++;
							ppSetsNum++;
							EPBlocks.registerSet(psb);
						}
					}
					debugLog("Generated " + ppSetsNum + " PieceSets from piece pack " + f.getName());
				}
				if (vanillaPieces != null) {
					for (Map.Entry<String, JsonElement> entry : vanillaPieces.entrySet()) {
						JsonObject jsonPiece = (JsonObject) entry.getValue();
						if (jsonPiece.containsKey("base") && jsonPiece.containsKey("type") && jsonPiece.containsKey("piece")) {
							Identifier base = new Identifier(jsonPiece.get(String.class, "base"));
							Identifier type = new Identifier(jsonPiece.get(String.class, "type"));
							Identifier piece = new Identifier(jsonPiece.get(String.class, "piece"));
							boolean add = true;
							if (jsonPiece.containsKey("required_mod")) {
								add = FabricLoader.getInstance().isModLoaded(jsonPiece.get(String.class, "required_mod"));
							}
							if (add) {
								ppVpNum++;
								EPBlocks.registerVanillaPiece(base, type, piece);
							}
						} else {
							debugLog("Invalid vanilla piece " + entry.getKey() + " in piece pack " + f.getName());
						}
					}
					debugLog("Added " + ppVpNum + " vanilla pieces from piece pack " + f.getName());
				}

			} catch (IOException e) {
				debugLog("IOException loading piece pack " + f.getName());
				debugLog(e.getMessage());
			} catch (SyntaxError e) {
				debugLog("SyntaxError loading piece pack " + f.getName());
				debugLog(e.getCompleteMessage());
			}
			ppSetsNum = 0;
			ppVpNum = 0;
		}
		debugLog("Generated " + setsNum + " PieceSets!");
	}

	public static void generateDefaultPack(File pack) {
		if (pack.exists()) pack.delete();
		EPBlocks.generateDefaultSets();
		try (FileWriter writer = new FileWriter(pack)) {
			JsonObject pp = new JsonObject();
			pp.put("version", new JsonPrimitive(ExtraPieces.PIECE_PACK_VERSION));
			JsonObject sets = new JsonObject();
			for (PieceSet set : PieceSets.defaults.values()) {
				sets.put(set.getName(), set.toJson());
			}
			pp.put("sets", sets);
			writer.write(pp.toJson(false, true));
		} catch (IOException e) {
			log("Failed to write to default piece pack!");
			log(e.getMessage());
		}
	}

	public static boolean isDefaultPackOutdated(File defaultPack) {
		try {
			JsonObject dpp = Jankson.builder().build().load(defaultPack);
			Version ver;
			if (dpp.containsKey("version")) {
				ver = new Version(dpp.get(String.class, "version"));
			} else {
				ver = new Version();
			}
			return new Version(ExtraPieces.PIECE_PACK_VERSION).isNewerThan(ver);
		} catch (Exception e) {
			log("Failed to check if default Piece Pack needed updating:");
			e.printStackTrace();
			return false;
		}
	}

	public static class Version implements Comparable<Version> {
		private final String version;

		public Version() {
			this("0.0.0");
		}

		public Version(String version) {
			if (version == null) throw new IllegalArgumentException("Version can not be null");
			if (!version.matches("[0-9]+(\\.[0-9]+)*")) throw new IllegalArgumentException("Invalid version format");
			this.version = version;
		}

		public boolean isOlderThan(Version that) {
			return compareTo(that) == -1;
		}

		@Override
		public int compareTo(Version that) {
			if (that == null) return 1;
			String[] thisParts = this.get().split("\\.");
			String[] thatParts = that.get().split("\\.");
			int length = Math.max(thisParts.length, thatParts.length);
			for (int i = 0; i < length; i++) {
				int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
				int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;
				if (thisPart < thatPart) return -1;
				if (thisPart > thatPart) return 1;
			}
			return 0;
		}

		public final String get() {
			return this.version;
		}

		public boolean isNewerThan(Version that) {
			return compareTo(that) == 1;
		}

		@Override
		public boolean equals(Object that) {
			if (this == that) return true;
			if (that == null) return false;
			if (this.getClass() != that.getClass()) return false;
			return this.compareTo((Version) that) == 0;
		}

		@Override
		public String toString() {
			return version;
		}
	}
}
