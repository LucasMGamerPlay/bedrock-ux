package com.bedrockux.mixin;

import com.bedrockux.BedrockUX;
import com.bedrockux.config.BedrockUXConfig;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * UI-06: o clique de madeira do Java vira o toque curto e agudo do Bedrock.
 *
 * <p>O mod <em>reafina</em> o som do vanilla em vez de trazer um arquivo proprio. Nao ha
 * como sintetizar um OGG aqui, e distribuir o audio do Bedrock seria copiar asset da Mojang.
 * Subir o tom e baixar o volume aproxima bem do original, e quem quiser o som exato pode
 * substituir {@code minecraft:ui.button.click} por um resource pack — este Mixin continua
 * respeitando o arquivo que estiver no lugar.
 */
@Mixin(AbstractWidget.class)
public class ButtonSoundMixin {
	@Inject(method = "playButtonClickSound", at = @At("HEAD"), cancellable = true)
	private static void bedrockux$retuneClick(SoundManager soundManager, CallbackInfo callbackInfo) {
		BedrockUXConfig.Sounds config = BedrockUX.config().sounds;

		if (!config.enabled) {
			return;
		}

		soundManager.play(SimpleSoundInstance.forUI(
				SoundEvents.UI_BUTTON_CLICK.value(), config.clickPitch, config.clickVolume));

		callbackInfo.cancel();
	}
}
