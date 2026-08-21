package org.intellij.plugin.mdx

import com.intellij.codeInsight.lookup.LookupElementPresentation
import org.junit.jupiter.api.Test

/**
 * All completion tests for MDX files, organized by the language layer they target.
 *
 * MDX files contain two language layers: Markdown (base) and MdxJS (template data).
 * - Keyword + module-path completions live in the Markdown (base) layer.
 * - JSX tag/attribute/expression completions live in the MdxJS layer.
 * - Compatibility tests verify MDX-only completions are absent in plain .md files and that
 *   native Markdown completions (fences, anchors, front-matter) still work in .mdx files.
 */
class MdxCompletionTest : MdxTestBase() {

  private fun completionStrings(
    content: String,
    fileName: String = "test.mdx",
    companionFile: Pair<String, String>? = null,
  ): List<String> {
    companionFile?.let { (name, fileContent) -> myFixture.configureByText(name, fileContent) }
    myFixture.configureByText(fileName, content)
    return myFixture.completeBasic()?.map { it.lookupString } ?: emptyList()
  }

  // --- Keyword and module-path completion (Markdown base layer) ----------------------------
  // 'import'/'export' keyword injection (MdxImportExportCompletionContributor),
  // module path strategy (.mdx keeps extension, .tsx omits it).

  @Test
  fun testImportKeywordAtParagraphStart() {
    assertCompletionContains("im<caret>", "import ")
  }

  @Test
  fun testExportKeywordAtParagraphStart() {
    assertCompletionContains("ex<caret>", "export ")
  }

  @Test
  fun testKeywordsAreBold() {
    myFixture.configureByText("test.mdx", "im<caret>")
    val elements = myFixture.completeBasic()
    assertNotNull("Expected completion lookup to stay open (multiple items available)", elements)
    val importItem = elements!!.firstOrNull { it.lookupString == "import " }
    assertNotNull("Expected 'import ' item in completion", importItem)
    val pres = LookupElementPresentation()
    importItem!!.renderElement(pres)
    assertTrue("'import ' keyword should render as bold", pres.isItemTextBold)
  }

  @Test
  fun testKeywordsAbsentMidParagraph() {
    // 'import'/'export' are valid MDX only at line start — not mid-sentence
    assertCompletionNotContains("Hello im<caret>", "export ")
  }

  @Test
  fun testKeywordsAbsentInsideJsx() {
    // Caret is inside JSX_BLOCK_CONTENT (template data lang), not a Markdown TEXT token
    assertCompletionNotContains("<div>im<caret></div>", "export ")
  }

  @Test
  fun testKeywordsAtSecondParagraph() {
    assertCompletionContains("# Heading\n\nim<caret>", "import ")
  }

  // --- Group B: module path completion (MdxImportModulePathStrategy) ---

  @Test
  fun testMdxFileAppearsInModulePathCompletion() {
    val strings = completionStrings("import X from './<caret>'",
                                    companionFile = "Helper.mdx" to "export const helper = 1")
    assertTrue("Expected 'Helper.mdx' in module path completion", strings.any { it.contains("Helper.mdx") })
  }

  @Test
  fun testTsxFileAppearsInModulePathCompletion() {
    val strings = completionStrings("import X from './<caret>'",
                                    companionFile = "Button.tsx" to "export default function Button() { return null }")
    assertTrue("Expected 'Button' or 'Button.tsx' in module path completion", strings.any { it.contains("Button") })
  }

  @Test
  fun testMdxExtensionForcedInPath() {
    // MdxImportModulePathStrategy.getPathSettings() returns FORCE_EXTENSION for .mdx
    val strings = completionStrings("import X from './<caret>'",
                                    companionFile = "Helper.mdx" to "export const helper = 1")
    assertTrue(
      "Module path completion for a .mdx file should show 'Helper.mdx' with the extension",
      strings.any { it == "Helper.mdx" }
    )
  }

  @Test
  fun testTsxExtensionOmittedInPath() {
    // Standard JS behaviour: .tsx extension is stripped from import paths
    val strings = completionStrings("import X from './<caret>'",
                                    companionFile = "Button.tsx" to "export default function Button() { return null }")
    assertEmpty(
      "Module path completion for a .tsx file should NOT include the .tsx extension",
      strings.filter { it == "Button.tsx" }
    )
    assertTrue(
      "Module path completion for a .tsx file should appear as 'Button' (no extension)",
      strings.any { it == "Button" }
    )
  }

  // --- JSX tag, attribute, and expression completion (MdxJS layer) -------------------------
  // Tag names from imports, HTML tags, attributes, expression values, named imports.

