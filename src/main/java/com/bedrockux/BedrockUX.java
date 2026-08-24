package com.bedrockux;

import com.bedrockux.config.BedrockUXConfig;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Estado global do mod: id, logger e a configuracao carregada.
 *
 * <p>A configuracao fica aqui (e nao no inicializador) porque os Mixins precisam
 * ler o estado sem depender da ordem de carregamento dos entrypoints.
 */
public final class BedrockUX {
	public static final String MOD_ID = "bedrockux";
	public static final Logger LOGGER = LoggerFactory.getLogger("Bedrock UX");

	private static volatile BedrockUXConfig config = new BedrockUXConfig();

	private BedrockUX() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static BedrockUXConfig config() {
		return config;
	}

	public static void setConfig(BedrockUXConfig newConfig) {
		config = newConfig;
	}
}
