package com.intellij.aws.cloudformation

enum class CloudFormationIntrinsicFunction(val id: String) {
  FnBase64("Fn::Base64"),
  FnFindInMap("Fn::FindInMap"),
  FnGetAtt("Fn::GetAtt"),
  FnGetAZs("Fn::GetAZs"),
  FnImportValue("Fn::ImportValue"),
  FnJoin("Fn::Join"),
  FnSelect("Fn::Select"),
  FnSplit("Fn::Split"),
  FnSub("Fn::Sub"),
  Ref("Ref"),

  // Conditions
  Condition("Condition"),
  FnAnd("Fn::And"),
  FnEquals("Fn::Equals"),
  FnIf("Fn::If"),
  FnNot("Fn::Not"),
  FnOr("Fn::Or");

  val shortForm = if (this.id.startsWith("Fn::")) id.substring("Fn::".length) else id

  companion object {
    val fullNames = CloudFormationIntrinsicFunction.values().map { Pair(it.id, it) }.toMap()
    val shortNames = CloudFormationIntrinsicFunction.values().map { Pair(it.shortForm, it) }.toMap()
  }
}
