package com.jetbrains.cidr.cpp.embedded.platformio.project.builds

import com.intellij.build.BuildDescriptor
import com.intellij.build.DefaultBuildDescriptor
import com.intellij.build.events.MessageEvent
import com.intellij.build.issue.BuildIssue
import com.intellij.build.issue.BuildIssueQuickFix
import com.intellij.build.progress.BuildProgress
import com.intellij.build.progress.BuildProgressDescriptor
import com.intellij.execution.ExecutionBundle
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.OpenFileHyperlinkInfo
import com.intellij.execution.process.ProcessHandler
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.execution.CidrPathConsoleFilter
import org.jetbrains.annotations.Nls
import java.nio.file.Path

class PlatformioBuildDescriptor(project: Project,
                                @Nls title: String,
                                workingDir: String,
                                processHandler: ProcessHandler,
                                indicator: ProgressIndicator,
                                buildProgress: BuildProgress<BuildProgressDescriptor>) :
  DefaultBuildDescriptor(
    Any(), title, workingDir, System.currentTimeMillis()),
  BuildProgressDescriptor {

  init {
    withExecutionFilter(object : CidrPathConsoleFilter(project, null, Path.of(workingDir)) {
      override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        val result = super.applyFilter(line, entireLength)
        result?.resultItems?.forEach {
          val openFileDescriptor = (it.hyperlinkInfo as? OpenFileHyperlinkInfo)?.descriptor
          if (openFileDescriptor != null) {
            val messageKind =
              when {
                line.indexOf("error:") >= 0 -> MessageEvent.Kind.ERROR
                line.indexOf("warning:") >= 0 -> MessageEvent.Kind.WARNING
                else -> MessageEvent.Kind.INFO
              }
            buildProgress.buildIssue(object : BuildIssue {
              override val title: String = line
              override val description: String = title
              override val quickFixes: List<BuildIssueQuickFix> = emptyList()
              override fun getNavigatable(project: Project): Navigatable = openFileDescriptor
            }, messageKind)
          }
        }
        return result
      }

    })
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