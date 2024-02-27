// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls

open class TFInitAction : TFExternalToolsAction() {

  override suspend fun invoke(project: Project, module: Module?, title: @Nls String, virtualFile: VirtualFile) {
    project.service<TerraformActionService>().initTerraform(virtualFile, title)
  }
}

class TFInitRequiredAction : TFInitAction() {

  override fun update(e: AnActionEvent) {
    super.update(e)
    e.presentation.icon = TerraformIcons.Terraform
    if (!e.presentation.isEnabledAndVisible) {
      e.presentation.isVisible = false
      return
    }
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    val project = e.project ?: return
    e.presentation.isEnabledAndVisible = isInitRequired(project, file)
  }

  companion object {

    fun createQuickFixNotInitialized(element: PsiElement): LocalQuickFix? {
      val virtualFile = element.containingFile.virtualFile ?: return null
      if (!isInitRequired(element.project, virtualFile)) return null

      return object : LocalQuickFix {

        override fun startInWriteAction(): Boolean = false

        override fun getFamilyName(): String = HCLBundle.message("action.TFInitRequiredAction.text")

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
          project.service<TerraformActionService>().scheduleTerraformInit(descriptor.psiElement.containingFile.virtualFile)
        }

      }
    }

    private fun isInitRequired(project: Project, virtualFile: VirtualFile): Boolean {
      val lock = project.service<LocalSchemaService>().findLockFile(virtualFile) ?: return true
      val terraformDirectory = lock.parent.findChild(".terraform") ?: return true
      return !terraformDirectory.isDirectory || terraformDirectory.children.isEmpty()
    }

  }

}

