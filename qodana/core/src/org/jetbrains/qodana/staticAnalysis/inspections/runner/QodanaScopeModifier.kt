package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.ex.ToolsImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.search.scope.packageSet.NamedScope
import org.jetbrains.annotations.Nls
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.extensions.VcsIgnoredFilesProvider
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.createScope
import java.nio.file.Path
import kotlin.io.path.Path

abstract class QodanaScopeModifier(val scope: InspectScope, private val scopeName: @Nls String) {
  internal fun getProfileScope(projectPath: Path): NamedScope {
    return createScope(scopeName, scope.getProfileScope(projectPath))
  }

  abstract fun updateProfileScopes(profile: QodanaInspectionProfile, project: Project, projectPath: Path)

  protected fun disableToolIfEmptyScopes(tools: ToolsImpl): Boolean {
    val isEmptyPathsAndPatterns = scope.paths.isEmpty() && scope.patterns.isEmpty()
    if (isEmptyPathsAndPatterns) {
        tools.isEnabled = false
    }
    return isEmptyPathsAndPatterns
  }
}

class ExcludeScopeModifier(scope: InspectScope)
  : QodanaScopeModifier(scope, QodanaBundle.message("qodana.yaml.exclude.scope.name", scope.name)) {

  override fun updateProfileScopes(profile: QodanaInspectionProfile, project: Project, projectPath: Path) {
    val tools = profile.getToolsOrNull(scope.name, project) ?: return

    if (disableToolIfEmptyScopes(tools)) return

    tools.prependTool(getProfileScope(projectPath), tools.tool, false, HighlightDisplayLevel.DO_NOT_SHOW)
  }
}

open class GlobalExcludeScopeModifier(scope: InspectScope)
  : QodanaScopeModifier(scope, QodanaBundle.message("qodana.yaml.exclude.scope.name", scope.name)) {

  override fun updateProfileScopes(profile: QodanaInspectionProfile, project: Project, projectPath: Path) {
    profile.tools.forEach {
      if (!disableToolIfEmptyScopes(it)) {
        it.prependTool(getProfileScope(projectPath), it.tool, false, HighlightDisplayLevel.DO_NOT_SHOW)
      }
    }
  }
}

class GitIgnoreExcludeScopeModifier(scope: InspectScope) : GlobalExcludeScopeModifier(scope) {
  override fun updateProfileScopes(profile: QodanaInspectionProfile, project: Project, projectPath: Path) {
    scope.paths = getGitIgnorePaths(project, projectPath)
    super.updateProfileScopes(profile, project, projectPath)
  }

  private fun getGitIgnorePaths(project: Project, projectPath: Path): List<String> {
    val files = VcsIgnoredFilesProvider.getVcsRepositoriesIgnoredFiles(project)
    return files.map { projectPath.relativize(Path(it.path)).toString() }
  }
}

class DefaultSeverityIncludeScopeModifier(scope: InspectScope)
  : QodanaScopeModifier(scope, QodanaBundle.message("qodana.yaml.include.scope.name.default.severity", scope.name)) {

  override fun updateProfileScopes(profile: QodanaInspectionProfile, project: Project, projectPath: Path) {
    val tools = profile.getToolsOrNull(scope.name, project) ?: return
    val isEmptyScope = scope.paths.isEmpty() && scope.patterns.isEmpty()

    if (tools.isEnabled) {
      if (!isEmptyScope) {
        tools.prependTool(getProfileScope(projectPath), tools.tool, true, tools.defaultState.level)
      }
      return
    }

    //tool was disabled, so enable it
    tools.defaultState.isEnabled = false
    tools.isEnabled = true

    if (isEmptyScope) {
      // including without scope is treated as enabling default scope, but if there were some enabled scope - we are doing nothing
      if (tools.tools.all { !it.isEnabled }) {
        tools.defaultState.isEnabled = true
      }
      return
    } else {
      tools.disableAllScopes()
      tools.prependTool(getProfileScope(projectPath), tools.tool, true, tools.defaultState.level)
    }
  }
}

private fun ToolsImpl.disableAllScopes() {
  tools.forEach {
    it.isEnabled = false
  }
}