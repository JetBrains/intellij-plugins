package org.intellij.plugin.mdx

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.testFramework.TestDataPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/highlight")
class MdxHighlightTest : MdxTestBase() {

    @BeforeEach fun enableHighlightTestInspections() = enableJsxInspections()

    // --- Semantic highlighting (inspection pipeline: myFixture.testHighlighting) --------------

    private fun doTestHighlighting() {
        myFixture.configureByFile("$testName.mdx")
        myFixture.testHighlighting()
    }


    @Test
    fun testJsxSimple() {
        doTestHighlighting()
    }

    @Test
    fun testUnresolvedVariable() {
        doTestHighlighting()
    }

    @Test
    fun testParsingError() {
        doTestHighlighting()
    }

    @Test
    fun testParsingErrorOneLine() {
        doTestHighlighting()
    }

    @Test
    fun testComment() {
        doTestHighlighting()
    }

    @Test
    fun testOpenTagInAttribute() {
        doTestHighlighting()
    }

    @Test
    fun testOpenCloseTagInAttribute() {
        doTestHighlighting()
    }

    @Test
    fun testOpenCloseTagIn2Attribute() {
        doTestHighlighting()
    }

    @Test
    fun testOpenCloseTagIn2AttributeSameLine() {
        doTestHighlighting()
    }

    // --- No-error floor: valid MDX must produce zero errors --------------------------------
    // These tests verify that syntactically valid MDX produces no PsiErrorElement and no
    // ERROR-severity highlights. The redesign (WEB-78468) must keep all of these green.

    private fun doAssertNoErrors() {
        myFixture.configureByFile("$testName.mdx")
        assertNoPsiErrors()
        assertNoErrorHighlights()
    }

    /** Plain Markdown prose is valid MDX. */
    @Test
    fun testPlainProse() {
        doAssertNoErrors()
    }

    /** A self-closing JSX flow element is valid MDX. */
    @Test
    fun testSelfClosingJsx() {
        doAssertNoErrors()
    }

    /** An ESM export and an inline `{expression}` that references it are valid MDX. */
    @Test
    fun testExportAndExpression() {
        doAssertNoErrors()
    }

    /** A JSX flow element with a string attribute and inline children is valid MDX. */
    @Test
    fun testJsxWithAttribute() {
        doAssertNoErrors()
    }

    /** A fenced code block with plain JavaScript is valid MDX (opaque code). */
    @Test
    fun testFencedJsCode() {
        doAssertNoErrors()
    }

    /** A GFM table is valid MDX (GFM is enabled in the MDX flavour). */
    @Test
    fun testGfmTable() {
        doAssertNoErrors()
    }

    // --- Editor-highlighter token/color layer (MdxEditorHighlighter + JS/JSX layer) --------
    // Queries myFixture.editor.highlighter directly, not the inspection pipeline.
    // Intentionally RED (WEB-78468): testFlowExpressionIsColoredAsJs, testInlineExpressionIsColoredAsJs —
    // the editor-highlighter JS layer doesn't yet cover inline/flow expressions in the redesigned PSI.

    /** Configures the file and returns (start, tokenText, attribute-key chain) per EDITOR token. */
    private fun editorHighlightingTokens(text: String): List<Triple<Int, String, List<String>>> {
        myFixture.configureByText("foo.mdx", text)
        val highlighter = myFixture.editor.highlighter
        // MdxEditorHighlighter is module-internal; assert by class name to stay independent of it.
        assertEquals(
            "Expected the registered MdxEditorHighlighter (layered JS/JSX coloring), got $highlighter",
            "MdxEditorHighlighter",
            highlighter.javaClass.simpleName
        )
        val tokens = ArrayList<Triple<Int, String, List<String>>>()
        val it: HighlighterIterator = highlighter.createIterator(0)
        while (!it.atEnd()) {
            val tokenText = myFixture.editor.document.charsSequence.subSequence(it.start, it.end).toString()
            val keys = it.textAttributesKeys.map { key -> serializeKey(key) }
            tokens.add(Triple(it.start, tokenText, keys))
            it.advance()
        }
        return tokens
    }

