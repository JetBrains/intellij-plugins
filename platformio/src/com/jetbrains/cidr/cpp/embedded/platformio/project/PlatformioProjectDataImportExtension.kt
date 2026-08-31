package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.service.project.manage.ContentRootDataService
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataService

internal class PlatformioProjectDataImportExtension: ProjectDataManager.ProjectDataImportExtension {
  override fun <E, I> ignoreDataService(
    service: ProjectDataService<E?, I?>,
    projectSystemId: ProjectSystemId?,
  ): Boolean {
    return projectSystemId == ID && service is ContentRootDataService
  }
}