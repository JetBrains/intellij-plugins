package com.intellij.aws.cloudformation.model

import com.intellij.aws.cloudformation.CloudFormationMetadataProvider
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceAttribute
import com.intellij.aws.cloudformation.metadata.awsServerlessFunction

class CfnResourceNode(name: CfnScalarValueNode?,
                      val type: CfnResourceTypeNode?,
                      val properties: CfnResourcePropertiesNode?,
                      val condition: CfnResourceConditionNode?,
                      val dependsOn: CfnResourceDependsOnNode?,
                      val allTopLevelProperties: Map<String, CfnNamedNode>) : CfnNamedNode(name) {
  val typeName: String?
    get() = type?.value?.value

  fun getAttributes(root: CfnRootNode): Map<String, CloudFormationResourceAttribute> {
    val resourceTypeName = typeName ?: return emptyMap()
    val resourceType = CloudFormationMetadataProvider.METADATA.findResourceType(resourceTypeName, root) ?: return emptyMap()

    // https://github.com/awslabs/serverless-application-model/blob/develop/versions/2016-10-31.md#referencing-lambda-version--alias-resources
    if (isAwsServerlessFunctionWithAutoPublishAlias()) {
      val additionalAttributes = listOf("Version", "Alias")
      return resourceType.attributes + additionalAttributes.map { it to CloudFormationResourceAttribute(it) }.toMap()
    }

    return resourceType.attributes
  }

  // https://github.com/awslabs/serverless-application-model/blob/develop/versions/2016-10-31.md#referencing-lambda-version--alias-resources
  fun isAwsServerlessFunctionWithAutoPublishAlias() =
      typeName == awsServerlessFunction.name &&
          properties != null && properties.properties.any { it.name?.value == "AutoPublishAlias" && it.value != null }
}