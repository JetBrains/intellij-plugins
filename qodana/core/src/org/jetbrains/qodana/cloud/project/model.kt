package org.jetbrains.qodana.cloud.project

import org.jetbrains.annotations.Nls

data class CloudProjectPrimaryData(
  val id: String,
  val cloudOrganization: CloudOrganizationPrimaryData
)

data class CloudProjectProperties(
  @Nls val name: String?
)

fun getCloudProjectPresentableName(primaryData: CloudProjectPrimaryData, properties: CloudProjectProperties): @Nls String {
  return properties.name ?: primaryData.id
}

data class CloudProjectData(
  val primaryData: CloudProjectPrimaryData,
  val properties: CloudProjectProperties
)

class CloudOrganizationPrimaryData(
  val id: String
)