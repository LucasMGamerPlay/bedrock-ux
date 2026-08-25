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

## Depois de enviar

Projetos novos passam por **revisão manual** antes de ficarem públicos — de algumas horas
a poucos dias. Não é rejeição, é fila.
