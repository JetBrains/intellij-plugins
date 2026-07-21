# MDX Parser & Formatter: Detailed Pipeline Analysis

---

## Part 1 — Official MDX Pipeline (`@mdx-js/mdx`)

The official pipeline is a **compiler**: it takes `.mdx` source and produces a JavaScript ES module.
It has 7 stages across 3 AST layers.

```
MDX source text
     │
     ▼ Stage 1 ── micromark (tokenizer)
  token stream
     │
     ▼ Stage 2 ── mdast-util-from-markdown + mdast-util-mdx (AST builder)
    mdast   ← Markdown AST; JSX/ESM/expressions are first-class nodes
     │
     ▼ Stage 3 ── remark plugins (user transforms on mdast)
    mdast
     │
     ▼ Stage 4 ── mdast-util-to-hast (convert to HTML AST)
    hast
     │
     ▼ Stage 5 ── rehype plugins (user transforms on hast)
    hast
     │
     ▼ Stage 6 ── rehype-recma (convert to JS AST)
    esast / estree
     │
     ▼ Stage 6b ── recma plugins (core MDX transforms + user transforms)
    esast
     │
     ▼ Stage 7 ── astring (code generator)
  JavaScript module (.js)
```

### Stage 1: Tokenization (micromark)

micromark is a **state-machine** tokenizer for CommonMark. MDX plugs in four extensions:

| Extension | Trigger | What it tokenizes |
|---|---|---|
| `mdxjs-esm` | line starts with `import ` or `export ` | ESM block until blank line |
| `mdx-jsx` | `<` not followed by whitespace | JSX tags, attributes, children |
| `mdx-expression` | `{` | balanced `{...}` expression spans |
| `mdx-md` | always | **disables** indented code, autolinks `<url>`, raw HTML, HTML comments |

The state machine processes characters one at a time, producing a flat event stream of
`(enter, exit, data)` events. Acorn is called inside `mdxjs-esm` and `mdx-expression` to
validate that the JavaScript is syntactically legal.

**Key tokenization rules:**
- `<` followed by whitespace → plain text (not JSX)
- `{` starts expression; balanced brace counting determines end
- ESM block ends at the first blank line or EOF
- Acorn validates JS on each line; if the last char causes an Acorn error the parser assumes
  the statement continues and reads the next line

### Stage 2: AST Construction (mdast)

`mdast-util-from-markdown` consumes the event stream and produces nodes.
`mdast-util-mdx` adds handlers for MDX-specific events:

| Node type | Example source | What it represents |
|---|---|---|
| `MdxjsEsm` | `import Foo from './Foo'` | top-level import/export |
| `MdxJsxFlowElement` | `<Foo>\n...\n</Foo>` | block-level JSX component |
| `MdxJsxTextElement` | `inline <Foo />` | inline JSX component |
| `MdxFlowExpression` | `{2 + 2}` on its own line | block-level JS expression |
| `MdxTextExpression` | `{name}` in prose | inline JS expression |

**Critical property**: `MdxJsxFlowElement.children` is a full mdast array — Markdown inside JSX
is parsed recursively as Markdown. Headings, lists, code fences, and paragraphs all work inside
a JSX component:

```
Source:
  <Callout>
    ## Important
    - item one
  </Callout>

mdast result:
  MdxJsxFlowElement { name: "Callout", children: [
    Heading { depth: 2 },
    List { children: [ListItem "item one"] }
  ]}
```

### Stages 3–5: remark / hast / rehype

Standard Unified pipeline. Plugins operate on their respective AST layer.
MDX-specific nodes survive as-is through the hast conversion.

### Stage 6: ESAST / Recma

`rehype-recma` converts hast to an estree-compatible AST. Core recma plugins then run:

- **`recma-build-jsx`**: compiles `MdxJsxFlowElement` → `_jsx(Component, {props}, children)` calls
- **`recma-stringify`**: wraps everything in a default-exported function `_createMdxContent(props)`
  and re-exports named exports

### Stage 7: Code Generation (astring)

