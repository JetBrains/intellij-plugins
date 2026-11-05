package org.jetbrains.qodana.js.psi

import com.intellij.lang.javascript.psi.JSIfStatement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiSignalExtractor

/**
 * Verifies [PsiSignalExtractor] over real TypeScript PSI.
 *
 * JS and TS code keeps most logic in an anonymous callback. An anonymous function must not hide the named
 * outer scope, because two structural fingerprints need `enclosingScopeName`.
 */
class JsPsiSignalExtractorTest : BasePlatformTestCase() {

  /** The reported case: the problem sits in the callback that `Promise.finally` receives. */
  fun testCallbackReportsNamedOuterFunction() = assertScope(
    """
    function handler(req: string) {
      const invokeRouteModule = async (span: string) => {
        const result = render(span)
        return result.finally(() => {
          if (attributes.get('next.span_type') !== BaseServerSpan.handleRequest) {
            report(span)
          }
        })
      }
      return invokeRouteModule(req)
    }
    """,
    "invokeRouteModule",
  )

  /** Two nested anonymous callbacks are both skipped. */
  fun testNestedCallbacksReportNamedOuterFunction() = assertScope(
    """
    function handler() {
      return load().then(() => {
        return save().finally(() => {
          if (a !== b) {
            report()
          }
        })
      })
    }
    """,
    "handler",
  )

  /** An anonymous function that a named function returns right away. */
  fun testReturnedAnonymousFunctionReportsNamedOuterFunction() = assertScope(
    """
    function makeHandler() {
      return () => {
        if (a !== b) {
          report()
        }
      }
    }
    """,
    "makeHandler",
  )

  /** An immediately invoked function inside a named function. */
  fun testInvokedAnonymousFunctionReportsNamedOuterFunction() = assertScope(
    """
    function handler() {
      (() => {
        if (a !== b) {
          report()
        }
      })()
    }
    """,
    "handler",
  )

  /** A named function declaration keeps reporting its own name. */
  fun testNamedFunctionReportsItself() = assertScope(
    """
    function handler() {
      if (a !== b) {
        report()
      }
    }
    """,
    "handler",
  )

  /** A class method keeps reporting its own name. */
  fun testClassMethodReportsItself() = assertScope(
    """
    class Router {
      handle() {
        if (a !== b) {
          report()
        }
      }
    }
    """,
    "handle",
  )

  /** JS PSI already names a function that a variable holds. The nearest scope is named, so nothing is skipped. */
  fun testArrowFunctionInVariableReportsVariableName() = assertScope(
    """
    const handler = () => {
      if (a !== b) {
        report()
      }
    }
    """,
    "handler",
  )

  /** JS PSI already names a function that an object property holds. */
  fun testArrowFunctionInPropertyReportsPropertyName() = assertScope(
    """
    const routes = {
      onRequest: () => {
        if (a !== b) {
          report()
        }
      }
    }
    """,
    "onRequest",
  )

  /**
   * No ancestor has a name, so no scope signal is produced. A test file of `describe` and `it` callbacks
   * reaches this path. The AST shape is still produced.
   */
  fun testFullyAnonymousChainReportsNoScope() = assertNoScope(
    """
    describe('suite', () => {
      it('case', () => {
        if (a !== b) {
          report()
        }
      })
    })
    """
  )

  /** Top-level code has no function ancestor at all, so the walk runs out at the file. */
  fun testTopLevelCodeReportsNoScope() = assertNoScope(
    """
    if (a !== b) {
      report()
    }
    """
  )

  /** The nearest *named* ancestor wins, not the outermost one. */
  fun testNamedFunctionInsideCallbackReportsInnerFunction() = assertScope(
    """
    function outer() {
      return items.map(() => {
        function inner() {
          if (a !== b) {
            report()
          }
        }
        return inner
      })
    }
    """,
    "inner",
  )

  /**
   * Pins today's answer for a titled callback inside a named function: the function wins and the `it` title
   * is never consulted. Deriving a scope name from call titles has to restate this expectation.
   */
  fun testTitledCallbackInsideNamedFunctionReportsFunction() = assertScope(
    """
    function setupSuite() {
      it("works", () => {
        if (a !== b) {
          report()
        }
      })
    }
    """,
    "setupSuite",
  )

  /**
   * A string-literal argument is not a scope. Pins the blast radius of deriving a name from a call title:
   * `describe` and `addEventListener` share this shape, so every `click` handler would collapse into one scope.
   */
  fun testStringLiteralArgumentIsNotAScope() = assertNoScope(
    """
    element.addEventListener("click", () => {
      if (a !== b) {
        report()
      }
    })
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
    val file = myFixture.configureByText("sample.ts", code.trimIndent())
    val ifStatement = PsiTreeUtil.findChildOfType(file, JSIfStatement::class.java)!!
    return PsiSignalExtractor.extractSignals(ifStatement)
  }
}
