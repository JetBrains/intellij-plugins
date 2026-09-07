# MDX Parser and Formatter Pipeline

This document describes the current IntelliJ MDX implementation. The plugin is an editor pipeline,
not an MDX compiler: it maintains a Markdown PSI tree and a JavaScript/JSX template-data PSI tree
over the same document.

## Reference model

The official `@mdx-js/mdx` pipeline tokenizes MDX with micromark, builds mdast, applies remark and
rehype transformations, converts to ESTree, and emits JavaScript. Its relevant syntax nodes are:

| MDX syntax | mdast node |
|---|---|
| Top-level `import` or `export` | `mdxjsEsm` |
| Block JSX | `mdxJsxFlowElement` |
| Inline JSX | `mdxJsxTextElement` |
| Line-start `{...}` | `mdxFlowExpression` |
| Inline `{...}` | `mdxTextExpression` |

Markdown remains recursive inside an `mdxJsxFlowElement`: headings, lists, paragraphs, and code
fences are children of the JSX element rather than opaque text.

## Two PSI roots

`MdxFileViewProvider` exposes two PSI files for one `.mdx` document:

```text
.mdx source
├── MdxLanguage
│   └── MdxParserDefinition
│       └── Markdown PSI with first-class MDX nodes
└── MdxJSLanguage
    └── MdxTemplateDataElementType
        └── JavaScript/JSX PSI over projected embedded ranges
```

The Markdown tree owns source structure. The JavaScript tree supplies JavaScript and JSX language
features for the ranges selected from that structure.

## Markdown parsing

### Flavour and parser entry points

`MarkdownToplevelLexer(MdxFlavourDescriptor)` runs the Markdown parser and replays its token stream.
`MarkdownParserAdapter(MdxFlavourDescriptor)` then builds PSI from those tokens.

`MdxFlavourDescriptor` extends CommonMark and delegates GFM inline behavior to
`GFMFlavourDescriptor`. Its marker and inline parser order makes three MDX-specific changes:

- `MdxHtmlCommentBlockProvider` gives closed multiline HTML comments an opaque block owner while
  leaving single-line HTML comments invalid MDX.
- `MdxBlockProvider` runs before the standard block providers. `MdxCodeFenceProvider` replaces the
  standard fence provider so an unclosed fence can recover at the active JSX boundary. The general
  raw-HTML and indented-code providers are removed because MDX disables those constructs.
- `MdxInlineElementParser` runs before `InlineLinkParser`, allowing MDX JSX and expressions to claim
  their ranges before Markdown link parsing.

### Block recognition

At a constrained line start, `MdxBlockProvider` dispatches one of three kinds:

| Kind | Start condition | Boundary owner |
|---|---|---|
| ESM | Top-level `import` or `export`, after at most three spaces | `MdxEsmScanner` |
| JSX | A parsed JSX tag or recoverable opening-tag prefix | `MdxJsxScanner` |
| Expression | `{` after at most three spaces | `MdxExpressionBoundaryScanner` |

A complete single-line construct is emitted immediately. Multiline constructs share the line and
finalization lifecycle in `MdxBlockMarkerBlock`, but use separate ownership policies:

- `MdxJsxBlockMarkerBlock` scans only through the lines the marker has observed. It allows nested
  Markdown once the opening tag is complete. One persistent JSX session retains its cursor, tag
  stack, incremental tag or expression parser, accumulated syntax ranges, and incrementally merged
  opacity. `MdxJsxTagParser` advances the tag grammar directly; it does not pre-scan and then replay
  a completed tag. Ambiguous malformed expressions fork only the candidates admitted by the shared
  recovery budget. Finalized code-fence and HTML-comment productions, together with provisional
  flow code spans, are added as explicit opaque ranges. When a code-span opener is observed, the
  remaining eligible paragraph segment is indexed once. Late opacity can replay the one line
  observed before a child marker took ownership and roll back syntax collected from that line.
  Accumulated results use an append-only committed prefix and an immutable provisional suffix, so a
  result retained by a caller is unchanged by that rollback. The marker reuses the session's
  detailed result when it is finalized. A nested JSX marker returns `PASS` after scheduling its
  close, so the ancestor continues to observe the boundary and owns its closing tag too.
