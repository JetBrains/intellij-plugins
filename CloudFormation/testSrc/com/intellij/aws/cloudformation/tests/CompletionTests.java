package com.intellij.aws.cloudformation.tests;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.Arrays;
import java.util.List;

public class CompletionTests extends LightCodeInsightFixtureTestCase {
  public void testResourceType1() throws Exception {
    myFixture.configureByFiles("ResourceType1.template");
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> strings = myFixture.getLookupElementStrings();
    assertContainsElements(strings, "AWS::IAM::AccessKey");
  }

  public void testResourceType2() throws Exception {
    myFixture.configureByFiles("ResourceType2.template");
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> strings = myFixture.getLookupElementStrings();
    assertDoesntContain(strings, "AWS::IAM::AccessKey");
  }

  public void testResourceProperty1() throws Exception {
    myFixture.configureByFiles("ResourceProperty1.template");
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> strings = myFixture.getLookupElementStrings();
    assertSameElements(strings, Arrays.asList("ApplicationName", "ApplicationVersions"));
  }

  public void testDependsOn1() throws Exception {
    myFixture.configureByFiles("DependsOn1.template");
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> strings = myFixture.getLookupElementStrings();
    assertSameElements(strings, Arrays.asList("WebServerUser1"));
  }

  public void testDependsOn2() throws Exception {
    myFixture.configureByFiles("DependsOn2.template");
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> strings = myFixture.getLookupElementStrings();
    assertSameElements(strings, Arrays.asList("WebServerUser1", "WebServerUser2"));
  }

  public void testGetAtt1() throws Exception {
    myFixture.configureByFiles("GetAtt1.template");
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> strings = myFixture.getLookupElementStrings();
    assertSameElements(strings, Arrays.asList("Arn"));
  }

  public void testGetAtt2() throws Exception {
    myFixture.configureByFiles("GetAtt2.template");
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> strings = myFixture.getLookupElementStrings();
    assertSameElements(strings, Arrays.asList("ConfigurationEndpoint.Address", "ConfigurationEndpoint.Port"));
  }

  public void testResourceTopLevelProperty1() throws Exception {
    myFixture.configureByFiles("ResourceTopLevelProperty1.template");
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> strings = myFixture.getLookupElementStrings();
    assertSameElements(strings, Arrays.asList("Condition", "DeletionPolicy", "Metadata", "Properties", "UpdatePolicy"));
  }

  @Override
  protected String getTestDataPath() {
    return TestUtil.getTestDataPath("/completion/");
  }
}
