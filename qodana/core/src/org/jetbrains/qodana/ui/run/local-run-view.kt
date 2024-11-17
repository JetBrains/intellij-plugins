package org.jetbrains.qodana.ui.run

import com.intellij.json.JsonFileType
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.filetype.SarifFileType
import org.jetbrains.qodana.ui.ci.providers.withBottomInsetBeforeComment
import javax.swing.event.DocumentEvent

fun localRunQodanaMainView(scope: CoroutineScope, viewModel: LocalRunQodanaViewModel, showCloudTokenField: Boolean = true): DialogPanel {
  return panel {
    row {
      cell(qodanaYamlView(scope, viewModel.qodanaYamlViewModel).withBottomInsetBeforeComment())
        .align(Align.FILL)
        .resizableColumn()
        .comment(QodanaBundle.message("qodana.run.wizard.step.add.qodana.yam.about"))
        .gap(RightGap.COLUMNS)
    }.resizableRow().bottomGap(BottomGap.SMALL)

    if (showCloudTokenField) {
      row {
        val publishToCloudCheckbox = checkBox(QodanaBundle.message("qodana.run.wizard.send.results.to.cloud"))
          .gap(RightGap.SMALL)

        publishToCloudCheckbox.actionListener { _, component ->
          viewModel.setPublishToCloud(component.isSelected)
        }

        val cloudTokenPasswordField = passwordField()
          .align(AlignX.FILL).resizableColumn()

        button(QodanaBundle.message("qodana.run.wizard.get.token")) {
          viewModel.openGetTokenPage()
        }.align(AlignX.RIGHT).gap(RightGap.COLUMNS)

        val cloudTokenPasswordComponent = cloudTokenPasswordField.component
        cloudTokenPasswordComponent.emptyText.text = "Project token"

        cloudTokenPasswordComponent.document.addDocumentListener(object : DocumentAdapter() {
          override fun textChanged(e: DocumentEvent) {
            viewModel.setCloudToken(cloudTokenPasswordComponent.passwordString)
          }
        })
        scope.launch(QodanaDispatchers.Ui) {
          launch {
            viewModel.cloudTokenStateFlow.collect { token ->
              if (cloudTokenPasswordComponent.passwordString != token) {
                cloudTokenPasswordComponent.text = token
              }
            }
          }
          launch {
            viewModel.publishToCloudStateFlow.collect { doPublish ->
              publishToCloudCheckbox.component.isSelected = doPublish
              cloudTokenPasswordComponent.isEnabled = doPublish
            }
          }
        }
      }
    }

    row {
      val checkBox = checkBox(QodanaBundle.message("qodana.run.wizard.step.add.qodana.yaml.save"))
      checkBox.actionListener { _, component ->
        viewModel.setSaveQodanaYaml(component.isSelected)
      }

      val checkBoxComponent = checkBox.component
      scope.launch(QodanaDispatchers.Ui) {
        viewModel.saveQodanaYamlStateFlow.collect { saveState ->
          when(saveState) {
            LocalRunQodanaViewModel.SaveQodanaYamlState.SAVE -> {
              checkBoxComponent.isVisible = true
              checkBoxComponent.isSelected = true
            }
            LocalRunQodanaViewModel.SaveQodanaYamlState.NO_SAVE -> {
              checkBoxComponent.isVisible = true
              checkBoxComponent.isSelected = false
            }
            LocalRunQodanaViewModel.SaveQodanaYamlState.ALREADY_PHYSICAL -> {
              checkBoxComponent.isVisible = false
            }
          }
          checkBoxComponent.revalidate()
          checkBoxComponent.repaint()
        }
      }
    }
    collapsibleGroup(QodanaBundle.message("local.run.advanced.configuration")) {
      row {
        val checkBox = checkBox(QodanaBundle.message("local.run.use.qodana.analysis.baseline"))
          .gap(RightGap.SMALL)
        checkBox.actionListener { _, component ->
          viewModel.setDoUseBaseline(component.isSelected)
        }

        val baselineFileTextField = TextFieldWithBrowseButton().apply {
          addBrowseFolderListener(viewModel.project, createBaselineFileChooser(viewModel.project).withTitle(QodanaBundle.message("local.run.baseline.file.location")))
          putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY)
        }
        baselineFileTextField.textField.document.addDocumentListener(object : DocumentAdapter() {
          override fun textChanged(e: DocumentEvent) {
            viewModel.setBaselineFile(baselineFileTextField.text)
          }
        })

        cell(baselineFileTextField)
          .resizableColumn()
          .align(AlignX.FILL)

        scope.launch(QodanaDispatchers.Ui) {
          launch {
            viewModel.doUseBaselineStateFlow.collect {
              checkBox.component.isSelected = it
              baselineFileTextField.isEnabled = it
            }
          }
          launch {
            viewModel.baselineFileStateFlow.collect {
              val currentText = baselineFileTextField.text
              if (currentText != it) {
                baselineFileTextField.text = it
              }
            }
          }
          launch {
            val validatorDisposable = Disposer.newDisposable()
            val baselineFileValidator = ComponentValidator(validatorDisposable).installOn(baselineFileTextField)
            try {
              viewModel.baselineFileErrorMessageFlow.collect { errorMessage ->
                baselineFileValidator.updateInfo(errorMessage?.let { ValidationInfo(it, baselineFileTextField) })
              }
            }
            finally {
              Disposer.dispose(validatorDisposable)
            }
          }
        }
      }.rowComment(QodanaBundle.message("local.run.baseline.comment"), maxLineLength = 80)
    }
  }
}

private val JBPasswordField.passwordString: String
  get() = this.password.joinToString("")

private fun createBaselineFileChooser(project: Project): FileChooserDescriptor =
  FileChooserDescriptorFactory.createSingleFileDescriptor()
    .withExtensionFilter(QodanaBundle.message("local.run.baseline.filter"), SarifFileType, JsonFileType.INSTANCE)
    .withRoots(listOfNotNull(project.guessProjectDir()))
