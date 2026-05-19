package com.intellij.lang.javascript.linter.eslint

import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.linter.eslint.EslintBundle
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.linter.AutodetectLinterPackage
import com.intellij.lang.javascript.linter.JSLinterUtil
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSUtil
import com.intellij.lang.javascript.linter.ui.JSLinterConfigFileTexts
import com.intellij.lang.javascript.linter.ui.JSLinterConfigFileView
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComponentWithBrowseButton
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.OSAgnosticPathUtil
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.DslComponentProperty
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.NotNullProducer
import com.intellij.util.ui.SwingHelper
import com.intellij.util.ui.UIUtil
import javax.swing.JLabel

internal class EslintPanel(project: Project, private val fullModeDialog: Boolean) {
  companion object {
    @JvmStatic
    fun isStandardJs(nodePackage: NodePackageRef): Boolean {
      val constantPackage = nodePackage.constantPackage
      val packageName = constantPackage?.name
      return StandardJSUtil.PACKAGE_NAME == packageName
    }
  }

  private val configFileView = JSLinterConfigFileView(
    project,
    JSLinterConfigFileTexts(
      JavaScriptBundle.message("javascript.linter.configurable.config.autoSearch.title"),
      EslintBundle.message("eslint.configurable.config.autoSearch.description.bodyInnerHtml"),
      EslintBundle.message("eslint.configurable.config.select.config.text")
    ),
    null
  ).apply {
    setAdditionalConfigFilesProducer(NotNullProducer { EslintUtil.findAllFlatAndLegacyConfigFiles(project) })
  }

  private val eslintPackageField = AutodetectLinterPackage.createNodePackageField(
    project, listOf(EslintUtil.PACKAGE_NAME, StandardJSUtil.PACKAGE_NAME), configFileView
  ).apply {
    addPackageRefSelectionListener(::adjustUiForPackage)
  }

  private val workDirPatternsComponent = createWorkDirPatternsComponent(project)
  private val additionalRulesDirField = createAdditionalRulesDirField(project)
  private val extraOptions = RawCommandLineEditor()
  private lateinit var packageErrorLabel: JLabel
  private lateinit var configurationPanel: Panel

