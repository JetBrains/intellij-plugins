package com.intellij.aws.cloudformation

enum class CloudFormationParameterProperty(val id: String) {
  AllowedPattern("AllowedPattern"),
  AllowedValues("AllowedValues"),
  ConstraintDescription("ConstraintDescription"),
  Default("Default"),
  Description("Description"),
  MaxLength("MaxLength"),
  MaxValue("MaxValue"),
  MinLength("MinLength"),
  MinValue("MinValue"),
  NoEcho("NoEcho"),
  Type("Type");

  companion object {
    val allIds = CloudFormationParameterProperty.values().map { it.id }.toSet()
  }
}