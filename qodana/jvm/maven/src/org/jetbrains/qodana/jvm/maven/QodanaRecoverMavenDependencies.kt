package org.jetbrains.qodana.jvm.maven

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.SystemProperties
import org.jetbrains.idea.maven.buildtool.MavenSyncSpec
import org.jetbrains.idea.maven.project.MavenProject
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenSimpleProjectComponent
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowCapability
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

const val QODANA_TRIGGER_MAVEN_IMPORT: String = "qodana.trigger.maven.import"

internal class QodanaRecoverMavenDependencies : QodanaWorkflowExtension {
  companion object {
    val LOG: Logger = Logger.getInstance(QodanaRecoverMavenDependencies::class.java)
  }

  override val dependsOn: Set<QodanaWorkflowCapability>
    get() = setOf(QODANA_MAVEN_SETTINGS_CONFIGURED)

  override suspend fun configureForQodana(config: QodanaConfig, project: Project) {
    if (SystemProperties.getBooleanProperty(QODANA_TRIGGER_MAVEN_IMPORT, false)) {
      return
    }
    if (!MavenSimpleProjectComponent.isNormalProjectInHeadless()) {
      return
    }

    val projectsManager = MavenProjectsManager.getInstance(project)
    val mavenProjects = projectsManager.projects
    if (mavenProjects.isEmpty()) {
      return
    }

    val projectsWithUnresolvedArtifacts = mavenProjects.filter { it.hasUnresolvedArtifacts() }
    if (projectsWithUnresolvedArtifacts.isEmpty()) {
      return
    }

    recoverMavenDependencies(mavenProjects, projectsWithUnresolvedArtifacts, projectsManager)
  }

  private suspend fun recoverMavenDependencies(
    mavenProjects: List<MavenProject>,
    projectsWithUnresolvedArtifacts: List<MavenProject>,
    projectsManager: MavenProjectsManager,
  ) {
    LOG.warn(
      "Detected unresolved Maven artifacts in headless project reuse for ${projectsWithUnresolvedArtifacts.map { it.displayName }}. " +
      "Running automatic Maven recovery sync for ${mavenProjects.map { it.displayName }}"
    )
    projectsManager.updateMavenProjects(
      MavenSyncSpec.full("QodanaRecoverMavenDependencies", true),
      mavenProjects.map { it.file },
      emptyList(),
    )
    LOG.info("Automatic Maven recovery sync completed")
  }
}
