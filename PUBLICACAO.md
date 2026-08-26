# Publicação no Modrinth

Checklist do formulário de criação do projeto. O corpo da página fica em
[MODRINTH.md](MODRINTH.md) — é só colar inteiro no campo **Description**.

## Summary

Texto puro, **sem Markdown**, limite de 256 caracteres. Colar em **uma linha só**:

```text
Bedrock Edition's GUI on Java: Paper Doll, Bedrock-style main menu, pause menu, inventory, loading screens and UI sounds. Client-side, works on any server.
```

155 caracteres. Não repita o nome do projeto no começo — o card de busca já mostra o nome
logo acima.

## Formulário

| Campo | Valor |
|---|---|
| Categorias | Decoration, Utility |
| Cliente | `required` |
| Servidor | `unsupported` |
| Licença | MIT |
| Ícone | [`Bedrock_UX_icon_512.jpg`](Bedrock_UX_icon_512.jpg) — 512×512, 105 KiB |
| Source | https://github.com/LucasMGamerPlay/bedrock-ux |
| Issues | https://github.com/LucasMGamerPlay/bedrock-ux/issues |

## Versão

| Campo | Valor |
|---|---|
| Número | `0.1.0` |
| Canal | Release |
| Loader | Fabric |
| Minecraft | 26.2 |
| Arquivo | `build/libs/bedrockux-0.1.0.jar` |
| Changelog | [CHANGELOG.md](CHANGELOG.md) |

## Sobre o ícone

**O limite do Modrinth é 256 KiB** e o PNG de 512×512 dá 582 KiB — é recusado no upload.
Três saídas foram comparadas:

| Variante | Tamanho | Veredito |
|---|---|---|
| PNG 512 | 582 KiB | estoura o limite |
| PNG 512 indexado | 49 KiB | cabe, mas **bandeia** — a paleta fixa suja as áreas chapadas com dithering e desloca as cores |
| **JPEG 512 q92** | **105 KiB** | cabe e fica fiel ao original — **escolhido** |

Reduzir para 256×256 também caberia (164 KiB), mas custa nitidez em telas de alta
densidade sem necessidade.

O `Bedrock_UX_logo.png` continua no repositório para o README, onde não há limite.

## Recusa da primeira submissão

O Modrinth recusou a primeira tentativa por duas regras. Ambas corrigidas no
[MODRINTH.md](MODRINTH.md) — não reintroduza:

**Regra 2.2 — Acessibilidade.** A descrição usava `# Bedrock UX` como abertura. Leitores de
tela interpretam cabeçalho de forma diferente de texto, e o Modrinth já exibe o nome do
projeto acima da descrição. **Nunca use `# ` (H1) na descrição.** Cabeçalho serve só para
separar seção, começando em `## `. Para dar ênfase, use **negrito**.

**Regra 2.1 — Descrição insuficiente.** Precisa dizer o que o mod adiciona, quais os
recursos e por que alguém baixaria. A descrição foi reescrita de ~800 para ~2000 palavras,
detalhando cada recurso e abrindo com uma seção de motivos.

### Imagens — o que provavelmente vem na próxima recusa

Um mod **visual** sem nenhuma captura de tela tende a ser recusado de novo. A galeria
precisa de capturas **do mod rodando no Java**.

**Não use as imagens de `Fotos de Menus direto do Minecraft Bedrock Como exemplo/`.** São
capturas do Bedrock real, usadas como referência de design — na galeria elas mostrariam o
Bedrock, não o mod, o que engana o leitor.

As capturas do mod ficam em [`galeria/`](galeria), geradas do cliente de desenvolvimento
em 1920x1080. Ordem sugerida na galeria — a primeira e a que vira capa:

| Arquivo | Legenda sugerida |
|---|---|
| `1-menu-principal.png` | Main menu — Bedrock layout, with the player model's head following the cursor |
| `2-hud-paper-doll.png` | Paper Doll on the HUD, horizontal during elytra flight, with Bedrock coordinates |
| `4-inventario.png` | Bedrock inventory panel, spaced slots and crafting arrow |
| `3-menu-pausa.png` | Pause menu with the buttons at the left and the player model at the right |
| `5-tela-carregamento.png` | Bedrock loading panel with progress bar and tips |

## Depois de enviar

Projetos novos passam por **revisão manual** antes de ficarem públicos — de algumas horas
a poucos dias. Não é rejeição, é fila.