- `MdxOpaqueBlockMarkerBlock` handles ESM and flow expressions. It never allows sub-blocks and
  returns `CANCEL`, keeping JavaScript content opaque to competing Markdown markers. Its persistent
  JavaScript scanner session retains lexer checkpoints and nesting state while the block grows.

JSX no longer captures an eager whole-source element range. That range could cross a Markdown block
whose ownership had not yet been established, making the result depend on edit order. Incomplete
JSX instead keeps the conservative incremental scan and `CANCEL` behavior. The scanner distinguishes
a matching closer, an immediate mismatch-recovery boundary, and an unterminated observed prefix;
the marker publishes matched and recovered roots, plus EOF recovery, but never a movable prefix.
Named tags and fragments only close identical identities.

`MdxJsxMarkdownConstraints` carries JSX ownership and tag or fragment identity through nested Markdown constraints.
It classifies a closing tag as current, ancestor, or mismatched. Every JSX wrapper survives each
list or blockquote modifier. The stored indentation stays separate from the number of consumed characters.

`MdxMarkdownConstraints` delegates modifier recognition to GFM and retains the indentation skipped by JSX.
List continuation uses the stored content indentation. Blockquotes can start beyond three spaces inside JSX.
Tabs advance to the next tab stop. Ordinary Markdown keeps the CommonMark continuation rules.

`MdxMarkdownOwnership` records each observed line with its actual constraints. It uses the configured
block providers and `ParagraphMarkerBlock` to determine paragraph ends. Code-span opacity and flow
paragraph projection share this information. This replaces separate guesses about headings, lists,
blockquotes, and fences.

`MdxMarkerProcessor.updateStateInfo` announces fence-opener opacity before existing JSX markers scan the line.
This protects JSX closers inside the fence info string. Completed fence and HTML-comment productions
then provide their full opaque ranges.

`MdxBlockNodeFactory` converts JSX and ESM results to Markdown nodes. It receives any leading paragraph
range from `MdxMarkdownOwnership`. Recognition and node projection remain separate.

### Inline recognition

`MdxInlineElementParser` scans inline ranges for:

- terminated JSX elements and recoverable opening-tag prefixes;
- balanced `{...}` expressions.

It excludes claimed Markdown tokens from later inline parsers and emits MDX JSX/expression nodes.
Placing it before `InlineLinkParser` prevents Markdown-looking text inside JSX attributes from being
interpreted as links. Inline parsing then continues in separate ownership spaces: the outer space
omits each complete JSX root but glues the text on either side so emphasis and links can wrap it;
each element body is parsed in its own inner space with tags, attributes, and expressions excluded.
Standalone expressions stay opaque within whichever space contains them. A delimiter inside an
element therefore cannot pair with one outside it.

When the scanner closes or recovers an element, it emits an immutable `ElementRecord`.
The record contains the parent index, full range, and optional body range. Indexes follow opening
order, while records follow closing order. Self-closing elements have no body range.

One token sweep assigns Markdown content to each body's owner. Each complete child stays opaque to
its parent, so parent emphasis and links can wrap the child without entering its body.
Flow parsing keeps the existing block-marker owners and avoids duplicate owners for nested flow elements.

### Scanner ownership

