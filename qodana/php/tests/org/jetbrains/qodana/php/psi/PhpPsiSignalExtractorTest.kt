package org.jetbrains.qodana.php.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.elements.ConstantReference
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.If
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiSignalExtractor
import org.jetbrains.qodana.staticAnalysis.sarif.psi.PsiSignalExtractorTestBase

/** `$` in a raw string, because Kotlin reads it as the start of a template. */
private const val DOLLAR = "$"

/**
 * Verifies [PsiSignalExtractor] over real PHP PSI.
 *
 * PHP names a closure and an anonymous class with the empty string, never null, so the extractor must pass
 * such a name and look for a label. A Pest test gives its title, an assignment the name of its target.
 */
class PhpPsiSignalExtractorTest : PsiSignalExtractorTestBase() {

  // region a named scope

  fun testFunctionReportsItsName() = assertScope(
    """
    <?php
    function handler(${DOLLAR}a, ${DOLLAR}b) {
      if (${DOLLAR}a !== ${DOLLAR}b) {
        report();
      }
    }
    """,
    "handler:FUNCTION",
  )

  fun testMethodReportsItsName() = assertScope(
    """
    <?php
    class Holder {
      public function handle(${DOLLAR}a, ${DOLLAR}b) {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      }
    }
    """,
    "handle:CLASS_METHOD",
  )

  fun testTraitMethodReportsItsName() = assertScope(
    """
    <?php
    trait Holder {
      public function handle(${DOLLAR}a, ${DOLLAR}b) {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      }
    }
    """,
    "handle:CLASS_METHOD",
  )

  fun testEnumMethodReportsItsName() = assertScope(
    """
    <?php
    enum Suit {
      case Hearts;
      public function handle(${DOLLAR}a, ${DOLLAR}b) {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      }
    }
    """,
    "handle:CLASS_METHOD",
  )

  /** The nearest scope wins, so an inner function hides the outer one. */
  fun testNestedFunctionReportsTheInnerFunction() = assertScope(
    """
    <?php
    function outer() {
      function inner(${DOLLAR}a, ${DOLLAR}b) {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      }
      inner(1, 2);
    }
    """,
    "inner:FUNCTION",
  )

  /** A problem outside any function reports the class, so a class-like declaration is a scope too. */
  fun testClassConstantReportsTheClass() = assertScopeAt<ConstantReference>(
    """
    <?php
    class Holder {
      const LIMIT = BASE + 1;
    }
    """,
    "BASE",
    "Holder:CLASS",
  )

  fun testInterfaceConstantReportsTheInterface() = assertScopeAt<ConstantReference>(
    """
    <?php
    interface Holder {
      const LIMIT = BASE + 1;
    }
    """,
    "BASE",
    "Holder:CLASS",
  )

  /** A problem reported on the function itself, not inside it. The walk starts at the element. */
  fun testProblemOnTheFunctionReportsThatFunction() = assertScopeAt<Function>(
    """
    <?php
    function handler() {
      report();
    }
    """,
    "function handler() {\n  report();\n}",
    "handler:FUNCTION",
  )

  fun testTopLevelProblemReportsNoScope() = assertNoScope(
    """
    <?php
    if (${DOLLAR}a !== ${DOLLAR}b) {
      report();
    }
    """,
  )

  // endregion

  // region the binding of a closure

  /** The reported gap: a closure that an assignment holds must report the target. */
  fun testClosureBoundToVariableReportsVariable() = assertScope(
    """
    <?php
    ${DOLLAR}handler = function (${DOLLAR}a, ${DOLLAR}b) {
      if (${DOLLAR}a !== ${DOLLAR}b) {
        report();
      }
    };
    """,
    "handler:FUNCTION",
  )

  fun testArrowFunctionBoundToVariableReportsVariable() = assertScopeAt<FunctionReference>(
    """
    <?php
    ${DOLLAR}pick = fn(${DOLLAR}a) => report(${DOLLAR}a);
    """,
    "report(${DOLLAR}a)",
    "pick:FUNCTION",
  )

  /** A field on `this` keeps its name. A common field name still collides. */
  fun testClosureBoundToFieldReportsField() = assertScope(
    """
    <?php
    class Holder {
      public function __construct() {
        ${DOLLAR}this->handler = function (${DOLLAR}a, ${DOLLAR}b) {
          if (${DOLLAR}a !== ${DOLLAR}b) {
            report();
          }
        };
      }
    }
    """,
    "handler:FUNCTION",
  )

