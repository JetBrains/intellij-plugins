package org.jetbrains.qodana.ui.problemsView.tree.model

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.highlight.InspectionInfoProvider
import java.time.Instant

class QodanaTreeContext(
  val groupBySeverity: Boolean,
  val groupByInspection: Boolean,
  val moduleDataProvider: ModuleDataProvider?,
  val groupByDirectory: Boolean,
  val createdAt: Instant?,
  val project: Project,
  val inspectionInfoProvider: InspectionInfoProvider,
) {
  @Suppress("unused")
  val groupByModule: Boolean
    get() = moduleDataProvider != null
}