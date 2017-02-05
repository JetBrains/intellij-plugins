package com.intellij.aws.cloudformation.metadata

import com.intellij.aws.cloudformation.CloudFormationConstants

data class CloudFormationResourceType(
    val name: String,
    val properties: Map<String, CloudFormationResourceProperty>,
    val attributes: Map<String, CloudFormationResourceAttribute>) {

  fun findProperty(name: String): CloudFormationResourceProperty? = properties[name]

  val requiredProperties: Set<String>
      get() = properties.values.filter { it.required }.map { it.name }.toSet()

  companion object {
    fun isCustomResourceType(value: String): Boolean {
      return value == CloudFormationConstants.CustomResourceType || value.startsWith(CloudFormationConstants.CustomResourceTypePrefix)
    }

    fun isCloudFormationStack(value: String): Boolean = value == "AWS::CloudFormation::Stack"
  }
}
