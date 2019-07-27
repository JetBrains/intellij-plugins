package com.intellij.aws.cloudformation.metadata

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.core.ClassLoaderReference
import com.thoughtworks.xstream.core.util.QuickWriter
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter
import com.thoughtworks.xstream.io.xml.StaxDriver
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer

object MetadataSerializer {
  private class CDataPrettyPrintWriter(out: Writer): PrettyPrintWriter(out) {
    internal var cdata = false

    override fun startNode(name: String, clazz: Class<*>?) {
      super.startNode(name, clazz)
      cdata = name == "description" || name == "name"
    }

    override fun writeText(writer: QuickWriter?, text: String) {
      writer!!.write("<![CDATA[")
      writer.write(text)
      writer.write("]]>")
    }
  }

  fun toXML(metadata: CloudFormationMetadata, output: OutputStream) {
    return createXStream().marshal(metadata, PrettyPrintWriter(OutputStreamWriter(output)))
  }

  fun toXML(descriptions: CloudFormationResourceTypesDescription, output: OutputStream) {
    return createXStream().marshal(descriptions, CDataPrettyPrintWriter(OutputStreamWriter(output)))
  }

  fun metadataFromXML(input: InputStream): CloudFormationMetadata {
    return createXStream().fromXML(input) as CloudFormationMetadata
  }

  fun descriptionsFromXML(input: InputStream): CloudFormationResourceTypesDescription {
    return createXStream().fromXML(input) as CloudFormationResourceTypesDescription
  }

  private fun createXStream(): XStream {
    val xstream = XStream(null, StaxDriver(), ClassLoaderReference(javaClass.classLoader))
    XStream.setupDefaultSecurity(xstream)

    val mapping = mapOf(
        Pair("Metadata", CloudFormationMetadata::class.java),
        Pair("ResourceType", CloudFormationResourceType::class.java),
        Pair("ResourceProperty", CloudFormationResourceProperty::class.java),
        Pair("ResourceAttribute", CloudFormationResourceAttribute::class.java),
        Pair("Limits", CloudFormationLimits::class.java),
        Pair("ResourceTypeDescription", CloudFormationResourceTypeDescription::class.java),
        Pair("ResourceTypesDescription", CloudFormationResourceTypesDescription::class.java)
    )

    xstream.allowTypes(mapping.map { it.value }.toTypedArray())
    mapping.forEach { (tag, clazz) -> xstream.alias(tag, clazz) }

    return xstream
  }
}
