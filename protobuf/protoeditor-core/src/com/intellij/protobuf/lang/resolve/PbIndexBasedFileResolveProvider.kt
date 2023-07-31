package com.intellij.protobuf.lang.resolve

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.protobuf.ide.settings.findFileByImportPath
import com.intellij.psi.search.GlobalSearchScope

internal class PbIndexBasedFileResolveProvider : FileResolveProvider {
  override fun findFile(path: String, project: Project): VirtualFile? {
    if (!isEnabled(project) || !path.endsWith(".proto")) return null
    val searchedFileName = path.substringAfterLast('/').let(FileUtilRt::getNameWithoutExtension)
    val searchScope = GlobalSearchScope.projectScope(project)
    return findFileByImportPath(searchedFileName, searchScope, path)
  }

  override fun findFile(path: String, module: Module): VirtualFile? {
    if (!isEnabled(module.project) || !path.endsWith(".proto")) return null
    val searchedFileName = path.substringAfterLast('/').let(FileUtilRt::getNameWithoutExtension)
    val searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module).uniteWith(module.getModuleContentWithDependenciesScope())
    return findFileByImportPath(searchedFileName, searchScope, path)
  }

  private fun isEnabled(project: Project): Boolean {
    return PbProjectSettings.getInstance(project).isIndexBasedResolveEnabled
  }

  override fun getChildEntries(path: String, project: Project): Collection<FileResolveProvider.ChildEntry> {
    return emptyList()
  }

  override fun getDescriptorFile(project: Project): VirtualFile? {
    return null
  }

  override fun getSearchScope(project: Project): GlobalSearchScope {
    return GlobalSearchScope.projectScope(project)
  }
}