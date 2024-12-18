package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.CommonBundle
import com.intellij.execution.ExecutionBundle
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.icons.AllIcons
import com.intellij.ide.ActivityTracker
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.application.WriteIntentReadAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsContexts.TabTitle
import com.intellij.terminal.JBTerminalSystemSettingsProviderBase
import com.intellij.terminal.TerminalExecutionConsole
import com.intellij.util.application
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.project.LOG
import com.jetbrains.cidr.cpp.embedded.platformio.refreshProject
import com.jetbrains.cidr.execution.CidrPathConsoleFilter
import com.jetbrains.cidr.system.LocalHost.TerminalEmulatorOSProcessHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.nio.file.Path
import javax.swing.JPanel

private const val TIMEOUT_MS = 30000L
private fun settingsProvider() = object : JBTerminalSystemSettingsProviderBase() {
  override fun overrideIdeShortcuts(): Boolean = true
}

fun doRun(service: PlatformioService,
          @TabTitle text: String,
          commandLine: GeneralCommandLine,
          reloadProject: Boolean) {
  val project = service.project
  try {
    val processHandler = TerminalEmulatorOSProcessHandler(commandLine)

    val console = TerminalExecutionConsole(project, processHandler, settingsProvider())
    console.addMessageFilter(CidrPathConsoleFilter(project, null, project.basePath?.let(Path::of)))

    val executor = DefaultRunExecutor.getRunExecutorInstance()
    val actions = DefaultActionGroup()

    val consolePanel = JPanel(BorderLayout())
    consolePanel.add(console.getComponent(), BorderLayout.CENTER)
    val actionToolbar = ActionManager.getInstance().createActionToolbar("RunContentExecutor", actions, false)
    actionToolbar.setTargetComponent(consolePanel)
    consolePanel.add(actionToolbar.getComponent(), BorderLayout.WEST)


    val descriptor = RunContentDescriptor(console, processHandler, consolePanel,
                                          text)
    descriptor.setActivateToolWindowWhenAdded(true)
    descriptor.setAutoFocusContent(true)

    Disposer.register(descriptor, console)

    val stopAction = object : DumbAwareAction(
      ExecutionBundle.messagePointer("action.AnAction.text.stop"),
      Presentation.NULL_STRING, AllIcons.Actions.Suspend) {
      override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

      override fun actionPerformed(e: AnActionEvent) {
        currentThreadCoroutineScope().launch(Dispatchers.IO) {
          processHandler.destroyProcess()
        }
      }

      override fun update(e: AnActionEvent) {
        if (processHandler.isProcessTerminated) {
          e.presentation.isEnabledAndVisible = false
        }
        else if (processHandler.isProcessTerminating) {
          e.presentation.isEnabled = false
        }
      }
    }
    actions.add(stopAction)

    val rerunAction = object : DumbAwareAction(
      CommonBundle.messagePointer("action.text.rerun"), Presentation.NULL_STRING, AllIcons.Actions.Restart) {
      override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

      init {
        registerCustomShortcutSet(CommonShortcuts.getRerun(), consolePanel)
      }

      override fun actionPerformed(e: AnActionEvent) {
        application.executeOnPooledThread {
          if (!project.isDisposed) {
            processHandler.destroyProcess()
            if (processHandler.waitFor(TIMEOUT_MS)) {
              application.invokeLater({
                                        doRun(service, text, commandLine, reloadProject)
                                      }, { project.isDisposed() })
            }
          }
        }
      }

      override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = processHandler.isProcessTerminated
      }

    }
    actions.add(rerunAction)

    val closeAction = object : DumbAwareAction(
      ExecutionBundle.messagePointer("close.tab.action.name"),
      Presentation.NULL_STRING, AllIcons.Actions.Cancel) {

      override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
      override fun actionPerformed(e: AnActionEvent) {
        RunContentManager.getInstance(project).removeRunContent(executor, descriptor)
      }
    }
    actions.add(closeAction)

    RunContentManager.getInstance(project).showRunContent(executor, descriptor)

    WriteIntentReadAction.run {
      FileDocumentManager.getInstance().saveAllDocuments()
    }

    console.attachToProcess(processHandler)

    processHandler.addProcessListener(object : ProcessAdapter() {
      override fun processTerminated(event: ProcessEvent) {
        ActivityTracker.getInstance().inc()
        if (reloadProject) {
          runInEdt { if (!project.isDisposed) refreshProject(project, true) }
        }
      }
    })
    processHandler.startNotify()
  }
  catch (e: Throwable) {
    LOG.warn(e)
    notifyPlatformioNotFound(project)
  }
}
