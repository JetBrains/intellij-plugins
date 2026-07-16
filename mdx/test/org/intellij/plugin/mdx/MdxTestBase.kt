@file:Suppress("DEPRECATION") // @RunInEdt is deprecated but remains the established EDT wrapper for migrated BasePlatformTestCase-style tests

package org.intellij.plugin.mdx

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownBooleanAttributeInspection
import com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection
import com.intellij.lang.javascript.inspections.JSXUnresolvedComponentInspection
import com.intellij.lang.javascript.inspections.TypeScriptCheckImportInspection
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import com.intellij.testFramework.junit5.fixture.testNameFixture
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.PlatformTestUtil
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection
import com.sixrr.inspectjs.validity.ThisExpressionReferencesGlobalObjectJSInspection
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach

@TestApplication
@RunInEdt(allMethods = true, writeIntent = true)
abstract class MdxTestBase {
  private val tempDirFixture = tempPathFixture()
  private val projectFixtureRef = projectFixture(tempDirFixture, openAfterCreation = true)

  @Suppress("unused") // codeInsightFixture requires the project to have at least one module
  private val moduleFixtureRef = projectFixtureRef.moduleFixture(tempDirFixture, addPathToSourceRoot = true)

  protected val myFixture: CodeInsightTestFixture by codeInsightFixture(projectFixtureRef, tempDirFixture)

  /** Equivalent of the old `getTestName(false)`: the test method name without the `test` prefix. */
  protected val testName: String by testNameFixture(lowerCaseFirstLetter = false)

  @TestDisposable
  protected lateinit var testRootDisposable: Disposable

  protected val testDataPath: String get() = myFixture.testDataPath

  @BeforeEach
  fun enableMdxInspections() {
    myFixture.enableInspections(
      HtmlUnknownTagInspection(),
      HtmlUnknownAttributeInspection(),
      BadExpressionStatementJSInspection(),
      ThisExpressionReferencesGlobalObjectJSInspection(),
      HtmlUnknownBooleanAttributeInspection()
    )
  }

  /**
   * Keep the completion lookup open even when a single item matches.
   *
   * `myFixture.completeBasic()` returns the lookup list only while the lookup stays open; when exactly
   * one item matches the prefix the platform auto-inserts it and `completeBasic()` returns `null`. The
   * completion tests assert on the returned list (e.g. a single `import ` keyword, a single `javascript`
   * fence language), so auto-insertion must be disabled — the same setup the platform's other
   * completion test bases use. Restored automatically via [testRootDisposable].
   */
  @BeforeEach
  fun disableCompletionAutoInsert() {
    val settings = CodeInsightSettings.getInstance()
    val previous = settings.AUTOCOMPLETE_ON_CODE_COMPLETION
    settings.AUTOCOMPLETE_ON_CODE_COMPLETION = false
    Disposer.register(testRootDisposable) { settings.AUTOCOMPLETE_ON_CODE_COMPLETION = previous }
  }

  /** Full TS/JSX inspection set for tests that exercise JSX resolve and type-checking. */
  protected fun enableJsxInspections() {
    myFixture.enableInspections(
      TypeScriptCheckImportInspection(),
      TypeScriptValidateTypesInspection(),
      JSXUnresolvedComponentInspection(),
      TypeScriptUnresolvedReferenceInspection(),
      JSUnresolvedReferenceInspection()
    )
  }

  // --- no-error assertions -------------------------------------------------------------------

  protected fun collectPsiErrorElements(): List<PsiErrorElement> =
    myFixture.file.viewProvider.allFiles
      .flatMap { PsiTreeUtil.collectElementsOfType(it, PsiErrorElement::class.java) }

  protected fun assertNoPsiErrors() {
    val errors = collectPsiErrorElements()
    assertEmpty(
      "Expected no PsiErrorElement, but found:\n" +
        errors.joinToString("\n") { "  ${it.errorDescription} @ ${it.textRange}" },
      errors
    )
  }

  protected fun assertNoErrorHighlights() {
    val highlights = myFixture.doHighlighting().filter { it.severity == HighlightSeverity.ERROR }
    assertEmpty(
      "Expected no ERROR-severity highlights, but found:\n" +
        highlights.joinToString("\n") { "  ${it.description} @ [${it.startOffset}, ${it.endOffset})" },
      highlights
    )
  }

