package org.angular2.cli

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.webSymbols.context.WebSymbolsContextProvider
import org.angular2.cli.config.AngularConfigProvider

class AngularCliWebSymbolsContextProvider : WebSymbolsContextProvider {

  override fun isEnabled(project: Project, directory: VirtualFile): CachedValueProvider.Result<Int?> {
    val config = AngularConfigProvider.getAngularConfig(project, directory)
                 ?: return CachedValueProvider.Result.create(null, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    val angularCliSourceDir = config.getProject(directory)?.sourceDir
                              ?: return CachedValueProvider.Result.create(null, config.angularJsonFile,
                                                                          VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    val depth = generateSequence(angularCliSourceDir) { it.parent }
      .takeWhile { it != angularCliSourceDir }
      .count()
    return CachedValueProvider.Result.create(depth, config.angularJsonFile,
                                             VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
  }
}