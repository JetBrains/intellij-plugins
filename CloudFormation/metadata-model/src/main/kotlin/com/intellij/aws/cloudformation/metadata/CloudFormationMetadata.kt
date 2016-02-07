package com.intellij.aws.cloudformation.metadata

import java.util.*

class CloudFormationMetadata {
  var resourceTypes: MutableList<CloudFormationResourceType> = ArrayList()
  var predefinedParameters: MutableList<String> = ArrayList()
  var limits = CloudFormationLimits()

  fun findResourceType(name: String): CloudFormationResourceType? {
    for (resourceType in resourceTypes) {
      if (resourceType.name == name) {
        return resourceType
      }
    }

    return null
  }
}
