// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.inspection

import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInspection.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.DocumentUtil
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLExpression
import org.intellij.terraform.hcl.psi.HCLMethodCallExpression
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.config.psi.TerraformElementGenerator

class HCLSimplifyExpressionInspection : LocalInspectionTool(), CleanupLocalInspectionTool {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val file = holder.file
    if (file.language !in listOf(HCLLanguage, TerraformLanguage)) return PsiElementVisitor.EMPTY_VISITOR
    if (file.fileType != TerraformFileType) return PsiElementVisitor.EMPTY_VISITOR

    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitMethodCallExpression(o: HCLMethodCallExpression) {
      val method = o.method
      val args = o.parameterList.elements

      // 'element(list, index)' -> 'list[index]'
      if (args.size == 2 && method.id == "element") {
        if (holder.isOnTheFly) {
          holder.registerProblem(o, HCLBundle.message("hcl.simplify.expression.inspection.could.be.replaced.with.list.indexing.message"), ProblemHighlightType.INFORMATION, ReplaceWithListIndexing(o))
        }
      }
    }
  }

  class ReplaceWithListIndexing(e: HCLMethodCallExpression) : LocalQuickFixAndIntentionActionOnPsiElement(e), BatchQuickFix {
    override fun getText() = HCLBundle.message("hcl.simplify.expression.inspection.replace.with.list.indexing.quick.fix.text")
    override fun getFamilyName() = HCLBundle.message("hcl.simplify.expression.inspection.replace.with.list.indexing.quick.fix.family.name")

    override fun startInWriteAction() = false

    override fun isAvailable(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Boolean {
      if (file.language !in listOf(HCLLanguage, TerraformLanguage)) return false
      return super.isAvailable(project, file, startElement, endElement)
    }

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
      if (!FileModificationService.getInstance().prepareFileForWrite(file)) return
      val call = startElement as? HCLMethodCallExpression ?: return
      val method = call.method
      if (method.id != "element" || call.parameterList.elements.size != 2) return

      val replacement = getReplacementValue(project, call)

      WriteCommandAction.writeCommandAction(project).withName(text).withGroupId(familyName).run<Throwable> {
        replace(project, file, call, replacement)
      }
    }

    override fun applyFix(project: Project, descriptors: Array<out CommonProblemDescriptor>, psiElementsToIgnore: MutableList<PsiElement>, refreshViews: Runnable?) {
      val targets = ArrayList<HCLMethodCallExpression>()
      for (descriptor in descriptors) {
        descriptor.fixes?.filterIsInstance(ReplaceWithListIndexing::class.java)?.map { it.startElement }?.filterIsInstanceTo(targets, HCLMethodCallExpression::class.java)
      }

      if (!FileModificationService.getInstance().preparePsiElementsForWrite(targets)) return

      DocumentUtil.writeInRunUndoTransparentAction {
        targets.forEach {
          val replacement = getReplacementValue(project, it)
          replace(project, it.containingFile, it, replacement)
        }
      }
      psiElementsToIgnore.addAll(targets)
      refreshViews?.run()
    }

    companion object {
      private fun getReplacementValue(project: Project, call: HCLMethodCallExpression): HCLExpression {
        val args = call.parameterList.elements
        val list = args[0]
        val index = args[1]
        return TerraformElementGenerator(project).createValue(list.text + '[' + index.text + ']')
      }

      private fun replace(project: Project, file: PsiFile, element: HCLExpression, replacement: HCLExpression) {
        CodeStyleManager.getInstance(project).performActionWithFormatterDisabled {
          element.replace(replacement)
          file.subtreeChanged()
        }
      }
    }
  }
}
