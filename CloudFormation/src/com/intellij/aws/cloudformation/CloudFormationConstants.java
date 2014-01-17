package com.intellij.aws.cloudformation;

import com.intellij.util.containers.HashSet;

import java.util.Arrays;
import java.util.Set;

public class CloudFormationConstants {
  public static final Set<String> PredefinedParameters = new HashSet<String>(
    Arrays.asList(
      "AWS::AccountId",
      "AWS::NotificationARNs",
      "AWS::NoValue",
      "AWS::Region",
      "AWS::StackId",
      "AWS::StackName"
    ));

  public static final Set<String> SupportedTemplateFormatVersions = new HashSet<String>(Arrays.asList(
    "2010-09-09"
  ));

  public static final int MaxParameters = 50;
  public static final int MaxOutputs = 10;

  public static final String DependsOn = "DependsOn";

  public static final String TypePropertyName = "Type";
}
