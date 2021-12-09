package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.metadata.awsServerlessApi
import com.intellij.aws.cloudformation.metadata.awsServerlessFunction
import com.intellij.aws.cloudformation.metadata.awsServerlessSimpleTable

object CloudFormationConstants {
  val SupportedTemplateFormatVersions = setOf("2010-09-09")

  const val awsServerless20161031TransformName = "AWS::Serverless-2016-10-31"

  // from https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/parameters-section-structure.html
  const val ParameterDescriptionLimit = 4000
  const val ParameterTypePropertyName = "Type"

  const val CloudFormationInterfaceType = "AWS::CloudFormation::Interface"
  const val CloudFormationInterfaceParameterLabels = "ParameterLabels"
  const val CloudFormationInterfaceParameterGroups = "ParameterGroups"
  const val CloudFormationInterfaceParameters = "Parameters"

  const val CustomResourceType = "AWS::CloudFormation::CustomResource"
  const val CustomResourceTypePrefix = "Custom::"

  const val CommentResourcePropertyName = "Comment"

  const val ConditionPropertyName = "Condition"
  const val TypePropertyName = "Type"
  const val PropertiesPropertyName = "Properties"
  const val CreationPolicyPropertyName = "CreationPolicy"
  const val DeletionPolicyPropertyName = "DeletionPolicy"
  const val DescriptionPropertyName = "Description"
  const val DependsOnPropertyName = "DependsOn"
  const val MetadataPropertyName = "Metadata"
  const val UpdatePolicyPropertyName = "UpdatePolicy"
  const val UpdateReplacePolicyPropertyName = "UpdateReplacePolicy"
  const val VersionPropertyName = "Version"

  // https://github.com/awslabs/serverless-application-model/blob/master/docs/globals.rst#supported-resources
  val GlobalsResourcesMap = mapOf(
      "Function" to awsServerlessFunction,
      "Api" to awsServerlessApi,
      "SimpleTable" to awsServerlessSimpleTable
  )

  val AllTopLevelResourceProperties = listOf(
      ConditionPropertyName,
      TypePropertyName,
      PropertiesPropertyName,
      CreationPolicyPropertyName,
      DescriptionPropertyName,
      DeletionPolicyPropertyName,
      DependsOnPropertyName,
      MetadataPropertyName,
      UpdatePolicyPropertyName,
      UpdateReplacePolicyPropertyName,
      VersionPropertyName
  ).toSet()

  // TODO fetch from https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/parameters-section-structure.html
  val AwsSpecificParameterTypes = listOf(
      "AWS::EC2::AvailabilityZone::Name",
      "AWS::EC2::Image::Id",
      "AWS::EC2::Instance::Id",
      "AWS::EC2::KeyPair::KeyName",
      "AWS::EC2::SecurityGroup::GroupName",
      "AWS::EC2::SecurityGroup::Id",
      "AWS::EC2::Subnet::Id",
      "AWS::EC2::Volume::Id",
      "AWS::EC2::VPC::Id",
      "AWS::Route53::HostedZone::Id",
      "List<AWS::EC2::AvailabilityZone::Name>",
      "List<AWS::EC2::Image::Id>",
      "List<AWS::EC2::Instance::Id>",
      "List<AWS::EC2::SecurityGroup::GroupName>",
      "List<AWS::EC2::SecurityGroup::Id>",
      "List<AWS::EC2::Subnet::Id>",
      "List<AWS::EC2::Volume::Id>",
      "List<AWS::EC2::VPC::Id>",
      "List<AWS::Route53::HostedZone::Id>"
  )

  val SsmParameterTypes = listOf(
      "AWS::SSM::Parameter::Name",
      "AWS::SSM::Parameter::Value<String>",
      "AWS::SSM::Parameter::Value<List<String>>",
      "AWS::SSM::Parameter::Value<CommaDelimitedList>") +
      AwsSpecificParameterTypes.map { "AWS::SSM::Parameter::Value<$it>" } +
      AwsSpecificParameterTypes.map { "AWS::SSM::Parameter::Value<List<$it>>" }

  val allParameterTypes = (CloudFormationParameterType.allIds + AwsSpecificParameterTypes + SsmParameterTypes).sorted()
}
