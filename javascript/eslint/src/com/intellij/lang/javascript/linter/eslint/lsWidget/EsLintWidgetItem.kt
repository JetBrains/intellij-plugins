package com.intellij.lang.javascript.linter.eslint.lsWidget

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.icons.AllIcons
import com.intellij.lang.LangBundle
import com.intellij.lang.javascript.linter.eslint.EslintBundle
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.linter.GlobPatternUtil
import com.intellij.lang.javascript.linter.JSLinterEditSettingsAction
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager
import com.intellij.lang.javascript.linter.MultiRootJSLinterLanguageServiceManager.ServiceInfo
import com.intellij.lang.javascript.linter.eslint.EslintConfigurable
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration
import com.intellij.lang.javascript.linter.eslint.service.ESLintLanguageService
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceManager
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.NlsActions
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lang.lsWidget.LanguageServicePopupSection
import com.intellij.platform.lang.lsWidget.LanguageServicePopupSection.ForCurrentFile
import com.intellij.platform.lang.lsWidget.LanguageServicePopupSection.Other
import com.intellij.platform.lang.lsWidget.LanguageServiceWidgetItem
import com.intellij.platform.lang.lsWidget.OpenSettingsAction
import com.intellij.platform.lang.lsWidget.impl.fus.LanguageServiceWidgetActionKind
import com.intellij.platform.lang.lsWidget.impl.fus.LanguageServiceWidgetUsagesCollector
import icons.JavaScriptLanguageIcons
import javax.swing.Icon

class EsLintWidgetItem(
  private val project: Project,
  private val currentFile: VirtualFile?,
  private val location: MultiRootJSLinterLanguageServiceManager.Location,
  private val serviceInfo: ServiceInfo<ESLintLanguageService>,
) : LanguageServiceWidgetItem() {

  override val statusBarIcon: Icon = JavaScriptLanguageIcons.FileTypes.Eslint

  override val statusBarTooltip: String
    get() = nameWithVersion

  override val widgetActionLocation: LanguageServicePopupSection by lazy {
    if (currentFile == null) return@lazy Other
    if (!VfsUtil.isAncestor(location.workingDirectory, currentFile, true)) return@lazy Other
    if (!ProjectFileIndex.getInstance(project).isInContent(currentFile)) return@lazy Other

    val pattern = EslintConfiguration.getInstance(project).extendedState.state.filesPattern
    if (GlobPatternUtil.isFileMatchingGlobPattern(project, pattern, currentFile)) ForCurrentFile
    else Other
  }

  override val isError: Boolean = serviceInfo.service.fileLevelAnnotation != null

  private val nameWithVersion: @NlsSafe String by lazy {
    val eslint = EslintBundle.message("settings.javascript.linters.eslint.configurable.name")
    val version = location.nodePackage.version?.let { " $it" } ?: ""
    eslint + version
  }

  private val nameWithVersionAndPath: @NlsSafe String by lazy {
    val rootFolder =
      if (EslintLanguageServiceManager.getInstance(project).jsLinterServices.size > 1) {
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

  override fun createWidgetMainAction(): AnAction =
    OpenSettingsAction(EslintConfigurable::class.java, widgetActionText, JavaScriptLanguageIcons.FileTypes.Eslint)

  override fun createWidgetInlineActions(): List<AnAction> {
    if (serviceInfo.isDeleted) return emptyList()

    val restartOrStop = when (widgetActionLocation) {
      ForCurrentFile -> RestartSingleEsLintServiceAction(serviceInfo)
      else -> StopSingleEsLintServiceAction(serviceInfo)
    }
    val openSettings = OpenSettingsAction(EslintConfigurable::class.java)
    val openError = serviceInfo.service.fileLevelAnnotation?.let { OpenEsLintErrorServiceAction(project, it) }
    return listOfNotNull(openError, restartOrStop, openSettings)
  }
}


private class RestartSingleEsLintServiceAction(
  private val serviceInfo: ServiceInfo<ESLintLanguageService>,
) : AnAction(JavaScriptBundle.message("action.restart.service"),
             null,
             AllIcons.Actions.StopAndRestart),
    DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    LanguageServiceWidgetUsagesCollector
      .actionInvoked(project, LanguageServiceWidgetActionKind.RestartService, serviceInfo.service.javaClass)
    EslintLanguageServiceManager.getInstance(project).terminateService(serviceInfo)
    // Restarting DaemonCodeAnalyzer will cause the ESLint service to start if needed
    DaemonCodeAnalyzer.getInstance(project).restart("RestartSingleEsLintServiceAction")

    val eslint = EslintBundle.message("settings.javascript.linters.eslint.configurable.name")
    val title = LangBundle.message("language.services.0.service.restarted.notification.title", eslint)
    JSLanguageServiceUtil.showServiceStoppedOrRestartedNotification(project, title, "")
  }
}

private class StopSingleEsLintServiceAction(
  private val serviceInfo: ServiceInfo<ESLintLanguageService>,
) : AnAction(JavaScriptBundle.message("action.StopSingleEsLintServiceAction.text"),
             null,
             AllIcons.Actions.StopAndRestart),
    DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    LanguageServiceWidgetUsagesCollector
      .actionInvoked(project, LanguageServiceWidgetActionKind.StopService, serviceInfo.service.javaClass)
    EslintLanguageServiceManager.getInstance(project).terminateService(serviceInfo)

    val eslint = EslintBundle.message("settings.javascript.linters.eslint.configurable.name")
    val title = LangBundle.message("language.services.0.service.stopped.notification.title", eslint)
    val content = LangBundle.message("language.services.service.stopped.notification.content")
    JSLanguageServiceUtil.showServiceStoppedOrRestartedNotification(project, title, content)
  }
}

// We take the first element from the other fixes because it is the meaningful one.
// The rest are either opening settings, which we already have in a widget, or opening the config file.
private class OpenEsLintErrorServiceAction(
  private val project: Project,
  private val annotation: JSLinterFileLevelAnnotation,
) : AnAction(
  @Suppress("DialogTitleCapitalization") annotation.fixes.otherFixes?.firstOrNull()?.text ?: LangBundle.message("language.services.widget.open.settings.action"),
             null,
             AllIcons.General.Error),
    DumbAware {
  override fun actionPerformed(e: AnActionEvent) {
    val action = annotation.fixes.otherFixes?.firstOrNull()
                 ?: JSLinterEditSettingsAction(EslintConfigurable(project, true), null)

    action.invoke(project, null, null)
  }
}