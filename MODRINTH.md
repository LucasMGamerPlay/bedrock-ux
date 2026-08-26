<!--
  Corpo da página do Modrinth. Colar no campo "Description" do projeto.
  Não faz parte do mod — é material de publicação.

  REGRAS DE CONTEÚDO (a primeira submissão foi recusada pelas duas):

  - 2.2 Acessibilidade: NUNCA usar "# " (H1). O Modrinth já exibe o nome do
    projeto acima da descrição, e leitores de tela interpretam cabeçalho de
    forma diferente. Cabeçalho só para separar seção, começando em "## ".
    Para dar ênfase, negrito — nunca cabeçalho.

  - 2.1 Descrição suficiente: precisa dizer o que o mod adiciona, quais os
    recursos e por que alguém baixaria. Detalhar, não resumir.

  FALTA AINDA: imagens. Mod visual sem captura de tela costuma ser recusado
  de novo. Ver PUBLICACAO.md.

  O restante do formulário (Summary, categorias, ícone) está em PUBLICACAO.md.
-->

**English** · [Português](#português)

**Bedrock UX rebuilds the Minecraft Bedrock Edition interface inside Java Edition.**

If you came to Java from Bedrock — from a console, a phone, or the Windows 10 edition — the
menus, the HUD and the inventory all look and behave differently here. This mod brings that
interface back: the player model in the corner of your screen, Bedrock's button styling, its
main menu layout, its inventory panel, and its loading screens.

It is **client-side only**. It does not need to be installed on the server, it works on any
server you already play on, and it adds no items, blocks or gameplay advantage. It only
changes what you see.

Every color in this mod was **sampled pixel by pixel from real Bedrock screenshots**, not
guessed. The same goes for the layouts and the proportions.

## What it adds

### Paper Doll — the player model on your HUD

Bedrock shows a small model of your character in the corner of the screen. Java has never
had one. This mod adds it, with the details that make it feel right:

- The model **faces you**, while its body turns to follow the direction you are moving.
- Your **head turns on its own** as you look around, independently of the body.
- When you **fly with an elytra the model goes horizontal**, pivoting around the middle of
  the body — not standing upright, and not hinged at the feet.
- It shows your **real skin**, hat layer included.

### Coordinates in Bedrock's format

Your position, styled like Bedrock's and placed right below the Paper Doll instead of buried
in the debug screen. Facing direction and biome name are available as options.

### Main menu

A single centered column of buttons instead of Java's split arrangement, with the secondary
icon buttons moved to the bottom-left corner, the way Bedrock arranges them.

Your **3D player model stands beside the menu with your name above it**, and its **head
follows your cursor** independently of the body — so it looks at you as you move the mouse.

### Pause menu

The buttons move to the left corner keeping their original order, the game logo sits above
them, and your player model stands on the right, following your cursor the same way.

### Inventory

Bedrock's inventory panel: the lighter background, the recessed slot styling, the black box
behind your character, the crafting arrow, and real spacing between slots instead of Java's
flush grid. The recipe book button moves above the panel, where Bedrock keeps its button row.

### Loading screens and transitions

Bedrock's loading panel with its progress bar and rotating tips, an animated saving icon, and
smooth transitions between screens instead of Java's instant cuts.

### Interface sounds

Bedrock's click feedback on buttons.

### Bedrock button styling everywhere

Buttons across every screen get Bedrock's palette and border treatment, including its color
coding — green for confirming actions, purple for accents.

## Why you might want it

- You **switched from Bedrock to Java** and the interface feels foreign.
- You **play both editions** and want them to stop feeling like different games.
- You want a **Paper Doll**, which Java simply does not have — handy for checking your armor
  and your skin at a glance without opening the inventory.
- You want your **coordinates visible** without the entire debug screen in the way.

## Everything is optional

All settings live in `config/bedrockux.json`, created the first time you launch the game.

**Every feature can be switched off on its own** through the `enabled` flag in its section.
If you only want the Paper Doll, turn the rest off. If you want the screens but not the HUD,
do the opposite. Sizes, positions, opacity, animation speed and color behavior are adjustable
as well.

## Requirements

| | |
|---|---|
| Minecraft | 26.2 |
| Mod loader | Fabric 0.19.3 or newer |
| Fabric API | required |
| Java | **25** |

**Java 25 is not this mod's requirement — it is Minecraft 26.2's.** If the game runs, you
already have it.

## Compatibility

**Servers:** any of them. Nothing is installed server-side, and nothing this mod does is
visible to other players.

**Sodium:** the screens and the HUD work normally. The optional fog tweak is off by default
and has not been validated with Sodium yet.

**Iris and shader packs:** see the section below.

### Known issue: shader packs distort the main menu model

**With a shader pack active, the 3D player model on the main menu renders distorted.** The mod
detects this and swaps it for a **2D face taken from your skin**, which blinks. Every other
screen is unaffected.

This is a **workaround, not a fix**, and the reason is worth knowing if you are wondering
whether something is broken on your end.

The menu model is not drawn as ordinary GUI. It goes through the game's **entity rendering
path**, which shader packs replace, and which expects world data — camera, lighting, normals.
There is no world on the title screen, so the shader transforms the model using data that was
never filled in.

That also explains two things that look like bugs but are not:

- **Entering a world and coming back "fixes" the model** — by then Iris has valid state
  cached.
- **The HUD Paper Doll never has this problem**, because it always runs inside a world.

If you would rather see the 3D model anyway, set `titleScreen.hideModelWithShaders` to
`false`.

## Not included yet

- **The creative inventory** — only the survival one has been styled.
- **The fog tweak** — implemented, but off by default and still unvalidated with Sodium.

Two items from the original plan were dropped because modern Java already handles them
natively: **water rendering** and **chat margins**.

## Source and bug reports

The mod is open source under the **MIT license**.

[GitHub repository](https://github.com/LucasMGamerPlay/bedrock-ux) ·
[Report a bug](https://github.com/LucasMGamerPlay/bedrock-ux/issues) ·
[Changelog](https://github.com/LucasMGamerPlay/bedrock-ux/blob/main/CHANGELOG.md)

---

<a name="português"></a>

[English](#what-it-adds) · **Português**

**O Bedrock UX reconstrói a interface do Minecraft Bedrock dentro da Java Edition.**

Se você veio do Bedrock para o Java — de um console, do celular ou da edição Windows 10 — os
menus, o HUD e o inventário são diferentes aqui. Este mod traz aquela interface de volta: o
boneco do jogador no canto da tela, os botões no estilo do Bedrock, o layout do menu
principal, o painel do inventário e as telas de carregamento.

Ele é **client-side**. Não precisa estar instalado no servidor, funciona em qualquer servidor
onde você já joga, e não adiciona itens, blocos nem vantagem de jogo. Só muda o que você vê.

Todas as cores foram **amostradas pixel a pixel de capturas reais do Bedrock**, não
estimadas. O mesmo vale para os layouts e as proporções.

## O que ele adiciona

### Paper Doll — o boneco do jogador no HUD

O Bedrock mostra um bonequinho do seu personagem no canto da tela. O Java nunca teve isso. O
mod adiciona, com os detalhes que fazem diferença:

- O boneco fica **de frente para você**, enquanto o corpo gira acompanhando a direção do
  movimento.
- A **cabeça vira sozinha** conforme você olha em volta, separada do corpo.
- Ao **voar de elytra o boneco deita**, girando pelo meio do corpo — não fica em pé, nem
  preso pelos pés.
- Mostra a sua **skin de verdade**, incluindo a camada de chapéu.

### Coordenadas no formato do Bedrock

Sua posição no estilo do Bedrock, logo abaixo do boneco, em vez de escondida na tela de
debug. Direção e bioma são opcionais.

### Menu principal

Uma coluna única centralizada em vez do arranjo dividido do Java, com os botões de ícone no
canto inferior esquerdo, como o Bedrock organiza.

Seu **modelo 3D fica ao lado do menu com o nome acima**, e a **cabeça segue o cursor**
separada do corpo — ele olha para você conforme o mouse se move.

### Menu de pausa

Os botões vão para o canto esquerdo mantendo a ordem original, a logo fica acima deles, e o
modelo do jogador fica à direita, seguindo o cursor da mesma forma.

### Inventário

O painel do inventário do Bedrock: o fundo mais claro, os slots rebaixados, a caixa preta
atrás do personagem, a seta de fabricação, e espaço de verdade entre os slots em vez da grade
colada do Java. O botão do livro de receitas sobe para cima do painel, onde o Bedrock mantém
a fileira de botões.

### Telas de carregamento e transições

O painel de carregamento do Bedrock com barra de progresso e dicas rotativas, um ícone
animado de salvamento, e transições suaves entre telas em vez dos cortes secos do Java.

### Sons de interface

O retorno sonoro de clique do Bedrock nos botões.

### Botões no estilo Bedrock em todas as telas

Os botões de todas as telas ganham a paleta e as bordas do Bedrock, incluindo o código de
cores — verde para confirmar, roxo para destaque.

## Por que você ia querer

- Você **migrou do Bedrock para o Java** e a interface parece estranha.
- Você **joga as duas edições** e quer que parem de parecer jogos diferentes.
- Você quer um **Paper Doll**, que o Java simplesmente não tem — útil para conferir sua
  armadura e sua skin sem abrir o inventário.
- Você quer as **coordenadas visíveis** sem a tela de debug inteira atrapalhando.

## Tudo é opcional

As configurações ficam em `config/bedrockux.json`, criado no primeiro início do jogo.

**Cada recurso pode ser desligado sozinho** pelo `enabled` da sua seção. Se você só quer o
Paper Doll, desligue o resto. Se quer as telas mas não o HUD, faça o contrário. Tamanhos,
posições, opacidade, velocidade das animações e o comportamento das cores também são
ajustáveis.

## Requisitos

| | |
|---|---|
| Minecraft | 26.2 |
| Carregador | Fabric 0.19.3 ou mais novo |
| Fabric API | obrigatório |
| Java | **25** |

**O Java 25 não é exigência deste mod — é do próprio Minecraft 26.2.** Se o jogo abre, você
já tem.

## Compatibilidade

**Servidores:** qualquer um. Nada é instalado no servidor, e nada do que o mod faz é visível
para os outros jogadores.

**Sodium:** as telas e o HUD funcionam normalmente. O ajuste opcional de névoa vem desligado e
ainda não foi validado com o Sodium.

**Iris e shader packs:** veja a seção abaixo.

### Problema conhecido: shader packs deformam o modelo do menu

**Com um shader pack ativo, o modelo 3D do menu principal aparece deformado.** O mod detecta
isso e troca pelo **rosto da skin em 2D**, que pisca. Todas as outras telas não são afetadas.

Isso é um **contorno, não um conserto**, e vale saber o motivo se você está se perguntando se
quebrou alguma coisa aí.

O modelo do menu não é desenhado como GUI comum. Ele passa pelo **caminho de renderização de
entidades**, que os shader packs substituem e que espera dados do mundo — câmera, luz,
normais. Na tela inicial não existe mundo, então o shader transforma o modelo usando dados
que nunca foram preenchidos.

Isso também explica duas coisas que parecem bug e não são:

- **Entrar num mundo e voltar "conserta"** o modelo — aí o Iris já tem estado válido em cache.
- **O Paper Doll do HUD nunca tem esse problema**, porque sempre roda dentro de um mundo.

Se você preferir ver o modelo 3D mesmo assim, coloque `titleScreen.hideModelWithShaders` como
`false`.

## Ainda não incluído

- **O inventário criativo** — só o de sobrevivência foi trabalhado.
- **O ajuste de névoa** — implementado, mas desligado por padrão e ainda não validado com o
  Sodium.

Dois itens do plano original saíram porque o Java atual já resolve nativamente:
**renderização da água** e **margens do chat**.

## Código e relatos de bug

O mod é open source sob a **licença MIT**.

[Repositório no GitHub](https://github.com/LucasMGamerPlay/bedrock-ux) ·
[Relatar um bug](https://github.com/LucasMGamerPlay/bedrock-ux/issues) ·
[Changelog](https://github.com/LucasMGamerPlay/bedrock-ux/blob/main/CHANGELOG.md)
