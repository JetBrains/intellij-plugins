// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElementVisitor
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.getType
import org.intellij.terraform.hil.GoUtil
import org.intellij.terraform.hil.HILElementTypes
import org.intellij.terraform.hil.HILElementTypes.IL_BINARY_EQUALITY_EXPRESSION
import org.intellij.terraform.hil.HILTypes.ILBinaryBooleanOnlyOperations
import org.intellij.terraform.hil.HILTypes.ILBinaryNumericOnlyOperations
import org.intellij.terraform.hil.psi.*
import org.intellij.terraform.hil.psi.impl.getHCLHost

class HILOperationTypesMismatchInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = InjectedLanguageManager.getInstance(holder.project).getTopLevelFile(holder.file)
    val ft = file.fileType
    if (ft != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }
    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : ILElementVisitor() {
    override fun visitILBinaryExpression(operation: ILBinaryExpression) {
      ProgressIndicatorProvider.checkCanceled()
      operation.getHCLHost() ?: return

      val left = operation.leftOperand
      val right = operation.rightOperand ?: return

      val leftType = left.getType()
      val rightType = right.getType()

      val elementType = operation.node.elementType
      if (elementType in ILBinaryNumericOnlyOperations) {
        if (leftType != null && leftType != Types.Number && leftType != Types.Any) {
          holder.registerProblem(left, HCLBundle.message("hil.operation.types.mismatch.inspection.number.expected.error.message", leftType.presentableText), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        }
        if (rightType != null && rightType != Types.Number && rightType != Types.Any) {
          holder.registerProblem(right, HCLBundle.message("hil.operation.types.mismatch.inspection.number.expected.error.message", rightType.presentableText), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        }
        return
      } else if (elementType == IL_BINARY_EQUALITY_EXPRESSION) {
        return // could compare anything with implicit 'toString' conversion. TODO: Add warning?
      } else if (elementType in ILBinaryBooleanOnlyOperations) {
        if (leftType != null && leftType != Types.Boolean && leftType != Types.Any) {
          holder.registerProblem(left, HCLBundle.message("hil.operation.types.mismatch.inspection.boolean.expected.error.message", leftType.presentableText), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        }
        if (rightType != null && rightType != Types.Boolean && rightType != Types.Any) {
          holder.registerProblem(right, HCLBundle.message("hil.operation.types.mismatch.inspection.boolean.expected.error.message", rightType.presentableText), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        }
        return
      } else {
        return
      }
    }

    override fun visitILUnaryExpression(operation: ILUnaryExpression) {
      ProgressIndicatorProvider.checkCanceled()
      operation.getHCLHost() ?: return

      val operand = operation.operand ?: return
      val sign = operation.operationSign

      // Return if we cannot determine operands type
      val type = operand.getType() ?: return


      if (sign == HILElementTypes.OP_PLUS || sign == HILElementTypes.OP_MINUS) {
        if (type != Types.Number && type != Types.Any) {
          holder.registerProblem(operand, HCLBundle.message("hil.operation.types.mismatch.inspection.number.expected.error.message", type.presentableText), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        }
        return
      } else if (sign == HILElementTypes.OP_NOT) {
        if (type != Types.Boolean && type != Types.Any) {
          holder.registerProblem(operand, HCLBundle.message("hil.operation.types.mismatch.inspection.boolean.expected.error.message", type.presentableText), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
        }
        return
      } else {
        return
      }
    }

    override fun visitILConditionalExpression(operation: ILConditionalExpression) {
      ProgressIndicatorProvider.checkCanceled()
      operation.getHCLHost() ?: return

      // First check condition
      val condition = operation.condition
      val type = condition.getType()
      if (type == Types.Boolean) {
        // Ok
      } else if (type == Types.String) {
        // Semi ok
        if (condition is ILLiteralExpression && condition.doubleQuotedString != null) {
          if (!GoUtil.isBoolean(condition.unquotedText)) {
            holder.registerProblem(condition,
                                   HCLBundle.message("hil.operation.types.mismatch.inspection.boolean.or.string.expected.error.message"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
          }
        }
      } else if (type != Types.Any) {
        holder.registerProblem(condition,
                               HCLBundle.message("hil.operation.types.mismatch.inspection.boolean.or.string.expected.error.message"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
      }

      ProgressIndicatorProvider.checkCanceled()

      // Then branches
      val left = operation.then ?: return
      val right = operation.otherwise ?: return

      // See TypeCachedValueProvider.doGetType(ILConditionalExpression) for details
      val l = left.getType()
      val r = right.getType()

      ProgressIndicatorProvider.checkCanceled()

      // There's some weird logic in HIL eval_test.go:
      // > // false expression is type-converted to match true expression
      // > // true expression is type-converted to match false expression if the true expression is string
      if (l == r) // Ok
      else if (l == null) // Ok
      else if (r == null) // Ok
      else if (l == Types.Any || r == Types.Any) // Ok
      else if (l == Types.String) // Ok // Would be casted //TODO Check actual value
      else if (r == Types.String) // Ok // Would be casted //TODO Check actual value
      else if (l != r) {
        holder.registerProblem(operation,
                               HCLBundle.message("hil.operation.types.mismatch.inspection.both.branches.must.have.same.type.error.message", l.presentableText, r.presentableText), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
      }

      // TODO: Report if some branch has type Array or Map, they're forbidden for now
    }
  }

}