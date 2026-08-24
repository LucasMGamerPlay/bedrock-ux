package com.bedrockux.mixin;

import com.bedrockux.BedrockUX;
import com.bedrockux.ui.BedrockTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fase 1: troca a textura 9-slice do botao pelo desenho plano do Bedrock.
 *
 * <p>{@code extractDefaultSprite} so desenha o fundo — o texto sai por
 * {@code extractDefaultLabel} —, entao cancelar aqui nao afeta o rotulo nem o
 * comportamento do clique. Cobre Button.Plain, CycleButton e SpriteIconButton.
 */
@Mixin(AbstractButton.class)
public class AbstractButtonMixin {
	@Inject(method = "extractDefaultSprite", at = @At("HEAD"), cancellable = true)
	private void bedrockux$drawFlatButton(GuiGraphicsExtractor context, CallbackInfo callbackInfo) {
		if (!BedrockUX.config().buttons.enabled) {
			return;
		}

		AbstractWidget widget = (AbstractWidget) (Object) this;

		BedrockTheme.drawButton(context, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(),
				widget.isHoveredOrFocused(), widget.active,
				((AbstractWidgetAccessor) widget).bedrockux$getAlpha());

		callbackInfo.cancel();
	}
}
