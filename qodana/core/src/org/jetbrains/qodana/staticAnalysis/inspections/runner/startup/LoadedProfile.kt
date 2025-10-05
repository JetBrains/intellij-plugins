package org.jetbrains.qodana.staticAnalysis.inspections.runner.startup

import com.intellij.codeInspection.inspectionProfile.YamlInspectionConfigRaw
import com.intellij.codeInspection.inspectionProfile.YamlInspectionGroupRaw
import com.intellij.codeInspection.inspectionProfile.YamlInspectionProfileImpl
import com.intellij.codeInspection.inspectionProfile.YamlInspectionProfileRaw
import com.intellij.openapi.project.Project
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.inspectionKts.DynamicInspectionInitializer.Companion.waitForDynamicInspectionsInitialization
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
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

      val baseProfile = loadBaseProfile(config, project, messageReporter)
      val profileWithFullProfileSection = loadProfileSectionAsYamlProfile(baseProfile.profile, config.profile)
      return LoadedProfile(profileWithFullProfileSection, baseProfile.nameForReporting, baseProfile.pathForReporting)
    }
  }
}

private suspend fun loadBaseProfile(
  config: QodanaConfig,
  project: Project,
  messageReporter: QodanaMessageReporter
): LoadedProfile {
  return runInterruptible(StaticAnalysisDispatchers.IO) {
    val profileLoader = QodanaInspectionProfileLoader(project)

    //load from config
    val (name, path) = config.profile.base
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

private suspend fun loadProfileSectionAsYamlProfile(
  baseProfile: QodanaInspectionProfile,
  profile: QodanaProfileConfig
): QodanaInspectionProfile {
  val yamlInspectionProfileGroups = profile.groups.map {
    YamlInspectionGroupRaw(
      groupId = it.groupId,
      inspections = it.inspections,
      groups = it.groups,
    )
  }
  val yamlInspectionProfileInspections = profile.inspections.map {
    YamlInspectionConfigRaw(
      inspection = it.inspection,
      group = it.group,
      enabled = it.enabled,
      severity = it.severity,
      ignore = it.ignore,
      options = it.options,
    )
  }
  if (yamlInspectionProfileInspections.isEmpty() && yamlInspectionProfileGroups.isEmpty()) {
    return baseProfile
  }

  // operations with profiles could be blocking, so IO
  return runInterruptible(QodanaDispatchers.IO) {
    val yamlProfile = YamlInspectionProfileImpl.loadFromYamlRaw(
      yaml = YamlInspectionProfileRaw(
        baseProfile = null,
        name = null,
        yamlInspectionProfileGroups,
        yamlInspectionProfileInspections,
      ),
      baseProfile,
      baseProfile.inspectionToolsSupplier,
      baseProfile.profileManager,
    )
    val yamlInspectionProfile = yamlProfile.buildEffectiveProfile()
    QodanaInspectionProfile.clone(
      yamlInspectionProfile,
      "effective profile (based on global qodana.yaml)",
      baseProfile.profileManager
    )
  }
}
