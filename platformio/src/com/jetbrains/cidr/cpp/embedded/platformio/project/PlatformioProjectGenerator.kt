package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.GeneratorPeerImpl
import com.intellij.platform.ProjectGeneratorPeer
import com.jetbrains.cidr.cpp.cmake.projectWizard.generators.CLionProjectGenerator
import com.jetbrains.cidr.cpp.embedded.EmbeddedBundle
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.home.PlatformioProjectSettingsDialogStep
import icons.ClionEmbeddedPlatformioIcons
import org.jetbrains.annotations.Nls
import javax.swing.Icon
import javax.swing.JPanel

fun useWebView(): Boolean = Registry.`is`("PlatformIO.use.webview")

val PROJECT_INIT_KEY = Key<BoardInfo>("platformio-board-info")

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
      project.putUserData(PROJECT_INIT_KEY, settings.get() ?: BoardInfo(SourceTemplate.ARDUINO, emptyList()))
      PlatformioProjectOpenProcessor().linkPlatformioProject(project, baseDir)
    }
  }


  override fun createPeer(): ProjectGeneratorPeer<Ref<BoardInfo?>> = GeneratorPeerImpl(Ref<BoardInfo?>(), JPanel())

  override fun getGroupName(): String = "Embedded"

  override fun getGroupOrder(): Int = GroupOrders.EMBEDDED.order + 1

  override fun getGroupDisplayName(): @Nls String = EmbeddedBundle.messagePointer("embedded.display.name").get()

  override fun getName(): String = ClionEmbeddedPlatformioBundle.message("platformio.project.type")

  override fun getDescription(): String = ClionEmbeddedPlatformioBundle.message("platformio.project.description")

}
