package org.jetbrains.qodana.staticAnalysis.inspections.targets

import com.intellij.codeInspection.ex.Tools
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import java.lang.Boolean.getBoolean

private const val DEFAULT_THRESHOLD = 100

@Service(Service.Level.PROJECT)
class QodanaTargetsService(val project: Project) {
  private val macroManager = PathMacroManager.getInstance(project)

  fun getTestTargets(tools: Map<String, Tools>): List<TestTarget> {
    return createEachInspectionTargets(tools, project) + createModuleInspectionTargets(tools, project)
  }

  private fun createEachInspectionTargets(tools: Map<String, Tools>, project: Project): List<TestTarget> {
    if (!getBoolean("qodana.report.inspection.test")) return emptyList()
    val threshold = Integer.getInteger("qodana.report.inspection.test.threshold", DEFAULT_THRESHOLD)
    return listOf(SingleScopeTestTarget("InspectionTest", tools.keys.toList(), threshold, QodanaAnalysisScope(project), macroManager))
  }

  private fun createModuleInspectionTargets(tools: Map<String, Tools>, project: Project): List<TestTarget> {
    if (!getBoolean("qodana.report.module.inspection.test")) return emptyList()
    val threshold = Integer.getInteger("qodana.report.inspection.test.threshold", DEFAULT_THRESHOLD)

    val modules = ModuleManager.getInstance(project).modules

    return modules.map { module ->
      SingleScopeTestTarget("ModuleInspectionTest[${module.name}]", tools.keys.toList(), threshold, QodanaAnalysisScope(module),
                            macroManager)
    }
  }
}