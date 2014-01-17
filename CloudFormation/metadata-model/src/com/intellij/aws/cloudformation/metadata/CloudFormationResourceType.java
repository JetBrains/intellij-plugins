package com.intellij.aws.cloudformation.metadata;

import java.util.ArrayList;
import java.util.List;

public class CloudFormationResourceType {
  public String name;
  public List<CloudFormationResourceProperty> properties = new ArrayList<CloudFormationResourceProperty>();
}
