package org.jetbrains.qodana.jvm.kotlin.psi

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiSignalExtractor

/**
 * Verifies [PsiSignalExtractor] over real Kotlin PSI.
 *
 * Kotlin reaches an unnamed scope by two different routes, and both must fall through to the enclosing
 * named function: a lambda is not a [org.jetbrains.kotlin.psi.KtNamedFunction] at all, while an anonymous
 * `fun() {}` is one that has no name.
 */
class KotlinPsiSignalExtractorTest : BasePlatformTestCase() {

  /** A named function keeps reporting its own name. */
  fun testNamedFunctionReportsItself() = assertScope(
    """
    fun handler() {
      if (x != y) {
        report()
      }
    }
    """,
    "handler",
  )

  /** A class member function keeps reporting its own name. */
  fun testClassMethodReportsItself() = assertScope(
    """
    class Router {
      fun handle() {
        if (x != y) {
          report()
        }
      }
    }
    """,
    "handle",
  )

  /** A lambda is not a function declaration, so the walk passes through it to the named function. */
  fun testLambdaInsideNamedFunctionReportsFunction() = assertScope(
    """
    fun handler() {
      items.forEach {
        if (x != y) {
          report()
        }
      }
    }
    """,
    "handler",
  )

  /** An anonymous `fun() {}` is a declaration with no name, so it is skipped and the walk continues outward. */
  fun testAnonymousFunctionInsideNamedFunctionReportsFunction() = assertScope(
    """
    fun handler() {
      val inner = fun() {
        if (x != y) {
          report()
        }
      }
      inner()
    }
    """,
    "handler",
  )

  /**
   * A lambda assigned to a top-level `val`. Unlike JS, Kotlin PSI does not name the lambda after the
   * property that holds it, and no named ancestor is left, so no scope signal is produced.
   */
  fun testLambdaAssignedToTopLevelValReportsNoScope() = assertNoScope(
    """
    val a = {
      if (x != y) {
        report()
      }
    }
    """
  )

  /** The same for an anonymous `fun() {}` assigned to a top-level `val`. */
  fun testAnonymousFunctionAssignedToTopLevelValReportsNoScope() = assertNoScope(
    """
    val a = fun() {
      if (x != y) {
        report()
      }
    }
    """
  )

  private fun assertScope(code: String, expectedName: String) {
    val signals = signalsOfFirstIf(code)

    assertEquals("scope was '${signals.enclosingScopeName}:${signals.enclosingScopeType}'",
                 expectedName, signals.enclosingScopeName)
    assertNotNull("the type must accompany the name", signals.enclosingScopeType)
    assertNotNull(signals.astShape)
  }

  private fun assertNoScope(code: String) {
    val signals = signalsOfFirstIf(code)

    assertNull("scope name was '${signals.enclosingScopeName}'", signals.enclosingScopeName)
    assertNull("the type must not outlive the name", signals.enclosingScopeType)
    assertNotNull("the AST shape is still produced", signals.astShape)
  }

  private fun signalsOfFirstIf(code: String): StructuralFingerprintSignals {
    val file = myFixture.configureByText("sample.kt", code.trimIndent())
    val ifExpression = PsiTreeUtil.findChildOfType(file, KtIfExpression::class.java)!!
    return PsiSignalExtractor.extractSignals(ifExpression)
  }
}
