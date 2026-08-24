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
	public Transitions transitions = new Transitions();
	public LoadingScreen loadingScreen = new LoadingScreen();
	public MessageScreen messageScreen = new MessageScreen();
	public TitleScreen titleScreen = new TitleScreen();

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

		if (titleScreen == null) {
			titleScreen = new TitleScreen();
		}

		if (transitions == null) {
			transitions = new Transitions();
		}

		if (loadingScreen == null) {
			loadingScreen = new LoadingScreen();
		}

		if (messageScreen == null) {
			messageScreen = new MessageScreen();
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

		loadingScreen.panelWidth = Mth.clamp(loadingScreen.panelWidth, 80, 800);
		messageScreen.panelWidth = Mth.clamp(messageScreen.panelWidth, 80, 800);
		loadingScreen.barHeight = Mth.clamp(loadingScreen.barHeight, 3, 40);

		transitions.durationMillis = Mth.clamp(transitions.durationMillis, 0, 2000);
		transitions.slideDistance = Mth.clamp(transitions.slideDistance, -400.0F, 400.0F);

		titleScreen.columns = Mth.clamp(titleScreen.columns, 1, 4);
		titleScreen.buttonWidth = Mth.clamp(titleScreen.buttonWidth, 40, 400);
		titleScreen.columnGap = Mth.clamp(titleScreen.columnGap, 0, 64);
		titleScreen.rowGap = Mth.clamp(titleScreen.rowGap, 0, 64);
		titleScreen.buttonHeight = Mth.clamp(titleScreen.buttonHeight, 12, 80);
		titleScreen.menuWidthFraction = Mth.clamp(titleScreen.menuWidthFraction, 0.15F, 1.0F);
		titleScreen.modelCenterFraction = Mth.clamp(titleScreen.modelCenterFraction, 0.0F, 1.0F);
		titleScreen.modelHeightFraction = Mth.clamp(titleScreen.modelHeightFraction, 0.1F, 1.0F);
		titleScreen.maxModelHeight = Mth.clamp(titleScreen.maxModelHeight, 20, 600);
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
		/** Verde para acao primaria e roxo no Realms, como no Bedrock. */
		public boolean semanticColors = true;
	}

	/** UI-07: barra continua e dicas no lugar do mapa de chunks do vanilla. */
	public static final class LoadingScreen {
		public boolean enabled = true;
		public boolean showTips = true;
		/** O Bedrock mostra o logo acima do painel de carregamento. */
		public boolean showLogo = true;
		public int panelWidth = 260;
		public int barHeight = 6;
	}

	/** UI-07 (parte 2): painel do Bedrock nas telas de mensagem (salvar, conectar). */
	public static final class MessageScreen {
		public boolean enabled = true;
		public boolean showLogo = true;
		/** Bau animado no meio do painel, como no salvamento do Bedrock. */
		public boolean showSavingIcon = true;
		public int panelWidth = 260;
	}

	/** UI-05: telas deslizam ao abrir, em vez de aparecerem secas. */
	public static final class Transitions {
		public boolean enabled = true;
		public int durationMillis = 180;
		/** Deslocamento inicial em pixels de GUI. Negativo faz entrar pela esquerda. */
		public float slideDistance = 24.0F;
	}

	/** UI-04: menu principal com botoes em grade e o modelo do jogador a direita. */
	public static final class TitleScreen {
		public boolean enabled = true;
		/** Colunas da pilha de botoes. O Bedrock usa uma coluna unica centralizada. */
		public int columns = 1;
		public int buttonWidth = 180;
		/** O botao do Bedrock e bem mais alto que os 20 do Java. */
		public int buttonHeight = 28;
		public int columnGap = 8;
		public int rowGap = 6;
		/** Teto de largura da coluna, como fracao da tela. */
		public float menuWidthFraction = 0.34F;
		public boolean showPlayerModel = true;
		/** Nome da conta acima do modelo, como no Bedrock. */
		public boolean showPlayerName = true;
		/** O personagem acompanha o cursor, como no menu do Bedrock. */
		public boolean modelFollowsMouse = true;
		/** Altura do modelo como fracao da tela. Fixo em pixels estoura em GUI scale alto. */
		public float modelHeightFraction = 0.55F;
		/** Teto absoluto de altura, para nao virar um gigante em telas muito altas. */
		public int maxModelHeight = 170;
		/** Centro da area do modelo, como fracao da largura da tela. */
		public float modelCenterFraction = 0.82F;
	}
}
