package com.intellij.openRewrite.run.before

import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.impl.ExecutionManagerImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.scratch.ScratchFileTypeIcon
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openRewrite.OPEN_REWRITE_NOTIFICATION_GROUP_ID
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openRewrite.run.OpenRewriteExternalSystemBridge
import com.intellij.openRewrite.run.OpenRewriteRunConfiguration
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.newvfs.ManagingFS
import com.intellij.task.ProjectTaskContext
import com.intellij.task.ProjectTaskManager
import com.intellij.task.impl.EmptyCompileScopeBuildTaskImpl
import com.intellij.task.impl.ProjectTaskManagerImpl
import com.intellij.util.concurrency.Semaphore
import com.intellij.util.io.Compressor
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.asPromise
import org.jetbrains.concurrency.resolvedPromise
import java.io.File
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import javax.swing.Icon
import kotlin.io.path.pathString
import kotlinx.coroutines.CancellationException

internal val INSTALL_BEFORE_RUN_TASK_KEY: Key<OpenRewriteInstallBeforeRunTask> = Key.create("OpenRewriteInstallBeforeRunTask")
private const val INSTALL_BEFORE_RUN_TASK_NOTIFICATION_DISPLAY_ID = "openRewrite.install.class.recipe"

internal fun getScratchOutputDirectory(project: Project): File? {
  val root = CompilerManager.getInstance(project).javacCompilerWorkingDir
  return if (root != null) File(root, "rewrite/out") else null
}

internal class OpenRewriteInstallBeforeRunTaskProvider : BeforeRunTaskProvider<OpenRewriteInstallBeforeRunTask>() {
  override fun getId(): Key<OpenRewriteInstallBeforeRunTask> = INSTALL_BEFORE_RUN_TASK_KEY

  override fun getName(): String = OpenRewriteBundle.message("open.rewrite.install.before.run.task.name")

  override fun getIcon(): Icon = ScratchFileTypeIcon(OpenRewriteIcons.OpenRewrite)

  override fun getTaskIcon(task: OpenRewriteInstallBeforeRunTask): Icon = icon

  override fun getDescription(task: OpenRewriteInstallBeforeRunTask): String {
    val fileName = task.scratchFileUrl?.substringAfterLast("/")?.substringAfterLast('\\') ?: ""
    return OpenRewriteBundle.message("open.rewrite.install.before.run.task.description", fileName.substringBeforeLast("."))
  }

  override fun isConfigurable(): Boolean = true

  override fun configureTask(
    context: DataContext,
    configuration: RunConfiguration,
    task: OpenRewriteInstallBeforeRunTask,
  ): Promise<Boolean> {
    val dialog = OpenRewriteInstallBeforeRunTaskDialog(configuration.project,
                                                       OpenRewriteBundle.message("open.rewrite.install.before.run.task.dialog.title"),
                                                       task)
    return resolvedPromise(dialog.showAndGet())
  }

  override fun canExecuteTask(configuration: RunConfiguration, task: OpenRewriteInstallBeforeRunTask): Boolean {
    if (configuration !is OpenRewriteRunConfiguration) return false

    val scratchFileUrl = task.scratchFileUrl ?: return false
    return scratchFileUrl.endsWith(JavaFileType.DOT_DEFAULT_EXTENSION)
  }

  override fun createTask(runConfiguration: RunConfiguration): OpenRewriteInstallBeforeRunTask? {
    if (runConfiguration !is OpenRewriteRunConfiguration) return null

    return OpenRewriteInstallBeforeRunTask()
  }