  @Test
  fun testBlockJsxTagNameCompletionFromImport() {
    // Block-level JSX: <TagName/> at the start of a line
    val strings = completionStrings(
      "import {MyComponent} from 'MyComponent.mdx'\n\n<My<caret>/>",
      companionFile = "MyComponent.mdx" to "export const MyComponent = () => <div/>"
    )
    assertTrue("Expected 'MyComponent' in block-level JSX tag name completion", strings.contains("MyComponent"))
  }

  @Test
  fun testInlineJsxTagNameCompletionFromImport() {
    // Inline JSX: tag inside a prose paragraph — "Some text <Badge/>"
    val strings = completionStrings(
      "import {Badge} from 'Badge.mdx'\n\nSome text <Bad<caret>/>",
      companionFile = "Badge.mdx" to "export const Badge = () => <span/>"
    )
    assertTrue("Expected 'Badge' in inline (prose) JSX tag name completion", strings.contains("Badge"))
  }

  @Test
  fun testSelfClosingJsxTagCompletion() {
    val strings = completionStrings(
      "import {Icon} from 'Icon.mdx'\n\n<Ico<caret> />",
      companionFile = "Icon.mdx" to "export const Icon = () => <svg/>"
    )
    assertTrue("Expected 'Icon' in self-closing JSX tag name completion", strings.contains("Icon"))
  }

  @Test
  fun testNestedJsxTagNameCompletion() {
    val strings = completionStrings(
      "import {Inner} from 'Inner.mdx'\n\n<div><Inn<caret>/></div>",
      companionFile = "Inner.mdx" to "export const Inner = () => <div/>"
    )
    assertTrue("Expected 'Inner' in nested JSX tag name completion", strings.contains("Inner"))
  }

  @Test
  fun testFragmentChildJsxTagCompletion() {
    val strings = completionStrings(
      "import {Item} from 'Item.mdx'\n\n<><Ite<caret>/></>",
      companionFile = "Item.mdx" to "export const Item = () => <li/>"
    )
    assertTrue("Expected 'Item' in fragment child JSX tag name completion", strings.contains("Item"))
  }

  @Test
  fun testMemberExpressionComponentCompletion() {
    // Member expression: <Foo.Bar/> — complete after the dot
    val strings = completionStrings(
      "import * as Foo from 'FooComponents.ts'\n\n<Foo.B<caret>/>",
      companionFile = "FooComponents.ts" to "export const Bar = () => null; export const Baz = () => null"
    )
    assertTrue("Expected 'Bar' or 'Baz' in member-expression JSX tag completion",
               strings.any { it == "Bar" || it == "Baz" })
  }

  @Test
  fun testHtmlTagNameCompletion() {
    assertCompletionContains("<d<caret>/>", "div")
  }

  @Test
  fun testVoidHtmlTagCompletionInsertsSelfClosing() {
    // Typing `<inp` and choosing `input` from the popup must insert a self-closing `<input/>`
    // (a void HTML element), not `<input>` left open and not `<input></input>`.
    myFixture.configureByText("test.mdx", "<inp<caret>")
    selectCompletionItem("input")
    myFixture.checkResult("<input/><caret>")
  }

  @Test
  fun testJsxInListItemTagCompletion() {
    // MDX allows JSX inside list items: "- <Icon/>"
    val strings = completionStrings(
      "import {Icon} from 'Icon.mdx'\n\n- <Ico<caret>/>",
      companionFile = "Icon.mdx" to "export const Icon = () => <svg/>"
    )
    assertTrue("Expected 'Icon' in JSX-in-list-item tag name completion", strings.contains("Icon"))
  }

  @Test
  fun testJsxInBlockquoteTagCompletion() {
    // MDX allows JSX inside blockquotes: "> <Note/>"
    val strings = completionStrings(
      "import {Note} from 'Note.mdx'\n\n> <Not<caret>/>",
      companionFile = "Note.mdx" to "export const Note = () => <blockquote/>"
    )
    assertTrue("Expected 'Note' in JSX-in-blockquote tag name completion", strings.contains("Note"))
  }

  @Test
  fun testHtmlAttributeNameCompletion() {
    val strings = completionStrings("<div cla<caret>/>")
    assertTrue("Expected 'className' or 'class' in HTML attribute name completion",
               strings.contains("className") || strings.contains("class"))
  }

  @Test
  fun testHtmlAttributeCompletionWithCodeFenceInBody() {
    // JSX attribute completions on the opening tag must work even when the element body
    // contains an indented code fence — fence scanning must not break the JSX injection range.
    val strings = completionStrings("<div on<caret>>\n    ```tsx\n    const x = 1\n    ```\n</div>")
    assertTrue("Expected event-handler attributes (e.g. 'onClick') when div body has a code fence",
               strings.contains("onClick") || strings.any { it.startsWith("on") })
  }

