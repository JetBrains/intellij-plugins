// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls

internal open class TfInitAction(private val notifyOnSuccess: Boolean = true) : TfExternalToolsAction() {

  override suspend fun invoke(project: Project, title: @Nls String, vararg virtualFiles: VirtualFile) {
    virtualFiles.firstOrNull()?.let { project.service<TfActionService>().initTerraform(it, notifyOnSuccess) }
  }

  companion object {

    fun createQuickFixNotInitialized(element: PsiElement): LocalQuickFix? {
      val virtualFile = element.containingFile.virtualFile ?: return null
      if (!isInitRequired(element.project, virtualFile)) return null

      return object : LocalQuickFix {

        override fun startInWriteAction(): Boolean = false

        override fun getFamilyName(): String = HCLBundle.message("action.TfInitRequiredAction.text")

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
          project.service<TfActionService>()
            .scheduleTerraformInit(
              descriptor.psiElement.containingFile.virtualFile, notifyOnSuccess = false
            )
        }

      }
    }

    fun isInitRequired(project: Project, virtualFile: VirtualFile): Boolean {
      val lock = project.service<LocalSchemaService>().findLockFile(virtualFile) ?: return true
      val terraformDirectory = lock.parent.findChild(".terraform") ?: return true
      return !terraformDirectory.isDirectory || terraformDirectory.children.isEmpty()
    }

  }

}

internal class TfInitRequiredAction : TfInitAction(false)

