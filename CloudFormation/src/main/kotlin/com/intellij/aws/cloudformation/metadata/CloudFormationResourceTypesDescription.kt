package com.intellij.aws.cloudformation.metadata

data class CloudFormationResourceTypesDescription(
    val resourceTypes: Map<String, CloudFormationResourceTypeDescription>
)