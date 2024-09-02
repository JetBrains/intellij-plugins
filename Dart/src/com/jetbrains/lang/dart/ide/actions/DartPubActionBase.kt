// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.actions

import com.intellij.CommonBundle
import com.intellij.execution.ExecutionException
import com.intellij.execution.actions.StopProcessAction
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.filters.UrlFilter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.icons.AllIcons
import com.intellij.ide.IdeCoreBundle
import com.intellij.ide.actions.CloseActiveTabAction
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.UIBundle
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import com.intellij.ui.content.MessageView
import com.jetbrains.lang.dart.DartBundle
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService
import com.jetbrains.lang.dart.excludeBuildAndToolCacheFolders
import com.jetbrains.lang.dart.flutter.FlutterUtil
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter
import com.jetbrains.lang.dart.ide.runner.DartRelativePathsConsoleFilter
import com.jetbrains.lang.dart.sdk.DartConfigurable
import com.jetbrains.lang.dart.sdk.DartSdk
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil
import com.jetbrains.lang.dart.sdk.DartSdkUtil
import com.jetbrains.lang.dart.util.PubspecYamlUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import java.io.File
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

abstract class DartPubActionBase : AnAction(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val moduleAndPubspec = getModuleAndPubspecYamlFile(e)
    // Defer to the Flutter plugin if appropriate.
    val visible = moduleAndPubspec != null &&
                  !(FlutterUtil.isFlutterPluginInstalled() && FlutterUtil.isPubspecDeclaringFlutter(moduleAndPubspec.second))
    e.presentation.isVisible = visible
    e.presentation.isEnabled = visible && !isInProgress
  }

  protected abstract fun getTitle(project: Project, pubspecYamlFile: VirtualFile): @Nls String

  protected abstract fun calculatePubParameters(project: Project, pubspecYamlFile: VirtualFile): Array<String>?

  override fun actionPerformed(e: AnActionEvent) {
    val (module, pubspecYamlFile) = getModuleAndPubspecYamlFile(e) ?: return
    performPubAction(module, pubspecYamlFile, true)
  }

  fun performPubAction(module: Module, pubspecYamlFile: VirtualFile, allowModalDialogs: Boolean) {
    var sdk = DartSdk.getDartSdk(module.project)

    if (sdk == null && allowModalDialogs) {
      val answer = Messages.showDialog(module.project,
                                       DartBundle.message("dart.sdk.is.not.configured"),
                                       getTitle(module.project, pubspecYamlFile),
                                       arrayOf(DartBundle.message("setup.dart.sdk"), CommonBundle.getCancelButtonText()),
                                       Messages.OK,
                                       Messages.getErrorIcon())
      if (answer != Messages.OK) return

      DartConfigurable.openDartSettings(module.project)
      sdk = DartSdk.getDartSdk(module.project)
    }

    if (sdk == null) return

    val useDartPub = StringUtil.compareVersionNumbers(sdk.version, DART_PUB_MIN_SDK_VERSION) >= 0
    val exeFile = if (useDartPub) File(DartSdkUtil.getDartExePath(sdk)) else File(DartSdkUtil.getPubPath(sdk))

    if (!exeFile.isFile) {
      if (allowModalDialogs) {
        val answer = Messages.showDialog(module.project,
                                         DartBundle.message("dart.sdk.bad.dartpub.path", exeFile.path),
                                         getTitle(module.project, pubspecYamlFile),
                                         arrayOf(DartBundle.message("setup.dart.sdk"), CommonBundle.getCancelButtonText()),
                                         Messages.OK,
                                         Messages.getErrorIcon())
        if (answer == Messages.OK) DartConfigurable.openDartSettings(module.project)
      }

      return
    }

    val pubParameters = calculatePubParameters(module.project, pubspecYamlFile)

    if (pubParameters != null) {
      val command = GeneralCommandLine().withWorkingDirectory(Path.of(pubspecYamlFile.parent.path))

      if (FlutterUtil.isFlutterModule(module)) {
        FlutterUtil.getFlutterRoot(sdk.homePath)?.let { flutterRoot ->
          command.environment["FLUTTER_ROOT"] = FileUtil.toSystemDependentName(flutterRoot)
        }
      }

      setupPubExePath(command, sdk)
      command.addParameters(*pubParameters)

      doPerformPubAction(module, pubspecYamlFile, command, getTitle(module.project, pubspecYamlFile))
    }
  }

  private class PubToolWindowContentInfo(
    val module: Module,
    val pubspecYamlFile: VirtualFile,
    val command: GeneralCommandLine,
    val actionTitle: @NlsContexts.NotificationTitle String,
    val console: ConsoleView,
  ) {
    var rerunPubCommandAction: RerunPubCommandAction? = null
    var stopProcessAction: StopProcessAction? = null

    override fun equals(other: Any?): Boolean = other is PubToolWindowContentInfo && command === other.command

    override fun hashCode(): Int = command.hashCode()
  }

  private class RerunPubCommandAction(
    private val info: PubToolWindowContentInfo,
  ) : DumbAwareAction(DartBundle.message("rerun.pub.command.action.name"),
                      DartBundle.message("rerun.pub.command.action.description"),
                      AllIcons.Actions.Execute) {
    private var myProcessHandler: OSProcessHandler? = null

    init {
      registerCustomShortcutSet(CommonShortcuts.getRerun(), info.console.getComponent())
    }

    fun setProcessHandler(processHandler: OSProcessHandler) {
      myProcessHandler = processHandler
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
      e.presentation.isEnabled = !isInProgress && myProcessHandler != null && myProcessHandler!!.isProcessTerminated
    }

    override fun actionPerformed(e: AnActionEvent) =
      doPerformPubAction(info.module, info.pubspecYamlFile, info.command, info.actionTitle)
  }

  companion object {
    const val PUB_ENV_VAR_NAME: String = "PUB_ENVIRONMENT"

    private const val GROUP_DISPLAY_ID: @NonNls String = "Dart Pub Tool"
    private val PUB_TOOL_WINDOW_CONTENT_INFO_KEY = Key.create<PubToolWindowContentInfo>("PUB_TOOL_WINDOW_CONTENT_INFO_KEY")

    private const val DART_PUB_MIN_SDK_VERSION = "2.10"
    private const val DART_RUN_TEST_MIN_SDK_VERSION = "2.11"

    private val ourInProgress = AtomicBoolean(false)

    @JvmStatic
    fun isUseDartRunTestInsteadOfPubRunTest(dartSdk: DartSdk): Boolean =
      StringUtil.compareVersionNumbers(dartSdk.version, DART_RUN_TEST_MIN_SDK_VERSION) >= 0

    @JvmStatic
    fun setupPubExePath(commandLine: GeneralCommandLine, dartSdk: DartSdk) {
      val useDartPub = StringUtil.compareVersionNumbers(dartSdk.version, DART_PUB_MIN_SDK_VERSION) >= 0
      if (useDartPub) {
        commandLine.withExePath(FileUtil.toSystemDependentName(DartSdkUtil.getDartExePath(dartSdk)))
        commandLine.addParameter("pub")
      }
      else {
        commandLine.withExePath(FileUtil.toSystemDependentName(DartSdkUtil.getPubPath(dartSdk)))
      }
    }

    @JvmStatic
    val pubEnvValue: String
      get() {
        val clientId = DartAnalysisServerService.getClientId()
        return System.getenv(PUB_ENV_VAR_NAME)?.let { "$it:$clientId" } ?: clientId
      }

    var isInProgress: Boolean
      @JvmStatic get() = ourInProgress.get()
      private set(inProgress) = ourInProgress.set(inProgress)

    @Suppress("unused") // external plugins compatibility
    @JvmStatic
    fun setIsInProgress(inProgress: Boolean) {
      isInProgress = inProgress
    }

    private fun getModuleAndPubspecYamlFile(e: AnActionEvent): Pair<Module, VirtualFile>? {
      val module = e.getData(PlatformCoreDataKeys.MODULE)
      val psiFile = e.getData(CommonDataKeys.PSI_FILE)

      if (module != null && psiFile != null && psiFile.name.equals(PubspecYamlUtil.PUBSPEC_YAML, ignoreCase = true)) {
        val file = psiFile.originalFile.virtualFile
        return if (file != null) Pair(module, file) else null
      }
      return null
    }

    private fun doPerformPubAction(
      module: Module,
      pubspecYamlFile: VirtualFile,
      command: GeneralCommandLine,
      actionTitle: @NlsContexts.NotificationTitle String,
    ) {
      FileDocumentManager.getInstance().saveAllDocuments()

      try {
        if (ourInProgress.compareAndSet(false, true)) {
          command.withEnvironment(PUB_ENV_VAR_NAME, pubEnvValue)

          val processHandler = OSProcessHandler(command)

          processHandler.addProcessListener(object : ProcessAdapter() {
            override fun processTerminated(event: ProcessEvent) {
              ourInProgress.set(false)

              ApplicationManager.getApplication().invokeLater {
                if (!module.isDisposed) {
                  excludeBuildAndToolCacheFolders(module, pubspecYamlFile)

                  // refresh later than exclude, otherwise IDE may start indexing excluded folders
                  VfsUtil.markDirtyAndRefresh(true, true, true, pubspecYamlFile.parent)

                  if (DartSdkLibUtil.isDartSdkEnabled(module)) {
                    DartAnalysisServerService.getInstance(module.project).serverReadyForRequest()
                  }
                }
              }
            }
          })

          module.project.service<DartPubActionsService>().cs.launch {
            showPubOutputConsole(module, command, processHandler, pubspecYamlFile, actionTitle)
          }
        }
      }
      catch (e: ExecutionException) {
        ourInProgress.set(false)

        // maybe better show it in Messages tool window console?
        Notifications.Bus.notify(
          Notification(GROUP_DISPLAY_ID, actionTitle, DartBundle.message("dart.pub.exception", e.message), NotificationType.ERROR))
      }
      catch (t: Throwable) {
        ourInProgress.set(false)
        throw t
      }
    }

    private suspend fun showPubOutputConsole(
      module: Module,
      command: GeneralCommandLine,
      processHandler: OSProcessHandler,
      pubspecYamlFile: VirtualFile,
      actionTitle: @Nls String,
    ) {
      val project = module.project
      val messageView = MessageView.getInstance(project)
      messageView.awaitInitialized()

      val console: ConsoleView
      var info = findExistingInfoForCommand(messageView, command)

      if (info != null) {
        // rerunning the same pub command in the same tool window tab (corresponding tool window action invoked)
        console = info.console
        console.clear()
      }
      else {
        val actionManager = serviceAsync<ActionManager>()
        console = readAction { createConsole(project, pubspecYamlFile) }
        info = PubToolWindowContentInfo(module, pubspecYamlFile, command, actionTitle, console)

        withContext(Dispatchers.EDT) {
          val actionToolbar = createToolWindowActionsBar(actionManager, info)

          val toolWindowPanel = SimpleToolWindowPanel(false, true)
          toolWindowPanel.setContent(console.component)
          toolWindowPanel.toolbar = actionToolbar.component

          val content = ContentFactory.getInstance().createContent(toolWindowPanel.component, actionTitle, true)
          content.putUserData(PUB_TOOL_WINDOW_CONTENT_INFO_KEY, info)
          Disposer.register(content, console)

          val contentManager = messageView.contentManager
          removeOldTabs(contentManager)
          contentManager.addContent(content)
          contentManager.setSelectedContent(content)

          ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.MESSAGES_WINDOW)?.activate(null, true)
        }
      }

      info.rerunPubCommandAction!!.setProcessHandler(processHandler)
      info.stopProcessAction!!.setProcessHandler(processHandler)

      processHandler.addProcessListener(object : ProcessAdapter() {
        override fun processTerminated(event: ProcessEvent) {
          console.print(IdeCoreBundle.message("finished.with.exit.code.text.message", event.exitCode), ConsoleViewContentType.SYSTEM_OUTPUT)
        }
      })

      console.print(DartBundle.message("working.dir.0", FileUtil.toSystemDependentName(pubspecYamlFile.parent.path)) + "\n",
                    ConsoleViewContentType.SYSTEM_OUTPUT)
      console.attachToProcess(processHandler)
      processHandler.startNotify()
    }

    private fun findExistingInfoForCommand(messageView: MessageView, command: GeneralCommandLine): PubToolWindowContentInfo? {
      for (content in messageView.contentManager.contents) {
        val info = content.getUserData(PUB_TOOL_WINDOW_CONTENT_INFO_KEY)
        if (info?.command === command) return info
      }
      return null
    }

    private fun createConsole(project: Project, pubspecYamlFile: VirtualFile): ConsoleView {
      val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
      consoleBuilder.setViewer(true)
      consoleBuilder.addFilter(DartConsoleFilter(project, pubspecYamlFile))
      consoleBuilder.addFilter(DartRelativePathsConsoleFilter(project, pubspecYamlFile.parent.path))
      consoleBuilder.addFilter(UrlFilter())
      return consoleBuilder.console
    }

    private fun createToolWindowActionsBar(actionManager: ActionManager, info: PubToolWindowContentInfo): ActionToolbar {
      val actionGroup = DefaultActionGroup()

      val rerunPubCommandAction = RerunPubCommandAction(info)
      info.rerunPubCommandAction = rerunPubCommandAction
      actionGroup.addAction(rerunPubCommandAction)

      val stopProcessAction =
        StopProcessAction(DartBundle.message("stop.pub.process.action"), DartBundle.message("stop.pub.process.action"), null)
      info.stopProcessAction = stopProcessAction
      actionGroup.addAction(stopProcessAction)

      actionGroup.add(actionManager.getAction(IdeActions.ACTION_PIN_ACTIVE_TAB))

      val closeContentAction: AnAction = CloseActiveTabAction()
      closeContentAction.templatePresentation.icon = AllIcons.Actions.Cancel
      closeContentAction.templatePresentation.setText(UIBundle.messagePointer("tabbed.pane.close.tab.action.name"))
      actionGroup.add(closeContentAction)

      val toolbar = actionManager.createActionToolbar("DartPubAction", actionGroup, false)
      toolbar.targetComponent = info.console.component
      return toolbar
    }

    private fun removeOldTabs(contentManager: ContentManager) {
      for (content in contentManager.contents) {
        if (!content.isPinned && content.isCloseable && content.getUserData(PUB_TOOL_WINDOW_CONTENT_INFO_KEY) != null) {
          contentManager.removeContent(content, false)
        }
      }
    }
  }
}

@Service(Service.Level.PROJECT)
private class DartPubActionsService(val cs: CoroutineScope)