  protected fun assertNoErrors(text: String) {
    myFixture.configureByText("foo.mdx", text)
    assertNoPsiErrors()
    assertNoErrorHighlights()
  }

  // Alias kept for oracle-test readability (tests that explicitly care about both PSI and highlight levels).
  protected fun assertNoErrorsAndNoErrorHighlights(text: String) = assertNoErrors(text)

  // --- PSI structure helpers — type names matched by last ':'-segment of elementType.toString() ---
  // Module-internal PSI element types aren't directly referenceable, so structural identity is
  // matched by this normalized leaf name (verified against *.MDX.txt / *.MdxJS.txt snapshots).

  protected fun PsiElement.typeName(): String = node.elementType.toString().substringAfterLast(':')

  protected fun allRoots(): List<PsiFile> = myFixture.file.viewProvider.allFiles

  protected fun nodesOfType(root: PsiFile, name: String): List<PsiElement> =
    PsiTreeUtil.collectElements(root) { it.typeName() == name }.toList()

  protected fun nodesOfTypeAllRoots(name: String): List<PsiElement> =
    allRoots().flatMap { nodesOfType(it, name) }

  protected fun collectFenceContents(): List<PsiElement> = nodesOfType(myFixture.file, "CODE_FENCE_CONTENT")

  /**
   * Oracle: a fenced code block parses to a `code` node whose `value` is the verbatim code.
   * Fence content is line-split into multiple CODE_FENCE_CONTENT nodes; asserts the joined text
   * contains [code] verbatim — i.e. the code is opaque and NOT split into JSX tags/expressions.
   */
  protected fun assertOpaqueFenceContains(code: String) {
    val joined = collectFenceContents().joinToString("\n") { it.text }
    assertTrue(
      "Oracle: a `code` node with value containing '$code' is expected (opaque fenced code), " +
        "but the joined CODE_FENCE_CONTENT did not hold it. Joined fence content:\n$joined",
      joined.contains(code)
    )
  }

  /**
   * Asserts that no JSX tag named one of [forbiddenTagNames] exists in any view-provider root.
   * Proves opaque code such as `<stdio.h>` / `<Baz>` / `Promise<Type>` is NOT parsed as a JSX tag.
   */
  protected fun assertNoJsxTagNamed(vararg forbiddenTagNames: String) {
    if (forbiddenTagNames.isEmpty()) return
    val forbidden = forbiddenTagNames.toSet()
    val tagNames = allRoots().flatMap { root ->
      PsiTreeUtil.collectElements(root) {
        it.typeName() == XML_TAG_NAME && it.text in forbidden
      }.asList()
    }
    assertEmpty(
      "Oracle: opaque code must NOT be parsed as a JSX tag, but found XmlToken:XML_TAG_NAME " +
        "node(s) named $forbidden:\n" +
        tagNames.joinToString("\n") { "  '${it.text}' @ ${it.textRange}" },
      tagNames
    )
  }

  /**
   * Oracle: the given text is a `heading{depth}` node. Asserts the base MDX PSI contains a
   * real Markdown ATX heading of [depth] (element-type name == "ATX_[depth]") whose text includes
   * [content]. Returns the matched heading element.
   */
  protected fun assertHasMarkdownHeading(content: String, depth: Int): PsiElement {
    val expected = "ATX_$depth"
    val headings = PsiTreeUtil.collectElements(myFixture.file) {
      it.typeName() == expected && it.text.contains(content)
    }
    val allAtx = PsiTreeUtil.collectElements(myFixture.file) {
      it.typeName().startsWith("ATX_") && it.firstChild != null
    }
    assertTrue(
      "Oracle: a Markdown heading{depth:$depth} (element-type '$expected') containing " +
        "'$content' is expected, but none was parsed. All ATX nodes found: " +
        allAtx.map { "${it.node.elementType}:'${it.text}'" },
      headings.isNotEmpty()
    )
    return headings.first()
  }

  /** First base-tree composite node of the given normalized type name (must have children). */
  protected fun firstContainer(name: String): PsiElement? =
    PsiTreeUtil.collectElements(myFixture.file) { it.typeName() == name && it.firstChild != null }
      .firstOrNull()

