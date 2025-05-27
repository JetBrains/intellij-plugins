package org.jetbrains.qodana.jvm.java.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import com.intellij.openapi.vfs.refreshAndFindVirtualFile
import com.intellij.openapi.vfs.writeText
import com.intellij.util.io.createParentDirectories
import com.intellij.vcsUtil.VcsFileUtil.addFilesToVcsWithConfirmation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.findQodanaConfigVirtualFile
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.stats.GithubPromoCreateWorkflowEvent
import org.jetbrains.qodana.stats.GithubPromoNotificationCreation
import org.jetbrains.qodana.stats.logGithubPromoAddQodanaWorkflowEvent
import org.jetbrains.qodana.stats.logGithubPromoNotificationCreationEvent
import org.jetbrains.qodana.ui.ProjectVcsDataProviderImpl
import org.jetbrains.qodana.ui.ci.providers.github.DefaultQodanaGithubWorkflowBuilder
import org.jetbrains.qodana.ui.ci.providers.github.GitHubCIFileChecker
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.createFile

private val LOG = Logger.getInstance(GithubPromoNotificationService::class.java)

@Service(Service.Level.PROJECT)
class GithubPromoNotificationService(private val project: Project, private val scope: CoroutineScope) {

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
      if (_qodanaJobPresent == true) {
        logGithubPromoNotificationCreationEvent(project, GithubPromoNotificationCreation.QODANA_CI_ALREADY_EXISTS)
      }
    }
    return _qodanaJobPresent
  }

  fun addQodanaFiles() {
    scope.launch(QodanaDispatchers.IO) {
      val files = listOf(addQodanaWorkflow(project), addQodanaYaml(project))
      addFilesToVcsWithConfirmation(project, files.filterNotNull())
    }
  }

  /**
   * Adds Qodana GitHub workflow file if it's not present yet in the background
   */
  private suspend fun addQodanaWorkflow(project: Project): VirtualFile? {
    try {
      val projectVcsDataProvider = ProjectVcsDataProviderImpl(project, scope)
      val defaultQodanaGithubWorkflowBuilder = DefaultQodanaGithubWorkflowBuilder(projectVcsDataProvider, project)
      val location = defaultQodanaGithubWorkflowBuilder.getWorkflowFileLocation()
      if (location == null) {
        LOG.warn("Failed to create Qodana GitHub workflow file because location is null")
        return null
      }
      val content = defaultQodanaGithubWorkflowBuilder.workflowFile(promo = true)
      val file = createFile(location, content)
      if (file == null) {
        notifyQodanaActionWorkflowNotAdded()
      }
      else {
        notifyQodanaActionWorkflowAdded()
      }
      return file
    } catch (e: IOException) {
      LOG.warn("Failed to create Qodana GitHub workflow file", e)
      notifyQodanaActionWorkflowNotAdded()
      return null
    }
  }

  /**
   * Adds a qodana.yaml file if it's not present yet in the background
   */
  private suspend fun addQodanaYaml(project: Project): VirtualFile? {
    val isQodanaYamlPresent = project.findQodanaConfigVirtualFile() != null
    if (isQodanaYamlPresent) {
      logGithubPromoAddQodanaWorkflowEvent(project, GithubPromoCreateWorkflowEvent.QODANA_YAML_EXISTS)
      return null
    }
    val yamlFileContents = GithubPromoQodanaYamlBuilder(project).build()
    return try {
      val projectPath = project.guessProjectDir()?.toNioPath()
      if (projectPath == null) {
        LOG.warn("Failed to create Qodana YAML file because project path is null")
        notifyQodanaYamlNotAdded()
        return null
      }
      val file = createFile(projectPath.resolve(QODANA_YAML_CONFIG_FILENAME), yamlFileContents)
      if (file == null) {
        notifyQodanaYamlNotAdded()
      }
      file
    } catch (e: IOException) {
      LOG.warn("Failed to create Qodana YAML file", e)
      notifyQodanaYamlNotAdded()
      null
    }
  }

  private suspend fun createFile(location: Path, content: String): VirtualFile? {
    val file = location
      .createParentDirectories()
      .createFile()
      .refreshAndFindVirtualFile()
    if (file == null) {
      LOG.warn("Failed to create file at $location")
      return null
    }
    edtWriteAction {
      file.writeText(content)
    }
    return file
  }

  private fun notifyQodanaYamlNotAdded() {
    NotificationGroupManager.getInstance()
      .getNotificationGroup("Qodana")
      .createNotification(QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.yaml.not.added.text"), NotificationType.ERROR)
      .notify(project)
  }

  private fun notifyQodanaActionWorkflowNotAdded() {
    logGithubPromoAddQodanaWorkflowEvent(project, GithubPromoCreateWorkflowEvent.FAILED)
    QodanaNotifications.General.notification(
      null,
      QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.not.added.text"),
      NotificationType.ERROR,
      withQodanaIcon = true
    ).notify(project)
  }

  private fun notifyQodanaActionWorkflowAdded() {
    logGithubPromoAddQodanaWorkflowEvent(project, GithubPromoCreateWorkflowEvent.CREATED)
    QodanaNotifications.General.notification(
      null,
      QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.added.text"),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    ).notify(project)
  }

}