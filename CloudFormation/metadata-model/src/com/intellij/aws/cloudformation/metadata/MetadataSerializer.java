package com.intellij.aws.cloudformation.metadata;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MetadataSerializer {
  public static void toXML(CloudFormationMetadata metadata, OutputStream output) throws IOException {
    createXStream().marshal(metadata, new PrettyPrintWriter(new OutputStreamWriter(output)));
  }

  public static CloudFormationMetadata fromXML(InputStream input) throws IOException {
    return (CloudFormationMetadata)createXStream().fromXML(input);
  }

  private static XStream createXStream() {
    final XStream xstream = new XStream(new StaxDriver());
    xstream.alias("Metadata", CloudFormationMetadata.class);
    xstream.alias("ResourceType", CloudFormationResourceType.class);
    xstream.alias("ResourceProperty", CloudFormationResourceProperty.class);

    return xstream;
  }
}
