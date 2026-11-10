package org.jetbrains.qodana.python.community.psi

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyCaseClause
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyComprehensionElement
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyLambdaExpression
import com.jetbrains.python.psi.PySequenceExpression
import com.jetbrains.python.psi.PyStatementList
import com.jetbrains.python.psi.PyTargetExpression
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.QodanaPsiStructuralFingerprintsProvider

class PythonPsiStructuralFingerprintsProvider : QodanaPsiStructuralFingerprintsProvider {
  override fun isCodeBlock(element: PsiElement): Boolean = element is PyStatementList

  /** [PySequenceExpression] is a list, a dict, a set or a tuple. A comprehension holds elements too. */
  override fun isCollectionLiteral(element: PsiElement): Boolean =
    element is PySequenceExpression || element is PyComprehensionElement

  override fun isCaseClause(element: PsiElement): Boolean = element is PyCaseClause
  override fun isClassDeclaration(element: PsiElement): Boolean = element is PyClass

  /** A `def`, an `async def`, a method and a lambda. A lambda with no name stays unnamed, never hidden. */
  override fun isFunctionDeclaration(element: PsiElement): Boolean =
    element is PyFunction || element is PyLambdaExpression

  /**
   * The name that `pick = lambda: 1` binds. The core walk reaches it from the lambda.
   *
   * A statement with several targets gives no name, because it cannot say which target holds the lambda.
   */
  override fun declaredName(element: PsiElement): String? =
    ((element as? PyAssignmentStatement)?.targets?.singleOrNull() as? PyTargetExpression)?.name
}
