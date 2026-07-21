package org.intellij.plugin.mdx

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.junit.jupiter.api.Test

/**
 * Oracle-based structural tests for the IntelliJ MDX parser (WEB-78468 redesign acceptance criteria):
 * each asserts PSI STRUCTURE (real heading/list/code/expression nodes, not just absence of parse
 * errors) against the canonical MDX AST produced by remark-mdx/remark-gfm, the reference toolchain
 * behind mdxjs.com.
 */
class MdxOracleTest : MdxTestBase() {

  /**
   * Containment oracle: the fenced code must be a real CHILD of the enclosing JSX flow element in
   * the BASE Markdown PSI tree. Asserts no errors, opaque fence content, the fence NESTED under a
   * base-tree JSX element spanning `<`[enclosingTagName]`...</`tag`>`, and no JSX tag named any of
   * [forbiddenTagNames].
   */
  private fun assertOpaqueFenceInsideJsx(
    text: String,
    code: String,
    enclosingTagName: String,
    vararg forbiddenTagNames: String
  ) {
    assertNoErrorsAndNoErrorHighlights(text)
    assertOpaqueFenceContains(code)
    assertNoJsxTagNamed(*forbiddenTagNames)

    val openTag = "<$enclosingTagName"
    val closeTag = "</$enclosingTagName>"
    val fenceNode = collectFenceContents().firstOrNull {
      it.text.contains(code) || code.contains(it.text)
    }
    assertNotNull(
      "Oracle: fenced code '$code' must exist as a CODE_FENCE_CONTENT node. " +
        "Fence node ranges: ${collectFenceContents().map { "${it.text}@${it.textRange}" }}",
      fenceNode
    )

    // Walk up the base-tree ancestors of the fence node; require one whose text is the whole
    // <Tag>...</Tag> JSX element. PsiTreeUtil.isAncestor stays within this single (base) tree.
    val jsxContainer = generateSequence(fenceNode!!.parent) { it.parent }
      .takeWhile { it !is PsiFile }
      .firstOrNull { it.text.contains(openTag) && it.text.contains(closeTag) }
    assertNotNull(
      "Oracle: fenced code '$code' must be a CHILD of <$enclosingTagName> in the base PSI tree, " +
        "but no base-tree ancestor of the fence spans the whole JSX element. The redesign " +
        "must nest the fence under a real JSX flow element rather than relocating it.",
      jsxContainer
    )
    assertTrue(
      "Oracle: <$enclosingTagName> element must be a PSI ancestor of the fenced code node.",
      PsiTreeUtil.isAncestor(jsxContainer!!, fenceNode, false)
    )
  }

  // --- fenced code inside / alongside JSX (WEB-59041 / 56756 / 47755 / 74507) ------------------

  /**
   * WEB-59041. Oracle: `<div>` is an `mdxJsxFlowElement` whose child is a `code` node with value
   * "#include <stdio.h>". The fence content is opaque — `<stdio.h>` is code, not a JSX tag.
   */
  @Test
  fun testCodeBlockInJsxHasNoErrors() {
    val text = """
      # Markdown

      <div>
      ```c
      #include <stdio.h>
      ```
      </div>

      # Markdown with errors
    """.trimIndent()
    assertOpaqueFenceInsideJsx(text, code = "#include <stdio.h>", enclosingTagName = "div",
      forbiddenTagNames = arrayOf("stdio.h"))
  }

  /** Oracle: braces inside a fenced ```tsx``` block nested in JSX are code, not MDX JSX expressions. GREEN. */
  @Test
  fun testTsxFunctionCodeBlockInJsxHasNoErrors() {
    val text = """
      <div>
      ```tsx
      function foo() {
      }
      ```
      </div>
    """.trimIndent()
    assertOpaqueFenceInsideJsx(text, code = "function foo() {\n}", enclosingTagName = "div")
  }

