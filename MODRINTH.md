<!--
  Corpo da página do Modrinth. Colar no campo "Description" do projeto.
  Não faz parte do mod — é material de publicação.
-->

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
