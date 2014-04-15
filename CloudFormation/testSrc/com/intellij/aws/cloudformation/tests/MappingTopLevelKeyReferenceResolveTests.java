package com.intellij.aws.cloudformation.tests;

import com.intellij.aws.cloudformation.references.CloudFormationMappingTopLevelKeyReference;

public class MappingTopLevelKeyReferenceResolveTests extends ResolveTestsBase {
  public MappingTopLevelKeyReferenceResolveTests() {
    super(CloudFormationMappingTopLevelKeyReference.class);
  }

  public void testFindInMapRefToTopLevelKey() throws Exception {
    assertEntityResolve("findInMapRefToTopLevelKey", "m2.xlarge");
  }

  @Override
  protected String getTestDataPath() {
    return TestUtil.getTestDataPath("topLevelKeyResolve");
  }
}