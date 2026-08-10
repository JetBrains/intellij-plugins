package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.lang.javascript.config.graph.JSConfigGraphCache
import com.intellij.lang.javascript.config.graph.JSImportGraph
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil.isJavaScriptOrTypeScriptFile
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.concurrency.annotations.RequiresReadLock
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.isVueFile

internal object VueKolarContext {
  @RequiresReadLock
  fun getRelatedFiles(
    project: Project,
    files: Collection<VirtualFile>,
  ): Collection<VirtualFile> {
    val importGraphs = files.asSequence()
      .filter { isVueProjectSourceFile(project, it) }
      .mapNotNull { it.findPsiFile(project) }
      .mapNotNull { TypeScriptConfigUtil.getPreferableConfig(it, true) }
      .distinct()
      .map { JSConfigGraphCache.getService(project).getGraph(it) }
      .toList()

    if (importGraphs.isEmpty())
      return emptyList()

    val excludedFiles = files.asSequence()
      .filter { it.isVueFile }
      .toSet()

    return FileTypeIndex.getFiles(VueFileType, VueProjectScope(importGraphs, excludedFiles))
  }

  private fun isVueProjectSourceFile(
    project: Project,
    file: VirtualFile,
  ): Boolean =
    when {
      file.isVueFile
        -> true

      isJavaScriptOrTypeScriptFile(file.name)
        -> isVueContext(file, project)

      else -> false
    }
}

private class VueProjectScope(
  private val importGraphs: List<JSImportGraph>,
  private val excludedFiles: Set<VirtualFile>,
) : GlobalSearchScope() {
  override fun contains(file: VirtualFile): Boolean =
    importGraphs.any { it.containsFile(file) }
    && file !in excludedFiles

  override fun isSearchInModuleContent(aModule: Module): Boolean = true
  override fun isSearchInLibraries(): Boolean = false
}
