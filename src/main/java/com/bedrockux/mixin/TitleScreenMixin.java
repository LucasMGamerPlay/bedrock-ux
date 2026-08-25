package com.bedrockux.mixin;

import com.bedrockux.BedrockUX;
import com.bedrockux.config.BedrockUXConfig;
import com.bedrockux.ui.PlayerModelPreview;
import com.bedrockux.ui.ShaderCompat;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.function.Supplier;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * UI-04: menu principal no estilo Bedrock — botoes em grade a esquerda e o modelo do
 * jogador a direita.
 *
 * <p>Em vez de reconstruir o menu, reposicionamos o que o vanilla ja montou. Isso mantem
 * intactos os handlers de clique, as tooltips e os botoes que outros mods adicionam, e
 * sobrevive as variacoes do proprio jogo (modo demo, Realms desligado).
 *
 * <p>O layout segue as capturas do Bedrock em
 * {@code Fotos de Menus direto do Minecraft Bedrock Como exemplo/}: coluna unica de botoes
 * altos no centro da tela, e a direita o modelo do jogador com o nome da conta acima.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
	/**
	 * Largura minima para um widget contar como botao de menu. Fica acima dos botoes de
	 * icone de 20x20 (idioma, acessibilidade) e abaixo do par de 98 (Opcoes / Sair).
	 */
	private static final int MIN_MENU_BUTTON_WIDTH = 90;

	/**
	 * Altura padrao de um botao de menu. Serve para descartar o texto de direitos autorais,
	 * que tambem e um botao largo (um PlainTextButton clicavel) mas tem a altura da fonte.
	 */
	private static final int VANILLA_BUTTON_HEIGHT = 20;

	/** Margem lateral para a coluna nao encostar na borda da tela. */
	private static final int SIDE_MARGIN = 8;

	/** Lado dos botoes de icone do vanilla (idioma, acessibilidade). */
	private static final int ICON_BUTTON_SIZE = 20;
	private static final int ICON_BUTTON_GAP = 4;

	/** Espaco entre o nome da conta e o topo do modelo. */
	private static final int NAME_GAP = 4;

	/**
	 * Largura da caixa do modelo em relacao a altura. O widget deriva a escala so da altura
	 * e usa a largura apenas como recorte, entao uma caixa estreita corta os bracos — ainda
	 * mais com o modelo girado.
	 */
	private static final float MODEL_ASPECT = 0.75F;

	/** Limites de giro ao seguir o cursor, em graus. */
	private static final float FOLLOW_YAW_LIMIT = 40.0F;
	private static final float FOLLOW_PITCH_LIMIT = 25.0F;

	/** Regiao do rosto na textura de skin, e a camada de chapeu por cima. */
	@Unique
	private static final int FACE_U = 8;

	@Unique
	private static final int FACE_V = 8;

	@Unique
	private static final int HAT_U = 40;

	@Unique
	private static final int FACE_SIZE = 8;

	@Unique
	private static final int SKIN_TEXTURE_SIZE = 64;

	/** Linhas dos olhos dentro do rosto (o rosto comeca em v=8; olhos em v=12 e 13). */
	@Unique
	private static final int EYE_ROW = 4;

	@Unique
	private static final int EYE_ROWS = 2;

	/** Faixa de pele logo acima dos olhos, usada como "palpebra". */
	@Unique
	private static final int LID_SOURCE_V = 10;

	@Unique
	private static final long BLINK_PERIOD_MILLIS = 4000L;

	@Unique
	private static final long BLINK_DURATION_MILLIS = 130L;

	@Unique
	private PlayerSkinWidget bedrockux$playerModel;

	/** Preenchido so quando o modelo 3D e trocado pelo rosto em 2D. */
	@Unique
	private Supplier<PlayerSkin> bedrockux$faceSkin;

	@Unique
	private int bedrockux$faceX;

	@Unique
	private int bedrockux$faceY;

	@Unique
	private int bedrockux$faceSize;

	private TitleScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void bedrockux$applyBedrockLayout(CallbackInfo callbackInfo) {
		BedrockUXConfig.TitleScreen config = BedrockUX.config().titleScreen;

		if (!config.enabled) {
			return;
		}

		List<AbstractWidget> menuButtons = collectMenuButtons();

		if (menuButtons.isEmpty()) {
			return;
		}

		layoutMenuButtons(menuButtons, config);
		layoutIconButtons();

		if (config.showPlayerModel) {
			addPlayerModel(config);
		}
	}

	/**
	 * Pega os botoes principais do menu por largura e altura. Os de icone (20x20) ficam onde
	 * estao, que e onde o Bedrock tambem os deixa; o par estreito de Opcoes / Sair entra na
	 * grade junto com os largos, senao sobraria solto embaixo. O filtro de altura descarta o
	 * texto de direitos autorais, que e largo o bastante para passar so pela largura.
	 */
	private List<AbstractWidget> collectMenuButtons() {
		List<AbstractWidget> buttons = new ArrayList<>();

		for (GuiEventListener child : this.children()) {
			if (child instanceof Button button
					&& button.getHeight() == VANILLA_BUTTON_HEIGHT
					&& button.getWidth() >= MIN_MENU_BUTTON_WIDTH) {
				buttons.add(button);
			}
		}

		return buttons;
	}

	/**
	 * Empilha os botoes numa coluna centralizada na tela, no formato do Bedrock: poucos
	 * botoes, largos e altos.
	 *
	 * <p>A largura sai do espaco disponivel, com {@code buttonWidth} servindo de teto.
	 * Largura fixa estoura a tela em janelas pequenas ou com GUI scale alto.
	 */
	private void layoutMenuButtons(List<AbstractWidget> buttons, BedrockUXConfig.TitleScreen config) {
		int columns = Math.min(config.columns, buttons.size());
		int rows = Math.ceilDiv(buttons.size(), columns);

		int maxGridWidth = Math.round(this.width * config.menuWidthFraction) * columns;
		int availableWidth = Math.min(maxGridWidth, this.width - SIDE_MARGIN * 2) - (columns - 1) * config.columnGap;
		int buttonWidth = Math.max(MIN_MENU_BUTTON_WIDTH / 2, availableWidth / columns);

		int gridWidth = columns * buttonWidth + (columns - 1) * config.columnGap;
		int originX = (this.width - gridWidth) / 2;

		// Faixa util: abaixo do logo e acima do rodape dos direitos autorais.
		int areaTop = Math.round(this.height * 0.40F);
		int areaBottom = this.height - 24;

		// Botao alto e bonito, mas nao pode empurrar a ultima linha para fora da tela. Se
		// nao couber, a altura cede ate o minimo do vanilla.
		int maxRowHeight = (areaBottom - areaTop) / rows;
		int buttonHeight = Math.max(VANILLA_BUTTON_HEIGHT,
				Math.min(config.buttonHeight, maxRowHeight - config.rowGap));

		int rowHeight = buttonHeight + config.rowGap;
		int gridHeight = rows * rowHeight - config.rowGap;
		int originY = Math.max(areaTop, areaTop + (areaBottom - areaTop - gridHeight) / 2);

		for (int index = 0; index < buttons.size(); index++) {
			AbstractWidget button = buttons.get(index);
			int column = index % columns;
			int row = index / columns;

			button.setSize(buttonWidth, buttonHeight);
			button.setX(originX + column * (buttonWidth + config.columnGap));
			button.setY(originY + row * rowHeight);
		}
	}

	/**
	 * O modelo vem de {@link PlayerSkinWidget}, que renderiza a skin sem precisar de uma
	 * entidade — no menu principal nao existe jogador no mundo. De brinde ele ja e
	 * arrastavel para girar, como no Bedrock.
	 */
	private void addPlayerModel(BedrockUXConfig.TitleScreen config) {
		if (this.minecraft == null) {
			return;
		}

		// Com shader pack ligado o modelo 3D sai deformado aqui — ver ShaderCompat. O rosto em
		// 2D e um blit comum de GUI, caminho que o shader nao toca, entao sobrevive.
		if (config.hideModelWithShaders && ShaderCompat.isShaderPackActive()) {
			bedrockux$useFaceFallback(config);
			return;
		}

		int centerX = Math.round(this.width * config.modelCenterFraction);
		int nameHeight = config.showPlayerName ? this.minecraft.font.lineHeight + NAME_GAP : 0;
		int areaTop = Math.round(this.height * 0.30F);
		int areaBottom = this.height - 24;

		// A altura sai da tela, nao de um valor fixo: em GUI scale alto um modelo de 170
		// unidades passa do rodape e o recorte da caixa corta o personagem ao meio.
		int modelHeight = Math.min(
				Math.round(this.height * config.modelHeightFraction),
				Math.min(config.maxModelHeight, areaBottom - areaTop - nameHeight));

		if (modelHeight < 20) {
			return;
		}

		int modelWidth = Math.round(modelHeight * MODEL_ASPECT);

		PlayerSkinWidget model = new PlayerSkinWidget(
				modelWidth,
				modelHeight,
				this.minecraft.getEntityModels(),
				this.minecraft.getSkinManager().createLookup(this.minecraft.getGameProfile(), false));

		// O bloco inteiro (nome + modelo) e centralizado na faixa abaixo do logo. Ancorar
		// pelo modelo deixava o nome subir por cima do logo.
		int blockTop = Math.max(areaTop, areaTop + (areaBottom - areaTop - nameHeight - modelHeight) / 2);

		if (config.showPlayerName) {
			addPlayerName(centerX, blockTop);
		}

		model.setX(centerX - modelWidth / 2);
		model.setY(blockTop + nameHeight);
		this.addRenderableWidget(model);
		this.bedrockux$playerModel = model;
	}

	/** Nome da conta logo acima do modelo, como no menu do Bedrock. */
	private void addPlayerName(int centerX, int nameY) {
		String name = this.minecraft.getGameProfile().name();

		if (name == null || name.isBlank()) {
			return;
		}

		StringWidget label = new StringWidget(Component.literal(name), this.minecraft.font);
		label.setX(centerX - label.getWidth() / 2);
		label.setY(nameY);
		this.addRenderableWidget(label);
	}

	/**
	 * Os botoes de icone 20x20 (idioma, acessibilidade) ficam no centro da tela no vanilla,
	 * bem onde a coluna do Bedrock passa. No Bedrock esses atalhos moram no canto inferior
	 * esquerdo, entao e para la que eles vao.
	 */
	private void layoutIconButtons() {
		int x = SIDE_MARGIN;
		int y = this.height - SIDE_MARGIN - ICON_BUTTON_SIZE;

		for (GuiEventListener child : this.children()) {
			if (child instanceof AbstractWidget widget
					&& widget.getWidth() == ICON_BUTTON_SIZE
					&& widget.getHeight() == ICON_BUTTON_SIZE) {
				widget.setX(x);
				widget.setY(y);
				x += ICON_BUTTON_SIZE + ICON_BUTTON_GAP;
			}
		}
	}

	/**
	 * Faz o personagem acompanhar o cursor, como no menu do Bedrock. O widget do vanilla so
	 * gira quando arrastado; aqui a rotacao e reescrita a cada frame a partir da posicao do
	 * mouse, entao o giro acontece sozinho.
	 */
	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void bedrockux$updatePlayerModel(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick,
			CallbackInfo callbackInfo) {
		PlayerSkinWidget model = this.bedrockux$playerModel;

		if (model == null) {
			return;
		}

		BedrockUXConfig.TitleScreen config = BedrockUX.config().titleScreen;
		PlayerModelPreview.update(model, this.width, this.height, mouseX, mouseY,
				config.modelFollowsMouse, config.bodyFollowFactor);
	}

	/** Prepara o rosto em 2D no lugar do modelo, mantendo o nome acima dele. */
	private void bedrockux$useFaceFallback(BedrockUXConfig.TitleScreen config) {
		int size = Math.min(Math.round(this.height * config.modelHeightFraction), config.maxModelHeight) / 2;

		if (size < FACE_SIZE) {
			return;
		}

		int centerX = Math.round(this.width * config.modelCenterFraction);
		int nameHeight = config.showPlayerName ? this.minecraft.font.lineHeight + NAME_GAP : 0;

		this.bedrockux$faceSize = size;
		this.bedrockux$faceX = centerX - size / 2;
		this.bedrockux$faceY = (this.height - size - nameHeight) / 2 + nameHeight;
		this.bedrockux$faceSkin =
				this.minecraft.getSkinManager().createLookup(this.minecraft.getGameProfile(), false);

		if (config.showPlayerName) {
			addPlayerName(centerX, this.bedrockux$faceY - nameHeight);
		}
	}

	/**
	 * Desenha o rosto da skin ampliado, com a camada de chapeu por cima.
	 *
	 * <p>Sai no RETURN para ficar acima do fundo da tela, e usa {@code blit} comum — o
	 * caminho de GUI, que o shader pack nao substitui.
	 */
	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void bedrockux$drawFaceFallback(GuiGraphicsExtractor context, int mouseX, int mouseY,
			float partialTick, CallbackInfo callbackInfo) {
		if (this.bedrockux$faceSkin == null) {
			return;
		}

		PlayerSkin skin = this.bedrockux$faceSkin.get();

		if (skin == null) {
			return;
		}

		Identifier texture = skin.body().texturePath();
		int x = this.bedrockux$faceX;
		int y = this.bedrockux$faceY;
		int size = this.bedrockux$faceSize;

		context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, FACE_U, FACE_V, size, size,
				FACE_SIZE, FACE_SIZE, SKIN_TEXTURE_SIZE, SKIN_TEXTURE_SIZE);

		if (BedrockUX.config().titleScreen.blink) {
			bedrockux$drawEyelid(context, texture, x, y, size);
		}

		// A camada de chapeu vem por ultimo: se a skin tiver cabelo cobrindo os olhos, ele
		// continua por cima da palpebra, como deve ser.
		context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, HAT_U, FACE_V, size, size,
				FACE_SIZE, FACE_SIZE, SKIN_TEXTURE_SIZE, SKIN_TEXTURE_SIZE);
	}

	/**
	 * Fecha os olhos copiando uma faixa de pele da propria skin por cima deles.
	 *
	 * <p>Nao ha arte embutida de proposito: a "palpebra" e a testa do jogador reaproveitada,
	 * entao ela fecha com qualquer tom de pele. Packs como o Just Expressions resolvem isso
	 * distribuindo variantes da skin com os olhos fechados, o que so funciona com as skins
	 * que eles conhecem.
	 */
	@Unique
	private static void bedrockux$drawEyelid(GuiGraphicsExtractor context, Identifier texture,
			int x, int y, int size) {
		if (System.currentTimeMillis() % BLINK_PERIOD_MILLIS >= BLINK_DURATION_MILLIS) {
			return;
		}

		int pixel = size / FACE_SIZE;

		if (pixel <= 0) {
			return;
		}

		context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + EYE_ROW * pixel,
				FACE_U, LID_SOURCE_V, size, EYE_ROWS * pixel,
				FACE_SIZE, EYE_ROWS, SKIN_TEXTURE_SIZE, SKIN_TEXTURE_SIZE);
	}
}
