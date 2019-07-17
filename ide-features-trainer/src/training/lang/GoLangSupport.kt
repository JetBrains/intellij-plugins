package training.lang

import com.goide.configuration.GoSdkConfigurable
import com.goide.sdk.GoSdkService
import com.goide.sdk.GoSdkUtil
import com.goide.sdk.combobox.GoSdkList
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.startup.StartupManager
import training.project.ProjectUtils

class GoLangSupport : AbstractLangSupport() {

  companion object {
    @JvmStatic
    val lang: String = "go"
  }

  override val filename: String
    get() = "learning.go"

  override fun importLearnProject(): Project? {
    TODO("not implemented")
  }

  override val primaryLanguage: String
    get() = lang

  override fun checkSdk(sdk: Sdk?, project: Project) {}

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = { project ->
    if (!project.hasValidSdk()) {
      StartupManager.getInstance(project).runWhenProjectIsInitialized {
        ApplicationManager.getApplication().invokeLater {
          if (!project.hasValidSdk()) {
            GoSdkList.getInstance().reloadSdks { }
            GoSdkUtil.automaticallyInitializeSdk(project, null)
          }
        }
        ApplicationManager.getApplication().invokeLater {
          if (!project.hasValidSdk()) {
            ShowSettingsUtil.getInstance().editConfigurable(project, GoSdkConfigurable(project, true))
          }
        }
      }
    }
  }

  private fun Project.hasValidSdk() = GoSdkService.getInstance(this).getSdk(null).isValid

  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    return ProjectUtils.importOrOpenProject("/learnProjects/go/LearnProject", "LearnProject")
  }

}