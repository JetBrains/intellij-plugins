package org.jetbrains.qodana.python.community.psi

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyCaseClause
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyDictLiteralExpression
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyListLiteralExpression
import com.jetbrains.python.psi.PySetLiteralExpression
import com.jetbrains.python.psi.PyStatementList
import com.jetbrains.python.psi.PyTupleExpression
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class PythonPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is PyStatementList
  override fun isCollectionLiteral(element: PsiElement): Boolean =
    element is PyListLiteralExpression || element is PyDictLiteralExpression ||
    element is PySetLiteralExpression || element is PyTupleExpression
  override fun isCaseClause(element: PsiElement): Boolean = element is PyCaseClause
  override fun isClassDeclaration(element: PsiElement): Boolean = element is PyClass
  override fun isFunctionDeclaration(element: PsiElement): Boolean = element is PyFunction
}
