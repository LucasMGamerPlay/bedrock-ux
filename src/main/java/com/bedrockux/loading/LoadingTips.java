package com.bedrockux.loading;

import com.bedrockux.BedrockUX;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Dicas de jogo mostradas durante o carregamento, como no Bedrock.
 *
 * <p>O arquivo guarda <em>chaves de traducao</em>, nao texto pronto: assim as dicas
 * acompanham o idioma do jogo em vez de ficarem presas a um so. Quem quiser dicas proprias
 * pode escrever texto literal — o que nao existir no idioma aparece como esta.
 *
 * <p>A lista padrao vem do jar; se existir {@code config/bedrockux-tips.json}, ela e usada
 * no lugar.
 */
public final class LoadingTips {
	private static final Gson GSON = new Gson();
	private static final String BUNDLED_PATH = "/assets/" + BedrockUX.MOD_ID + "/loading_tips.json";

	private static List<String> keys = List.of();
	private static String currentKey;

	private LoadingTips() {
	}

	public static Path overridePath() {
		return FabricLoader.getInstance().getConfigDir().resolve(BedrockUX.MOD_ID + "-tips.json");
	}

	public static void load() {
		Path override = overridePath();

		if (Files.exists(override)) {
			try (Reader reader = Files.newBufferedReader(override, StandardCharsets.UTF_8)) {
				keys = readList(reader);
				BedrockUX.LOGGER.info("Dicas de carregamento vindas de {}.", override);
				return;
			} catch (IOException | JsonSyntaxException e) {
				BedrockUX.LOGGER.error("Falha ao ler {}, usando as dicas padrao.", override, e);
			}
		}

		try (InputStream stream = LoadingTips.class.getResourceAsStream(BUNDLED_PATH)) {
			if (stream == null) {
				BedrockUX.LOGGER.error("Recurso {} nao encontrado no jar.", BUNDLED_PATH);
				keys = List.of();
				return;
			}

			keys = readList(new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (IOException | JsonSyntaxException e) {
			BedrockUX.LOGGER.error("Falha ao ler {}.", BUNDLED_PATH, e);
			keys = List.of();
		}
	}

	@SuppressWarnings("unchecked")
	private static List<String> readList(Reader reader) {
		List<String> parsed = GSON.fromJson(reader, List.class);
		return parsed == null ? List.of() : List.copyOf(parsed);
	}

	/** Sorteia uma dica nova. Chamado ao abrir a tela, nao a cada frame. */
	public static void pickRandom() {
		if (keys.isEmpty()) {
			currentKey = null;
			return;
		}

		currentKey = keys.get(RandomGenerator.getDefault().nextInt(keys.size()));
	}

	/** Dica atual ja traduzida, ou {@code null} se nao ha nenhuma. */
	public static Component current() {
		return currentKey == null ? null : Component.translatable(currentKey);
	}
}
