// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption
import com.intellij.openapi.vcs.checkin.*
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.intellij.terraform.config.Constants.TF_FMT
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.isTerraformFileExtension
import org.intellij.terraform.runtime.TfProjectSettings

class TfFmtCheckinFactory : CheckinHandlerFactory() {
  override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
    return TFFmtCommitCheck(panel.project)
  }

  private class TFFmtCommitCheck(private val project: Project) : CheckinHandler(), CommitCheck, DumbAware {
    override fun getExecutionOrder(): CommitCheck.ExecutionOrder = CommitCheck.ExecutionOrder.MODIFICATION

    override fun isEnabled(): Boolean = TfProjectSettings.getInstance(project).isFormattedBeforeCommit

    override suspend fun runCheck(commitInfo: CommitInfo): CommitProblem? {
      FileDocumentManager.getInstance().saveAllDocuments()

      val manager = PsiManager.getInstance(project)
      val commitedPsiFiles: List<PsiFile> = readAction {
        commitInfo.committedVirtualFiles
          .filter { isTerraformFileExtension(it.extension) }
          .mapNotNull { manager.findFile(it) }
          .filterIsInstance<HCLFile>()
      }

      try {
        val virtualFiles = commitedPsiFiles.map { it.virtualFile }.toTypedArray()
        TfFmtFileAction().invoke(project, TF_FMT, *virtualFiles)
      }
      catch (_: Exception) {
        return TfFmtCommitProblem()
      }
      return null
    }

    override fun getBeforeCheckinConfigurationPanel(): RefreshableOnComponent =
      BooleanCommitOption.create(project, this, false, TF_FMT, TfProjectSettings.getInstance(project)::isFormattedBeforeCommit)
  }

  private class TfFmtCommitProblem : CommitProblem {
    override val text: String
      get() = HCLBundle.message("terraform.fmt.commit.error.message")
  }
}