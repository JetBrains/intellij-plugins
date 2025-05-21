package org.jetbrains.qodana.jvm.java.ui

import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.PropertiesComponent
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
import org.jetbrains.qodana.stats.logGithubPromoExploreQodanaPress
import java.util.function.Function
import javax.swing.JComponent

private const val QODANA_GITHUB_PROMO_PROJECT_DISABLED_KEY = "qodana.github.promo.project.disabled"
private const val QODANA_GITHUB_PROMO_APPLICATION_DISABLED_KEY = "qodana.github.promo.application.disabled"
private const val QODANA_DOCUMENTATION_URL = "https://www.jetbrains.com/help/qodana/about-qodana.html"

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
  private fun isQodanaJobPresent(project: Project): Boolean = project.service<GithubPromoFileCreatorService>().qodanaJobPresent ?: true

  private fun createNotificationPanel(file: VirtualFile, project: Project): EditorNotificationPanel {
    val panel = EditorNotificationPanel(EditorNotificationPanel.Status.Info)
    panel.text = QodanaBundle.message("qodana.github.promo.notification.text")

    panel.createActionLabel(QodanaBundle.message("qodana.github.promo.notification.explore.button.text")) {
      logGithubPromoExploreQodanaPress(project)
      BrowserUtil.browse(QODANA_DOCUMENTATION_URL)
    }

    panel.createActionLabel(QodanaBundle.message("qodana.github.promo.notification.add.workflow.button.text")) {
      try {
        val service = project.service<GithubPromoFileCreatorService>()
        service.addQodanaYaml(project)
        service.addQodanaWorkflow(project)
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
    return PropertiesComponent.getInstance(project).getBoolean(QODANA_GITHUB_PROMO_PROJECT_DISABLED_KEY, false)
           || PropertiesComponent.getInstance().getBoolean(QODANA_GITHUB_PROMO_APPLICATION_DISABLED_KEY, false)
  }

  private fun disableNotificationForProject(project: Project) {
    PropertiesComponent.getInstance(project).setValue(QODANA_GITHUB_PROMO_PROJECT_DISABLED_KEY, true)
  }

  private fun disableNotificationInApplication() {
    PropertiesComponent.getInstance().setValue(QODANA_GITHUB_PROMO_APPLICATION_DISABLED_KEY, true)
  }
}