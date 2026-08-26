package com.bedrockux.ext;

/**
 * Reposiciona um slot a partir da posicao original do vanilla.
 *
 * <p>Implementado por um mixin em {@code Slot}. Vive fora do pacote {@code mixin} porque
 * o Mixin e dono daquele pacote e proibe referencia direta as classes dele. Existe para o espacamento ser <b>idempotente</b>:
 * o menu do inventario vive no jogador e sobrevive a cada abertura da tela, entao aplicar o
 * fator sobre a posicao corrente iria compondo a cada vez.
 */
public interface SpacedSlot {
	/** Move o slot para {@code original * factor}. Chamar de novo nao acumula. */
	void bedrockux$applySpacing(float factor);
}
