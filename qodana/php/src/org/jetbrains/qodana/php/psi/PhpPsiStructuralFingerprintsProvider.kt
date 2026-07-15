package org.jetbrains.qodana.php.psi

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.Function
import com.jetbrains.php.lang.psi.elements.GroupStatement
import com.jetbrains.php.lang.psi.elements.PhpCase
import com.jetbrains.php.lang.psi.elements.PhpClass
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class PhpPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is GroupStatement
  override fun isCollectionLiteral(element: PsiElement): Boolean = element is ArrayCreationExpression
  override fun isCaseClause(element: PsiElement): Boolean = element is PhpCase
  override fun isClassDeclaration(element: PsiElement): Boolean = element is PhpClass
  override fun isFunctionDeclaration(element: PsiElement): Boolean = element is Function
}
