package com.intellij.aws.cloudformation.tests;

import com.intellij.aws.cloudformation.references.CloudFormationEntityReference;

public class EntityResolveTests extends ResolveTestsBase {
  public EntityResolveTests() {
    super(CloudFormationEntityReference.class);
  }

  public void testDependsOnSingle() throws Exception {
    assertEntityResolve("dependsOnSingle", "WebServerUser");
  }

  public void testDependsOnMulti() throws Exception {
    assertEntityResolve("dependsOnMulti", "WebServerUser1", "WebServerUser2");
  }

  public void testFindInMapRefToResource() throws Exception {
    assertEntityResolve("findInMapRefToResource", NotResolved);
  }

  public void testFindInMapRefToMapping() throws Exception {
    assertEntityResolve("findInMapRefToMapping", "AWSInstanceType2Arch");
  }

  public void testGetAttrParameterRef() throws Exception {
    assertEntityResolve("getAttrParameterRef", NotResolved);
  }

  public void testGetAttrResourceRef() throws Exception {
    assertEntityResolve("getAttrResourceRef", "WebServerUser");
  }

  public void testRefToResource() throws Exception {
    assertEntityResolve("refToResource", "WebServerUser");
  }

  public void testRefToParameter() throws Exception {
    assertEntityResolve("refToParameter", "WebServerInstanceType");
  }

  public void testRefToMapping() throws Exception {
    assertEntityResolve("refToMapping", NotResolved);
  }

  public void testRefToOutput() throws Exception {
    assertEntityResolve("refToOutput", NotResolved, NotResolved);
  }

  public void testConditionOnResource() throws Exception {
    assertEntityResolve("conditionOnResource", "CreateProdResources");
  }

  public void testCondition() throws Exception {
    assertEntityResolve("condition", "CreateProdResources");
  }

  public void testConditionInIf() throws Exception {
    assertEntityResolve("conditionInIf", "CreateProdResources");
  }

  @Override
  protected String getTestDataPath() {
    return TestUtil.getTestDataPath("resolve");
  }
}