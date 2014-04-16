package com.intellij.aws.cloudformation.metadata;

import java.util.ArrayList;
import java.util.List;

public class CloudFormationMetadata {
  public List<CloudFormationResourceType> resourceTypes = new ArrayList<CloudFormationResourceType>();
  public List<String> predefinedParameters = new ArrayList<String>();
  public CloudFormationLimits limits = new CloudFormationLimits();

  public CloudFormationResourceType findResourceType(String name) {
    for (CloudFormationResourceType resourceType : resourceTypes) {
      if (resourceType.name.equals(name)) {
        return resourceType;
      }
    }

    return null;
  }
}
