package com.intellij.aws.cloudformation.metadata

class CloudFormationResourceProperty {
  var name: String? = null
  var description: String? = null
  var type: String? = null
  var required: Boolean = false
  var updateRequires: String? = null

  companion object {
    fun create(name: String, description: String, type: String, required: Boolean, updateRequires: String): CloudFormationResourceProperty {
      val property = CloudFormationResourceProperty()
      property.name = name
      property.description = description
      property.type = type
      property.required = required
      property.updateRequires = updateRequires
      return property
    }
  }
}