| Module | Responsibility |
|---|---|
| `MdxJsxScanner` | JSX tags, attributes, exact-identity nesting, immediate mismatch recovery, and JSX ranges |
| `MdxExpressionBoundaryScanner` | JavaScript `{...}` boundaries using the platform JS/JSX lexer |
| `MdxEsmScanner` | Top-level module-statement completeness, recovery, and missing separators |
| `MdxCodeFenceProvider` | Standard fence ownership plus recovery from an unclosed fence at an active JSX closer |
| `MdxMarkdownOwnership` | Shared paragraph boundaries and early fence-opener opacity from real Markdown constraints and providers |
| `MdxMarkdownCodeSpanScanner` | Provisional flow code-span opacity before paragraph inline parsing runs |
| `MdxHtmlCommentBoundary` | Closed multiline HTML-comment boundaries shared by block and JSX scanning |

`MdxExpressionBoundaryScanner` and `MdxEsmScanner` delegate strings, templates, comments, regular
expressions, and nested JSX tokenization to `JSFlexAdapter` through a prefix-bounded lexer. Sessions
commit tokens only at JavaScript base-state checkpoints; an unterminated lexical suffix is re-lexed
as more text becomes visible because its token boundaries can still change. ESM evaluates that
suffix against a disposable semantic snapshot. JSX expressions likewise keep provisional
continuations separate until their boundary becomes stable.

JSX scanning receives Markdown opacity from the block parser. Closed fences remain opaque when their
content contains a matching JSX closer. An unclosed fence yields only to the current or an ancestor
JSX closer. A mismatched closer remains fence content.

### Resulting Markdown structure

For this source:

```mdx
import Callout from './Callout'

<Callout>
## Important

- item one
</Callout>
```

the base tree is conceptually:

```text
MdxFile
├── MDX_ESM_BLOCK
│   └── EMBEDDED_JS_CONTENT
└── MDX_JSX_FLOW_ELEMENT
    ├── MDX_JSX_OPENING_ELEMENT
    ├── ATX_2
    ├── UNORDERED_LIST
    └── MDX_JSX_CLOSING_ELEMENT
```

The heading and list are real Markdown descendants of the JSX flow element.

## JavaScript template-data projection

`MdxTemplateDataElementType.collectTemplateModifications()` reparses the source with the same
`MdxFlavourDescriptor` and derives template roots from its AST. It does not maintain an independent
JSX/ESM text walk.

The projection performs these steps:

1. Collect top-level ESM, JSX, and expression roots.
2. Collect and merge code blocks, code fences, and code spans as ordered opaque Markdown ranges.
3. Sweep the ordered roots and opaque ranges once, subtracting opacity from JSX roots while
   retaining ESM and expression roots whole. Each resulting piece keeps its root kind.
4. Classify HTML comments with a moving cursor over merged lexer and Markdown code ranges, then
   subtract opaque multiline comments with another ordered sweep. Invalid single-line comments
   remain embedded so the JavaScript parser can diagnose them.
5. Sweep the embedded pieces to mark every non-embedded range as outer language and add virtual
   semicolons where adjacent JavaScript statements need separation.

Opaque-range indentation is indexed in one source pass. Projection therefore avoids rescanning
every opaque range for every root, every comment for every embedded piece, or every embedded piece
when deciding statement separators.

`MdxOuterLanguagePatcher` represents outer ranges as `\n;`. `MdxJSLanguageParser`, based on the ES6
parser with JSX enabled, parses the resulting virtual JavaScript file. The source document itself is
not rewritten.

The same Markdown flavour is therefore the semantic source of truth for both PSI roots, even though
the template-data pipeline performs its own parser invocation.

## Highlighting

Highlighting uses a parallel layered lexer pipeline:

```text
MdxHighlightingLexer
├── MdxHighlightingLexerBase
│   └── MarkdownToplevelLexer(MdxFlavourDescriptor)
└── MdxInlineHighlightingLexer for Markdown inline containers
```

`MdxHighlightingLexerBase` merges adjacent embedded-JavaScript spans. The inline layer collapses a
balanced `{...}` into one embedded token so JavaScript highlighting covers expressions in prose.
`MdxEditorHighlighter` registers the JavaScript/JSX highlighter for embedded content and JSX tag
tokens.

## Formatting

Formatting interleaves the two PSI roots:

