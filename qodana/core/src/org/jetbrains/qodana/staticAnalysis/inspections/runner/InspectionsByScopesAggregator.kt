package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.ex.*
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.packageDependencies.DependencyValidationManager
import com.intellij.psi.PsiFile
import com.intellij.psi.search.scope.packageSet.PackageSet

class InspectionsByScopesAggregator(
  private val localTools: List<Tools>,
  private val globalSimpleTools: List<Tools>,
  private val project: Project,
) : EnabledInspectionsProvider {
  private val packageSetsWithToolsStates: Map<PackageSet, List<ToolsWithState>> by lazy {
    createPackageSetsWithToolsStatesMap()
  }

  private fun createPackageSetsWithToolsStatesMap(): Map<PackageSet, List<ToolsWithState>> {
    return sequenceOf(localTools, globalSimpleTools)
      .flatten()
      .filterIsInstance<ToolsImpl>()
      .filter {
        it.isEnabled
      }
      .flatMap { tools ->
        tools.nonDefaultTools?.mapIndexed { idx, state -> ToolsWithState(tools, state, priority = -idx) } ?: emptyList()
      }
      .mapNotNull { toolsWithState ->
        val packageSet = toolsWithState.state.getScope(project)?.value ?: return@mapNotNull null
        packageSet to toolsWithState
      }
      .groupBy(
        keySelector = { it.first },
        valueTransform = { it.second }
      )
  }

  override fun getEnabledTools(psiFile: PsiFile?, includeDoNotShow: Boolean): EnabledInspectionsProvider.ToolWrappers {
    if (psiFile == null) return EnabledInspectionsProvider.ToolWrappers(emptyList(), emptyList())

    val inspectionIdWithNonDefaultState = mutableMapOf<String, ToolsWithState>()
    val manager = DependencyValidationManager.getInstance(project)

    runReadAction {
      if (!psiFile.isValid) {
        return@runReadAction
      }
      for ((packageSet: PackageSet, toolsWithStates: List<ToolsWithState>) in packageSetsWithToolsStates.entries) {
        if (!packageSet.contains(psiFile, manager)) continue

        for (entry in toolsWithStates) {
          val inspectionId = entry.tools.shortName
          val alreadyPresentStatePriority = inspectionIdWithNonDefaultState[inspectionId]?.priority ?: Int.MIN_VALUE
          if (entry.priority > alreadyPresentStatePriority) {
            inspectionIdWithNonDefaultState[inspectionId] = entry
          }
        }
      }
    }

    val localEnabledTools = getEnabledToolsBatch(localTools, inspectionIdWithNonDefaultState, includeDoNotShow)
      .filterIsInstance<LocalInspectionToolWrapper>()
      .toList()
    val globalSimpleEnabledTools = getEnabledToolsBatch(globalSimpleTools, inspectionIdWithNonDefaultState, includeDoNotShow)
      .filterIsInstance<GlobalInspectionToolWrapper>()
      .toList()
    return EnabledInspectionsProvider.ToolWrappers(localEnabledTools, globalSimpleEnabledTools)
  }
}

private data class ToolsWithState(
  val tools: Tools,
  val state: ScopeToolState,
  val priority: Int
)

private fun getEnabledToolsBatch(
  toolsList: List<Tools>,
  toolsIdWithNonDefaultState: Map<String, ToolsWithState>,
  includeDoNotShow: Boolean
): Sequence<InspectionToolWrapper<*, *>> {
  return toolsList
    .asSequence()
    .filter { it.isEnabled }
    .map { tools ->
      toolsIdWithNonDefaultState[tools.shortName]?.state ?: tools.defaultState
    }
    .filter { toolState ->
      toolState.isEnabled && (includeDoNotShow || ToolsImpl.isAvailableInBatch(toolState))
    }
    .map { toolState ->
      toolState.tool
    }
}