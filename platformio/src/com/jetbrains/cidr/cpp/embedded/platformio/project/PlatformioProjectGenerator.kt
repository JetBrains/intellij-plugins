package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.execution.process.OSProcessHandler
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.GeneratorPeerImpl
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.util.ExceptionUtil
import com.jetbrains.cidr.cpp.cmake.projectWizard.generators.CLionProjectGenerator
import com.jetbrains.cidr.cpp.embedded.EmbeddedBundle
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioUsagesCollector
import com.jetbrains.cidr.cpp.embedded.platformio.home.PlatformioProjectSettingsDialogStep
import com.jetbrains.cidr.cpp.embedded.platformio.ui.notifyPlatformioFailed
import com.jetbrains.cidr.cpp.embedded.platformio.ui.notifyPlatformioNotFound
import icons.ClionEmbeddedPlatformioIcons
import org.jetbrains.annotations.Nls
import java.io.IOException
import javax.swing.Icon
import javax.swing.JPanel

fun useWebView(): Boolean = Registry.`is`("PlatformIO.use.webview")

class PlatformioProjectGenerator : CLionProjectGenerator<Ref<BoardInfo?>>(), CustomStepProjectGenerator<Ref<BoardInfo?>> {


  override fun createStep(projectGenerator: DirectoryProjectGenerator<Ref<BoardInfo?>>,
                          callback: AbstractNewProjectStep.AbstractCallback<Ref<BoardInfo?>>): AbstractActionWithPanel {
    if (useWebView()) return PlatformioProjectSettingsDialogStep(projectGenerator, callback)
    else return PlatformioProjectSettingsStep(projectGenerator, callback)
  }

  override fun getLogo(): Icon = ClionEmbeddedPlatformioIcons.Platformio
  override fun generateProject(project: Project, baseDir: VirtualFile, settings: Ref<BoardInfo?>, module: Module) {
    super.generateProject(project, baseDir, settings, module)
    if (!useWebView()) {
      cliGenerateProject(project, baseDir, settings.get())
    }
  }

  private fun cliGenerateProject(project: Project, baseDir: VirtualFile, boardInfo: BoardInfo?) {
    /* This method starts two-stage process
     1. PlatformIO utility is started asynchronously
     2. When it's done, another asynchronous code writes empty source code stub if no main.c or main.cpp is generated
   */

    PlatformioUsagesCollector.NEW_PROJECT.log(project)

    object : Task.Modal(project, ClionEmbeddedPlatformioBundle.message("platformio.init.title"), true) {
      override fun run(indicator: ProgressIndicator) {
        val commandLine = PlatfromioCliBuilder(project)
          .withParams("init")
          .withParams(boardInfo?.parameters ?: emptyList())
          .withRedirectErrorStream(true)
          .withVerboseAllowed(true)
          .build()
        try {
          val processHandler = OSProcessHandler(commandLine)
          processHandler.startNotify()
          processHandler.waitFor()
        }
        catch (e: IOException) {
          LOG.warn(e)
          notifyPlatformioNotFound(project)
        }
      }
    }.queue()
    ApplicationManager.getApplication().runWriteAction {
      finishFileStructure(project, baseDir, boardInfo?.template ?: SourceTemplate.GENERIC)
    }

    PlatformioProjectOpenProcessor().linkPlatformioProject(project, baseDir)
  }

  override fun createPeer(): ProjectGeneratorPeer<Ref<BoardInfo?>> = GeneratorPeerImpl(Ref<BoardInfo?>(), JPanel())

  private fun finishFileStructure(project: Project,
                                  baseDir: VirtualFile,
                                  template: SourceTemplate) {
    baseDir.refresh(false, true)
    if (template !== SourceTemplate.NONE) {
      val srcFolder = baseDir.findChild("src")
      if (srcFolder == null || !srcFolder.isDirectory) {
        showError(ClionEmbeddedPlatformioBundle.message("src.not.found"))
        return
      }
      if (srcFolder.findChild("main.cpp") == null && srcFolder.findChild("main.c") == null) {
        try {
          val virtualFile = srcFolder.createChildData(this, template.fileName)
          virtualFile.setBinaryContent(template.content.toByteArray(Charsets.US_ASCII))
          ApplicationManager.getApplication().invokeLater {
            if (!project.isDisposed) {
              val descriptor = OpenFileDescriptor(project, virtualFile)
              FileEditorManager.getInstance(project).openEditor(descriptor, true)
            }
          }
        }
        catch (e: IOException) {
          showError(ExceptionUtil.getThrowableText(e))
          return
        }
      }
    }
  }

  private fun showError(message: @NlsContexts.NotificationContent String) {
    notifyPlatformioFailed(null, ClionEmbeddedPlatformioBundle.message("project.init.failed"), null, message)
  }

  override fun getGroupName(): String = "Embedded"

  override fun getGroupOrder(): Int = GroupOrders.EMBEDDED.order + 1

  override fun getGroupDisplayName(): @Nls String = EmbeddedBundle.messagePointer("embedded.display.name").get()

  override fun getName(): String = ClionEmbeddedPlatformioBundle.message("platformio.project.type")

  override fun getDescription(): String = ClionEmbeddedPlatformioBundle.message("platformio.project.description")

}
