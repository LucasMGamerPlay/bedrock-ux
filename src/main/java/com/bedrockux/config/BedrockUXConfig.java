package com.bedrockux.config;

import net.minecraft.util.Mth;

/**
 * Modelo da configuracao serializada em {@code config/bedrockux.json}.
 *
 * <p>Campos publicos e construtor vazio de proposito: o GSON preenche por reflexao,
 * e qualquer campo ausente no arquivo mantem o valor padrao definido aqui.
 */
public final class BedrockUXConfig {
	public Coordinates coordinates = new Coordinates();
	public PaperDoll paperDoll = new PaperDoll();
	public Buttons buttons = new Buttons();

	/** Corrige valores invalidos vindos de um arquivo editado a mao. */
	public void sanitize() {
		if (coordinates == null) {
			coordinates = new Coordinates();
		}

		if (paperDoll == null) {
			paperDoll = new PaperDoll();
		}

		if (buttons == null) {
			buttons = new Buttons();
		}

		coordinates.backgroundOpacity = Mth.clamp(coordinates.backgroundOpacity, 0.0F, 1.0F);
		coordinates.offsetX = Mth.clamp(coordinates.offsetX, 0, 512);
		coordinates.offsetY = Mth.clamp(coordinates.offsetY, 0, 512);

		paperDoll.yawOffsetDegrees = Mth.wrapDegrees(paperDoll.yawOffsetDegrees);
		paperDoll.offsetX = Mth.clamp(paperDoll.offsetX, 0, 512);
		paperDoll.offsetY = Mth.clamp(paperDoll.offsetY, 0, 512);
		paperDoll.gap = Mth.clamp(paperDoll.gap, 0, 64);
		paperDoll.width = Mth.clamp(paperDoll.width, 8, 256);
		paperDoll.height = Mth.clamp(paperDoll.height, 8, 256);
		paperDoll.scale = Mth.clamp(paperDoll.scale, 1.0F, 200.0F);
		paperDoll.tiltDegrees = Mth.clamp(paperDoll.tiltDegrees, -60.0F, 60.0F);
		paperDoll.headPitchLimit = Mth.clamp(paperDoll.headPitchLimit, 0.0F, 90.0F);
	}

	/** UI-02: coordenadas no estilo Bedrock. */
	public static final class Coordinates {
		public boolean enabled = true;
		public boolean showFacing = false;
		public boolean showBiome = false;
		/** Some quando o F3 esta aberto, para nao sobrepor o debug da Mojang. */
		public boolean hideWithDebugScreen = true;
		/** O Bedrock nao usa sombra no texto da HUD. */
		public boolean textShadow = false;
		/** No Bedrock as coordenadas ficam abaixo do boneco. */
		public boolean belowPaperDoll = true;
		public int offsetX = 4;
		/** So vale quando {@code belowPaperDoll} e false. */
		public int offsetY = 4;
		public float backgroundOpacity = 0.4F;
	}

	/** UI-01: miniatura 3D do jogador no canto superior esquerdo. */
	public static final class PaperDoll {
		public boolean enabled = true;
		public boolean hideWithDebugScreen = true;
		public int offsetX = 4;
		/** Topo da pilha do canto superior esquerdo. */
		public int offsetY = 4;
		/** Espaco entre o boneco e a caixa de coordenadas logo abaixo. */
		public int gap = 2;
		public int width = 44;
		public int height = 66;
		public float scale = 28.0F;
		/** Inclinacao vertical da camera. */
		public float tiltDegrees = 0.0F;
		/**
		 * Giro do corpo em graus a partir da vista de frente, que e a do Bedrock.
		 * {@code 0} deixa o boneco de frente e reto; valores pequenos dao o leve angulo
		 * do Bedrock; {@code 180} mostra as costas.
		 */
		public float yawOffsetDegrees = 20.0F;
		/**
		 * Mantem o boneco em pe durante elytra, nado e tridente. Sem isso o vanilla deita o
		 * modelo na horizontal, que nao cabe na caixa e sai cortado. Desligue para ver a
		 * pose crua de voo.
		 */
		public boolean uprightWhileFlying = true;
		/**
		 * Limite do pitch da cabeca, em graus. Sem limite, olhar para cima deixa a cabeca
		 * quase na horizontal e o boneco fica ilegivel numa caixa pequena. 90 desliga o
		 * limite e espelha o pitch real.
		 */
		public float headPitchLimit = 35.0F;
	}

	/** Fase 1: cores planas dos botoes no lugar das texturas do vanilla. */
	public static final class Buttons {
		public boolean enabled = true;
	}
}
