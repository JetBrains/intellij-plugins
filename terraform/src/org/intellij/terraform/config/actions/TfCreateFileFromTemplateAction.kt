// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.HCLBundle

class TfCreateFileFromTemplateAction : CreateFileFromTemplateAction(), DumbAware {
  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder.run {
      setTitle(HCLBundle.message("action.create.terraform.file.title"))
      addKind(HCLBundle.message("action.NewTerraformFile.description"), TerraformIcons.Terraform, "Terraform File")
    }
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
    HCLBundle.message("action.create.terraform.file", templateName)
}