astring serializes the estree to a JavaScript string. Output is a standard ES module for bundlers.

---

## Part 2 — Our Implementation Pipeline (IntelliJ plugin)

Our goal is NOT to compile MDX to JS — it is to provide **editor features**: syntax highlighting,
code completion, navigation, formatting, and inspections.

We use IntelliJ's **template language** infrastructure, which allows one file to have two separate
PSI trees parsed independently and then linked together.

```
.mdx file on disk
       │
       ▼  MdxFileViewProviderFactory
  MdxFileViewProvider
  ├── [base language]           MdxLanguage (Markdown-based)
  │        │
  │        ▼ Phase 2: MdxParserDefinition
  │   Markdown PSI tree
  │   (contains opaque JSX_BLOCK nodes)
  │
  └── [template data language]  MdxJSLanguage (ES6 + JSX dialect)
           │
           ▼ Phase 3: MdxTemplateDataElementType → MdxJSParserDefinition
      JS/JSX PSI tree
      (stitched from JSX_BLOCK_CONTENT regions)
```

### Phase 1: File Open → ViewProvider

`MdxFileViewProviderFactory.createFileViewProvider()` returns `MdxFileViewProvider`.

`MdxFileViewProvider` extends both:
- `MultiplePsiFilesPerDocumentFileViewProvider` — manages multiple PSI roots for one document
- `TemplateLanguageFileViewProvider` — declares which language is "base" vs "template data"

When IntelliJ needs a PSI file for a given language it calls `createFile(lang)`:
- `lang == MdxLanguage` → standard parse using `MdxParserDefinition`
- `lang == MdxJSLanguage` → creates JS `PsiFileImpl` and sets `contentElementType = MdxTemplateDataElementType`

### Phase 2: Markdown PSI Tree

**2a. Lexer — `MarkdownToplevelLexer(MdxFlavourDescriptor)`**

`MarkdownToplevelLexer` is unusual: it runs the **full Markdown parser** internally and then replays
the resulting token sequence. The "lexer" is a cached parser run.

`MdxFlavourDescriptor` (in `lang/parse/MdxHighlightingLexerBase.kt`) extends `CommonMarkFlavourDescriptor`:
- `sequentialParserManager` → delegated to `GFMFlavourDescriptor` (GFM inlines: tables, strikethrough)
- `createInlinesLexer()` → delegated to `GFMFlavourDescriptor`
- `markerProcessorFactory` → `MdxProcessFactory` → creates `MdxMarkerProcessor`

**2b. Parser — `MarkdownParserAdapter(MdxFlavourDescriptor)`**

Wraps the token stream in a `PsiBuilder` and builds the PSI tree.

**2c. Block providers in `MdxMarkerProcessor`**

For each new line, the processor asks each block provider in order whether it wants to open a block:

