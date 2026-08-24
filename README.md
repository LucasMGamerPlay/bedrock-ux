# Bedrock UX

Mod **client-side** para Minecraft Java Edition (Fabric) que traz a GUI/UX da edição Bedrock.
Documentos de origem: `Documento_Design_Bedrock_UX.pdf` (GDD) e `Planejamento_Mod_BedrockUI.xlsx` (escopo).

## Alvo

| Item | Versão | Observação |
|---|---|---|
| Minecraft | `26.2` | mesma versão do perfil de teste |
| Fabric Loader | `0.19.3` | |
| Fabric API | `0.158.0+26.2` | |
| Java | **25** | o manifesto do 26.2 pede `majorVersion: 25` |
| Loom | `1.17.19` | o template usa `1.17-SNAPSHOT`, que resolveu para essa release |
| Mapeamentos | **nenhum** | ver abaixo |

### Por que não usamos Yarn nem Mojmap

O GDD previa Java 21 + Yarn/Mojmap. Isso mudou no 26.x:

- o Yarn parou no `1.21.11` — não existe build para 26.x;
- o `intermediary-26.2.jar` do Fabric vem com o `mappings.tiny` **vazio**;
- o manifesto do 26.2 na Mojang não tem mais o download `client_mappings`.

Motivo: **o client do 26.2 é distribuído sem ofuscação**. As classes já vêm com o nome
oficial (`net.minecraft.client.gui.Gui`, `Hud`, `GuiGraphicsExtractor`), então o
`build.gradle` não declara bloco `mappings` — é o que o template oficial faz.

Isso também explica por que o `deployToProfile` copia o `jar` e não o `remapJar`: sem
mapeamentos, o Loom nem chega a registrar a task de remap.

Consequência prática: os nomes de classe do GDD e da planilha estão em Yarn e precisam
ser traduzidos para os nomes oficiais. Equivalências já levantadas:

| Planilha (Yarn) | 26.2 (oficial) |
|---|---|
| `InGameHud` | `net.minecraft.client.gui.Hud` (a `Gui` agora é só o container de telas) |
| `DrawContext` | `net.minecraft.client.gui.GuiGraphicsExtractor` |
| `Screen` | `net.minecraft.client.gui.screens.Screen` |
| `TitleScreen` | `net.minecraft.client.gui.screens.TitleScreen` |
| `ChatHud` | `net.minecraft.client.gui.components.ChatComponent` |
| `SplashOverlay` | `net.minecraft.client.gui.screens.LoadingOverlay` |
| `LevelLoadingScreen` | `net.minecraft.client.gui.screens.LevelLoadingScreen` |
| `Identifier` | `net.minecraft.resources.Identifier` |

Além do rename, o 26.2 mudou o modelo de renderização de GUI: nada desenha direto na
tela. Cada elemento implementa `extractRenderState(GuiGraphicsExtractor, ...)`, que
**monta um render state** desenhado depois. É por isso que os hooks da Fase 2 (Paper Doll)
e Fase 3 (transições) vão precisar de um desenho diferente do que o GDD imaginava.

## Estado das features

| ID | Feature | Status |
|---|---|---|
| UI-02 | HUD de coordenadas | ✅ Fase 1 |
| — | Botões com a paleta do Bedrock | ✅ Fase 1 / revisto na Fase 3 |
| UI-01 | Paper Doll | ✅ Fase 2 |
| UI-03 | Margens do chat | ➖ não aplicável (ver abaixo) |
| UI-04 | Menu principal | ✅ Fase 3 |
| UI-05 | Transições de tela | ✅ Fase 3 |
| UI-07 | Tela de carregamento | ✅ Fase 3 |
| UI-06 | Sons de UI | ✅ Fase 4 |
| UI-08 | Água e névoa | 🟡 névoa: ajuste opcional · água: **obsoleto**, já é nativo |

## Como rodar

