package org.jetbrains.qodana.python.community.psi

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyIfStatement
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiSignalExtractor
import org.jetbrains.qodana.staticAnalysis.sarif.psi.PsiSignalExtractorTestBase

/**
 * Verifies [PsiSignalExtractor] over real Python PSI.
 *
 * Python names every `def`. The part that needs care is the set of block containers, which decides
 * where the AST shape walk stops.
 */
class PythonPsiSignalExtractorTest : PsiSignalExtractorTestBase() {

  // region a named scope

  fun testFunctionReportsItsName() = assertScope(
    """
    def handler(a, b):
        if a != b:
            report()
    """,
    "handler:Py:FUNCTION_DECLARATION",
  )

  fun testAsyncFunctionReportsItsName() = assertScope(
    """
    async def handler(a, b):
        if a != b:
            report()
    """,
    "handler:Py:FUNCTION_DECLARATION",
  )

  fun testMethodReportsItsName() = assertScope(
    """
    class Holder:
        def handle(self, a, b):
            if a != b:
                report()
    """,
    "handle:Py:FUNCTION_DECLARATION",
  )

  /** The nearest scope wins, so an inner function hides the outer one. */
  fun testNestedFunctionReportsTheInnerFunction() = assertScope(
    """
    def outer():
        def inner(a, b):
            if a != b:
                report()
        inner(1, 2)
    """,
    "inner:Py:FUNCTION_DECLARATION",
  )

  /** A problem in the class body, outside any method, reports the class. */
  fun testClassBodyReportsTheClass() = assertScope(
    """
    class Holder:
        if a != b:
            report()
    """,
    "Holder:Py:CLASS_DECLARATION",
  )

  /** A decorator belongs to the function, so a problem there keeps the function. */
  fun testDecoratorReportsTheFunction() = assertScopeAt<PyCallExpression>(
    """
    @route(build())
    def handler():
        pass
    """,
    "build()",
    "handler:Py:FUNCTION_DECLARATION",
  )

  /** A problem reported on the function itself, not inside it. The walk starts at the element. */
  fun testProblemOnTheFunctionReportsThatFunction() = assertScopeAt<PyFunction>(
    """
    def handler():
        report()
    """,
    "def handler():\n    report()",
    "handler:Py:FUNCTION_DECLARATION",
  )

  fun testModuleLevelProblemReportsNoScope() = assertNoScope(
    """
    if a != b:
        report()
    """,
  )

  // endregion

  // region a lambda stays transparent

  /** A lambda with no name reports the enclosing function, never the lambda. */
  fun testLambdaReportsEnclosingFunction() = assertScopeAt<PyCallExpression>(
    """
    def handler(items):
        return sorted(items, key=lambda item: weigh(item))
    """,
    "weigh(item)",
    "handler:Py:FUNCTION_DECLARATION",
  )

  /** A lambda in a conditional. The binding walk passes the conditional and finds the name. */
  fun testLambdaInsideConditionalReportsTheName() = assertScopeAt<PyCallExpression>(
    """
    def handler(c):
        pick = (lambda item: weigh(item)) if c else None
        return pick
    """,
    "weigh(item)",
    "pick:Py:LAMBDA_EXPRESSION",
  )

  /** A lambda in parentheses. The walk passes them too. */
  fun testParenthesizedLambdaReportsTheName() = assertScopeAt<PyCallExpression>(
    """
    def handler():
        pick = (lambda item: weigh(item))
        return pick
    """,
    "weigh(item)",
    "pick:Py:LAMBDA_EXPRESSION",
  )

  /** An attribute assignment binds the plain attribute name, never the receiver. */
  fun testLambdaAssignedToAttributeReportsThePlainName() = assertScopeAt<PyCallExpression>(
    """
    class Holder:
        def install(self):
            self.pick = lambda item: weigh(item)
    """,
    "weigh(item)",
    "pick:Py:LAMBDA_EXPRESSION",
  )

  /** A lambda in a dict literal. The binding walk passes the literal and finds the name. */
  fun testLambdaInsideDictLiteralReportsTheBinding() = assertScopeAt<PyCallExpression>(
    """
    def handler():
        table = {"a": lambda item: weigh(item)}
        return table
    """,
    "weigh(item)",
    "table:Py:LAMBDA_EXPRESSION",
  )

  /** Several targets name no single holder, so the lambda stays unnamed. */
  fun testChainedAssignmentReportsEnclosingFunction() = assertScopeAt<PyCallExpression>(
    """
    def handler():
        a = b = lambda item: weigh(item)
        return a
    """,
    "weigh(item)",
    "handler:Py:FUNCTION_DECLARATION",
  )

  /** A lambda that a name holds is a scope, so it reports that name. */
  fun testLambdaBoundToVariableReportsTheName() = assertScopeAt<PyCallExpression>(
    """
    def handler():
        pick = lambda item: weigh(item)
        return pick(1)
    """,
    "weigh(item)",
    "pick:Py:LAMBDA_EXPRESSION",
  )

  /** A bound lambda at module level reports its name, where it used to report nothing. */
  fun testModuleLevelBoundLambdaReportsTheName() = assertScopeAt<PyCallExpression>(
    """
    pick = lambda item: weigh(item)
    """,
    "weigh(item)",
    "pick:Py:LAMBDA_EXPRESSION",
  )

  // endregion

  // region the collection containers

  /**
   * The reported gap. A comprehension builds a collection, so the walk stops at it and serializes the
   * element inside. Without the rule the walk runs past the comprehension and up to the statement.
   */
  fun testAstShapeInsideListComprehensionStopsAtTheComprehension() = assertAstShapeIs<PyCallExpression>(
    """
    def handler(items):
        return [weigh(x) for x in items]
    """,
    "weigh(x)",
    "Py:CALL_EXPRESSION(Py:REFERENCE_EXPRESSION(Py:IDENTIFIER),Py:ARGUMENT_LIST)",
  )

