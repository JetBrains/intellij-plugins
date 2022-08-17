package com.intellij.lang.javascript.frameworks.nextjs.generator

import com.intellij.execution.filters.Filter
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.boilerplate.NpxPackageDescriptor.NpxCommand
import com.intellij.lang.javascript.frameworks.nextjs.NextJsBundle
import com.intellij.lang.javascript.frameworks.react.ReactLikeProjectGenerator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.UIUtil
import icons.NextjsIcons
import javax.swing.Icon
import javax.swing.JPanel

private const val CREATE_NEXT_APP = "create-next-app"
private val ARGUMENTS = Key.create<Array<String>>("create.next.args")

class NextJsProjectGenerator : NpmPackageProjectGenerator(), ReactLikeProjectGenerator {

  override fun getDescription(): String = NextJsBundle.message("create.next.app.description")
  override fun getName(): String = NextJsBundle.message("create.next.app.name")

  override fun filters(project: Project, baseDir: VirtualFile): Array<Filter> = Filter.EMPTY_ARRAY

  override fun customizeModule(baseDir: VirtualFile, entry: ContentEntry?) {

  }

  override fun order(): Int = 1

  override fun getIcon(): Icon {
    return NextjsIcons.NextJsGen
  }

  override fun generatorArgs(project: Project, baseDir: VirtualFile, settings: Settings): Array<String> {
    return arrayOf(if (generateInTemp()) baseDir.name else ".") + (settings.getUserData(ARGUMENTS) ?: emptyArray())
  }


  override fun getNpxCommands(): List<NpxCommand> = listOf(NpxCommand(CREATE_NEXT_APP, CREATE_NEXT_APP))

  override fun packageName(): String = CREATE_NEXT_APP

  override fun presentablePackageName(): String = "create-&next-app:"

  override fun createPeer(): ProjectGeneratorPeer<Settings?> {
    return object : NpmPackageGeneratorPeer() {
      private lateinit var typescript: JBCheckBox
      override fun createPanel(): JPanel {
        val panel = super.createPanel()
        typescript = JBCheckBox(UIUtil.replaceMnemonicAmpersand(NextJsBundle.message("create.next.app.typescript.checkbox")))
        panel.add(typescript)
        return panel
      }

      override fun buildUI(settingsStep: SettingsStep) {
        super.buildUI(settingsStep)
        settingsStep.addSettingsComponent(typescript)
      }

      override fun getSettings(): Settings {
        val settings = super.getSettings()
        if (typescript.isSelected) {
          val args = arrayOf("--ts")
          settings.putUserData(ARGUMENTS, args)
        }

        return settings
      }
    }
  }

}