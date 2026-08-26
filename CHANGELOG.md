# Changelog

Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/).
Este projeto usa [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [0.1.1] — 2026-08-25

### Corrigido

- **A grade do inventário crescia a cada abertura.** O menu do inventário vive no jogador e
  é reusado toda vez que a tela abre, mas a tela é construída do zero. O espaçamento
  multiplicava a posição corrente do slot pelo fator a cada abertura, então a grade ia
  compondo enquanto o painel voltava ao tamanho do vanilla — depois de algumas aberturas os
  slots apareciam fora do painel. A posição de fábrica agora é guardada e o espaçamento
  sempre deriva dela.

## [0.1.0] — 2026-08-24

Primeira versão pública. Mod **client-side**: não precisa estar no servidor, e
funciona em qualquer servidor sem alteração.

### Adicionado

**HUD**

- **Paper Doll** (UI-01) — boneco do jogador no canto da tela, de frente e com o
  corpo acompanhando a direção do movimento. Deita junto ao usar elytra, com o
  eixo de rotação no meio do corpo, como no Bedrock.
- **Coordenadas** (UI-02) — posição no formato do Bedrock, abaixo do boneco.

**Telas**

- **Menu principal** (UI-04) — coluna única centralizada, botões de ícone no
  canto inferior esquerdo e modelo 3D do jogador com o nome acima.
- **Cabeça articulada** — a cabeça do modelo segue o cursor separada do corpo,
  com o quanto o corpo acompanha ajustável em `bodyFollowFactor`.
- **Menu de pausa** — botões empurrados para o canto esquerdo mantendo o arranjo
  original, logo acima deles e modelo do jogador à direita.
- **Inventário** — painel, slots e seta de fabricação no estilo Bedrock, com
  folga entre os slots e o botão do livro de receitas acima do painel.
- **Transições de tela** (UI-05) e **tela de carregamento** (UI-07), com ícone
  animado de salvamento.

**Outros**

- **Sons de UI** (UI-06) — clique nos botões.
- **Paleta e botões do Bedrock** em todas as telas, com as cores amostradas
  pixel a pixel das capturas de referência.
- **Configuração por JSON**, com cada feature podendo ser desligada
  individualmente.

### Contornado

- **Modelo da tela inicial com shader pack** — com um shader ativo o modelo 3D
  do menu aparece deformado. O mod detecta o Iris e troca o modelo pelo rosto da
  skin em 2D, com piscada. Veja o aviso no README; desligue em
  `titleScreen.hideModelWithShaders`.

### Fora de escopo nesta versão

- **Margens do chat** (UI-03) — o comportamento do Java atual já equivale.
- **Água** (parte do UI-08) — já é nativo desde as versões modernas.
- **Névoa** (parte do UI-08) — ajuste opcional, ainda não validado com Sodium.
- **Inventário criativo** — só o de sobrevivência foi trabalhado.

[0.1.1]: https://github.com/LucasMGamerPlay/bedrock-ux/releases/tag/v0.1.1
[0.1.0]: https://github.com/LucasMGamerPlay/bedrock-ux/releases/tag/v0.1.0
