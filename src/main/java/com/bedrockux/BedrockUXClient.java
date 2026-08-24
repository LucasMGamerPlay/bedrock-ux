package com.bedrockux;

import com.bedrockux.config.BedrockUXConfig;
import com.bedrockux.config.ConfigManager;
import com.bedrockux.hud.CoordinatesHudElement;
import com.bedrockux.hud.PaperDollHudElement;
import com.bedrockux.loading.LoadingTips;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class BedrockUXClient implements ClientModInitializer {
	private static KeyMapping toggleCoordinatesKey;
	private static KeyMapping togglePaperDollKey;
	private static KeyMapping reloadConfigKey;

	@Override
	public void onInitializeClient() {
		BedrockUX.setConfig(ConfigManager.load());
		LoadingTips.load();

		// A ordem importa e e a do Bedrock: o boneco abre a pilha do canto superior esquerdo
		// e reinicia o cursor de layout; as coordenadas se encaixam logo abaixo dele.
		HudElementRegistry.addLast(BedrockUX.id("paper_doll"), new PaperDollHudElement());
		HudElementRegistry.addLast(BedrockUX.id("coordinates"), new CoordinatesHudElement());

		registerKeyMappings();

		BedrockUX.LOGGER.info("Bedrock UX carregado (config em {}).", ConfigManager.path());
	}

	private void registerKeyMappings() {
		KeyMapping.Category category = KeyMapping.Category.register(BedrockUX.id("main"));

		toggleCoordinatesKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.bedrockux.toggle_coordinates", InputConstants.Type.KEYSYM, InputConstants.KEY_F4, category));

		// Sem tecla padrao para nao brigar com os atalhos do vanilla; o jogador liga se quiser.
		togglePaperDollKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.bedrockux.toggle_paper_doll", InputConstants.Type.KEYSYM,
				InputConstants.UNKNOWN.getValue(), category));

		reloadConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.bedrockux.reload_config", InputConstants.Type.KEYSYM,
				InputConstants.UNKNOWN.getValue(), category));

		ClientTickEvents.END_CLIENT_TICK.register(BedrockUXClient::handleKeyPresses);
	}

	private static void handleKeyPresses(Minecraft minecraft) {
		while (toggleCoordinatesKey.consumeClick()) {
			BedrockUXConfig config = BedrockUX.config();
			config.coordinates.enabled = !config.coordinates.enabled;
			ConfigManager.save(config);

			showOverlayMessage(minecraft, config.coordinates.enabled
					? "text.bedrockux.coordinates.enabled"
					: "text.bedrockux.coordinates.disabled");
		}

		while (togglePaperDollKey.consumeClick()) {
			BedrockUXConfig config = BedrockUX.config();
			config.paperDoll.enabled = !config.paperDoll.enabled;
			ConfigManager.save(config);

			showOverlayMessage(minecraft, config.paperDoll.enabled
					? "text.bedrockux.paper_doll.enabled"
					: "text.bedrockux.paper_doll.disabled");
		}

		while (reloadConfigKey.consumeClick()) {
			BedrockUX.setConfig(ConfigManager.load());
		LoadingTips.load();
			showOverlayMessage(minecraft, "text.bedrockux.config_reloaded");
		}
	}

	private static void showOverlayMessage(Minecraft minecraft, String translationKey) {
		minecraft.gui.hud.setOverlayMessage(Component.translatable(translationKey), false);
	}
}
