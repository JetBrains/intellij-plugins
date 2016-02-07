package com.intellij.aws.cloudformation

object CloudFormationSections {
  val Resources = "Resources"
  val Parameters = "Parameters"
  val Mappings = "Mappings"
  val Metadata = "Metadata"
  val Conditions = "Conditions"
  val Outputs = "Outputs"

  val ResourcesSingletonList: Collection<String> = listOf(Resources)
  val ParametersSingletonList: Collection<String> = listOf(Parameters)
  val MappingsSingletonList: Collection<String> = listOf(Mappings)
  val MetadataSingletonList: Collection<String> = listOf(Metadata)
  val ConditionsSingletonList: Collection<String> = listOf(Conditions)
  val OutputsSingletonList: Collection<String> = listOf(Outputs)

  val FormatVersion = "AWSTemplateFormatVersion"
  val Description = "Description"
}
