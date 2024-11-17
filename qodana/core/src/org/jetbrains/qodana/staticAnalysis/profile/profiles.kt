package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.ex.DynamicGroupTool
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.ex.ToolsImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.search.scope.ProjectFilesScope
import com.intellij.psi.search.scope.packageSet.NamedScope
import com.intellij.psi.search.scope.packageSet.PackageSet


fun copyDependentTools(project: Project?, tool: ToolsImpl, from:InspectionProfileImpl, to: QodanaInspectionProfile) {
  val wrapper = tool.defaultState.tool.tool
  if (wrapper is DynamicGroupTool) {
    for (child in wrapper.children) {
      to.addTool(project, child, null)
      val fromDependentTool = from.getToolsOrNull(child.shortName, project)
      val toDependentTool = to.getToolsOrNull(child.shortName, project)

      if (fromDependentTool != null && toDependentTool != null) {
        toDependentTool.copyTool(fromDependentTool, project)
      }
    }
  }
}

internal fun ToolsImpl.copyTool(from: ToolsImpl, project: Project?) {
  setDefaultState(InspectionProfileImpl.copyToolSettings(from.defaultState.tool), from.defaultState.isEnabled, from.defaultState.level,
                  from.defaultState.editorAttributesExternalName)
  removeAllScopes()
  from.nonDefaultTools?.forEach {
    val scope = it.getScope(project)
    val tool = if (scope != null) {
      addTool(scope, it.tool, it.isEnabled, it.level)
    }
    else {
      addTool(it.scopeName, it.tool, it.isEnabled, it.level)
    }
    tool.editorAttributesExternalName = it.editorAttributesExternalName
  }
  isEnabled = from.isEnabled
}

fun createScope(scopeName: String, set: PackageSet?): NamedScope {
  if (set == null) return ProjectFilesScope.INSTANCE
  return NamedScope(scopeName, set)
}