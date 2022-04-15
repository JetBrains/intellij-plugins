package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceTypesDescription
import com.intellij.aws.cloudformation.metadata.MetadataSerializer

object CloudFormationMetadataProvider {
  val METADATA: CloudFormationMetadata by lazy {
    val classLoader = CloudFormationMetadataProvider::class.java.classLoader
    val stream = classLoader.getResourceAsStream("com/intellij/aws/meta/cloudformation-metadata.xml")
                 ?: throw RuntimeException("Metadata resource is not found")

    stream.use {
      MetadataSerializer.metadataFromXML(stream)
    }
  }

  val DESCRIPTIONS: CloudFormationResourceTypesDescription by lazy {
    val classLoader = CloudFormationMetadataProvider::class.java.classLoader
    val stream = classLoader.getResourceAsStream("com/intellij/aws/meta/cloudformation-descriptions.xml")
                 ?: throw RuntimeException("Descriptions resource is not found")

    stream.use {
      MetadataSerializer.descriptionsFromXML(stream)
    }
  }
}
