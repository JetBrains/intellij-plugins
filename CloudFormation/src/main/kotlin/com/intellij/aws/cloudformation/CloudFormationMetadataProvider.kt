package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceTypesDescription
import com.intellij.aws.cloudformation.metadata.MetadataSerializer
import com.intellij.aws.cloudformation.metadata.awsServerless20161031ResourceTypes
import toResourceTypeBuilder
import java.util.*

object CloudFormationMetadataProvider {
  val METADATA: CloudFormationMetadata by lazy {
    val classLoader = CloudFormationMetadataProvider::class.java.classLoader
    val stream = classLoader.getResourceAsStream("com/intellij/aws/meta/cloudformation-metadata.xml")
                 ?: throw RuntimeException("Metadata resource is not found")

    stream.use {
      val metadataFromXML = MetadataSerializer.metadataFromXML(stream)

      val customBuilders = awsServerless20161031ResourceTypes
        .map { it.toResourceTypeBuilder() }
        .associate { Pair(it.name, it.toResourceType()) }

      val allResources = TreeMap(metadataFromXML.resourceTypes)
      allResources.putAll(customBuilders)

      CloudFormationMetadata(
        allResources,
        metadataFromXML.predefinedParameters,
        metadataFromXML.limits
      )
    }
  }

  val DESCRIPTIONS: CloudFormationResourceTypesDescription by lazy {
    val classLoader = CloudFormationMetadataProvider::class.java.classLoader
    val stream = classLoader.getResourceAsStream("com/intellij/aws/meta/cloudformation-descriptions.xml")
                 ?: throw RuntimeException("Descriptions resource is not found")

    stream.use {
      val descriptionsFromXML = MetadataSerializer.descriptionsFromXML(stream)

      val customDescriptions = awsServerless20161031ResourceTypes
        .map { it.toResourceTypeBuilder() }
        .associate { Pair(it.name, it.toResourceTypeDescription()) }

      val allResources = TreeMap(descriptionsFromXML.resourceTypes)
      allResources.putAll(customDescriptions)

      CloudFormationResourceTypesDescription(allResources)
    }
  }
}
