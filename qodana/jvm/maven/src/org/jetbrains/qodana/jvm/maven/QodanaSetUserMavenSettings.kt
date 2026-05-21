package org.jetbrains.qodana.jvm.maven

import com.intellij.ide.impl.OpenProjectTaskBuilder
import com.intellij.openapi.project.Project
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.project.MavenWorkspaceSettingsComponent
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowCapability
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import org.jetbrains.qodana.staticAnalysis.workflow.appendBeforeOpen

internal val QODANA_MAVEN_SETTINGS_CONFIGURED: QodanaWorkflowCapability = QodanaWorkflowCapability("qodana.jvm.maven.settings.configured")

internal class QodanaSetUserMavenSettings : QodanaWorkflowExtension {
  override val provides: Set<QodanaWorkflowCapability>
    get() = setOf(QODANA_MAVEN_SETTINGS_CONFIGURED)

  override suspend fun configureProjectOpening(config: QodanaConfig, openProjectTaskBuilder: OpenProjectTaskBuilder) {
    val mavenSettingsPath = config.mavenSettingsPath ?: return
    openProjectTaskBuilder.appendBeforeOpen { project ->
      setUserMavenSettings(project, mavenSettingsPath.toString())
      true
    }
  }

  override suspend fun configureForQodana(config: QodanaConfig, project: Project) {
    val mavenSettingsPath = config.mavenSettingsPath ?: return
    setUserMavenSettings(project, mavenSettingsPath.toString())
  }

  private fun setUserMavenSettings(project: Project, mavenSettingsPath: String) {
    MavenWorkspaceSettingsComponent.getInstance(project)
      .settings
      .generalSettings
      .setUserSettingsFile(mavenSettingsPath)
    MavenProjectsManager.getInstance(project).generalSettings.setUserSettingsFile(mavenSettingsPath)
  }
}
