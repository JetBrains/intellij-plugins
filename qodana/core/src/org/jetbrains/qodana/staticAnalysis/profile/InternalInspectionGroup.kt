package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.util.InternalInspectionsProvider

internal const val INTERNAL_INSPECTION_GROUP_NAME = "internal"

/** The SARIF run property that holds the results of the internal inspections, as a list of SARIF results. */
internal const val INTERNAL_INSPECTIONS_RESULTS_KEY = "qodana.internal.results"

/**
 * The inspections named by [InternalInspectionsProvider], run on every file in scope. Qodana disables them in all its other groups,
 * see [withoutInspections], and puts their results to [INTERNAL_INSPECTIONS_RESULTS_KEY] instead of the report, so they affect
 * neither the quality gate nor the baseline.
 */
internal class InternalInspectionGroup(profile: QodanaInspectionProfile) : NamedInspectionGroup(INTERNAL_INSPECTION_GROUP_NAME, profile) {
  companion object {
    /** The group of the [inspectionIds] registered in [project], or `null` when none of them is. */
    fun create(project: Project, inspectionIds: Set<String>): InternalInspectionGroup? {
      if (inspectionIds.isEmpty()) return null
      val profileManager = QodanaInspectionProfileManager.getInstance(project)
      val profile = QodanaInspectionProfile.newWithDisabledTools("qodana.internal", profileManager)
      val registeredIds = inspectionIds.filter { profile.getToolsOrNull(it, project) != null }
      if (registeredIds.isEmpty()) return null
      registeredIds.forEach { profile.enableTool(it, project) }
      return InternalInspectionGroup(profile)
    }
  }

  override fun applyConfig(config: QodanaConfig, project: Project, addDefaultExclude: Boolean): InternalInspectionGroup {
    super.applyConfig(config, project, addDefaultExclude)
    val excludeModifiers = config.getExcludeModifiers(addDefaultExclude, project)
    if (excludeModifiers.isEmpty()) return this

    val profileManager = QodanaInspectionProfileManager.getInstance(project)
    val newProfile = QodanaInspectionProfile.clone(profile, "qodana.internal.profile(base:${profile.name})", profileManager)

    excludeModifiers.forEach { it.updateProfileScopes(newProfile, project, config.projectPath) }
    return InternalInspectionGroup(newProfile)
  }
}

/**
 * This profile with [inspectionIds] disabled: a copy under the same name when any of them is enabled, as the profile may be shared,
 * or else this profile itself.
 */
internal fun QodanaInspectionProfile.withoutInspections(inspectionIds: Set<String>, project: Project): QodanaInspectionProfile {
  val enabledIdsToDisable = inspectionIds.filter { getToolsOrNull(it, project)?.isEnabled == true }
  if (enabledIdsToDisable.isEmpty()) return this
  val copy = QodanaInspectionProfile.clone(this, name, QodanaInspectionProfileManager.getInstance(project))
  enabledIdsToDisable.forEach { copy.setToolEnabled(it, false, project) }
  return copy
}
