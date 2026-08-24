package com.bedrockux.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

/**
 * Paleta e primitivas de desenho no estilo Bedrock.
 *
 * <p>O Bedrock desenha os botoes com cores planas e um relevo de 1px em vez das
 * texturas 9-slice do Java, entao tudo aqui e {@code fill} — sem textura, sem atlas.
 */
public final class BedrockTheme {
	// HUD de coordenadas (UI-02).
	public static final int COORDINATES_BACKGROUND = 0x000000;
	public static final int COORDINATES_TEXT = 0xFFFFFFFF;

	// Botoes: corpo.
	private static final int BUTTON_FILL = 0xFF8C8C8C;
	private static final int BUTTON_FILL_HOVERED = 0xFFBFBFBF;
	private static final int BUTTON_FILL_DISABLED = 0xFF5C5C5C;

	// Botoes: borda externa.
	private static final int BUTTON_BORDER = 0xFF2B2B2B;
	private static final int BUTTON_BORDER_HOVERED = 0xFFFFFFFF;
	private static final int BUTTON_BORDER_DISABLED = 0xFF3A3A3A;

	// Botoes: relevo interno (linha clara em cima, escura embaixo).
	private static final int BUTTON_HIGHLIGHT = 0xFFA8A8A8;
	private static final int BUTTON_HIGHLIGHT_HOVERED = 0xFFD6D6D6;
	private static final int BUTTON_HIGHLIGHT_DISABLED = 0xFF6E6E6E;

	private static final int BUTTON_SHADOW = 0xFF6E6E6E;
	private static final int BUTTON_SHADOW_HOVERED = 0xFF9E9E9E;
	private static final int BUTTON_SHADOW_DISABLED = 0xFF4A4A4A;

	private BedrockTheme() {
	}

	/** Combina um RGB com um alfa 0..1. */
	public static int withOpacity(int rgb, float opacity) {
		int alpha = Math.round(Mth.clamp(opacity, 0.0F, 1.0F) * 255.0F);
		return (alpha << 24) | (rgb & 0x00FFFFFF);
	}

	/** Multiplica o alfa ja embutido em um ARGB por um fator 0..1. */
	public static int scaleAlpha(int argb, float factor) {
		int alpha = Math.round(((argb >>> 24) & 0xFF) * Mth.clamp(factor, 0.0F, 1.0F));
		return (alpha << 24) | (argb & 0x00FFFFFF);
	}

	/**
	 * Desenha um botao no estilo Bedrock.
	 *
	 * @param alpha o {@code AbstractWidget.alpha} do vanilla, usado no fade da tela inicial
	 */
	public static void drawButton(GuiGraphicsExtractor context, int x, int y, int width, int height,
			boolean hovered, boolean active, float alpha) {
		if (width <= 0 || height <= 0) {
			return;
		}

		int fill;
		int border;
		int highlight;
		int shadow;

		if (!active) {
			fill = BUTTON_FILL_DISABLED;
			border = BUTTON_BORDER_DISABLED;
			highlight = BUTTON_HIGHLIGHT_DISABLED;
			shadow = BUTTON_SHADOW_DISABLED;
		} else if (hovered) {
			fill = BUTTON_FILL_HOVERED;
			border = BUTTON_BORDER_HOVERED;
			highlight = BUTTON_HIGHLIGHT_HOVERED;
			shadow = BUTTON_SHADOW_HOVERED;
		} else {
			fill = BUTTON_FILL;
			border = BUTTON_BORDER;
			highlight = BUTTON_HIGHLIGHT;
			shadow = BUTTON_SHADOW;
		}

		int right = x + width;
		int bottom = y + height;

		// Borda externa de 1px.
		int borderColor = scaleAlpha(border, alpha);
		context.fill(x, y, right, y + 1, borderColor);
		context.fill(x, bottom - 1, right, bottom, borderColor);
		context.fill(x, y + 1, x + 1, bottom - 1, borderColor);
		context.fill(right - 1, y + 1, right, bottom - 1, borderColor);

		// Corpo.
		context.fill(x + 1, y + 1, right - 1, bottom - 1, scaleAlpha(fill, alpha));

		// Relevo: so cabe se sobrar altura depois da borda.
		if (height >= 4) {
			context.fill(x + 1, y + 1, right - 1, y + 2, scaleAlpha(highlight, alpha));
			context.fill(x + 1, bottom - 2, right - 1, bottom - 1, scaleAlpha(shadow, alpha));
		}
	}
}
