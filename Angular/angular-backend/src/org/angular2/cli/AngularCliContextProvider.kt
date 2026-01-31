package org.angular2.cli

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.polySymbols.context.PolyContextProvider
import com.intellij.psi.util.CachedValueProvider
import org.angular2.cli.config.AngularConfigProvider

class AngularCliContextProvider : PolyContextProvider {

  override fun isEnabled(project: Project, directory: VirtualFile): CachedValueProvider.Result<Int?> {
    val config = AngularConfigProvider.findAngularConfig(project, directory)
                 ?: return CachedValueProvider.Result.create(null, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    val angularCliSourceDir = config.getProject(directory)?.sourceDir
                              ?: return CachedValueProvider.Result.create(null, config.file,
                                                                          VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    val depth = generateSequence(angularCliSourceDir) { it.parent }
      .takeWhile { it != angularCliSourceDir }
      .count()
    return CachedValueProvider.Result.create(depth, config.file,
                                             VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
  }
}