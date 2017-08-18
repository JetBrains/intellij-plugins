package training.project

import com.intellij.ide.impl.ProjectUtil
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VfsUtil.findFileByIoFile
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.net.URL

object ProjectUtils {

  val ideProjectsBasePath by lazy { WizardContext(null, null).projectFileDirectory }

  /**
   * For example:
   * @projectPath = "/learnProjects/SimpleProject"
   * @projectName = "SimpleProject"
   *
   */
  fun importOrOpenProject(projectPath: String, projectName: String): Project? {
    val dest = File(ideProjectsBasePath, projectName)
    if (!checkProjectExistence(projectName)) {
      val inputUrl: URL = javaClass.getResource(projectPath)
      FileUtils.copyResourcesRecursively(inputUrl, dest)
    }
    val toSelect = findFileByIoFile(dest, false) ?: throw Exception("Copied Learn project folder is null")
    return doImportOrOpenProject(toSelect)
  }

  private fun doImportOrOpenProject(projectDir: VirtualFile): Project? {
    val projectRef = Ref<Project>()
    TransactionGuard.getInstance().submitTransactionAndWait({
      projectRef.set(ProjectUtil.openOrImport(projectDir.path, null, false))
    })
    return projectRef.get()
  }

  private fun checkProjectExistence(projectName: String): Boolean {
    return File(ideProjectsBasePath, projectName).exists()
  }

}