package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains
import com.jetbrains.cidr.lang.toolchains.CidrToolEnvironment
import com.jetbrains.cidr.project.CidrProjectAction
import com.jetbrains.cidr.project.workspace.CidrWorkspace
import com.jetbrains.cidr.project.workspace.WorkspaceWithEnvironment
import com.jetbrains.cidr.toolchains.EnvironmentProblems

class ShowRemoteHostsInfoAction : CidrProjectAction() {

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = true
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = AnAction.getEventProject(e) ?: return
    val workspaces = getActionWorkspaces(e)
    if (workspaces.isEmpty()) {
      return
    }

    val log = CdIndenter(indentSize = 4)
    processSystemInfo(log)
    log.put()

    val hosts = HashSet<String>()
    ProgressManager.getInstance().runProcessWithProgressSynchronously(
      {
        getRemoteEnvironments(workspaces, project).forEach { environment ->
          val hostId = environment.hostMachine.hostId
          if (hosts.add(hostId)) {
            try {
              processRemoteHost(log, environment)
              log.put()
            }
            catch (t: Throwable) {
              log.put(hostId, "  ==>  ", stackTraceToString(t))
              log.put()
            }
          }
        }

        if (!ProgressManager.getInstance().progressIndicator.isCanceled) {
          ApplicationManager.getApplication().invokeLater {
            val infoFile = LightVirtualFile("Remote Hosts Info", PlainTextFileType.INSTANCE, log.result)
            FileEditorManager.getInstance(project).openFile(infoFile, false)
          }
        }
      }, CppDiagnosticsBundle.message("cpp.diagnostics.progress.hosts.info"), true, project)
  }

  private fun getRemoteEnvironments(workspaces: List<CidrWorkspace>, project: Project): List<CidrToolEnvironment> {
    val isRemote: (T: CidrToolEnvironment) -> Boolean =
      { it.hostMachine.isRemote && it.hostMachine.name.startsWith("Remote") }

    val activeRemoteEnvironments = workspaces
      .mapNotNull { it as? WorkspaceWithEnvironment }
      .flatMap { it.getEnvironment() }
      .filter(isRemote)

    val allRemoteEnvironments = runReadAction { CPPToolchains.getInstance().toolchains }
      .mapNotNull {
        CPPToolchains.createCPPEnvironment(project,
                                           null,
                                           it.name,
                                           EnvironmentProblems(),
                                           false,
                                           null)
      }.filter(isRemote)

    return activeRemoteEnvironments + allRemoteEnvironments
  }
}