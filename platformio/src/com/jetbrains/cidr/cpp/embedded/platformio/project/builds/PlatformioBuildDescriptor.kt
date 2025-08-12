package com.jetbrains.cidr.cpp.embedded.platformio.project.builds

import com.intellij.build.BuildDescriptor
import com.intellij.build.DefaultBuildDescriptor
import com.intellij.build.progress.BuildProgressDescriptor
import com.intellij.execution.ExecutionBundle
import com.intellij.execution.process.ProcessHandler
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.execution.CidrPathWithOffsetConsoleFilter
import org.jetbrains.annotations.Nls
import java.nio.file.Path

class PlatformioBuildDescriptor(project: Project,
                                @Nls title: String,
                                workingDir: String,
                                processHandler: ProcessHandler,
                                indicator: ProgressIndicator) :
  DefaultBuildDescriptor(
    Any(), title, workingDir, System.currentTimeMillis()),
  BuildProgressDescriptor {

  init {
    withExecutionFilter(CidrPathWithOffsetConsoleFilter(project, null, Path.of(workingDir)))
    withRestartAction(object : DumbAwareAction(ClionEmbeddedPlatformioBundle.messagePointer("action.rerun.text"),
                                               AllIcons.Actions.Rerun) {

      override fun actionPerformed(e: AnActionEvent) {
        ActionManager.getInstance().getAction("Build").actionPerformed(e)
      }

      override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
      }

      override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = processHandler.isProcessTerminated
      }
    })

    withRestartAction(object : DumbAwareAction(
      ExecutionBundle.messagePointer("action.AnAction.text.stop"),
      AllIcons.Actions.Suspend) {
      override fun actionPerformed(e: AnActionEvent) {
        indicator.cancel()
      }

      override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
      }

      override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible =
          with(processHandler) { isStartNotified && !isProcessTerminating && !isProcessTerminated }
      }
    })
  }

  override fun getBuildDescriptor(): BuildDescriptor = this
}