Pré-requisito: **JDK 25**. Aponte o `JAVA_HOME` para ele antes de qualquer comando.

Depois de clonar, copie `local.properties.example` para `local.properties` e ajuste o
caminho do perfil de teste — esse arquivo é ignorado pelo git porque guarda caminhos da
sua máquina. Sem ele tudo funciona, menos a task `deployToProfile`.

```bash
gradlew.bat build
```

Rodar o cliente de desenvolvimento (mundo de teste isolado, em `run/`):

```bash
gradlew.bat runClient
```

Para iterar em features de HUD sem passar pelos menus, entre direto num mundo:

```bash
gradlew.bat runClient "-PquickPlay=Novo mundo"
```

Instalar num perfil do Modrinth para testar junto com Sodium/Iris:

```bash
gradlew.bat deployToProfile
```

O destino vem de `test_profile_dir`, no `local.properties`.

## Configuração

Arquivo gerado em `config/bedrockux.json` na primeira execução:

```json
{
  "coordinates": {
    "enabled": true,
    "showFacing": false,
    "showBiome": false,
    "hideWithDebugScreen": true,
    "textShadow": false,
    "belowPaperDoll": true,
    "offsetX": 4,
    "offsetY": 4,
    "backgroundOpacity": 0.4
  },
  "paperDoll": {
    "enabled": true,
    "hideWithDebugScreen": true,
    "offsetX": 4,
    "offsetY": 4,
    "gap": 2,
    "width": 44,
    "height": 66,
    "scale": 28.0,
    "tiltDegrees": 0.0,
    "yawOffsetDegrees": 20.0,
    "uprightWhileFlying": true,
    "headPitchLimit": 35.0
  },
  "buttons": {
    "enabled": true,
    "semanticColors": true
  },
  "loadingScreen": {
    "enabled": true,
    "showTips": true,
    "showLogo": true,
    "panelWidth": 260,
    "barHeight": 6
  },
  "messageScreen": {
    "enabled": true,
    "showLogo": true,
    "showSavingIcon": true,
    "panelWidth": 260
  },
  "sounds": {
    "enabled": true,
    "clickPitch": 1.6,
    "clickVolume": 0.5
  },
  "fog": {
    "enabled": false,
    "startMultiplier": 0.6,
    "endMultiplier": 0.85
  },
  "transitions": {
    "enabled": true,
    "durationMillis": 180,
    "slideDistance": 24.0
  },
  "titleScreen": {
    "enabled": true,
    "columns": 1,
    "buttonWidth": 180,
    "buttonHeight": 28,
    "columnGap": 8,
    "rowGap": 6,
    "menuWidthFraction": 0.34,
    "showPlayerModel": true,
    "showPlayerName": true,
    "modelFollowsMouse": true,
    "hideModelWithShaders": true,
    "modelHeightFraction": 0.55,
    "maxModelHeight": 170,
    "modelCenterFraction": 0.82
  }
}
```

Teclas (rebindáveis em Opções → Controles → Bedrock UX):

- **F4** — liga/desliga as coordenadas
- *(sem tecla)* — liga/desliga a Paper Doll
- *(sem tecla)* — recarrega o `bedrockux.json` sem reiniciar o jogo

## Estrutura

```
src/main/java/com/bedrockux/
├── BedrockUX.java              estado global (id, logger, config)
├── BedrockUXClient.java        entrypoint client, keybinds, registro da HUD
├── config/                     modelo + leitura/escrita do JSON
├── hud/CoordinatesHudElement   UI-02
├── hud/PaperDollHudElement     UI-01
├── hud/HudLayout               cursor de empilhamento do canto superior esquerdo
├── ui/BedrockTheme             paleta e primitivas de desenho
└── mixin/                      AbstractButton (botões) + accessor de alpha
```

### Sobre Mixins

