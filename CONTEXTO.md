# Bedrock UX — contexto do projeto

Documento de estado. Serve para retomar o trabalho sem reler o histórico inteiro: o que o
projeto é, o que foi feito, o que foi corrigido e por quê, e o que está pendente.

Última atualização: **26/08/2026**, na versão **0.1.1**.

---

## O que é

Mod **client-side** para Minecraft Java Edition (Fabric) que reproduz a GUI/UX da Bedrock
Edition. Não adiciona itens, blocos nem vantagem de jogo — só muda o que aparece na tela.
Não precisa estar no servidor.

Documentos de origem, na raiz do repositório:

- `Documento_Design_Bedrock_UX.pdf` — GDD
- `Planejamento_Mod_BedrockUI.xlsx` — escopo, itens UI-01 a UI-08

As telas foram reproduzidas a partir de capturas reais do Bedrock em
`Fotos de Menus direto do Minecraft Bedrock Como exemplo/`. **As cores foram amostradas
pixel a pixel dessas imagens**, não estimadas.

---

## Ambiente técnico

| Item | Valor | Observação |
|---|---|---|
| Minecraft | 26.2 | |
| Java | **25** | exigência do próprio 26.2 (`majorVersion: 25`) |
| Fabric Loader | 0.19.3 | |
| Fabric API | 0.158.0+26.2 | |
| Loom | 1.17.19 | |
| Gradle | 9.5.1 | |
| Mapeamentos | **nenhum** | ver abaixo |

### Por que não há Yarn nem Mojmap

O GDD previa Java 21 + Yarn/Mojmap. Isso não existe mais no 26.x:

- o Yarn parou no `1.21.11`;
- o `intermediary-26.2.jar` vem com `mappings.tiny` **vazio**;
- o manifesto do 26.2 não tem mais o download `client_mappings`.

Motivo de fundo: **o client do 26.2 é distribuído sem ofuscação**. As classes já vêm com o
nome oficial, então o `build.gradle` não declara bloco `mappings` — é o que o template
oficial do Fabric faz nessa versão.

Consequência prática: **não existe a task `remapJar`**. O `deployToProfile` copia o `jar`
direto. Sem mapeamentos o Loom nem chega a registrar a task de remap.

### Regra de trabalho que vale manter

**Ler as APIs reais do `client.jar` com `javap`, nunca de memória.** A 26.2 renomeou coisas
(`GuiGraphicsExtractor` no lugar de `GuiGraphics`, GUI em modo retido via
`extractRenderState`), e chutar assinatura custa um ciclo de build inteiro.

```bash
javap -p -cp <client.jar> net.minecraft.client.gui.screens.inventory.InventoryScreen
```

---

## Estrutura do código

```
com.bedrockux
├── BedrockUX / BedrockUXClient    entrada, acesso à config
├── config/                        modelo JSON + carregamento (config/bedrockux.json)
├── ext/SpacedSlot                 interface tocada por mixin (fora do pacote mixin de propósito)
├── hud/                           Paper Doll, coordenadas, layout do HUD
├── loading/LoadingTips            dicas rotativas da tela de carregamento
├── mixin/                         13 mixins, todos client-side
└── ui/
    ├── BedrockTheme               paleta e primitivas de desenho (painel, slot, botão, seta)
    ├── PlayerModelPreview         modelo do jogador com cabeça articulada (menu e pausa)
    └── ShaderCompat               detecta Iris por reflexão, sem dependência de compilação
```

O HUD usa `HudElementRegistry` (fabric-rendering-v1) em vez de mixin. As telas usam mixin
porque não há ponto de extensão equivalente.

---

## Estado das features

| ID | Feature | Estado |
|---|---|---|
| UI-01 | Paper Doll | ✅ |
| UI-02 | Coordenadas no HUD | ✅ |
| UI-03 | Margens do chat | ➖ o Java atual já equivale |
| UI-04 | Menu principal | ✅ |
| UI-05 | Transições de tela | ✅ |
| UI-06 | Sons de UI | ✅ |
| UI-07 | Tela de carregamento | ✅ |
| UI-08 | Água | ➖ **obsoleto**, já é nativo |
| UI-08 | Névoa | 🟡 implementado, desligado por padrão, **não validado com Sodium** |
| — | Menu de pausa | ✅ fora do plano original |
| — | Inventário de sobrevivência | ✅ fora do plano original |
| — | Inventário criativo | ❌ fora de escopo por decisão |

