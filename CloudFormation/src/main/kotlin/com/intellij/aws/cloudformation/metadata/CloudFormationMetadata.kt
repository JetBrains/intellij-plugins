package com.intellij.aws.cloudformation.metadata

import com.intellij.aws.cloudformation.model.CfnRootNode
import java.util.TreeMap

data class CloudFormationMetadata(
    val resourceTypes: TreeMap<String, CloudFormationResourceType>,
    val predefinedParameters: List<String>,
    val limits: CloudFormationLimits,
    val serverlessGlobals: Map<String, List<String>>? = null) {

  fun findResourceType(name: String, context: CfnRootNode): CloudFormationResourceType? {
    val resourceType = resourceTypes[name]
    if (resourceType?.transform != null &&
        context.transformValues.all { resourceType.transform != it }) {
      return null
    }

    return resourceType
  }

  fun supportedGlobalsProperties(sectionName: String): Set<String>? {
    return serverlessGlobals?.get(sectionName)?.toSet()
  }

  val supportedGlobalsSections: List<String>
    get() = serverlessGlobals.orEmpty().keys.sorted()
}
