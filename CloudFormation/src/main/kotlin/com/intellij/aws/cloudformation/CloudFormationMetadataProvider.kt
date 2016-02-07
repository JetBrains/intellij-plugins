package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata
import com.intellij.aws.cloudformation.metadata.MetadataSerializer
import java.io.IOException

object CloudFormationMetadataProvider {
  var METADATA = createMetadata()

  private fun createMetadata(): CloudFormationMetadata {
    val stream = CloudFormationMetadataProvider::class.java.classLoader.getResourceAsStream("cloudformation-metadata.xml") ?: throw RuntimeException("Metadata resource is not found")

    try {
      try {
        return MetadataSerializer.fromXML(stream)
      } finally {
        stream.close()
      }
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
  }
}
