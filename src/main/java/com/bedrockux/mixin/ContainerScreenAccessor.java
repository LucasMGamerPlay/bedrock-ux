package com.bedrockux.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Permite crescer o painel junto com a grade espacada. */
@Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccessor {
	@Accessor("leftPos")
	int bedrockux$getLeftPos();

	@Accessor("topPos")
	int bedrockux$getTopPos();

	@Accessor("imageWidth")
	int bedrockux$getImageWidth();

	@Accessor("imageHeight")
	int bedrockux$getImageHeight();

	@Mutable
	@Accessor("imageWidth")
	void bedrockux$setImageWidth(int width);

	@Mutable
	@Accessor("imageHeight")
	void bedrockux$setImageHeight(int height);
}
