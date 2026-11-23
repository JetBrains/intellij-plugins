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
    val newProfile = calculateNewProfileForGroup(config, project, addDefaultExclude, "qodana.sanity.profile(base:${profile.name})") ?: return this
    return SanityInspectionGroup(name, newProfile)
  }
}