package com.bedrockux.mixin;

import com.bedrockux.BedrockUX;
import com.bedrockux.config.BedrockUXConfig;
import com.bedrockux.ui.BedrockTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * UI-07 (parte 2): o painel do Bedrock nas telas de mensagem — salvar mundo, conectar,
 * carregar.
 *
 * <p>O layout segue {@code tela de salvamento.png}: logo no topo, painel cinza com o titulo
 * no cabecalho e uma caixa translucida com a mensagem explicativa.
 *
 * <p>O Bedrock mostra <em>duas</em> frases (titulo curto + explicacao), enquanto a tela do
 * Java carrega uma so. Por isso a mensagem do vanilla vira o titulo e o corpo vem de um mapa
 * proprio, por chave de traducao. Telas fora do mapa ganham so o cabecalho, sem a caixa —
 * inventar texto para elas seria pior do que nao mostrar nada.
 */
@Mixin(GenericMessageScreen.class)
public abstract class GenericMessageScreenMixin extends Screen {
	@Unique
	private static final LogoRenderer BEDROCKUX$LOGO = new LogoRenderer(true);

	@Unique
	private static final int PANEL_PADDING = 6;

	@Unique
	private static final int TITLE_HEIGHT = 16;

	/** Explicacoes no espirito do Bedrock, por chave da mensagem do vanilla. */
	@Unique
	private static final Map<String, String> BEDROCKUX$BODY_KEYS = Map.of(
			"menu.savingLevel", "text.bedrockux.message.saving");

	/**
	 * Icone animado do salvamento. O Minecraft nao le GIF, entao a animacao vem de uma folha
	 * de sprites vertical: 52 quadros de 32x32 empilhados, um por linha.
	 */
	@Unique
	private static final Identifier BEDROCKUX$SAVING_ICON = BedrockUX.id("textures/gui/saving_icon.png");

	@Unique
	private static final int ICON_SIZE = 32;

	@Unique
	private static final int ICON_FRAMES = 52;

	/** 100 ms por quadro, o mesmo tempo do GIF original. */
	@Unique
	private static final long ICON_FRAME_MILLIS = 100L;

	@Shadow
	private FocusableTextWidget textWidget;

	private GenericMessageScreenMixin(Component title) {
		super(title);
	}

	/**
	 * Esconde o texto do vanilla, que ficaria duplicado com o cabecalho do painel.
	 *
	 * <p>O widget continua existindo, entao a narracao de acessibilidade segue funcionando —
	 * some apenas o desenho.
	 */
	@Inject(method = "init", at = @At("TAIL"))
	private void bedrockux$hideVanillaText(CallbackInfo callbackInfo) {
		if (BedrockUX.config().messageScreen.enabled && this.textWidget != null) {
			this.textWidget.visible = false;
		}
	}

	@Inject(method = "extractBackground", at = @At("TAIL"))
	private void bedrockux$drawPanel(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick,
			CallbackInfo callbackInfo) {
		BedrockUXConfig.MessageScreen config = BedrockUX.config().messageScreen;

		if (!config.enabled) {
			return;
		}

		int screenWidth = context.guiWidth();
		int screenHeight = context.guiHeight();
		Font font = Minecraft.getInstance().font;

		if (config.showLogo) {
			BEDROCKUX$LOGO.extractRenderState(context, screenWidth, 1.0F);
		}

		Component title = this.getTitle();
		Component body = bedrockux$bodyFor(title);

		int panelWidth = Math.min(config.panelWidth, screenWidth - 40);
		int innerWidth = panelWidth - PANEL_PADDING * 2;
		int textWidth = innerWidth - PANEL_PADDING * 2;

		boolean showIcon = config.showSavingIcon && body != null;
		int iconBlock = showIcon ? ICON_SIZE + PANEL_PADDING : 0;

		int bodyHeight = body == null ? 0 : font.wordWrapHeight(body, textWidth);
		int innerHeight = body == null ? 0 : bodyHeight + iconBlock + PANEL_PADDING * 2;
		int panelHeight = TITLE_HEIGHT + innerHeight + PANEL_PADDING * 2;

		int panelX = (screenWidth - panelWidth) / 2;
		int panelY = (screenHeight - panelHeight) / 2;

		BedrockTheme.drawPanel(context, panelX, panelY, panelWidth, panelHeight);
		context.text(font, title, panelX + (panelWidth - font.width(title)) / 2,
				panelY + PANEL_PADDING, BedrockTheme.PANEL_TITLE_TEXT, false);

		if (body != null) {
			int innerX = panelX + PANEL_PADDING;
			int innerY = panelY + TITLE_HEIGHT + PANEL_PADDING;

			BedrockTheme.drawInnerBox(context, innerX, innerY, innerWidth, innerHeight);
			context.textWithWordWrap(font, body, innerX + PANEL_PADDING, innerY + PANEL_PADDING,
					textWidth, BedrockTheme.PANEL_INNER_TEXT);

			if (showIcon) {
				bedrockux$drawSavingIcon(context, panelX + panelWidth / 2 - ICON_SIZE / 2,
						innerY + PANEL_PADDING + bodyHeight + PANEL_PADDING);
			}
		}
	}

	@Unique
	private static Component bedrockux$bodyFor(Component title) {
		if (!(title.getContents() instanceof TranslatableContents translatable)) {
			return null;
		}

		String bodyKey = BEDROCKUX$BODY_KEYS.get(translatable.getKey());
		return bodyKey == null ? null : Component.translatable(bodyKey);
	}

	/**
	 * Desenha o quadro atual recortando a folha pela coordenada V.
	 *
	 * <p>A animacao anda pelo relogio do sistema, nao por ticks do jogo: durante o
	 * salvamento o jogo trava por instantes, e um contador de ticks faria o icone engasgar
	 * justamente quando ele precisa mostrar que algo esta acontecendo.
	 */
	@Unique
	private static void bedrockux$drawSavingIcon(GuiGraphicsExtractor context, int x, int y) {
		int frame = (int) ((System.currentTimeMillis() / ICON_FRAME_MILLIS) % ICON_FRAMES);

		context.blit(RenderPipelines.GUI_TEXTURED, BEDROCKUX$SAVING_ICON, x, y,
				0.0F, (float) (frame * ICON_SIZE), ICON_SIZE, ICON_SIZE,
				ICON_SIZE, ICON_SIZE * ICON_FRAMES);
	}
}
