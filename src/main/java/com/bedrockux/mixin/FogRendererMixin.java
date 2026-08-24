package com.bedrockux.mixin;

import com.bedrockux.BedrockUX;
import com.bedrockux.config.BedrockUXConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * UI-08 (parte 1): a nevoa do Bedrock comeca mais perto do jogador e fecha mais rapido.
 *
 * <p>O ajuste e multiplicativo em cima do que o jogo calculou, nao um valor fixo: assim
 * bioma, clima, distancia de renderizacao e efeitos como cegueira continuam mandando na
 * nevoa, e o mod so aperta a curva.
 *
 * <p>{@code skyEnd} e {@code cloudEnd} ficam intactos de proposito — mexer neles desloca o
 * horizonte e as nuvens, que no Bedrock nao acompanham a nevoa proxima.
 *
 * <p><b>Prioridade.</b> O Sodium tambem entra em {@code FogRenderer} para ler os parametros
 * e alimentar o proprio renderizador. A prioridade abaixo do padrao (1000) faz este Mixin
 * ser aplicado antes, de modo que o Sodium leia os valores ja ajustados em vez dos
 * originais. Sem isso a nevoa mudaria sem Sodium e ficaria vanilla com ele.
 */
@Mixin(value = FogRenderer.class, priority = 900)
public class FogRendererMixin {
	@Inject(method = "setupFog", at = @At("RETURN"))
	private void bedrockux$tightenFog(Camera camera, int renderDistance, DeltaTracker deltaTracker,
			float partialTick, ClientLevel level, CallbackInfoReturnable<FogData> callbackInfo) {
		BedrockUXConfig.Fog config = BedrockUX.config().fog;

		if (!config.enabled) {
			return;
		}

		FogData data = callbackInfo.getReturnValue();

		if (data == null) {
			return;
		}

		data.environmentalStart *= config.startMultiplier;
		data.renderDistanceStart *= config.startMultiplier;
		data.environmentalEnd *= config.endMultiplier;
		data.renderDistanceEnd *= config.endMultiplier;
	}
}