O GDD pede Mixins para tudo. Onde a Fabric API já oferece um ponto de extensão oficial,
usamos a API — é menos invasivo e sobrevive melhor às atualizações, que é justamente o
objetivo declarado no GDD. A HUD de coordenadas usa `HudElementRegistry`; o Mixin só
entra nos botões, onde não existe API equivalente.

## Antivírus que interceptam TLS

AVG, Kaspersky e ESET fazem MITM nas conexões HTTPS. O `curl` aceita porque usa a loja de
certificados do Windows, mas o Java usa o próprio `cacerts` e quebra com
`PKIX path building failed` ao baixar o Gradle, as dependências ou ao autenticar na Mojang.

O projeto resolve a parte que dá para resolver sozinho: o `build.gradle` injeta
`javax.net.ssl.trustStoreType=Windows-ROOT` nas run configs do Loom **quando roda no
Windows**, para o JVM do jogo — que é um processo separado e não herda nada do Gradle.

A parte que o repositório não pode carregar é o JVM do próprio Gradle, porque a flag
quebraria o build em Linux e macOS, onde esse tipo de trust store não existe. Ela vai na
configuração de usuário, em `~/.gradle/gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx2G -Djavax.net.ssl.trustStoreType=Windows-ROOT
```

Para o download do próprio wrapper, que roda antes de qualquer `gradle.properties` ser
lido, a flag precisa estar no ambiente:

```powershell
$env:GRADLE_OPTS = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"
```

## Onde o projeto fica

Evite pastas sincronizadas (OneDrive, Dropbox): o Loom avisa que elas deixam o build lento
e podem dar erro de arquivo em uso, porque `.gradle/` e `build/` são reescritas a cada
build. Se mover o projeto, apague `.gradle/` e `build/` antes do primeiro build no caminho
novo — eles guardam caminhos absolutos do local anterior.

## Notas da Fase 2

**Como a Paper Doll é montada.** `EntityRenderer.createRenderState(player, partialTick)`
já devolve tudo pronto — pose, animação de caminhada, elytra, equipamento. O mod não
reproduz nenhuma dessas matemáticas; ele só corrige a orientação e entrega o estado a
`GuiGraphicsExtractor.entity(...)`, que desenha num passo separado de picture-in-picture.

Semântica dos campos de rotação em `LivingEntityRenderState` (levantada do bytecode de
`LivingEntityRenderer` e de `InventoryScreen`):

| Campo | Significado |
|---|---|
| `bodyRot` | yaw do corpo, absoluto — `180` deixa a entidade de frente para quem olha, `0` de costas |
| `yRot` | yaw da cabeça **relativo ao corpo** |
| `xRot` | pitch da cabeça |

Como o render state traz o `bodyRot` em coordenadas do mundo, o boneco giraria junto com a
câmera. Tornando-o relativo ao yaw da cabeça, ele para de girar e mostra só a diferença
real entre corpo e cabeça — o comportamento do Bedrock. O `yRot` não é tocado, justamente
por já ser relativo.

Sobre esse valor relativo aplica-se `yawOffsetDegrees`, a partir da vista **de frente**
(`bodyRot` 180), que é a do Bedrock: o boneco encara quem joga. O padrão de 20° dá o leve
ângulo do original em vez da pose reta; `0` deixa o boneco de frente e reto e `180` mostra
as costas.

**O ângulo entra subtraindo, não somando.** `LivingEntityRenderer.setupRotations` gira o
modelo por `Axis.YP.rotationDegrees(180 - bodyRot)` — ou seja, o ângulo que aparece na tela
é o *inverso* do campo. Somar o offset manda o boneco para o lado oposto ao do Bedrock. A
mesma inversão vale para o offset corpo/cabeça: numa câmera posicionada de frente, o giro
do corpo aparece espelhado.

