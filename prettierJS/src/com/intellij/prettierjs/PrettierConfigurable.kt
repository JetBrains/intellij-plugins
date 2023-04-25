// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.ide.actionsOnSave.*
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.whenItemSelected
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil
import com.intellij.prettierjs.PrettierConfiguration.ConfigurationMode
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.MutableProperty
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.Gaps
import com.intellij.ui.layout.not
import com.intellij.ui.layout.selected
import com.intellij.util.text.SemVer
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.nio.file.FileSystems
import java.util.regex.PatternSyntaxException
import javax.swing.JCheckBox
import javax.swing.JRadioButton
import javax.swing.text.JTextComponent

private const val CONFIGURABLE_ID = "settings.javascript.prettier"

class PrettierConfigurable(private val project: Project) : BoundSearchableConfigurable(
  PrettierBundle.message("configurable.PrettierConfigurable.display.name"), "reference.settings.prettier", CONFIGURABLE_ID) {

  private lateinit var packageField: NodePackageField
  private lateinit var runForFilesField: JBTextField
  private lateinit var runOnSaveCheckBox: JCheckBox

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
          disabledConfiguration =
            radioButton(JavaScriptBundle.message("settings.javascript.linters.autodetect.disabled", displayName))
              .bindSelected(ConfigurationModeProperty(prettierState, defaultMode, ConfigurationMode.DISABLED))
              .component
        }
        row {
          automaticConfiguration =
            radioButton(JavaScriptBundle.message("settings.javascript.linters.autodetect.configure.automatically", displayName))
              .bindSelected(ConfigurationModeProperty(prettierState, defaultMode, ConfigurationMode.AUTOMATIC))
              .component

          val detectAutomaticallyHelpText = JavaScriptBundle.message(
            "settings.javascript.linters.autodetect.configure.automatically.help.text",
            ApplicationNamesInfo.getInstance().fullProductName,
            displayName,
            ".prettierrc.*"
          ) + " " + PrettierBundle.message("run.on.save.auto-mode.tooltip")

          val helpLabel = ContextHelpLabel.create(detectAutomaticallyHelpText)
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
          customize(Gaps(left = 21))
          row(PrettierBundle.message("prettier.package.label")) {
            packageField = NodePackageField(project, PrettierUtil.PACKAGE_NAME) {
              NodeJsInterpreterManager.getInstance(project).interpreter
            }
            cell(packageField)
              .align(AlignX.FILL)
              .bind({ it.selectedRef }, { nodePackageField, nodePackageRef -> nodePackageField.selectedRef = nodePackageRef },
                    MutableProperty({ prettierConfiguration.nodePackageRef }, { prettierConfiguration.withLinterPackage(it) })
              )
          }
          row {
            checkBox(PrettierBundle.message("run.on.code.reformat.label"))
              .bindSelected({ prettierState.runOnReformat }, { prettierState.runOnReformat = it })

            val shortcut = ActionManager.getInstance().getKeyboardShortcut(IdeActions.ACTION_EDITOR_REFORMAT)
            shortcut?.let { comment(KeymapUtil.getShortcutText(it)) }
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
          .bindSelected(RunOnObservableProperty(
            { prettierState.configurationMode != ConfigurationMode.DISABLED && prettierState.runOnSave },
            { prettierState.runOnSave = it },
            { !disabledConfiguration.isSelected && runOnSaveCheckBox.isSelected }
          ))
          .component

        val link = ActionsOnSaveConfigurable.createGoToActionsOnSavePageLink()
        cell(link)
      }.enabledIf(!disabledConfiguration.selected)
    }
  }

  private inner class RunOnObservableProperty(
    private val getter: () -> Boolean,
    private val setter: (Boolean) -> Unit,
    private val afterConfigModeChangeGetter: () -> Boolean,
  ) : ObservableMutableProperty<Boolean> {
    override fun set(value: Boolean) {
      setter(value)
    }

    override fun get(): Boolean =
      getter()

    override fun afterChange(parentDisposable: Disposable?, listener: (Boolean) -> Unit) {

      fun emitChange(radio: JBRadioButton) {
        if (radio.isSelected) {
          listener(afterConfigModeChangeGetter())
        }
      }

      manualConfiguration.whenItemSelected(parentDisposable, ::emitChange)
      automaticConfiguration.whenItemSelected(parentDisposable, ::emitChange)
      disabledConfiguration.whenItemSelected(parentDisposable, ::emitChange)
    }

  }

  private class ConfigurationModeProperty(private val prettierConfiguration: PrettierConfiguration.State,
                                          private val defaultMode: ConfigurationMode,
                                          private val mode: ConfigurationMode) : MutableProperty<Boolean> {
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

    override fun getCommentAccordingToStoredState() =
      PrettierConfiguration.getInstance(project).let { getComment(it.getPackage(null).getVersion(project), it.filesPattern) }

    override fun getCommentAccordingToUiState(configurable: PrettierConfigurable) =
      getComment(configurable.packageField.selectedRef.constantPackage?.getVersion(project),
                 configurable.runForFilesField.text.trim())

    private fun getComment(prettierVersion: @Nullable SemVer?, filesPattern: @NotNull String): ActionOnSaveComment? {
      if (prettierVersion == null) {
        val message = PrettierBundle.message("run.on.save.prettier.package.not.specified.warning")
        // no need to show warning if Prettier is not enabled in this project
        return if (isActionOnSaveEnabled) ActionOnSaveComment.warning(message) else ActionOnSaveComment.info(message)
      }

      return ActionOnSaveComment.info(PrettierBundle.message("run.on.save.prettier.version.and.files.pattern",
                                                             shorten(prettierVersion.rawVersion, 15),
                                                             shorten(filesPattern, 40)))
    }

    override fun isActionOnSaveEnabledAccordingToStoredState() = PrettierConfiguration.getInstance(project).isRunOnSave

    override fun isActionOnSaveEnabledAccordingToUiState(configurable: PrettierConfigurable) = configurable.runOnSaveCheckBox.isSelected

    override fun setActionOnSaveEnabled(configurable: PrettierConfigurable, enabled: Boolean) {
      configurable.runOnSaveCheckBox.isSelected = enabled
    }

    override fun getActionLinks() = listOf(createGoToPageInSettingsLink(CONFIGURABLE_ID))

    private fun shorten(s: String, max: Int) = StringUtil.shortenTextWithEllipsis(s, max, 0, true)
  }
}
