package com.intellij.aws.cloudformation

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.json.JsonFileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class CreateJsonTemplateFileAction : CreateFileFromTemplateAction(
    CloudFormationBundle.getString("aws.cloudformation.new.json.file.action"),
    CloudFormationBundle.getString("aws.cloudformation.new.json.file.action.description"),
    JsonFileType.INSTANCE.icon), DumbAware {

  override fun getDefaultTemplateProperty(): String? = "DefaultJsonCloudFormationTemplate"

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
        .setTitle(CloudFormationBundle.getString("aws.cloudformation.new.json.file.action"))
        .addKind(CloudFormationBundle.getString("aws.cloudformation.new.json.file.action"),
            JsonFileType.INSTANCE.icon, "AWS CloudFormation Template (JSON)")
  }

  override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String {
    return CloudFormationBundle.getString("aws.cloudformation.new.json.file.action")
  }
}