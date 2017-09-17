package com.intellij.aws.cloudformation.tests

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.util.BuildNumber
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import java.util.Arrays

class JsonCompletionTests : LightCodeInsightFixtureTestCase() {
  fun testResourceType1() {
    myFixture.configureByFiles("ResourceType1.template")
    myFixture.complete(CompletionType.BASIC, 1)
    val strings = myFixture.lookupElementStrings!!
    UsefulTestCase.assertContainsElements(strings, "AWS::IAM::AccessKey")
  }

  fun testResourceType2() {
    myFixture.configureByFiles("ResourceType2.template")
    myFixture.complete(CompletionType.BASIC, 1)
    val strings = myFixture.lookupElementStrings!!
    UsefulTestCase.assertDoesntContain(strings, "AWS::IAM::AccessKey")
  }

  fun testCloudFormationInterfaceParameterGroups() {
    checkBasicCompletion("CloudFormationInterfaceParameterGroups.template", "WebServerInstanceType1", "WebServerInstanceType2")
  }

  fun testCloudFormationInterfaceParameterLabels() {
    checkBasicCompletion("CloudFormationInterfaceParameterLabels.template", "WebServer12InstanceType1", "WebServer12InstanceType2")
  }

  fun testResourceProperty1() {
    checkBasicCompletion("ResourceProperty1.template", "ApplicationName", "ApplicationVersions")
  }

  fun testDependsOn1() {
    checkBasicCompletion("DependsOn1.template", "WebServerUser1")
  }

  fun testDependsOn1Bare() {
    checkBasicCompletion("DependsOn1_bare.template", "\"WebServerUser1\"")
  }

  fun testDependsOn2() {
    checkBasicCompletion("DependsOn2.template", "WebServerUser1", "WebServerUser2")
  }

  fun testGetAtt1() {
    checkBasicCompletion("GetAtt1.template", "Arn")
  }

  fun testGetAtt2() {
    checkBasicCompletion("GetAtt2.template",
        "ConfigurationEndpoint.Address", "ConfigurationEndpoint.Port",
        "RedisEndpoint.Address", "RedisEndpoint.Port")
  }

  fun testResourceTopLevelProperty1() {
    checkBasicCompletion("ResourceTopLevelProperty1.template", "Condition", "CreationPolicy", "DeletionPolicy", "Metadata", "Properties", "UpdatePolicy", "Version")
  }

  fun testPrefix1() {
    if (!checkIdeaHasWeb11859Fixed()) {
      return
    }

    myFixture.testCompletion("Prefix1.template", "Prefix1_after.template")
  }

  fun testPrefix2() {
    myFixture.testCompletion("Prefix2.template", "Prefix2_after.template")
  }

  fun testPrefix3() {
    if (!checkIdeaHasWeb11859Fixed()) {
      return
    }

    checkBasicCompletion("Prefix3.template", "AWS::ElasticBeanstalk::Application", "AWS::ElasticBeanstalk::ApplicationVersion")
  }

  fun testPredefinedParameters() {
    checkBasicCompletion("PredefinedParameters.template",
        "AWS::AccountId",
        "AWS::NoValue",
        "AWS::NotificationARNs",
        "AWS::Partition",
        "AWS::Region",
        "AWS::StackId",
        "AWS::StackName",
        "AWS::URLSuffix")
  }

  fun testMappingTopLevelKey1() {


    // checkBasicCompletion("MappingTopLevelKey1.template", "cc1.4xlarge", "cc2.8xlarge");
  }

  fun _testMappingTopLevelKey2() {
    checkBasicCompletion("MappingTopLevelKey2.template", "m1.small", "t1.micro", "m2.small")
  }

  fun testMappingSecondLevelKey1() {
    checkBasicCompletion("MappingSecondLevelKey1.template", "Arch", "Arch2")
  }

  fun _testMappingSecondLevelKey2() {
    checkBasicCompletion("MappingSecondLevelKey2.template", "Arch", "Arch2", "Arch3")
  }

  fun _testMappingSecondLevelKey3() {
    checkBasicCompletion("MappingSecondLevelKey3.template", "Arch", "Arch2", "Arch3", "A56", "A4")
  }

  private fun checkBasicCompletion(fileName: String, vararg expectedElements: String) {
    myFixture.configureByFiles(fileName)
    myFixture.complete(CompletionType.BASIC, 1)
    val strings = myFixture.lookupElementStrings!!
    UsefulTestCase.assertSameElements(strings, Arrays.asList(*expectedElements))
  }

  private fun checkIdeaHasWeb11859Fixed(): Boolean {
    val build = ApplicationInfo.getInstance().build
    if (build.compareTo(BuildNumber("IU", 135, 670)) < 0) {
      println("fixed only by http://youtrack.jetbrains.com/issue/WEB-11859")
      return false
    }

    return true
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("completion/json")
  }
}
