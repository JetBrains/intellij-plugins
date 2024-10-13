// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.jvm.gradle

import com.intellij.openapi.externalSystem.model.ProjectKeys.LIBRARY
import com.intellij.openapi.externalSystem.model.project.LibraryData
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.plugins.gradle.util.GradleConstants.GRADLE_NAME
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaProjectDescriber

class GradleProjectDescriber : QodanaProjectDescriber {
  override val id: String = GRADLE_NAME

  override suspend fun description(project: Project): GradleDescription {
    val projectSettings = project.basePath?.let { GradleSettings.getInstance(project).getLinkedProjectSettings(it) }

    val externalProjects = ProjectDataManager.getInstance().getExternalProjectsData(project, GradleConstants.SYSTEM_ID)
    val libraries = mutableListOf<LibraryDescription>()
    externalProjects.forEach { projectInfo ->
      projectInfo.externalProjectStructure?.visit {
        if (LIBRARY == it.key) {
          val libraryData = it.data as LibraryData
          libraries.add(LibraryDescription(libraryData))
        }
      }
    }
    return GradleDescription(projectSettings?.gradleJvm, libraries)
  }

  @Suppress("unused")
  class GradleDescription(val gradleJvm: String?, val libraries: List<LibraryDescription>)

  @Suppress("unused")
  class LibraryDescription(data: LibraryData) {
    val ideaName = data.internalName
    val gradleName = data.externalName
    val unresolved = data.isUnresolved
    val groupId = data.groupId
    val artifactId = data.artifactId
    val version = data.version
  }
}