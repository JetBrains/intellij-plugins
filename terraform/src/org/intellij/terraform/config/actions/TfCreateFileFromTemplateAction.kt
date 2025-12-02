// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
      setDefaultText(DEFAULT_FILE_NAME)

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
    val isDefaultName = name.isNullOrEmpty() || name == DEFAULT_FILE_NAME

    val newName = TerraformTemplate.entries
      .find { it.templateName == template?.name }
      ?.defaultFileName
      ?: name

    return if (isDefaultName) newName else name
  }

  override fun getCategory(): String = "Deployment"
}

private const val DEFAULT_FILE_NAME = "main"