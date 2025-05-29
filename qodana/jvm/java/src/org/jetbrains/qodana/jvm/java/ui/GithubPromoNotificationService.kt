package org.jetbrains.qodana.jvm.java.ui

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.refreshAndFindVirtualFile
import com.intellij.openapi.vfs.writeText
import com.intellij.ui.EditorNotifications
import com.intellij.util.io.createParentDirectories
import com.intellij.vcsUtil.VcsFileUtil.addFilesToVcsWithConfirmation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.findQodanaConfigVirtualFile
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.stats.logGithubPromoAddQodanaWorkflowEvent
import org.jetbrains.qodana.ui.ProjectVcsDataProviderImpl
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.github.DefaultQodanaGithubWorkflowBuilder
import org.jetbrains.qodana.ui.ci.providers.github.GitHubCIFileChecker
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.createFile

private val LOG = Logger.getInstance(GithubPromoNotificationService::class.java)

@Service(Service.Level.PROJECT)
class GithubPromoNotificationService(private val project: Project, private val scope: CoroutineScope) {

  private enum class ProcessState {
    NOT_STARTED,
    STARTED,
  }

  @Volatile
  private var _qodanaJobPresent: Boolean? = null
  private val qodanaJobSearchState: AtomicReference<ProcessState> = AtomicReference(ProcessState.NOT_STARTED)
  /**
   * Returns null if the computation is not yet finished
   */
  val qodanaJobPresent: Boolean?
    get() = determineIfQodanaJobPresent()

  /**
   * Launches [runnable] in background if it hasn't been launched yet and returns
   */
  private fun launchProcessOnce(stateReference: AtomicReference<ProcessState>, runnable: suspend () -> Unit) {
    if (stateReference.compareAndSet(ProcessState.NOT_STARTED, ProcessState.STARTED)) {
      scope.launch(QodanaDispatchers.IO) {
        runnable()
      }
    }
  }

  private fun determineIfQodanaJobPresent(): Boolean? {
    launchProcessOnce(qodanaJobSearchState) {
      GitHubCIFileChecker(project, scope).ciFileFlow.filter { ciFile ->
        // do not update notifications if state didn't change
        ciFile.qodanaPresent() != _qodanaJobPresent
      }.collect { ciFile ->
        _qodanaJobPresent = ciFile.qodanaPresent()
        EditorNotifications.getInstance(project).updateAllNotifications()
      }
    }
    return _qodanaJobPresent
  }

  private fun CIFile?.qodanaPresent() = this is CIFile.ExistingWithQodana

  fun addQodanaFiles() {
    scope.launch(QodanaDispatchers.IO) {
      val qodanaYamlExists = project.findQodanaConfigVirtualFile() != null
      val workflowFile = addQodanaWorkflow()
      val yamlFile = if (!qodanaYamlExists) addQodanaYaml() else null
      if (workflowFile != null) {
        logGithubPromoAddQodanaWorkflowEvent(project, qodanaYamlExists)
        withContext(Dispatchers.EDT) {
          FileEditorManager.getInstance(project).openFile(workflowFile, true)
        }
        addFilesToVcsWithConfirmation(project, listOfNotNull(workflowFile, yamlFile))
      } else {
        notifyQodanaActionWorkflowNotAdded()
      }
    }
  }

  /**
   * Adds Qodana GitHub workflow file if it's not present yet in the background
   * Returns null if the file was not added due to an error. In case of an error, the notification is shown
   */
  private suspend fun addQodanaWorkflow(): VirtualFile? {
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
      return file
    } catch (e: IOException) {
      LOG.warn("Failed to create Qodana GitHub workflow file", e)
      return null
    }
  }

  /**
   * Adds a qodana.yaml file if it's not present yet in the background
   * Returns null if the file was not added due to an error
   */
  private suspend fun addQodanaYaml(): VirtualFile? {
    val yamlFileContents = GithubPromoQodanaYamlBuilder(project).build()
    return try {
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

  private fun notifyQodanaActionWorkflowNotAdded() {
    QodanaNotifications.General.notification(
      QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.not.added.title"),
      QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.not.added.text"),
      NotificationType.ERROR,
      withQodanaIcon = false
    ).notify(project)
  }
}