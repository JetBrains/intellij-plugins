package org.jetbrains.qodana.ui.ci.providers.github

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.util.addDocumentListener
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBFont
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.ci.providers.CIConfigFileState
import org.jetbrains.qodana.ui.ci.providers.bannerWithEditorComponent
import org.jetbrains.qodana.ui.ci.providers.withBottomInsetBeforeComment
import org.jetbrains.yaml.YAMLFileType
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

class SetupGitHubActionsView(private val viewScope: CoroutineScope, val viewModel: SetupGitHubActionsViewModel) {
  private val configEditorDescriptionLabel = JBLabel().apply {
    font = JBFont.h2()
  }

  private val configFileTextField = TextFieldWithBrowseButton().apply {
    addBrowseFolderListener(viewModel.project, createConfigFileChooserDescriptor().withTitle(QodanaBundle.message("qodana.add.to.ci.github.actions.workflow.file.location")))
    putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY)
  }

  init {
    viewScope.launch(QodanaDispatchers.Ui) {
      launch {
        val textFieldListenerDisposable = Disposer.newDisposable()
        configFileTextField.textField.document.addDocumentListener(textFieldListenerDisposable, object : DocumentAdapter() {
          override fun textChanged(e: DocumentEvent) {
            viewModel.setConfigStringPath(configFileTextField.text)
          }
        })
        try {
          awaitCancellation()
        }
        finally {
          Disposer.dispose(textFieldListenerDisposable)
        }
      }
      launch {
        viewModel.configEditorStateFlow.collect { editorState ->
          when(editorState?.ciConfigFileState) {
            null, is CIConfigFileState.InMemory -> {
              configEditorDescriptionLabel.text = QodanaBundle.message("qodana.add.to.ci.github.actions.add.workflow.file")
            }
            else -> {
              configEditorDescriptionLabel.text = QodanaBundle.message("qodana.add.to.ci.github.actions.edit.workflow.file")
            }
          }
        }
      }
      launch {
        viewModel.configStringPathStateFlow.collect { pathString ->
          if (configFileTextField.text == pathString) return@collect
          configFileTextField.text = pathString
        }
      }
      launch {
        val validatorDisposable = Disposer.newDisposable()
        val stringPathValidator = ComponentValidator(validatorDisposable).installOn(configFileTextField)
        try {
          viewModel.configStringPathErrorMessageFlow.collect { errorMessage ->
            stringPathValidator.updateInfo(errorMessage?.let { ValidationInfo(it, configFileTextField) })
          }
        }
        finally {
          Disposer.dispose(validatorDisposable)
        }
      }
    }
  }

  private fun createConfigFileChooserDescriptor(): FileChooserDescriptor {
    val projectDir = viewModel.project.guessProjectDir()
    val dotGithubDir = projectDir?.findChild(".github")
    val workflowsDir = dotGithubDir?.findChild("workflows")

    val chooserRoot = workflowsDir ?: dotGithubDir ?: projectDir

    var chooser = FileChooserDescriptorFactory.createSingleFileDescriptor(YAMLFileType.YML)
      .withFileFilter { viewModel.isConfigPath(it.toNioPath()) }
    if (chooserRoot != null) {
      chooser = chooser.withRoots(chooserRoot)
    }
    return chooser
  }

  fun getView(): JComponent {
    val mainPanel = bannerWithEditorComponent(
      viewScope,
      viewModel.bannerContentProviderFlow,
      viewModel.configEditorStateFlow.mapNotNull { it?.editor },
      viewModel.project
    ).withBottomInsetBeforeComment()

    return panel {
      row {
        cell(configEditorDescriptionLabel)
      }
      row {
        val descriptionLabel = text("")
        viewScope.launch(QodanaDispatchers.Ui, CoroutineStart.UNDISPATCHED) {
          viewModel.configEditorStateFlow
            .mapNotNull { it?.ciConfigFileState }
            .collect { ciConfigFileState ->
            val description = when(ciConfigFileState) {
              is CIConfigFileState.InMemory -> {
                QodanaBundle.message("qodana.add.to.ci.github.actions.workflow.file.description.new")
              }
              is CIConfigFileState.InMemoryPatchOfPhysicalFile -> {
                QodanaBundle.message("qodana.add.to.ci.github.actions.workflow.file.description.patch")
              }
              is CIConfigFileState.Physical -> {
                QodanaBundle.message("qodana.add.to.ci.github.actions.workflow.file.description.physical")
              }
            }
            descriptionLabel.applyToComponent {
              text = description
            }
          }
        }
      }.bottomGap(BottomGap.SMALL)
      row {
        cell(mainPanel)
          .resizableColumn()
          .align(Align.FILL)
          .comment(QodanaBundle.message("qodana.add.to.ci.github.actions.about"))
      }.resizableRow()
      row(QodanaBundle.message("qodana.add.to.ci.github.actions.workflow.location")) {
        cell(configFileTextField)
          .resizableColumn()
          .align(AlignX.FILL)
      }.bottomGap(BottomGap.SMALL)
    }
  }
}
