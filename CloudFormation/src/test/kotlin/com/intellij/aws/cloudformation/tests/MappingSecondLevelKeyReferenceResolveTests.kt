package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.references.CloudFormationMappingSecondLevelKeyReference

class MappingSecondLevelKeyReferenceResolveTests : ResolveTestsBase(CloudFormationMappingSecondLevelKeyReference::class.java) {
  @Throws(Exception::class)
  fun testFindInMapRefToSecondLevelKey() {
    assertEntityResolve("findInMapRefToSecondLevelKey", "Arch")
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("secondLevelKeyResolve")
  }
}