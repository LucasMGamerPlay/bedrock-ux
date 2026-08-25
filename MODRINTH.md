<!--
  Corpo da página do Modrinth. Colar no campo "Description" do projeto.
  Não faz parte do mod — é material de publicação.

  Bilíngue: o Modrinth aceita um único corpo de descrição, então o inglês vem
  primeiro (público maior) e o português logo abaixo.

  O restante do formulario (Summary, categorias, icone) esta em PUBLICACAO.md.
-->

**English** · [Português](#português)

# Bedrock UX

**The Minecraft Bedrock interface, on Java Edition.**

A **client-side** mod: it does not need to be on the server, and works on any server
unchanged. No new content, no gameplay advantage — just the GUI.

## What it does

### On the HUD

- **Paper Doll** — the player model in the corner of the screen, facing you, with the body
  turning to follow your direction of movement. It goes horizontal when you fly with an
  elytra, pivoting around the middle of the body like Bedrock does.
- **Coordinates** in Bedrock's format, right below the model.

### On the screens

- **Main menu** with a single centered column, icon buttons in the bottom-left corner, and
  the 3D player model with the name above it. **The head follows your cursor independently
  of the body.**
- **Pause menu** with the buttons pushed to the left corner, the logo above them, and the
  player model on the right.
- **Inventory** with Bedrock's panel, slots, and crafting arrow.
- **Screen transitions** and a **loading screen** with Bedrock's panel and animated saving
  icon.
- **Button click sounds.**

Every color was sampled pixel by pixel from Bedrock screenshots, not eyeballed.

## ⚠️ Warning: shader packs (Iris)

**With a shader pack active, the 3D player model on the main menu renders distorted.** The
mod detects this and swaps the model for a **2D face from your skin**, with a blink.

This is a **workaround, not a fix** — the cause is outside the mod's reach. The menu model
is not drawn as regular GUI: it goes through the *entity* rendering path, which the shader
pack replaces and which expects world data (camera, lighting, normals). There is no world
on the title screen, so the shader transforms the vertices using data that was never
filled in.

Two side effects of this that tend to confuse people:

- **Entering a world and coming back "fixes" it** — by then Iris has valid state cached.
- **The HUD Paper Doll never suffers from this**, because it runs inside a world.

The rest of the screens are unaffected: they are plain GUI drawing, which the shader does
not replace.

If you would rather see the 3D model even with a shader active, turn off
`titleScreen.hideModelWithShaders` in the config.

## Requirements

| | |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | required |
| Java | **25** |

> **Java 25 is not optional.** It is what Minecraft 26.2 itself requires.

## Configuration

Everything is adjustable in `config/bedrockux.json`, created on first launch. **Each
feature can be turned off individually** through its section's `enabled` flag — you can run
just the Paper Doll, or just the screens, without taking the rest.

## Compatibility

- **Servers:** any. The mod is client-side only.
- **Iris / shader packs:** works, with the main-menu model caveat described above.
- **Sodium:** the screens and the HUD work. The optional fog tweak (off by default) has not
  been validated with Sodium yet.

## Not in scope yet

The creative inventory, and the fog tweak that is still unvalidated. Water and chat margins
were dropped from the plan because modern Java already handles both natively.

## Source

[GitHub](https://github.com/LucasMGamerPlay/bedrock-ux) · MIT license · Bug reports on
[Issues](https://github.com/LucasMGamerPlay/bedrock-ux/issues).

---

<a name="português"></a>

[English](#bedrock-ux) · **Português**

# Bedrock UX

**A interface do Minecraft Bedrock, na Java Edition.**

Mod **client-side**: não precisa estar no servidor, e funciona em qualquer servidor sem
alteração. Nada de novo conteúdo, nada de vantagem em jogo — só a GUI.

## O que ele faz

### No HUD

- **Paper Doll** — o boneco do jogador no canto da tela, de frente, com o corpo
  acompanhando a direção do movimento. Deita junto ao usar elytra, girando pelo meio do
  corpo como no Bedrock.
- **Coordenadas** no formato do Bedrock, logo abaixo do boneco.

### Nas telas

- **Menu principal** com coluna única centralizada, botões de ícone no canto inferior
  esquerdo e o modelo 3D do jogador com o nome acima. **A cabeça segue o cursor separada
  do corpo.**
- **Menu de pausa** com os botões no canto esquerdo, logo acima deles e o modelo do
  jogador à direita.
- **Inventário** com o painel, os slots e a seta de fabricação do Bedrock.
- **Transições de tela** e **tela de carregamento** com o painel e o ícone animado de
  salvamento.
- **Sons de clique** nos botões.

Todas as cores foram amostradas pixel a pixel de capturas do Bedrock, não estimadas.

## ⚠️ Aviso: shader packs (Iris)

**Com um shader pack ativo, o modelo 3D do jogador no menu principal aparece deformado.**
O mod detecta isso e troca o modelo pelo **rosto da skin em 2D**, com piscada.

Isso é um **contorno, não um conserto** — a causa está fora do alcance do mod. O modelo do
menu não é desenhado como GUI comum: ele passa pelo caminho de renderização de *entidades*,
que o shader pack substitui e que espera dados do mundo (câmera, luz, normais). Na tela
inicial não existe mundo, então o shader transforma os vértices com dados nunca
preenchidos.

Dois efeitos disso que costumam confundir:

- **Entrar num mundo e voltar "conserta"** o modelo — aí o Iris já tem estado válido em
  cache.
- **A Paper Doll do HUD nunca sofre disso**, porque roda dentro de um mundo.

O resto das telas não é afetado: é desenho de GUI comum, que o shader não substitui.

Se preferir ver o modelo 3D mesmo com shader ativo, desligue
`titleScreen.hideModelWithShaders` na configuração.

## Requisitos

| | |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 ou mais novo |
| Fabric API | obrigatório |
| Java | **25** |

> **Java 25 não é opcional.** É o que o próprio Minecraft 26.2 exige.

## Configuração

Tudo é ajustável em `config/bedrockux.json`, criado no primeiro início. **Cada feature pode
ser desligada individualmente** pelo `enabled` da sua seção — dá para usar só a Paper Doll,
ou só as telas, sem levar o resto.

## Compatibilidade

- **Servidores:** qualquer um. O mod é só do cliente.
- **Iris / shader packs:** funciona, com a ressalva do modelo do menu descrita acima.
- **Sodium:** as telas e o HUD funcionam. O ajuste opcional de névoa (desligado por padrão)
  ainda não foi validado com o Sodium.

## Fora de escopo por enquanto

Inventário criativo, e o ajuste de névoa ainda não validado. Água e margens de chat saíram
do plano porque o Java atual já resolve os dois nativamente.

## Código

[GitHub](https://github.com/LucasMGamerPlay/bedrock-ux) · Licença MIT · Relatos de bug em
[Issues](https://github.com/LucasMGamerPlay/bedrock-ux/issues).
