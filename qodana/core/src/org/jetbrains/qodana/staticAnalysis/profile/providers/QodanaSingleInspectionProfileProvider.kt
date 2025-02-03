package org.jetbrains.qodana.staticAnalysis.profile.providers

import com.intellij.codeInspection.InspectionApplicationException
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileProvider
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile

private const val QODANA_SINGLE = "qodana.single:"

class QodanaSingleInspectionProfileProvider : QodanaInspectionProfileProvider {

  override fun provideProfile(profileName: String, project: Project?): QodanaInspectionProfile? {
    val inspectionId = profileName.removePrefix(QODANA_SINGLE)
    if (inspectionId === profileName) return null

    val inspectionProfile = QodanaInspectionProfile.newWithDisabledTools(profileName, QodanaInspectionProfileManager.getInstance(project))
    val tools = inspectionProfile.getToolsOrNull(inspectionId, project)
                ?: throw InspectionApplicationException("Unknown inspection id '$inspectionId' in qodana.single profile")

    tools.isEnabled = true
    tools.defaultState.isEnabled = true
    inspectionProfile.lockProfile(true)
    return inspectionProfile
  }

  override fun getAllProfileNames(project: Project?): List<String> {
    // Too many profiles available for completion, better not to show them completely
    return emptyList()
  }
}
