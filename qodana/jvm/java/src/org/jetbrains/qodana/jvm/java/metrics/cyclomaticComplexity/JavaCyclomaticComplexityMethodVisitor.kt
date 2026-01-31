package org.jetbrains.qodana.jvm.java.metrics.cyclomaticComplexity

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiCatchSection
import com.intellij.psi.PsiConditionalExpression
import com.intellij.psi.PsiDoWhileStatement
import com.intellij.psi.PsiForStatement
import com.intellij.psi.PsiForeachStatement
import com.intellij.psi.PsiIfStatement
import com.intellij.psi.PsiLambdaExpression
import com.intellij.psi.PsiPolyadicExpression
import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiSwitchBlock
import com.intellij.psi.PsiSwitchExpression
import com.intellij.psi.PsiSwitchLabelStatement
import com.intellij.psi.PsiSwitchLabeledRuleStatement
import com.intellij.psi.PsiSwitchStatement
import com.intellij.psi.PsiWhileStatement

class JavaCyclomaticComplexityMethodVisitor : JavaRecursiveElementWalkingVisitor() {
  var cyclomaticComplexityValue: Int = 1
    private set

  override fun visitAnonymousClass(aClass: PsiAnonymousClass) {
    // do nothing, override not to traverse further
  }

  override fun visitLambdaExpression(expression: PsiLambdaExpression) {
    // do nothing, override not to traverse further
  }

  override fun visitIfStatement(statement: PsiIfStatement) {
    super.visitIfStatement(statement)
    cyclomaticComplexityValue += 1
  }

  override fun visitConditionalExpression(expression: PsiConditionalExpression) {
    super.visitConditionalExpression(expression)
    cyclomaticComplexityValue += 1
  }

  override fun visitWhileStatement(statement: PsiWhileStatement) {
    super.visitWhileStatement(statement)
    cyclomaticComplexityValue += 1
  }

  override fun visitDoWhileStatement(statement: PsiDoWhileStatement) {
    super.visitDoWhileStatement(statement)
    cyclomaticComplexityValue += 1
  }

  override fun visitForStatement(statement: PsiForStatement) {
    super.visitForStatement(statement)
    cyclomaticComplexityValue += 1
  }

  override fun visitForeachStatement(statement: PsiForeachStatement) {
    super.visitForeachStatement(statement)
    cyclomaticComplexityValue += 1
  }

  override fun visitSwitchExpression(expression: PsiSwitchExpression) {
    super.visitSwitchExpression(expression)
    visitSwitchBlock(expression)
  }

  override fun visitSwitchStatement(statement: PsiSwitchStatement) {
    super.visitSwitchStatement(statement)
    visitSwitchBlock(statement)
  }

  private fun visitSwitchBlock(statement: PsiSwitchBlock) {
    val body = statement.body
    if (body == null) return
    val statements: Array<PsiStatement> = body.statements
    var numberOfCaseStatements = 0
    var hasDefault = false
    statements.forEach { s ->
      if (s is PsiSwitchLabelStatement || s is PsiSwitchLabeledRuleStatement) {
        val isDefault: Boolean = when (s) {
          is PsiSwitchLabelStatement -> s.isDefaultCase
          is PsiSwitchLabeledRuleStatement -> s.isDefaultCase
          else -> false
        }
        if (!isDefault) {
          numberOfCaseStatements++
        }
        else {
          hasDefault = true
        }
      }
    }
    if (!hasDefault) {
      numberOfCaseStatements--
    }
    cyclomaticComplexityValue += numberOfCaseStatements
  }

  override fun visitCatchSection(section: PsiCatchSection) {
    super.visitCatchSection(section)
    cyclomaticComplexityValue += 1
  }

  override fun visitPolyadicExpression(expression: PsiPolyadicExpression) {
    super.visitPolyadicExpression(expression)
    val token = expression.operationTokenType
    if (token == JavaTokenType.ANDAND || token == JavaTokenType.OROR) {
      cyclomaticComplexityValue += expression.operands.size - 1
    }
  }
}