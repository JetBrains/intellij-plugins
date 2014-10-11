package com.intellij.aws.cloudformation.metadata;

public class CloudFormationResourceAttribute {
  public String name;
  public String description;

  public static CloudFormationResourceAttribute create(String name, String description) {
    final CloudFormationResourceAttribute property = new CloudFormationResourceAttribute();
    property.name = name;
    property.description = description;
    return property;
  }
}