    private fun testDataText(name: String = testName): String {
        val virtualFile = myFixture.copyFileToProject("$name.mdx")
        val psiFile = myFixture.psiManager.findFile(virtualFile) ?: error("No PSI file for $virtualFile")
        return psiFile.text
    }

    /** Mirrors EditorTestUtil.serializeTextAttributeKey: external name + fallback chain. */
    private fun serializeKey(key: TextAttributesKey): String {
        val fallback = key.fallbackAttributeKey
        return if (fallback == null) key.externalName else "${key.externalName} => ${serializeKey(fallback)}"
    }

    /** The attribute-key chain of the first editor token that exactly equals [tokenText]. */
    private fun keysOf(tokens: List<Triple<Int, String, List<String>>>, tokenText: String): List<String> {
        val match = tokens.firstOrNull { it.second == tokenText }
        assertNotNull(
            "Editor highlighter produced no token equal to '$tokenText'. Tokens: " +
                tokens.joinToString { "'${it.second}'->${it.third}" },
            match
        )
        return match!!.third
    }

    /** The attribute-key chain of the editor token covering [offset] (start <= offset < end). */
    private fun keysAt(tokens: List<Triple<Int, String, List<String>>>, offset: Int): List<String> {
        val match = tokens.firstOrNull { offset >= it.first && offset < it.first + it.second.length }
        assertNotNull(
            "No editor token covers offset $offset. Tokens: " +
                tokens.joinToString { "[${it.first},${it.first + it.second.length})'${it.second}'" },
            match
        )
        return match!!.third
    }

    /**
     * Correct behaviour (mdxjs.com: JSX is JavaScript-with-JSX). The editor highlighter already
     * colors a JSX flow element's tag name and attribute name with XML/JSX keys (green floor).
     */
    @Test
    fun testJsxTagAndAttributeAreColoredAsJsx() {
        val tokens = editorHighlightingTokens(testDataText())
        assertTrue(
            "Oracle: JSX tag name `Button` must carry an XML/JSX tag-name color, got ${keysOf(tokens, "Button")}.",
            keysOf(tokens, "Button").any { it.contains("XML_TAG_NAME") }
        )
        assertTrue(
            "Oracle: JSX attribute name `kind` must carry an XML/JSX attribute color, got ${keysOf(tokens, "kind")}.",
            keysOf(tokens, "kind").any { it.contains("XML_ATTRIBUTE_NAME") }
        )
    }

    @Test
    fun testMismatchedMultilineTagNamesRemainColoredAsJsx() {
        val tokens = editorHighlightingTokens("<dix>\n    Hello\n</div>")
        assertTrue(
            "A temporarily renamed opening tag must keep JSX coloring: ${keysOf(tokens, "dix")}",
            keysOf(tokens, "dix").any { it.contains("XML_TAG_NAME") }
        )
        assertTrue(
            "The mismatched closing tag must keep JSX coloring: ${keysOf(tokens, "div")}",
            keysOf(tokens, "div").any { it.contains("XML_TAG_NAME") }
        )
    }

