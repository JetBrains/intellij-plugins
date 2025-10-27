package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext

internal class SanityInspectionGroup(name: String, profile: QodanaInspectionProfile) : NamedInspectionGroup(name, profile) {
  companion object {
    const val SANITY_FAILURE_NOTIFICATION = "sanityFailure"
  }

  override fun createState(context: QodanaGlobalInspectionContext): GroupState = StateWithThreshold(context, this)

  override fun applyConfig(config: QodanaConfig, project: Project, addDefaultExclude: Boolean): SanityInspectionGroup {
    super.applyConfig(config, project, addDefaultExclude)
    val excludeModifiers = config.getExcludeModifiers(addDefaultExclude, project)
    if (excludeModifiers.isEmpty()) return this

    val profileManager = QodanaInspectionProfileManager.getInstance(project)
    val newProfile = QodanaInspectionProfile.clone(profile, "qodana.sanity.profile(base:${profile.name})", profileManager)

    excludeModifiers.forEach { it.updateProfileScopes(newProfile, project, config.projectPath) }
    return SanityInspectionGroup(name, newProfile)
  }
}