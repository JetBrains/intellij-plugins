package com.intellij.aws.cloudformation.metadata

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter
import com.thoughtworks.xstream.io.xml.StaxDriver

import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter

object MetadataSerializer {
  fun toXML(metadata: CloudFormationMetadata, output: OutputStream) {
    createXStream().marshal(metadata, PrettyPrintWriter(OutputStreamWriter(output)))
  }

  fun fromXML(input: InputStream): CloudFormationMetadata {
    return createXStream().fromXML(input) as CloudFormationMetadata
  }

  private fun createXStream(): XStream {
    val xstream = XStream(StaxDriver())
    xstream.alias("Metadata", CloudFormationMetadata::class.java)
    xstream.alias("ResourceType", CloudFormationResourceType::class.java)
    xstream.alias("ResourceProperty", CloudFormationResourceProperty::class.java)
    xstream.alias("ResourceAttribute", CloudFormationResourceAttribute::class.java)
    xstream.alias("Limits", CloudFormationLimits::class.java)

    return xstream
  }
}
