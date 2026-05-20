package org.jetbrains.qodana.inspectionKts.fileFactory

import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import java.nio.file.Path

/**
 * Extension point for creating PSI files in MCP tools.
 * Implementations can provide language-specific PSI file creation logic,
 * such as setting up proper context modules for Kotlin files.
 */
interface CustomPsiFileFactory {
  companion object {
    @JvmField
    val EP_NAME: ExtensionPointName<CustomPsiFileFactory> =
      ExtensionPointName.create("org.jetbrains.qodana.inspectionKts.customPsiFileFactory")

    /**
     * Creates a PSI file using the appropriate factory for the given path.
     * Falls back to the default implementation if no specific factory is found.
     */
    suspend fun createPsiFile(project: Project, contextPath: Path, content: String): PsiFile {
      for (factory in EP_NAME.extensionList) {
        if (factory.canHandle(contextPath)) {
          val psiFile = factory.createFile(project, contextPath, content)
          if (psiFile != null) return psiFile
        }
      }
      return DefaultPsiFileFactory.createFile(project, contextPath, content)
    }

    suspend fun createOrFindPsiFile(project: Project, contextPath: Path, content: String?): PsiFile? {
      if (content == null) {
        val virtualFile = LocalFileSystem.getInstance().findFileByNioFile(contextPath)
                          ?: return null
        return readAction {
          PsiManager.getInstance(project).findFile(virtualFile)
        }
      }

      val psiFile = createPsiFile(project, contextPath, content)
      return psiFile
    }

  }

  /**
   * Returns true if this factory can handle this path.
   */
  fun canHandle(contextPath: Path): Boolean

  /**
   * Creates a PSI file with the given path and content.
   * Implementations may set up additional context (e.g., context modules for Kotlin).
   */
  suspend fun createFile(project: Project, contextPath: Path, content: String): PsiFile?
}

/**
 * Default implementation that creates PSI files using the standard PsiFileFactory.
 */
internal object DefaultPsiFileFactory {
  suspend fun createFile(project: Project, contextPath: Path, content: String): PsiFile {
    return edtWriteAction {
      val fileName = contextPath.fileName.toString()
      val fileType = FileTypeRegistry.getInstance().getFileTypeByFileName(fileName)
      PsiFileFactory.getInstance(project).createFileFromText(fileName, fileType, content)
    }
  }
}