  @JvmField
  val panel = panel {
    row(EslintBundle.message("eslint.configurable.eslintPackage.label")) {
      cell(eslintPackageField)
        .align(AlignX.FILL)
    }
    row {
      packageErrorLabel = label("")
        .applyToComponent {
          font = UIUtil.getTitledBorderFont()
          isVisible = false
        }
        .component
    }
    row(EslintBundle.message("eslint.configurable.label.working.directories")) {
      cell(workDirPatternsComponent)
        .comment(EslintBundle.message("eslint.configurable.working.directories.comment"))
        .align(AlignX.FILL)
    }

    configurationPanel = panel {
      row {
        cell(configFileView.component)
          .align(AlignX.FILL)
          .applyToComponent { putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY) }
      }

      separator()

      row(EslintBundle.message("eslint.configurable.additionalRulesDir.label")) {
        cell(additionalRulesDirField)
          .align(AlignX.FILL)
      }
      row(EslintBundle.message("eslint.configurable.extraOptions.label")) {
        cell(extraOptions)
          .align(AlignX.FILL)
      }

      separator()
    }
  }

  fun handleEnableStatusChanged(enabled: Boolean) {
    val selectedRef = eslintPackageField.selectedRef
    adjustUiForPackage(selectedRef)
    if (selectedRef === AutodetectLinterPackage.INSTANCE) {
      configFileView.setEnabled(false)
    }
    configFileView.onEnabledStateChanged(enabled)
  }

  fun setState(state: EslintState) {
    eslintPackageField.selectedRef = state.nodePackageRef
    workDirPatternsComponent.childComponent.text = changeSlashesInFirstPathIfItIsWindowsAbsolutePath(state.workDirPatterns, '\\')
    configFileView.setCustomConfigFileUsed(state.isCustomConfigFileUsed)
    configFileView.setCustomConfigFilePath(state.customConfigFilePath)
    additionalRulesDirField.text = FileUtil.toSystemDependentName(state.additionalRulesDirPath)
    extraOptions.text = state.extraOptions

    if (fullModeDialog) {
      configFileView.setPreferredWidthToComponents()
      SwingHelper.setPreferredWidthToFitText(additionalRulesDirField)
    }

    adjustUiForPackage(state.nodePackageRef)
  }

  fun buildEslintState(): EslintState.Builder {
    val selected = eslintPackageField.selectedRef
    val builder = EslintState.Builder()
    builder.setEslintPackage(selected)
    val wdPatterns = changeSlashesInFirstPathIfItIsWindowsAbsolutePath(workDirPatternsComponent.childComponent.getText().trim(), '/')
    builder.setWorkDirPatterns(wdPatterns)
    builder.setCustomConfigFilePath(configFileView.customConfigFilePath)
    builder.setCustomConfigFileUsed(configFileView.isCustomConfigFileUsed)
    builder.setAdditionalRulesDirPath(FileUtil.toSystemIndependentName(additionalRulesDirField.getText().trim()))
    builder.setExtraOptions(extraOptions.text)
    return builder
  }

  private fun createWorkDirPatternsComponent(project: Project): ComponentWithBrowseButton<ExpandableTextField> {
    val workDirPatternsTextField = ExpandableTextField(
      /*parser =*/ { it.split(";") },
      /*joiner =*/ { it -> it.joinToString(";") }
    )
    workDirPatternsTextField.emptyText.text = EslintBundle.message("eslint.configurable.working.dir.field.empty.text")
    val result = ComponentWithBrowseButton(workDirPatternsTextField, null)
    result.addBrowseFolderListener(project, FileChooserDescriptorFactory.createMultipleFoldersDescriptor(), TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT)
    return result
  }

  private fun createAdditionalRulesDirField(project: Project): TextFieldWithBrowseButton {
    val result = TextFieldWithBrowseButton()
    val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle(EslintBundle.message("eslint.configurable.additionalRulesDir.browseDialogTitle"))
    SwingHelper.installFileCompletionAndBrowseDialog(project, result, descriptor)
    return result
  }

  private fun adjustUiForPackage(nodePackage: NodePackageRef) {
    val error = formatErrorMessage(nodePackage)
    packageErrorLabel.setText(JSLinterUtil.getRedErrorTextHtml(error))
    packageErrorLabel.isVisible = error.isNotBlank()
    configurationPanel.visible(!isStandardJs(nodePackage))
  }

  @InspectionMessage
  private fun formatErrorMessage(packageRef: NodePackageRef): String {
    val pkg = packageRef.constantPackage
    return when {
      pkg == null -> ""
      pkg.systemDependentPath.trim { it <= ' ' }.isEmpty() -> JavaScriptBundle.message("javascript.linter.error.empty.path")
      !pkg.isValid(null, null) -> EslintBundle.message("eslint.error.package.directory.expected")
      else -> ""
    }
  }

  private fun changeSlashesInFirstPathIfItIsWindowsAbsolutePath(input: String, slashInReturnedString: Char): String {
    // Input string contains one or more paths or glob patterns, semicolon-separated.
    // If the first path is a Windows absolute path, we want to convert slashes, to make sure it's replaced with $PROJECT_DIR$ when saved to
    // eslint.xml file, or to make sure it looks like a Windows path in UI.
    // In any other case we should keep slashes as is, not to break escaped symbols in glob patterns.
    if (!SystemInfo.isWindows) return input
    if (input.isEmpty()) return input
    val semicolonIndex = input.indexOf(';')
    val firstPath = if (semicolonIndex < 0) input else input.substring(0, semicolonIndex)
    val windowsAbsolutePath = OSAgnosticPathUtil.isAbsoluteDosPath(firstPath)
    if (windowsAbsolutePath) {
      val oldChar = if (slashInReturnedString == '\\') '/' else '\\'
      return firstPath.replace(oldChar, slashInReturnedString) + if (semicolonIndex > 0) input.substring(semicolonIndex) else ""
    }
    return input
  }
}
