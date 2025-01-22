package org.angular2.lang.expr.service.tcb

import com.intellij.lang.javascript.psi.*
import com.intellij.psi.PsiElement
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2Interpolation
import org.angular2.lang.expr.psi.Angular2PipeExpression

const val ANY_EXPRESSION: String = "0 as any"

/**
 * Checks whether View Engine will infer a type of 'any' for the left-hand side of a safe navigation
 * operation.
 *
 * In View Engine's template type-checker, certain receivers of safe navigation operations will
 * cause a temporary variable to be allocated as part of the checking expression, to save the value
 * of the receiver and use it more than once in the expression. This temporary variable has type
 * 'any'. In practice, this means certain receivers cause View Engine to not check the full
 * expression, and other receivers will receive more complete checking.
 *
 * For compatibility, this logic is adapted from View Engine's expression_converter.ts so that the
 * Ivy checker can emulate this bug when needed.
 */
internal class VeSafeLhsInferenceBugDetector : Angular2ElementVisitor() {

  private var result: Boolean = false

  fun veWillInferAnyFor(ast: PsiElement): Boolean {
    ast.accept(this)
    return result
  }

  override fun visitJSPrefixExpression(node: JSPrefixExpression) {
    node.expression?.accept(this)
  }

  override fun visitJSBinaryExpression(node: JSBinaryExpression) {
    if (result) return
    node.lOperand?.accept(this)
    if (result) return
    node.rOperand?.accept(this)
  }

  override fun visitJSConditionalExpression(node: JSConditionalExpression) {
    if (result) return
    node.condition?.accept(this)
    if (result) return
    node.thenBranch?.accept(this)
    if (result) return
    node.elseBranch?.accept(this)

  }

  override fun visitJSCallExpression(node: JSCallExpression) {
    result = true
  }

  override fun visitAngular2Interpolation(interpolation: Angular2Interpolation) {
    interpolation.expression?.accept(this)
  }

  override fun visitJSArrayLiteralExpression(node: JSArrayLiteralExpression) {
    result = true
  }

  override fun visitJSObjectLiteralExpression(node: JSObjectLiteralExpression) {
    result = true
  }

  override fun visitAngular2PipeExpression(pipe: Angular2PipeExpression) {
    result = true
  }

  override fun visitJSPostfixExpression(node: JSPostfixExpression) {
    node.expression?.accept(this)
  }
}
