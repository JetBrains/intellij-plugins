package com.intellij.aws.cloudformation.metadata

data class CloudFormationMetadata(
    val resourceTypes: Map<String, CloudFormationResourceType>,
    val predefinedParameters: List<String>,
    val limits: CloudFormationLimits) {

  fun findResourceType(name: String): CloudFormationResourceType? = resourceTypes[name]
}
