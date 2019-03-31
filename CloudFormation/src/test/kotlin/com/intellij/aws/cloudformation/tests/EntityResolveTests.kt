package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.references.CloudFormationEntityReference

class EntityResolveTests : ResolveTestsBase(CloudFormationEntityReference::class.java) {
  fun testDependsOnSingle() {
    assertEntityResolve("dependsOnSingle.template", "WebServerUser")
  }

  fun testDependsOnMulti() {
    assertEntityResolve("dependsOnMulti.template", "WebServerUser1", "WebServerUser2")
  }

  fun testFindInMapRefToResource() {
    assertEntityResolve("findInMapRefToResource.template", NotResolved)
  }

  fun testFindInMapRefToMapping() {
    assertEntityResolve("findInMapRefToMapping.template", "AWSInstanceType2Arch")
  }

  fun testGetAttrParameterRef() {
    assertEntityResolve("getAttrParameterRef.template", NotResolved)
  }

  fun testGetAttrResourceRef() {
    assertEntityResolve("getAttrResourceRef.template", "WebServerUser")
  }

  fun testRefToResource() {
    assertEntityResolve("refToResource.template", "WebServerUser")
  }

  fun testRefToParameter() {
    assertEntityResolve("refToParameter.template", "WebServerInstanceType")
  }

  fun testRefToParameterInCloudFormationInterfaceParameterLabels() {
    assertEntityResolve("refToParameterInCloudFormationInterfaceParameterLabels.template", "WebServer12InstanceType1", "WebServer12InstanceType2", NotResolved)
  }

  fun testRefToParameterInCloudFormationInterfaceParameterGroups() {
    assertEntityResolve("refToParameterInCloudFormationInterfaceParameterGroups.template", NotResolved, "WebServerInstanceType1", "WebServerInstanceType2")
  }

  fun testRefToMapping() {
    assertEntityResolve("refToMapping.template", NotResolved)
  }

  fun testRefToOutput() {
    assertEntityResolve("refToOutput.template", NotResolved, NotResolved)
  }

  fun testConditionOnResource() = assertEntityResolve("conditionOnResource.template", "CreateProdResources")

  fun testShortRef() = assertEntityResolve("shortRef.yaml", "WebServer")

  fun testCondition() {
    assertEntityResolve("condition.template", "CreateProdResources")
  }

  fun testConditionInIf() {
    assertEntityResolve("conditionInIf.template", "CreateProdResources")
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("resolve")
  }
}