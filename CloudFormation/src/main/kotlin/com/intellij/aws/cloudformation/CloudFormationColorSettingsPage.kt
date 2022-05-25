package com.intellij.aws.cloudformation

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.jetbrains.yaml.YAMLSyntaxHighlighter
import javax.swing.Icon

internal class CloudFormationColorSettingsPage : ColorSettingsPage {
  private val attrDescriptors: Array<AttributesDescriptor> = arrayOf(
    AttributesDescriptor(CloudFormationBundle.message("colors.aws.resource.type"), CloudFormationYamlAnnotator.AWS_RESOURCE_TYPE),
  )

  @Suppress("HardCodedStringLiteral")
  override fun getDisplayName(): String = "AWS CloudFormation YAML"
  override fun getIcon(): Icon = CloudFormationIcons.AwsFile
  override fun getAttributeDescriptors(): Array<AttributesDescriptor> = attrDescriptors
  override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

  override fun getHighlighter(): SyntaxHighlighter {
    return YAMLSyntaxHighlighter()
  }

  override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> {
    return mapOf("resourceType" to CloudFormationYamlAnnotator.AWS_RESOURCE_TYPE)
  }

  override fun getDemoText(): String {
    return """
      AWSTemplateFormatVersion: "2010-09-09"
      Resources:
        DefaultBucket:
          Type: <resourceType>AWS::S3::Bucket</resourceType>
          Properties:
            BucketName: system
    """.trimIndent()
  }
}