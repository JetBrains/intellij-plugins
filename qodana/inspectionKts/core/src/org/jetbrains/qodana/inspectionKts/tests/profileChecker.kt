package org.jetbrains.qodana.inspectionKts.tests

import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.InspectionProfileManager
import org.jetbrains.annotations.VisibleForTesting

@Suppress("unused")
@VisibleForTesting
internal fun isInspectionPresentInProfile(project: Project, inspectionId: String): Boolean {
  val profile = InspectionProfileManager.getInstance(project).currentProfile
  return profile.getToolsOrNull(inspectionId, project) != null
}
