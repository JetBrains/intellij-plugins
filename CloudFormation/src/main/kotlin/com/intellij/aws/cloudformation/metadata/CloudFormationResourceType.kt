package com.intellij.aws.cloudformation.metadata

data class CloudFormationResourceType(
    val name: String,
    val properties: Map<String, CloudFormationResourceProperty>,
    val attributes: Map<String, CloudFormationResourceAttribute>) {

  fun findProperty(name: String): CloudFormationResourceProperty? = properties[name]

  val requiredProperties: Set<String>
      get() = properties.values.filter { it.required }.map { it.name }.toSet()
}