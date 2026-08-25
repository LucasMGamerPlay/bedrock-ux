package com.bedrockux.ui;

import com.bedrockux.mixin.PlayerSkinWidgetAccessor;
import net.minecraft.client.gui.components.PlayerSkinWidget;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.util.Mth;

/**
 * Comportamento compartilhado do modelo do jogador nas telas de menu.
 *
 * <p>Reune o que o menu principal e o de pausa fazem igual: devolver as partes a pose
 * original a cada frame e dividir o giro entre corpo e cabeca.
 */
public final class PlayerModelPreview {
	private static final float YAW_LIMIT = 40.0F;
	private static final float PITCH_LIMIT = 25.0F;

	private PlayerModelPreview() {
	}

	/**
	 * Atualiza a pose e a rotacao do modelo para o frame atual.
	 *
	 * @param bodyShare fracao do giro que o corpo absorve; o resto vai para a cabeca
	 */
	public static void update(PlayerSkinWidget model, int screenWidth, int screenHeight,
			int mouseX, int mouseY, boolean followMouse, float bodyShare) {
		PlayerSkinWidgetAccessor accessor = (PlayerSkinWidgetAccessor) model;

		// Model.Simple nao anima nada, entao as partes ficam com a ultima transformacao que
		// alguem tiver deixado nelas. Sem este reset, mods que substituem modelos de entidade
		// deixam o boneco torto ate a primeira entrada num mundo.
		resetPose(accessor.bedrockux$getWideModel());
		resetPose(accessor.bedrockux$getSlimModel());

		if (!followMouse || screenWidth <= 0 || screenHeight <= 0) {
			return;
		}

		float centerX = model.getX() + model.getWidth() / 2.0F;
		float centerY = model.getY() + model.getHeight() / 2.0F;

		float yaw = normalizedOffset(mouseX, centerX, screenWidth) * YAW_LIMIT;
		float pitch = normalizedOffset(mouseY, centerY, screenHeight) * PITCH_LIMIT;

		if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) {
			return;
		}

		float share = Mth.clamp(bodyShare, 0.0F, 1.0F);
		float bodyYaw = yaw * share;
		float bodyPitch = pitch * share;

		accessor.bedrockux$setRotationY(bodyYaw);
		accessor.bedrockux$setRotationX(-bodyPitch);

		// O yaw da cabeca entra negado: o modelo e exibido de frente, ou seja, girado 180
		// graus em relacao ao proprio "para frente", entao um giro no espaco do modelo
		// aparece espelhado na tela. O pitch nao sofre disso.
		rotateHead(accessor.bedrockux$getWideModel(), -(yaw - bodyYaw), pitch - bodyPitch);
		rotateHead(accessor.bedrockux$getSlimModel(), -(yaw - bodyYaw), pitch - bodyPitch);
	}

	/**
	 * Posicao do cursor em relacao ao modelo, de -1 a 1, medindo cada lado pelo espaco que
	 * realmente existe ali. Assim o giro chega ao maximo nos dois sentidos mesmo com o modelo
	 * fora do centro da tela.
	 */
	private static float normalizedOffset(float value, float center, float extent) {
		float delta = value - center;
		float available = delta < 0.0F ? center : extent - center;

		if (available <= 0.0F) {
			return 0.0F;
		}

		return Mth.clamp(delta / available, -1.0F, 1.0F);
	}

	private static void resetPose(Model.Simple model) {
		if (model == null) {
			return;
		}

		for (ModelPart part : model.root().getAllParts()) {
			part.resetPose();
		}
	}

	private static void rotateHead(Model.Simple model, float yawDegrees, float pitchDegrees) {
		if (model == null) {
			return;
		}

		ModelPart root = model.root();

		if (!root.hasChild(PartNames.HEAD)) {
			return;
		}

		ModelPart head = root.getChild(PartNames.HEAD);
		head.yRot = yawDegrees * Mth.DEG_TO_RAD;
		head.xRot = pitchDegrees * Mth.DEG_TO_RAD;

		if (root.hasChild(PartNames.HAT)) {
			ModelPart hat = root.getChild(PartNames.HAT);
			hat.yRot = head.yRot;
			hat.xRot = head.xRot;
		}
	}
}
