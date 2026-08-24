package com.bedrockux.config;

import com.bedrockux.BedrockUX;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Le e grava {@code config/bedrockux.json}. */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ConfigManager() {
	}

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(BedrockUX.MOD_ID + ".json");
	}

	/** Carrega do disco; grava o arquivo padrao se ele nao existir ou estiver corrompido. */
	public static BedrockUXConfig load() {
		Path path = path();

		if (!Files.exists(path)) {
			BedrockUXConfig fresh = new BedrockUXConfig();
			save(fresh);
			return fresh;
		}

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			BedrockUXConfig loaded = GSON.fromJson(reader, BedrockUXConfig.class);

			if (loaded == null) {
				loaded = new BedrockUXConfig();
			}

			loaded.sanitize();
			return loaded;
		} catch (IOException | JsonSyntaxException e) {
			BedrockUX.LOGGER.error("Falha ao ler {}, usando os valores padrao.", path, e);
			return new BedrockUXConfig();
		}
	}

	public static void save(BedrockUXConfig config) {
		config.sanitize();
		Path path = path();

		try {
			Files.createDirectories(path.getParent());

			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			BedrockUX.LOGGER.error("Falha ao gravar {}.", path, e);
		}
	}
}
