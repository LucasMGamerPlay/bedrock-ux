package com.bedrockux.hud;

import com.bedrockux.BedrockUX;
import com.bedrockux.config.BedrockUXConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Pose;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * UI-01: a "Paper Doll" do Bedrock — miniatura 3D do jogador no canto da tela.
 *
 * <p>O 26.2 renderiza GUI em duas etapas: aqui so montamos um {@code EntityRenderState}
 * e o entregamos a {@code GuiGraphicsExtractor.entity}, que desenha depois, no passo de
 * picture-in-picture. Toda a animacao (andar, nadar, elytra, agachar) ja vem pronta de
 * {@code EntityRenderer.createRenderState} — nao precisamos reproduzir nada disso a mao.
 *
 * <p>A unica correcao necessaria e de orientacao. O render state traz o yaw do corpo em
 * coordenadas do mundo, o que faria o boneco girar junto com a camera. Tornando o
 * {@code bodyRot} relativo ao yaw da cabeca, o boneco para de girar e passa a mostrar so a
 * diferenca real entre corpo e cabeca — o comportamento do Bedrock.
 */
public final class PaperDollHudElement implements HudElement {
	/** {@code bodyRot} 180 deixa a entidade de frente para quem olha — a vista do Bedrock. */
	private static final float FRONT_FACING = 180.0F;

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, DeltaTracker deltaTracker) {
		BedrockUXConfig.PaperDoll config = BedrockUX.config().paperDoll;

		// O boneco e o topo da pilha do canto superior esquerdo: reinicia o cursor do frame
		// antes de qualquer desvio, senao as coordenadas leem lixo do frame anterior.
		HudLayout.resetTopLeft(config.offsetY);

		if (!config.enabled) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;

		if (player == null || minecraft.level == null || minecraft.gui.hud.isHidden()) {
			return;
		}

		if (config.hideWithDebugScreen && minecraft.gui.hud.getDebugOverlay().showDebugScreen()) {
			return;
		}

		float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
		EntityRenderState state = captureRenderState(minecraft, player, partialTick);

		if (!(state instanceof LivingEntityRenderState living)) {
			return;
		}

		orientTowardsCamera(living, player, partialTick, config);

		if (config.uprightWhileFlying) {
			keepUpright(living);
		}

		normalizeScale(living);

		int x0 = config.offsetX;
		int y0 = config.offsetY;
		int x1 = x0 + config.width;
		int y1 = y0 + config.height;

		Quaternionf cameraAngle = new Quaternionf().rotateX(config.tiltDegrees * Mth.DEG_TO_RAD);
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(cameraAngle);
		Vector3f translation = new Vector3f(0.0F, living.boundingBoxHeight / 2.0F, 0.0F);

		context.entity(living, config.scale, translation, rotation, cameraAngle, x0, y0, x1, y1);

		HudLayout.advanceTopLeft(config.height + config.gap);
	}

	/**
	 * Monta o render state do jogador. Sombra e contorno saem fora: dentro da GUI eles
	 * aparecem como artefatos soltos (mesmo tratamento que o inventario faz).
	 */
	private static EntityRenderState captureRenderState(Minecraft minecraft, LocalPlayer player, float partialTick) {
		EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
		EntityRenderer<? super LocalPlayer, ?> renderer = dispatcher.getRenderer(player);
		EntityRenderState state = renderer.createRenderState(player, partialTick);

		state.shadowPieces.clear();
		state.outlineColor = EntityRenderState.NO_OUTLINE;
		return state;
	}

	/**
	 * Converte a rotacao de coordenadas do mundo para coordenadas da camera.
	 *
	 * <p>{@code yRot} guarda o yaw da cabeca <em>relativo ao corpo</em>, entao ele continua
	 * valido depois de mexermos no corpo e nao deve ser tocado. Ja o {@code xRot} vem com o
	 * pitch real da camera, que chega a 90 graus e deita a cabeca — dentro de uma caixa
	 * pequena isso vira uma mancha, entao ele e limitado.
	 */
	private static void orientTowardsCamera(LivingEntityRenderState living, LocalPlayer player,
			float partialTick, BedrockUXConfig.PaperDoll config) {
		float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
		float relativeBodyYaw = Mth.wrapDegrees(living.bodyRot - headYaw);

		// O vanilla gira o modelo por `180 - bodyRot`, ou seja, o angulo na tela e o inverso
		// do campo. Por isso o giro desejado entra subtraindo: somar mandaria o boneco para
		// o lado oposto. A subtracao tambem espelha o offset corpo/cabeca, que e o que se ve
		// de uma camera posicionada na frente do jogador.
		living.bodyRot = FRONT_FACING - (config.yawOffsetDegrees + relativeBodyYaw);
		living.xRot = Mth.clamp(living.xRot, -config.headPitchLimit, config.headPitchLimit);
	}

	/**
	 * Desfaz as rotacoes que deitam o modelo na horizontal.
	 *
	 * <p>Em voo com elytra o {@code AvatarRenderer} aplica
	 * {@code XP.rotationDegrees(fallFlyingScale * (-90 - xRot))}; nado e tridente tem
	 * transformacoes equivalentes. Na tela cheia isso e o certo, mas numa caixa de 44x66 o
	 * modelo deitado nao cabe e sai cortado. Neutralizar as flags e mais barato e mais
	 * estavel do que tentar reenquadrar a caixa a cada pose.
	 */
	private static void keepUpright(LivingEntityRenderState living) {
		living.isAutoSpinAttack = false;

		// Dormindo o vanilla pula a rotacao do corpo por completo, e o boneco travaria virado.
		if (living.pose == Pose.SLEEPING) {
			living.pose = Pose.STANDING;
		}

		if (living instanceof HumanoidRenderState humanoid) {
			humanoid.isFallFlying = false;
			humanoid.isVisuallySwimming = false;
			humanoid.swimAmount = 0.0F;
		}

		if (living instanceof AvatarRenderState avatar) {
			avatar.shouldApplyFlyingYRot = false;
		}
	}

	/**
	 * Neutraliza a escala da entidade dobrando-a na caixa delimitadora, para que o
	 * parametro de escala da GUI seja a unica coisa que define o tamanho na tela.
	 */
	private static void normalizeScale(LivingEntityRenderState living) {
		living.boundingBoxWidth /= living.scale;
		living.boundingBoxHeight /= living.scale;
		living.scale = 1.0F;
	}
}
