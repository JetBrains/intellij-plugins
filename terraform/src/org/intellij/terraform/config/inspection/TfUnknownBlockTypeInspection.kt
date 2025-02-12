// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInspection.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.parentOfTypes
import org.intellij.terraform.config.actions.TfInitAction
import org.intellij.terraform.config.codeinsight.TfCompletionUtil
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.*
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
          holder.registerProblem(block.nameElements.first(),
                                 HCLBundle.message("unknown.block.type.inspection.missing.closing.brace.error.message"),
                                 ProblemHighlightType.GENERIC_ERROR, AddClosingBraceFix(block.nameElements.first()))
          return
        }
        registerUnknownBlockProblem(block, holder, type)
      }
      else -> return
    }
  }

  private fun registerUnknownBlockProblem(block: HCLBlock, holder: ProblemsHolder, type: String) {
    holder.registerProblem(block.nameElements.first(),
                           HCLBundle.message("unknown.block.type.inspection.unknown.block.type.error.message", type),
                           ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                           *listOfNotNull(TfInitAction.createQuickFixNotInitialized(block), RemoveBlockQuickFix(block)).toTypedArray())
  }
}

class AddClosingBraceFix(before: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(before) {
  override fun getText(): String {
    return HCLBundle.message("unknown.block.type.inspection.add.closing.brace.quick.fix.test")
  }

  override fun getFamilyName(): String {
    return text
  }

  override fun startInWriteAction(): Boolean {
    return false
  }

  override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
    if (!FileModificationService.getInstance().prepareFileForWrite(file)) return
    WriteCommandAction.writeCommandAction(project).run<Throwable> {
      CodeStyleManager.getInstance(project).performActionWithFormatterDisabled {
        if (editor != null) {
          editor.document.insertString(startElement.node.startOffset, "}\n")
        }
        else {
          startElement.parent.addBefore(HCLElementGenerator(project).createObject("").lastChild, startElement)
          startElement.parent.addBefore(PsiParserFacade.getInstance(project).createWhiteSpaceFromText("\n"), startElement)
          file.subtreeChanged()
        }
      }
    }
  }
}

internal class RemoveBlockQuickFix(element: HCLBlock) : LocalQuickFixOnPsiElement(element) {
  override fun getText(): String = HCLBundle.message("unknown.block.type.inspection.quick.fix.name")
  override fun getFamilyName(): String = text
  override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
    startElement.delete()
  }
}