  @Test
  fun testAttributeCompletionInUnclosedTag() {
    // An unclosed tag (`<div ` with no `>` or `/>`) must still project into the MdxJS layer
    // and produce attribute completions — same behaviour as in a .tsx file.
    val strings = completionStrings("<div <caret>")
    assertTrue(
      "Expected HTML/JSX attribute completions inside an unclosed <div> tag (e.g. 'className', 'onClick')",
      strings.contains("className") || strings.contains("onClick") || strings.any { it.startsWith("on") }
    )
  }

  @Test
  fun testCustomComponentAttributeCompletionInUnclosedTag() {
    // TypeScript-typed props must be suggested even when the component tag is not yet closed.
    val strings = completionStrings(
      "import {Button} from 'Button.tsx'\n\n<Button <caret>",
      companionFile = "Button.tsx" to
        "interface ButtonProps { variant: string; disabled: boolean } " +
        "export function Button(props: ButtonProps) { return null }"
    )
    assertTrue(
      "Expected 'variant' prop completion inside an unclosed <Button> tag",
      strings.contains("variant")
    )
  }

  @Test
  fun testJsxAttributeExpressionValueCompletion() {
    assertCompletionContains("export const myColor = 'red'\n\n<div style={{color: myCo<caret>}}/>", "myColor")
  }

  @Test
  fun testJsxAttributeNameOnCustomComponent() {
    // TypeScript-typed custom component props should appear as attribute names
    val strings = completionStrings(
      "import {Alert} from 'Alert.tsx'\n\n<Alert sev<caret>/>",
      companionFile = "Alert.tsx" to
        "interface AlertProps { severity: string; message: string } " +
        "export function Alert(props: AlertProps) { return null }"
    )
    assertTrue("Expected 'severity' in custom component attribute name completion",
               strings.contains("severity"))
  }

  @Test
  fun testJsxExpressionChildCompletion() {
    assertCompletionContains("export const greeting = 'hello'\n\n<div>{greet<caret>}</div>", "greeting")
  }

  @Test
  fun testStandaloneBlockExpressionCompletion() {
    // {expr} on its own line (MDX flow expression) — exported var should be accessible
    assertCompletionContains("export const answer = 42\n\n{ans<caret>}", "answer")
  }

  @Test
  fun testExpressionInsideJsxAttributeArray() {
    assertCompletionContains("export const tabA = 'A'\n\n<div data-items={[tabA, tab<caret>]}/>", "tabA")
  }

  @Test
  fun testNamedImportDestructuringCompletionFromMdx() {
    val strings = completionStrings(
      "import { Foo<caret> } from 'Foo.mdx'",
      companionFile = "Foo.mdx" to "export const FooA = () => <div/>\nexport const FooB = () => <div/>"
    )
    assertTrue("Expected named exports from Foo.mdx in import destructuring completion",
               strings.any { it.contains("FooA") || it.contains("FooB") })
  }

  @Test
  fun testNamedImportDestructuringCompletionFromTs() {
    val strings = completionStrings(
      "import { format<caret> } from 'utils.ts'",
      companionFile = "utils.ts" to "export function formatDate() {} export function parseDate() {}"
    )
    assertTrue("Expected 'formatDate' in import destructuring completion from .ts file",
               strings.contains("formatDate"))
  }

  // --- MDX ↔ Markdown feature isolation --------------------------------------------------------
  // MDX-only completions absent in .md; Markdown completions (fences, anchors, front-matter
  // schema, image tags) present in .mdx. Paired tests: MDX variant + .md baseline.

  @Test
  fun testImportKeywordAbsentInMarkdownFile() {
    // The MdxImportExportCompletionContributor is registered for language="Markdown",
    // so it runs in .md files. The `is MdxFile` guard must prevent it from adding items.
    assertEmpty("'import ' must not appear in plain .md file completions — MDX contributor guard check",
                completionStrings("im<caret>", fileName = "test.md").filter { it == "import " })
  }

  @Test
  fun testExportKeywordAbsentInMarkdownFile() {
    assertEmpty("'export ' must not appear in plain .md file completions — MDX contributor guard check",
                completionStrings("ex<caret>", fileName = "test.md").filter { it == "export " })
  }

  @Test
  fun testImportKeywordAbsentInMarkdownFileAtSecondParagraph() {
    // Verify the guard holds even in the "paragraph start" position that triggers the contributor
    assertEmpty("'import ' must not appear at second-paragraph start in .md",
                completionStrings("# Heading\n\nim<caret>", fileName = "test.md").filter { it == "import " })
  }

