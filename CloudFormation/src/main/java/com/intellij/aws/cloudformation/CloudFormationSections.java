package com.intellij.aws.cloudformation;

import java.util.Collection;
import java.util.Collections;

public class CloudFormationSections {
  public static final String Resources = "Resources";
  public static final String Parameters = "Parameters";
  public static final String Mappings = "Mappings";
  public static final String Metadata = "Metadata";
  public static final String Conditions = "Conditions";
  public static final String Outputs = "Outputs";

  public static final Collection<String> ResourcesSingletonList = Collections.singletonList(Resources);
  public static final Collection<String> ParametersSingletonList = Collections.singletonList(Parameters);
  public static final Collection<String> MappingsSingletonList = Collections.singletonList(Mappings);
  public static final Collection<String> MetadataSingletonList = Collections.singletonList(Metadata);
  public static final Collection<String> ConditionsSingletonList = Collections.singletonList(Conditions);
  public static final Collection<String> OutputsSingletonList = Collections.singletonList(Outputs);

  public static final String FormatVersion = "AWSTemplateFormatVersion";
  public static final String Description = "Description";
}
