package org.jetbrains.qodana.ui.problemsView.tree.model

import com.intellij.openapi.application.readAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.qodana.problem.SarifProblem
import java.nio.file.Path
import kotlin.io.path.Path

class ModuleDataProvider private constructor(private val relativePathsToModuleData: Map<String, ModuleData?>) {
  companion object {
    suspend fun create(project: Project, sarifProblemsWithVirtualFiles: List<Pair<SarifProblem, VirtualFile>>): ModuleDataProvider {
      val projectFileIndex = ProjectFileIndex.getInstance(project)

      val moduleToModuleData = mutableMapOf<Module?, ModuleData?>()
      val relativePathsToModuleData: Map<String, ModuleData?> = sarifProblemsWithVirtualFiles
        .distinctBy { it.first.relativePathToFile }
        .associate { (sarifProblem, virtualFile) ->
          val module = readAction {
            projectFileIndex.getModuleForFile(virtualFile)
          }
          val moduleData = if (module !in moduleToModuleData) {
            val moduleData = module?.let { ModuleData(project, getModuleRootFilePath(module), module) }
            moduleToModuleData[module] = moduleData
            moduleData
          } else {
            moduleToModuleData[module]
          }

          sarifProblem.relativePathToFile to moduleData
        }

      return ModuleDataProvider(relativePathsToModuleData)
    }
  }
  fun getModuleDataForSarifProblem(sarifProblem: SarifProblem): ModuleData? {
    return relativePathsToModuleData[sarifProblem.relativePathToFile]
  }
}

private suspend fun getModuleRootFilePath(module: Module): Path {
  return readAction {
    val moduleDirPath by lazy { Path(ModuleUtilCore.getModuleDirPath(module)) }

    if (module.isDisposed) return@readAction moduleDirPath
    val roots = ModuleRootManager.getInstance(module).contentRoots

    if (roots.size == 1) {
      val rootVirtualFile = roots.first()
      val path = rootVirtualFile.fileSystem.getNioPath(rootVirtualFile)
      if (path != null) return@readAction path
    }

    moduleDirPath
  }
}