  /** A closure that a call receives has no binding, so the enclosing function names it. */
  fun testClosureArgumentReportsEnclosingFunction() = assertScope(
    """
    <?php
    function handler(${DOLLAR}items) {
      return array_map(function (${DOLLAR}a) {
        if (${DOLLAR}a !== null) {
          report();
        }
      }, ${DOLLAR}items);
    }
    """,
    "handler:FUNCTION",
  )

  fun testClosureArgumentInMethodReportsEnclosingMethod() = assertScope(
    """
    <?php
    class Holder {
      public function handle(${DOLLAR}items) {
        return array_map(function (${DOLLAR}a) {
          if (${DOLLAR}a !== null) {
            report();
          }
        }, ${DOLLAR}items);
      }
    }
    """,
    "handle:CLASS_METHOD",
  )

  /** A closure with no binding and no enclosing scope reports nothing. */
  fun testTopLevelClosureArgumentReportsNoScope() = assertNoScope(
    """
    <?php
    array_map(function (${DOLLAR}a) {
      if (${DOLLAR}a !== null) {
        report();
      }
    }, ${DOLLAR}items);
    """,
  )

  /** A nested closure takes the binding of the closure around it, not a name from further out. */
  fun testNestedClosureReportsTheOuterBinding() = assertScope(
    """
    <?php
    ${DOLLAR}outer = function (${DOLLAR}items) {
      return array_map(function (${DOLLAR}a) {
        if (${DOLLAR}a !== null) {
          report();
        }
      }, ${DOLLAR}items);
    };
    """,
    "outer:FUNCTION",
  )

  /** The walk stops at the next scope, so no name comes from outside the function. */
  fun testBindingWalkStopsAtTheEnclosingFunction() = assertScope(
    """
    <?php
    ${DOLLAR}outer = function () {
      function inner() {
        return array_map(function (${DOLLAR}a) {
          if (${DOLLAR}a !== null) {
            report();
          }
        }, ${DOLLAR}items);
      }
      inner();
    };
    """,
    "inner:FUNCTION",
  )

  /** An anonymous class has the empty name too, so the walk passes it and reports the method. */
  fun testAnonymousClassMethodReportsItsMethod() = assertScope(
    """
    <?php
    function make() {
      return new class {
        public function handle(${DOLLAR}a, ${DOLLAR}b) {
          if (${DOLLAR}a !== ${DOLLAR}b) {
            report();
          }
        }
      };
    }
    """,
    "handle:CLASS_METHOD",
  )

  /** The method is nearer than the anonymous class, so it wins. */
  fun testClosureInsideAnonymousClassMethodReportsThatMethod() = assertScope(
    """
    <?php
    function make() {
      return new class {
        public function handle(${DOLLAR}items) {
          return array_map(function (${DOLLAR}a) {
            if (${DOLLAR}a !== null) {
              report();
            }
          }, ${DOLLAR}items);
        }
      };
    }
    """,
    "handle:CLASS_METHOD",
  )

  // endregion

  // region the Pest title

  fun testPestItReportsTitle() = assertScope(
    """
    <?php
    it('rejects a bad port', function () {
      if (${DOLLAR}a !== ${DOLLAR}b) {
        report();
      }
    });
    """,
    "rejects a bad port:FUNCTION",
  )

  fun testPestTestReportsTitle() = assertScope(
    """
    <?php
    test('accepts a good port', function () {
      if (${DOLLAR}a !== ${DOLLAR}b) {
        report();
      }
    });
    """,
    "accepts a good port:FUNCTION",
  )

  fun testPestDescribeAndItReportTheFullPath() = assertScope(
    """
    <?php
    describe('the server', function () {
      it('rejects a bad port', function () {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      });
    });
    """,
    "the server > rejects a bad port:FUNCTION",
  )

  fun testPestTwoNestedDescribesReportEveryLevel() = assertScope(
    """
    <?php
    describe('the server', function () {
      describe('the port check', function () {
        it('rejects a bad port', function () {
          if (${DOLLAR}a !== ${DOLLAR}b) {
            report();
          }
        });
      });
    });
    """,
    "the server > the port check > rejects a bad port:FUNCTION",
  )

  /** A problem in the suite body, outside any test, reports the suite title. */
  fun testPestDescribeBodyReportsSuiteTitle() = assertScope(
    """
    <?php
    describe('the server', function () {
      if (${DOLLAR}a !== ${DOLLAR}b) {
        report();
      }
    });
    """,
    "the server:FUNCTION",
  )

  /** A chained modifier wraps the call, so the closure keeps its own call as the parent. */
  fun testPestChainedModifierReportsTitle() = assertScope(
    """
    <?php
    it('rejects a bad port', function () {
      if (${DOLLAR}a !== ${DOLLAR}b) {
        report();
      }
    })->skip();
    """,
    "rejects a bad port:FUNCTION",
  )

