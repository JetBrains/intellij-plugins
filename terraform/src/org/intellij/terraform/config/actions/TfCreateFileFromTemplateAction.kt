// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.actions.NewFileActionWithCategory
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.HCLBundle

class TfCreateFileFromTemplateAction : CreateFileFromTemplateAction(), DumbAware, NewFileActionWithCategory {
  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder.run {
      setTitle(HCLBundle.message("action.create.terraform.file.title"))

      TerraformTemplate.entries.forEach { template ->
        addKind(template.title, template.icon, template.templateName)
      }
    }
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
    HCLBundle.message("action.create.terraform.file", templateName)

  override fun createFileFromTemplate(name: String?, template: FileTemplate?, dir: PsiDirectory?): PsiFile =
    super.createFileFromTemplate(getDefaultFileName(name, template), template, dir)

  private fun getDefaultFileName(name: String?, template: FileTemplate?): String? {
    if (name.isNullOrBlank() || template == null) return name

    val terraformTemplate = TerraformTemplate.entries.firstOrNull {
      it.templateName == template.name
    } ?: return name

    val pattern = terraformTemplate.fileNamePattern ?: return name
    return "$name.$pattern"
  }

  override fun getCategory(): String = "Deployment"
}