package org.jetbrains.qodana.go.psi

import com.goide.GoTestUtil
import com.goide.psi.GoCallExpr
import com.goide.psi.GoFunctionDeclaration
import com.goide.psi.GoIfStatement
import com.goide.psi.GoTypeReferenceExpression
import com.intellij.psi.PsiElement
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiSignalExtractor
import org.jetbrains.qodana.staticAnalysis.sarif.psi.PsiSignalExtractorTestBase

/**
 * Verifies [PsiSignalExtractor] over real Go PSI.
 *
 * Go names almost every function. The gap is a subtest: many `t.Run` closures share one test function.
 * A subtest with a literal name reports its own `go test` name. Every other closure stays transparent.
 */
class GoPsiSignalExtractorTest : PsiSignalExtractorTestBase() {

  override fun setUp() {
    super.setUp()
    // Mounts the temporary file system as a GOPATH root, so `/src/testing` resolves without a real Go SDK.
    GoTestUtil.setupGoSupport(this, project, module, true, null)
    myFixture.addFileToProject(
      "testing/testing.go",
      """
      package testing

      type T struct{}
      type B struct{}

      func (t *T) Run(name string, f func(t *T)) bool {
        return true
      }

      func (b *B) Run(name string, f func(b *B)) bool {
        return true
      }
      """.trimIndent(),
    )
  }

  // region a named declaration

  fun testFunctionReportsItsName() = assertScope(
    """
    package main

    func handler(a int, b int) {
      if a != b {
        report()
      }
    }
    """,
    "handler:FUNCTION_DECLARATION",
  )

  fun testMethodReportsItsName() = assertScope(
    """
    package main

    type Holder struct{}

    func (h *Holder) handle(a int, b int) {
      if a != b {
        report()
      }
    }
    """,
    "handle:METHOD_DECLARATION",
  )

  /** A `func init()` has a real name. Two of them in one file still collide. */
  fun testInitFunctionReportsItsName() = assertScope(
    """
    package main

    func init() {
      if a != b {
        report()
      }
    }
    """,
    "init:FUNCTION_DECLARATION",
  )

  /** A problem outside any function reports the type. */
  fun testStructFieldTypeReportsTheType() = assertScopeAt<GoTypeReferenceExpression>(
    """
    package main

    type Holder struct {
      handler SomeType
    }
    """,
    "SomeType",
    "Holder:TYPE_SPEC",
  )

  fun testInterfaceMethodReportsTheType() = assertScopeAt<GoTypeReferenceExpression>(
    """
    package main

    type Reader interface {
      Read(p SomeType) error
    }
    """,
    "SomeType",
    "Reader:TYPE_SPEC",
  )

  /** The walk starts at the element, so a problem on the function reports it. */
  fun testProblemOnTheFunctionReportsThatFunction() = assertScopeAt<GoFunctionDeclaration>(
    """
    package main

    func handler() {
      report()
    }
    """,
    "func handler() {\n  report()\n}",
    "handler:FUNCTION_DECLARATION",
  )

  fun testTopLevelVarReportsNoScope() = assertNoScopeAt<GoCallExpr>(
    """
    package main

    var limit = compute()
    """,
    "compute()",
  )

  // endregion

  // region a closure stays transparent

  fun testGoroutineReportsEnclosingFunction() = assertScope(
    """
    package main

    func handler(a int, b int) {
      go func() {
        if a != b {
          report()
        }
      }()
    }
    """,
    "handler:FUNCTION_DECLARATION",
  )

  fun testDeferredClosureReportsEnclosingFunction() = assertScope(
    """
    package main

    func handler(a int, b int) {
      defer func() {
        if a != b {
          report()
        }
      }()
    }
    """,
    "handler:FUNCTION_DECLARATION",
  )

  /** A callback argument is the common Go shape. It keeps the enclosing function. */
  fun testCallbackArgumentReportsEnclosingFunction() = assertScope(
    """
    package main

    func handler(items []int, a int, b int) {
      sortSlice(items, func(i int, j int) bool {
        if a != b {
          report()
        }
        return true
      })
    }
    """,
    "handler:FUNCTION_DECLARATION",
  )

  /** A closure that a name holds is a scope, so it reports that name. */
  fun testClosureBoundToVariableReportsTheName() = assertScope(
    """
    package main

    func handler(a int, b int) {
      inner := func() {
        if a != b {
          report()
        }
      }
      inner()
    }
    """,
    "inner:FUNCTION_LIT",
  )

  /** A bound closure at top level reports its name, where it used to report nothing. */
  fun testTopLevelBoundClosureReportsTheName() = assertScope(
    """
    package main

    var handler = func(a int, b int) {
      if a != b {
        report()
      }
    }
    """,
    "handler:FUNCTION_LIT",
  )

