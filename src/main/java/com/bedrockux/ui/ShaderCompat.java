package com.bedrockux.ui;

import com.bedrockux.BedrockUX;

import java.lang.reflect.Method;

/**
 * Descobre se ha um shader pack ativo, sem criar dependencia de compilacao com o Iris.
 *
 * <p>Existe por causa de um problema concreto: com shader pack ligado, o modelo 3D do
 * jogador na tela inicial sai deformado. O desenho passa pelo
 * {@code PictureInPictureRenderer}, que renderiza a entidade pelo caminho do mundo — e na
 * tela inicial nao ha mundo nenhum, entao o shader transforma os vertices com dados que
 * nunca foram preenchidos. Dentro de um mundo o mesmo caminho funciona, que e por que a
 * Paper Doll nao sofre disso.
 *
 * <p>A consulta e por reflexao para o mod continuar funcionando sem o Iris instalado.
 */
public final class ShaderCompat {
	private static final String IRIS_API = "net.irisshaders.iris.api.v0.IrisApi";

	private static boolean resolved;
	private static Object irisInstance;
	private static Method inUseMethod;

	private ShaderCompat() {
	}

	/** {@code true} quando ha um shader pack carregado neste momento. */
	public static boolean isShaderPackActive() {
		if (!resolved) {
			resolve();
		}

		if (inUseMethod == null) {
			return false;
		}

		try {
			return (Boolean) inUseMethod.invoke(irisInstance);
		} catch (ReflectiveOperationException | ClassCastException e) {
			// Uma versao do Iris com API diferente nao deve derrubar a tela inicial.
			BedrockUX.LOGGER.warn("Nao foi possivel consultar o estado do shader pack.", e);
			inUseMethod = null;
			return false;
		}
	}

	private static void resolve() {
		resolved = true;

		try {
			Class<?> api = Class.forName(IRIS_API);
			irisInstance = api.getMethod("getInstance").invoke(null);
			inUseMethod = api.getMethod("isShaderPackInUse");
		} catch (ReflectiveOperationException e) {
			// Iris ausente: caminho normal, sem shaders para detectar.
			inUseMethod = null;
		}
	}
}