  fun testAstShapeInsideSetComprehensionStopsAtTheComprehension() = assertAstShapeIs<PyCallExpression>(
    """
    def handler(items):
        return {weigh(x) for x in items}
    """,
    "weigh(x)",
    "Py:CALL_EXPRESSION(Py:REFERENCE_EXPRESSION(Py:IDENTIFIER),Py:ARGUMENT_LIST)",
  )

  fun testAstShapeInsideGeneratorStopsAtTheGenerator() = assertAstShapeIs<PyCallExpression>(
    """
    def handler(items):
        return sum(weigh(x) for x in items)
    """,
    "weigh(x)",
    "Py:CALL_EXPRESSION(Py:REFERENCE_EXPRESSION(Py:IDENTIFIER),Py:ARGUMENT_LIST)",
  )

  /** A dict comprehension holds a key and a value, so the walk stops one level higher. */
  fun testAstShapeInsideDictComprehensionStopsAtTheComprehension() = assertAstShapeIs<PyCallExpression>(
    """
    def handler(items):
        return {x: weigh(x) for x in items}
    """,
    "weigh(x)",
    "Py:KEY_VALUE_EXPRESSION(Py:CALL_EXPRESSION(Py:REFERENCE_EXPRESSION,Py:ARGUMENT_LIST))",
  )

  /** A comprehension and a list literal hold the same call, so the two shapes must agree. */
  fun testComprehensionAndListLiteralStopAtTheSameDepth() {
    val inComprehension = astShapeAt<PyCallExpression>(
      """
      def handler(items):
          return [weigh(x) for x in items]
      """,
      "weigh(x)",
    )
    val inLiteral = astShapeAt<PyCallExpression>(
      """
      def handler(items):
          return [weigh(x), weigh(y)]
      """,
      "weigh(x)",
    )

    assertEquals(inLiteral, inComprehension)
  }

  // endregion

  // region the containers that already worked

  fun testAstShapeInsideListLiteralStopsAtTheLiteral() = assertAstShapeStopsBefore<PyCallExpression>(
    """
    def handler():
        return [weigh(1), weigh(2)]
    """,
    "weigh(1)",
    "Py:RETURN_STATEMENT",
  )

  fun testAstShapeInsideDictLiteralStopsAtTheLiteral() = assertAstShapeStopsBefore<PyCallExpression>(
    """
    def handler():
        return {"a": weigh(1)}
    """,
    "weigh(1)",
    "Py:RETURN_STATEMENT",
  )

  fun testAstShapeInsideSetLiteralStopsAtTheLiteral() = assertAstShapeStopsBefore<PyCallExpression>(
    """
    def handler():
        return {weigh(1), weigh(2)}
    """,
    "weigh(1)",
    "Py:RETURN_STATEMENT",
  )

  fun testAstShapeInsideTupleStopsAtTheTuple() = assertAstShapeStopsBefore<PyCallExpression>(
    """
    def handler():
        return weigh(1), weigh(2)
    """,
    "weigh(1)",
    "Py:RETURN_STATEMENT",
  )

  fun testAstShapeInsideMatchCaseStopsAtTheCase() = assertAstShapeStopsBefore<PyCallExpression>(
    """
    def pick(v):
        match v:
            case 1:
                weigh(1)
    """,
    "weigh(1)",
    "Py:MATCH_STATEMENT",
  )

  /** A wildcard `case _` is a [com.jetbrains.python.psi.PyCaseClause] too. */
  fun testAstShapeInsideWildcardCaseStopsAtTheCase() = assertAstShapeStopsBefore<PyCallExpression>(
    """
    def pick(v):
        match v:
            case 1:
                weigh(1)
            case _:
                weigh(2)
    """,
    "weigh(2)",
    "Py:MATCH_STATEMENT",
  )

  // endregion

  // region the lambda shape contract, kept in sync with every other language

  /** A lambda bounds the shape walk. The shape describes the call, never the binding around it. */
  fun testShapeInsideBoundLambdaStopsAtTheLambda() = assertAstShapeIs<PyCallExpression>(
    """
    def handler():
        pick = lambda i: weigh(i)
    """,
    "weigh(i)",
    "Py:CALL_EXPRESSION(Py:REFERENCE_EXPRESSION(Py:IDENTIFIER),Py:ARGUMENT_LIST)",
  )

  /** The same lambda as an argument gives the same shape. */
  fun testShapeInsideUnboundLambdaStopsAtTheLambda() = assertAstShapeIs<PyCallExpression>(
    """
    def handler(items):
        return sorted(items, key=lambda i: weigh(i))
    """,
    "weigh(i)",
    "Py:CALL_EXPRESSION(Py:REFERENCE_EXPRESSION(Py:IDENTIFIER),Py:ARGUMENT_LIST)",
  )

  /** The invariant the two tests above pin by value. A drift here breaks the baseline of every language. */
  fun testShapeDoesNotDependOnTheLambdaBinding() = assertShapeIgnoresTheBinding<PyCallExpression>(
    """
    def handler():
        pick = lambda i: weigh(i)
    """,
    """
    def handler(items):
        return sorted(items, key=lambda i: weigh(i))
    """,
    "weigh(i)",
  )

  // endregion

  fun testProblemElementTypeOfAnIf() {
    val signals = signalsOfAnchor(
      """
      def handler(a, b):
          if a != b:
              report()
      """
    )

    assertEquals("Py:IF_STATEMENT", signals.problemElementType)
  }

  override val fileName: String = "sample.py"
  override val anchorType: Class<out PsiElement> = PyIfStatement::class.java
}
