package org.jetbrains.qodana.dart

import com.intellij.codeInspection.InspectionApplicationException
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileProvider

internal const val DART_STARTER_PROFILE = "qodana.dart.starter"

class DartInspectionProfileProvider : QodanaInspectionProfileProvider {
  override fun provideProfile(profileName: String, project: Project?): QodanaInspectionProfile? {
    if (profileName != DART_STARTER_PROFILE) return null

    return QodanaInspectionProfile.newWithDisabledTools(
      profileName,
      QodanaInspectionProfileManager.getInstance(project),
    ).apply {
      val tool = getToolsOrNull(DART_ANALYZE_SHORT_NAME, project)
                 ?: throw InspectionApplicationException(QodanaDartBundle.message("dart.profile.error.tool.not.found", DART_ANALYZE_SHORT_NAME))
      tool.isEnabled = true
      tool.defaultState.isEnabled = true
    }
  }

  override fun getAllProfileNames(project: Project?): List<String> = listOf(DART_STARTER_PROFILE)
}
