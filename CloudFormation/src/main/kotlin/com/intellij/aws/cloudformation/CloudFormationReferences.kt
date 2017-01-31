package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnVisitor
import com.intellij.openapi.util.TextRange

enum class ReferenceType {
  Ref,
  Condition,
  Resource,
  Mapping,
  Parameter
}

data class CloudFormationReference(
    val name: String,
    val type: ReferenceType,
    val rangeInElement: TextRange? = null,
    val excludeFromCompletion: List<String> = emptyList())

class CloudFormationReferences private constructor(val parsed: CloudFormationParsedFile): CfnVisitor() {
}
