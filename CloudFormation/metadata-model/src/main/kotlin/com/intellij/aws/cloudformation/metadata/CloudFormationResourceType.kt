package com.intellij.aws.cloudformation.metadata

import java.util.*

class CloudFormationResourceType {
  var name: String? = null
  var description: String? = null
  var properties: MutableList<CloudFormationResourceProperty> = ArrayList()
  var attributes: MutableList<CloudFormationResourceAttribute> = ArrayList()

  fun findProperty(name: String): CloudFormationResourceProperty? {
    for (property in properties) {
      if (property.name == name) {
        return property
      }
    }

    return null
  }

  val requiredProperties: Set<String>
    get() {
      val requiredProperties = HashSet<String>()
      for (property in properties) {
        if (property.required) {
          requiredProperties.add(property.name!!)
        }
      }

      return requiredProperties
    }
}
