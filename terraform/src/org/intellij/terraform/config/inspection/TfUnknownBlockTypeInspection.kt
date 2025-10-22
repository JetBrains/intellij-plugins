// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfTypes
import org.intellij.terraform.config.actions.createQuickFixNotInitialized
import org.intellij.terraform.config.codeinsight.TfCompletionUtil
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.isTerraformCompatiblePsiFile

class TfUnknownBlockTypeInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerraformCompatiblePsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return TfBlockVisitor(holder)
  }

  inner class TfBlockVisitor(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      ProgressIndicatorProvider.checkCanceled()
      val type = block.getNameElementUnquoted(0)
      if (!type.isNullOrEmpty()) {
        doCheck(block, holder, type)
      }
    }
  }

  private fun doCheck(block: HCLBlock, holder: ProblemsHolder, type: String) {
    // It could be a root block or block inside Object
    // Object could be the value of some property or right part of another object
    val parent = block.parentOfTypes(HCLBlock::class, HCLFile::class) ?: return
    when (parent) {
      is HCLFile -> {
        if (TfCompletionUtil.RootBlockKeywords.contains(type))
          return

        registerUnknownBlockProblem(block, holder, type)
      }
      is HCLBlock -> {
        parent.getNameElementUnquoted(0) ?: return
        parent.`object` ?: return
        if (TfPsiPatterns.DynamicBlock.accepts(block) || TfPsiPatterns.DynamicBlockContent.accepts(block))
          return

        val properties = TfModelHelper.getBlockProperties(parent)
        if (properties[type] is BlockType) return

        // Check for non-closed root block (issue #93)
        if (TfPsiPatterns.RootBlock.accepts(parent) && TfCompletionUtil.RootBlockKeywords.contains(type)) {
          holder.problem(block.nameElements.first(),
                         HCLBundle.message("unknown.block.type.inspection.missing.closing.brace.error.message"))
            .highlight(ProblemHighlightType.GENERIC_ERROR)
            .fix(AddClosingBraceFix(block.nameElements.first()))
            .register()
          return
        }
        registerUnknownBlockProblem(block, holder, type)
      }
      else -> return
    }
  }

  private fun registerUnknownBlockProblem(block: HCLBlock, holder: ProblemsHolder, type: String) {
    holder.problem(block.nameElements.first(), HCLBundle.message("unknown.block.type.inspection.unknown.block.type.error.message", type))
      .maybeFix(createQuickFixNotInitialized(block))
      .fix(RemoveBlockQuickFix(block))
      .register()
  }
}

class AddClosingBraceFix(before: PsiElement) : PsiUpdateModCommandAction<PsiElement>(before) {
  override fun getFamilyName(): String = HCLBundle.message("unknown.block.type.inspection.add.closing.brace.quick.fix.test")

  override fun invoke(context: ActionContext, element: PsiElement, updater: ModPsiUpdater) {
    element.containingFile.fileDocument.insertString(element.node.startOffset, "}\n")
  }
}

internal class RemoveBlockQuickFix(element: HCLBlock) : PsiUpdateModCommandAction<HCLBlock>(element) {
  override fun getFamilyName(): String = HCLBundle.message("unknown.block.type.inspection.quick.fix.name")
  override fun invoke(context: ActionContext, element: HCLBlock, updater: ModPsiUpdater) {
    element.delete()
  }
}