  /** A closure nested inside the test body reports the title through the walk, not through the title rule. */
  fun testPestNestedClosureReportsTitle() = assertScope(
    """
    <?php
    it('rejects a bad port', function () {
      array_map(function (${DOLLAR}a) {
        if (${DOLLAR}a !== null) {
          report();
        }
      }, ${DOLLAR}items);
    });
    """,
    "rejects a bad port:FUNCTION",
  )

  /** A closer binding beats the title, the same way it does for JS. */
  fun testPestCloserBindingBeatsTitle() = assertScope(
    """
    <?php
    it('rejects a bad port', function () {
      ${DOLLAR}inner = function (${DOLLAR}a) {
        if (${DOLLAR}a !== null) {
          report();
        }
      };
      ${DOLLAR}inner(1);
    });
    """,
    "inner:FUNCTION",
  )

  /** A named function inside a test beats the title, because the scope has a real name. */
  fun testPestNamedFunctionInsideTestReportsFunction() = assertScope(
    """
    <?php
    it('rejects a bad port', function () {
      function helper(${DOLLAR}a) {
        if (${DOLLAR}a !== null) {
          report();
        }
      }
      helper(1);
    });
    """,
    "helper:FUNCTION",
  )

  /** Two suites with the same test title keep different names, so the path is load-bearing. */
  fun testPestSameTitleUnderTwoSuitesReportsDifferentNames() = assertScopesOfAllAnchors(
    """
    <?php
    describe('the server', function () {
      it('works', function () {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      });
    });
    describe('the client', function () {
      it('works', function () {
        if (${DOLLAR}c !== ${DOLLAR}d) {
          report();
        }
      });
    });
    """,
    "the server > works:FUNCTION",
    "the client > works:FUNCTION",
  )

  /** A computed title is no string literal, so the rule gives nothing and the closure stays unnamed. */
  fun testPestComputedTitleReportsNoScope() = assertNoScope(
    """
    <?php
    it(${DOLLAR}name, function () {
      if (${DOLLAR}a !== ${DOLLAR}b) {
        report();
      }
    });
    """,
  )

  /** A call that is not a Pest test gives no title, so a string argument cannot leak in. */
  fun testNonPestCallWithStringArgumentReportsNoScope() = assertNoScope(
    """
    <?php
    add_action('init', function () {
      if (${DOLLAR}a !== ${DOLLAR}b) {
        report();
      }
    });
    """,
  )

  /** A sibling call inside the test still reaches the title, through the walk. */
  fun testPestTitleReachesASiblingCallThroughTheWalk() = assertScope(
    """
    <?php
    it('rejects a bad port', function () {
      add_action('init', function () {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      });
    });
    """,
    "rejects a bad port:FUNCTION",
  )

  /** A Pest test inside a PHPUnit-style method keeps the method, because the method has a real name. */
  fun testPestCallInsideNamedFunctionKeepsTheTitleOfTheTest() = assertScope(
    """
    <?php
    function register() {
      it('rejects a bad port', function () {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      });
    }
    """,
    "rejects a bad port:FUNCTION",
  )


  /** An instance method named `test` is no Pest call, so the enclosing function keeps the scope. */
  fun testMethodCallNamedLikeAPestFunctionReportsEnclosingFunction() = assertScope(
    """
    <?php
    function handler(${DOLLAR}v) {
      ${DOLLAR}v->test('rejects a bad port', function () {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      });
    }
    """,
    "handler:FUNCTION",
  )

  /** A static method named `it` is no Pest call either. */
  fun testStaticCallNamedLikeAPestFunctionReportsEnclosingFunction() = assertScope(
    """
    <?php
    function handler() {
      Foo::it('rejects a bad port', function () {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      });
    }
    """,
    "handler:FUNCTION",
  )

  /** An interpolated title is still a string literal, so it keeps its source text, as it does for JS. */
  fun testInterpolatedTitleReportsSourceText() = assertScope(
    """
    <?php
    it("rejects port ${DOLLAR}port", function () {
      if (${DOLLAR}a !== ${DOLLAR}b) {
        report();
      }
    });
    """,
    "rejects port ${DOLLAR}port:FUNCTION",
  )

  /** A `describe` with a computed title contributes no level, so only the test name is left. */
  fun testComputedDescribeTitleDropsItsLevel() = assertScope(
    """
    <?php
    describe(${DOLLAR}suite, function () {
      it('works', function () {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      });
    });
    """,
    "works:FUNCTION",
  )
  // endregion

  // region the AST shape