- `MdxFormattingModelBuilder` builds the template-language model.
- `MdxBlock` represents Markdown blocks and delegates embedded ranges to data-language wrappers.
- `MdxJsFormattingModelBuilder` and its XML policy adapt JavaScript/JSX formatting to MDX.
- `MdxFileIndentOptionsProvider` uses JavaScript indentation settings for `.mdx` files.

Formatter changes must preserve both Markdown nesting and the projected JavaScript ranges.

## Structural invariants

- Markdown AST nodes, not a separate template scan, decide which source ranges are MDX.
- JSX flow children remain available to the Markdown block parser.
- A complete nested JSX marker is transparent to ancestor JSX markers; ESM, expressions, fences,
  and HTML comments are opaque.
- JSX scans are bounded by the marker's observed prefix. Incomplete editor input uses conservative
  recovery and must not publish a movable root over a Markdown sibling.
- JavaScript lexer input is bounded by that same observed prefix. Only restartable token prefixes
  become durable; unstable lexical suffixes and the semantic results derived from them stay
  provisional.
- A retained JSX scan result is immutable. Late opacity checks every supplied range against the
  current provisional scan interval before rolling that interval back. Repeated limits preserve the
  rollback point and reuse the current result.
- A matching JSX closer, an unrelated closer used for immediate recovery, and EOF are distinct
  scanner outcomes; named tags and fragments never close one another.
- JSX constraint wrappers retain tag identity and survive list and blockquote modifier recognition,
  preserving every ancestor closing boundary.
- Fence openers become opaque before existing JSX markers scan their line. Completed fence and
  HTML-comment productions provide the full opaque ranges.
- Code blocks, fences, and spans are subtracted from JSX roots during template projection.
- Inline Markdown delimiters pair only within the outer paragraph or one JSX element body; complete
  JSX roots and standalone expressions remain opaque gaps in their containing space.
- Closed multiline HTML comments are owned by a comment-only block provider. Single-line HTML
  comments retain the existing invalid-MDX diagnostics.
- Scanner source ranges use half-open `TextRange` values. `IntRange` is used only at Markdown
  token and node boundaries, where the Markdown API treats its final value as an exclusive offset.
- Incomplete editor input may produce recoverable MDX roots, but stable Markdown siblings must not be
  swallowed.

## Tests

Use the owning test module and fully qualified class names:

```sh
./tests.cmd --module intellij.mdx.tests --test org.intellij.plugin.mdx.MdxJsxScannerTest
./tests.cmd --module intellij.mdx.tests --test org.intellij.plugin.mdx.MdxEsmScannerTest
./tests.cmd --module intellij.mdx.tests --test org.intellij.plugin.mdx.MdxExpressionBoundaryScannerTest
./tests.cmd --module intellij.mdx.tests --test org.intellij.plugin.mdx.MdxCodeFenceProviderTest
./tests.cmd --module intellij.mdx.tests --test org.intellij.plugin.mdx.MdxMarkdownCodeSpanScannerTest
./tests.cmd --module intellij.mdx.tests --test org.intellij.plugin.mdx.MdxInlineElementParserTest
./tests.cmd --module intellij.mdx.tests --test org.intellij.plugin.mdx.MdxOracleTest
./tests.cmd --module intellij.mdx.tests --test org.intellij.plugin.mdx.MdxParsingTest
./tests.cmd --module intellij.mdx.tests --test 'org.intellij.plugin.mdx.*'
```

- Scanner tests pin the individual boundary contracts.
- `MdxOracleTest` asserts structural behavior against the official MDX model.
- `MdxParsingTest` checks both PSI roots against golden files under `testData/parsing`.
- `MdxLiveEditingTest`, `MdxHighlightTest`, and `MdxFormatterTest` cover editor integration.

Only regenerate golden files for an intentional PSI change. Run the exact parsing test with
`-Dpass.idea.tests.overwrite.data=true`, then inspect every resulting fixture diff.
