package org.jetbrains.qodana.jvm.kotlin.psi

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtCollectionLiteralExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class KotlinPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is KtBlockExpression
  override fun isCollectionLiteral(element: PsiElement): Boolean = element is KtCollectionLiteralExpression
  override fun isCaseClause(element: PsiElement): Boolean = element is KtWhenEntry
  override fun isClassDeclaration(element: PsiElement): Boolean = element is KtClassOrObject || element is KtClassBody
  override fun isFunctionDeclaration(element: PsiElement): Boolean = element is KtNamedFunction
}