```
1. CodeBlockProvider       (indented code: 4 spaces)
2. HorizontalRuleProvider  (--- or ***)
3. CodeFenceProvider       (``` or ~~~)
4. SetextHeaderProvider    (underline === or ---)
5. BlockQuoteProvider      (> prefix)
6. ListMarkerProvider      (- / * / 1.)
7. JsxBlockProvider        ← MDX custom (runs before HtmlBlockProvider)
8. HtmlBlockProvider
9. GitHubTableMarkerProvider
10. AtxHeaderProvider      (# headings)
11. CommentAwareLinkReferenceDefinitionProvider
```

**2d. `JsxBlockProvider.matches()` — detection logic** (`lang/parse/JsxBlockProvider.kt`)

Called on every line start. Returns a group index (0–3) for JSX, 6 for import/export, or -1.

```
text[0] != '<' → check FIND_START_IMPORT_EXPORT regex (import/export keyword)
text[0] == '<' → run FIND_START_REGEX against the line:
  group 0: <script|pre|style (close regex: </script|style|pre>)
  group 1: known block-level HTML tag names  (close: blank line)
  group 2: any complete open/close/self-closing tag  (close: blank line)
  group 3: <TagName...  (multiline, no close regex — wait for blank + empty stack)
```

After matching, `JsxBlockUtil.parseParenthesis()` scans the first line to seed the tag stack and
emit initial `JSX_BLOCK_CONTENT` tokens.

**2e. `JsxBlockMarkerBlock.doProcessToken()` — consuming the block body** (`lang/parse/JsxBlockMarkerBlock.kt`)

*JSX block mode* (`isExportImport = false`):
- On each new line, calls `JsxBlockUtil.parseParenthesis()` which:
  - Finds all tag matches via `TAG_REGEX`
  - Pushes open tags onto `tagOrBracketStack`, pops on close tags
  - Text between tags → `JSX_BLOCK_CONTENT` if inside a tag, `TEXT` if outside
- Block ends when `endCheckingRegex` matches the previous line, OR 2+ blank lines with empty stack
- **`allowsSubBlocks() = false`** — the Markdown parser does NOT recurse into this block

*Import/export mode* (`isExportImport = true`):
- Calls `JsxBlockUtil.parseExportParenthesis()` which tracks `{` `(` `}` `)` balance
- Blank line + empty bracket stack → block ends
- Blank line + non-empty stack → keep consuming (multi-line object literal)

**2f. Output: Markdown PSI structure**

```
MdxFile
├── MarkdownParagraph ("# Heading")
├── JSX_BLOCK                         ← opaque; allowsSubBlocks=false
│   ├── JSX_BLOCK_CONTENT "<Callout>"
│   ├── TEXT " some content "         ← TEXT because outside any nested tag
│   └── JSX_BLOCK_CONTENT "</Callout>"
├── JSX_BLOCK                         ← import statement
│   └── JSX_BLOCK_CONTENT "import Foo from './Foo'"
└── MarkdownParagraph ("more text")
```

Note: content inside the JSX block is NOT parsed as Markdown. The `JSX_BLOCK_CONTENT` tokens
are opaque from the Markdown parser's perspective. This is the root cause of WEB-78468.

### Phase 3: JS/JSX PSI Tree (Template Data)

**3a. `MdxTemplateDataElementType.collectTemplateModifications()`** (`lang/psi/MdxTemplateDataElementType.kt`)

Runs the base lexer over the source. Tokens are classified:
- `JSX_BLOCK_CONTENT` → kept as JS template data
- Any other token → `addOuterRange(range)` → replaced by `MdxOuterLanguagePatcher` with `"\n;"`

Special handling for import/export: if the consumed block doesn't end with `;`, inserts one via
`modifications.addRangeToRemove()`.

**3b. `MdxOuterLanguagePatcher`** (`lang/psi/MdxOuterLanguagePatcher.kt`)

Returns `"\n;"` for every outer range placeholder. This keeps the stitched JS file syntactically
valid — each Markdown paragraph becomes an empty JS statement.

**3c. Virtual JS file (conceptual)**

Given this source:
```mdx
# Hello world
import Foo from './Foo'

<Foo>
  some content
</Foo>

More text here
```

The virtual JS file seen by the JS parser:
```js
\n;                       // "# Hello world" replaced
import Foo from './Foo';  // kept, semicolon ensured
\n;
<Foo>
\n;                       // "  some content" is TEXT token → replaced
</Foo>
\n;                       // "More text here" replaced
```

**3d. `MdxJSParserDefinition` + `MdxJSLanguageParser`** (`js/MdxJSLanguageParser.kt`)

The virtual file is parsed by `MdxJSParserDefinition` (extends `ECMA6ParserDefinition`).
`MdxJSLanguageParser` extends `ES6Parser` with one override in `statementParser`: if the current
token is `XML_START_TAG_START`, it parses the JSX expression as an expression statement rather
than expecting a normal JS statement. Everything else is standard ES6 parsing.

**3e. Output: JS PSI tree**

```
JSFile (MdxJSLanguage)
├── OuterLanguageElement "\n;"         ← placeholder for "# Hello world"
├── ES6ImportDeclaration "import Foo from './Foo'"
├── OuterLanguageElement "\n;"
├── ExpressionStatement
│   └── XmlElement <Foo>
│       ├── OuterLanguageElement "\n;" ← placeholder for "some content"
│       └── XmlClosingTag </Foo>
└── OuterLanguageElement "\n;"         ← placeholder for "More text here"
```

### Phase 4: Syntax Highlighting

Separate from parsing — must be fast and incremental.

```
MdxEditorHighlighterProvider
  └── MdxEditorHighlighter
        └── MdxHighlightingLexer  (LayeredLexer)
              ├── base: MdxHighlightingLexerBase
              │     MergingLexerAdapterBase(MarkdownToplevelLexer(MdxFlavourDescriptor))
              │     merge function: collapses consecutive JSX_BLOCK_CONTENT spans into one,
              │     stopping at double-newlines
              └── layer: MarkdownMergingLexer
                    registered for INLINE_HOLDING_ELEMENT_TYPES
                    handles inline elements (bold, italic, code spans, links)
```

`MdxSyntaxHighlighter` (extends `MarkdownSyntaxHighlighter`) maps token types → `TextAttributesKey`.

### Phase 5: Formatting

**5a. Entry: `MdxFormattingModelBuilder.createModel()`** (`format/MdxFormattingModelBuilder.kt`)

- Node is `OUTER_ELEMENT_TYPE` → `SimpleTemplateLanguageFormattingModelBuilder` (no-op for outer elements)
- Otherwise → `DocumentBasedFormattingModel` with the full block tree

**5b. Block tree — `MdxBlock`**

`TemplateLanguageFormattingModelBuilder.getRootBlock()` builds a hybrid block tree interleaving
`MdxBlock` (Markdown) and `DataLanguageBlockWrapper` (JS) nodes.

`MdxBlock.getIndent()`:
```
if node text is whitespace-only              → NoneIndent
else if DataLanguageBlockWrapper ancestor exists:
    if HtmlPolicy says don't indent children → NoneIndent
    else                                     → NormalIndent
else                                         → NoneIndent
```

`MdxBlock.getChildAttributes()`:
- `JSX_BLOCK_CONTENT` children → `NormalIndent`
- other children → `NoneIndent`

**5c. JS/JSX formatting — `MdxJsFormattingModelBuilder`** (`format/MdxJsFormattingModelBuilder.kt`)

Extends `JavascriptFormattingModelBuilder`. Overrides `createModel()` to inject a custom XML policy
via `getPolicy()`:

*JSX mode* (`DialectDetector.isJSX()` → true, custom `HtmlPolicy`):
- `isInlineTag(tag)` → true when tag name is capitalized (treats `<MyComponent>` as inline)
- `insertLineBreakBeforeTag()` → always false
- `allowWrapBeforeText()` → false
- `getWrappingTypeForTagBegin()` → `NONE` inside return statements or after text siblings;
  otherwise delegates up

*Non-JSX mode* (custom `XmlPolicy`):
- Root-level tags get `NORMAL` wrap; nested tags use default

**5d. Spacing — `MdxJsBlockContext` + `MdxJsSpacingProcessor`**

`MdxJsBlockContext` overrides `createSpacingStrategy()` to wire in `MdxJsSpacingProcessor`
for MDX-specific spacing rules between AST nodes in the JS/JSX tree.

**5e. Indent options — `MdxFileIndentOptionsProvider`**

Delegates indent tab/space settings for `.mdx` files to the JS/JSX code style settings.

---

## Key Structural Comparison

| | Official MDX | Our Implementation |
|---|---|---|
| **Goal** | Compile MDX → JavaScript module | IDE features (highlight, complete, navigate, format) |
| **JSX nesting** | Fully recursive mdast (Markdown inside JSX = Markdown nodes) | Flat opaque `JSX_BLOCK_CONTENT`; `allowsSubBlocks=false` |
| **Expression `{}`** | First-class; Acorn-validated | Not explicitly handled; treated as JS text in virtual file |
| **Two parse trees** | No — one unified AST | Yes — Markdown PSI + JS PSI linked via template language |
| **JS validation** | Acorn inside micromark extensions | ES6Parser on stitched virtual file |
| **Formatter** | None (Prettier plugin) | Two-tier: TemplateLanguageFormattingModelBuilder + JavascriptFormattingModelBuilder |
| **Core limitation** | None for parsing | `JsxBlockProvider` regex can't handle Markdown inside JSX or `<` in code blocks |

---

## Parsing test workflow

Two test classes drive the parsing gate (run **one class at a time** — running several MDX test
classes together pollutes JVM/application state and yields false failures):

```sh
./tests.cmd --module intellij.mdx.tests --test 'org.intellij.plugin.mdx.MdxParsingTest'
./tests.cmd --module intellij.mdx.tests --test 'org.intellij.plugin.mdx.MdxRedesignTargetTest'   # append #methodName for one test
```

- **`MdxParsingTest`** — golden self-snapshots (`checkAllPsiRoots`). Each `parsing/<Name>.mdx` has a
  `<Name>.MDX.txt` (base Markdown PSI) and `<Name>.MdxJS.txt` (template-data JS PSI). These capture
  CURRENT behavior as a regression net; they are all green by construction. Regenerate after an
  intentional change: add `-Dpass.idea.tests.overwrite.data=true` to the run (overwrites the
  committed `.txt` snapshots in place), then review the diff.
- **`MdxRedesignTargetTest`** — correctness/acceptance, keyed to the mdast oracle
  (`testData/oracle/<Name>.mdast.json`, the mdxjs.com toolchain). Tests that are RED here are
  intentional: they assert spec-correct behaviour the current parser does not yet produce. Do NOT
  weaken them; they go green only when the parser is fixed.

### Which `parsing/` fixtures are spec-correct vs capture-current-bug

Cross-checked against `testData/oracle/*.mdast.json`:

| Spec-correct (PSI already matches the oracle) | Captures a current bug (snapshot ≠ oracle; redesign target) |
|---|---|
| ParsingFrontMatter, ParsingTomlFrontMatter (`yaml`/`toml`) | ParsingCodeBlockInJsx, ParsingGenericsInCodeBlock, ParsingJsxExpressionAttribute (opaque fence / bogus JSX errors) |
| ParsingGfmTable (`table`), ParsingStrikethrough (`delete`) | ParsingMarkdownInJsx, ParsingMarkdownInJsxIndented (Markdown inside JSX is opaque) |
| ParsingNestedFence (opaque `code`), ParsingTsxCodeBlock | ParsingTaskList (`[ ]`/`[x]` not GFM checkboxes) |
| ParsingMemberExpressionComponent (`Foo.Bar`), ParsingExportDefault (ESM) | ParsingGfmAlert (`[!NOTE]` parsed as reference link) |
| ParsingInlineJsxInEmphasis, ParsingJsxInBlockquote, ParsingJsxInListItem | ParsingEmptyExpression (`{}` is text, not an expression) |
| ParsingFragments, ParsingOperatorInAttribute, ParsingJsxInAttributeArray | ParsingFlowExpressionWithJsx (`{a < b ? ...}` split, not one expression) |
| ParsingCrlf, ParsingWhitespaceOnly, ParsingEsm, ParsingMultilineEsm | ParsingJsxInOrderedList, ParsingMultilineJsxAttribute (JSX/body opaque under list/element) |
| ParsingList, ParsingExpressions, ParsingNestedComponents, ParsingInlineJsx | ParsingImportInProse (BOL `import`/`export` → oracle parseError) |
| ParsingInlineExpressionComment, ParsingJsxInMarkdownInline, ParsingEmbedded | ParsingHtmlComment, ParsingAutolink (invalid MDX → oracle parseError) |
| ParsingAlert (a JSX `<Alert>` component), ParsingPrisma, ParsingLongText | ParsingIndentedCode (indented code disabled in MDX; oracle = paragraph) |
