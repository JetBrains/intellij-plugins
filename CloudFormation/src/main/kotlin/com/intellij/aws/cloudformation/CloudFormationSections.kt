package com.intellij.aws.cloudformation

object CloudFormationSections {
  val Resources = "Resources"
  val Parameters = "Parameters"
  val Mappings = "Mappings"
  val Metadata = "Metadata"
  val Conditions = "Conditions"
  val Outputs = "Outputs"

  val ResourcesSingletonList = listOf(Resources)
  val ParametersSingletonList = listOf(Parameters)
  val MappingsSingletonList = listOf(Mappings)
  val MetadataSingletonList = listOf(Metadata)
  val ConditionsSingletonList = listOf(Conditions)
  val OutputsSingletonList = listOf(Outputs)

  val FormatVersion = "AWSTemplateFormatVersion"
  val Description = "Description"
}