  /**
   * Asserts [node] lies within the `<`[tag]`>...</`[tag]`>` source span — i.e. it is interleaved
   * INSIDE the JSX element, not relocated to the file root after a prematurely-terminated JSX block.
   */
  protected fun assertNestedInJsxElement(node: PsiElement, tag: String) {
    val source = myFixture.file.text
    val open = source.indexOf("<$tag")
    val close = source.indexOf("</$tag>")
    assertTrue("Test setup: '<$tag' / '</$tag>' not found in source", open in 0 until close)
    assertTrue(
      "Oracle: '${node.text}' (${node.typeName()}) must be nested inside <$tag>...</$tag> " +
        "(source offsets [$open, ${close + tag.length + 3}]), but it was at ${node.textRange} " +
        "— it appears outside the JSX element (WEB-78468).",
      node.textRange.startOffset in open..(close + tag.length + 3) &&
        node.textRange.endOffset <= close + tag.length + 3
    )
  }

  protected fun assertTrue(condition: Boolean): Unit = Assertions.assertTrue(condition)

  protected fun assertTrue(message: String, condition: Boolean): Unit = Assertions.assertTrue(condition, message)

  protected fun assertEquals(expected: Any?, actual: Any?): Unit = Assertions.assertEquals(expected, actual)

  protected fun assertEquals(message: String, expected: Any?, actual: Any?): Unit = Assertions.assertEquals(expected, actual, message)

  protected fun assertNotNull(actual: Any?): Unit = Assertions.assertNotNull(actual)

  protected fun assertNotNull(message: String, actual: Any?): Unit = Assertions.assertNotNull(actual, message)

  protected fun <T> assertEmpty(message: String, collection: Collection<T>): Unit = Assertions.assertTrue(collection.isEmpty(), message)

  protected fun assertFalse(condition: Boolean): Unit = Assertions.assertFalse(condition)

  protected fun assertFalse(message: String, condition: Boolean): Unit = Assertions.assertFalse(condition, message)

  protected fun assertCompletionContains(text: String, vararg expected: String) {
    myFixture.configureByText("test.mdx", text)
    val items = myFixture.completeBasic()
    val strings = items?.map { it.lookupString } ?: emptyList()
    for (item in expected) {
      assertTrue("Expected '$item' in completion, got: $strings", strings.contains(item))
    }
  }

  protected fun assertCompletionNotContains(text: String, vararg unexpected: String) {
    myFixture.configureByText("test.mdx", text)
    val items = myFixture.completeBasic()
    val strings = items?.map { it.lookupString } ?: emptyList()
    for (item in unexpected) {
      assertFalse("Did not expect '$item' in completion, got: $strings", strings.contains(item))
    }
  }

  protected fun selectCompletionItem(lookupString: String): LookupElement {
    val elements = myFixture.completeBasic()
    val item = elements?.firstOrNull { it.lookupString == lookupString }
               ?: error("Expected a '$lookupString' lookup item, got: ${elements?.map { it.lookupString }}")
    val lookup = myFixture.lookup ?: error("No active lookup after completeBasic")
    lookup.currentItem = item
    myFixture.type('\n')
    return item
  }

  protected fun doEmmetTest(before: String = testName, after: String = "${testName}_after") {
    myFixture.configureByFile("$before.mdx")
    TemplateManagerImpl.setTemplateTesting(testRootDisposable)
    WriteCommandAction.runWriteCommandAction(myFixture.project) {
      TemplateManager.getInstance(myFixture.project).startTemplate(myFixture.editor, TemplateSettings.TAB_CHAR)
    }
    NonBlockingReadActionImpl.waitForAsyncTaskCompletion()
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    myFixture.checkResultByFile("$after.mdx")
  }

  protected companion object {
    // Element-type NAME constants used in PSI structure assertions.
    // Match the last ':'-segment of elementType.toString() (module-internal types aren't referenceable).
    const val JSX_BLOCK_CONTENT = "JSX_BLOCK_CONTENT"
    // A JSX tag name leaf in the template-data (MdxJS) root is an `XmlToken:XML_TAG_NAME`.
    const val XML_TAG_NAME = "XML_TAG_NAME"
  }
}