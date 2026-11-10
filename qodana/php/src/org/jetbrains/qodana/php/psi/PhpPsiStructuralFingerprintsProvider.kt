package org.jetbrains.qodana.php.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.AssignmentExpression
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.GroupStatement
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.PhpCase
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpMatchArm
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

private const val TEST_PATH_SEPARATOR = " > "
private const val PEST_DESCRIBE = "describe"
private val PEST_TEST_CALLS = setOf("it", "test", PEST_DESCRIBE)
private const val PEST_BODY_ARGUMENT = 1
private const val PEST_TITLE_ARGUMENT = 0

class PhpPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is GroupStatement
  override fun isCollectionLiteral(element: PsiElement): Boolean = element is ArrayCreationExpression

  /** A `match` arm is no [PhpCase]. [PhpMatchArm] covers the `default` arm too. */
  override fun isCaseClause(element: PsiElement): Boolean = element is PhpCase || element is PhpMatchArm

  override fun isClassDeclaration(element: PsiElement): Boolean = element is PhpClass

  /** [Function] covers a named function, a method, a closure and an arrow `fn`. */
  override fun isFunctionDeclaration(element: PsiElement): Boolean = element is Function

  override fun anonymousScopeName(scope: PsiElement): String? = pestTestTitle(scope)

  /**
   * The path of the Pest test whose closure is [scope], such as `a suite > works`.
   *
   * A chained modifier like `->skip()` wraps the call, so the closure keeps its own call as the parent.
   */
  private fun pestTestTitle(scope: PsiElement): String? {
    // A closure sits three levels under its call: Function, Closure, ParameterList, call.
    val argument = scope.parent
    val call = argument?.parent?.parent as? FunctionReference ?: return null
    if (!call.isPestCall() || call.getParameter(PEST_BODY_ARGUMENT) !== argument) return null

    val title = callTitle(call) ?: return null
    return (describeTitles(call) + title).joinToString(TEST_PATH_SEPARATOR)
  }

  /** The `describe` titles around [call], outermost first. A computed title contributes no level. */
  private fun describeTitles(call: FunctionReference): List<String> =
    generateSequence(call.parentOfType<FunctionReference>()) { it.parentOfType<FunctionReference>() }
      .takeWhile { it !is MethodReference && it.name == PEST_DESCRIBE }
      .mapNotNull(::callTitle)
      .toList()
      .asReversed()

  /** Pest declares free functions, so `${'$'}v->test(...)` and `Foo::it(...)` are code under analysis. */
  private fun FunctionReference.isPestCall(): Boolean = this !is MethodReference && name in PEST_TEST_CALLS

  /** The title must be a string literal. An interpolated one keeps its source text, as in JS. */
  private fun callTitle(call: FunctionReference): String? =
    (call.getParameter(PEST_TITLE_ARGUMENT) as? StringLiteralExpression)?.contents

  /** PHP names a closure with the empty string, so the nearest binding labels it. */
  override fun declaredName(element: PsiElement): String? = (element as? AssignmentExpression)?.variable?.name
}
