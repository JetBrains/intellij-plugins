// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.execution.configurations.CommandLineTokenizer
import com.intellij.execution.filters.Filter
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.lang.javascript.boilerplate.NpxPackageDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.NlsContexts.DialogMessage
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.TextAccessor
import com.intellij.util.ArrayUtilRt
import com.intellij.util.Consumer
import com.intellij.util.PathUtil
import com.intellij.util.text.SemVer
import com.intellij.util.ui.UIUtil
import com.intellij.xml.util.XmlStringUtil
import icons.AngularIcons
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CLI_PACKAGE
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.io.File
import java.util.regex.Pattern
import javax.swing.Icon
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class AngularCliProjectGenerator : NpmPackageProjectGenerator() {

  override fun getId(): String {
    return "AngularCLI"
  }

  @Nls
  override fun getName(): String {
    return Angular2Bundle.message("angular.action.new-project.name")
  }

  override fun getDescription(): String {
    return Angular2Bundle.message("angular.action.new-project.description")
  }

  override fun getIcon(): Icon {
    return AngularIcons.Angular2
  }

  override fun customizeModule(baseDir: VirtualFile, entry: ContentEntry?) {
    entry?.addDefaultAngularExcludes(baseDir)
  }

  override fun generatorArgs(project: Project, baseDir: VirtualFile): Array<String> {
    return ArrayUtilRt.EMPTY_STRING_ARRAY
  }

  override fun generatorArgs(project: Project, baseDir: VirtualFile, settings: Settings): Array<String> {
    val ngSettings = settings as AngularCLIProjectSettings
    val result = ArrayList<String>()
    result.add("new")
    result.add(baseDir.name)
    val tokenizer = CommandLineTokenizer(ngSettings.options)
    while (tokenizer.hasMoreTokens()) {
      result.add(tokenizer.nextToken())
    }

    if (isPackageGreaterOrEqual(settings.myPackage, 7, 0, 0) && ngSettings.useDefaults) {
      if (result.none { param -> param == "--defaults" || param.startsWith("--defaults=") }) {
        result.add("--defaults")
      }
    }

    if (isPackageGreaterOrEqual(settings.myPackage, 16, 0, 0)) {
      if (result.none { param -> param == "--standalone" || param.startsWith("--standalone=") }) {
        result.add("--standalone=${ngSettings.useStandalone}")
      }
    }

    return ArrayUtilRt.toStringArray(result)
  }

  override fun filters(project: Project, baseDir: VirtualFile): Array<Filter> {
    return arrayOf(AngularCliFilter(project, baseDir.parent.path))
  }

  override fun executable(pkg: NodePackage): String {
    return ng(pkg.systemDependentPath)
  }

  override fun packageName(): String {
    return ANGULAR_CLI_PACKAGE
  }

  override fun presentablePackageName(): String {
    return Angular2Bundle.message("angular.action.new-project.presentable-package-name")
  }

  override fun getNpxCommands(): List<NpxPackageDescriptor.NpxCommand> {
    return listOf(NpxPackageDescriptor.NpxCommand(ANGULAR_CLI_PACKAGE, NG_EXECUTABLE))
  }

  override fun validateProjectPath(path: String): String? {
    return validateFolderName(path, Angular2Bundle.message("angular.action.new-project.label-project-name"))
           ?: super.validateProjectPath(path)
  }

  override fun createPeer(): ProjectGeneratorPeer<Settings> {
    return AngularCLIProjectGeneratorPeer()
  }

  override fun workingDir(settings: Settings, baseDir: VirtualFile): File {
    return VfsUtilCore.virtualToIoFile(baseDir).parentFile
  }


  override fun postInstall(
    project: Project,
    baseDir: VirtualFile,
    workingDir: File,
  ): Runnable {
    return Runnable {
      ApplicationManager.getApplication().executeOnPooledThread {
        super.postInstall(project, baseDir, workingDir).run()
        AngularCliUtil.createRunConfigurations(project, baseDir)
      }
    }
  }


  private inner class AngularCLIProjectGeneratorPeer : NpmPackageProjectGenerator.NpmPackageGeneratorPeer() {

    private var myContentRoot: TextAccessor? = null

    private lateinit var myOptionsTextField: SchematicOptionsTextField
    private lateinit var myUseDefaults: JCheckBox
    private lateinit var myUseStandalone: JCheckBox

    override fun createPanel(): JPanel {
      val panel = super.createPanel()

      myOptionsTextField = SchematicOptionsTextField(ProjectManager.getInstance().defaultProject,
                                                     emptyList(), UNKNOWN_VERSION)
      myOptionsTextField.setVariants(listOf(Option("test")))

      val component = LabeledComponent.create(
        myOptionsTextField, Angular2Bundle.message("angular.action.new-project.label-additional-parameters"))
      component.setAnchor(panel.getComponent(0) as JComponent)
      component.labelLocation = BorderLayout.WEST
      panel.add(component)

      myUseStandalone = JCheckBox(Angular2Bundle.message("angular.action.new-project.label-standalone"), true)
      panel.add(myUseStandalone)

      myUseDefaults = JCheckBox(Angular2Bundle.message("angular.action.new-project.label-defaults"), true)
      panel.add(myUseDefaults)


      return panel
    }

    override fun addExtraFields(settingsStep: SettingsStep) {
      val field = settingsStep.moduleNameLocationSettings
      if (field != null) {
        myContentRoot = object : TextAccessor {
          override fun setText(text: String) {
            field.moduleContentRoot = text
          }

          override fun getText(): String {
            return field.moduleContentRoot
          }
        }
      }
      settingsStep.addSettingsField(UIUtil.replaceMnemonicAmpersand(
        Angular2Bundle.message("angular.action.new-project.label-additional-parameters")), myOptionsTextField)
      settingsStep.addSettingsComponent(myUseStandalone)
      settingsStep.addSettingsComponent(myUseDefaults)
      packageField.addSelectionListener(Consumer { this.nodePackageChanged(it) })
      nodePackageChanged(packageField.selected)
    }

    override fun getSettings(): Settings {
      return AngularCLIProjectSettings(super.getSettings(), myUseDefaults.isSelected, myUseStandalone.isSelected, myOptionsTextField.text)
    }

    override fun validate(): ValidationInfo? {
      val info = super.validate()
      if (info != null) {
        return info
      }
      if (myContentRoot != null) {
        val message = validateFolderName(myContentRoot!!.text,
                                         Angular2Bundle.message("angular.action.new-project.label-content-root-folder"))
        if (message != null) {
          return ValidationInfo(message)
        }
      }
      return null
    }

    private fun nodePackageChanged(nodePackage: NodePackage) {
      ApplicationManager.getApplication().executeOnPooledThread {
        val options = Ref.create(emptyList<Option>())
        val cliVersion = Ref.create(UNKNOWN_VERSION)
        if (nodePackage.systemIndependentPath.endsWith("/node_modules/@angular/cli")) {
          var localFile = StandardFileSystems.local().findFileByPath(
            nodePackage.systemDependentPath)
          if (localFile != null) {
            localFile = localFile.parent.parent.parent
            try {
              val availableOptions =
                AngularCliSchematicsRegistryService.instance
                  .getSchematics(ProjectManager.getInstance().defaultProject, localFile!!, true, false)
                  .asSequence()
                  .filter { s -> "ng-new" == s.name }
                  .firstOrNull()
                  ?.let { schematic ->
                    val list = ArrayList(schematic.options)
                    list.add(createOption("verbose", "Boolean", false, "Adds more details to output logging."))
                    list.add(createOption("collection", "String", null, "Schematics collection to use"))
                    list.sortBy { it.name }
                    list
                  }
                ?: emptyList()

              options.set(availableOptions)
            }
            catch (e: Exception) {
              thisLogger().error("Failed to load schematics", e)
            }

            val packageVersion = nodePackage.version
            if (packageVersion != null) {
              cliVersion.set(packageVersion)
            }
          }
        }
        ReadAction.run<RuntimeException> { myOptionsTextField.setVariants(options.get(), cliVersion.get()) }
      }
    }

    private fun createOption(name: String, type: String, defaultVal: Any?, description: String): Option {
      val res = Option(name)
      res.type = type
      res.default = defaultVal
      res.description = description
      return res
    }
  }

  private class AngularCLIProjectSettings(
    settings: Settings,
    val useDefaults: Boolean,
    val useStandalone: Boolean,
    val options: String,
  )
    : Settings(settings.myInterpreterRef, settings.myPackage)

  companion object {
    const val NG_EXECUTABLE: String = "ng"
  }
}

