package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.references.CloudFormationMappingTopLevelKeyReference

class MappingTopLevelKeyReferenceResolveTests : ResolveTestsBase(CloudFormationMappingTopLevelKeyReference::class.java) {
  fun testFindInMapRefToTopLevelKey() {
    assertEntityResolve("findInMapRefToTopLevelKey.template", "m2.xlarge")
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("topLevelKeyResolve")
  }
}