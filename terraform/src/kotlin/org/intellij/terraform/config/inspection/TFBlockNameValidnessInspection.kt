// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInspection.*
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ThrowableRunnable
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.codeinsight.InsertHandlersUtil
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.config.refactoring.TerraformElementRenameValidator

class TFBlockNameValidnessInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    val ft = holder.file.fileType
    if (ft != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return MyEV(holder)
  }

  override fun getID(): String {
    return "BlockNameValidness"
  }

  override fun getBatchSuppressActions(element: PsiElement?): Array<SuppressQuickFix> {
    return super.getBatchSuppressActions(PsiTreeUtil.getParentOfType(element, HCLBlock::class.java, false))
  }

  inner class MyEV(private val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitStringLiteral(o: HCLStringLiteral) {
      val parent = o.parent as? HCLBlock ?: return
      if (parent.nameIdentifier !== o) return
      if (StringUtil.isEmptyOrSpaces(o.value)) {
        holder.registerProblem(o, HCLBundle.message("block.name.validness.inspection.block.name.should.not.be.empty.error.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
      }
    }

    override fun visitBlock(o: HCLBlock) {
      if (!TerraformPatterns.TerraformConfigFile.accepts(o.containingFile)) return
      val validator = TerraformElementRenameValidator()
      val identifier = o.nameIdentifier
      if (identifier != null && validator.pattern.accepts(o)) {
        if (!validator.isInputValid(o.name, o)) {
          holder.registerProblem(identifier, HCLBundle.message("block.name.validness.inspection.invalid.name.error.message"),
                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING, RenameBlockFix)
        }
      }

      val type = ModelHelper.getAbstractBlockType(o) ?: return
      val nameElements = o.nameElements

      val required = (type.args + 1) - nameElements.size
      if (required == 0) return
      if (required > 0) {
        val range = TextRange(nameElements.first().startOffsetInParent, nameElements.last().let { it.startOffsetInParent + it.textLength })
        holder.registerProblem(o, HCLBundle.message("block.name.validness.inspection.missing.block.name.error.message", required),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING, range, AddNameElementsQuickFix(o))
      } else {
        val extra = nameElements.toList().takeLast(-1 * required)
        val range = TextRange(extra.first().startOffsetInParent, extra.last().let { it.startOffsetInParent + it.textLength })
        holder.registerProblem(o, HCLBundle.message("block.name.validness.inspection.extra.block.name.error.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING, range, RemoveExtraNameElementsQuickFix(o))
      }
    }
  }

  class AddNameElementsQuickFix(element: HCLBlock) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
    override fun getText(): String = HCLBundle.message("block.name.validness.inspection.add.name.quick.fix.name")
    override fun getFamilyName(): String = text
    override fun startInWriteAction(): Boolean = false

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
      val block = startElement as? HCLBlock ?: return
      if (editor == null) return
      if (!TerraformPatterns.TerraformConfigFile.accepts(block.containingFile)) return
      val type = ModelHelper.getAbstractBlockType(block) ?: return
      val nameElements = block.nameElements
      val required = (type.args + 1) - nameElements.size
      if (required <= 0) return

      WriteAction.run(ThrowableRunnable {
        val offset = nameElements.last().let { it.textOffset + it.textLength }
        editor.caretModel.moveToOffset(offset)
        InsertHandlersUtil.addArguments(required, editor)
        editor.caretModel.moveToOffset(offset + required * 3 - 1)
      })
      CodeCompletionHandlerBase.createHandler(CompletionType.BASIC).invokeCompletion(project, editor)
    }
  }

  class RemoveExtraNameElementsQuickFix(element: HCLBlock) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
    override fun getText(): String = HCLBundle.message("block.name.validness.inspection.remove.name.quick.fix.name")
    override fun getFamilyName(): String = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
      val block = startElement as? HCLBlock ?: return
      val obj = block.`object` ?: return
      if (!TerraformPatterns.TerraformConfigFile.accepts(block.containingFile)) return
      val type = ModelHelper.getAbstractBlockType(block) ?: return
      val extra = block.nameElements.size - (type.args + 1)
      if (extra <= 0) return
      val toRemove = block.nameElements.toList().takeLast(extra)
      var end = obj.prevSibling!!
      if (end.textContains('\n')) end = toRemove.last()
      block.deleteChildRange(toRemove.first(), end)
    }
  }

  private object RenameBlockFix : TFDuplicatedInspectionBase.Companion.RenameQuickFix() {
    override fun getFamilyName(): String = HCLBundle.message("block.name.validness.inspection.rename.block.quick.fix.name")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
      val block = descriptor.psiElement as? HCLBlock ?: return
      invokeRenameRefactoring(project, block)
    }
  }
}


