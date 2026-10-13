package org.jetbrains.qodana.inspectionKts.mcp.impl

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.codeInspection.ex.DynamicInspectionsProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private const val EXTRACTED_INSPECTIONS_PROVIDER_NAME = "MCP inspection.kts"

@Service(Service.Level.APP)
internal class ExtractedInspectionsService {
  private val inspectionsFlow = MutableStateFlow<Set<LocalInspectionTool>>(emptySet())

  fun getInspectionsFlow(): Flow<Set<DynamicInspectionDescriptor>> = inspectionsFlow.map { inspections ->
    inspections.mapTo(mutableSetOf()) { inspection ->
      DynamicInspectionDescriptor.Local(inspection, EXTRACTED_INSPECTIONS_PROVIDER_NAME)
    }
  }

  suspend fun <T> withInspections(inspections: Collection<LocalInspectionTool>, action: suspend () -> T): T {
    inspectionsFlow.update { it + inspections }
    try {
      return action()
    }
    finally {
      inspectionsFlow.update { it - inspections.toSet() }
    }
  }
}

internal class ExtractedInspectionsDynamicProvider : DynamicInspectionsProvider {
  override fun inspections(project: Project): Flow<Set<DynamicInspectionDescriptor>> =
    service<ExtractedInspectionsService>().getInspectionsFlow()

  override fun name(): String = EXTRACTED_INSPECTIONS_PROVIDER_NAME
}
