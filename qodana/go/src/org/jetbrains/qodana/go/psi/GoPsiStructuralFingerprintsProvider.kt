package org.jetbrains.qodana.go.psi

import com.goide.execution.GoRunUtil
import com.goide.psi.GoArgumentList
import com.goide.psi.GoAssignmentStatement
import com.goide.psi.GoBlock
import com.goide.psi.GoCallExpr
import com.goide.psi.GoCaseClause
import com.goide.psi.GoFunctionLit
import com.goide.psi.GoFunctionOrMethodDeclaration
import com.goide.psi.GoLiteralValue
import com.goide.psi.GoReferenceExpression
import com.goide.psi.GoTypeSpec
import com.goide.psi.GoVarSpec
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class GoPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is GoBlock
  override fun isCollectionLiteral(element: PsiElement): Boolean = element is GoLiteralValue
  override fun isCaseClause(element: PsiElement): Boolean = element is GoCaseClause
  override fun isClassDeclaration(element: PsiElement): Boolean = element is GoTypeSpec

  /** A declaration and a closure. A closure with no name stays unnamed, never hidden. */
  override fun isFunctionDeclaration(element: PsiElement): Boolean =
    element is GoFunctionOrMethodDeclaration || element is GoFunctionLit

  /** A subtest name beats the binding, because `go test` addresses the subtest by it. */
  override fun anonymousScopeName(scope: PsiElement): String? = subTestName(scope)

  /**
   * The name that `h := func() { }` or `h = func() { }` binds.
   *
   * A statement with several names gives none, because it cannot say which name holds the closure.
   */
  override fun declaredName(element: PsiElement): String? = when (element) {
    is GoVarSpec -> element.varDefinitionList.singleOrNull()?.name
    is GoAssignmentStatement -> assignedName(element)
    else -> null
  }

  private fun assignedName(assignment: GoAssignmentStatement): String? =
    (assignment.leftHandExprList.expressionList.singleOrNull() as? GoReferenceExpression)?.identifier?.text

  /**
   * The `go test` name of the subtest whose body is [element], such as `TestServer/rejects_a_bad_port`.
   *
   * The Go plugin owns the callees and the naming. A computed name, as in a table-driven test, gives null.
   */
  private fun subTestName(element: PsiElement): String? {
    if (element !is GoFunctionLit) return null

    // An argument of the call, never a closure nested deeper.
    val call = (element.parent as? GoArgumentList)?.parent as? GoCallExpr ?: return null

    val function = call.parentOfType<GoFunctionOrMethodDeclaration>() ?: return null
    return GoRunUtil.getSubTests(function)[call]
  }
}