    /**
     * Redesign target (WEB-78468): a block-level flow `{expression}` (`<div>{typeof window}</div>`)
     * must carry embedded-JS coloring in the editor highlighter — `typeof` a JS keyword, the braces
     * JS braces (mdxjs.com: `{...}` holds a JavaScript expression).
     *
     * Currently RED. WEB-78496 structured the flow element into discrete JSX nodes, but the editor-
     * highlighter JS/JSX layer (registered only for `JSX_BLOCK_CONTENT`/`HTML_TAG` in
     * `MdxEditorHighlighter`) no longer covers the inner `{expression}`, which now surfaces as a
     * single `MARKDOWN_TEXT` token. The PSI/JS-projection layer does treat the expression as JS, so
     * this gap is specific to the unported editor-highlighter coloring.
     */
    @Test
    fun testFlowExpressionIsColoredAsJs() {
        val tokens = editorHighlightingTokens(testDataText())
        assertTrue(
            "Oracle: `typeof` inside the {expression} must carry a JS keyword color, got ${keysOf(tokens, "typeof")}.",
            keysOf(tokens, "typeof").any { it.contains("JS.KEYWORD") }
        )
        assertTrue(
            "Oracle: the expression braces must carry a JS braces color, got ${keysOf(tokens, "{")}.",
            keysOf(tokens, "{").any { it.contains("JS.BRACES") }
        )
    }

    /**
     * Correct behaviour: JS inside a `code={`...`}` expression attribute is colored as JS. The editor
     * highlighter already colors the template-literal body as a JS string (green floor).
     */
    @Test
    fun testJsInExpressionAttributeIsColoredAsJs() {
        val tokens = editorHighlightingTokens(testDataText())
        assertTrue(
            "Oracle: the template literal in `code={`...`}` must carry a JS string color, " +
                "got ${keysOf(tokens, "const x = 1")}.",
            keysOf(tokens, "const x = 1").any { it.contains("JS.STRING") }
        )
    }

    @Test
    fun testEsmInListIsNotColoredAsJavaScript() {
        val text = testDataText()
        val tokens = editorHighlightingTokens(text)
        val topLevelKeys = keysAt(tokens, text.indexOf("import bbb from"))
        assertTrue(
            "Test setup: top-level ESM must carry a JS keyword color, got $topLevelKeys.",
            topLevelKeys.any { it.contains("JS.KEYWORD") }
        )

        val listKeys = keysAt(tokens, text.lastIndexOf("import bbb"))
        assertFalse(
            "List-item `import` must remain Markdown text, not a JavaScript keyword: $listKeys.",
            listKeys.any { it.contains("JS.KEYWORD") }
        )
    }

    /**
     * Currently fails: an INLINE MDX text expression in prose must be colored as embedded JS, the same
     * as an identical block-level flow expression (mdxjs.com: `{...}` is a JS expression). WEB-78468.
     */
    @Test
    fun testInlineExpressionIsColoredAsJs() {
        // Block-level flow expression: the `+` is known to be JS-colored (the JS-injection green floor).
        val blockText = testDataText("BlockLevelExpression")
        val block = editorHighlightingTokens(blockText)
        val blockPlusKeys = keysAt(block, blockText.indexOf('+'))
        assertTrue(
            "Test setup: the `+` in a block-level {expression} should carry a JS color, got $blockPlusKeys.",
            blockPlusKeys.any { it.contains("JS.") }
        )
        // Inline text expression in prose: must match the block-level JS coloring.
        val inlineText = testDataText("InlineTextExpression")
        val inline = editorHighlightingTokens(inlineText)
        assertEquals(
            "Oracle (WEB-78468): the `+` of an inline `{1 + 2}` expression must be JS-colored, the same " +
                "as in a block-level flow expression, but the editor highlighter colored it differently.",
            blockPlusKeys,
            keysAt(inline, inlineText.indexOf('+'))
        )
    }

    /**
     * A ```js fence nested in a JSX component (`<Tabs>`/`<TabItem>`) is colored the same as an
     * identical top-level ```js fence, because Markdown inside a JSX flow element is parsed as
     * Markdown (mdxjs.com). Implemented by WEB-78496 (deriving MDX blocks from Markdown and
     * recognizing Markdown in flow JSX children), satisfying WEB-59952.
     */
    @Test
    fun testFencedCodeColorInsideJsxMatchesTopLevel() {
        val topLevel = editorHighlightingTokens(testDataText("TopLevelFencedCode"))
        val insideJsx = editorHighlightingTokens(testDataText("FencedCodeInsideJsx"))
        assertEquals(
            "Oracle (WEB-59952): the ```js fence body inside <TabItem> must be colored the same as a " +
                "top-level ```js fence, but the editor highlighter assigned different attribute keys.",
            keysOf(topLevel, "const answer = 42"),
            keysOf(insideJsx, "const answer = 42")
        )
    }
}
