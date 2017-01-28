package com.intellij.aws.cloudformation

enum class CloudFormationSection(val id: String) {
  Resources("Resources"),
  Parameters("Parameters"),
  Mappings("Mappings"),
  Metadata("Metadata"),
  Conditions("Conditions"),
  Outputs("Outputs"),

  FormatVersion("AWSTemplateFormatVersion"),
  Transform("Transform"),
  Description("Description");

  companion object {
    val ConditionsSingletonList = listOf(Conditions)
    val MappingsSingletonList = listOf(Mappings)
    val ResourcesSingletonList = listOf(Resources)
    val ParametersSingletonList = listOf(Parameters)

    val id2enum = CloudFormationSection.values().map { Pair(it.id, it) }.toMap()
  }
}
