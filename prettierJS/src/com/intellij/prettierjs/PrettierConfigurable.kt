// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.ide.actionsOnSave.*
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.emptyText
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil.shortenTextWithEllipsis
import com.intellij.prettierjs.PrettierConfiguration.ConfigurationMode
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.MutableProperty
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.event.ItemEvent
import java.nio.file.FileSystems
import java.util.regex.PatternSyntaxException
import javax.swing.JCheckBox
import javax.swing.JRadioButton
import javax.swing.text.JTextComponent

const val CONFIGURABLE_ID: String = "settings.javascript.prettier"

class PrettierConfigurable(private val project: Project) : BoundSearchableConfigurable(
  PrettierBundle.message("configurable.PrettierConfigurable.display.name"), "reference.settings.prettier", CONFIGURABLE_ID) {

  private lateinit var packageField: NodePackageField
  private lateinit var runForFilesField: JBTextField
  private lateinit var runOnSaveCheckBox: JCheckBox
  private lateinit var codeStyleModifierCheckBox: JCheckBox
  private lateinit var customIgnorePathField: TextFieldWithBrowseButton

  private lateinit var disabledConfiguration: JRadioButton
  private lateinit var automaticConfiguration: JRadioButton
  private lateinit var manualConfiguration: JRadioButton

  override fun createPanel(): DialogPanel {
    val prettierConfiguration = PrettierConfiguration.getInstance(project)
    val prettierState = prettierConfiguration.state
    val defaultMode = prettierConfiguration.configurationMode

    return panel {
      buttonsGroup {
        row {
          disabledConfiguration = radioButton(JavaScriptBundle.message("settings.javascript.linters.autodetect.disabled", displayName))
            .bindSelected(ConfigurationModeProperty(prettierState, defaultMode, ConfigurationMode.DISABLED))
            .component
            .apply {
              addItemListener { e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                  runOnSaveCheckBox.isSelected = false
                  codeStyleModifierCheckBox.isSelected = false
                }
              }
            }
        }
        row {
          automaticConfiguration =
            radioButton(JavaScriptBundle.message("settings.javascript.linters.autodetect.configure.automatically", displayName))
              .bindSelected(ConfigurationModeProperty(prettierState, defaultMode, ConfigurationMode.AUTOMATIC))
              .component

          val autoConfigHelpText = JavaScriptBundle.message(
            "settings.javascript.linters.autodetect.configure.automatically.help.text",
            ApplicationNamesInfo.getInstance().fullProductName,
            displayName,
            ".prettierrc.*"
          )
          val runOnSaveTooltip = PrettierBundle.message("run.on.save.auto-mode.tooltip")
          val ignorePathHelpText = PrettierBundle.message(
            "prettier.ignore.path.autodetect.configure.automatically.help.text",
            ApplicationNamesInfo.getInstance().fullProductName,
            PrettierUtil.IGNORE_FILE_NAME
          )

          val helpLabel = ContextHelpLabel.create(
            PrettierBundle.message(
              "prettier.automatic.configuration.tooltip.help",
              autoConfigHelpText,
              runOnSaveTooltip,
              ignorePathHelpText
            )
          )
          helpLabel.border = JBUI.Borders.emptyLeft(UIUtil.DEFAULT_HGAP)
          cell(helpLabel)
        }
        row {
          manualConfiguration =
            radioButton(JavaScriptBundle.message("settings.javascript.linters.autodetect.configure.manually", displayName))
              .bindSelected(ConfigurationModeProperty(prettierState, defaultMode, ConfigurationMode.MANUAL))
              .component
        }
      }
      row {
        panel {
          customize(UnscaledGaps(left = 21))
          row(PrettierBundle.message("prettier.package.label")) {
            packageField = NodePackageField(project, PrettierUtil.PACKAGE_NAME) {
              NodeJsInterpreterManager.getInstance(project).interpreter
            }
            cell(packageField)
              .align(AlignX.FILL)
              .bind({ it.selectedRef }, { nodePackageField, nodePackageRef -> nodePackageField.selectedRef = nodePackageRef },
                    MutableProperty({ prettierConfiguration.packageRefForPackageFieldBindingInConfigurable },
                                    { prettierConfiguration.withLinterPackage(it) })
              )
          }
          row(PrettierBundle.message("prettier.ignore.path.field.label")) {
            customIgnorePathField = textFieldWithBrowseButton(project)
              .align(AlignX.FILL)
              .bind({ textField -> textField.text.trim() },
                    TextFieldWithBrowseButton::setText,
                    MutableProperty({ prettierState.customIgnorePath }, { prettierState.customIgnorePath = it }))
              .component
            customIgnorePathField.emptyText.setText(PrettierBundle.message("prettier.ignore.path.field.empty.text"))
          }.enabledIf(manualConfiguration.selected)
          row {
            checkBox(PrettierBundle.message("run.on.code.reformat.label"))
              .bindSelected({ prettierState.runOnReformat }, { prettierState.runOnReformat = it })

            val shortcut = ActionManager.getInstance().getKeyboardShortcut(IdeActions.ACTION_EDITOR_REFORMAT)
            shortcut?.let { comment(KeymapUtil.getShortcutText(it)) }
          }.enabledIf(manualConfiguration.selected)
          row {
            checkBox(PrettierBundle.message("prettier.format.files.outside.dependency.scope.label"))
              .bindSelected({ prettierState.formatFilesOutsideDependencyScope }, { prettierState.formatFilesOutsideDependencyScope = it })
            val helpLabel = ContextHelpLabel.create(PrettierBundle.message("prettier.format.files.outside.dependency.scope.help.text"))
            helpLabel.border = JBUI.Borders.emptyLeft(UIUtil.DEFAULT_HGAP)
            cell(helpLabel)

          }.enabledIf(manualConfiguration.selected)
          separator()
        }.visibleIf(manualConfiguration.selected)
      }

      row(PrettierBundle.message("run.for.files.label")) {
        runForFilesField = textField()
          .comment(PrettierBundle.message("files.pattern.comment"))
          .align(AlignX.FILL)
          .bind({ textField -> textField.text.trim() },
                JTextComponent::setText,
                MutableProperty({ prettierState.filesPattern }, { prettierState.filesPattern = it }))
          .validationOnInput {
            try {
              FileSystems.getDefault().getPathMatcher("glob:" + it.text)
              null
            }
            catch (e: PatternSyntaxException) {
              @NlsSafe val firstLine = e.localizedMessage?.lines()?.firstOrNull()
              ValidationInfo(firstLine ?: PrettierBundle.message("invalid.pattern"), it)
            }
          }
          .component
      }.enabledIf(!disabledConfiguration.selected)

      row {
        runOnSaveCheckBox = checkBox(PrettierBundle.message("run.on.save.label"))
          .bindSelected({ prettierState.configurationMode != ConfigurationMode.DISABLED && prettierState.runOnSave }, { prettierState.runOnSave = it })
          .component

        val link = ActionsOnSaveConfigurable.createGoToActionsOnSavePageLink()
        cell(link)
      }.enabledIf(!disabledConfiguration.selected)

      row {
        codeStyleModifierCheckBox = checkBox(PrettierBundle.message("prettier.checkbox.code.style.modification"))
          .bindSelected({ prettierState.configurationMode != ConfigurationMode.DISABLED && prettierState.codeStyleSettingsModifierEnabled }, { prettierState.codeStyleSettingsModifierEnabled = it })
          .component

        val helpLabel = ContextHelpLabel.create(
          PrettierBundle.message(
            "prettier.checkbox.code.style.modification.help.text",
            ApplicationNamesInfo.getInstance().fullProductName
          )
        )
        helpLabel.border = JBUI.Borders.emptyLeft(UIUtil.DEFAULT_HGAP)
        cell(helpLabel)
      }.enabledIf(!disabledConfiguration.selected)

      onApply {
        CodeStyleSettingsManager.getInstance(project).notifyCodeStyleSettingsChanged()
      }
    }
  }

  private class ConfigurationModeProperty(
    private val prettierConfiguration: PrettierConfiguration.State,
    private val defaultMode: ConfigurationMode,
    private val mode: ConfigurationMode,
  ) : MutableProperty<Boolean> {
    override fun get(): Boolean =
      prettierConfiguration.configurationMode.let {
        it == mode || (it == null && defaultMode == mode)
      }

    override fun set(value: Boolean) {
      if (value)
        prettierConfiguration.configurationMode = mode
    }

  }

  class PrettierOnSaveInfoProvider : ActionOnSaveInfoProvider() {
    override fun getActionOnSaveInfos(context: ActionOnSaveContext):
      List<ActionOnSaveInfo> = listOf(PrettierOnSaveActionInfo(context))

    override fun getSearchableOptions(): Collection<String> {
      return listOf(PrettierBundle.message("run.on.save.checkbox.on.actions.on.save.page"))
    }
  }


  private class PrettierOnSaveActionInfo(actionOnSaveContext: ActionOnSaveContext)
    : ActionOnSaveBackedByOwnConfigurable<PrettierConfigurable>(actionOnSaveContext, CONFIGURABLE_ID, PrettierConfigurable::class.java) {

    override fun getActionOnSaveName() = PrettierBundle.message("run.on.save.checkbox.on.actions.on.save.page")

    override fun isApplicableAccordingToStoredState(): Boolean =
      PrettierConfiguration.getInstance(project).configurationMode != ConfigurationMode.DISABLED

    override fun isApplicableAccordingToUiState(configurable: PrettierConfigurable): Boolean =
      !configurable.disabledConfiguration.isSelected

    override fun getCommentAccordingToStoredState(): ActionOnSaveComment? =
      getComment(PrettierConfiguration.getInstance(this.project).filesPattern)

    override fun getCommentAccordingToUiState(configurable: PrettierConfigurable) =
      getComment(configurable.runForFilesField.text.trim())

    private fun getComment(filesPattern: String): ActionOnSaveComment? {
      if (!isSaveActionApplicable) return ActionOnSaveComment.info(PrettierBundle.message("run.on.save.comment.disabled"))

      val filesPatternShortened = shortenTextWithEllipsis(filesPattern, 60, 0, true)
      return ActionOnSaveComment.info(PrettierBundle.message("run.on.save.comment.files", filesPatternShortened))
    }

    override fun isActionOnSaveEnabledAccordingToStoredState() = PrettierConfiguration.getInstance(project).isRunOnSave

    override fun isActionOnSaveEnabledAccordingToUiState(configurable: PrettierConfigurable) = configurable.runOnSaveCheckBox.isSelected

    override fun setActionOnSaveEnabled(configurable: PrettierConfigurable, enabled: Boolean) {
      configurable.runOnSaveCheckBox.isSelected = enabled
    }

    override fun getActionLinks() = listOf(createGoToPageInSettingsLink(CONFIGURABLE_ID))
  }
}
