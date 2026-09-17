package org.jetbrains.qodana.dart

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileProvider

internal const val DART_STARTER_PROFILE = "qodana.dart.starter"

class DartInspectionProfileProvider : QodanaInspectionProfileProvider {
  override fun provideProfile(profileName: String, project: Project?): QodanaInspectionProfile? {
    if (profileName != DART_STARTER_PROFILE) return null

    return QodanaInspectionProfile.newWithEnabledByDefaultTools(
      profileName,
      QodanaInspectionProfileManager.getInstance(project),
    )
  }

  override fun getAllProfileNames(project: Project?): List<String> = listOf(DART_STARTER_PROFILE)
}
