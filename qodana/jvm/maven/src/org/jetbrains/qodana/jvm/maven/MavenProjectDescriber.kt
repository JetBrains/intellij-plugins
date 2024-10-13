// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.jvm.maven

import com.intellij.openapi.project.Project
import org.jetbrains.idea.maven.model.MavenArtifact
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenUtil.MAVEN_NAME
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaProjectDescriber

class MavenProjectDescriber : QodanaProjectDescriber {

  override val id: String = MAVEN_NAME

  override suspend fun description(project: Project): MavenDescription {
    val projects = MavenProjectsManager.getInstance(project).projects
    val libraries = projects.flatMap {
      it.dependencies
    }.map { LibraryDescription(it) }

    return MavenDescription(libraries)
  }

  class MavenDescription(val libraries: List<LibraryDescription>)

  @Suppress("unused")
  class LibraryDescription(data: MavenArtifact) {
    val ideaName: String = data.libraryName
    val unresolved = !data.isResolved
    val groupId: String = data.groupId
    val artifactId: String = data.artifactId
    val version: String? = data.version
    val baseVersion: String? = data.baseVersion
    val type: String? = data.type
    val classifier: String? = data.classifier
    val optional = data.isOptional
    val scope: String? = data.scope
  }
}