**Poses horizontais são reenquadradas, não neutralizadas.** Voando de elytra, o
`AvatarRenderer` aplica `Axis.XP.rotationDegrees(fallFlyingScale * (-90 - xRot))`, e nado e
tridente têm transformações equivalentes. O Bedrock mostra essa pose deitada, então o mod
mantém — mas ela exige três ajustes, porque a caixa é um recorte real:

1. **A caixa alarga** (`flyingWidthMultiplier`, padrão 2×). Deitado, o modelo ocupa no
   comprimento o que ocupava na altura, e as asas abrem ainda mais. A altura fica igual de
   propósito: mudá-la faria a caixa de coordenadas pular de lugar toda vez que o jogador
   voasse.
2. **A translação vertical zera.** Em pé, o modelo cresce dos pés para cima e a translação
   de meia altura o centraliza. Deitado ele gira em torno dos pés e se estende em
   profundidade — a mesma translação só o empurrava para fora da caixa, que era o bug de
   "sumir ao voar".
3. **O eixo de rotação acompanha a inclinação.** O modelo gira em torno dos **pés**, não do
   centro, então uma translação fixa deixa o boneco pendurado pelos pés em vez de girar no
   lugar. A translação usa `metadeAltura · cos(ânguloAplicado)`: em pé (0°) dá meia altura,
   deitado (90°) dá zero, porque aí o centro do corpo está na altura dos pés.

   A altura vem do **modelo**, não da caixa delimitadora — voando ela encolhe de 1,8 para
   0,6, e centralizar por ela cortava a cabeça e as asas no topo.

4. **O limite de pitch não vale em voo.** `headPitchLimit` existe para a cabeça não deitar
   com o boneco em pé, mas durante o voo o mesmo `xRot` comanda a inclinação do corpo
   inteiro — limitar ali impedia o boneco de virar para baixo ao mergulhar.

5. **`flyingPitchScale`** (padrão 1,0) permite amortecer o mergulho, se desejado.

`uprightWhileFlying` (padrão `false`) volta ao comportamento antigo, forçando o boneco em pé.
Ele também converte pose `SLEEPING` em `STANDING`, porque dormindo o vanilla pula a rotação
do corpo por completo e o boneco travaria virado.

**Ordem da pilha.** No Bedrock o boneco fica em cima e as coordenadas embaixo. Quem define
isso é a ordem de registro em `HudElementRegistry.addLast`: a Paper Doll roda primeiro e
reinicia o cursor de `HudLayout`, as coordenadas leem esse cursor e se encaixam abaixo. Com
o boneco desligado, o cursor continua no topo e a caixa de coordenadas sobe sozinha.

O `xRot` é limitado por `headPitchLimit` (padrão 35°). Sem isso, olhar para cima deixa a
cabeça quase na horizontal e o boneco vira uma mancha na caixa pequena. Ponha `90` para
espelhar o pitch real sem limite.

**A caixa é um recorte real.** O `PictureInPictureRenderer` desenha a entidade numa textura
do tamanho exato de `width` × `height` — o que passar disso é cortado, não só escondido.
Ao aumentar `scale`, aumente as dimensões junto.

**UI-03 (margens do chat) não é necessária.** A planilha previa ajustar o chat para
acomodar a Paper Doll, o que pressupõe o boneco perto do chat. Como UI-01 pede o canto
superior esquerdo e o chat fica no inferior esquerdo, não há sobreposição. Se a Paper Doll
for movida para baixo, o item volta a fazer sentido.

## Notas da Fase 3

**UI-04 reposiciona, não reconstrói.** O Mixin entra em `TitleScreen.init` no `TAIL` e move
os botões que o vanilla já criou, em vez de montar o menu do zero. Assim os handlers de
clique, as tooltips e os botões que outros mods adicionam continuam funcionando, e o layout
sobrevive às variações do próprio jogo (modo demo, Realms desligado).

Os botões são identificados por **largura e altura**: largura ≥ 90 pega os principais e
ignora os de ícone 20×20; altura exatamente 20 descarta o texto de direitos autorais, que é
um `PlainTextButton` clicável largo o bastante para passar pelo filtro de largura sozinho.

