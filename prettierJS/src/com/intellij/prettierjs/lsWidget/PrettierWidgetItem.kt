// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.lsWidget

import com.intellij.execution.process.ProcessOutput
import com.intellij.icons.AllIcons
import com.intellij.javascript.nodejs.npm.InstallNodeLocalDependenciesAction
import com.intellij.javascript.nodejs.npm.NpmManager
import com.intellij.javascript.nodejs.settings.NodeSettingsConfigurable
import com.intellij.lang.LangBundle
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.linter.JsqtProcessOutputViewer
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager.ServiceInfo
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsActions
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServicePopupSection
import com.intellij.platform.lang.lsWidget.LanguageServicePopupSection.ForCurrentFile
import com.intellij.platform.lang.lsWidget.LanguageServicePopupSection.Other
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.platform.lang.lsWidget.OpenSettingsAction
import com.intellij.platform.lang.lsWidget.impl.fus.LanguageServiceWidgetActionKind
import com.intellij.platform.lang.lsWidget.impl.fus.LanguageServiceWidgetUsagesCollector
import com.intellij.prettierjs.*
import javax.swing.Icon

class PrettierWidgetItem(
  private val project: Project,
  private val currentFile: VirtualFile?,
  private val location: MultiRootJSLinterLanguageServiceManager.Location,
  private val serviceInfo: ServiceInfo<PrettierLanguageServiceImpl>,
) : LanguageServiceWidgetItem() {

  override val statusBarIcon: Icon = PrettierUtil.ICON

  override val statusBarTooltip: String
    get() = nameWithVersion

  override val widgetActionLocation: LanguageServicePopupSection by lazy {
    if (currentFile == null) return@lazy Other
    if (!PrettierUtil.isFormattingAllowedForFile(project, currentFile)) return@lazy Other
    else ForCurrentFile
  }

  override val isError: Boolean = serviceInfo.service.error != null

  private val nameWithVersion: @NlsSafe String by lazy {
    val prettier = PrettierBundle.message("configurable.PrettierConfigurable.display.name")
    val version = location.nodePackage.version?.let { " $it" } ?: ""
    prettier + version
  }

  private val nameWithVersionAndPath: @NlsSafe String by lazy {
    val rootFolder = if (PrettierLanguageServiceManager.getInstance(project).jsLinterServices.size > 1) {
      " …/${location.workingDirectory.name}"
    }
    else ""

    nameWithVersion + rootFolder
  }

  private val widgetActionText: @NlsActions.ActionText String
    get() = if (serviceInfo.isDeleted) {
      LangBundle.message("language.services.widget.item.shutdown.normally", nameWithVersionAndPath)
    }
    else nameWithVersionAndPath

  override fun createWidgetMainAction(): AnAction = OpenSettingsAction(PrettierConfigurable::class.java, widgetActionText, PrettierUtil.ICON)

  override fun createWidgetInlineActions(): List<AnAction> {
    if (serviceInfo.isDeleted) return emptyList()

    val restartOrStop = if (isError) {
      RestartPrettierServiceAction(serviceInfo, location)
    }
    else {
      StopPrettierServiceAction(serviceInfo)
    }
    val openSettings = OpenSettingsAction(PrettierConfigurable::class.java)
    val openConfig = OpenConfigurationAction(project, currentFile ?: location.workingDirectory, AllIcons.Ide.ConfigFile)
    val openError = serviceInfo.service.error?.let {
      PerformPrettierErrorServiceAction(project, currentFile ?: location.workingDirectory, it)
    }
    return listOfNotNull(openError, openConfig, restartOrStop, openSettings)
  }
}


private class RestartPrettierServiceAction(
  private val serviceInfo: ServiceInfo<PrettierLanguageServiceImpl>,
  private val location: MultiRootJSLinterLanguageServiceManager.Location,
) : DumbAwareAction(JavaScriptBundle.message("action.restart.service"), null, AllIcons.Actions.StopAndRestart) {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    LanguageServiceWidgetUsagesCollector.actionInvoked(project, LanguageServiceWidgetActionKind.RestartService, serviceInfo.service.javaClass)
    PrettierLanguageServiceManager.getInstance(project).terminateService(serviceInfo)

    // Getting will cause the Prettier service to start if needed
    PrettierLanguageService.getInstance(project, location.workingDirectory, location.nodePackage)

    val prettier = PrettierBundle.message("configurable.PrettierConfigurable.display.name")
    val title = LangBundle.message("language.services.0.service.restarted.notification.title", prettier)
    JSLanguageServiceUtil.showServiceStoppedOrRestartedNotification(project, title, "")
  }
}

private class StopPrettierServiceAction(
  private val serviceInfo: ServiceInfo<PrettierLanguageServiceImpl>,
) : DumbAwareAction(JavaScriptBundle.message("action.StopSingleEsLintServiceAction.text"), null, AllIcons.Actions.StopAndRestart) {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    LanguageServiceWidgetUsagesCollector.actionInvoked(project, LanguageServiceWidgetActionKind.StopService, serviceInfo.service.javaClass)
    PrettierLanguageServiceManager.getInstance(project).terminateService(serviceInfo)

    val prettier = PrettierBundle.message("configurable.PrettierConfigurable.display.name")
    val title = LangBundle.message("language.services.0.service.stopped.notification.title", prettier)
    val content = LangBundle.message("language.services.service.stopped.notification.content")
    JSLanguageServiceUtil.showServiceStoppedOrRestartedNotification(project, title, content)
  }
}

private class PerformPrettierErrorServiceAction(
  private val project: Project,
  private val file: VirtualFile,
  private val error: PrettierError,
) : DumbAwareAction(when (error) {
                      is PrettierError.ShowDetails -> PrettierBundle.message("prettier.action.show.details")
                      is PrettierError.Unsupported -> PrettierBundle.message("prettier.action.show.details")
                      is PrettierError.NodeSettings -> PrettierBundle.message("prettier.action.configure.node")
                      is PrettierError.InstallPackage -> PrettierBundle.message("prettier.action.install.package", NpmManager.getInstance(project).npmInstallPresentableText)
                      is PrettierError.EditSettings -> PrettierBundle.message("prettier.action.open.settings.simple")
                    }, null, AllIcons.General.Error) {
  override fun actionPerformed(e: AnActionEvent) {
    when (error) {
      is PrettierError.InstallPackage -> {
        val packageJson = PackageJsonUtil.findUpPackageJson(file)
        if (packageJson != null) {
          InstallNodeLocalDependenciesAction.runAndShowConsole(project, packageJson)
        }
        else {
          ShowSettingsUtil.getInstance().editConfigurable(project, PrettierConfigurable(project))
        }
      }
      is PrettierError.EditSettings -> {
        ShowSettingsUtil.getInstance().editConfigurable(project, PrettierConfigurable(project))
      }
      is PrettierError.NodeSettings -> {
        NodeSettingsConfigurable.showSettingsDialog(project)
      }
      is PrettierError.ShowDetails -> {
        val output = ProcessOutput()
        output.appendStderr(error.message)
        JsqtProcessOutputViewer.show(project, PrettierBundle.message("prettier.formatter.notification.title"), PrettierUtil.ICON, null, null, output)
      }
      is PrettierError.Unsupported -> {
        val output = ProcessOutput()
        output.appendStderr(error.message)
        JsqtProcessOutputViewer.show(project, PrettierBundle.message("prettier.formatter.notification.title"), PrettierUtil.ICON, null, null, output)
      }
    }
  }
}

