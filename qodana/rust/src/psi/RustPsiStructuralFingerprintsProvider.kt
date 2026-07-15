package psi

import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsArrayExpr
import org.rust.lang.core.psi.RsBlock
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsMatchArm
import org.rust.lang.core.psi.RsStructLiteralBody
import org.rust.lang.core.psi.ext.RsStructOrEnumItemElement
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class RustPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is RsBlock
  override fun isCollectionLiteral(element: PsiElement): Boolean =
    element is RsStructLiteralBody || element is RsArrayExpr
  override fun isCaseClause(element: PsiElement): Boolean = element is RsMatchArm
  override fun isClassDeclaration(element: PsiElement): Boolean = element is RsStructOrEnumItemElement
  override fun isFunctionDeclaration(element: PsiElement): Boolean = element is RsFunction
}
