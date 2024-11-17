package org.jetbrains.qodana.inspectionKts.tests

import com.google.common.annotations.VisibleForTesting
import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.InspectionProfileManager

@Suppress("unused")
@VisibleForTesting
internal fun isInspectionPresentInProfile(project: Project, inspectionId: String): Boolean {
  val profile = InspectionProfileManager.getInstance(project).currentProfile
  return profile.getToolsOrNull(inspectionId, project) != null
}