private val NPX_PACKAGE_PATTERN = Pattern.compile("npx --package @angular/cli(?:@([0-9]+\\.[0-9]+\\.[0-9a-zA-Z-.]+))? ng")
private val VALID_NG_APP_NAME = Pattern.compile("[a-zA-Z][0-9a-zA-Z]*(-[a-zA-Z][0-9a-zA-Z]*)*")
private val UNKNOWN_VERSION = SemVer("0.0.0", 0, 0, 0)

private fun isPackageGreaterOrEqual(pkg: NodePackage, major: Int, minor: Int, patch: Int): Boolean {
  var ver: SemVer? = null
  if (pkg.name == ANGULAR_CLI_PACKAGE) {
    ver = pkg.version
  }
  else {
    val m = NPX_PACKAGE_PATTERN.matcher(pkg.systemIndependentPath)
    if (m.matches()) {
      ver = SemVer.parseFromText(m.group(1))
    }
  }
  return ver == null || ver.isGreaterOrEqualThan(major, minor, patch)
}

fun ng(path: String): String {
  return path + File.separator + "bin" + File.separator + AngularCliProjectGenerator.NG_EXECUTABLE
}

@DialogMessage
private fun validateFolderName(path: String, label: String): String? {
  val fileName = PathUtil.getFileName(path)
  return if (!VALID_NG_APP_NAME.matcher(fileName).matches()) {
    XmlStringUtil.wrapInHtml(
      Angular2Bundle.message("angular.action.new-project.wrong-folder-name", label, fileName)
    )
  }
  else null
}