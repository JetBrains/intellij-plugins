package com.intellij.aws.cloudformation

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.yaml.YAMLFileType

class CreateYamlTemplateFileAction : CreateFileFromTemplateAction(
    CloudFormationBundle.message("aws.cloudformation.new.yaml.file.action"),
    CloudFormationBundle.message("aws.cloudformation.new.yaml.file.action.description"),
    YAMLFileType.YML.icon), DumbAware {

  override fun getDefaultTemplateProperty(): String = "DefaultYamlCloudFormationTemplate"

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
        .setTitle(CloudFormationBundle.message("aws.cloudformation.new.yaml.file.action"))
        .addKind(CloudFormationBundle.message("aws.cloudformation.new.yaml.file.action"),
            YAMLFileType.YML.icon, "AWS CloudFormation Template (YAML)")
  }

  override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String {
    return CloudFormationBundle.message("aws.cloudformation.new.yaml.file.action")
  }
}