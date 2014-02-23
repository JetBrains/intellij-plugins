package com.intellij.aws.cloudformation.tests;

import com.intellij.aws.cloudformation.references.CloudFormationMappingSecondLevelKeyReference;

public class MappingSecondLevelKeyReferenceResolveTests extends ResolveTestsBase {
  public MappingSecondLevelKeyReferenceResolveTests() {
    super(CloudFormationMappingSecondLevelKeyReference.class);
  }

  public void testFindInMapRefToTopLevelKey() throws Exception {
    assertEntityResolve("findInMapRefToSecondLevelKey", "Arch");
  }

  @Override
  protected String getTestDataPath() {
    return TestUtil.getTestDataPath("/secondLevelKeyResolve/");
  }
}