package com.intellij.aws.cloudformation.metadata

data class CloudFormationMetadata(
    val resourceTypes: List<CloudFormationResourceType>,
    val predefinedParameters: List<String>,
    val limits: CloudFormationLimits) {

  fun findResourceType(name: String): CloudFormationResourceType? =
      resourceTypes.filter { it.name == name }.singleOrNull()
}
