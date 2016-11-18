package com.intellij.aws.cloudformation.metadata

data class CloudFormationResourceTypeDescription(
    val description: String,
    val properties: Map<String, String>,
    val attributes: Map<String, String>
)

