package com.intellij.deno.generator

import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoUtil.getDefaultDenoIcon
import com.intellij.deno.UseDeno
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WebProjectTemplate
import com.intellij.javascript.nodejs.execution.withBackgroundProgress
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.Module
import com.intellij.openapi.observable.util.whenTextChanged
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.Icon
import javax.swing.JComponent

private class DenoProjectTemplateGenerator : WebProjectTemplate<DenoProjectTemplateSettings>() {
  override fun getDescription(): @NlsContexts.DetailedDescription String = DenoBundle.message("deno.name")
  override fun getName(): @NlsContexts.Label String = DenoBundle.message("deno.name")
  override fun generateProject(project: Project, baseDir: VirtualFile, settings: DenoProjectTemplateSettings, module: Module) {
    val denoSettings = DenoSettings.getService(project)
    val denoPath = settings.denoPath
    denoSettings.setDenoPath(denoPath)
    denoSettings.setUseDeno(UseDeno.CONFIGURE_AUTOMATICALLY)

    val commandLine = GeneralCommandLine(denoPath, "init").withWorkingDirectory(baseDir.toNioPath())

    try {
      val handler = withBackgroundProgress(project, DenoBundle.message("progress.title.deno.init")) {
        KillableColoredProcessHandler(commandLine)
      }

      handler.addProcessListener(object : ProcessAdapter() {
        override fun processTerminated(event: ProcessEvent) {
          baseDir.refresh(false, true)
        }
      })
    }
    catch (e: ExecutionException) {
      NotificationGroupManager.getInstance().getNotificationGroup("Project generator")
        .createNotification(JavaScriptBundle.message("notification.title.cannot.generate", getName()),
                            (StringUtil.getMessage(e) ?: ""),
                            NotificationType.ERROR)
        .notify(project)
    }
  }

  override fun createPeer(): ProjectGeneratorPeer<DenoProjectTemplateSettings> {
    return object : ProjectGeneratorPeer<DenoProjectTemplateSettings> {
      private val settings = DenoProjectTemplateSettings()
      private val path = TextFieldWithBrowseButton().also { it.text = settings.denoPath }

      override fun addSettingsListener(listener: ProjectGeneratorPeer.SettingsListener) {
        path.whenTextChanged(null) {
          listener.stateChanged(true)
        }
      }

      override fun getComponent(myLocationField: TextFieldWithBrowseButton, checkValid: Runnable): JComponent {
        return panel {
          row(DenoBundle.message("deno.path")) {
            cell(path)
              .align(AlignX.FILL)
              .bindText(settings::denoPath)
          }
        }
      }

      override fun buildUI(settingsStep: SettingsStep) {
        settingsStep.addSettingsField(DenoBundle.message("deno.path"), path)
      }

      override fun getSettings(): DenoProjectTemplateSettings {
        return settings.also { it.denoPath = path.text }
      }

      override fun validate(): ValidationInfo? =
        if (path.text.isEmpty()) ValidationInfo(DenoBundle.message("deno.path.empty"), path) else null

      override fun isBackgroundJobRunning(): Boolean = false
    }
  }

  override fun getId(): String = "Deno"
  override fun getIcon(): Icon = getDefaultDenoIcon()
}

class DenoProjectTemplateSettings(private var path: String = DenoSettings.getService(ProjectManager.getInstance().defaultProject).getDenoPath()) {
  var denoPath: String
    get() = path
    set(value) {
      path = value
    }
}