  /** An assignment binds a name too, so a closure written to an existing variable reports it. */
  fun testClosureAssignedToExistingVariableReportsTheName() = assertScope(
    """
    package main

    func handler(a int, b int) {
      var inner func()
      inner = func() {
        if a != b {
          report()
        }
      }
      inner()
    }
    """,
    "inner:FUNCTION_LIT",
  )

  /** A field assignment binds the plain field name, never the receiver. */
  fun testClosureAssignedToFieldReportsThePlainName() = assertScope(
    """
    package main

    type Holder struct{ fn func() }

    func handler(h *Holder, a int, b int) {
      h.fn = func() {
        if a != b {
          report()
        }
      }
    }
    """,
    "fn:FUNCTION_LIT",
  )

  /** A closure in a struct literal. The binding walk passes the literal and finds the name. */
  fun testClosureInStructLiteralReportsTheBinding() = assertScope(
    """
    package main

    func handler(a int, b int) {
      holder := Holder{fn: func() {
        if a != b {
          report()
        }
      }}
      _ = holder
    }
    """,
    "holder:FUNCTION_LIT",
  )

  /** An unnamed closure is transparent, so the walk reaches the bound closure around it. */
  fun testNestedClosuresReportTheOuterBinding() = assertScope(
    """
    package main

    func handler(a int, b int) {
      outer := func() {
        go func() {
          if a != b {
            report()
          }
        }()
      }
      outer()
    }
    """,
    "outer:FUNCTION_LIT",
  )

  fun testReturnedClosureReportsEnclosingFunction() = assertScope(
    """
    package main

    func makeHandler(a int, b int) func() {
      return func() {
        if a != b {
          report()
        }
      }
    }
    """,
    "makeHandler:FUNCTION_DECLARATION",
  )

  fun testClosureInMethodReportsTheMethod() = assertScope(
    """
    package main

    type Holder struct{}

    func (h *Holder) handle(a int, b int) {
      go func() {
        if a != b {
          report()
        }
      }()
    }
    """,
    "handle:METHOD_DECLARATION",
  )

  // endregion

  // region the subtest name

  fun testSubTestReportsItsFullName() = assertScope(
    """
    package main

    import "testing"

    func TestServer(t *testing.T) {
      t.Run("rejects a bad port", func(t *testing.T) {
        if a != b {
          report()
        }
      })
    }
    """,
    "TestServer/rejects_a_bad_port:FUNCTION_LIT",
  )

  /** The reported gap: two subtests used to share the name of their test function. */
  fun testTwoSubTestsReportDifferentNames() = assertScopesOfAllAnchors(
    """
    package main

    import "testing"

    func TestServer(t *testing.T) {
      t.Run("rejects a bad port", func(t *testing.T) {
        if a != b {
          report()
        }
      })
      t.Run("accepts a good port", func(t *testing.T) {
        if c != d {
          report()
        }
      })
    }
    """,
    "TestServer/rejects_a_bad_port:FUNCTION_LIT",
    "TestServer/accepts_a_good_port:FUNCTION_LIT",
  )

  fun testNestedSubTestReportsEveryLevel() = assertScope(
    """
    package main

    import "testing"

    func TestServer(t *testing.T) {
      t.Run("the port check", func(t *testing.T) {
        t.Run("rejects a bad port", func(t *testing.T) {
          if a != b {
            report()
          }
        })
      })
    }
    """,
    "TestServer/the_port_check/rejects_a_bad_port:FUNCTION_LIT",
  )

  /** A benchmark uses `*testing.B`, which the Go plugin accepts too. */
  fun testBenchmarkSubTestReportsItsFullName() = assertScope(
    """
    package main

    import "testing"

    func BenchmarkServer(b *testing.B) {
      b.Run("one port", func(b *testing.B) {
        if a != c {
          report()
        }
      })
    }
    """,
    "BenchmarkServer/one_port:FUNCTION_LIT",
  )

  /** The documented gap: a subtest named from a struct field has no constant name, so nothing changes. */
  fun testTableDrivenSubTestReportsTheTestFunction() = assertScope(
    """
    package main

    import "testing"

    func TestServer(t *testing.T) {
      for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {
          if a != b {
            report()
          }
        })
      }
    }
    """,
    "TestServer:FUNCTION_DECLARATION",
  )

  /** A closure nested inside a subtest reaches the subtest through the walk. */
  fun testClosureInsideSubTestReportsTheSubTest() = assertScope(
    """
    package main

    import "testing"

    func TestServer(t *testing.T) {
      t.Run("rejects a bad port", func(t *testing.T) {
        go func() {
          if a != b {
            report()
          }
        }()
      })
    }
    """,
    "TestServer/rejects_a_bad_port:FUNCTION_LIT",
  )

  /** Outside any subtest the test function stays the scope. */
  fun testCodeOutsideASubTestReportsTheTestFunction() = assertScope(
    """
    package main

    import "testing"

    func TestServer(t *testing.T) {
      if a != b {
        report()
      }
      t.Run("rejects a bad port", func(t *testing.T) {
        report()
      })
    }
    """,
    "TestServer:FUNCTION_DECLARATION",
  )

