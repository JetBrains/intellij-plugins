package com.intellij.aws.cloudformation

import com.intellij.util.containers.HashSet
import java.util.*

object CloudFormationConstants {
  val SupportedTemplateFormatVersions: Set<String> = HashSet(Arrays.asList(
      "2010-09-09"))

  val CustomResourceType = "AWS::CloudFormation::CustomResource"
  val CustomResourceTypePrefix = "Custom::"

  val CommentResourcePropertyName = "Comment"

  val ConditionPropertyName = "Condition"
  val TypePropertyName = "Type"
  val PropertiesPropertyName = "Properties"
  val CreationPolicyPropertyName = "CreationPolicy"
  val DeletionPolicyPropertyName = "DeletionPolicy"
  val DependsOnPropertyName = "DependsOn"
  val MetadataPropertyName = "Metadata"
  val UpdatePolicyPropertyName = "UpdatePolicy"
  val VersionPropertyName = "Version"

  val AllTopLevelResourceProperties: Set<String> = HashSet(Arrays.asList(
      ConditionPropertyName,
      TypePropertyName,
      PropertiesPropertyName,
      CreationPolicyPropertyName,
      DeletionPolicyPropertyName,
      DependsOnPropertyName,
      MetadataPropertyName,
      UpdatePolicyPropertyName,
      VersionPropertyName))
}