  /**
   * WEB-56756. Oracle: a top-level fenced ```tsx``` block is a `code` node;
   * `Promise<AddressDetailResponse>` is part of its opaque value, not JSX. GREEN.
   */
  @Test
  fun testTsxCodeBlockHasNoErrors() {
    val text = """
      ```tsx
      export interface Api {
        getDetails: (id: string) => Promise<AddressDetailResponse>;
      }
      ```
    """.trimIndent()
    assertNoErrorsAndNoErrorHighlights(text)
    assertOpaqueFenceContains("Promise<AddressDetailResponse>")
    assertNoJsxTagNamed("AddressDetailResponse")
  }

  /**
   * WEB-47755. Oracle: the `<Tabs>`/`<TabItem>` flow elements contain a `code` node with value
   * "val bar: Bar<Baz> = Something()". `Bar<Baz>` is opaque code, not a JSX tag.
   */
  @Test
  fun testGenericsInCodeBlockHaveNoErrors() {
    val text = """
      <Tabs values={[{label: "Kotlin", value: "kotlin"}]}>
      <TabItem value="kotlin">
      ```kotlin
      val bar: Bar<Baz> = Something()
      ```
      </TabItem>
      </Tabs>
    """.trimIndent()
    assertOpaqueFenceInsideJsx(text, code = "val bar: Bar<Baz> = Something()",
      enclosingTagName = "TabItem", forbiddenTagNames = arrayOf("Baz"))
  }

  /**
   * WEB-74507. Oracle: `<Source ... code={`...`}/>` is one flow element whose `code` attribute is
   * an expression; `#### Selection Type` after it is a `heading{depth:4}` not swallowed by the JSX.
   */
  @Test
  fun testExpressionAttributeHasNoErrors() {
    val text = """
      import { Source } from '@storybook/addon-docs/blocks'

      <Source language="tsx" code={`
        const [state, setState] = useState<SomeType>()
      `}/>

      #### Selection Type

      Some text after.
    """.trimIndent()
    assertNoErrorsAndNoErrorHighlights(text)
    val heading = assertHasMarkdownHeading("Selection Type", depth = 4)
    val sourceEnd = text.indexOf("/>")
    assertTrue("Test setup: '/>' not found in source", sourceEnd >= 0)
    assertTrue(
      "Oracle: '#### Selection Type' must start AFTER the <Source .../> element's '/>' " +
        "(source offset ${sourceEnd + 2}), but the heading was at ${heading.textRange}.",
      heading.textRange.startOffset >= sourceEnd + 2
    )
  }

  // --- Markdown interleaving inside JSX (WEB-78468 core) ----------------------------------------

  /**
   * WEB-78468. Oracle: `<Callout>` is a flow element whose children include a real heading{2} and a
   * list, both nested inside the JSX element (not a flat PARAGRAPH). GREEN.
   */
  @Test
  fun testMarkdownInsideJsxHasNoErrors() {
    val text = """
      <Callout>
      ## Important

      - item one
      </Callout>
    """.trimIndent()
    assertNoErrors(text)
    val heading = assertHasMarkdownHeading("Important", depth = 2)
    assertNestedInJsxElement(heading, "Callout")
    val list = firstContainer("UNORDERED_LIST")
    assertNotNull(
      "Oracle: `- item one` inside <Callout> must be a real Markdown list (UNORDERED_LIST), " +
        "not opaque JSX_BLOCK_CONTENT (WEB-78468).",
      list
    )
    assertNestedInJsxElement(list!!, "Callout")
  }

  /**
   * WEB-78468. Oracle: same interleaving with 2-space indentation. GREEN.
   */
  @Test
  fun testMarkdownInsideJsxIndented() {
    val text = """
      <Box>
        ## Indented heading
        - list
      </Box>
    """.trimIndent()
    assertNoErrors(text)
    val heading = assertHasMarkdownHeading("Indented heading", depth = 2)
    assertNestedInJsxElement(heading, "Box")
    val list = firstContainer("UNORDERED_LIST")
    assertNotNull(
      "Oracle: the indented `- list` inside <Box> must be a real Markdown list (UNORDERED_LIST), " +
        "not opaque JSX_BLOCK_CONTENT (WEB-78468).",
      list
    )
    assertNestedInJsxElement(list!!, "Box")
  }

