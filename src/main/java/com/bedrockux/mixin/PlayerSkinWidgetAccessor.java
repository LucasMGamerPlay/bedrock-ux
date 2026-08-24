package com.bedrockux.mixin;

import net.minecraft.client.model.Model;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Expoe a rotacao do modelo, que o vanilla so deixa mudar arrastando com o mouse.
 *
 * <p>No menu do Bedrock o personagem acompanha o cursor sozinho, sem precisar arrastar.
 */
@Mixin(PlayerSkinWidget.class)
public interface PlayerSkinWidgetAccessor {
	@Accessor("rotationX")
	void bedrockux$setRotationX(float rotationX);

	@Accessor("rotationY")
	void bedrockux$setRotationY(float rotationY);

	@Accessor("wideModel")
	Model.Simple bedrockux$getWideModel();

	@Accessor("slimModel")
	Model.Simple bedrockux$getSlimModel();
}
