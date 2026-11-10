package org.jetbrains.qodana.js.psi

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.lang.javascript.psi.JSIfStatement
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
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

  /**
   * The inner `it` owns the callback, and the name is the whole path. A bare `case` repeats across files,
   * and the extraction-tolerant hash mixes in no file name.
   */
  fun testDescribeAndItReportTheFullPath() = assertScope(
    """
    describe('suite', () => {
      it('case', () => {
        if (a !== b) {
          report()
        }
      })
    })
    """,
    "suite > case:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
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
   * An interpolated title keeps its source text. The interpolation is not evaluated, so one call site always
   * gives one name, and the title still separates the sibling cases of the suite.
   */
  fun testInterpolatedTitleReportsSourceText() = assertScope(
    $$"""
    function setupSuite() {
      it(`renders ${name}`, () => {
        if (a !== b) {
          report()
        }
      })
    }
    """,
    $$"renders ${name}:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /**
   * The reported case: a parameterised title inside a shared `runTests` function. The title must win, because
   * `runTests` holds every case of the suite and cannot separate them.
   */
  fun testParameterisedTitleBeatsSharedTestFunction() = assertScopeAt<JSReferenceExpression>(
    $$"""
    function runTests(ctx) {
      it(`should handle maximumDiskCacheSize ${maximumDiskCacheSize}`, async () => {
        const opts = { headers: { accept: 'image/webp' } }

        const res = await fetchViaHTTP(
          ctx.appPort,
          '/_next/image',
          opts
        )
      })
    }
    """,
    text = "ctx.appPort",
    expected = $$"should handle maximumDiskCacheSize ${maximumDiskCacheSize}:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /**
   * The reported file shape: a suite calls one shared `runTests` that holds many cases. Each case must report
   * its own title. Before this rule every case of the suite reported `runTests`, so they shared one scope.
   *
   * The structure builder walks a function declaration where it stands, so the cases register under the
   * suite even though the call to `runTests` comes later.
   */
  fun testEachCaseOfASharedTestFunctionReportsItsOwnTitle() = assertScopesOfAllIfs(
    $$"""
    describe('image optimizer', () => {
      function runTests(ctx) {
        it(`should handle maximumDiskCacheSize ${maximumDiskCacheSize}`, async () => {
          const res = await fetchViaHTTP(ctx.appPort, '/_next/image')
          if (res.status !== 200) {
            report()
          }
        })

        it('should maintain the ETag header', async () => {
          const res = await fetchViaHTTP(ctx.appPort, '/_next/image')
          if (res.headers.get('etag') !== etag) {
            report()
          }
        })
      }

      runTests(ctx)
    })
    """,
    $$"image optimizer > should handle maximumDiskCacheSize ${maximumDiskCacheSize}:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
    "image optimizer > should maintain the ETag header:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /**
   * The same shape with `runTests` declared outside the suite, which is the more common form. The cases
   * register with no parent suite, so `allNames` holds the title alone. The answer does not change.
   */
  fun testSharedTestFunctionOutsideTheSuiteReportsEachTitle() = assertScopesOfAllIfs(
    $$"""
    function runTests(ctx) {
      it(`should handle maximumDiskCacheSize ${maximumDiskCacheSize}`, async () => {
        const res = await fetchViaHTTP(ctx.appPort, '/_next/image')
        if (res.status !== 200) {
          report()
        }
      })

      it('should set the Cache-Control header', async () => {
        const res = await fetchViaHTTP(ctx.appPort, '/_next/image')
        if (res.headers.get('cache-control') !== expected) {
          report()
        }
      })
    }

    describe('image optimizer', () => {
      runTests(ctx)
    })
    """,
    $$"should handle maximumDiskCacheSize ${maximumDiskCacheSize}:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
    "should set the Cache-Control header:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /**
   * A computed title gives no title at all. `name` is the name of a variable, not a title, and every case of
   * a `for` loop over the titles would collapse onto it. The walk falls through to the enclosing function.
   */
  fun testComputedTitleReportsEnclosingFunction() = assertScope(
    """
    function setupSuite() {
      it(name, () => {
        if (a !== b) {
          report()
        }
      })
    }
    """,
    "setupSuite:JS:TYPESCRIPT_FUNCTION",
  )

  // --- A class-like declaration is a scope too, so a type or an interface body gets a name. ---

  /** The reported case: a problem on a property of a TypeScript type alias. */
  fun testTypeAliasReportsItsName() = assertScopeAt<TypeScriptPropertySignature>(
    """
    type RunTestsCtx = {
      next: NextInstance
      w: number
      nextConfigImages?: Partial<import('next').NextConfig['images']>
    }
    """,
    text = "next: NextInstance",
    expected = "RunTestsCtx:JS:TYPESCRIPT_TYPE_ALIAS",
  )

  /** An interface body behaves the same way. */
  fun testInterfaceReportsItsName() = assertScopeAt<TypeScriptPropertySignature>(
    """
    interface RunTestsCtx {
      next: NextInstance
    }
    """,
    text = "next: NextInstance",
    expected = "RunTestsCtx:JS:TYPESCRIPT_INTERFACE",
  )

  /** A method still wins over the class that holds it, because it is the nearer scope. */
  fun testMethodWinsOverEnclosingClass() = assertScope(
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

  /** A function that a `const` holds keeps its own name inside a test. The platform names it, not our rule. */
  fun testNamedConstInsideTestReportsConst() = assertScope(
    """
    it('x', () => {
      const handler = () => {
        if (a !== b) { report() }
      }
      return handler
    })
    """,
    "handler:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /**
   * A callback with no binding inside a test. The title rule rejects it, because it is not an argument of the
   * test call. The walk then leaves it and reaches the callback of the test, which carries the title.
   */
  fun testUnboundCallbackInsideTestReportsTitle() = assertScope(
    """
    it('x', () => {
      arr.map(a => {
        if (a !== b) { report() }
      })
    })
    """,
    "x:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /**
   * A binding nearer than the test wins. The title of a test describes its own callback, not every function
   * that the callback holds. Before the argument check, this case reported `x`.
   */
  fun testCloserBindingInsideTestBeatsTitle() = assertScope(
    """
    it('x', () => {
      var inner = tt(() => {
        if (a !== b) { report() }
      })
      return inner
    })
    """,
    "inner:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /**
   * The binding walk stops at the class, so the callback takes no name from outside it. The anonymous class
   * is the next scope, it has no name either, and its own binding walk answers `outer`.
   */
  fun testCallbackInClassExtendsReportsOuterBinding() = assertScope(
    """
    var outer = class extends tt(() => {
      if (a !== b) { report() }
    }) {
    }
    """,
    "outer:JS:TYPESCRIPT_CLASS_EXPRESSION",
  )

  /** The walk stops at the class, so the enclosing class names the scope, never the variable outside it. */
  fun testCallbackInClassMemberReportsClass() = assertScope(
    """
    var outer = {
      inner: class Holder {
        static [tt(() => { if (a !== b) { report() } })] = 1
      }
    }
    """,
    "Holder:JS:TYPESCRIPT_CLASS_EXPRESSION",
  )

  /**
   * Two files with the same test title. Both report that title, so `enclosingScopeName` alone cannot tell
   * them apart. A fingerprint that must separate them has to mix in the file name.
   */
  fun testSameTitleInTwoFilesReportsTheSameName() {
    val body = """
      it('case', () => {
        if (a !== b) {
          report()
        }
      })
    """.trimIndent()

    val first = myFixture.addFileToProject("first.test.ts", body)
    val second = myFixture.addFileToProject("second.test.ts", body)

    val scopes = listOf(first, second).map { file ->
      val ifStatement = PsiTreeUtil.findChildOfType(file, JSIfStatement::class.java)!!
      val signals = PsiSignalExtractor.extractSignals(ifStatement)
      "${signals.enclosingScopeName}:${signals.enclosingScopeType}"
    }

    assertEquals(listOf("case:JS:TYPESCRIPT_FUNCTION_EXPRESSION", "case:JS:TYPESCRIPT_FUNCTION_EXPRESSION"), scopes)
  }

  /**
   * The same title under two different suites. The path separates them, so a generic title such as
   * `renders correctly` no longer collapses two files onto one scope name.
   */
  fun testSameTitleUnderTwoSuitesReportsDifferentNames() {
    fun body(suite: String) = """
      describe('$suite', () => {
        it('renders correctly', () => {
          if (a !== b) {
            report()
          }
        })
      })
    """.trimIndent()

    val first = myFixture.addFileToProject("suite-a.test.ts", body("header"))
    val second = myFixture.addFileToProject("suite-b.test.ts", body("footer"))

    val scopes = listOf(first, second).map { file ->
      val ifStatement = PsiTreeUtil.findChildOfType(file, JSIfStatement::class.java)!!
      PsiSignalExtractor.extractSignals(ifStatement).enclosingScopeName
    }

    assertEquals(listOf("header > renders correctly", "footer > renders correctly"), scopes)
  }

  // --- A CommonJS export reports `exports` like any other binding. A collision across files is not handled. ---

  /** The CommonJS default export, as an arrow. */
  fun testModuleExportsArrowReportsExports() = assertScope(
    """
    module.exports = () => {
      if (a !== b) {
        report()
      }
    }
    """,
    "exports:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** The same as a function expression. Here the platform supplies the name, not the binding walk. */
  fun testModuleExportsFunctionReportsExports() = assertScope(
    """
    module.exports = function () {
      if (a !== b) {
        report()
      }
    }
    """,
    "exports:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** A bare `exports` assignment behaves the same way. */
  fun testBareExportsReportsExports() = assertScope(
    """
    exports = () => {
      if (a !== b) {
        report()
      }
    }
    """,
    "exports:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** A callback inside the export call. The binding walk reaches the export assignment. */
  fun testCallbackUnderModuleExportsReportsExports() = assertScope(
    """
    module.exports = tt(() => {
      if (a !== b) {
        report()
      }
    })
    """,
    "exports:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** A chained binding. The walk stops at the nearest binding, which is the export assignment. */
  fun testChainedBindingThroughExportsReportsExports() = assertScope(
    """
    const x = module.exports = () => {
      if (a !== b) {
        report()
      }
    }
    """,
    "exports:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** A named export reports its own name, not `exports`. */
  fun testNamedExportReportsItsName() = assertScope(
    """
    exports.handler = () => {
      if (a !== b) {
        report()
      }
    }
    """,
    "handler:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** A field on `this` keeps its name. A common field name still collides, and that is not handled. */
  fun testThisFieldReportsItsName() = assertScope(
    """
    function Ctor() {
      this.x = () => {
        if (a !== b) {
          report()
        }
      }
    }
    """,
    "x:JS:TYPESCRIPT_FUNCTION_EXPRESSION",
  )

  /** Asserts the whole scope signal as `name:type`, so every case also pins the type. */
  private fun assertScope(code: String, expected: String) {
    val signals = signalsOfFirstIf(code)

    assertEquals(expected, "${signals.enclosingScopeName}:${signals.enclosingScopeType}")
    assertNotNull(signals.astShape)
  }

  // region the lambda shape contract, kept in sync with every other language

  /** An arrow function bounds the shape walk. The shape describes the call, never the binding around it. */
  fun testShapeInsideBoundLambdaStopsAtTheLambda() =
    assertAstShapeAt<JSCallExpression>("bound.ts", "function bar() { const f = () => weigh(1) }", "weigh(1)", "JS:CALL_EXPRESSION(JS:REFERENCE_EXPRESSION(JS:IDENTIFIER),JS:ARGUMENT_LIST)")

  /** The same arrow function as an argument gives the same shape. */
  fun testShapeInsideUnboundLambdaStopsAtTheLambda() =
    assertAstShapeAt<JSCallExpression>("argument.ts", "function bar() { run(() => weigh(1)) }", "weigh(1)", "JS:CALL_EXPRESSION(JS:REFERENCE_EXPRESSION(JS:IDENTIFIER),JS:ARGUMENT_LIST)")

  /** The invariant the two tests above pin by value. A drift here breaks the baseline of every language. */
  fun testShapeDoesNotDependOnTheLambdaBinding() {
    val bound = astShapeOf<JSCallExpression>("bound.ts", "function bar() { const f = () => weigh(1) }", "weigh(1)")

    assertNotNull(bound)
    assertEquals("the shape must not depend on the binding", bound,
                 astShapeOf<JSCallExpression>("argument.ts", "function bar() { run(() => weigh(1)) }", "weigh(1)"))
  }

  private inline fun <reified T : PsiElement> assertAstShapeAt(fileName: String, code: String, text: String, expected: String) {
    assertEquals(expected, astShapeOf<T>(fileName, code, text))
  }

  /** The AST shape at the [T] whose text equals [text]. Each snippet needs its own [fileName]. */
  private inline fun <reified T : PsiElement> astShapeOf(fileName: String, code: String, text: String): String? {
    val file = myFixture.configureByText(fileName, code)
    val element = PsiTreeUtil.findChildrenOfType(file, T::class.java).firstOrNull { it.text == text }
    assertNotNull("no ${T::class.simpleName} with text '$text'", element)

    return PsiSignalExtractor.extractSignals(element!!).astShape
  }

  // endregion

  private fun assertNoScope(code: String) {
    val signals = signalsOfFirstIf(code)

    assertNull("scope name was '${signals.enclosingScopeName}'", signals.enclosingScopeName)
    assertNull("the type must not outlive the name", signals.enclosingScopeType)
    assertNotNull("the AST shape is still produced", signals.astShape)
  }

  /** Asserts the scope of every `if` of the snippet, in source order. Pins that two cases differ. */
  private fun assertScopesOfAllIfs(code: String, vararg expected: String) {
    val file = myFixture.configureByText("sample.ts", code.trimIndent())
    val scopes = PsiTreeUtil.findChildrenOfType(file, JSIfStatement::class.java)
      .map { PsiSignalExtractor.extractSignals(it) }
      .map { "${it.enclosingScopeName}:${it.enclosingScopeType}" }

    assertEquals(expected.toList(), scopes)
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
