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
| — | Botões com cores planas do Bedrock | ✅ Fase 1 |
| UI-01 | Paper Doll | ✅ Fase 2 |
| UI-03 | Margens do chat | ➖ não aplicável (ver abaixo) |
| UI-04 | Menu principal | ⬜ Fase 3 |
| UI-05 | Transições de tela | ⬜ Fase 3 |
| UI-07 | Tela de carregamento | ⬜ Fase 3 |
| UI-06 | Sons de UI | ⬜ Fase 4 |
| UI-08 | Água e névoa | ⬜ Fase 4 |

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
    "enabled": true
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

**Poses horizontais são neutralizadas.** Voando de elytra, o `AvatarRenderer` aplica
`Axis.XP.rotationDegrees(fallFlyingScale * (-90 - xRot))`, e nado e tridente têm
transformações equivalentes. Na tela cheia isso é o certo; numa caixa de 44×66 o modelo
deitado não cabe e sai cortado. Com `uprightWhileFlying` (padrão `true`), as flags
`isFallFlying`, `isVisuallySwimming`, `swimAmount` e `isAutoSpinAttack` são zeradas e o
boneco fica em pé — a animação dos membros continua indicando o estado. Pose `SLEEPING`
também vira `STANDING`, porque dormindo o vanilla pula a rotação do corpo por completo e o
boneco travaria virado. Ponha `false` para ver a pose crua.

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
