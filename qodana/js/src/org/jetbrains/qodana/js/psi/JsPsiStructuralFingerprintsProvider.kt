package org.jetbrains.qodana.js.psi

import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSBlockStatement
import com.intellij.lang.javascript.psi.JSCaseClause
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class JsPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is JSBlockStatement
  override fun isCollectionLiteral(element: PsiElement): Boolean =
    element is JSArrayLiteralExpression || element is JSObjectLiteralExpression
  override fun isCaseClause(element: PsiElement): Boolean = element is JSCaseClause
  override fun isClassDeclaration(element: PsiElement): Boolean = element is JSClass
  override fun isFunctionDeclaration(element: PsiElement): Boolean = element is JSFunction
}