Itens marcados ➖ foram verificados no jogo antes de serem descartados — o plano é anterior
às versões modernas do Java, que resolveram os dois nativamente.

A configuração tem 11 seções (`coordinates`, `paperDoll`, `buttons`, `transitions`,
`sounds`, `fog`, `loadingScreen`, `messageScreen`, `titleScreen`, `pauseScreen`,
`inventory`), **cada uma desligável pelo próprio `enabled`**.

---

## Bugs corrigidos e a causa de cada um

Esta é a parte que não se recupera lendo o código. As causas eram quase sempre diferentes
do sintoma.

### A grade do inventário crescia a cada abertura — `722f7b0`

**Sintoma:** depois de algumas aberturas do inventário, os slots apareciam fora do painel.

**Causa:** o `player.inventoryMenu` **vive no jogador e é reusado** toda vez que a tela
abre, mas a *tela* é construída do zero. O espaçamento multiplicava `slot.x` pelo fator a
cada `init`, então a grade compunha (19/18 elevado ao número de aberturas) enquanto o
painel, que sai do `imageWidth` da tela, voltava ao tamanho do vanilla a cada vez.

**Correção:** `SlotMixin` guarda a posição de fábrica na primeira chamada e **sempre deriva
dela**, tornando o reposicionamento idempotente.

**Como apareceu:** só ao gerar as capturas para a galeria do Modrinth. Teria ido para a
release.

### Modelo da tela inicial deforma com shader pack — `1f17449`

**Sintoma:** com shader ativo, o modelo 3D do menu aparece cisalhado. Entrar num mundo e
voltar "conserta".

**Causa:** o modelo não é desenhado como GUI. Passa pelo `PictureInPictureRenderer`, que
renderiza pelo **caminho de entidades**, que o shader substitui e que espera dados de mundo
(câmera, luz, normais). Na tela inicial não existe mundo.

**Contorno:** `ShaderCompat` consulta `IrisApi.isShaderPackInUse()` **por reflexão** (sem
dependência de compilação) e troca o modelo pelo rosto da skin em 2D, que é `blit` comum.
Desligável em `titleScreen.hideModelWithShaders`.

**É contorno, não conserto** — a causa está na interação do Iris com renderização de
entidade fora de um mundo.

### Paper Doll ficava em pé ao voar de elytra — `c4e7b01`

Três causas empilhadas, descobertas uma de cada vez:

1. a caixa do `PictureInPictureRenderer` é um **clip real** — o boneco deitado era cortado,
   daí `flyingWidthMultiplier`;
2. a translação precisa acompanhar `cos(pitch)`, não ser fixa;
3. o `headPitchLimit` estava capando o mergulho.

### Sentido do giro da cabeça invertido — `f6d2f5c`

**Sintoma:** cursor à esquerda virava a cabeça à direita. Cima e baixo corretos.

**Causa:** o modelo é exibido de frente, ou seja, girado 180° em relação ao próprio "para
frente", então um giro aplicado no espaço do modelo **aparece espelhado na tela**. O pitch
não sofre disso porque o eixo de inclinação não muda de sentido com esse giro — exatamente
o que o sintoma dizia.

**Correção:** o yaw da cabeça entra negado; o do corpo não.

### Texto sobrando nos botões de ícone — `ad3d26c`

`SpriteIconButton` desenha o próprio conteúdo, e o vanilla nunca desenha o `getMessage()`
dele. Como o mod passou a desenhar o rótulo por conta própria, o texto vazava por cima do
ícone.

---

## Armadilhas de build já resolvidas

- **PKIX / TLS falhando nos downloads.** Antivírus que intercepta TLS (AVG) quebra o Gradle.
  Resolvido com `trustStoreType=Windows-ROOT` — no `gradle.properties` do usuário para a JVM
  do Gradle, e via `runs.configureEach` (só no Windows) para a JVM do jogo.
- **`remapJar` não existe.** Ver a seção de mapeamentos.
- **`GameProfile.getName()` não existe** — é um record, use `.name()`.
- **`@Shadow` não acha campo herdado.** `leftPos`, `topPos` e `getMenu()` são declarados em
  `AbstractContainerScreen`, não em `InventoryScreen`. Use `@Accessor` num mixin da
  superclasse, ou cast.
