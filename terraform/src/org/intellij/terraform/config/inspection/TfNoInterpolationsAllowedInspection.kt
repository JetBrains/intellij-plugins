// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.InjectedLanguagePlaces
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.patterns.TfPsiPatterns.DependsOnPattern
import org.intellij.terraform.config.patterns.TfPsiPatterns.HeredocContentAnywhereInVariable
import org.intellij.terraform.config.patterns.TfPsiPatterns.ModuleRootBlock
import org.intellij.terraform.config.patterns.TfPsiPatterns.StringLiteralAnywhereInVariable
import org.intellij.terraform.config.patterns.TfPsiPatterns.TerraformRootBlock
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.MethodCallExpression
import org.intellij.terraform.hil.ILLanguageInjector
import org.intellij.terraform.isTerraformFile

class TfNoInterpolationsAllowedInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerraformFile(file) && TfPsiPatterns.TerraformConfigFile.accepts(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      if (ModuleRootBlock.accepts(block)) {
        checkModule(block)
      } else if (TerraformRootBlock.accepts(block)) {
        checkTerraform(block)
      }
    }

    override fun visitStringLiteral(o: HCLStringLiteral) {
      if (StringLiteralAnywhereInVariable.accepts(o)) {
        checkForVariableInterpolations(o)
      }
    }

    override fun visitHeredocContent(o: HCLHeredocContent) {
      if (HeredocContentAnywhereInVariable.accepts(o)) {
        checkForVariableInterpolations(o)
      }
    }

    override fun visitProperty(o: HCLProperty) {
      if (DependsOnPattern.accepts(o)) {
        checkDependsOn(o)
      }
    }

    private fun checkModule(block: HCLBlock) {
      // Ensure there's no interpolation in module 'source' string
      val source = block.`object`?.findProperty("source")?.value
      if (source != null) {
        if (source is HCLStringLiteral) {
          reportRanges(source, "module source")
        } else {
          holder.registerProblem(source, HCLBundle.message("illegal.interpolations.inspection.no.double.quotes.error.message"))
        }
      }
    }

    private fun checkTerraform(block: HCLBlock) {
      // Ensure there's no interpolation in all string properties
      (block.`object`?.propertyList ?: return)
          .map { it.value }
          .filterIsInstance<HCLStringLiteral>()
          .forEach { reportRanges(it, "properties inside 'terraform' block") }
    }

    private fun checkForVariableInterpolations(o: HCLStringLiteral) {
      reportRanges(o, "variables")
    }

    private fun checkForVariableInterpolations(o: HCLHeredocContent) {
      val ranges = ArrayList<TextRange>()
      ILLanguageInjector.getHCLHeredocContentInjections(o, getInjectedLanguagePlacesCollector(ranges))
      for (range in ranges) {
        holder.registerProblem(o, HCLBundle.message("illegal.interpolations.inspection.in.variable.error.message"),
                               ProblemHighlightType.ERROR, range)
      }
    }

    private fun checkDependsOn(o: HCLProperty) {
      val value = o.value as? HCLArray ?: return
      val list = value.elements
      for (e in list) {
        if (e is HCLStringLiteral) {
          reportRanges(e, "depends_on")
        } else if (e is MethodCallExpression<*>) {
          holder.registerProblem(e, HCLBundle.message("illegal.interpolations.inspection.no.function.calls.error.message"),
                                 ProblemHighlightType.ERROR)
        }
      }
    }

    private fun reportRanges(e: HCLStringLiteral, where: String) {
      val ranges = ArrayList<TextRange>()
      ILLanguageInjector.getStringLiteralInjections(e, getInjectedLanguagePlacesCollector(ranges))
      for (range in ranges) {
        holder.registerProblem(e, HCLBundle.message("illegal.interpolations.inspection.depends.on.error.message", where),
                               ProblemHighlightType.ERROR, range)
      }
    }

    private fun getInjectedLanguagePlacesCollector(ranges: ArrayList<TextRange>) =
        InjectedLanguagePlaces { _, rangeInsideHost, _, _ -> ranges.add(rangeInsideHost) }
  }
}

