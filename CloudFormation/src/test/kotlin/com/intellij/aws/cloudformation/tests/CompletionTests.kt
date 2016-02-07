package com.intellij.aws.cloudformation.tests

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.util.BuildNumber
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import java.util.*

class CompletionTests : LightCodeInsightFixtureTestCase() {
  @Throws(Exception::class)
  fun testResourceType1() {
    myFixture.configureByFiles("ResourceType1.template")
    myFixture.complete(CompletionType.BASIC, 1)
    val strings = myFixture.lookupElementStrings
    UsefulTestCase.assertContainsElements(strings, "AWS::IAM::AccessKey")
  }

  @Throws(Exception::class)
  fun testResourceType2() {
    myFixture.configureByFiles("ResourceType2.template")
    myFixture.complete(CompletionType.BASIC, 1)
    val strings = myFixture.lookupElementStrings
    UsefulTestCase.assertDoesntContain(strings, "AWS::IAM::AccessKey")
  }

  @Throws(Exception::class)
  fun testResourceProperty1() {
    checkBasicCompletion("ResourceProperty1.template", "ApplicationName", "ApplicationVersions")
  }

  @Throws(Exception::class)
  fun testDependsOn1() {
    checkBasicCompletion("DependsOn1.template", "WebServerUser1")
  }

  @Throws(Exception::class)
  fun testDependsOn2() {
    checkBasicCompletion("DependsOn2.template", "WebServerUser1", "WebServerUser2")
  }

  @Throws(Exception::class)
  fun testGetAtt1() {
    checkBasicCompletion("GetAtt1.template", "Arn")
  }

  @Throws(Exception::class)
  fun testGetAtt2() {
    checkBasicCompletion("GetAtt2.template", "ConfigurationEndpoint.Address", "ConfigurationEndpoint.Port")
  }

  @Throws(Exception::class)
  fun testResourceTopLevelProperty1() {
    checkBasicCompletion("ResourceTopLevelProperty1.template", "Condition", "CreationPolicy", "DeletionPolicy", "Metadata", "Properties", "UpdatePolicy", "Version")
  }

  @Throws(Exception::class)
  fun testPrefix1() {
    if (!checkIdeaHasWeb11859Fixed()) {
      return
    }

    myFixture.testCompletion("Prefix1.template", "Prefix1_after.template")
  }

  @Throws(Exception::class)
  fun testPrefix2() {
    myFixture.testCompletion("Prefix2.template", "Prefix2_after.template")
  }

  @Throws(Exception::class)
  fun testPrefix3() {
    if (!checkIdeaHasWeb11859Fixed()) {
      return
    }

    checkBasicCompletion("Prefix3.template", "AWS::ElasticBeanstalk::Application", "AWS::ElasticBeanstalk::ApplicationVersion")
  }

  @Throws(Exception::class)
  fun testPredefinedParameters() {
    checkBasicCompletion("PredefinedParameters.template",
        "AWS::AccountId",
        "AWS::NoValue",
        "AWS::NotificationARNs",
        "AWS::Region",
        "AWS::StackId",
        "AWS::StackName")
  }

  @Throws(Exception::class)
  fun testMappingTopLevelKey1() {


    // checkBasicCompletion("MappingTopLevelKey1.template", "cc1.4xlarge", "cc2.8xlarge");
  }

  @Throws(Exception::class)
  fun _testMappingTopLevelKey2() {
    checkBasicCompletion("MappingTopLevelKey2.template", "m1.small", "t1.micro", "m2.small")
  }

  @Throws(Exception::class)
  fun testMappingSecondLevelKey1() {
    checkBasicCompletion("MappingSecondLevelKey1.template", "Arch", "Arch2")
  }

  @Throws(Exception::class)
  fun _testMappingSecondLevelKey2() {
    checkBasicCompletion("MappingSecondLevelKey2.template", "Arch", "Arch2", "Arch3")
  }

  @Throws(Exception::class)
  fun _testMappingSecondLevelKey3() {
    checkBasicCompletion("MappingSecondLevelKey3.template", "Arch", "Arch2", "Arch3", "A56", "A4")
  }

  private fun checkBasicCompletion(fileName: String, vararg expectedElements: String) {
    myFixture.configureByFiles(fileName)
    myFixture.complete(CompletionType.BASIC, 1)
    val strings = myFixture.lookupElementStrings
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
    return TestUtil.getTestDataPath("completion")
  }
}
