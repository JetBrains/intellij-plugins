// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import com.intellij.codeInspection.*
import com.intellij.execution.ExecutionException
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.toArray
import org.intellij.terraform.config.actions.TfInitAction
import org.intellij.terraform.config.model.ModuleDetectionUtil
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.util.TfExecutor
import org.intellij.terraform.config.util.TfExecutorService
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.isTerraformCompatiblePsiFile
import org.jetbrains.annotations.NonNls

class TfMissingModuleInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerraformCompatiblePsiFile(file)
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return MyEV(holder)
  }

  override fun getID(): String {
    return "MissingModule"
  }

  override fun getBatchSuppressActions(element: PsiElement?): Array<SuppressQuickFix> {
    return super.getBatchSuppressActions(PsiTreeUtil.getParentOfType(element, HCLBlock::class.java, false))
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitBlock(block: HCLBlock) {
      ProgressIndicatorProvider.checkCanceled()
      block.getNameElementUnquoted(0) ?: return
      block.`object` ?: return
      if (!TfPsiPatterns.ModuleRootBlock.accepts(block)) return
      if (TfPsiPatterns.ModuleWithEmptySource.accepts(block)) return
      doCheck(holder, block)
    }
  }

  private fun doCheck(holder: ProblemsHolder, block: HCLBlock) {
    val directory = block.containingFile.containingDirectory ?: return

    val pair = ModuleDetectionUtil.getAsModuleBlockOrError(block)
    if (pair !is ModuleDetectionUtil.Result.Failure) return
    @NonNls val err = pair.failureString

    ProgressIndicatorProvider.checkCanceled()

    val applicableToolType = getApplicableToolType(directory.virtualFile)

    holder.registerProblem(block, HCLBundle.message("missing.module.inspection.missing.module.error.message", err),
                           ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                           *listOfNotNull(RunTFToolGetFix(directory.name, applicableToolType.executableName),
                                          TfInitAction.createQuickFixNotInitialized(block)).toArray(LocalQuickFix.EMPTY_ARRAY)
    )
  }
}


class RunTFToolGetFix(private val directoryName: String, private val executableName: String) : LocalQuickFix {

  companion object {
    private val LOG = Logger.getInstance(RunTFToolGetFix::class.java)
  }

  override fun getName(): String = HCLBundle.message("missing.module.inspection.run.terraform.get.quick.fix.name", directoryName, executableName)

  override fun getFamilyName(): String = HCLBundle.message("missing.module.inspection.run.terraform.get.quick.fix.family.name", executableName)

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val block = descriptor.psiElement as? HCLBlock ?: return
    val dir = block.containingFile?.containingDirectory ?: return

    val vf = dir.virtualFile
    if (vf.fileSystem !is LocalFileSystem) {
      LOG.warn("Cannot run on non-local FS: $vf")
      return
    }
    val toolType = getApplicableToolType(vf)
    try {
      project.service<TfExecutorService>().executeInBackground(
        TfExecutor.`in`(project, toolType)
          .withWorkDirectory(vf.path)
          .withParameters("get")
          .withPresentableName("${toolType.executableName} get")
          .showOutputOnError()
          .showNotifications(true, false)
      )
    }
    catch (e: ExecutionException) {
      LOG.warn("Failed to run '${toolType.executableName} get': ${e.message}", e)
      Messages.showMessageDialog(project,
                                 HCLBundle.message("missing.module.inspection.run.terraform.get.quick.fix.failure.message", e.message, toolType.executableName),
                                 HCLBundle.message("missing.module.inspection.run.terraform.get.quick.fix.failure.title", toolType.displayName),
                                 Messages.getErrorIcon())
    }
  }
}