package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import training.project.ProjectUtils

/**
 * @author Sergey Karashevich
 */
class GoLangSupport : AbstractLangSupport() {

  override fun importLearnProject(): Project? {
    TODO("not implemented")
  }

  override val primaryLanguage: String
    get() = "go"

  override fun checkSdk(sdk: Sdk?, project: Project) {}

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {}

  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    return ProjectUtils.importOrOpenProject("/learnProjects/go/LearnProject", "LearnProject")
  }

}