O layout segue as capturas reais em `Fotos de Menus direto do Minecraft Bedrock Como
exemplo/`: **coluna única centralizada** com botões altos, não uma grade — a planilha dizia
"grid", mas o Bedrock empilha numa coluna só. Os botões de ícone (idioma, acessibilidade)
vão para o canto inferior esquerdo, que é onde o Bedrock põe seus atalhos equivalentes, e
onde não colidem com a coluna.

Largura **e** altura são derivadas do espaço disponível, com o config servindo de teto.
Valores fixos estouram a tela em janelas pequenas ou GUI scale alto: a largura invadia a
área do modelo, e a altura empurrava o último botão para fora da tela.

**O modelo do menu não é a Paper Doll.** No menu principal não existe entidade de jogador
(`Minecraft.player` é `null`), então `createRenderState` não serve. O 26.2 tem
`PlayerSkinWidget`, que renderiza a skin a partir de um `Model.Simple` sem precisar de
entidade — alimentado por `SkinManager.createLookup(perfil, false)`. De brinde ele já é
arrastável para girar, como no Bedrock. O nome da conta fica acima dele, e o bloco inteiro
(nome + modelo) é centralizado na faixa abaixo do logo — ancorar pelo modelo fazia o nome
subir por cima do logo.

## Paleta dos botões

As cores **não foram estimadas** — saíram de amostragem pixel a pixel das capturas em
`Fotos de Menus direto do Minecraft Bedrock Como exemplo/`. A anatomia de um botão do
Bedrock, de cima para baixo: borda externa, linha de brilho, corpo, linha de sombra, borda
externa.

| Variante | Corpo | Borda | Brilho | Sombra | Texto |
|---|---|---|---|---|---|
| Secundária (padrão) | `#C6C6C6` | `#131313` | `#F7F7F7` | `#656465` | `#4C4C4C` |
| Primária (verde) | `#3C8527` | `#1E1E1F` | `#639D52` | `#1D4D13` | `#FFFFFF` |
| Destaque (Realms) | `#7345E5` | `#131313` | `#A164F2` | `#4A1CAC` | `#FFFFFF` |

**O rótulo precisou ser redesenhado.** O fundo do Bedrock é claro, e o texto branco do
vanilla ficaria ilegível. Não dá para só trocar a cor: o rótulo passa por
`ActiveTextCollector`, cujo `Parameters` tem pose, opacidade e scissor — e nenhuma cor. A
saída foi cancelar `extractDefaultLabel` e desenhar o texto em `extractDefaultSprite`, onde
o extractor está em mãos.

Isso é seguro porque **nenhuma classe do jogo chama `extractDefaultLabel` sem chamar
`extractDefaultSprite`** (verificado varrendo o client.jar): todo rótulo suprimido pertence
a um botão cujo fundo repintamos. O custo é a rolagem que o vanilla faz em rótulos longos
demais, que aqui vira corte simples.

A exceção é `SpriteIconButton.TextAndIcon`, que desenha o próprio texto fora do
`extractDefaultLabel` — como não dá para recolori-lo, esses botões mantêm o visual do
vanilla em vez de virarem texto branco sobre fundo claro.

**Variantes semânticas** são mapeadas por chave de tradução (não por texto, para valer em
qualquer idioma) em `AbstractButtonMixin`. O Java não tem noção de "ação primária", então a
lista é manual e cresce conforme as telas forem cobertas: hoje cobre `selectWorld.create`,
`selectWorld.select`, `gui.done` (verde) e `menu.online` (roxo). Desligue com
`buttons.semanticColors`.

## O modelo do menu principal

`PlayerSkinWidget` deriva a escala **só da altura** (`0.97 × altura / 2.125`); a largura
serve apenas de recorte. Isso tem duas consequências que morderam:

