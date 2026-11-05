package org.jetbrains.qodana.js.psi

import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.lang.javascript.psi.JSIfStatement
import com.intellij.psi.PsiElement
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
    "invokeRouteModule:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
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
    "handler:JS:TYPESCRIPT_FUNCTION",
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
    "makeHandler:JS:TYPESCRIPT_FUNCTION",
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
    "handler:JS:TYPESCRIPT_FUNCTION",
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
    "handler:JS:TYPESCRIPT_FUNCTION",
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
    "handle:JS:TYPESCRIPT_FUNCTION",
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
    "handler:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
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
    "onRequest:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** The inner `it` owns the callback, so its title names the scope. The outer `describe` is never reached. */
  fun testDescribeAndItReportInnerTitle() = assertScope(
    """
    describe('suite', () => {
      it('case', () => {
        if (a !== b) {
          report()
        }
      })
    })
    """,
    "case:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
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
    "inner:JS:TYPESCRIPT_FUNCTION",
  )

  /**
   * A title beats the enclosing named function, because the walk stops at the first scope that yields a name.
   * The callback belongs to the test, not to the helper that registers it.
   */
  fun testTitledCallbackInsideNamedFunctionReportsTitle() = assertScope(
    """
    function setupSuite() {
      it("works", () => {
        if (a !== b) {
          report()
        }
      })
    }
    """,
    "works:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
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

  // --- A bundled file: the platform gives no name, so the nearest binding supplies a contextual label. ---

  /**
   * The reported bundler shape. The problem sits in the returned `function(e)`.
   *
   * That function finds no binding of its own, so the walk moves out to the arrow, whose call result goes to
   * `Nu`. The label belongs to an outer scope, and that is intended.
   */
  fun testBundledIifeChainReportsBinding() = assertScope(
    """
    var Ds = tt(() => {
      Nu = (() => {
        var A = import.meta.url
        return function(e) {
          if (a !== b) {
            report(e)
          }
        }
      })()
    })
    """,
    "Nu:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** A problem in the outermost callback reaches the variable that holds the call result. */
  fun testCallArgumentBoundToVariableReportsVariable() = assertScope(
    """
    var Ds = tt(() => {
      if (a !== b) {
        report()
      }
    })
    """,
    "Ds:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /**
   * The same call without a binding reports nothing. Pins that the callee `tt` is never used as a name.
   */
  fun testUnassignedCallReportsNoScope() = assertNoScope(
    """
    tt(() => {
      if (a !== b) {
        report()
      }
    })
    """
  )

  /** Every nested callback finds no binding, so they all collapse onto the one binding above them. */
  fun testNestedCallbacksUnderOneBindingReportBinding() = assertScope(
    """
    Nu = (() => {
      return load().then(() => {
        return save().finally(() => {
          if (a !== b) {
            report()
          }
        })
      })
    })()
    """,
    "Nu:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /**
   * The binding walk stops at the next function-like ancestor, so it never crosses `namedOne` to reach
   * `outer`. An unbounded walk would answer `outer`. This is the only case that separates the two.
   */
  fun testBindingWalkStopsAtEnclosingFunction() = assertScope(
    """
    var outer = (function namedOne() {
      return () => {
        if (a !== b) {
          report()
        }
      }
    })()
    """,
    "namedOne:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** An assignment to a property. The platform already handles this, so the case guards the existing path. */
  fun testPropertyAssignmentReportsPropertyName() = assertScope(
    """
    t.ready = function () {
      if (a !== b) {
        report()
      }
    }
    """,
    "ready:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  // --- A test callback takes the title of the test or the suite that owns it. ---

  /** The reported case, verbatim. The problem has no `if`, so it anchors on the `expect` call. */
  fun testTestCallbackReportsTitle() = assertScopeAt<JSExpressionStatement>(
    """
    it("should error if the resource isn't a valid image", async () => {
      expect(await res.text()).toBe("The requested resource isn't a valid image.")
    })
    """,
    text = """expect(await res.text()).toBe("The requested resource isn't a valid image.")""",
    expected = "should error if the resource isn't a valid image:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /**
   * A problem in a `describe` body, outside any `it`. The suite title names it.
   * Pins that we read `allNames.lastOrNull()`, because `getTestName()` is null for a suite.
   */
  fun testSuiteBodyReportsSuiteTitle() = assertScope(
    """
    describe('suite', () => {
      if (a !== b) {
        report()
      }
    })
    """,
    "suite:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** A modifier form. Pins that the IDE builder handles the variants, so we never keep a callee list. */
  fun testModifierFormReportsTitle() = assertScope(
    """
    test.only("only this one", () => {
      if (a !== b) {
        report()
      }
    })
    """,
    "only this one:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** The lookup is by text range, so a callback nested inside a test also reports that test. */
  fun testNestedCallbackInsideTestReportsTitle() = assertScope(
    """
    it('case', () => {
      return load().then(() => {
        if (a !== b) {
          report()
        }
      })
    })
    """,
    "case:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** A real name still beats a title, because the walk asks for the declared name first. */
  fun testNamedHelperInsideTestReportsHelper() = assertScope(
    """
    it('case', () => {
      function helper() {
        if (a !== b) {
          report()
        }
      }
      return helper
    })
    """,
    "helper:JS:TYPESCRIPT_FUNCTION",
  )

  /**
   * `beforeEach` carries no title, and the structure model does not cover it.
   * The walk therefore falls through to the enclosing function. This documents the gap.
   */
  fun testTitlelessHookReportsEnclosingFunction() = assertScope(
    """
    function setupSuite() {
      beforeEach(() => {
        if (a !== b) {
          report()
        }
      })
    }
    """,
    "setupSuite:JS:TYPESCRIPT_FUNCTION",
  )

  /**
   * An interpolated title is not a quoted literal, so the rule rejects it. Its text changes whenever the
   * interpolation is edited, which would make the fingerprint churn.
   */
  fun testInterpolatedTitleIsRejected() = assertScope(
    $$"""
    function setupSuite() {
      it(`renders ${name}`, () => {
        if (a !== b) {
          report()
        }
      })
    }
    """,
    "setupSuite:JS:TYPESCRIPT_FUNCTION",
  )

  /** Asserts the whole scope signal as `name:type`, so every case also pins the type. */
  private fun assertScope(code: String, expected: String) {
    val signals = signalsOfFirstIf(code)

    assertEquals(expected, "${signals.enclosingScopeName}:${signals.enclosingScopeType}")
    assertNotNull(signals.astShape)
  }

  private fun assertNoScope(code: String) {
    val signals = signalsOfFirstIf(code)

    assertNull("scope name was '${signals.enclosingScopeName}'", signals.enclosingScopeName)
    assertNull("the type must not outlive the name", signals.enclosingScopeType)
    assertNotNull("the AST shape is still produced", signals.astShape)
  }

  /** Asserts the scope at the [T] whose text equals [text], for a snippet that holds no `if`. */
  private inline fun <reified T : PsiElement> assertScopeAt(code: String, text: String, expected: String) {
    val file = myFixture.configureByText("sample.ts", code.trimIndent())
    val element = PsiTreeUtil.findChildrenOfType(file, T::class.java).firstOrNull { it.text == text }
    assertNotNull("no ${T::class.simpleName} with text '$text'", element)

    val signals = PsiSignalExtractor.extractSignals(element!!)
    assertEquals(expected, "${signals.enclosingScopeName}:${signals.enclosingScopeType}")
  }

  private fun signalsOfFirstIf(code: String): StructuralFingerprintSignals {
    val file = myFixture.configureByText("sample.ts", code.trimIndent())
    val ifStatement = PsiTreeUtil.findChildOfType(file, JSIfStatement::class.java)!!
    return PsiSignalExtractor.extractSignals(ifStatement)
  }
}
