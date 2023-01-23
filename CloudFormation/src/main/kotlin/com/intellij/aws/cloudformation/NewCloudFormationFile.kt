package com.intellij.aws.cloudformation

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.json.JsonFileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.yaml.YAMLFileType

class NewCloudFormationFile : CreateFileFromTemplateAction(
  CloudFormationBundle.messagePointer("aws.cloudformation.new.file.action"),
  CloudFormationBundle.messagePointer("aws.cloudformation.new.file.action.description"),
  CloudFormationIcons.AwsFile
), DumbAware {

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
      .setTitle(CloudFormationBundle.message("aws.cloudformation.new.file.action"))
      .addKind(CloudFormationBundle.message("aws.cloudformation.new.yaml.file.action"),
               YAMLFileType.YML.icon, "AWS CloudFormation Template (YAML)")
      .addKind(CloudFormationBundle.message("aws.cloudformation.new.json.file.action"),
               JsonFileType.INSTANCE.icon, "AWS CloudFormation Template (JSON)")
  }

  override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String {
    return CloudFormationBundle.message("aws.cloudformation.new.file.action.0", newName)
  }
}