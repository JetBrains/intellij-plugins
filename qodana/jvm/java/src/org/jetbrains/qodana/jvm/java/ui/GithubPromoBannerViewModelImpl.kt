package org.jetbrains.qodana.jvm.java.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.refreshAndFindVirtualFile
import com.intellij.openapi.vfs.writeText
import com.intellij.util.io.createParentDirectories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.findQodanaConfigVirtualFile
import org.jetbrains.qodana.jvm.java.QodanaGithubPromoNotificationApplicationDismissalState
import org.jetbrains.qodana.jvm.java.QodanaGithubPromoNotificationProjectDismissalState
import org.jetbrains.qodana.settings.DefaultQodanaYamlBuilder
import org.jetbrains.qodana.settings.DefaultQodanaYamlContext
import org.jetbrains.qodana.settings.LinterUsed
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.stats.logGithubPromoAddQodanaPressed
import org.jetbrains.qodana.stats.logGithubPromoWorkflowCreatedEvent
import org.jetbrains.qodana.stats.logGithubPromoDismissed
import org.jetbrains.qodana.stats.logGithubPromoExploreQodanaPressed
import org.jetbrains.qodana.ui.ProjectVcsDataProviderImpl
import org.jetbrains.qodana.ui.ci.providers.github.DEFAULT_GITHUB_WORKFLOW_FILENAME
import org.jetbrains.qodana.ui.ci.providers.github.DefaultQodanaGithubWorkflowBuilder
import java.io.IOException
import java.nio.file.Path
import java.nio.file.FileAlreadyExistsException
import kotlin.io.path.createFile

private val LOG = Logger.getInstance(GithubPromoBannerViewModelImpl::class.java)

private const val QODANA_LANDING_PAGE = "https://www.jetbrains.com/qodana/"

class GithubPromoBannerViewModelImpl(
  private val project: Project,
  private val fileEditorViewModel: GithubPromoEditorViewModel,
  private val scope: CoroutineScope
): GithubPromoEditorViewModel.GithubPromoBannerViewModel {

  override fun addQodanaWorkflow() {
    logGithubPromoAddQodanaPressed(project)
    disableNotificationForProject()
    scope.launch {
      try {
        val qodanaYamlExists = project.findQodanaConfigVirtualFile() != null
        val workflowFile = addQodanaWorkflowFile()
        if (workflowFile != null) {
          logGithubPromoWorkflowCreatedEvent(project, qodanaYamlExists)
          val yamlFile = if (!qodanaYamlExists) addQodanaYaml() else null
          fileEditorViewModel.notifySuccessfulWorkflowAddition(GithubPromoEditorViewModel.CreatedFiles(workflowFile, yamlFile))
        }
        else {
          fileEditorViewModel.notifyFailedWorkflowAddition(
            QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.not.added.default.text")
          )
        }
      } catch (_: FileAlreadyExistsException) {
        fileEditorViewModel.notifyFailedWorkflowAddition(
          QodanaBundle.message(
            "qodana.github.promo.notification.bubble.qodana.github.workflow.not.added.file.exists.text",
            DEFAULT_GITHUB_WORKFLOW_FILENAME
          )
        )
      } catch (e: IOException) {
        LOG.warn("Could not create qodana workflow", e)
        fileEditorViewModel.notifyFailedWorkflowAddition(
          QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.not.added.default.text")
        )
      }
    }
  }

  override fun openLandingPage() {
    logGithubPromoExploreQodanaPressed(project)
    BrowserUtil.browse(QODANA_LANDING_PAGE)
  }

  override fun dismissPromo() {
    logGithubPromoDismissed(project)
    disableNotificationInApplication()
  }

  private suspend fun addQodanaWorkflowFile(): VirtualFile? {
    val projectVcsDataProvider = ProjectVcsDataProviderImpl(project, scope)
    val defaultQodanaGithubWorkflowBuilder = DefaultQodanaGithubWorkflowBuilder(projectVcsDataProvider, project)
    val location = defaultQodanaGithubWorkflowBuilder.getWorkflowFileLocation()
    if (location == null) {
      LOG.warn("Failed to create Qodana GitHub workflow file because location is not defined")
      return null
    }
    val content = defaultQodanaGithubWorkflowBuilder.workflowFile(promo = true)
    return createFile(location, content)
  }
  private suspend fun addQodanaYaml(): VirtualFile? {
    return try {
      val yamlFileContents = DefaultQodanaYamlBuilder(project).build(DefaultQodanaYamlContext(linterUsed = LinterUsed.GITHUB_PROMO))
      val projectPath = project.guessProjectDir()?.toNioPath()
      if (projectPath == null) {
        LOG.warn("Failed to create Qodana YAML file because project path is null")
        return null
      }
      val file = createFile(projectPath.resolve(QODANA_YAML_CONFIG_FILENAME), yamlFileContents)
      file
    } catch (e: IOException) {
      LOG.warn("Failed to create Qodana YAML file", e)
      null
    }
  }

  private suspend fun createFile(location: Path, content: String): VirtualFile? {
    val file = withContext(Dispatchers.IO) {
      location
        .createParentDirectories()
        .createFile()
        .refreshAndFindVirtualFile()
    }
    if (file == null) {
      LOG.warn("Failed to create file at $location")
      return null
    }
    writeAction {
      file.writeText(content)
    }
    return file
  }

  private fun disableNotificationForProject() {
    project.service<QodanaGithubPromoNotificationProjectDismissalState>().disableNotification()
  }

  private fun disableNotificationInApplication() {
    service<QodanaGithubPromoNotificationApplicationDismissalState>().disableNotification()
  }
}