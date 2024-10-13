package org.jetbrains.qodana.staticAnalysis.inspections.runner.startup

import com.intellij.openapi.project.Project
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.inspectionKts.waitForDynamicInspectionsInitialization
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaInspectionProfileLoader
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.profile.QODANA_BASE_PROFILE_NAME
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager

data class LoadedProfile(
  val profile: QodanaInspectionProfile,
  val nameForReporting: String,
  val pathForReporting: String,
) {
  companion object {
    suspend fun load(config: QodanaConfig, project: Project, messageReporter: QodanaMessageReporter): LoadedProfile {
      waitForDynamicInspectionsInitialization(project, messageReporter)

      return runInterruptible(StaticAnalysisDispatchers.IO) {
        val profileLoader = QodanaInspectionProfileLoader(project)

        //load from config
        val (path, name) = config.profile
        var profile = profileLoader.tryLoadProfileByNameOrPath(name, path, config.profileSource) {
          throw QodanaException(it)
        }
        if (profile != null) return@runInterruptible LoadedProfile(profile, name, path)

        //fallback
        profile = profileLoader.tryLoadProfileByNameOrPath(config.defaultProfileName, "", "qodana default inspection profile") {
          throw QodanaException(it)
        }
        if (profile != null) return@runInterruptible LoadedProfile(profile, "", "")

        val inspectionProfileManager = QodanaInspectionProfileManager.getInstance(project)
        profile = QodanaInspectionProfile.newWithEnabledByDefaultTools(QODANA_BASE_PROFILE_NAME, inspectionProfileManager)
        messageReporter.reportError("Using the default project profile")
        return@runInterruptible LoadedProfile(profile, "other", "")
      }
    }
  }
}
