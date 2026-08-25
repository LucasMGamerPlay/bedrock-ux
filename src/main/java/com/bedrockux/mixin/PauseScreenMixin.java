package com.bedrockux.mixin;

import com.bedrockux.BedrockUX;
import com.bedrockux.config.BedrockUXConfig;
import com.bedrockux.ui.PlayerModelPreview;
import com.bedrockux.ui.ShaderCompat;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu de pausa no estilo Bedrock: botoes empilhados a esquerda e o modelo do jogador a
 * direita.
 *
 * <p>Mesma abordagem do menu principal — reposiciona o que o vanilla ja criou, em vez de
 * remontar a tela, para nao perder handlers, tooltips nem botoes de outros mods.
 */
@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
	/** Largura minima para contar como botao de menu; deixa de fora os icones 20x20. */
	private static final int MIN_MENU_BUTTON_WIDTH = 90;

	private static final int VANILLA_BUTTON_HEIGHT = 20;

	private static final int SIDE_MARGIN = 12;

	private static final int ROW_GAP = 6;

	/** Tamanho dos botoes de icone do vanilla, que acompanham o bloco. */
	private static final int ICON_BUTTON_SIZE = 20;

	/** Regiao do logo dentro de minecraft.png, em coordenadas logicas. */
	private static final int LOGO_SRC_WIDTH = 256;

	private static final int LOGO_SRC_HEIGHT = 44;

	private static final int LOGO_TEX_WIDTH = 256;

	private static final int LOGO_TEX_HEIGHT = 64;

	@Unique
	private PlayerSkinWidget bedrockux$playerModel;

	@Unique
	private int bedrockux$blockCenterX;

	@Unique
	private int bedrockux$logoY;

	private PauseScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void bedrockux$applyBedrockLayout(CallbackInfo callbackInfo) {
		BedrockUXConfig.PauseScreen config = BedrockUX.config().pauseScreen;

		if (!config.enabled) {
			return;
		}

		List<AbstractWidget> widgets = bedrockux$collectMenuWidgets();

		if (widgets.isEmpty()) {
			return;
		}

		bedrockux$shiftBlockLeft(widgets, config);

		if (config.showLogo) {
			bedrockux$hideTitleWidget();
		}

		if (config.showPlayerModel && !ShaderCompat.isShaderPackActive()) {
			bedrockux$addPlayerModel(config);
		}
	}

	/**
	 * Pega os botoes do menu e os de icone que os acompanham. Os dois grupos formam um bloco
	 * so, que e deslocado junto para preservar o arranjo do vanilla.
	 */
	private List<AbstractWidget> bedrockux$collectMenuWidgets() {
		List<AbstractWidget> widgets = new ArrayList<>();

		for (GuiEventListener child : this.children()) {
			if (!(child instanceof Button button)) {
				continue;
			}

			boolean menuButton = button.getHeight() == VANILLA_BUTTON_HEIGHT
					&& button.getWidth() >= MIN_MENU_BUTTON_WIDTH;
			boolean iconButton = button.getHeight() == ICON_BUTTON_SIZE
					&& button.getWidth() == ICON_BUTTON_SIZE;

			if (menuButton || iconButton) {
				widgets.add(button);
			}
		}

		return widgets;
	}

	/**
	 * Move o bloco inteiro para a esquerda, sem reorganizar nada.
	 *
	 * <p>O arranjo do vanilla — o botao largo em cima, os pares lado a lado, os icones
	 * embaixo — e preservado porque todos os widgets recebem o mesmo deslocamento. Reposicionar
	 * um por um mudaria o desenho da tela, que nao e o pedido.
	 */
	private void bedrockux$shiftBlockLeft(List<AbstractWidget> widgets, BedrockUXConfig.PauseScreen config) {
		int left = Integer.MAX_VALUE;
		int top = Integer.MAX_VALUE;
		int bottom = Integer.MIN_VALUE;

		for (AbstractWidget widget : widgets) {
			left = Math.min(left, widget.getX());
			top = Math.min(top, widget.getY());
			bottom = Math.max(bottom, widget.getY() + widget.getHeight());
		}

		// Abre espaco para o logo acima do bloco e encosta tudo na margem esquerda.
		int logoSpace = config.showLogo
				? Math.round(LOGO_SRC_HEIGHT * config.logoScale) + ROW_GAP * 2
				: 0;
		int blockHeight = bottom - top;
		int desiredTop = Math.max(logoSpace + SIDE_MARGIN, (this.height - blockHeight) / 2 + logoSpace / 2);

		int deltaX = SIDE_MARGIN - left;
		int deltaY = desiredTop - top;

		for (AbstractWidget widget : widgets) {
			widget.setX(widget.getX() + deltaX);
			widget.setY(widget.getY() + deltaY);
		}

		this.bedrockux$blockCenterX = SIDE_MARGIN + bedrockux$blockWidth(widgets) / 2;
		this.bedrockux$logoY = Math.max(SIDE_MARGIN, desiredTop - logoSpace + ROW_GAP);
	}

	private static int bedrockux$blockWidth(List<AbstractWidget> widgets) {
		int left = Integer.MAX_VALUE;
		int right = Integer.MIN_VALUE;

		for (AbstractWidget widget : widgets) {
			left = Math.min(left, widget.getX());
			right = Math.max(right, widget.getX() + widget.getWidth());
		}

		return right - left;
	}

	/**
	 * Desenha o logo acima do bloco de botoes.
	 *
	 * <p>O {@code LogoRenderer} centraliza o logo na largura que recebe. Passando o dobro do
	 * centro do bloco, ele acaba centralizado sobre os botoes em vez de sobre a tela.
	 */
	/**
	 * O titulo "Menu do jogo" e um widget de texto centralizado no topo, que ficaria
	 * atravessado sobre o logo. Escondemos o desenho e mantemos o widget vivo, para a
	 * narracao de acessibilidade continuar funcionando.
	 */
	private void bedrockux$hideTitleWidget() {
		for (GuiEventListener child : this.children()) {
			if (child instanceof StringWidget widget
					&& widget.getMessage().getString().equals(this.getTitle().getString())) {
				widget.visible = false;
			}
		}
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void bedrockux$drawScreenExtras(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick,
			CallbackInfo callbackInfo) {
		BedrockUXConfig.PauseScreen config = BedrockUX.config().pauseScreen;

		if (!config.enabled) {
			return;
		}

		if (config.showLogo && this.bedrockux$blockCenterX > 0) {
			bedrockux$drawLogo(context, config.logoScale);
		}

		if (this.bedrockux$playerModel != null) {
			PlayerModelPreview.update(this.bedrockux$playerModel, this.width, this.height, mouseX, mouseY,
					config.modelFollowsMouse, config.bodyFollowFactor);
		}
	}

	/**
	 * Desenha o logo em escala propria.
	 *
	 * <p>O {@code LogoRenderer} do vanilla so desenha no tamanho fixo de 256x44, grande
	 * demais para dividir a tela com os botoes. Aqui o mesmo recorte da textura e esticado
	 * para o tamanho desejado.
	 */
	private void bedrockux$drawLogo(GuiGraphicsExtractor context, float scale) {
		int width = Math.max(16, Math.round(LOGO_SRC_WIDTH * scale));
		int height = Math.max(4, Math.round(LOGO_SRC_HEIGHT * scale));

		context.blit(RenderPipelines.GUI_TEXTURED, LogoRenderer.MINECRAFT_LOGO,
				this.bedrockux$blockCenterX - width / 2, this.bedrockux$logoY,
				0.0F, 0.0F, width, height, LOGO_SRC_WIDTH, LOGO_SRC_HEIGHT,
				LOGO_TEX_WIDTH, LOGO_TEX_HEIGHT);
	}

	/**
	 * O modelo usa {@link PlayerSkinWidget} pelo mesmo motivo do menu principal: ele desenha
	 * a skin sem depender de uma entidade. Com shader pack ativo ele nao entra, porque fora
	 * do caminho normal de mundo o shader deforma o modelo — ver {@code ShaderCompat}.
	 */
	private void bedrockux$addPlayerModel(BedrockUXConfig.PauseScreen config) {
		if (this.minecraft == null) {
			return;
		}

		int areaLeft = Math.round(this.width * config.menuWidthFraction);
		int modelHeight = Math.min(Math.round(this.height * config.modelHeightFraction), config.maxModelHeight);

		if (modelHeight < 20) {
			return;
		}

		int modelWidth = Math.round(modelHeight * 0.75F);

		PlayerSkinWidget model = new PlayerSkinWidget(
				modelWidth,
				modelHeight,
				this.minecraft.getEntityModels(),
				this.minecraft.getSkinManager().createLookup(this.minecraft.getGameProfile(), false));

		model.setX(areaLeft + (this.width - areaLeft - modelWidth) / 2);
		model.setY((this.height - modelHeight) / 2);
		this.addRenderableWidget(model);
		this.bedrockux$playerModel = model;
	}
}
