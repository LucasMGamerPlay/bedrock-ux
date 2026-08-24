package com.bedrockux.mixin;

import com.bedrockux.BedrockUX;
import com.bedrockux.config.BedrockUXConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * UI-05: telas deslizam ao abrir, em vez de aparecerem secas.
 *
 * <p>O GDD propunha guardar a imagem da tela anterior num buffer e interpolar as duas. No
 * modelo retained-mode do 26.2 isso exigiria capturar framebuffer a cada troca de tela, o
 * que e caro e briga com mods de renderizacao. Aqui a tela nova entra deslizando pela
 * matriz de {@code GuiGraphicsExtractor.pose()}: mesmo efeito percebido, sem tocar em
 * framebuffer nenhum.
 *
 * <p>A matriz e empilhada no inicio da extracao e desempilhada em <em>todos</em> os pontos
 * de retorno — {@code RETURN} em vez de {@code TAIL}, senao um retorno antecipado deixaria
 * a pilha desbalanceada e entortaria o resto da GUI.
 *
 * <p>O alvo e {@code extractRenderStateWithTooltipAndSubtitles}, e nao
 * {@code extractRenderState}: este ultimo e sobrescrito por quase toda tela sem chamar
 * {@code super}, entao um hook nele so pegaria as telas que nao o sobrescrevem. O wrapper e
 * {@code final}, logo sempre passa por aqui.
 */
@Mixin(Screen.class)
public abstract class ScreenTransitionMixin {
	@Unique
	private long bedrockux$openedAtMillis;

	@Unique
	private boolean bedrockux$awaitingFirstFrame;

	@Unique
	private boolean bedrockux$posePushed;

	/**
	 * {@code init} apenas <em>arma</em> a animacao; o relogio so comeca no primeiro frame
	 * desenhado.
	 *
	 * <p>Isso importa porque {@code init} roda bem antes da tela aparecer — a tela inicial e
	 * construida enquanto o overlay de carregamento ainda cobre tudo. Marcando o tempo em
	 * {@code init}, a animacao inteira acontecia escondida e o jogador via a tela ja parada.
	 */
	@Inject(method = "init(II)V", at = @At("TAIL"))
	private void bedrockux$armTransition(int width, int height, CallbackInfo callbackInfo) {
		this.bedrockux$awaitingFirstFrame = true;
		this.bedrockux$openedAtMillis = 0L;
	}

	@Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"))
	private void bedrockux$slideIn(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick,
			CallbackInfo callbackInfo) {
		BedrockUXConfig.Transitions config = BedrockUX.config().transitions;
		this.bedrockux$posePushed = false;

		if (!config.enabled) {
			return;
		}

		if (this.bedrockux$awaitingFirstFrame) {
			this.bedrockux$awaitingFirstFrame = false;
			this.bedrockux$openedAtMillis = System.currentTimeMillis();
		}

		if (this.bedrockux$openedAtMillis == 0L) {
			return;
		}

		float progress = (System.currentTimeMillis() - this.bedrockux$openedAtMillis) / (float) config.durationMillis;

		if (progress >= 1.0F) {
			return;
		}

		float offset = (1.0F - bedrockux$easeOut(Mth.clamp(progress, 0.0F, 1.0F))) * config.slideDistance;

		context.pose().pushMatrix();
		context.pose().translate(offset, 0.0F);
		this.bedrockux$posePushed = true;
	}

	@Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("RETURN"))
	private void bedrockux$endSlide(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick,
			CallbackInfo callbackInfo) {
		if (this.bedrockux$posePushed) {
			context.pose().popMatrix();
			this.bedrockux$posePushed = false;
		}
	}

	/** Ease-out cubico: entra rapido e assenta devagar, como as telas do Bedrock. */
	@Unique
	private static float bedrockux$easeOut(float t) {
		float inverted = 1.0F - t;
		return 1.0F - inverted * inverted * inverted;
	}
}
