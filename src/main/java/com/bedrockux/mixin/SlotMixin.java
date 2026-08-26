package com.bedrockux.mixin;

import com.bedrockux.ext.SpacedSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Guarda a posicao de fabrica do slot e reposiciona a partir dela.
 *
 * <p>{@code x} e {@code y} sao finais no vanilla — {@code @Mutable} os abre. Mexer neles e o
 * unico jeito de espacar a grade sem quebrar o clique: tanto o desenho quanto a deteccao do
 * slot sob o cursor saem dessas mesmas coordenadas.
 *
 * <p>A posicao original e capturada na primeira chamada e nunca mais muda, entao o
 * espacamento pode ser reaplicado a vontade. Isso importa porque o menu do inventario vive
 * no jogador e e reusado a cada abertura da tela: multiplicar a posicao corrente faria a
 * grade crescer um pouco mais a cada vez, ate os slots saírem de dentro do painel.
 */
@Mixin(Slot.class)
public abstract class SlotMixin implements SpacedSlot {
	@Mutable
	@Shadow
	public int x;

	@Mutable
	@Shadow
	public int y;

	/** {@code MIN_VALUE} marca "ainda nao capturado" — 0 e uma posicao valida. */
	@Unique
	private int bedrockux$originalX = Integer.MIN_VALUE;

	@Unique
	private int bedrockux$originalY;

	@Override
	public void bedrockux$applySpacing(float factor) {
		if (this.bedrockux$originalX == Integer.MIN_VALUE) {
			this.bedrockux$originalX = this.x;
			this.bedrockux$originalY = this.y;
		}

		this.x = Math.round(this.bedrockux$originalX * factor);
		this.y = Math.round(this.bedrockux$originalY * factor);
	}
}
