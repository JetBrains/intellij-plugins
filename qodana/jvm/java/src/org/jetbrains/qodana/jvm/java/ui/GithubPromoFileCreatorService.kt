package org.jetbrains.qodana.jvm.java.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.findDirectory
import com.intellij.openapi.vfs.refreshAndFindVirtualFile
import com.intellij.openapi.vfs.writeText
import com.intellij.util.io.createParentDirectories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.findQodanaConfigVirtualFile
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.stats.GithubPromoCreateWorkflowResult
import org.jetbrains.qodana.stats.logGithubPromoAddQodanaWorkflow
import org.jetbrains.qodana.ui.ProjectVcsDataProviderImpl
import org.jetbrains.qodana.ui.ci.providers.github.DefaultQodanaGithubWorkflowBuilder
import org.jetbrains.qodana.ui.ci.providers.github.GitHubCIFileChecker
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.createFile

private val LOG = Logger.getInstance(GithubPromoFileCreatorService::class.java)

@Service(Service.Level.PROJECT)
class GithubPromoFileCreatorService(private val project: Project, private val scope: CoroutineScope) {

  private enum class ComputationState {
    NOT_STARTED,
    INITIALIZED,
  }

  @Volatile
  private var _qodanaJobPresent: Boolean? = null
  private val qodanaJobSearchState: AtomicReference<ComputationState> = AtomicReference(ComputationState.NOT_STARTED)
  /**
   * Returns null if the computation is not yet finished
   */
  val qodanaJobPresent: Boolean?
    get() = determineIfQodanaJobPresent()

  /**
   * Launches [runnable] in background if it hasn't been launched yet and returns
   */
  private fun computeJobOnce(stateReference: AtomicReference<ComputationState>, runnable: suspend () -> Unit) {
    if (stateReference.compareAndSet(ComputationState.NOT_STARTED, ComputationState.INITIALIZED)) {
      scope.launch(QodanaDispatchers.IO) {
        runnable()
      }
    }
  }

  private fun determineIfQodanaJobPresent(): Boolean? {
    computeJobOnce(qodanaJobSearchState) {
      val workflowDir = readAction {
        project.guessProjectDir()?.findDirectory(".github/workflows")
      } ?: run {
        _qodanaJobPresent = false
        return@computeJobOnce
      }
      val githubCIFileChecker = GitHubCIFileChecker(project, scope)

      _qodanaJobPresent = readAction { workflowDir.children }
        .map {
          scope.async {
            try {
              githubCIFileChecker.isQodanaPresent(it)
            } catch (e: IOException) {
              LOG.warn("Failed to check if Qodana job is present in file ${it.path}", e)
              false
            }
          }
        }
        .awaitAll().any { it }
        .also {
          logGithubPromoAddQodanaWorkflow(project, GithubPromoCreateWorkflowResult.ALREADY_EXISTS)
        }
    }
    return _qodanaJobPresent
  }

  /**
   * Adds Qodana GitHub workflow file if it's not present yet in the background
   */
  fun addQodanaWorkflow(project: Project) {
    scope.launch(QodanaDispatchers.IO) {
      try {
        val projectVcsDataProvider = ProjectVcsDataProviderImpl(project, this)
        val defaultQodanaGithubWorkflowBuilder = DefaultQodanaGithubWorkflowBuilder(projectVcsDataProvider, project)
        val location = defaultQodanaGithubWorkflowBuilder.getWorkflowFileLocation()
        if (location == null) {
          LOG.warn("Failed to create Qodana GitHub workflow file because location is null")
          return@launch
        }
        val content = defaultQodanaGithubWorkflowBuilder.workflowFile()
        if (!createFile(location, content)) {
          notifyQodanaActionWorkflowNotAdded()
        }
        else {
          notifyQodanaActionWorkflowAdded()
        }
      } catch (e: IOException) {
        LOG.warn("Failed to create Qodana GitHub workflow file", e)
        notifyQodanaActionWorkflowNotAdded()
      }
    }
  }

  /**
   * Adds a qodana.yaml file if it's not present yet in the background
   */
  fun addQodanaYaml(project: Project) {
    scope.launch(QodanaDispatchers.IO) {
      val isQodanaYamlPresent = project.findQodanaConfigVirtualFile() != null
      if (isQodanaYamlPresent) {
        return@launch
      }
      val yamlFileContents = GithubPromoQodanaYamlBuilder(project).build()
      try {
        val projectPath = project.guessProjectDir()?.toNioPath()
        if (projectPath == null) {
          LOG.warn("Failed to create Qodana YAML file because project path is null")
          notifyQodanaYamlNotAdded()
          return@launch
        }
        if (!createFile(projectPath.resolve(QODANA_YAML_CONFIG_FILENAME), yamlFileContents)) {
          notifyQodanaYamlNotAdded()
        }
      } catch (e: IOException) {
        LOG.warn("Failed to create Qodana YAML file", e)
        notifyQodanaYamlNotAdded()
      }
    }
  }

  private suspend fun createFile(location: Path, content: String): Boolean {
    val file = location
      .createParentDirectories()
      .createFile()
      .refreshAndFindVirtualFile()
    if (file == null) {
      LOG.warn("Failed to create file at $location")
      return false
    }
    edtWriteAction {
      file.writeText(content)
    }
    return true
  }

  private fun notifyQodanaYamlNotAdded() {
    NotificationGroupManager.getInstance()
      .getNotificationGroup("Qodana")
      .createNotification(QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.yaml.not.added.text"), NotificationType.ERROR)
      .notify(project)
  }

  private fun notifyQodanaActionWorkflowNotAdded() {
    logGithubPromoAddQodanaWorkflow(project, GithubPromoCreateWorkflowResult.FAILED)
    NotificationGroupManager.getInstance()
      .getNotificationGroup("Qodana")
      .createNotification(QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.not.added.text"), NotificationType.ERROR)
      .notify(project)
  }

  private fun notifyQodanaActionWorkflowAdded() {
    logGithubPromoAddQodanaWorkflow(project, GithubPromoCreateWorkflowResult.CREATED)
    NotificationGroupManager.getInstance()
      .getNotificationGroup("Qodana")
      .createNotification(QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.added.text"), NotificationType.INFORMATION)
      .notify(project)
  }

}