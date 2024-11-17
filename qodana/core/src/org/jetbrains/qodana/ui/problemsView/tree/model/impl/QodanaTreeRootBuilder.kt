package org.jetbrains.qodana.ui.problemsView.tree.model.impl

import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.problem.SarifProblemWithProperties
import org.jetbrains.qodana.problem.SarifProblemWithPropertiesAndFile
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.jetbrains.qodana.ui.problemsView.tree.model.*

class QodanaTreeRootBuilder(
  private val treeContext: QodanaTreeContext,
  private val problemsWithVirtualFiles: List<Pair<SarifProblemWithProperties, VirtualFile>>,
  private val ignoredData: Set<ConfigExcludeItem>,
  private val rootData: QodanaTreeRoot.PrimaryData,
) {
  suspend fun buildRoot(): QodanaTreeRoot {
    return withContext(QodanaDispatchers.Default) {
      val problemsSortedInOptimalInsertionOrder: List<Pair<SarifProblemWithProperties, VirtualFile>> = sortSarifProblemsInOptimalInsertionOrder()

      var root = QodanaTreeRootImpl.newEmpty(treeContext, rootData, ignoredData)
      val mappedProblems = problemsSortedInOptimalInsertionOrder
        .map { SarifProblemWithPropertiesAndFile(it.first.problem, it.first.properties, treeContext.project, it.second) }
      val event = QodanaTreeProblemEvent(mappedProblems.toSet())

      val ignored = QodanaTreePath.Builder()
      root = root.processTreeEvent(event, ignored)

      root.problemsCount
      root
    }
  }

  private fun sortSarifProblemsInOptimalInsertionOrder(): List<Pair<SarifProblemWithProperties, VirtualFile>> {
    return problemsWithVirtualFiles
      .map { (sarifProblemWithProperties, virtualFile) ->
        val sarifProblem = sarifProblemWithProperties.problem
        val qodanaSeverity = if (treeContext.groupBySeverity) {
          sarifProblem.qodanaSeverity
        } else {
          null
        }
        val inspectionCategory = if (treeContext.groupByInspection) {
          treeContext.inspectionInfoProvider.getCategory(sarifProblem.inspectionId)
        } else {
          null
        }
        val inspectionId = if (treeContext.groupByInspection) {
          sarifProblem.inspectionId
        } else {
          null
        }
        val moduleName = treeContext.moduleDataProvider?.getModuleDataForSarifProblem(sarifProblem)?.module?.name
        val filePath = sarifProblem.relativePathToFile

        val comparisonData = SarifProblemComparisonData(qodanaSeverity, inspectionCategory, inspectionId, moduleName, filePath)
        (sarifProblemWithProperties to virtualFile) to comparisonData
      }
      .sortedWith(
        compareByDescending<Pair<Pair<SarifProblemWithProperties, VirtualFile>, SarifProblemComparisonData>> { it.second.qodanaSeverity }
          .thenByDescending { it.second.inspectionCategory }
          .thenByDescending { it.second.inspectionId }
          .thenByDescending { it.second.moduleName }
          .thenByDescending { it.second.filePath }
      )
      .map { it.first }
  }

  private class SarifProblemComparisonData(
    val qodanaSeverity: QodanaSeverity?,
    val inspectionCategory: String?,
    val inspectionId: String?,
    val moduleName: String?,
    val filePath: String,
  )
}