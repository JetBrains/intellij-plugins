package com.intellij.aws.cloudformation.metadata;

public class CloudFormationResourceProperty {
  public String name;
  public String description;
  public String type;
  public boolean required;

  public static CloudFormationResourceProperty create(String name, String description, String type, boolean required) {
    final CloudFormationResourceProperty property = new CloudFormationResourceProperty();
    property.name = name;
    property.description = description;
    property.type = type;
    property.required = required;
    return property;
  }
}
