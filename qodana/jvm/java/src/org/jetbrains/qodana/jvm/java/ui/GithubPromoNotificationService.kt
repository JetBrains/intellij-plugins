package org.jetbrains.qodana.jvm.java.ui

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
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
import kotlinx.coroutines.Job
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
import org.jetbrains.qodana.ui.problemsView.isSetupCiEnabled
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

  fun addQodanaFiles(): Job {
    return scope.launch(QodanaDispatchers.IO) {
      val success = doAddQodanaFiles()
      if (!success) {
        notifyQodanaActionWorkflowNotAdded()
      }
    }
  }

  private suspend fun doAddQodanaFiles(): Boolean {
    val qodanaYamlExists = project.findQodanaConfigVirtualFile() != null
    val workflowFile = addQodanaWorkflow()
    if (workflowFile != null) {
      val yamlFile = if (!qodanaYamlExists) addQodanaYaml() else null
      logGithubPromoAddQodanaWorkflowEvent(project, qodanaYamlExists)
      withContext(Dispatchers.EDT) {
        FileEditorManager.getInstance(project).openFile(workflowFile, true)
      }
      addFilesToVcsWithConfirmation(project, listOfNotNull(workflowFile, yamlFile))
      return true
    } else {
      return  false
    }
  }

  private suspend fun addQodanaWorkflow(): VirtualFile? {
    try {
      val projectVcsDataProvider = ProjectVcsDataProviderImpl(project, scope)
      val defaultQodanaGithubWorkflowBuilder = DefaultQodanaGithubWorkflowBuilder(projectVcsDataProvider, project)
      val location = defaultQodanaGithubWorkflowBuilder.getWorkflowFileLocation()
      if (location == null) {
        LOG.warn("Failed to create Qodana GitHub workflow file because location is not defined")
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
  private suspend fun addQodanaYaml(): VirtualFile? {
    return try {
      val yamlFileContents = GithubPromoQodanaYamlBuilder(project).build()
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

  private fun notifyQodanaActionWorkflowNotAdded(tryAgain: Boolean = true) {
    val notification = QodanaNotifications.General.notification(
      QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.not.added.title"),
      QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.not.added.text"),
      NotificationType.ERROR,
      withQodanaIcon = false
    )
    if (tryAgain) {
      notification.addAction(createTryAgainAction())
    }
    notification.notify(project)
  }

  private fun createTryAgainAction() = object : NotificationAction(QodanaBundle.message("qodana.github.promo.notification.bubble.try.add.workflow.again")) {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
      scope.launch(QodanaDispatchers.IO) {
        try {
          val success = doAddQodanaFiles()
          if (success) {
            notification.expire()
            return@launch
          }
          // fallback to just regular "Add to CI action"
          if (isSetupCiEnabled()) {
            val addToCIAction = ActionManager.getInstance().getAction("Qodana.AddQodanaToCiAction")
            withContext(Dispatchers.EDT) {
              ActionUtil.performAction(addToCIAction, e)
              notification.expire()
            }
            return@launch
          }
        } finally {
          // notify without action to try again in case of failure
          notifyQodanaActionWorkflowNotAdded(tryAgain = false)
        }
      }
    }
  }
}