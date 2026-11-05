package org.jetbrains.qodana.jvm.java.psi

import com.intellij.psi.PsiArrayInitializerExpression
import com.intellij.psi.PsiAssignmentExpression
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassInitializer
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLambdaExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiSwitchLabeledRuleStatement
import com.intellij.psi.PsiTypeParameter
import com.intellij.psi.PsiVariable
import com.intellij.psi.util.PsiUtil
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class JavaPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is PsiCodeBlock
  override fun isCollectionLiteral(element: PsiElement): Boolean = element is PsiArrayInitializerExpression
  override fun isCaseClause(element: PsiElement): Boolean = element is PsiSwitchLabeledRuleStatement

  /** A [PsiTypeParameter] is a [PsiClass] too, and no scope: `<T extends Foo>` must not report `T`. */
  override fun isClassDeclaration(element: PsiElement): Boolean = element is PsiClass && element !is PsiTypeParameter

  override fun isFunctionDeclaration(element: PsiElement): Boolean =
    element is PsiMethod || element is PsiLambdaExpression || element is PsiClassInitializer

  /** A `static { }` or instance initializer has no name, so its class labels it. */
  override fun anonymousScopeName(scope: PsiElement): String? = (scope as? PsiClassInitializer)?.containingClass?.name

  /** Java wraps an expression in parentheses, so a step skips them. */
  override fun bindingWalkParent(element: PsiElement): PsiElement? = PsiUtil.skipParenthesizedExprUp(element.parent)

  /** Covers a field, a local variable and an assignment to a reference. */
  override fun declaredName(element: PsiElement): String? = when (element) {
    is PsiVariable -> element.name
    is PsiAssignmentExpression -> (element.lExpression as? PsiReferenceExpression)?.referenceName
    else -> null
  }
}