- **Altura fixa estoura em GUI scale alto.** Um valor de 170 unidades vira mais da metade
  da tela quando o scale é 4, o modelo passa do rodapé e o recorte da caixa corta o
  personagem ao meio. A altura agora vem de `modelHeightFraction` (fração da tela), com
  `maxModelHeight` de teto.
- **Caixa estreita corta os braços.** Como a largura não influencia a escala, ela precisa
  acompanhar a altura. É derivada com `MODEL_ASPECT = 0.75`, com folga para o modelo girado.

**Seguir o cursor** (`modelFollowsMouse`) reescreve `rotationX` / `rotationY` a cada frame a
partir da posição do mouse, via accessor — o widget do vanilla só gira quando arrastado. Os
ângulos são normalizados pela metade da tela, então o giro é o mesmo em qualquer resolução.

Consequência a saber: com `modelFollowsMouse` ligado, **arrastar não funciona**, porque a
rotação é sobrescrita todo frame. Ponha `false` para voltar ao giro manual do vanilla.

## Notas da UI-05 e UI-07

**A transição desliza pela matriz, não por framebuffer.** O GDD propunha guardar a tela
anterior num buffer e interpolar as duas. No modelo retained-mode do 26.2 isso exigiria
capturar framebuffer a cada troca de tela. Empurrar a matriz de `GuiGraphicsExtractor.pose()`
dá o mesmo efeito percebido sem tocar em framebuffer — e foi verificado deslocando a tela
inteira 100px de propósito: logo, botões, ícones e rodapé acompanham.

Três armadilhas custaram um ciclo cada até a animação aparecer:

1. **`Screen.extractRenderState` é o alvo errado.** Quase toda tela o sobrescreve sem chamar
   `super`, então o hook nunca rodava. O alvo certo é
   `extractRenderStateWithTooltipAndSubtitles`, que é `final`.
2. **`added()` não serve de âncora** — nem toda tela passa por ele. `init(int,int)` é `final`
   e sempre roda.
3. **O relógio não pode começar em `init`.** A tela inicial é construída enquanto o overlay
   de carregamento ainda cobre tudo, então a animação inteira acontecia escondida. O relógio
   começa no **primeiro frame desenhado**.

Duas limitações conhecidas:

- **Na tela inicial, ao abrir o jogo, a animação continua invisível.** A tela é desenhada
  atrás do overlay que faz fade, então até o primeiro frame já está coberto. Em navegação
  normal entre telas o deslize aparece.
- **O modelo do jogador não desliza junto.** `PlayerSkinWidget` e a Paper Doll desenham pelo
  caminho picture-in-picture, que usa coordenadas de tela próprias e ignora a pose.

**As dicas de carregamento** ficam em `assets/bedrockux/loading_tips.json` como **chaves de
tradução**, não texto pronto — assim acompanham o idioma do jogo. Um
`config/bedrockux-tips.json` substitui a lista embutida; texto literal também funciona, e o
que não existir no idioma aparece como está. O progresso reaproveita o `smoothedProgress`
que o próprio jogo já suaviza, então não há nada a sincronizar com o threading de chunks —
que era a preocupação anotada na planilha.

## Painel de carregamento (revisão pela referência)

A primeira versão da UI-07 pintava a tela inteira de escuro com uma barra cinza contínua.
As capturas `tela de caregamento.png` e `tela de salvamento.png` mostraram que o Bedrock faz
outra coisa: o mundo continua visível ao fundo, com o logo no topo e um **painel** centrado.

Anatomia amostrada pixel a pixel:

| Elemento | Cor |
|---|---|
| Borda externa do painel | `#000000` |
| Realce branco (2px) | `#FFFFFF` |
| Corpo do painel | `#C6C6C6` |
| Sombra inferior | `#555555` |
| Título ("Carregando") | `#4C4C4C` |
| Borda da caixa interna | `#393939` |
| Interior da caixa | `#0A0909` a ~80% — **translúcido**, o mundo aparece atrás |
| Barra: segmento cheio | `#96D464` (topo `#AEEE7A`) |
| Barra: segmento vazio | `#416032` |

