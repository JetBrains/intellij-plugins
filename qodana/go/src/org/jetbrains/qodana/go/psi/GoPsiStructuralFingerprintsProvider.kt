package org.jetbrains.qodana.go.psi

import com.goide.psi.GoBlock
import com.goide.psi.GoCaseClause
import com.goide.psi.GoFunctionOrMethodDeclaration
import com.goide.psi.GoLiteralValue
import com.goide.psi.GoTypeSpec
import com.intellij.psi.PsiElement
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class GoPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is GoBlock
  override fun isCollectionLiteral(element: PsiElement): Boolean = element is GoLiteralValue
  override fun isCaseClause(element: PsiElement): Boolean = element is GoCaseClause
  override fun isClassDeclaration(element: PsiElement): Boolean = element is GoTypeSpec
  override fun isFunctionDeclaration(element: PsiElement): Boolean = element is GoFunctionOrMethodDeclaration
}
