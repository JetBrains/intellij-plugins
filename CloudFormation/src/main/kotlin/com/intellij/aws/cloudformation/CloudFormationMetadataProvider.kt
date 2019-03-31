package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceTypesDescription
import com.intellij.aws.cloudformation.metadata.MetadataSerializer

object CloudFormationMetadataProvider {
  val METADATA: CloudFormationMetadata by lazy {
    val stream = CloudFormationMetadataProvider::class.java.classLoader.getResourceAsStream("cloudformation-metadata.xml")
        ?: throw RuntimeException("Metadata resource is not found")

    stream.use {
      MetadataSerializer.metadataFromXML(stream)
    }
  }

  val DESCRIPTIONS: CloudFormationResourceTypesDescription by lazy {
    val stream = CloudFormationMetadataProvider::class.java.classLoader.getResourceAsStream("cloudformation-descriptions.xml")
        ?: throw RuntimeException("Descriptions resource is not found")

    stream.use {
      MetadataSerializer.descriptionsFromXML(stream)
    }
  }
}
