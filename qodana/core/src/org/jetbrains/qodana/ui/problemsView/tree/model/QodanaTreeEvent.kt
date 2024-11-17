package org.jetbrains.qodana.ui.problemsView.tree.model

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.problem.SarifProblemWithPropertiesAndFile
import org.jetbrains.qodana.settings.ConfigExcludeItem

sealed interface QodanaTreeEvent

data class QodanaTreeProblemEvent(
  val sarifProblemsToProperties: Set<SarifProblemWithPropertiesAndFile>,
) : QodanaTreeEvent

data class QodanaTreeExcludeEvent(
  val excludedData: Set<ConfigExcludeItem>,
  val project: Project
) : QodanaTreeEvent