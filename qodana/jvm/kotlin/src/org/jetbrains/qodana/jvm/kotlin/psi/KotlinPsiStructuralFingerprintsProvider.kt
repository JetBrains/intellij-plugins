package org.jetbrains.qodana.jvm.kotlin.psi

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassInitializer
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtCollectionLiteralExpression
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPropertyAccessor
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

  /** The property that holds an anonymous function, for example `a` in `val a = { }`. */
  override fun declaredName(element: PsiElement): String? = (element as? KtVariableDeclaration)?.name
}
