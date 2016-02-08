package com.intellij.aws.cloudformation.metadata

data class CloudFormationResourceType(
    val name: String,
    val description: String,
    val properties: List<CloudFormationResourceProperty>,
    val attributes: List<CloudFormationResourceAttribute>) {

  // Called by xstream on deserialization
  @Suppress("Unused")
  private fun readResolve(): Any {
    // Call the usual constructor, which calls initializers
    return this.copy()
  }

  val propertiesMap = properties.map { it.name to it }.toMap()
  fun findProperty(name: String): CloudFormationResourceProperty? = propertiesMap[name]

  val requiredProperties: Set<String> = properties.filter { it.required }.map { it.name }.toSet()
}