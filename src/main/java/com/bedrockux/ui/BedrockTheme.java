package com.bedrockux.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

/**
 * Paleta e primitivas de desenho no estilo Bedrock.
 *
 * <p>O Bedrock desenha os botoes com cores planas e um relevo de 1px em vez das texturas
 * 9-slice do Java, entao tudo aqui e {@code fill} — sem textura, sem atlas.
 *
 * <p>As cores foram amostradas pixel a pixel das capturas em
 * {@code Fotos de Menus direto do Minecraft Bedrock Como exemplo/}, nao estimadas. A
 * anatomia de um botao, de cima para baixo: borda externa, linha de brilho, corpo, linha
 * de sombra, borda externa.
 */
public final class BedrockTheme {
	// HUD de coordenadas (UI-02).
	public static final int COORDINATES_BACKGROUND = 0x000000;
	public static final int COORDINATES_TEXT = 0xFFFFFFFF;

	// Painel de carregamento / salvamento (UI-07), amostrado das capturas do Bedrock.
	public static final int PANEL_BODY = 0xFFC6C6C6;
	public static final int PANEL_BORDER = 0xFF000000;
	public static final int PANEL_HIGHLIGHT = 0xFFFFFFFF;
	public static final int PANEL_SHADOW = 0xFF555555;
	public static final int PANEL_TITLE_TEXT = 0xFF4C4C4C;
	public static final int PANEL_INNER_BORDER = 0xFF393939;
	/** A caixa interna e translucida: o mundo aparece atras dela. */
	public static final int PANEL_INNER_FILL = 0xCC0A0909;
	public static final int PANEL_INNER_TEXT = 0xFFFFFFFF;

	// Slots de inventario, amostrados de inventario sobrevivencia (1).jpg.
	private static final int SLOT_FILL = 0xFF8B8B8B;
	private static final int SLOT_SHADOW = 0xFF363636;
	private static final int SLOT_HIGHLIGHT = 0xFFFFFFFF;

	/** Caixa escura atras do modelo do jogador no inventario. */
	public static final int MODEL_BOX = 0xFF000000;

	// Barra de progresso: segmentos verdes, nao um preenchimento continuo.
	private static final int BAR_SEGMENT_FILLED = 0xFF96D464;
	private static final int BAR_SEGMENT_FILLED_TOP = 0xFFAEEE7A;
	private static final int BAR_SEGMENT_EMPTY = 0xFF416032;

	/** Conjunto de cores de uma variante de botao. */
	public record ButtonVariant(int fill, int border, int highlight, int shadow, int text) {
		/**
		 * Estado de mouse em cima: o Bedrock destaca com a borda clara e o corpo um pouco
		 * mais luminoso, mantendo a cor da variante.
		 */
		ButtonVariant hovered() {
			return new ButtonVariant(lighten(fill, 0.18F), 0xFFFFFFFF, lighten(highlight, 0.18F),
					lighten(shadow, 0.18F), text);
		}
	}

	/** Botao comum, cinza-claro com texto escuro. */
	public static final ButtonVariant SECONDARY =
			new ButtonVariant(0xFFC6C6C6, 0xFF131313, 0xFFF7F7F7, 0xFF656465, 0xFF4C4C4C);

	/** Acao primaria, verde (ex.: "Criar novo mundo"). */
	public static final ButtonVariant PRIMARY =
			new ButtonVariant(0xFF3C8527, 0xFF1E1E1F, 0xFF639D52, 0xFF1D4D13, 0xFFFFFFFF);

	/** Destaque roxo do Realms. */
	public static final ButtonVariant ACCENT =
			new ButtonVariant(0xFF7345E5, 0xFF131313, 0xFFA164F2, 0xFF4A1CAC, 0xFFFFFFFF);

	/** Botao inativo: dessaturado, com texto apagado. */
	public static final ButtonVariant DISABLED =
			new ButtonVariant(0xFF8C8C8C, 0xFF3A3A3A, 0xFF9E9E9E, 0xFF6B6B6B, 0xFF6E6E6E);

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

