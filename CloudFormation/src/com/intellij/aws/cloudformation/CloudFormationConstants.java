package com.intellij.aws.cloudformation;

import com.intellij.util.containers.HashSet;

import java.util.Arrays;
import java.util.Set;

public class CloudFormationConstants {
  public static final Set<String> SupportedTemplateFormatVersions = new HashSet<String>(Arrays.asList(
    "2010-09-09"
  ));

  public static final int MaxParameters = 50;
  public static final int MaxOutputs = 10;

  public static final String ConditionPropertyName = "Condition";
  public static final String TypePropertyName = "Type";
  public static final String PropertiesPropertyName = "Properties";
  public static final String DeletionPolicyPropertyName = "DeletionPolicy";
  public static final String DependsOnPropertyName = "DependsOn";
  public static final String MetadataPropertyName = "Metadata";
  public static final String UpdatePolicyPropertyName = "UpdatePolicy";

  public static final Set<String> AllTopLevelResourceProperties = new HashSet<String>(Arrays.asList(
    ConditionPropertyName,
    TypePropertyName,
    PropertiesPropertyName,
    DeletionPolicyPropertyName,
    DependsOnPropertyName,
    MetadataPropertyName,
    UpdatePolicyPropertyName
  ));
}
