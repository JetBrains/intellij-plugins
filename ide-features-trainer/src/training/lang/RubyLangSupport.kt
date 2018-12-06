package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkTypeId
import training.project.ProjectUtils

class RubyLangSupport : AbstractLangSupport() {

  override fun importLearnProject(): Project? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private val acceptableLanguages = setOf("ruby")
  override fun acceptLang(ext: String) = acceptableLanguages.contains(ext.toLowerCase())

  override val defaultProjectName:String
    get() = "RubyLearnProject"

  override val FILE_EXTENSION: String
    get() = "rb"

  override fun applyProjectSdk(project: Project) { }

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = {}

  override fun checkSdkCompatibility(sdk: Sdk, sdkTypeId: SdkTypeId) {}

  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    return ProjectUtils.importOrOpenProject("/learnProjects/ruby/RubyLearnProject", "RubyLearnProject")
  }

  override fun needToCheckSDK() = false

}