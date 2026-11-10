package org.jetbrains.qodana.jvm.kotlin.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiSignalExtractor

/**
 * Verifies [PsiSignalExtractor] over real Kotlin PSI.
 *
 * Kotlin reaches an unnamed scope by two routes. A lambda is a `KtLambdaExpression`, and an anonymous
 * `fun() {}` is a [org.jetbrains.kotlin.psi.KtNamedFunction] with no name. Either one takes the name of the
 * property that holds it. With no property, the walk continues to the enclosing named function.
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

  /** An anonymous `fun() {}` has no name, so the property that holds it labels the scope. */
  fun testAnonymousFunctionInsideNamedFunctionReportsProperty() = assertScope(
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
    "inner",
  )

  /** A lambda held by a top-level `val`. The property labels the scope, as it does in JS. */
  fun testLambdaAssignedToTopLevelValReportsProperty() = assertScope(
    """
    val a = {
      if (x != y) {
        report()
      }
    }
    """,
    "a",
  )

  /** The same for an anonymous `fun() {}` held by a top-level `val`. */
  fun testAnonymousFunctionAssignedToTopLevelValReportsProperty() = assertScope(
    """
    val a = fun() {
      if (x != y) {
        report()
      }
    }
    """,
    "a",
  )

  /** An assignment binds a name too, so a lambda written to an existing variable reports it. */
  fun testLambdaAssignedToExistingVariableReportsTheName() = assertScope(
    """
    fun handler() {
      var h: (() -> Unit)? = null
      h = {
        if (x != y) {
          report()
        }
      }
    }
    """,
    "h",
  )

  /** The same for an anonymous `fun() {}`. */
  fun testAnonymousFunctionAssignedToExistingVariableReportsTheName() = assertScope(
    """
    fun handler() {
      var h: (() -> Unit)? = null
      h = fun() {
        if (x != y) {
          report()
        }
      }
    }
    """,
    "h",
  )

  /** A qualified target reports the plain name, never the qualifier, as Java and JS report it. */
  fun testLambdaAssignedToPropertyReportsThePlainName() = assertScope(
    """
    class Holder {
      var f: (() -> Unit)? = null

      fun handler() {
        this.f = {
          if (x != y) {
            report()
          }
        }
      }
    }
    """,
    "f",
  )

  /** Only `=` binds. A `+=` reads the target and writes it back, so it labels nothing. */
  fun testCompoundAssignmentReportsEnclosingFunction() = assertScope(
    """
    fun handler() {
      var total = 0
      total += items.sumOf { if (x != y) 1 else 2 }
    }
    """,
    "handler",
  )

  /** A trailing-lambda call with no binding and no named ancestor. Nothing can label this scope. */
  fun testUnboundLambdaReportsNoScope() = assertNoScope(
    """
    items.forEach {
      if (x != y) {
        report()
      }
    }
    """
  )

  /** A top-level property initializer sits in no scope at all. A property in a class would report the class. */
  fun testTopLevelPropertyReportsNoScope() = assertNoScope(
    """
    val a = if (x != y) 1 else 2
    """
  )

  // --- astShape. A lambda is a scope now, so it is also a boundary of the astShape walk. ---

  /** A statement in a lambda block. The block was always a boundary, so the shape does not change. */
  fun testAstShapeOfStatementInLambdaBlock() = assertSig<KtCallExpression>(
    """
    fun handler() {
      items.forEach {
        report()
      }
    }
    """,
    "CALL_EXPRESSION(REFERENCE_EXPRESSION(IDENTIFIER),VALUE_ARGUMENT_LIST)  [scope=handler:FUNCTION]",
    text = "report()",
  )

  /** The lambda itself. It is a scope now, so it reports its own shape at depth 1. */
  fun testAstShapeOfLambdaItself() = assertSig<KtLambdaExpression>(
    """
    val a = {
      report()
    }
    """,
    "LAMBDA_EXPRESSION(FUNCTION_LITERAL)  [scope=a:LAMBDA_EXPRESSION]",
  )

  /** The body of a lambda. The walk stops under the lambda instead of leaving it. */
  fun testAstShapeOfLambdaBody() = assertSig<KtBlockExpression>(
    """
    val a = {
      report()
    }
    """,
    "FUNCTION_LITERAL(BLOCK(CALL_EXPRESSION))  [scope=a:LAMBDA_EXPRESSION]",
  )

  /** A lambda argument with no property. The shape is the lambda, and the scope is the outer function. */
  fun testAstShapeOfLambdaArgument() = assertSig<KtLambdaExpression>(
    """
    fun handler() {
      items.forEach {
        report()
      }
    }
    """,
    "LAMBDA_EXPRESSION(FUNCTION_LITERAL)  [scope=handler:FUNCTION]",
  )

  /** Pins `astShape` with the scope, in the format `astShape  [scope=name:type]`. */
  private inline fun <reified T : PsiElement> assertSig(code: String, expected: String, text: String? = null) {
    val file = myFixture.configureByText("sample.kt", code.trimIndent())
    val candidates = PsiTreeUtil.findChildrenOfType(file, T::class.java)
    val element = if (text == null) candidates.firstOrNull() else candidates.firstOrNull { it.text == text }
    assertNotNull("no ${T::class.simpleName}" + (text?.let { " with text '$it'" } ?: ""), element)

    val signals = PsiSignalExtractor.extractSignals(element!!)
    assertEquals(expected, "${signals.astShape ?: "-"}  [scope=${signals.enclosingScopeName ?: "-"}:${signals.enclosingScopeType ?: "-"}]")
  }

  // --- An accessor and a constructor are scopes too, so a member names them instead of the class. ---

  /** A getter with a block body. The property names the scope, never the class that holds it. */
  fun testGetterReportsProperty() = assertSig<KtIfExpression>(
    """
    class Holder {
      val value: Int
        get() {
          if (x != y) {
            report()
          }
          return 1
        }
    }
    """,
    "IF(if,CONDITION(BINARY_EXPRESSION),THEN(BLOCK))  [scope=value:PROPERTY_ACCESSOR]",
  )

  /** The same for an expression-bodied getter. */
  fun testExpressionGetterReportsProperty() = assertSig<KtIfExpression>(
    """
    class Holder {
      val value: Int get() = if (x != y) 1 else 2
    }
    """,
    "IF(if,CONDITION(BINARY_EXPRESSION),THEN,else,ELSE)  [scope=value:PROPERTY_ACCESSOR]",
  )

  /** A setter reports the same property. A getter and a setter of one property share the scope. */
  fun testSetterReportsProperty() = assertSig<KtIfExpression>(
    """
    class Holder {
      var value: Int = 0
        set(v) {
          if (x != y) {
            report()
          }
        }
    }
    """,
    "IF(if,CONDITION(BINARY_EXPRESSION),THEN(BLOCK))  [scope=value:PROPERTY_ACCESSOR]",
  )

  /** A secondary constructor carries the name of its class, with its own element type. */
  fun testSecondaryConstructorReportsClass() = assertSig<KtIfExpression>(
    """
    class Holder {
      constructor(a: Int) {
        if (x != y) {
          report()
        }
      }
    }
    """,
    "IF(if,CONDITION(BINARY_EXPRESSION),THEN(BLOCK))  [scope=Holder:SECONDARY_CONSTRUCTOR]",
  )

  /** An `init { }` block takes the class name, and its element type keeps it apart from the class body. */
  fun testInitBlockReportsClassWithItsOwnType() = assertSig<KtIfExpression>(
    """
    class Holder {
      init {
        if (x != y) {
          report()
        }
      }
    }
    """,
    "IF(if,CONDITION(BINARY_EXPRESSION),THEN(BLOCK))  [scope=Holder:CLASS_INITIALIZER]",
  )

  /** A lambda in a default value of the primary constructor stops at the constructor, not at the class. */
  fun testLambdaInPrimaryConstructorDefaultReportsConstructor() = assertSig<KtIfExpression>(
    """
    class Holder(val f: () -> Int = { if (x != y) 1 else 2 })
    """,
    "IF(if,CONDITION(BINARY_EXPRESSION),THEN,else,ELSE)  [scope=Holder:PRIMARY_CONSTRUCTOR]",
  )

  /** A lambda in a getter stops at the accessor, so it reports the property, as the getter body does. */
  fun testLambdaInGetterReportsProperty() = assertSig<KtIfExpression>(
    """
    class Holder {
      val value: Int
        get() = items.map { if (x != y) 1 else 2 }.first()
    }
    """,
    "IF(if,CONDITION(BINARY_EXPRESSION),THEN,else,ELSE)  [scope=value:PROPERTY_ACCESSOR]",
  )

  // region the lambda shape contract, kept in sync with every other language

  /** A lambda bounds the shape walk. The shape describes the call, never the binding around it. */
  fun testShapeInsideBoundLambdaStopsAtTheLambda() = assertSig<KtCallExpression>(
    """
    fun bar() { val f = { weigh(1) } }
    """,
    "CALL_EXPRESSION(REFERENCE_EXPRESSION(IDENTIFIER),VALUE_ARGUMENT_LIST(VALUE_ARGUMENT))  [scope=f:LAMBDA_EXPRESSION]",
    text = "weigh(1)",
  )

  /** The same lambda as a trailing argument gives the same shape, and the enclosing function names it. */
  fun testShapeInsideUnboundLambdaStopsAtTheLambda() = assertSig<KtCallExpression>(
    """
    fun bar() { run { weigh(1) } }
    """,
    "CALL_EXPRESSION(REFERENCE_EXPRESSION(IDENTIFIER),VALUE_ARGUMENT_LIST(VALUE_ARGUMENT))  [scope=bar:FUNCTION]",
    text = "weigh(1)",
  )

  /** The invariant the two tests above pin by value. A drift here breaks the baseline of every language. */
  fun testShapeDoesNotDependOnTheLambdaBinding() {
    val bound = astShapeOf<KtCallExpression>("bound.kt", "fun bar() { val f = { weigh(1) } }", "weigh(1)")

    assertNotNull(bound)
    assertEquals("the shape must not depend on the binding", bound,
                 astShapeOf<KtCallExpression>("argument.kt", "fun bar() { run { weigh(1) } }", "weigh(1)"))
  }

  /** The AST shape at the [T] whose text equals [text]. Each snippet needs its own [fileName]. */
  private inline fun <reified T : PsiElement> astShapeOf(fileName: String, code: String, text: String): String? {
    val file = myFixture.configureByText(fileName, code)
    val element = PsiTreeUtil.findChildrenOfType(file, T::class.java).firstOrNull { it.text == text }
    assertNotNull("no ${T::class.simpleName} with text '$text'", element)

    return PsiSignalExtractor.extractSignals(element!!).astShape
  }

  // endregion

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
