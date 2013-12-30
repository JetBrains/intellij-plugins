package com.intellij.aws.cloudformation.tests;

import com.intellij.aws.cloudformation.CloudFormationEntityReference;

public class EntityResolveTests extends ResolveTestsBase {
  public EntityResolveTests() {
    super(CloudFormationEntityReference.class);
  }

  public void testDependsOnSingle() throws Exception {
    assertResolve("dependsOnSingle", "WebServerUser");
  }

  public void testDependsOnMulti() throws Exception {
    assertResolve("dependsOnMulti", "WebServerUser1", "WebServerUser2");
  }

  public void testFindInMapRefToResource() throws Exception {
    assertResolve("findInMapRefToResource", NotResolved);
  }

  public void testGetAttrParameterRef() throws Exception {
    assertResolve("getAttrParameterRef", NotResolved);
  }

  public void testGetAttrResourceRef() throws Exception {
    assertResolve("getAttrResourceRef", "WebServerUser");
  }

  public void testRefToResource() throws Exception {
    assertResolve("refToResource", "WebServerUser");
  }

  public void testRefToParameter() throws Exception {
    assertResolve("refToParameter", "WebServerInstanceType");
  }

  public void testRefToMapping() throws Exception {
    assertResolve("refToMapping", NotResolved);
  }

  public void testRefToOutput() throws Exception {
    assertResolve("refToOutput", NotResolved, NotResolved);
  }

  @Override
  protected String getTestDataPath() {
    return TestUtil.getTestDataPath("/resolve/");
  }
}