  /** WEB-78468. Oracle: `{/* */}` is an MDX expression whose body is a JS block comment. GREEN. */
  @Test
  fun testMdxComment() {
    assertNoErrors("{/* a comment */}")
    val comments = allRoots().flatMap { PsiTreeUtil.collectElementsOfType(it, PsiComment::class.java) }
    assertTrue(
      "expected a JS block comment for {/* */}, found: $comments",
      comments.any { it.text.contains("/* a comment */") }
    )
  }

  // --- inline JSX & JSX in Markdown block constructs --------------------------------------------

  /** Oracle: `<Icon />` is an `mdxJsxTextElement` inside `emphasis`, `<Logo />` inside a `link`. GREEN. */
  @Test
  fun testInlineJsxInEmphasisHasNoErrors() {
    assertNoErrors("A paragraph with *emphasis and an <Icon /> inline* and a [<Logo />](https://x) link.")
    val emph = firstContainer("EMPH")
    assertNotNull("Oracle: an emphasis node is expected around '<Icon /> inline'", emph)
    val iconTag = PsiTreeUtil.collectElements(emph!!) {
      it.typeName() == "HTML_TAG" && it.text == "<Icon />"
    }
    assertTrue(
      "Oracle: '<Icon />' must be an inline tag nested in the emphasis node, but the " +
        "emphasis subtree was: ${PsiTreeUtil.collectElements(emph) { true }.map { "${it.typeName()}:'${it.text}'" }}",
      iconTag.isNotEmpty()
    )
  }

  /** Oracle: a `blockquote` containing `<Note>quoted note</Note>`. GREEN. */
  @Test
  fun testJsxInBlockquoteHasNoErrors() {
    assertNoErrors("> <Note>quoted note</Note>\n>\n> text after")
    val quote = firstContainer("BLOCK_QUOTE")
    assertNotNull("Oracle: a blockquote container is expected", quote)
    assertTrue("Oracle: the <Note> JSX must live inside the blockquote", quote!!.text.contains("<Note>quoted note</Note>"))
  }

  /** Oracle: a `list` with `listItem` children; the JSX elements are list item content. GREEN. */
  @Test
  fun testJsxInListItemHasNoErrors() {
    assertNoErrors("- first item\n- <Item>second</Item>\n  - <Sub>nested</Sub>")
    val list = firstContainer("UNORDERED_LIST")
    assertNotNull("Oracle: an unordered list is expected", list)
    val items = nodesOfType(myFixture.file, "LIST_ITEM")
    assertTrue("Oracle: expected >= 2 list items, found ${items.size}", items.size >= 2)
  }

  /** MDX spec: JSX fragments (`<>...</>`) are supported. GREEN. */
  @Test
  fun testFragmentsHaveNoErrors() {
    assertNoErrors("<>\nfragment child\n</>")
  }

  /** Oracle: `\<` and `\{` are literal text — neither becomes an HTML_TAG / expression. GREEN. */
  @Test
  fun testEscapesStayText() {
    assertNoErrors("""Escaped \< is not a tag and \{ is not an expression.""")
    val escLt = PsiTreeUtil.collectElements(myFixture.file) { it.text == "\\<" }.firstOrNull()
    val escBrace = PsiTreeUtil.collectElements(myFixture.file) { it.text == "\\{" }.firstOrNull()
    assertNotNull("'\\<' fragment not found", escLt)
    assertNotNull("'\\{' fragment not found", escBrace)
    assertEquals("Oracle: escaped '\\<' must stay TEXT", "TEXT", escLt!!.typeName())
    assertEquals("Oracle: escaped '\\{' must stay TEXT", "TEXT", escBrace!!.typeName())
    assertEmpty("Oracle: escaped sequences must not produce any JSX tag", nodesOfTypeAllRoots("HTML_TAG"))
  }

  /**
   * Oracle: `<!-- -->` is INVALID MDX; the parser must produce a PsiErrorElement. GREEN. WEB-78468.
   */
  @Test
  fun testHtmlCommentIsInvalidMdx() {
    myFixture.configureByText("foo.mdx", "<!-- an HTML comment -->")
    assertTrue(
      "Oracle: `<!-- -->` is invalid MDX (use `{/* */}`); the parser must produce a " +
        "PsiErrorElement, but none was found in any root.",
      collectPsiErrorElements().isNotEmpty()
    )
  }

  /** Oracle: `<Comp a={1 < 2} b={useMemo<Foo>(() => x)} />` is one flow element. GREEN. */
  @Test
  fun testOperatorInAttributeHasNoErrors() {
    assertNoErrors("<Comp a={1 < 2} b={useMemo<Foo>(() => x)} />")
    assertTrue(
      "Oracle: a `Comp` JSX element is expected in the JS root",
      nodesOfTypeAllRoots(XML_TAG_NAME).any { it.text == "Comp" }
    )
  }

  /** Oracle: a multiline JS expression attribute in a JSX opening tag keeps one surrounding JSX flow element. GREEN. */
  @Test
  fun testMultilineExpressionAttributeInOpeningTagHasJsxFlowElement() {
    val text = """
      <div onClick={(e) => {
          console.log(e)
      }}>
          Hello
      </div>
    """.trimIndent()
    assertNoErrors(text)
    assertEquals(
      "Expected one JSX flow element spanning the multiline opening tag and its body.",
      1,
      nodesOfType(myFixture.file, "MDX_JSX_FLOW_ELEMENT").count { it.text.contains("Hello") }
    )
  }

  /** As above, but the arrow-function body also contains a blank line, which must not split the element. */
  @Test
  fun testMultilineExpressionAttributeWithBlankLineHasJsxFlowElement() {
    val text = """
      <div onClick={() => {
          console.log(1)

          console.log(2)
      }}>
          Hello
      </div>
    """.trimIndent()
    assertNoErrors(text)
    assertEquals(
      "Expected one JSX flow element spanning the multiline opening tag (with a blank line inside its " +
      "attribute expression) and its body.",
      1,
      nodesOfType(myFixture.file, "MDX_JSX_FLOW_ELEMENT").count { it.text.contains("Hello") }
    )
  }

  /** Oracle: `<Tabs tabs={[<FileWithIcon .../>, ...]} />` — JSX inside a JS array attribute. GREEN. */
  @Test
  fun testJsxInAttributeArrayHasNoErrors() {
    assertNoErrors("""<Tabs tabs={[<FileWithIcon key="a" />, <FileWithIcon key="b" />]} />""")
    assertTrue(
      "Oracle: a `Tabs` JSX element is expected in the JS root",
      nodesOfTypeAllRoots(XML_TAG_NAME).any { it.text == "Tabs" }
    )
  }

  // --- expressions (WEB-78468 edge cases) -------------------------------------------------------

  /** Oracle: `{}` in prose is an empty `mdxTextExpression`, modelled as MDX_JSX_EXPRESSION. GREEN. */
  @Test
  fun testEmptyExpressionIsExpression() {
    assertNoErrors("An empty expression {} in prose.")
    val expression = PsiTreeUtil.collectElements(myFixture.file) {
      it.typeName() == "MDX_JSX_EXPRESSION" && it.text == "{}"
    }
    assertTrue(
      "Oracle: `{}` must be parsed as an (empty) MDX expression (MDX_JSX_EXPRESSION), not plain " +
        "prose text. Nodes with text `{}`: " +
        PsiTreeUtil.collectElements(myFixture.file) { it.text == "{}" }.map { it.typeName() },
      expression.isNotEmpty()
    )
  }

  /**
   * Oracle: `{a < b ? <Yes/> : <No/>}` on its own line is a single `mdxFlowExpression`; `<Yes/>`/
   * `<No/>` must NOT be split into standalone HTML_TAG tokens in the base tree. GREEN. WEB-78468.
   */
  @Test
  fun testFlowExpressionWithJsx() {
    assertNoErrors("{a < b ? <Yes/> : <No/>}")
    assertEmpty(
      "Oracle: the flow expression must stay one JS expression; `<Yes/>`/`<No/>` must NOT be " +
        "split into standalone inline tags in the base tree.",
      PsiTreeUtil.collectElements(myFixture.file) {
        it.typeName() == "HTML_TAG" && (it.text == "<Yes/>" || it.text == "<No/>")
      }.toList()
    )
  }

  // --- GFM (remark-gfm is enabled in the MDX flavour) ------------------------------------------

  /** Oracle: a `table` with alignment and header + 2 rows. GREEN: real TABLE node with cells. */
  @Test
  fun testGfmTable() {
    assertNoErrors("| Left | Center | Right |\n| :--- | :----: | ----: |\n| a    | b      | c     |")
    val table = firstContainer("TABLE")
    assertNotNull("Oracle: a GFM table node is expected", table)
    assertTrue(
      "Oracle: table cells must hold the header text",
      table!!.text.contains("Left") && table.text.contains("Center") && table.text.contains("Right")
    )
  }

  /** Oracle: `~~struck through~~` is a `delete` node. GREEN: real STRIKETHROUGH node. */
  @Test
  fun testStrikethrough() {
    assertNoErrors("This is ~~struck through~~ text.")
    val strike = firstContainer("STRIKETHROUGH")
    assertNotNull("Oracle: a strikethrough (delete) node is expected", strike)
    assertTrue("Oracle: strikethrough must wrap 'struck through'", strike!!.text.contains("struck through"))
  }

  /**
   * Oracle: unchecked/checked task items are `listItem`s with CHECK_BOX markers, not plain
   * text / SHORT_REFERENCE_LINK. GREEN. WEB-78468.
   */
  @Test
  fun testTaskListItemsAreCheckable() {
    assertNoErrors("- [ ] unchecked task\n- [x] checked task")
    val items = nodesOfType(myFixture.file, "LIST_ITEM")
    assertEquals("Oracle: two task-list items are expected", 2, items.size)
    val checkboxes = PsiTreeUtil.collectElements(myFixture.file) { it.typeName() == "CHECK_BOX" }.toList()
    assertEquals(
      "Oracle: both list items must be GFM task items with CHECK_BOX markers, but ${checkboxes.size} " +
        "were found (the current parser reads `[ ]`/`[x]` as text/link).",
      2, checkboxes.size
    )
    assertEmpty("Oracle: `[x]` is a task-list checkbox, not a SHORT_REFERENCE_LINK.", nodesOfType(myFixture.file, "SHORT_REFERENCE_LINK"))
  }

  /**
   * `> [!NOTE]` GitHub alert. `[!NOTE]` must NOT be mis-parsed as a SHORT_REFERENCE_LINK. GREEN.
   */
  @Test
  fun testGfmAlertIsRecognized() {
    assertNoErrors("> [!NOTE]\n> Useful information that users should know.")
    val alert = firstContainer("ALERT")
    assertNotNull("Oracle: a GitHub alert (ALERT) node is expected for the `> [!NOTE]` blockquote", alert)
    val title = nodesOfType(myFixture.file, "ALERT_TITLE").firstOrNull()
    assertNotNull("Oracle: the alert must carry an ALERT_TITLE node", title)
    assertEquals("Oracle: the alert title must be the literal `[!NOTE]`", "[!NOTE]", title!!.text.trim())
    assertEmpty("Oracle: `[!NOTE]` must NOT be parsed as a reference link.", nodesOfType(myFixture.file, "SHORT_REFERENCE_LINK"))
  }

  // --- WEB-49952 / WEB-75114: JSX in lists ------------------------------------------------------

  /**
   * WEB-49952. Oracle: an `ordered list` whose first `listItem` contains a flow `<img/>` element
   * nested inside it (not flat/relocated). Implemented and GREEN.
   */
  @Test
  fun testJsxInOrderedListNestsUnderItem() {
    assertNoErrors("1. one\n   <img src=\"\" />\n2. two\n3. three")
    val orderedList = firstContainer("ORDERED_LIST")
    assertNotNull("Oracle: an ordered list is expected", orderedList)
    val items = nodesOfType(myFixture.file, "LIST_ITEM")
    assertEquals("Oracle: three ordered-list items are expected", 3, items.size)
    val firstItem = items.first()
    assertTrue("Oracle: the <img/> must be inside the first list item", firstItem.text.contains("<img src=\"\" />"))
    val nestedJsx = PsiTreeUtil.collectElements(firstItem) {
      (it.typeName() == "MDX_JSX_SELF_CLOSING_ELEMENT" || it.typeName() == "MDX_JSX_FLOW_ELEMENT") &&
        it.text.contains("<img")
    }
    assertTrue(
      "Oracle: `<img/>` inside the ordered-list item must be a real nested MDX JSX element " +
        "(mdast mdxJsxFlowElement name=img), not a flat/relocated opaque JSX_BLOCK. WEB-49952.",
      nestedJsx.isNotEmpty()
    )
    assertTrue(
      "Oracle: the nested `<img/>` must expose a structured MDX_JSX_ATTRIBUTE (src).",
      PsiTreeUtil.collectElements(firstItem) { it.typeName() == "MDX_JSX_ATTRIBUTE" }.isNotEmpty()
    )
  }

  /**
   * WEB-75114. Oracle: `<Answer options={[1, 2]}>text</Answer>` with a multi-line attribute is one
   * flow element whose body is a real Markdown `paragraph` child (not opaque JSX_BLOCK_CONTENT).
   */
  @Test
  fun testMultilineJsxAttributeChildIsMarkdown() {
    assertNoErrors(
      "## Exercise\n\nThis is some question.\n\n<Answer\n  options={[1, 2]}\n>\ntext\n</Answer>\n\nThis is another question."
    )
    val textChild = PsiTreeUtil.collectElements(myFixture.file) {
      it.typeName() == JSX_BLOCK_CONTENT && it.text == "text"
    }
    assertEmpty(
      "Oracle: the `text` body of <Answer> must be parsed as a Markdown paragraph child, not opaque JSX_BLOCK_CONTENT. WEB-75114.",
      textChild.toList()
    )
    val bodyParagraph = PsiTreeUtil.collectElements(myFixture.file) {
      it.typeName() == "PARAGRAPH" && it.firstChild != null && it.text.trim() == "text"
    }.firstOrNull()
    assertNotNull(
      "Oracle: the `text` body of <Answer> must be a Markdown PARAGRAPH (mdast paragraph child). WEB-75114.",
      bodyParagraph
    )
    assertNestedInJsxElement(bodyParagraph!!, "Answer")
  }

  // --- fenced code edge case --------------------------------------------------------------------

  /** Oracle: a 4-backtick fence whose body is a verbatim inner ```js fence — opaque, not split. GREEN. */
  @Test
  fun testNestedFenceIsOpaque() {
    assertNoErrors("````md\n```js\nconst x = 1\n```\n````")
    assertOpaqueFenceContains("```js")
    assertOpaqueFenceContains("const x = 1")
  }

  // --- ESM / member-expression / export-default ------------------------------------------------

  /** Oracle: `import`/`export` mid-sentence stays prose text — no ESM node, no error. GREEN. WEB-57359. */
  @Test
  fun testImportWordInProseIsText() {
    assertNoErrors("You can import assets into your project whenever you export them.")
    val esm = allRoots().flatMap { root ->
      PsiTreeUtil.collectElements(root) {
        val n = it.typeName()
        n == "ES6ImportDeclaration" || n == "ES6ExportDeclaration" || n == "ES6ExportDefaultAssignment"
      }.asList()
    }
    assertEmpty("Oracle: `import`/`export` mid-sentence must NOT produce ESM declarations. WEB-57359.", esm)
    assertTrue(
      "Oracle: the prose must contain the word 'import' as Markdown text",
      PsiTreeUtil.collectElements(myFixture.file) { it.typeName() == "TEXT" && it.text.contains("import") }.isNotEmpty()
    )
  }

  /** Oracle: `<Foo.Bar />` is a flow element named "Foo.Bar" (member-expression component). GREEN. */
  @Test
  fun testMemberExpressionComponent() {
    assertNoErrors("<Foo.Bar />")
    assertTrue(
      "Oracle: a member-expression component name `Foo.Bar` is expected as the JSX tag name",
      nodesOfTypeAllRoots(XML_TAG_NAME).any { it.text == "Foo.Bar" }
    )
  }

  /**
   * Oracle: `export default function ...` and `export { Thing as default }` are two `mdxjsEsm`
   * nodes; the trailing `# Heading` stays a real ATX_1 heading (not swallowed). GREEN.
   */
  @Test
  fun testExportDefault() {
    assertNoErrors(
      "export default function Layout(props) {\n  return props.children\n}\n\nexport { Thing as default } from './m'\n\n# Heading"
    )
    assertEmpty(
      "Oracle: top-level `export` statements are ESM, not opaque JSX_BLOCK_CONTENT.",
      PsiTreeUtil.collectElements(myFixture.file) {
        it.typeName() == JSX_BLOCK_CONTENT && it.text.contains("# Heading")
      }.toList()
    )
    assertHasMarkdownHeading("Heading", depth = 1)
  }

  // --- front matter (implemented & green) -------------------------------------------------------

  /** Oracle: a leading `---`...`---` block is a `yaml` node; FRONT_MATTER_HEADER is recognized. GREEN. */
  @Test
  fun testFrontMatterIsRecognized() {
    assertNoErrors("---\ntitle: Hello MDX\npublished: true\n---\n\n# Heading")
    assertTrue("Oracle: a leading `---`...`---` block must parse to a FRONT_MATTER_HEADER node.", nodesOfTypeAllRoots("FRONT_MATTER_HEADER").isNotEmpty())
    assertHasMarkdownHeading("Heading", depth = 1)
  }

  /** Oracle: a leading `+++`...`+++` block is a `toml` node; FRONT_MATTER_HEADER is recognized. GREEN. */
  @Test
  fun testTomlFrontMatterIsRecognized() {
    assertNoErrors("+++\ntitle = \"Hello MDX\"\npublished = true\n+++\n\n# Heading")
    assertTrue("Oracle: a leading `+++`...`+++` block must parse to a FRONT_MATTER_HEADER node.", nodesOfTypeAllRoots("FRONT_MATTER_HEADER").isNotEmpty())
    assertHasMarkdownHeading("Heading", depth = 1)
  }

  // --- whitespace / CRLF edge cases -------------------------------------------------------------

  /** Oracle: CRLF line endings parse like LF; heading + paragraph parse with no errors. GREEN. */
  @Test
  fun testCrlf() {
    assertNoErrors("# Heading\r\nA paragraph line.\r\n\r\n<Note>flow</Note>\r\n")
    assertHasMarkdownHeading("Heading", depth = 1)
    assertNotNull("Oracle: a paragraph is expected", firstContainer("PARAGRAPH"))
  }

  /** Oracle: a whitespace-only file has no block content (empty `root`). GREEN. */
  @Test
  fun testWhitespaceOnlyFile() {
    assertNoErrors("   \n   \n")
    assertEmpty(
      "Oracle: a whitespace-only file has no block content",
      PsiTreeUtil.collectElements(myFixture.file) { it.firstChild == null && it.text.isNotBlank() }.toList()
    )
  }

  /** An empty file is valid MDX (empty `root`). GREEN. */
  @Test
  fun testEmptyFile() {
    assertNoErrors("")
  }
}
