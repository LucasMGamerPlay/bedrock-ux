package com.bedrockux.mixin;

import com.bedrockux.BedrockUX;
import com.bedrockux.ui.BedrockTheme;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inventario com o painel e os slots do Bedrock no lugar da textura do vanilla.
 *
 * <p>A substituicao e cirurgica: troca apenas a chamada de {@code blit} que desenha a
 * textura de fundo. O livro de receitas e o modelo do jogador continuam sendo desenhados
 * pelo vanilla, antes e depois dessa chamada — cancelar o metodo inteiro levaria os dois
 * junto.
 *
 * <p>A grade e espacada mexendo direto no {@code Slot}: o desenho e a deteccao do slot sob
 * o cursor saem das mesmas coordenadas, entao mover as duas coisas de uma vez mantem o
 * clique alinhado com o que aparece na tela.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
	/** Retangulo onde o vanilla desenha o jogador, em coordenadas relativas ao painel. */
	private static final int MODEL_LEFT = 26;

	private static final int MODEL_TOP = 8;

	private static final int MODEL_RIGHT = 75;

	private static final int MODEL_BOTTOM = 78;

	private static final int SLOT_SIZE = 18;

	/** Vao entre a grade de fabricacao e o slot de resultado, relativo ao painel. */
	private static final int ARROW_X = 136;

	private static final int ARROW_Y = 37;

	private static final int ARROW_LENGTH = 16;

	private static final int ARROW_THICKNESS = 4;

	/** Folga entre o topo do painel e a fileira de botoes do Bedrock. */
	private static final int BUTTON_GAP = 3;

	private static final int BUTTON_WIDTH = 20;

	private static final int BUTTON_HEIGHT = 18;


	/** Passo entre slots no vanilla. O Bedrock deixa folga entre eles. */
	private static final int VANILLA_PITCH = 18;

	@Unique
	private float bedrockux$spacingFactor = 1.0F;

	@Unique
	private boolean bedrockux$slotsSpaced;

	/**
	 * Espaca a grade antes de a tela calcular a propria posicao.
	 *
	 * <p>Roda no HEAD do {@code init} de proposito: o {@code leftPos} e o {@code topPos} sao
	 * derivados de {@code imageWidth} e {@code imageHeight}, entao crescer o painel depois
	 * deixaria tudo fora de centro.
	 *
	 * <p>Os slots pertencem ao menu, que sobrevive a um redimensionamento — por isso a
	 * marcacao, para o espacamento nao ser aplicado em cima de si mesmo e a grade nao sair
	 * crescendo a cada resize.
	 */
	@Inject(method = "init", at = @At("HEAD"))
	private void bedrockux$spaceSlots(CallbackInfo callbackInfo) {
		if (this.bedrockux$slotsSpaced || !BedrockUX.config().inventory.enabled) {
			return;
		}

		this.bedrockux$slotsSpaced = true;

		int pitch = BedrockUX.config().inventory.slotPitch;
		this.bedrockux$spacingFactor = pitch / (float) VANILLA_PITCH;

		if (this.bedrockux$spacingFactor == 1.0F) {
			return;
		}

		// Escala uniforme: mantem a estrutura do vanilla — coluna de armadura, modelo,
		// fabricacao, grade e barra rapida — e so abre folga entre os slots.
		for (Slot slot : ((AbstractContainerScreen<?>) (Object) this).getMenu().slots) {
			SlotAccessor accessor = (SlotAccessor) slot;
			accessor.bedrockux$setX(Math.round(slot.x * this.bedrockux$spacingFactor));
			accessor.bedrockux$setY(Math.round(slot.y * this.bedrockux$spacingFactor));
		}

		ContainerScreenAccessor screen = (ContainerScreenAccessor) this;
		screen.bedrockux$setImageWidth(Math.round(screen.bedrockux$getImageWidth() * this.bedrockux$spacingFactor));
		screen.bedrockux$setImageHeight(Math.round(screen.bedrockux$getImageHeight() * this.bedrockux$spacingFactor));
	}

	@Redirect(
			method = "extractBackground",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit("
							+ "Lcom/mojang/blaze3d/pipeline/RenderPipeline;"
							+ "Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
	private void bedrockux$replacePanel(GuiGraphicsExtractor context, RenderPipeline pipeline, Identifier texture,
			int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		if (!BedrockUX.config().inventory.enabled) {
			context.blit(pipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);
			return;
		}

		BedrockTheme.drawPanel(context, x, y, width, height);

		// A caixa escura atras do jogador vem antes do modelo, que o vanilla desenha logo
		// depois desta chamada.
		float factor = this.bedrockux$spacingFactor;
		context.fill(x + Math.round(MODEL_LEFT * factor), y + Math.round(MODEL_TOP * factor),
				x + Math.round(MODEL_RIGHT * factor), y + Math.round(MODEL_BOTTOM * factor),
				BedrockTheme.MODEL_BOX);

		BedrockTheme.drawCraftArrow(context, x + Math.round(ARROW_X * factor), y + Math.round(ARROW_Y * factor),
				Math.round(ARROW_LENGTH * factor), ARROW_THICKNESS);

		// getMenu() vive em AbstractContainerScreen e devolve o tipo generico, nao
		// InventoryMenu — por isso o cast em vez de @Shadow.
		for (Slot slot : ((AbstractContainerScreen<?>) (Object) this).getMenu().slots) {
			BedrockTheme.drawSlot(context, x + slot.x - 1, y + slot.y - 1, SLOT_SIZE);
		}
	}

	/**
	 * Leva o botao do livro de receitas para cima do painel, encostado a direita.
	 *
	 * <p>No Bedrock os botoes nao ficam dentro do painel: eles formam uma fileira sobre a
	 * borda superior, no canto direito. Este e o unico botao equivalente que a tela do Java
	 * tem, entao ele ocupa esse lugar.
	 */
	@Inject(method = "getRecipeBookButtonPosition", at = @At("HEAD"), cancellable = true)
	private void bedrockux$moveRecipeBookButton(CallbackInfoReturnable<ScreenPosition> callback) {
		if (!BedrockUX.config().inventory.enabled) {
			return;
		}

		ContainerScreenAccessor size = (ContainerScreenAccessor) this;

		callback.setReturnValue(new ScreenPosition(
				size.bedrockux$getLeftPos() + size.bedrockux$getImageWidth() - BUTTON_WIDTH,
				size.bedrockux$getTopPos() - BUTTON_HEIGHT - BUTTON_GAP));
	}
}
