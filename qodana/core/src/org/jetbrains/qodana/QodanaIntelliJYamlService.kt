package org.jetbrains.qodana

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.cloud.project.CloudOrganizationPrimaryData
import org.jetbrains.qodana.cloud.project.CloudProjectPrimaryData


class QodanaIntelliJYamlState : BaseState() {
  val defaultCloudProjectId by string()
  val defaultCloudOrganizationId by string()
  val disableApplyDefaultCloudProjectNotification by property(false)
  val disableOpenInIdeLinkNotification by property(false)

  val disableInspectionKtsResolve by property(false)
}

@Suppress("LightServiceMigrationCode")
class QodanaIntelliJYamlService : SimplePersistentStateComponent<QodanaIntelliJYamlState>(QodanaIntelliJYamlState())  {
  companion object {
    fun getInstance(project: Project) : QodanaIntelliJYamlService = project.service()
  }

  val disableOpenInIdeLinkNotification: Boolean
    get() = state.disableOpenInIdeLinkNotification

  val disableApplyDefaultCloudProjectNotification: Boolean
    get() = state.disableApplyDefaultCloudProjectNotification

  val disableInspectionKtsResolve: Boolean
    get() = state.disableInspectionKtsResolve

  val cloudProjectPrimaryData: CloudProjectPrimaryData?
    get() {
      val loadedState = state
      val defaultCloudProjectId = loadedState.defaultCloudProjectId ?: return null
      val defaultCloudOrganizationId = loadedState.defaultCloudOrganizationId ?: return null
      return CloudProjectPrimaryData(defaultCloudProjectId, CloudOrganizationPrimaryData(defaultCloudOrganizationId))
    }
}