  /** A `match` arm is a block container now, so the shape stops at the arm. */
  fun testAstShapeInsideMatchArmStopsAtTheArm() = assertAstShapeStopsBefore<FunctionReference>(
    """
    <?php
    function pick(${DOLLAR}v) {
      return match(${DOLLAR}v) {
        1 => doA(${DOLLAR}x),
        default => doB(${DOLLAR}y),
      };
    }
    """,
    "doA(${DOLLAR}x)",
    "Match expression",
  )

  /** `PhpDefaultMatchArm` extends `PhpMatchArm`, so the `default` arm stops the walk too. */
  fun testAstShapeInsideDefaultMatchArmStopsAtTheArm() = assertAstShapeStopsBefore<FunctionReference>(
    """
    <?php
    function pick(${DOLLAR}v) {
      return match(${DOLLAR}v) {
        1 => doA(${DOLLAR}x),
        default => doB(${DOLLAR}y),
      };
    }
    """,
    "doB(${DOLLAR}y)",
    "Match expression",
  )

  /** A `switch` case already stopped the walk, so its shape must not reach the switch. */
  fun testAstShapeInsideSwitchCaseStopsAtTheCase() = assertAstShapeStopsBefore<FunctionReference>(
    """
    <?php
    function pick(${DOLLAR}v) {
      switch (${DOLLAR}v) {
        case 1:
          doA(${DOLLAR}x);
          break;
      }
    }
    """,
    "doA(${DOLLAR}x)",
    "Switch statement",
  )

  /** Two arms of one `match` hold the same structure, so they share the shape. */
  fun testAstShapeOfTwoMatchArmsIsTheSame() {
    val code = """
      <?php
      function pick(${DOLLAR}v) {
        return match(${DOLLAR}v) {
          1 => doA(${DOLLAR}x),
          default => doB(${DOLLAR}y),
        };
      }
    """
    val file = myFixture.configureByText("sample.php", code.trimIndent())
    val calls = PsiTreeUtil.findChildrenOfType(file, FunctionReference::class.java)
    val first = calls.first { it.text == "doA(${DOLLAR}x)" }
    val second = calls.first { it.text == "doB(${DOLLAR}y)" }

    assertEquals(
      PsiSignalExtractor.extractSignals(first).astShape,
      PsiSignalExtractor.extractSignals(second).astShape,
    )
  }

  /** The shape stops at a scope, so a problem on a closure gives the closure alone. */
  fun testAstShapeOfAClosureStopsAtTheClosure() {
    val code = """
      <?php
      ${DOLLAR}handler = function (${DOLLAR}a) {
        report(${DOLLAR}a);
      };
    """
    val file = myFixture.configureByText("sample.php", code.trimIndent())
    val closure = PsiTreeUtil.findChildOfType(file, Function::class.java)!!

    val astShape = PsiSignalExtractor.extractSignals(closure).astShape!!
    assertTrue("the shape was '$astShape'", astShape.startsWith("FUNCTION"))
  }

  // endregion

  /** The problem element type is the raw PSI type of the reported element. */
  fun testProblemElementTypeOfAnIf() {
    val signals = signalsOfAnchor(
      """
      <?php
      function handler(${DOLLAR}a, ${DOLLAR}b) {
        if (${DOLLAR}a !== ${DOLLAR}b) {
          report();
        }
      }
      """
    )

    assertEquals("If", signals.problemElementType)
  }

  // region the lambda shape contract, kept in sync with every other language

  /** An arrow `fn` bounds the shape walk. The shape describes the call, never the binding around it. */
  fun testShapeInsideBoundLambdaStopsAtTheLambda() = assertAstShapeIs<FunctionReference>(
    """
    <?php
    function bar() {
      ${DOLLAR}f = fn() => weigh(1);
    }
    """,
    "weigh(1)",
    "Function call(identifier,Parameter list)",
  )

  /** The same arrow `fn` as an argument gives the same shape. */
  fun testShapeInsideUnboundLambdaStopsAtTheLambda() = assertAstShapeIs<FunctionReference>(
    """
    <?php
    function bar() {
      run(fn() => weigh(1));
    }
    """,
    "weigh(1)",
    "Function call(identifier,Parameter list)",
  )

  /** The invariant the two tests above pin by value. A drift here breaks the baseline of every language. */
  fun testShapeDoesNotDependOnTheLambdaBinding() = assertShapeIgnoresTheBinding<FunctionReference>(
    """
    <?php
    function bar() {
      ${DOLLAR}f = fn() => weigh(1);
    }
    """,
    """
    <?php
    function bar() {
      run(fn() => weigh(1));
    }
    """,
    "weigh(1)",
  )

  // endregion

  override val fileName: String = "sample.php"
  override val anchorType: Class<out PsiElement> = If::class.java
}
