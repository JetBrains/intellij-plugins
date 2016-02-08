package com.intellij.aws.cloudformation.metadata

data class CloudFormationMetadata(
    val resourceTypes: List<CloudFormationResourceType>,
    val predefinedParameters: List<String>,
    val limits: CloudFormationLimits) {

  // Called by xstream on deserialization
  @Suppress("Unused")
  private fun readResolve(): Any {
    // Call the usual constructor, which calls resourceTypesMap initializer
    return this.copy()
  }

  val resourceTypesMap = resourceTypes.map { it.name to it }.toMap()
  fun findResourceType(name: String): CloudFormationResourceType? = resourceTypesMap[name]
}
