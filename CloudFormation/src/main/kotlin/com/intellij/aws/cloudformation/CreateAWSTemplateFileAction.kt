package com.intellij.aws.cloudformation

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.json.JsonFileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class CreateAWSTemplateFileAction : CreateFileFromTemplateAction(
    CloudFormationBundle.getString("aws.cloudformation.new.file.action"),
    CloudFormationBundle.getString("aws.cloudformation.new.file.action.description"),
    JsonFileType.INSTANCE.icon), DumbAware {

  override fun getDefaultTemplateProperty(): String? = "DefaultAwsCloudFormationTemplate"

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
        .setTitle(CloudFormationBundle.getString("aws.cloudformation.new.file.action"))
        .addKind(CloudFormationBundle.getString("aws.cloudformation.new.file.action"),
            JsonFileType.INSTANCE.icon, "AWS CloudFormation Template")
  }

  override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String {
    return CloudFormationBundle.getString("aws.cloudformation.new.file.action")
  }
}