  private fun compile(
    configuration: OpenRewriteRunConfiguration,
    environment: ExecutionEnvironment,
    task: OpenRewriteInstallBeforeRunTask,
  ): Boolean {
    ApplicationManager.getApplication().invokeAndWait {
      (FileDocumentManager.getInstance() as FileDocumentManagerImpl).saveAllDocuments(false)
    }
    //flush pending IO tasks, if any:
    ManagingFS.getInstance().flushPendingUpdates()

    val projectTask = EmptyCompileScopeBuildTaskImpl(true)
    val context = ProjectTaskContext(ExecutionManagerImpl.EXECUTION_SESSION_ID_KEY[environment], configuration)
    environment.copyUserDataTo(context)
    configuration.putUserData(INSTALL_BEFORE_RUN_TASK_KEY, task)

    try {
      val result = CompletableFuture<Boolean>()
      ProjectTaskManagerImpl.putBuildOriginator(configuration.project, OpenRewriteInstallBeforeRunTaskProvider::class.java)
      ProjectTaskManager.getInstance(configuration.project).run(context, projectTask)
        .onProcessed(Consumer<ProjectTaskManager.Result?> { result.complete(it != null && !it.hasErrors() && !it.isAborted) })
      return result.get()
    }
    finally {
      configuration.putUserData(INSTALL_BEFORE_RUN_TASK_KEY, null)
    }
  }

  private fun build(
    project: Project,
    name: String,
  ): Boolean {
    val path = getScratchOutputDirectory(project)?.toPath() ?: return false
    try {
      val jar = Compressor.Jar(path.parent.resolve(name))
      jar.use {
        jar.addDirectory(path)
      }
      return true
    }
    catch (e: IOException) {
      ApplicationManager.getApplication().invokeAndWait {
        NotificationGroupManager.getInstance().getNotificationGroup(OPEN_REWRITE_NOTIFICATION_GROUP_ID)
          .createNotification(OpenRewriteBundle.message("open.rewrite.install.before.run.task.failed.to.build.artifact"),
                              NotificationType.ERROR)
          .setDisplayId(INSTALL_BEFORE_RUN_TASK_NOTIFICATION_DISPLAY_ID)
          .setIcon(OpenRewriteIcons.OpenRewrite)
          .notify(project)
      }
      return false
    }
  }

  private fun install(
    configuration: RunConfiguration,
    task: OpenRewriteInstallBeforeRunTask,
    name: String,
  ): Boolean {
    val parentPath = getScratchOutputDirectory(configuration.project)?.toPath()?.parent ?: return false
    val artifactPath = parentPath.resolve(name)
    val coordinates = task.getCoordinates()

    val commandLineBuilder = StringBuilder("install:install-file")
    commandLineBuilder.append(" -Dfile=${artifactPath.pathString}")
    commandLineBuilder.append(" -DgroupId=${coordinates.groupId}")
    commandLineBuilder.append(" -DartifactId=${coordinates.artifactId}")
    commandLineBuilder.append(" -Dversion=${coordinates.version}")
    commandLineBuilder.append(" -Dpackaging=${coordinates.packaging}")
    val commandLine = commandLineBuilder.toString()

    return OpenRewriteExternalSystemBridge.EP_NAME.findFirstSafe { it.installFile(configuration.project, commandLine) } != null
  }

  override fun executeTask(
    context: DataContext,
    configuration: RunConfiguration,
    environment: ExecutionEnvironment,
    task: OpenRewriteInstallBeforeRunTask,
  ): Boolean {
    if (configuration !is OpenRewriteRunConfiguration) return false

    try {
      val reload = OpenRewriteRecipeService.getInstance(environment.project).reload()?.asPromise()
      if (reload != null) {
        val done = Semaphore(1)
        reload.onProcessed { done.up() }
        done.waitFor()
      }

      if (!compile(configuration, environment, task)) return false
      val coordinates = task.getCoordinates()
      val name = "${coordinates.artifactId}-${coordinates.version}.jar"
      if (!build(configuration.project, name)) return false

      return install(configuration, task, name)
    }
    catch (e: CancellationException) {
      throw e
    }
    catch (e: Exception) {
      Logger.getInstance(OpenRewriteInstallBeforeRunTaskProvider::class.java).error(e.message, e)
      return false
    }
  }
}