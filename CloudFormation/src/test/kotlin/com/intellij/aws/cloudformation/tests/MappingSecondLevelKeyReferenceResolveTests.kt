package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.references.CloudFormationMappingSecondLevelKeyReference
import java.io.File

class MappingSecondLevelKeyReferenceResolveTests : ResolveTestsBase(CloudFormationMappingSecondLevelKeyReference::class.java) {
  fun testFindInMapRefToSecondLevelKey() {
    assertEntityResolve("findInMapRefToSecondLevelKey.template", "Arch")
  }

  override val testDataRoot: File
    get() = TestUtil.getTestDataFile("secondLevelKeyResolve")
}
