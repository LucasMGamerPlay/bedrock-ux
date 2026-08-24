package com.bedrockux.mixin;

import com.bedrockux.BedrockUX;
import com.bedrockux.ui.BedrockTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Fase 1 / 3: troca a textura 9-slice do botao pelo desenho plano do Bedrock.
 *
 * <p>O fundo do Bedrock e claro, entao o rotulo branco do vanilla ficaria ilegivel. Nao da
 * para so trocar a cor: o rotulo passa por {@code ActiveTextCollector}, cujo
 * {@code Parameters} tem pose, opacidade e scissor — e nenhuma cor. A saida e cancelar o
 * rotulo do vanilla e desenha-lo aqui, onde temos o extractor em maos.
 *
 * <p>Isso e seguro porque nenhuma classe do jogo chama {@code extractDefaultLabel} sem
 * chamar {@code extractDefaultSprite}: todo rotulo suprimido pertence a um botao cujo fundo
 * nos repintamos. O custo e a rolagem que o vanilla faz em rotulos longos demais, que aqui
 * vira corte simples.
 */
@Mixin(AbstractButton.class)
public class AbstractButtonMixin {
	/**
	 * Botoes de acao primaria e de destaque, por chave de traducao. Usar a chave em vez do
	 * texto faz o mapa valer em qualquer idioma. O Java nao tem nocao de "acao primaria",
	 * entao nao ha como derivar isso sozinho — a lista cresce conforme as telas forem
	 * cobertas.
	 */
	private static final Map<String, BedrockTheme.ButtonVariant> BEDROCKUX$VARIANTS = Map.of(
			"selectWorld.create", BedrockTheme.PRIMARY,
			"selectWorld.select", BedrockTheme.PRIMARY,
			"gui.done", BedrockTheme.PRIMARY,
			"menu.online", BedrockTheme.ACCENT);

	@Inject(method = "extractDefaultSprite", at = @At("HEAD"), cancellable = true)
	private void bedrockux$drawFlatButton(GuiGraphicsExtractor context, CallbackInfo callbackInfo) {
		if (!BedrockUX.config().buttons.enabled || bedrockux$keepsVanillaLabel()) {
			return;
		}

		AbstractWidget widget = (AbstractWidget) (Object) this;
		float alpha = ((AbstractWidgetAccessor) widget).bedrockux$getAlpha();

		BedrockTheme.ButtonVariant variant = BedrockTheme.variantFor(
				bedrockux$baseVariant(widget.getMessage()), widget.isHoveredOrFocused(), widget.active);

		BedrockTheme.drawButton(context, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(),
				variant, alpha);
		bedrockux$drawLabel(context, widget, variant, alpha);

		callbackInfo.cancel();
	}

	@Inject(method = "extractDefaultLabel", at = @At("HEAD"), cancellable = true)
	private void bedrockux$suppressVanillaLabel(ActiveTextCollector collector, CallbackInfo callbackInfo) {
		if (BedrockUX.config().buttons.enabled && !bedrockux$keepsVanillaLabel()) {
			callbackInfo.cancel();
		}
	}

	/**
	 * O {@code SpriteIconButton.TextAndIcon} desenha o proprio texto fora do
	 * {@code extractDefaultLabel}, entao nao conseguimos recolori-lo. Repintar o fundo dele
	 * de claro deixaria texto branco sobre claro, ilegivel — melhor manter o visual do
	 * vanilla nesse caso.
	 */
	private boolean bedrockux$keepsVanillaLabel() {
		return (Object) this instanceof SpriteIconButton.TextAndIcon;
	}

	private static BedrockTheme.ButtonVariant bedrockux$baseVariant(Component message) {
		if (BedrockUX.config().buttons.semanticColors
				&& message.getContents() instanceof TranslatableContents translatable) {
			return BEDROCKUX$VARIANTS.getOrDefault(translatable.getKey(), BedrockTheme.SECONDARY);
		}

		return BedrockTheme.SECONDARY;
	}

	/** Rotulo centralizado e sem sombra, como no Bedrock. */
	private static void bedrockux$drawLabel(GuiGraphicsExtractor context, AbstractWidget widget,
			BedrockTheme.ButtonVariant variant, float alpha) {
		Component message = widget.getMessage();

		if (message.getString().isEmpty()) {
			return;
		}

		Font font = Minecraft.getInstance().font;
		int textX = widget.getX() + (widget.getWidth() - font.width(message)) / 2;
		int textY = widget.getY() + (widget.getHeight() - font.lineHeight) / 2 + 1;

		context.text(font, message, textX, textY, BedrockTheme.scaleAlpha(variant.text(), alpha), false);
	}
}
