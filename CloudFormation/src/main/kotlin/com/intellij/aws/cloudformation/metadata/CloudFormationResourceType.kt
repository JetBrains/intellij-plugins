package com.intellij.aws.cloudformation.metadata

data class CloudFormationResourceType(
    val name: String,
    val description: String,
    val properties: List<CloudFormationResourceProperty>,
    val attributes: List<CloudFormationResourceAttribute>) {

  fun findProperty(name: String): CloudFormationResourceProperty? =
      properties.filter { it.name == name }.singleOrNull()

  val requiredProperties: Set<String>
    get() = properties.filter { it.required }.map { it.name }.toSet()
}