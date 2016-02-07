package com.intellij.aws.cloudformation

object CloudFormationIntrinsicFunctions {
  val FnBase64 = "Fn::Base64"
  val FnFindInMap = "Fn::FindInMap"
  val FnGetAtt = "Fn::GetAtt"
  val FnGetAZs = "Fn::GetAZs"
  val FnJoin = "Fn::Join"
  val FnSelect = "Fn::Select"
  val FnIf = "Fn::If"
  val Ref = "Ref"
}
