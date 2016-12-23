package com.intellij.aws.cloudformation

enum class CloudFormationIntrinsicFunctions(val id: String) {
  FnBase64("Fn::Base64"),
  FnFindInMap("Fn::FindInMap"),
  FnGetAtt("Fn::GetAtt"),
  FnGetAZs("Fn::GetAZs"),
  FnJoin("Fn::Join"),
  FnSelect("Fn::Select"),
  FnIf("Fn::If"),
  Ref("Ref");

  companion object {
    val allNames = CloudFormationIntrinsicFunctions.values().map { it.id }.toSet()
  }
}
