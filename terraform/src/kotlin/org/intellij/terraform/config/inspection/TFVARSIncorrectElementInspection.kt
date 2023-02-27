// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.impl.HCLStringLiteralMixin
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.jetbrains.annotations.Nls

class TFVARSIncorrectElementInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    if (!TerraformPatterns.TerraformVariablesFile.accepts(file)) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return MyEV(holder)
  }


  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      ProgressIndicatorProvider.checkCanceled()
      if (block.parent !is HCLFile) return
      holder.registerProblem(block, HCLBundle.message("tfvars.unsupported.element.inspection.only.key.values.allowed.error.message"),
                             ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
    }

    override fun visitProperty(property: HCLProperty) {
      // TODO: Support expressions
      val value = property.value as? HCLValue ?: return
      if (property.parent is HCLFile) {
        if (value !is HCLNumberLiteral && value !is HCLObject && value !is HCLArray) {
          if (value is HCLStringLiteral && value.quoteSymbol != '"') {
            holder.registerProblem(value, HCLBundle.message("tfvars.unsupported.element.inspection.illegal.value.type.error.message"),
                                   *getQuoteFix(value))
          }
        }
        if (property.nameElement is HCLStringLiteral) {
          holder.registerProblem(property.nameElement,
                                 HCLBundle.message("tfvars.unsupported.element.inspection.no.quotes.in.argument.name.error.message"),
                                 ProblemHighlightType.ERROR)
        }
        val vName = property.name.substringBefore('.')
        val variables = property.getTerraformModule().findVariables(vName)
        if (variables.isEmpty()) {
          // TODO: Add 'Define variable' quick fix.
          holder.registerProblem(property.nameElement,
                                 HCLBundle.message("tfvars.unsupported.element.inspection.undefined.variable.error.message", vName),
                                 ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
        } else if (!property.name.contains('.')) {
          // TODO: Support Terraform 0.12
          val expected = (variables.first().getTypeExpression() as? HCLValue)?.name
          val actual = value.getType()
          if ((expected == "string" && actual !in Types.SimpleValueTypes)
              || (expected == "list" && !isListType(actual))
              || (expected == "map" && !isObjectType(actual))) {
            @Nls val e = if (expected == "string") HCLBundle.message(
              "tfvars.unsupported.element.inspection.incorrect.variable.type.error.message.string.expected")
            else "'$expected'"
            holder.registerProblem(value,
                                   HCLBundle.message("tfvars.unsupported.element.inspection.incorrect.variable.type.error.message", e))
          }
        }
      }
    }
  }

  private fun getQuoteFix(element: HCLValue): Array<LocalQuickFix> {
    if (element is HCLStringLiteralMixin || element is HCLIdentifier || element is HCLNullLiteral || element is HCLBooleanLiteral) {
      return arrayOf(ConvertToHCLStringQuickFix(element))
    }
    return emptyArray()
  }

  class ConvertToHCLStringQuickFix(element: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
    override fun getText() = HCLBundle.message("tfvars.unsupported.element.inspection.convert.to.double.quoted.string.quick.fix.name")
    override fun getFamilyName() = text
    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
      val element = startElement
      val text: String
      if (element is HCLIdentifier || element is HCLNullLiteral || element is HCLBooleanLiteral) {
        text = element.text
      } else if (element is HCLStringLiteral) {
        text = element.value
      } else return
      element.replace(HCLElementGenerator(project).createStringLiteral(text))
    }
  }
}
