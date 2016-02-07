package com.intellij.aws.cloudformation.metadata

data class CloudFormationResourceProperty(
    val name: String,
    val description: String,
    val type: String,
    val required: Boolean,
    val updateRequires: String)

data class CloudFormationResourceAttribute(val name: String, val description: String)

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

data class CloudFormationLimits(val maxParameters: Int, val maxOutputs: Int, val maxMappings: Int)

data class CloudFormationMetadata(
    val resourceTypes: List<CloudFormationResourceType>,
    val predefinedParameters: List<String>,
    val limits: CloudFormationLimits) {

  fun findResourceType(name: String): CloudFormationResourceType? =
      resourceTypes.filter { it.name == name }.singleOrNull()
}
