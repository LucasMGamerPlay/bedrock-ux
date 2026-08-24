package com.bedrockux.hud;

/**
 * Cursor vertical compartilhado do canto superior esquerdo da HUD.
 *
 * <p>Os elementos do Bedrock UX empilham nesse canto e precisam saber onde o anterior
 * terminou. Como a extracao da HUD roda inteira na render thread, um estado estatico
 * simples basta — nao ha concorrencia. A ordem vem do registro em
 * {@code HudElementRegistry.addLast}: boneco primeiro, coordenadas embaixo — que e a
 * ordem do Bedrock.
 */
public final class HudLayout {
	private static int topLeftCursor;

	private HudLayout() {
	}

	/** Reinicia o cursor no comeco do frame. Chamado pelo primeiro elemento da pilha. */
	public static void resetTopLeft(int top) {
		topLeftCursor = top;
	}

	/** Desce o cursor depois que um elemento ocupou espaco, ja incluindo o espacamento. */
	public static void advanceTopLeft(int pixels) {
		topLeftCursor += pixels;
	}

	/** Proximo Y livre da pilha. */
	public static int topLeft() {
		return topLeftCursor;
	}
}
