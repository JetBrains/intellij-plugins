package org.intellij.prisma.ide.indexing

import com.intellij.javascript.nodejs.library.node_modules.NodeModulesDirectoryManager
import com.intellij.lang.javascript.library.JSSyntheticLibraryProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

private const val PRISMA_CLIENT = ".prisma"

class PrismaAdditionalLibraryRootsProvider : AdditionalLibraryRootsProvider(), JSSyntheticLibraryProvider {
  override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> =
    listOf(PrismaModulesCache.getInstance(project).library)
}

@Service
private class PrismaModulesCache(project: Project) {
  private val directoryManager = NodeModulesDirectoryManager.getInstance(project)

  private val libraryCache = CachedValuesManager.getManager(project).createCachedValue {
    val roots = directoryManager.nodeModulesDirs
      .asSequence()
      .mapNotNull { it.findChild(PRISMA_CLIENT) }
      .filter { it.isValid }
      .toList()
    val library = SyntheticLibrary.newImmutableLibrary(roots)
    CachedValueProvider.Result.create(library, VirtualFileManager.getInstance())
  }

  val library: SyntheticLibrary
    get() = libraryCache.value

  companion object {
    fun getInstance(project: Project) = project.service<PrismaModulesCache>()
  }
}