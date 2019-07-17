package training.lang

import com.goide.configuration.GoSdkConfigurable
import com.goide.sdk.GoSdkService
import com.goide.sdk.GoSdkUtil
import com.goide.sdk.combobox.GoSdkList
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.ui.DialogBuilder
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
    StartupManager.getInstance(project).runWhenProjectIsInitialized {
      if (!project.hasValidSdk()) {
        GoSdkList.getInstance().reloadSdks { }
        GoSdkUtil.automaticallyInitializeSdk(project, null)
        if (!project.hasValidSdk()) {
          val configurable = GoSdkConfigurable(project, true)
          val dialog = DialogBuilder().centerPanel(configurable.createComponent())
                  .title("Specify Go SDK to continue learning")
          if (dialog.showAndGet()) {
            configurable.apply()
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