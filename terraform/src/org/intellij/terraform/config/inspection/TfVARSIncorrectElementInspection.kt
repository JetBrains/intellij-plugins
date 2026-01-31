// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModCommandAction
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.model.getType
import org.intellij.terraform.config.model.isListType
import org.intellij.terraform.config.model.isObjectType
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLArray
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLBooleanLiteral
import org.intellij.terraform.hcl.psi.HCLElementGenerator
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLNullLiteral
import org.intellij.terraform.hcl.psi.HCLNumberLiteral
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.HCLValue
import org.intellij.terraform.hcl.psi.impl.HCLStringLiteralMixin
import org.intellij.terraform.isTfOrTofuPsiFile
import org.jetbrains.annotations.Nls

class TfVARSIncorrectElementInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTfOrTofuPsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    if (!TfPsiPatterns.TerraformVariablesFile.accepts(file)) {
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
            holder.problem(value, HCLBundle.message("tfvars.unsupported.element.inspection.illegal.value.type.error.message"))
              .maybeFix(getQuoteFix(value))
              .register()
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

  private fun getQuoteFix(element: HCLValue): ModCommandAction? {
    if (element is HCLStringLiteralMixin || element is HCLIdentifier || element is HCLNullLiteral || element is HCLBooleanLiteral) {
      return ConvertToHCLStringQuickFix(element)
    }
    return null
  }

  class ConvertToHCLStringQuickFix(element: PsiElement) : PsiUpdateModCommandAction<PsiElement>(element) {
    override fun getFamilyName(): @Nls String = HCLBundle.message("tfvars.unsupported.element.inspection.convert.to.double.quoted.string.quick.fix.name")
    override fun invoke(context: ActionContext, element: PsiElement, updater: ModPsiUpdater) {
      val text: String
      when (element) {
        is HCLIdentifier, is HCLNullLiteral, is HCLBooleanLiteral -> text = element.text
        is HCLStringLiteral -> text = element.value
        else -> return
      }
      element.replace(HCLElementGenerator(context.project).createStringLiteral(text))
    }
  }
}
