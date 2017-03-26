package com.intellij.aws.cloudformation

enum class CloudFormationParameterType(val id: kotlin.String) {
  String("String"),
  Number("Number"),
  ListNumber("List<Number>"),
  ListString("List<String>"),
  CommaDelimitedList("CommaDelimitedList");

  companion object {
    val allIds = CloudFormationParameterType.values().map { it.id }.toSet()
  }
}