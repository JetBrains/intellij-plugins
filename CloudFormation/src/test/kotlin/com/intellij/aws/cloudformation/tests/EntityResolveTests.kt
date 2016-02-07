package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.references.CloudFormationEntityReference

class EntityResolveTests : ResolveTestsBase(CloudFormationEntityReference::class.java) {

  @Throws(Exception::class)
  fun testDependsOnSingle() {
    assertEntityResolve("dependsOnSingle", "WebServerUser")
  }

  @Throws(Exception::class)
  fun testDependsOnMulti() {
    assertEntityResolve("dependsOnMulti", "WebServerUser1", "WebServerUser2")
  }

  @Throws(Exception::class)
  fun testFindInMapRefToResource() {
    assertEntityResolve("findInMapRefToResource", NotResolved)
  }

  @Throws(Exception::class)
  fun testFindInMapRefToMapping() {
    assertEntityResolve("findInMapRefToMapping", "AWSInstanceType2Arch")
  }

  @Throws(Exception::class)
  fun testGetAttrParameterRef() {
    assertEntityResolve("getAttrParameterRef", NotResolved)
  }

  @Throws(Exception::class)
  fun testGetAttrResourceRef() {
    assertEntityResolve("getAttrResourceRef", "WebServerUser")
  }

  @Throws(Exception::class)
  fun testRefToResource() {
    assertEntityResolve("refToResource", "WebServerUser")
  }

  @Throws(Exception::class)
  fun testRefToParameter() {
    assertEntityResolve("refToParameter", "WebServerInstanceType")
  }

  @Throws(Exception::class)
  fun testRefToMapping() {
    assertEntityResolve("refToMapping", NotResolved)
  }

  @Throws(Exception::class)
  fun testRefToOutput() {
    assertEntityResolve("refToOutput", NotResolved, NotResolved)
  }

  @Throws(Exception::class)
  fun testConditionOnResource() {
    assertEntityResolve("conditionOnResource", "CreateProdResources")
  }

  @Throws(Exception::class)
  fun testCondition() {
    assertEntityResolve("condition", "CreateProdResources")
  }

  @Throws(Exception::class)
  fun testConditionInIf() {
    assertEntityResolve("conditionInIf", "CreateProdResources")
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("resolve")
  }
}