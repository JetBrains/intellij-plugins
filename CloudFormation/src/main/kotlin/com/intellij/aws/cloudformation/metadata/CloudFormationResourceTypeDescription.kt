package com.intellij.aws.cloudformation.metadata

import org.jetbrains.annotations.Nls

data class CloudFormationResourceTypeDescription(
  @Nls val description: String,
  val properties: Map<String, String>,
  val attributes: Map<String, String>
)

