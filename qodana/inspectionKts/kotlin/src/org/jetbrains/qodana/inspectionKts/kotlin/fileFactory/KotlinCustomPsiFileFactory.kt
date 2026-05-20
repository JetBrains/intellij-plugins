package org.jetbrains.qodana.inspectionKts.kotlin.fileFactory

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModuleProvider
import org.jetbrains.kotlin.analysis.api.projectStructure.contextModule
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
  override suspend fun createFile(project: Project, contextPath: Path, content: String): PsiFile? {
    return edtWriteAction {

        val factory = KtPsiFactory(project)
        val ktFile = factory.createFile(content)

        // Find any Kotlin file in the project to get its context module for correct analysis
        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)

        val ktVirtualFile =
          FilenameIndex.firstVirtualFileWithName(contextPath.fileName.toString(), false, scope, null) //TODO: fix finding context
          ?: return@edtWriteAction null

        val psi = psiManager.findFile(ktVirtualFile) ?: return@edtWriteAction null
        val contextModuleFromProject = KaModuleProvider.Companion.getModule(project, psi, useSiteModule = null)

        ktFile.contextModule = contextModuleFromProject
        ktFile
    }
  }
}