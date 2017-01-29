package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.references.CloudFormationMappingSecondLevelKeyReference

class MappingSecondLevelKeyReferenceResolveTests : ResolveTestsBase(CloudFormationMappingSecondLevelKeyReference::class.java) {
  fun testFindInMapRefToSecondLevelKey() {
    assertEntityResolve("findInMapRefToSecondLevelKey.template", "Arch")
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("secondLevelKeyResolve")
  }
}