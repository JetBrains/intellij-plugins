package com.intellij.aws.cloudformation.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CloudFormationResourceType {
  public String name;
  public List<CloudFormationResourceProperty> properties = new ArrayList<CloudFormationResourceProperty>();
  public List<CloudFormationResourceAttribute> attributes = new ArrayList<CloudFormationResourceAttribute>();

  public CloudFormationResourceProperty findProperty(String name) {
    for (CloudFormationResourceProperty property : properties) {
      if (property.name.equals(name)) {
        return property;
      }
    }

    return null;
  }

  public Set<String> getRequiredProperties() {
    Set<String> requiredProperties = new HashSet<String>();
    for (CloudFormationResourceProperty property : properties) {
      if (property.required) {
        requiredProperties.add(property.name);
      }
    }

    return requiredProperties;
  }
}
