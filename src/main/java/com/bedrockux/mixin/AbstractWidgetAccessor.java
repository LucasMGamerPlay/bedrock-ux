package com.bedrockux.mixin;

import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Expoe {@code AbstractWidget.alpha}, que e protected e controla o fade da tela inicial. */
@Mixin(AbstractWidget.class)
public interface AbstractWidgetAccessor {
	@Accessor("alpha")
	float bedrockux$getAlpha();
}
