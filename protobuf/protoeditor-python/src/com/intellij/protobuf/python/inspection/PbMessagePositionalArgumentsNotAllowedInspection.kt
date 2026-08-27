package com.intellij.protobuf.python.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.protobuf.python.PbPythonBundle
import com.intellij.protobuf.python.inspection.quickfix.ConvertToKeywordArgumentsQuickFix
import com.intellij.protobuf.python.types.PbPythonMessageType
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyKeywordArgument
import com.jetbrains.python.psi.PyStarArgument

internal class PbMessagePositionalArgumentsNotAllowedInspection : LocalInspectionTool() {
  override fun buildVisitor(
    holder: ProblemsHolder,
    isOnTheFly: Boolean,
    session: LocalInspectionToolSession,
  ): PsiElementVisitor =
    object : PyInspectionVisitor(holder, getContext(session)) {
      override fun visitPyCallExpression(element: PyCallExpression) {
        super.visitPyCallExpression(element)

        val callee = element.callee ?: return
        val messageType = myTypeEvalContext.getType(callee) as? PbPythonMessageType ?: return
        if (!messageType.isDefinition) return

        val arguments = element.argumentList?.arguments ?: return
        // `*args` can be empty and `**kwargs` passes keywords, so the inspection skips both
        val positionalArguments = arguments.filter { it !is PyKeywordArgument && it !is PyStarArgument }
        if (positionalArguments.isEmpty()) return

        val quickFix = createQuickFix(messageType, arguments.filterIsInstance<PyKeywordArgument>(), positionalArguments.size)
        for (argument in positionalArguments) {
          registerProblem(
            argument,
            PbPythonBundle.message("inspection.message.positional.argument.problem"),
            *listOfNotNull(quickFix).toTypedArray()
          )
        }
      }

      /**
       * Maps the positional arguments to the message fields in declaration order.
       * Returns `null` when a field is missing or is already passed as a keyword argument.
       */
      private fun createQuickFix(
        messageType: PbPythonMessageType,
        keywordArguments: List<PyKeywordArgument>,
        positionalCount: Int,
      ): LocalQuickFix? {
        val parameters = messageType.getParameters(myTypeEvalContext) ?: return null
        if (parameters.size < positionalCount) return null

        val names = parameters.take(positionalCount).map { it.name ?: return null }
        val usedNames = keywordArguments.mapNotNullTo(HashSet()) { it.keyword }
        if (names.any { it in usedNames }) return null

        return ConvertToKeywordArgumentsQuickFix(names)
      }
    }
}
