package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import training.project.ProjectUtils

class RubyLangSupport : AbstractLangSupport() {
  private val rubyProjectName: String
    get() = "RubyLearnProject"

  override fun checkSdk(sdk: Sdk?, project: Project) {}

  override fun getSdkForProject(project: Project): Sdk? {
    return null
  }

  override fun importLearnProject(): Project? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
  private val acceptableLanguages = setOf("ruby")
  override fun acceptLang(ext: String) = acceptableLanguages.contains(ext.toLowerCase())

  override val defaultProjectName:String
    get() = rubyProjectName

  override val FILE_EXTENSION: String
    get() = "rb"

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {}

  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    return ProjectUtils.importOrOpenProject("/learnProjects/ruby/RubyLearnProject", projectName)
  }
}