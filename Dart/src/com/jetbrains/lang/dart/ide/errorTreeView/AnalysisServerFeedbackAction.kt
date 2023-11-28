// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.ReportFeedbackService
import com.intellij.ide.actions.SendFeedbackAction.Companion.getDescription
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.ToolWindowManager.Companion.getInstance
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.jetbrains.lang.dart.DartBundle
import com.jetbrains.lang.dart.sdk.DartSdk
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class AnalysisServerFeedbackAction : DumbAwareAction(DartBundle.messagePointer("analysis.server.status.good.text"),
                                                     Presentation.NULL_STRING, AllIcons.General.Balloon) {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    if (project == null) return

    sendFeedback(project)
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun update(e: AnActionEvent) {
    val presentation = e.presentation
    val project = e.project
    if (isApplicable(project)) {
      presentation.setEnabledAndVisible(true)
    }
    else {
      presentation.setEnabledAndVisible(false)
    }
  }

  private fun sendFeedback(project: Project) {
    val appInfo = ApplicationInfoEx.getInstanceEx()
    val isEAP = appInfo.isEAP
    val intellijBuild = if (isEAP) appInfo.getBuild().asStringWithoutProductCode() else appInfo.getBuild().asString()
    val sdkVersion = getSdkVersion(project)

    service<ReportFeedbackService>().coroutineScope.launch {
      withBackgroundProgress(project, IdeBundle.message("reportProblemAction.progress.title.submitting"), true) {
        val platformDescription = StringUtil.replace(getDescription(project), ";", " ").trim { it <= ' ' }

        val url = DartBundle.message("dart.feedback.url", urlEncode("Analyzer Feedback from IntelliJ"))
        val body = DartBundle.message("dart.feedback.template", intellijBuild, sdkVersion, platformDescription)

        BrowserUtil.browse(url + urlEncode(body + "\n"), project)
      }
    }
  }

  protected fun getSdkVersion(project: Project): String {
    val sdk = DartSdk.getDartSdk(project)
    return sdk?.version ?: "<NO SDK>"
  }

  companion object {
    private fun isApplicable(project: Project?): Boolean {
      return project != null && getInstance(project).getToolWindow(DartProblemsView.TOOLWINDOW_ID) != null
    }

    private fun urlEncode(input: String): String {
      return URLEncoder.encode(input, StandardCharsets.UTF_8)
    }
  }
}