  @Test
  fun testCodeFenceLanguageCompletionInMdxFile() {
    // Code fence language names are suggested by the Markdown plugin's
    // CodeFenceLanguageListCompletionProvider; this must work in .mdx files too.
    // 'javascript' (not 'java'): Java plugin is not on the MDX test classpath.
    assertCompletionContains("```ja<caret>\n```", "javascript")
  }

  @Test
  fun testCodeFenceLanguageCompletionBetweenAdjacentDelimiters() {
    // The shape typing three backticks leaves behind, with auto-close having added the closer. Six adjacent
    // backticks are a collapsed code span, not a fence, so MarkdownFenceLangCompletionContributor.beforeCompletion
    // has to swap in its own dummy identifier for FENCE_LANG to match -- and it used to do that only for
    // MarkdownFile, leaving .mdx with an empty lookup here while the uncollapsed fence above worked.
    assertCompletionContains("```<caret>```", "javascript")
  }

  @Test
  fun testCodeFenceLanguageCompletionInPlainMarkdown() {
    // Baseline: code fence language completion must work in plain .md
    myFixture.configureByText("test.md", "```ja<caret>\n```")
    val elements = myFixture.completeBasic()
    val strings = elements?.map { it.lookupString } ?: emptyList()
    assertTrue("Expected 'javascript' in code fence language completion inside .md file (baseline)",
               strings.contains("javascript"))
  }

  @Test
  fun testHeaderAnchorCompletionInMdxFile() {
    val strings = completionStrings("## My Section\n\n[link](#my-s<caret>)")
    assertTrue("Expected header anchor '#my-section' in .mdx anchor reference completion",
               strings.any { it.contains("my-section") })
  }

  @Test
  fun testHeaderAnchorCompletionInPlainMarkdown() {
    // Baseline: anchor completion in .md
    myFixture.configureByText("test.md", "## My Section\n\n[link](#my-s<caret>)")
    val elements = myFixture.completeBasic()
    val strings = elements?.map { it.lookupString } ?: emptyList()
    assertTrue("Expected header anchor '#my-section' in .md anchor reference completion (baseline)",
               strings.any { it.contains("my-section") })
  }

  @Test
  fun testYamlFrontMatterSchemaCompletionInMdxFile() {
    assertCompletionContains("---\ntitl<caret>: value\n---", "title")
  }

  @Test
  fun testYamlFrontMatterSchemaCompletionInPlainMarkdown() {
    // Baseline: front matter schema completion in .md
    myFixture.configureByText("test.md", "---\ntitl<caret>: value\n---")
    val elements = myFixture.completeBasic()
    val strings = elements?.map { it.lookupString } ?: emptyList()
    assertTrue("Expected 'title' in front matter schema completion inside .md file (baseline)",
               strings.contains("title"))
  }

  @Test
  fun testImageTagCompletionInMdxFile() {
    // The Markdown image tag contributor (<img>) should still work in .mdx
    val strings = completionStrings("Some prose. <<caret>")
    assertTrue("Expected 'img' HTML image tag completion to be available in .mdx",
               strings.any { it.startsWith("img") })
  }

  @Test
  fun testTagNameCompletionAtUnbalancedPrefix() {
    // Assert tag-name completion is non-empty for a freshly-typed, still-unbalanced `<My` prefix:
    // such a prefix must be projected into the MdxJS layer for the platform JSX completion to run.
    val strings = completionStrings(
      "import {MyComponent} from 'MyComponent.mdx'\n\n<My<caret>",
      companionFile = "MyComponent.mdx" to "export const MyComponent = () => <div/>"
    )
    assertTrue("Expected 'MyComponent' in tag-name completion at unbalanced '<My' prefix, got: $strings",
               strings.contains("MyComponent"))
  }

  @Test
  fun testTagNameCompletionAtBareCaretBeforeLaterJsxElement() {
    // A bare `<` must still get tag-name completion even when an unrelated JSX element follows later
    // in the file after a blank line.
    val strings = completionStrings(
      "import {MyComponent} from 'MyComponent.mdx'\n\n<<caret>\n\n<a>\n\n</a>",
      companionFile = "MyComponent.mdx" to "export const MyComponent = () => <div/>"
    )
    assertTrue("Expected 'MyComponent' in tag-name completion at a bare '<' before a later JSX element, got: $strings",
               strings.contains("MyComponent"))
  }

  //todo: it doesn't work in runtime because of TypeScriptConfigAccessibilityChecker.checkImpl and work in test because it's LightVirtualFile
  @Test
  fun testCodeFenceCompletionInMdxFile() {
    val testCase = """
    <div>
      ```typescript
      function myFunction() {

      }
      <caret>
      ```
    </div>
    """.trimIndent()
    val strings = completionStrings(testCase)
    assertTrue(strings.any { it.contains("myFunction") })
  }
}
