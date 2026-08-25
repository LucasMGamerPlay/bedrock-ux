package com.bedrockux.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Torna a posicao do slot gravavel.
 *
 * <p>{@code Slot.x} e {@code Slot.y} sao finais no vanilla. Mexer neles e o unico jeito de
 * espacar a grade sem quebrar o clique: tanto o desenho quanto a deteccao do slot sob o
 * cursor saem dessas mesmas coordenadas, entao mudar as duas coisas de uma vez mantem o
 * inventario coerente.
 */
@Mixin(Slot.class)
public interface SlotAccessor {
	@Mutable
	@Accessor("x")
	void bedrockux$setX(int x);

	@Mutable
	@Accessor("y")
	void bedrockux$setY(int y);
}
