package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.references.CloudFormationEntityReference

class EntityResolveTests : ResolveTestsBase(CloudFormationEntityReference::class.java) {
  fun testDependsOnSingle() {
    assertEntityResolve("dependsOnSingle", "WebServerUser")
  }

  fun testDependsOnMulti() {
    assertEntityResolve("dependsOnMulti", "WebServerUser1", "WebServerUser2")
  }

  fun testFindInMapRefToResource() {
    assertEntityResolve("findInMapRefToResource", NotResolved)
  }

  fun testFindInMapRefToMapping() {
    assertEntityResolve("findInMapRefToMapping", "AWSInstanceType2Arch")
  }

  fun testGetAttrParameterRef() {
    assertEntityResolve("getAttrParameterRef", NotResolved)
  }

  fun testGetAttrResourceRef() {
    assertEntityResolve("getAttrResourceRef", "WebServerUser")
  }

  fun testRefToResource() {
    assertEntityResolve("refToResource", "WebServerUser")
  }

  fun testRefToParameter() {
    assertEntityResolve("refToParameter", "WebServerInstanceType")
  }

  fun testRefToParameterInCloudFormationInterfaceParameterLabels() {
    assertEntityResolve("refToParameterInCloudFormationInterfaceParameterLabels", "WebServer12InstanceType1", "WebServer12InstanceType2", NotResolved)
  }

  fun testRefToParameterInCloudFormationInterfaceParameterGroups() {
    assertEntityResolve("refToParameterInCloudFormationInterfaceParameterGroups", NotResolved, "WebServerInstanceType1", "WebServerInstanceType2")
  }

  fun testRefToMapping() {
    assertEntityResolve("refToMapping", NotResolved)
  }

  fun testRefToOutput() {
    assertEntityResolve("refToOutput", NotResolved, NotResolved)
  }

  fun testConditionOnResource() {
    assertEntityResolve("conditionOnResource", "CreateProdResources")
  }

  fun testCondition() {
    assertEntityResolve("condition", "CreateProdResources")
  }

  fun testConditionInIf() {
    assertEntityResolve("conditionInIf", "CreateProdResources")
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("resolve")
  }
}