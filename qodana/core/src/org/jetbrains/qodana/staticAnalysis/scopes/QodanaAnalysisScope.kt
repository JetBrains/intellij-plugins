package org.jetbrains.qodana.staticAnalysis.scopes

import com.intellij.analysis.AnalysisScope
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.psi.search.SearchScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

class QodanaAnalysisScope : AnalysisScope {

  constructor(project: Project) : super(project)

  constructor(scope: SearchScope, project: Project) : super(scope, project)

  constructor(module: Module) : super(module)

  constructor(project: Project, virtualFiles: Collection<VirtualFile?>) : super(project, virtualFiles)

  override fun containsModule(module: Module): Boolean {
    return if (myType != CUSTOM) {
      super.containsModule(module)
    }
    else {
      !module.isDisposed() && ModuleRootManager.getInstance(module).contentRoots.any { myScope.contains(it) }
    }
  }

  companion object {
    fun fromConfigOrDefault(config: QodanaConfig, project: Project, onPathNotFound: (Path) -> Unit): QodanaAnalysisScope {
      val configured = config.sourceDirectory?.let(::Path) ?: return QodanaAnalysisScope(GlobalSearchScope.projectScope(project), project)
      val absolute = configured.let { if (!it.isAbsolute) config.projectPath.resolve(it) else it }

      val vfsDir = LocalFileSystem.getInstance().findFileByPath(absolute.invariantSeparatorsPathString)
      val searchScope = if (vfsDir == null) {
        onPathNotFound(absolute)
        GlobalSearchScope.projectScope(project)
      }
      else {
        GlobalSearchScopesCore.directoriesScope(project, true, vfsDir)
      }

      return QodanaAnalysisScope(searchScope, project)
    }
  }
}