Dois detalhes que só a referência revelou: a barra é **segmentada**, não um preenchimento
contínuo — os segmentos vazios ficam verde-escuro em vez de sumirem, e é isso que dá o
aspecto de trilho tracejado. E a dica fica **dentro** da caixa, acima da barra, com quebra
de linha automática — não abaixo dela.

O mesmo painel serve para as telas de mensagem (`GenericMessageScreen`): salvar mundo,
conectar, carregar.

**A tela do Bedrock mostra duas frases, a do Java carrega uma só.** A referência tem um
título curto ("Salvando mundo") e uma explicação ("Seu jogo está sendo salvo. Não desligue o
dispositivo."), mas `GenericMessageScreen` recebe um único `Component`. A solução: a
mensagem do vanilla vira o **título** e o corpo sai de um mapa próprio, por chave de
tradução — hoje só `menu.savingLevel`. Telas fora do mapa ganham só o cabeçalho, sem a
caixa; inventar texto para elas seria pior do que não mostrar nada.

O texto do vanilla é escondido (`visible = false`) para não duplicar o cabeçalho. O widget
continua existindo, então a narração de acessibilidade segue funcionando — some apenas o
desenho.

## O ícone animado do salvamento

**O Minecraft não lê GIF.** O baú animado veio de um GIF de 52 quadros a 100 ms, 518×518 com
fundo transparente, convertido para uma **folha de sprites vertical**: 52 quadros de 32×32
empilhados em `assets/bedrockux/textures/gui/saving_icon.png` (32×1664).

O desenho recorta a linha do quadro atual pela coordenada V:

```java
int frame = (int) ((System.currentTimeMillis() / 100L) % 52);
context.blit(RenderPipelines.GUI_TEXTURED, icone, x, y,
        0.0F, frame * 32.0F, 32, 32, 32, 32 * 52);
```

A animação anda pelo **relógio do sistema, não por ticks do jogo**: durante o salvamento o
jogo trava por instantes, e um contador de ticks faria o ícone engasgar justamente quando
ele precisa mostrar que algo está acontecendo.

Para reconverter a partir de outro GIF, o passo é: redimensionar cada quadro para 32×32,
empilhar verticalmente, salvar como PNG com alfa, e ajustar `ICON_FRAMES` /
`ICON_FRAME_MILLIS` no `GenericMessageScreenMixin`.

## Notas da Fase 4

**UI-06 reafina o som, não traz áudio novo.** Não dá para sintetizar um OGG aqui, e
distribuir o som do Bedrock seria copiar asset da Mojang. O Mixin toca o
`minecraft:ui.button.click` do próprio jogo com tom mais alto e volume menor
(`clickPitch` 1.6, `clickVolume` 0.5), o que aproxima do toque curto e agudo do original.
Quem quiser o som exato pode substituir o evento por um resource pack — o Mixin continua
respeitando o arquivo que estiver no lugar.

**UI-08 (névoa) vem desligada por padrão.** É a única feature do mod que mexe na
renderização do mundo, que é onde mods de otimização brigam. Ligue com `fog.enabled`.

O ajuste é multiplicativo sobre o que o jogo calculou, não um valor fixo: bioma, clima,
distância de renderização e efeitos como cegueira continuam mandando, e o mod só aperta a
curva (`startMultiplier` traz o início para perto, `endMultiplier` fecha antes). `skyEnd` e
`cloudEnd` ficam intactos — mexer neles desloca o horizonte e as nuvens, que no Bedrock não
acompanham a névoa próxima.

**Sobre o Sodium.** Ele também entra em `FogRenderer`, com `@Inject` para ler os parâmetros
e alimentar o próprio renderizador — não com `@Overwrite`, então dá para coexistir. Mas a
ordem importa: este Mixin usa `priority = 900` (abaixo do padrão 1000) para ser aplicado
antes, de modo que o Sodium leia os valores já ajustados. Sem isso a névoa mudaria sem
Sodium e ficaria vanilla com ele.

**Isso ainda não foi verificado com o Sodium instalado.** O cliente de desenvolvimento não
tem Sodium; quem tem é o perfil `voxy teste`. A verificação precisa de uma execução por lá.

## O que o plano original previa e o jogo já resolveu

O GDD e a planilha foram escritos para uma versão bem mais antiga do Java, e parte do escopo
envelheceu. Verificado no `client.jar` do 26.2:

**Colormap de água por bioma já é nativo.** `BiomeColors.WATER_COLOR_RESOLVER` alimenta o
`BlockTintCache`, que mistura a cor entre biomas vizinhos — exatamente o "colormap adaptativo
por bioma" que a planilha pede. Isso existe desde a 1.13. A planilha descreve "cores de água
rígidas", que era a realidade do Java **pré-1.13** — e é justamente por isso que o
BedrockWaters, citado no GDD como referência, existia.

**A transição suave de neblina também já é nativa.** `AtmosphericFogEnvironment` usa
`Mth.clampedLerp` e mantém um `rainFogMultiplier` suavizado ao longo do tempo, em vez de
trocar de valor de golpe.

**A arquitetura de névoa foi reescrita.** `BackgroundRenderer`, a classe que a planilha manda
alterar, não existe mais. No lugar há um pacote `renderer/fog/` com um `FogEnvironment` por
condição (atmosférica, lava, cegueira, escuridão, efeito de poção). O
`BiomeSpecialEffects` nem carrega mais `fogColor` / `waterFogColor`.

**O que sobra de UI-08** não é uma funcionalidade faltando, é uma diferença de gosto: a curva
do Bedrock começa mais perto e fecha mais denso que a do Java. É só isso que o
`FogRendererMixin` faz, e por ser preferência — e por mexer na renderização do mundo — vem
desligado por padrão.

## Shader pack quebra o modelo da tela inicial

Com um shader pack ativo (reproduzido com Photon + Iris), o modelo 3D do menu principal
aparece deformado — membros cisalhados, geometria esticada. Sem shader, normal.

**A causa não é o modelo.** Ele não é desenhado como GUI: passa pelo
`PictureInPictureRenderer`, que renderiza pelo **caminho de entidades do jogo** para dentro
de uma textura. É esse caminho que o shader pack substitui, e o shader espera dados do mundo
— câmera, luz, normais. Na tela inicial não existe mundo, então ele transforma os vértices
com dados nunca preenchidos.

Isso fecha as duas pontas que confundiam:

- **A Paper Doll não sofre disso** porque roda dentro de um mundo, onde os dados existem.
- **Entrar num mundo e voltar "conserta"** porque aí o Iris já tem estado válido em cache.

**O contorno:** `ShaderCompat` consulta `IrisApi.isShaderPackInUse()` **por reflexão**, sem
criar dependência de compilação — o mod continua funcionando sem o Iris instalado. Com
shader ativo, o modelo 3D dá lugar ao **rosto da skin em 2D**, que é um `blit` comum de GUI
— caminho que o shader não substitui. Desligue com `hideModelWithShaders`.

É contorno, não conserto: a causa está na interação entre o Iris e a renderização de
entidade fora de um mundo, e não dá para resolver do lado do mod.

### Testar compatibilidade no cliente de desenvolvimento

Jars soltos em `run/mods/` são carregados pelo `runClient`, e `run/shaderpacks/` mais um
`run/config/iris.properties` copiado do perfil reproduzem a configuração real. Foi assim que
este bug saiu de "não reproduz aqui" para corrigido com verificação.
