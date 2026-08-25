package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.ExtensionPointName.Companion.create
import com.intellij.openapi.project.Project

/**
 * Provides derived inspections for [com.intellij.lang.annotation.ProblemGroup] values from an inspection run.
 */
interface DerivedInspectionProvider {
  fun isApplicableTo(parentInspectionId: String): Boolean

  fun describeDerivedInspections(project: Project?, inspections: Collection<DetectedProblemGroup>): List<DerivedInspection>

  companion object {
    val EP_NAME: ExtensionPointName<DerivedInspectionProvider> = create("org.intellij.qodana.derivedInspectionProvider")

    fun createDerivedInspections(
      project: Project?,
      detectedProblemGroups: Collection<DetectedProblemGroup>,
    ): List<DerivedInspection> = detectedProblemGroups
      .groupBy { it.parentInspectionId }
      .flatMap { (parentInspectionId, problemGroups) ->
        EP_NAME.extensionList
          .filter { it.isApplicableTo(parentInspectionId) }
          .flatMap { it.describeDerivedInspections(project, problemGroups) }
      }
  }
}

data class DetectedProblemGroup(
  val parentInspectionId: String,
  val problemGroupId: String,
)

data class DerivedInspection(
  val id: String,
  val parentInspectionId: String,
  val displayName: String,
  val fullDescription: String? = null,
)
