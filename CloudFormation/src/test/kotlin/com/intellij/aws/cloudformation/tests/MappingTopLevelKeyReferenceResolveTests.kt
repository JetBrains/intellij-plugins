package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.references.CloudFormationMappingFirstLevelKeyReference
import java.io.File

class MappingTopLevelKeyReferenceResolveTests : ResolveTestsBase(CloudFormationMappingFirstLevelKeyReference::class.java) {
  fun testFindInMapRefToTopLevelKey() {
    assertEntityResolve("findInMapRefToTopLevelKey.template", "m2.xlarge")
  }

  override val testDataRoot: File
    get() = TestUtil.getTestDataFile("topLevelKeyResolve")
}
