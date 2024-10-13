package org.jetbrains.qodana.cloud.project

import org.jetbrains.qodana.cloud.UserState

/**
 * State of the link of IDE project with Qodana Cloud project
 *
 * See [QodanaCloudProjectLinkService]
 */
sealed interface LinkState {
  interface Linked : LinkState {
    val authorized: UserState.Authorized

    val projectDataProvider: QodanaCloudProjectDataProvider

    val cloudReportDescriptorBuilder: CloudReportDescriptorBuilder

    fun unlink(): NotLinked?
  }

  interface NotLinked : LinkState {
    fun linkWithQodanaCloudProject(authorized: UserState.Authorized, cloudProjectData: CloudProjectData): Linked?
  }
}