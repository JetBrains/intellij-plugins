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
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.HCLBundle

class TfCreateFileFromTemplateAction : CreateFileFromTemplateAction(), DumbAware, NewFileActionWithCategory {
  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder.run {
      setTitle(HCLBundle.message("action.create.terraform.file.title"))
      setDefaultText(DEFAULT_FILE_NAME)

      addKind(HCLBundle.message("action.new.empty.terraform.file.description"), TerraformIcons.Terraform, EMPTY_TF_TEMPLATE)
      addKind(HCLBundle.message("action.new.template.terraform.file.description"), TerraformIcons.Terraform, TEMPLATE_TF_TEMPLATE)
      addKind(HCLBundle.message("action.new.outputs.terraform.file.description"), TerraformIcons.Terraform, OUTPUTS_TF_TEMPLATE)
      addKind(HCLBundle.message("action.new.variables.terraform.file.description"), TerraformIcons.Terraform, VARIABLES_TF_TEMPLATE)
    }
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
    HCLBundle.message("action.create.terraform.file", templateName)

  override fun createFileFromTemplate(name: String?, template: FileTemplate?, dir: PsiDirectory?): PsiFile =
    super.createFileFromTemplate(getDefaultFileName(name, template), template, dir)

  private fun getDefaultFileName(name: String?, template: FileTemplate?): String? {
    val isDefaultName = name.isNullOrEmpty() || name == DEFAULT_FILE_NAME

    val newName = when (template?.name) {
      OUTPUTS_TF_TEMPLATE -> "outputs"
      VARIABLES_TF_TEMPLATE -> "variables"
      else -> name
    }
    return if (isDefaultName) newName else name
  }

  override fun getCategory(): String = "Deployment"
}

private const val EMPTY_TF_TEMPLATE = "Empty File"
private const val TEMPLATE_TF_TEMPLATE = "Template File"
private const val OUTPUTS_TF_TEMPLATE = "Outputs File"
private const val VARIABLES_TF_TEMPLATE = "Variables File"

private const val DEFAULT_FILE_NAME = "main"