package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationConstants
import com.intellij.aws.cloudformation.CloudFormationMetadataProvider
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.builders.ModuleFixtureBuilder
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.ModuleFixture
import java.util.Arrays

class YamlCompletionTests : CodeInsightFixtureTestCase<ModuleFixtureBuilder<ModuleFixture>>() {
  val predefinedAndECSCluster = (CloudFormationMetadataProvider.METADATA.predefinedParameters + "ECSCluster").toTypedArray()
  private fun Array<String>.withQuotes(quote: String): Array<String> = this.map { "$quote$it$quote" }.toTypedArray()

  fun testRefNoQuotes() = checkBasicCompletion("ref_no_quotes.yaml", *predefinedAndECSCluster)
  fun testRefSingleQuotes() = checkBasicCompletion("ref_single_quotes.yaml", *predefinedAndECSCluster)
  fun testRefSingleQuotes2() = checkBasicCompletion("ref_single_quotes_2.yaml", *predefinedAndECSCluster)
  fun testRefSingleQuotes3() = checkBasicCompletion("ref_single_quotes_3.yaml", *predefinedAndECSCluster.withQuotes("'"))
  fun testRefDoubleQuotes() = checkBasicCompletion("ref_double_quotes.yaml", *predefinedAndECSCluster.withQuotes("\""))
  fun testRefDoubleQuotes2() = checkBasicCompletion("ref_double_quotes_2.yaml", *predefinedAndECSCluster)
  fun testRefDoubleQuotes3() = checkBasicCompletion("ref_double_quotes_3.yaml", *predefinedAndECSCluster.withQuotes("\""))

  fun testResourceProperty1() = checkBasicCompletion("resource_property_1.yaml",
      "ApplicationName", "ApplicationVersions", "ConfigurationTemplates",
      "Description", "ResourceLifecycleConfig")
  fun testResourceProperty2() = checkBasicCompletion("resource_property_2.yaml",
      "ApplicationName", "ApplicationVersions", "ConfigurationTemplates",
      "Description", "ResourceLifecycleConfig")

  fun testProperty1() = checkBasicCompletion("resource_1.yaml",
      "Condition", "CreationPolicy", "DeletionPolicy", "DependsOn",
      "Description", "Metadata", "Properties", "UpdatePolicy", "UpdateReplacePolicy", "Version")

  fun testParameterProperty() = checkBasicCompletion("parameter_property.yaml",
      "AllowedPattern", "AllowedValues", "ConstraintDescription",
      "Default", "MaxLength", "MaxValue", "MinLength", "MinValue", "NoEcho")
  fun testParameterType() = checkBasicCompletion("parameter_type.yaml",
      "AWS::SSM::Parameter::Value<List<String>>",
      "AWS::SSM::Parameter::Value<String>", "String", "List<String>")
  fun testParameterType2() = checkBasicCompletion("parameter_type_2.yaml",
      *CloudFormationConstants.allParameterTypes.toTypedArray())
  fun testParameterType3() = checkBasicCompletion("parameter_type_3.yaml")

  fun testServerless1() = checkBasicCompletion("serverless_1.yaml",
      "AutoPublishAlias", "CodeUri", "DeadLetterQueue", "DeploymentPreference", "InlineCode", "PermissionsBoundary",
      "Description", "Environment", "Events", "FunctionName", "KmsKeyArn", "MemorySize",
      "Policies", "VersionDescription", "ReservedConcurrentExecutions", "Role", "Layers", "Tags", "Timeout", "Tracing", "VpcConfig")
  fun testServerless2() = checkBasicCompletion("serverless_2.yaml",
      "AWS::Serverless::Api", "AWS::Serverless::Function", "AWS::Serverless::SimpleTable", "AWS::Serverless::Application", "AWS::Serverless::LayerVersion")
  fun testServerless3() = checkBasicCompletion("serverless_3.yaml")

  private fun checkBasicCompletion(fileName: String, vararg expectedElements: String) {
    myFixture.configureByFiles(fileName)
    myFixture.complete(CompletionType.BASIC, 1)
    val strings = myFixture.lookupElementStrings!!
    UsefulTestCase.assertSameElements(strings, Arrays.asList(*expectedElements))
  }

  override fun setUp() {
    super.setUp()
    myFixture.testDataPath = TestUtil.getTestDataPath("completion/yaml")
  }
}
