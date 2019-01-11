package training.lang

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import training.learn.exceptons.NoJavaModuleException
import training.project.ProjectUtils

/**
 * @author Sergey Karashevich
 */
class GoLangSupport : AbstractLangSupport() {

  override fun importLearnProject(): Project? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private val acceptableLanguages = setOf("go", "html")
  override fun acceptLang(ext: String) = acceptableLanguages.contains(ext.toLowerCase())

  override val FILE_EXTENSION: String
    get() = "go"

  override fun checkSdk(sdk: Sdk?, project: Project) {}

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {}

  @Throws(NoJavaModuleException::class)
  private fun checkJavaModule(project: Project) = { if (ModuleManager.getInstance(project).modules.isEmpty()) throw NoJavaModuleException() }

  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    return ProjectUtils.importOrOpenProject("/learnProjects/go/LearnProject", "LearnProject")
  }

}