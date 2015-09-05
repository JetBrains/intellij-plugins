package com.intellij.aws.cloudformation.tests;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.util.BuildNumber;
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
    checkBasicCompletion("ResourceProperty1.template", "ApplicationName", "ApplicationVersions");
  }

  public void testDependsOn1() throws Exception {
    checkBasicCompletion("DependsOn1.template", "WebServerUser1");
  }

  public void testDependsOn2() throws Exception {
    checkBasicCompletion("DependsOn2.template", "WebServerUser1", "WebServerUser2");
  }

  public void testGetAtt1() throws Exception {
    checkBasicCompletion("GetAtt1.template", "Arn");
  }

  public void testGetAtt2() throws Exception {
    checkBasicCompletion("GetAtt2.template", "ConfigurationEndpoint.Address", "ConfigurationEndpoint.Port");
  }

  public void testResourceTopLevelProperty1() throws Exception {
    checkBasicCompletion("ResourceTopLevelProperty1.template", "Condition", "CreationPolicy", "DeletionPolicy", "Metadata", "Properties", "UpdatePolicy", "Version");
  }

  public void testPrefix1() throws Exception {
    if (!checkIdeaHasWeb11859Fixed()) {
      return;
    }

    myFixture.testCompletion("Prefix1.template", "Prefix1_after.template");
  }

  public void testPrefix2() throws Exception {
    myFixture.testCompletion("Prefix2.template", "Prefix2_after.template");
  }

  public void testPrefix3() throws Exception {
    if (!checkIdeaHasWeb11859Fixed()) {
      return;
    }

    checkBasicCompletion("Prefix3.template", "AWS::ElasticBeanstalk::Application", "AWS::ElasticBeanstalk::ApplicationVersion");
  }

  public void testPredefinedParameters() throws Exception {
    checkBasicCompletion("PredefinedParameters.template",
        "AWS::AccountId",
        "AWS::NoValue",
        "AWS::NotificationARNs",
        "AWS::Region",
        "AWS::StackId",
        "AWS::StackName");
  }

  public void testMappingTopLevelKey1() throws Exception {


    // checkBasicCompletion("MappingTopLevelKey1.template", "cc1.4xlarge", "cc2.8xlarge");
  }

  public void _testMappingTopLevelKey2() throws Exception {
    checkBasicCompletion("MappingTopLevelKey2.template", "m1.small", "t1.micro", "m2.small");
  }

  public void testMappingSecondLevelKey1() throws Exception {
    checkBasicCompletion("MappingSecondLevelKey1.template", "Arch", "Arch2");
  }

  public void _testMappingSecondLevelKey2() throws Exception {
    checkBasicCompletion("MappingSecondLevelKey2.template", "Arch", "Arch2", "Arch3");
  }

  public void _testMappingSecondLevelKey3() throws Exception {
    checkBasicCompletion("MappingSecondLevelKey3.template", "Arch", "Arch2", "Arch3", "A56", "A4");
  }

  private void checkBasicCompletion(String fileName, String... expectedElements) {
    myFixture.configureByFiles(fileName);
    myFixture.complete(CompletionType.BASIC, 1);
    List<String> strings = myFixture.getLookupElementStrings();
    assertSameElements(strings, Arrays.asList(expectedElements));
  }

  private static boolean checkIdeaHasWeb11859Fixed() {
    BuildNumber build = ApplicationInfo.getInstance().getBuild();
    if (build.compareTo(new BuildNumber("IU", 135, 670)) < 0) {
      System.out.println("fixed only by http://youtrack.jetbrains.com/issue/WEB-11859");
      return false;
    }

    return true;
  }

  @Override
  protected String getTestDataPath() {
    return TestUtil.getTestDataPath("completion");
  }
}
