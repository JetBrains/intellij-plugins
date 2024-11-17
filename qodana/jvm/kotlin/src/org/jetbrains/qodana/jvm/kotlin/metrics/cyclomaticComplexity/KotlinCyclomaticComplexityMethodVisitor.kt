package org.jetbrains.qodana.jvm.kotlin.metrics.cyclomaticComplexity

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*

class KotlinCyclomaticComplexityMethodVisitor : KtTreeVisitorVoid() {
  var cyclomaticComplexityValue: Int = 1
    private set

  override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
    // do nothing, override not to traverse further
  }

  override fun visitAnonymousInitializer(initializer: KtAnonymousInitializer) {
    // do nothing, override not to traverse further
  }

  override fun visitNamedFunction(function: KtNamedFunction) {
    // do nothing, override not to traverse further
  }

  override fun visitIfExpression(expression: KtIfExpression) {
    super.visitIfExpression(expression)
    cyclomaticComplexityValue += 1
  }

  override fun visitWhileExpression(expression: KtWhileExpression) {
    super.visitWhileExpression(expression)
    cyclomaticComplexityValue += 1
  }

  override fun visitDoWhileExpression(expression: KtDoWhileExpression) {
    super.visitDoWhileExpression(expression)
    cyclomaticComplexityValue += 1
  }

  override fun visitForExpression(expression: KtForExpression) {
    super.visitForExpression(expression)
    cyclomaticComplexityValue += 1
  }

  override fun visitWhenExpression(expression: KtWhenExpression) {
    super.visitWhenExpression(expression)
    cyclomaticComplexityValue += expression.entries.size
  }

  override fun visitCatchSection(catchClause: KtCatchClause) {
    super.visitCatchSection(catchClause)
    cyclomaticComplexityValue += 1
  }

  override fun visitSafeQualifiedExpression(expression: KtSafeQualifiedExpression) {
    visitExpr(expression)
  }

  override fun visitBinaryExpression(expression: KtBinaryExpression) {
    visitExpr(expression)
  }

  override fun visitBinaryWithTypeRHSExpression(expression: KtBinaryExpressionWithTypeRHS) {
    visitExpr(expression)
  }

  private fun visitExpr(expression: KtExpression) {
    val binaryExpressionVisitor = RecursiveExpressionVisitor { element ->
      val elementType = element.elementType
      if (elementType == KtTokens.OROR || elementType == KtTokens.ANDAND) {
        cyclomaticComplexityValue += 1
      }
    }
    expression.accept(binaryExpressionVisitor)
  }

  private inner class RecursiveExpressionVisitor(
    private val onVisitElement: (PsiElement) -> Unit,
  ) : KtTreeVisitorVoid() {
    override fun visitElement(element: PsiElement) {
      super.visitElement(element)
      onVisitElement(element)
    }
  }
}