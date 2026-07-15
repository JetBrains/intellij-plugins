package org.jetbrains.qodana.jvm.java.psi

import com.intellij.psi.PsiArrayInitializerExpression
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiSwitchLabeledRuleStatement
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class JavaPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is PsiCodeBlock
  override fun isCollectionLiteral(element: PsiElement): Boolean = element is PsiArrayInitializerExpression
  override fun isCaseClause(element: PsiElement): Boolean = element is PsiSwitchLabeledRuleStatement
  override fun isClassDeclaration(element: PsiElement): Boolean = element is PsiClass
  override fun isFunctionDeclaration(element: PsiElement): Boolean = element is PsiMethod
}
