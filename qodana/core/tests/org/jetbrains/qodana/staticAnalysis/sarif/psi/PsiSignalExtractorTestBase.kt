package org.jetbrains.qodana.staticAnalysis.sarif.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiSignalExtractor

/**
 * Shared assertions for the per-language [PsiSignalExtractor] tests.
 *
 * A subclass gives the fixture file name and the default anchor. The scope reads as `name:type`.
 */
abstract class PsiSignalExtractorTestBase : BasePlatformTestCase() {

  /** The name the snippets get. Its extension picks the language. */
  protected abstract val fileName: String

  /** The element a snippet anchors on by default, usually the `if` of the language. */
  protected abstract val anchorType: Class<out PsiElement>

  /** The scope signal at the default anchor, plus a shape. */
  protected fun assertScope(code: String, expected: String) {
    val signals = signalsOfAnchor(code)

    assertEquals(expected, signals.scope())
    assertNotNull(signals.astShape)
  }

  protected fun assertNoScope(code: String) {
    val signals = signalsOfAnchor(code)

    assertNull("scope name was '${signals.enclosingScopeName}'", signals.enclosingScopeName)
    assertNull("the type must not outlive the name", signals.enclosingScopeType)
    assertNotNull("the AST shape is still produced", signals.astShape)
  }

  /** The scope of every anchor, in source order. Pins that two cases differ. */
  protected fun assertScopesOfAllAnchors(code: String, vararg expected: String) {
    val scopes = PsiTreeUtil.findChildrenOfType(configure(code), anchorType)
      .map { PsiSignalExtractor.extractSignals(it).scope() }

    assertEquals(expected.toList(), scopes)
  }

  /** The scope at the [T] whose text equals [text], for a snippet with no anchor. */
  protected inline fun <reified T : PsiElement> assertScopeAt(code: String, text: String, expected: String) {
    assertEquals(expected, signalsAt(code, text, T::class.java).scope())
  }

  protected inline fun <reified T : PsiElement> assertNoScopeAt(code: String, text: String) {
    val signals = signalsAt(code, text, T::class.java)

    assertNull("scope name was '${signals.enclosingScopeName}'", signals.enclosingScopeName)
    assertNull("the type must not outlive the name", signals.enclosingScopeType)
  }

  /** Asserts the exact AST shape at the [T] whose text equals [text]. */
  protected inline fun <reified T : PsiElement> assertAstShapeIs(code: String, text: String, expected: String) {
    assertEquals(expected, astShapeAt<T>(code, text))
  }

  /**
   * Asserts the AST shape at the [T] whose text equals [text] does not reach [outerType].
   *
   * The exact shape text is brittle. The boundary is what the block-container checks decide.
   */
  protected inline fun <reified T : PsiElement> assertAstShapeStopsBefore(code: String, text: String, outerType: String) {
    val astShape = astShapeAt<T>(code, text)

    assertNotNull(astShape)
    assertFalse("the shape reached '$outerType': '$astShape'", astShape!!.startsWith(outerType))
  }

  /**
   * The shape at [text] must be the same whether a name holds the lambda or not.
   *
   * Every language pins this. A lambda decides the scope name, and it must never decide the shape.
   */
  protected inline fun <reified T : PsiElement> assertShapeIgnoresTheBinding(
    boundCode: String,
    argumentCode: String,
    text: String,
  ) {
    val bound = astShapeAt<T>(boundCode, text)

    assertNotNull(bound)
    assertEquals("the shape must not depend on the binding", bound, astShapeAt<T>(argumentCode, text))
  }

  protected inline fun <reified T : PsiElement> astShapeAt(code: String, text: String): String? =
    signalsAt(code, text, T::class.java).astShape

  /** The signals at the single element of [type] whose text equals [text]. */
  protected fun <T : PsiElement> signalsAt(code: String, text: String, type: Class<T>): StructuralFingerprintSignals {
    val element = PsiTreeUtil.findChildrenOfType(configure(code), type).firstOrNull { it.text == text }
    assertNotNull("no ${type.simpleName} with text '$text'", element)

    return PsiSignalExtractor.extractSignals(element!!)
  }

  /** The signals at the first default anchor of [code]. */
  protected fun signalsOfAnchor(code: String): StructuralFingerprintSignals =
    PsiSignalExtractor.extractSignals(PsiTreeUtil.findChildOfType(configure(code), anchorType)!!)

  /** The scope signal as `name:type`. */
  protected fun StructuralFingerprintSignals.scope(): String = "$enclosingScopeName:$enclosingScopeType"

  private fun configure(code: String) = myFixture.configureByText(fileName, code.trimIndent())
}
