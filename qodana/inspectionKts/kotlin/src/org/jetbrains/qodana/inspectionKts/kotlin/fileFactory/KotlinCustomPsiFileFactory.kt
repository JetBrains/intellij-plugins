package org.jetbrains.qodana.inspectionKts.kotlin.fileFactory

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModule
import org.jetbrains.kotlin.analysis.api.projectStructure.contextModule
import org.jetbrains.kotlin.analysis.api.projectStructure.kaModule
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.qodana.inspectionKts.fileFactory.CustomPsiFileFactory
import java.nio.file.Path

/**
 * Kotlin-specific PSI file factory that sets up the proper context module
 * for correct analysis of Kotlin files.
 */
class KotlinCustomPsiFileFactory : CustomPsiFileFactory {

  override fun canHandle(contextPath: Path): Boolean {
    return contextPath.fileName.endsWith(".kt") || contextPath.fileName.endsWith(".kts")
  }

  @OptIn(KaExperimentalApi::class)
  override suspend fun createFile(project: Project, contextPath: Path, content: String): PsiFile {
    val contextModule: KaModule? = readAction {
      val scope = GlobalSearchScope.projectScope(project)
      val fileName = contextPath.fileName.toString()

      val byName = FilenameIndex.firstVirtualFileWithName(fileName, false, scope, null)
      val ktVirtualFile = byName ?: findKtFileInMatchingModule(project, contextPath)

      ktVirtualFile
        ?.let { PsiManager.getInstance(project).findFile(it) }
        ?.let { psi ->
          psi.kaModule(useSiteModule = null)
        }
    }

    return edtWriteAction {
      val ktFile = KtPsiFactory(project).createFile(content)
      if (contextModule != null) {
        ktFile.contextModule = contextModule
      }
      ktFile
    }
  }

  /**
   * Finds a .kt file from the module that owns [contextPath].
   * Walks up from the context path to find the nearest existing directory in the VFS,
   * then asks [ProjectFileIndex] which module contains it. This avoids iterating all modules
   * and touching their source roots, which forces lazy initialization and can break SDK checks.
   */
  private fun findKtFileInMatchingModule(project: Project, contextPath: Path): VirtualFile? {
    val normalizedContext = if (contextPath.isAbsolute) {
      contextPath.normalize()
    }
    else {
      val basePath = project.basePath ?: return null
      Path.of(basePath).resolve(contextPath).normalize()
    }

    val projectFileIndex = ProjectFileIndex.getInstance(project)
    val localFs = LocalFileSystem.getInstance()

    var current: Path? = normalizedContext.parent
    while (current != null) {
      val vFile = localFs.findFileByPath(current.toString()) ?: run {
        current = current.parent
        continue
      }
      val module = projectFileIndex.getModuleForFile(vFile, false) ?: run {
        current = current.parent
        continue
      }
      return FilenameIndex.getAllFilesByExt(project, "kt", GlobalSearchScope.moduleScope(module)).firstOrNull()
    }

    return null
  }
}
