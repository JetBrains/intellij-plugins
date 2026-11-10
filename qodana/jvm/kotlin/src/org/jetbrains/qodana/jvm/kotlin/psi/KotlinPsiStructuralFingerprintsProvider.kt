package org.jetbrains.qodana.jvm.kotlin.psi

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassInitializer
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtCollectionLiteralExpression
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class KotlinPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is KtBlockExpression
  override fun isCollectionLiteral(element: PsiElement): Boolean = element is KtCollectionLiteralExpression
  override fun isCaseClause(element: PsiElement): Boolean = element is KtWhenEntry
  override fun isClassDeclaration(element: PsiElement): Boolean = element is KtClassOrObject || element is KtClassBody

  /** Accepts [KtLambdaExpression], never `KtFunctionLiteral`, whose name is the literal `<anonymous>`. */
  override fun isFunctionDeclaration(element: PsiElement): Boolean =
    element is KtNamedFunction || element is KtLambdaExpression ||
    element is KtPropertyAccessor || element is KtConstructor<*> || element is KtClassInitializer

  /** An `init { }` block has no name, so its class labels it. Its own element type keeps it apart. */
  override fun anonymousScopeName(scope: PsiElement): String? =
    (scope as? KtClassInitializer)?.containingClassOrObject?.name

  /** The name that holds an anonymous function. A `val a = { }` and an `a = { }` both give `a`. */
  override fun declaredName(element: PsiElement): String? = when (element) {
    is KtVariableDeclaration -> element.name
    is KtBinaryExpression -> assignedName(element)
    else -> null
  }

  /**
   * The plain name an assignment writes to. `this.a = { }` gives `a`, as Java and JS report it.
   *
   * Only `=` binds a name. A `+=` reads the target and writes it back, so it labels nothing.
   */
  private fun assignedName(assignment: KtBinaryExpression): String? {
    if (assignment.operationToken != KtTokens.EQ) return null

    val target = assignment.left
    val reference = (target as? KtQualifiedExpression)?.selectorExpression ?: target
    return (reference as? KtNameReferenceExpression)?.getReferencedName()
  }
}
