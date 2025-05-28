package org.jetbrains.qodana.jvm.java.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.projectRoots.JavaSdkType
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.stats.logGithubPromoDismissed
import org.jetbrains.qodana.stats.logGithubPromoExploreQodanaPressed
import org.jetbrains.qodana.stats.logGithubPromoNotificationShown
import java.util.function.Function
import javax.swing.JComponent
private const val QODANA_LANDING_PAGE = "https://www.jetbrains.com/qodana/"

class QodanaGithubCIPromoNotificationProvider: EditorNotificationProvider, DumbAware {

  override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
    return Function {
      when {
        isNotificationDisabled(project) -> null
        !isGithubActionsFile(file) -> null
        !isJavaProject(project) -> null
        isQodanaJobPresent(project) -> null
        else -> createNotificationPanel(file, project)
      }
    }
  }

  // copied from community/plugins/github/github-core/src/org/jetbrains/plugins/github/extensions/githubYamlFileDetection.kt
  private fun isGithubActionsFile(virtualFile: VirtualFile): Boolean {
    return isGithubActionYamlFile(virtualFile) || isGithubWorkflowYamlFile(virtualFile)
  }

  // copied from community/plugins/github/github-core/src/org/jetbrains/plugins/github/extensions/githubYamlFileDetection.kt
  private fun isGithubActionYamlFile(virtualFile: VirtualFile): Boolean {
    val fileName = virtualFile.name
    return virtualFile.isFile
           && (FileUtilRt.extensionEquals(fileName, "yml") || FileUtilRt.extensionEquals(fileName, "yaml"))
           && virtualFile.nameWithoutExtension == "action"
  }

  // copied from community/plugins/github/github-core/src/org/jetbrains/plugins/github/extensions/githubYamlFileDetection.kt
  private fun isGithubWorkflowYamlFile(virtualFile: VirtualFile): Boolean {
    val fileName = virtualFile.name
    val filePath = virtualFile.path
    val workflowDirIndex = filePath.indexOf("/workflows")
    val githubDirIndex = filePath.indexOf(".github/")
    return virtualFile.isFile
           && (FileUtilRt.extensionEquals(fileName, "yml") || FileUtilRt.extensionEquals(fileName, "yaml"))
           && workflowDirIndex != -1
           && githubDirIndex != -1
           && workflowDirIndex > githubDirIndex
  }

  private fun isJavaProject(project: Project): Boolean {
    val isJavaSdk = { sdk: Sdk? -> sdk != null && sdk.sdkType is JavaSdkType }
    val projectSdk = ProjectRootManager.getInstance(project).projectSdk
    if (isJavaSdk(projectSdk)) {
      return true
    }
    val modules = project.modules
    for (module in modules) {
      val moduleSdk = ModuleRootManager.getInstance(module).sdk
      if (isJavaSdk(moduleSdk)) {
        return true
      }
    }
    return false
  }

  // the default value is true to not show notification until computation completes
  private fun isQodanaJobPresent(project: Project): Boolean = project.service<GithubPromoNotificationService>().qodanaJobPresent ?: true

  private fun createNotificationPanel(file: VirtualFile, project: Project): EditorNotificationPanel {
    logGithubPromoNotificationShown(project)
    val panel = EditorNotificationPanel(EditorNotificationPanel.Status.Info)
    panel.text = QodanaBundle.message("qodana.github.promo.notification.text")

    panel.createActionLabel(QodanaBundle.message("qodana.github.promo.notification.explore.button.text")) {
      logGithubPromoExploreQodanaPressed(project)
      BrowserUtil.browse(QODANA_LANDING_PAGE)
    }

    panel.createActionLabel(QodanaBundle.message("qodana.github.promo.notification.add.workflow.button.text")) {
      try {
        val service = project.service<GithubPromoNotificationService>()
        service.addQodanaFiles()
      } finally {
        disableNotificationForProject(project)
        EditorNotifications.getInstance(project).updateNotifications(file)
      }
    }

    panel.setCloseAction {
      logGithubPromoDismissed(project)
      disableNotificationInApplication()
      EditorNotifications.getInstance(project).updateNotifications(file)
    }
    return panel
  }

  private fun isNotificationDisabled(project: Project): Boolean {
    return service<QodanaGithubPromoNotificationApplicationDismissalState>().dismissedState ||
           project.service<QodanaGithubPromoNotificationProjectDismissalState>().dismissedState
  }

  private fun disableNotificationForProject(project: Project) {
    project.service<QodanaGithubPromoNotificationProjectDismissalState>().disableNotification()
  }

  private fun disableNotificationInApplication() {
    service<QodanaGithubPromoNotificationApplicationDismissalState>().disableNotification()
  }
}

@Service
@State(name = "qodana.github.promo.notification.application.dismissed", storages = [Storage(value = "qodana.xml")])
internal class QodanaGithubPromoNotificationApplicationDismissalState: BaseQodanaGithubPromoNotificationDismissalState()

@Service(Service.Level.PROJECT)
@State(name = "qodana.github.promo.notification.project.dismissed", storages = [Storage(value = "qodana.xml")])
internal class QodanaGithubPromoNotificationProjectDismissalState: BaseQodanaGithubPromoNotificationDismissalState()


internal open class BaseQodanaGithubPromoNotificationDismissalState: PersistentStateComponent<BaseQodanaGithubPromoNotificationDismissalState.State> {
  class State : BaseState() {
    var dismissed: Boolean by property(false)
  }

  var dismissedState: Boolean = state.dismissed
    private set

  override fun getState(): BaseQodanaGithubPromoNotificationDismissalState.State {
    return State().apply { dismissed = dismissedState }
  }

  override fun loadState(state: BaseQodanaGithubPromoNotificationDismissalState.State) {
    dismissedState = state.dismissed
  }

  fun disableNotification() {
    dismissedState = true
  }
}