  /** A call that is not `t.Run` gives no name, so no string argument leaks in. */
  fun testNonTestCallWithStringArgumentReportsEnclosingFunction() = assertScope(
    """
    package main

    func handler(a int, b int) {
      withRetry("rejects a bad port", func() {
        if a != b {
          report()
        }
      })
    }
    """,
    "handler:FUNCTION_DECLARATION",
  )

  /** A `Run` on another receiver is no subtest. */
  fun testRunOnAnotherReceiverReportsEnclosingFunction() = assertScope(
    """
    package main

    import "testing"

    type Runner struct{}

    func (r *Runner) Run(name string, f func()) {}

    func TestServer(t *testing.T) {
      runner := Runner{}
      runner.Run("rejects a bad port", func() {
        if a != b {
          report()
        }
      })
    }
    """,
    "TestServer:FUNCTION_DECLARATION",
  )

  // endregion

  // region the AST shape

  /** The shape stops at a subtest body, which is a scope now. */
  fun testAstShapeOfASubTestBodyStopsAtTheBody() {
    val astShape = signalsOfAnchor(
      """
      package main

      import "testing"

      func TestServer(t *testing.T) {
        t.Run("rejects a bad port", func(t *testing.T) {
          if a != b {
            report()
          }
        })
      }
      """
    ).astShape

    assertNotNull(astShape)
    assertTrue("the shape was '$astShape'", astShape!!.startsWith("IF_STATEMENT"))
  }

  /** A plain closure is no scope, so the shape inside one is unchanged. */
  fun testAstShapeInsideAPlainClosureMatchesAPlainFunction() {
    val inClosure = signalsOfAnchor(
      """
      package main

      func handler(a int, b int) {
        go func() {
          if a != b {
            report()
          }
        }()
      }
      """
    ).astShape
    val inFunction = signalsOfAnchor(
      """
      package main

      func handler(a int, b int) {
        if a != b {
          report()
        }
      }
      """
    ).astShape

    assertEquals(inFunction, inClosure)
  }

  /** A `switch` case stops the walk. */
  fun testAstShapeInsideSwitchCaseStopsAtTheCase() = assertAstShapeStopsBefore<GoCallExpr>(
    """
    package main

    func pick(v int) {
      switch v {
      case 1:
        doA(x)
      }
    }
    """,
    "doA(x)",
    "SWITCH_STATEMENT",
  )

  /** A `select` comm clause is a [com.goide.psi.GoCaseClause] too, so it stops the walk. */
  fun testAstShapeInsideSelectClauseStopsAtTheClause() = assertAstShapeStopsBefore<GoCallExpr>(
    """
    package main

    func pick(ch chan int) {
      select {
      case v := <-ch:
        doA(v)
      }
    }
    """,
    "doA(v)",
    "SELECT_STATEMENT",
  )

  /** A composite literal stops the walk. */
  fun testAstShapeInsideCompositeLiteralStopsAtTheLiteral() = assertAstShapeStopsBefore<GoCallExpr>(
    """
    package main

    func build() []int {
      return []int{compute(), 2, 3}
    }
    """,
    "compute()",
    "RETURN_STATEMENT",
  )

  // endregion

  // region the lambda shape contract, kept in sync with every other language

  /** A closure bounds the shape walk. The shape describes the call, never the binding around it. */
  fun testShapeInsideBoundLambdaStopsAtTheLambda() = assertAstShapeIs<GoCallExpr>(
    """
    package main

    func handler() {
      h := func() { weigh(1) }
      h()
    }
    """,
    "weigh(1)",
    "SIMPLE_STATEMENT(LEFT_HAND_EXPR_LIST)",
  )

  /** The same closure as an argument gives the same shape. */
  fun testShapeInsideUnboundLambdaStopsAtTheLambda() = assertAstShapeIs<GoCallExpr>(
    """
    package main

    func handler() {
      run(func() { weigh(1) })
    }
    """,
    "weigh(1)",
    "SIMPLE_STATEMENT(LEFT_HAND_EXPR_LIST)",
  )

  /** The invariant the two tests above pin by value. A drift here breaks the baseline of every language. */
  fun testShapeDoesNotDependOnTheLambdaBinding() = assertShapeIgnoresTheBinding<GoCallExpr>(
    """
    package main

    func handler() {
      h := func() { weigh(1) }
      h()
    }
    """,
    """
    package main

    func handler() {
      run(func() { weigh(1) })
    }
    """,
    "weigh(1)",
  )

  // endregion

  fun testProblemElementTypeOfAnIf() {
    val signals = signalsOfAnchor(
      """
      package main

      func handler(a int, b int) {
        if a != b {
          report()
        }
      }
      """
    )

    assertEquals("IF_STATEMENT", signals.problemElementType)
  }

  override val fileName: String = "sample_test.go"
  override val anchorType: Class<out PsiElement> = GoIfStatement::class.java
}
