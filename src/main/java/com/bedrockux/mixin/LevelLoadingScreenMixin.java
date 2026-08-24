package com.bedrockux.mixin;

import com.bedrockux.BedrockUX;
import com.bedrockux.config.BedrockUXConfig;
import com.bedrockux.loading.LoadingTips;
import com.bedrockux.ui.BedrockTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * UI-07: troca o mapa de chunks do vanilla pelo painel de carregamento do Bedrock.
 *
 * <p>O layout segue a captura {@code tela de caregamento.png}: o mundo continua visivel ao
 * fundo, com o logo no topo e um painel cinza-claro no centro. Dentro do painel vem o
 * titulo em texto escuro e uma caixa translucida com a dica e a barra segmentada.
 *
 * <p>O progresso reaproveita o {@code smoothedProgress} que o proprio jogo ja suaviza a
 * cada tick — nao ha necessidade de sincronizar nada com o threading de chunks por conta
 * propria, que era a preocupacao anotada na planilha.
 */
@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin {
	@Unique
	private static final LogoRenderer BEDROCKUX$LOGO = new LogoRenderer(true);

	@Unique
	private static final int PANEL_PADDING = 6;

	@Unique
	private static final int TITLE_HEIGHT = 16;

	@Shadow
	private float smoothedProgress;

	/**
	 * Sorteia a dica uma vez, ao abrir a tela. {@code added()} nao serve como alvo porque
	 * {@code LevelLoadingScreen} nao o sobrescreve — o Mixin so enxerga metodos da propria
	 * classe alvo.
	 */
	@Inject(method = "<init>", at = @At("RETURN"))
	private void bedrockux$pickTip(CallbackInfo callbackInfo) {
		LoadingTips.pickRandom();
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void bedrockux$drawBedrockLoading(GuiGraphicsExtractor context, int mouseX, int mouseY,
			float partialTick, CallbackInfo callbackInfo) {
		BedrockUXConfig.LoadingScreen config = BedrockUX.config().loadingScreen;

		if (!config.enabled) {
			return;
		}

		int screenWidth = context.guiWidth();
		int screenHeight = context.guiHeight();
		Font font = Minecraft.getInstance().font;

		if (config.showLogo) {
			BEDROCKUX$LOGO.extractRenderState(context, screenWidth, 1.0F);
		}

		int panelWidth = Math.min(config.panelWidth, screenWidth - 40);
		int innerWidth = panelWidth - PANEL_PADDING * 2;
		int textWidth = innerWidth - PANEL_PADDING * 2;

		Component tip = config.showTips ? LoadingTips.current() : null;
		int textHeight = tip == null ? 0 : font.wordWrapHeight(tip, textWidth);
		int innerHeight = PANEL_PADDING * 2 + textHeight + PANEL_PADDING + config.barHeight;
		int panelHeight = TITLE_HEIGHT + innerHeight + PANEL_PADDING * 2;

		int panelX = (screenWidth - panelWidth) / 2;
		int panelY = (screenHeight - panelHeight) / 2;

		BedrockTheme.drawPanel(context, panelX, panelY, panelWidth, panelHeight);

		Component title = Component.translatable("text.bedrockux.loading.title");
		context.text(font, title, panelX + (panelWidth - font.width(title)) / 2,
				panelY + PANEL_PADDING, BedrockTheme.PANEL_TITLE_TEXT, false);

		int innerX = panelX + PANEL_PADDING;
		int innerY = panelY + TITLE_HEIGHT + PANEL_PADDING;
		BedrockTheme.drawInnerBox(context, innerX, innerY, innerWidth, innerHeight);

		if (tip != null) {
			context.textWithWordWrap(font, tip, innerX + PANEL_PADDING, innerY + PANEL_PADDING,
					textWidth, BedrockTheme.PANEL_INNER_TEXT);
		}

		BedrockTheme.drawProgressBar(context, innerX + PANEL_PADDING,
				innerY + innerHeight - PANEL_PADDING - config.barHeight,
				textWidth, config.barHeight, Mth.clamp(this.smoothedProgress, 0.0F, 1.0F));

		callbackInfo.cancel();
	}
}
