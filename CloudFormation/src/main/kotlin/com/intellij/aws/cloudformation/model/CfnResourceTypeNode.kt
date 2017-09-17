package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.CloudFormationConstants
import com.intellij.aws.cloudformation.CloudFormationMetadataProvider
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType.Companion.isCustomResourceType

class CfnResourceTypeNode(name: CfnScalarValueNode?, val value: CfnScalarValueNode?) : CfnNamedNode(name) {
  fun metadata(context: CfnRootNode): CloudFormationResourceType? {
    val typeName = value?.value ?: return null

    return CloudFormationMetadataProvider.METADATA.findResourceType(
          if (isCustomResourceType(typeName)) CloudFormationConstants.CustomResourceType else typeName, context)
  }
}
