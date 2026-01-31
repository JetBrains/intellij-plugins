package org.jetbrains.qodana.inspectionKts.kotlin.script

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.NonClasspathDirectoriesScope
import org.jetbrains.kotlin.idea.base.projectStructure.scope.KotlinSourceFilterScope
import org.jetbrains.kotlin.idea.base.projectStructure.toKaLibraryModule

internal class InspectionKtsDependenciesScope(
  private val roots: Set<VirtualFile>,
  private val jarsDependenciesScope: NonClasspathDirectoriesScope,
) {
  fun getResolveScopeForFile(project: Project, file: VirtualFile): GlobalSearchScope? {
    if (!isUnderDependenciesRoot(file)) return null

    return GlobalSearchScope.union(
      listOfNotNull(
        ProjectRootManager.getInstance(project).projectSdk?.toKaLibraryModule(project)?.contentScope,
        KotlinSourceFilterScope.libraryClasses(jarsDependenciesScope, project),
        GlobalSearchScope.fileScope(project, file),
      ).toTypedArray()
    )
  }

  private fun isUnderDependenciesRoot(file: VirtualFile): Boolean {
    if (roots.isEmpty()) {
      return false
    }

    val isJar = file.fileSystem is JarFileSystem
    val jarFile = if (isJar) {
      VfsUtilCore.getVirtualFileForJar(file) ?: file
    } else {
      file
    }
    return VfsUtilCore.isUnder(jarFile, roots)
  }
}