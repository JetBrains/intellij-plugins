package org.intellij.plugin.mdx

import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceUtil
import com.intellij.psi.util.parents
import com.intellij.testFramework.TestDataPath
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownLinkDestination
import org.intellij.plugins.markdown.model.psi.headers.HeaderAnchorLinkDestinationReference
import org.intellij.plugins.markdown.model.psi.headers.HeaderSymbol
import org.junit.jupiter.api.Test

@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/markdownReferences")
class MdxMarkdownReferencesTest : MdxTestBase() {
  @Test
  fun testSameFileHeaderAnchorResolvesInMdx() {
    myFixture.configureByFile("$testName.mdx")

    assertResolvedHeader("local-header")
    assertEquals("Expected one shared Markdown header-anchor reference", 1, headerAnchorReferencesAtCaret().size)
  }

  @Test
  fun testCrossFileMdxHeaderAnchorResolvesInMdx() {
    copyTargetToProject("target.mdx")
    myFixture.configureByFile("$testName.mdx")

    assertResolvedHeader("target-header")
  }

  @Test
  fun testExplicitRelativeMdxFileLinkResolvesInMdx() {
    val target = copyTargetToProject("target.mdx")
    myFixture.configureByFile("$testName.mdx")

    assertReferenceToFileAtCaret(target)
  }

  @Test
  fun testLineFragmentResolvesForNonMarkdownTargetInMdx() {
    copyTargetToProject("Target.cs")
    myFixture.configureByFile("$testName.mdx")

    val resolved = referencesAtCaret()
      .firstNotNullOfOrNull { it.resolve() as? PsiNamedElement }
      ?.takeIf { it.name == "Target.cs:L3" }
    assertNotNull("Expected line fragment to resolve to Target.cs:L3", resolved)
  }

  @Test
  fun testMarkdownLinkDestinationResolvesInMdxJsxBody() {
    val target = copyTargetToProject("target.mdx")
    myFixture.configureByFile("$testName.mdx")

    assertReferenceToFileAtCaret(target)
  }

  @Test
  fun testBacktickPathReferenceStillResolvesInMdxCodeSpan() {
    val target = copyTargetToProject("target.mdx")
    myFixture.configureByFile("$testName.mdx")

    assertReferenceToFileAtCaret(target)
  }

  @Test
  fun testMarkdownLinkDestinationsAreNotCreatedInMdxNonMarkdownContexts() {
    val cases = mapOf(
      "JSX opening tag" to "<Al<caret>ert>[Target](./target.mdx)</Alert>",
      "ESM block" to "import target from './tar<caret>get.mdx'\n\nText",
      "MDX expression" to "Some {[Target](./tar<caret>get.mdx)} text",
      "JSX attribute" to "<Alert href=\"./tar<caret>get.mdx\" />",
      "inline JSX expression attribute" to "Text <Alert value={[Target](./tar<caret>get.mdx)} /> end",
      "code span" to "`[Target](./tar<caret>get.mdx)`",
      "front matter" to "---\ntarget: ./tar<caret>get.mdx\n---\n\nText",
    )

    for ((name, text) in cases) {
      myFixture.configureByText("source.mdx", text)
      assertTrue("Expected no Markdown link destination in $name", linkDestinationAtCaretOrNull() == null)
    }
  }

  /** Copies `<testName>_target.<ext>` into the project at [projectPath]. */
  private fun copyTargetToProject(projectPath: String): PsiFile {
    val extension = projectPath.substringAfterLast('.')
    val virtualFile = myFixture.copyFileToProject("${testName}_target.$extension", projectPath)
    return myFixture.psiManager.findFile(virtualFile) ?: error("No PSI file for $virtualFile")
  }

  private fun assertResolvedHeader(expectedAnchor: String) {
    val resolved = headerAnchorReferencesAtCaret()
      .flatMap { it.resolveReference().filterIsInstance<HeaderSymbol>() }
    assertTrue(
      "Expected header anchor '$expectedAnchor' to resolve, actual anchors: ${resolved.map { it.anchorText }}",
      resolved.any { it.anchorText == expectedAnchor }
    )
  }

  private fun assertReferenceToFileAtCaret(expected: PsiFile) {
    val references = referencesAtCaret()
    assertTrue(
      "Expected references at caret to resolve to ${expected.name}, actual: ${references.map { it.resolve()?.text }}",
      references.any { it.isReferenceTo(expected) }
    )
  }

  private fun headerAnchorReferencesAtCaret(): List<HeaderAnchorLinkDestinationReference> {
    val linkDestination = linkDestinationAtCaretOrNull()
      ?: error("Expected caret to be inside a Markdown link destination")
    return service<PsiSymbolReferenceService>().getReferences(linkDestination)
      .filterIsInstance<HeaderAnchorLinkDestinationReference>()
  }

  private fun linkDestinationAtCaretOrNull(): MarkdownLinkDestination? {
    val file = myFixture.file
    val offset = myFixture.editor.caretModel.offset.coerceAtMost(file.textLength - 1)
    val element = file.findElementAt(offset) ?: return null
    return element.parents(withSelf = true).filterIsInstance<MarkdownLinkDestination>().firstOrNull()
  }

  private fun referencesAtCaret(): List<PsiReference> {
    val reference = myFixture.file.findReferenceAt(myFixture.editor.caretModel.offset)
    return reference?.let { PsiReferenceUtil.unwrapMultiReference(it).toList() } ?: emptyList()
  }
}