- **Interface tocada por mixin não pode viver em `com.bedrockux.mixin`.** O Mixin é dono do
  pacote e recusa referência direta. Por isso `SpacedSlot` está em `com.bedrockux.ext`.

---

## Testar telas no cliente de desenvolvimento

Jars soltos em `run/mods/` são carregados pelo `runClient`, e `run/shaderpacks/` mais um
`run/config/iris.properties` reproduzem a configuração real. Foi assim que o bug do shader
saiu de "não reproduz aqui" para corrigido com verificação.

Atalho para entrar direto num mundo:

```bash
./gradlew runClient -PquickPlay="Novo mundo"
```

Para capturar telas por script: o jogo **pausa ao perder o foco**. Desligue
`pauseOnLostFocus` no `run/options.txt` antes, senão toda captura sai com o menu de pausa
por cima. O teclado sintético só chega no jogo via `PostMessage` (`WM_KEYDOWN`); `SendKeys`
e `keybd_event` são ignorados pelo GLFW.

---

## Estado atual

### Git

Tudo empurrado. `main` e `origin/main` em `d400dce`, working tree limpo.

Tags `v0.1.0` e `v0.1.1` no remoto.

### Releases no GitHub

| Release | Jar | Notas |
|---|---|---|
| `v0.1.0` | anexado | completas, bilíngues |
| `v0.1.1` | anexado (153 KB) | **vazias** — pendente |

A 0.1.1 é só a correção da grade do inventário.

### Modrinth

**Projeto rejeitado na primeira submissão**, por duas regras:

- **2.2, acessibilidade** — a descrição usava `# ` (H1) como abertura. Corrigido: não há
  mais nenhum H1 no corpo, seções começam em `##`, ênfase é negrito.
- **2.1, descrição insuficiente** — reescrita de ~800 para ~2000 palavras, detalhando cada
  recurso e abrindo com uma seção de motivos.

A página de moderação lista ainda dois itens **não resolvidos**: enviar imagem de galeria e
preencher as **divulgações de conteúdo**.

> A própria página avisa: reenviar sem tratar todas as questões pode suspender a conta.
> Trate as duas pendências antes de reenviar.

Material pronto para a publicação:

| O quê | Onde |
|---|---|
| Corpo da página (bilíngue) | [MODRINTH.md](MODRINTH.md) |
| Summary, categorias, formulário | [PUBLICACAO.md](PUBLICACAO.md) |
| Ícone 512×512, 105 KiB | [Bedrock_UX_icon_512.jpg](Bedrock_UX_icon_512.jpg) |
| Galeria, 5 capturas em 1920×1080 | [galeria/](galeria) |
| Changelog | [CHANGELOG.md](CHANGELOG.md) |

**O ícone precisa ser o `.jpg`.** O limite do Modrinth é 256 KiB e o PNG de 512×512 dá
582 KiB — é recusado no upload.

**As imagens de `Fotos de Menus direto do Minecraft Bedrock...` não servem para a galeria.**
São capturas do Bedrock real, usadas como referência de design; na galeria mostrariam o jogo
da Mojang, não o mod.

---

## Pendências

1. **Notas da release `v0.1.1`** — está publicada sem descrição.
2. **Modrinth** — subir galeria, preencher divulgações de conteúdo, reenviar para revisão.
   Projetos novos passam por revisão manual, de horas a poucos dias.
3. **Summary do Modrinth** — o que está no ar é uma tradução em português; a descrição nova
   é bilíngue com inglês primeiro. O texto em inglês sugerido está no `PUBLICACAO.md`.
4. **Névoa com Sodium** — implementada e desligada, nunca validada. Adiada por decisão.
5. **Inventário criativo** — fora de escopo por decisão.

### Limitações conhecidas que não são bugs

- **Os slots do inventário têm 18px**, menores que os do Bedrock. O ícone do item é fixo em
  16×16 no Java; aumentar o slot deixaria o item pequeno no canto. Por isso o mod mexe no
  **espaçamento** (`inventory.slotPitch`, padrão 19) e não no tamanho.
- **As abas do livro de receitas ficam na vertical à esquerda**, e não na horizontal em
  cima como no Bedrock. Isso é layout interno do `RecipeBookComponent`, uma reescrita à
  parte da tela do inventário.
- **Não existem os botões `?` e `×`** do Bedrock. A tela do Java não tem equivalentes; o
  botão do livro de receitas foi movido para a posição que a fileira ocupa no Bedrock.
