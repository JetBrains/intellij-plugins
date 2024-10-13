package org.jetbrains.qodana.staticAnalysis.profile.providers

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.profile.*

const val QODANA_EMPTY_PROFILE_NAME = "empty"

class QodanaEmptyProfileProvider : QodanaInspectionProfileProvider {
  override fun provideProfile(profileName: String, project: Project?): QodanaInspectionProfile? {
    if (profileName != QODANA_EMPTY_PROFILE_NAME) return null
    return QodanaInspectionProfileManager.getInstance(project).qodanaEmptyProfile
  }

  override fun getAllProfileNames(project: Project?): List<String> = listOf(QODANA_EMPTY_PROFILE_NAME)
}
