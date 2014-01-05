package com.intellij.aws.cloudformation;

import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata;
import com.intellij.aws.cloudformation.metadata.MetadataSerializer;

import java.io.IOException;
import java.io.InputStream;

public class CloudFormationMetadataProvider {
  public static CloudFormationMetadata METADATA = createMetadata();

  private static CloudFormationMetadata createMetadata() {
    final InputStream stream = CloudFormationMetadataProvider.class.getClassLoader().getResourceAsStream("cloudformation-metadata.xml");
    if (stream == null) {
      throw new RuntimeException("Metadata resource is not found");
    }

    try {
      try {
        return MetadataSerializer.fromXML(stream);
      }
      finally {
        stream.close();
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
