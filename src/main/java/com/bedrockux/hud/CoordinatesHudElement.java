package com.bedrockux.hud;

import com.bedrockux.BedrockUX;
import com.bedrockux.config.BedrockUXConfig;
import com.bedrockux.ui.BedrockTheme;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * UI-02: caixa de coordenadas do Bedrock no canto superior esquerdo.
 *
 * <p>Nao usa Mixin: a Fabric API expoe {@code HudElementRegistry}, que respeita o
 * F1 e a ordem de camadas da HUD sem tocar em {@code Hud.extractRenderState}.
 */
public final class CoordinatesHudElement implements HudElement {
	private static final int PADDING = 3;
	private static final int LINE_SPACING = 1;

	/** Reaproveitada a cada frame para nao alocar lista nova 60x por segundo. */
	private final List<String> lines = new ArrayList<>(3);

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, DeltaTracker deltaTracker) {
		BedrockUXConfig.Coordinates config = BedrockUX.config().coordinates;

		if (!config.enabled) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;

		if (player == null || minecraft.level == null || minecraft.gui.hud.isHidden()) {
			return;
		}

		if (config.hideWithDebugScreen && minecraft.gui.hud.getDebugOverlay().showDebugScreen()) {
			return;
		}

		BlockPos pos = player.blockPosition();

		lines.clear();
		lines.add(label("position") + ": " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());

		if (config.showFacing) {
			lines.add(label("facing") + ": " + facingName(player.getDirection()));
		}

		if (config.showBiome) {
			minecraft.level.getBiome(pos).unwrapKey()
					.ifPresent(key -> lines.add(label("biome") + ": " + prettify(key.identifier().getPath())));
		}

		Font font = minecraft.font;
		int textWidth = 0;

		for (String line : lines) {
			textWidth = Math.max(textWidth, font.width(line));
		}

		int boxWidth = textWidth + PADDING * 2;
		int boxHeight = lines.size() * font.lineHeight + (lines.size() - 1) * LINE_SPACING + PADDING * 2;
		int x = config.offsetX;
		// A Paper Doll roda antes e deixa o cursor no proximo Y livre; quando ela esta
		// desligada, o cursor e o topo da pilha, entao a caixa sobe sozinha.
		int y = config.belowPaperDoll ? HudLayout.topLeft() : config.offsetY;

		context.fill(x, y, x + boxWidth, y + boxHeight,
				BedrockTheme.withOpacity(BedrockTheme.COORDINATES_BACKGROUND, config.backgroundOpacity));

		int lineY = y + PADDING;

		for (String line : lines) {
			context.text(font, line, x + PADDING, lineY, BedrockTheme.COORDINATES_TEXT, config.textShadow);
			lineY += font.lineHeight + LINE_SPACING;
		}

		HudLayout.advanceTopLeft(boxHeight);

	}

	private static String label(String key) {
		return Component.translatable("text.bedrockux.coordinates." + key).getString();
	}

	private static String facingName(Direction direction) {
		return switch (direction) {
			case NORTH -> Component.translatable("text.bedrockux.direction.north").getString();
			case SOUTH -> Component.translatable("text.bedrockux.direction.south").getString();
			case EAST -> Component.translatable("text.bedrockux.direction.east").getString();
			case WEST -> Component.translatable("text.bedrockux.direction.west").getString();
			default -> prettify(direction.getSerializedName());
		};
	}

	/** {@code old_growth_taiga} -> {@code Old Growth Taiga}. */
	private static String prettify(String raw) {
		String[] parts = raw.split("_");
		StringBuilder builder = new StringBuilder(raw.length());

		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}

			if (!builder.isEmpty()) {
				builder.append(' ');
			}

			builder.append(Character.toUpperCase(part.charAt(0))).append(part, 1, part.length());
		}

		return builder.toString();
	}
}