	/** Clareia um ARGB em direcao ao branco, preservando o alfa. */
	private static int lighten(int argb, float amount) {
		int alpha = argb >>> 24;
		int red = channelTowardsWhite(argb >> 16, amount);
		int green = channelTowardsWhite(argb >> 8, amount);
		int blue = channelTowardsWhite(argb, amount);
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	private static int channelTowardsWhite(int channel, float amount) {
		int value = channel & 0xFF;
		return Math.round(value + (255 - value) * Mth.clamp(amount, 0.0F, 1.0F));
	}

	/** Escolhe a variante conforme o estado do botao. */
	public static ButtonVariant variantFor(ButtonVariant base, boolean hovered, boolean active) {
		if (!active) {
			return DISABLED;
		}

		return hovered ? base.hovered() : base;
	}

	/**
	 * Desenha o fundo de um botao no estilo Bedrock.
	 *
	 * @param alpha o {@code AbstractWidget.alpha} do vanilla, usado no fade da tela inicial
	 */
	public static void drawButton(GuiGraphicsExtractor context, int x, int y, int width, int height,
			ButtonVariant variant, float alpha) {
		if (width <= 0 || height <= 0) {
			return;
		}

		int right = x + width;
		int bottom = y + height;

		// Borda externa de 1px.
		int borderColor = scaleAlpha(variant.border(), alpha);
		context.fill(x, y, right, y + 1, borderColor);
		context.fill(x, bottom - 1, right, bottom, borderColor);
		context.fill(x, y + 1, x + 1, bottom - 1, borderColor);
		context.fill(right - 1, y + 1, right, bottom - 1, borderColor);

		// Corpo.
		context.fill(x + 1, y + 1, right - 1, bottom - 1, scaleAlpha(variant.fill(), alpha));

		// Relevo: so cabe se sobrar altura depois da borda.
		if (height >= 4) {
			context.fill(x + 1, y + 1, right - 1, y + 2, scaleAlpha(variant.highlight(), alpha));
			context.fill(x + 1, bottom - 2, right - 1, bottom - 1, scaleAlpha(variant.shadow(), alpha));
		}
	}

	/**
	 * Painel do Bedrock: borda escura, realce branco, corpo cinza e sombra embaixo.
	 */
	public static void drawPanel(GuiGraphicsExtractor context, int x, int y, int width, int height) {
		int right = x + width;
		int bottom = y + height;

		context.fill(x, y, right, bottom, PANEL_BORDER);
		context.fill(x + 1, y + 1, right - 1, bottom - 1, PANEL_HIGHLIGHT);
		context.fill(x + 1, y + 3, right - 1, bottom - 2, PANEL_BODY);
		context.fill(x + 1, bottom - 2, right - 1, bottom - 1, PANEL_SHADOW);
	}

	/** Caixa interna translucida onde ficam o texto e a barra. */
	public static void drawInnerBox(GuiGraphicsExtractor context, int x, int y, int width, int height) {
		int right = x + width;
		int bottom = y + height;

		context.fill(x, y, right, bottom, PANEL_INNER_BORDER);
		context.fill(x + 1, y + 1, right - 1, bottom - 1, PANEL_INNER_FILL);
	}

	/**
	 * Barra de progresso segmentada do Bedrock. Os segmentos vazios ficam verde-escuro em
	 * vez de sumirem, que e o que da o aspecto de trilho tracejado do original.
	 */
	public static void drawProgressBar(GuiGraphicsExtractor context, int x, int y, int width, int height,
			float progress) {
		if (width <= 0 || height <= 0) {
			return;
		}

		int segmentWidth = 6;
		int gap = 2;
		int stride = segmentWidth + gap;
		int segments = Math.max(1, (width + gap) / stride);
		int filledSegments = Math.round(segments * Mth.clamp(progress, 0.0F, 1.0F));

		for (int index = 0; index < segments; index++) {
			int segmentX = x + index * stride;
			int segmentRight = Math.min(segmentX + segmentWidth, x + width);

			if (index < filledSegments) {
				context.fill(segmentX, y, segmentRight, y + height, BAR_SEGMENT_FILLED);
				context.fill(segmentX, y, segmentRight, y + 1, BAR_SEGMENT_FILLED_TOP);
			} else {
				context.fill(segmentX, y, segmentRight, y + height, BAR_SEGMENT_EMPTY);
			}
		}
	}

	/**
	 * Slot no estilo Bedrock: rebaixado, com sombra em cima e a esquerda e realce embaixo e
	 * a direita. Os slots ficam encostados, entao o realce de um toca a sombra do vizinho —
	 * e o que da o aspecto de grade gravada do original.
	 */
	private static final int ARROW_FILL = 0xFFA0A0A0;
	private static final int ARROW_SHADOW = 0xFF7C7C7C;

	/**
	 * Seta entre a grade de fabricacao e o resultado.
	 *
	 * <p>No vanilla ela faz parte da textura do painel. Como o painel virou desenho proprio,
	 * a seta precisa ser redesenhada ou some junto.
	 */
	public static void drawCraftArrow(GuiGraphicsExtractor context, int x, int y, int length, int thickness) {
		int shaftLength = Math.max(1, length - thickness);
		int shaftTop = y - thickness / 2;

		context.fill(x, shaftTop + 1, x + shaftLength, shaftTop + thickness + 1, ARROW_SHADOW);
		context.fill(x, shaftTop, x + shaftLength, shaftTop + thickness, ARROW_FILL);

		// Ponta: uma linha por passo, estreitando ate o bico.
		int head = thickness + thickness / 2;
		for (int step = 0; step < head; step++) {
			int half = head - step;
			int lineX = x + shaftLength + step;
			context.fill(lineX, y - half + 1, lineX + 1, y + half + 1, ARROW_SHADOW);
			context.fill(lineX, y - half, lineX + 1, y + half, ARROW_FILL);
		}
	}

	public static void drawSlot(GuiGraphicsExtractor context, int x, int y, int size) {
		int right = x + size;
		int bottom = y + size;

		context.fill(x, y, right, bottom, SLOT_HIGHLIGHT);
		context.fill(x, y, right - 1, bottom - 1, SLOT_SHADOW);
		context.fill(x + 1, y + 1, right - 1, bottom - 1, SLOT_FILL);
	}
}
