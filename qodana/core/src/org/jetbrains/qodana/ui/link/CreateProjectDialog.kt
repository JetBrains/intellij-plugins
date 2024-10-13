@file:JvmName("CreateProjectDialogKt")

package org.jetbrains.qodana.ui.link

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.observable.util.whenItemSelected
import com.intellij.openapi.observable.util.whenTextChangedFromUi
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsContexts
import com.intellij.platform.util.coroutines.childScope
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.AsyncProcessIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.project.CloudProjectData
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.ui.setContentAndRepaint
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants

class CreateProjectDialog(
  private val project: Project,
  organizationsFilter: (QDCloudSchema.Organization) -> Boolean,
  onProjectCreated: (CloudProjectData) -> Unit
) : DialogWrapper(project) {
  private val scope: CoroutineScope = project.qodanaProjectScope.childScope(ModalityState.any().asContextElement())

  private val viewModel = CreateProjectDialogViewModel(scope, organizationsFilter, onProjectCreated)

  private val viewOfCurrentState = Wrapper()

  private val creatingProjectProgressIcon = AsyncProcessIcon("Creating project").apply {
    isVisible = false
    alignmentX = JComponent.LEFT_ALIGNMENT
  }

  private val loadingTeamsProgressIcon = AsyncProcessIcon("Loading teams").apply {
    isVisible = false
  }

  private var errorWithoutComponent: @NlsContexts.DialogMessage String? = null

  private val teamComboBoxModel: CollectionComboBoxModel<QodanaCloudTeamResponseWrapper> = CollectionComboBoxModel()
  private val teamComboBox: ComboBox<QodanaCloudTeamResponseWrapper> = ComboBox(teamComboBoxModel).apply {
    renderer = SimpleListCellRenderer.create("") { teamResponse ->
      teamResponse.team.name ?: teamResponse.team.id
    }
    whenItemSelected { viewModel.setSelectedTeam(it) }
  }

  private val projectNameField: JBTextField = JBTextField().apply {
    text = viewModel.escapeProjectNameInvalidChars(project.name)
    viewModel.setProjectName(text)
    whenTextChangedFromUi { viewModel.setProjectName(it) }
  }

  init {
    title = QodanaBundle.message("qodana.create.cloud.project.dialog.title")
    setOKButtonText(QodanaBundle.message("qodana.create.cloud.project.dialog.ok.button"))
    init()
    initValidation()
    scope.launch(QodanaDispatchers.Ui) {
      launch {
        val validatorsDisposable = Disposer.newDisposable()
        val projectNameValidator = ComponentValidator(validatorsDisposable).installOn(projectNameField)
        val loadTeamsValidator = ComponentValidator(validatorsDisposable).installOn(teamComboBox)
        try {
          viewModel.errorFlow.collect { error ->
            when(error) {
              is CreateProjectDialogViewModel.CreateProjectError.ProjectNameError -> {
                projectNameValidator.updateInfo(error.message?.let { ValidationInfo(it, projectNameField) })
              }
              is CreateProjectDialogViewModel.CreateProjectError.ProjectCreateError -> {
                errorWithoutComponent = error.message
              }
              is CreateProjectDialogViewModel.CreateProjectError.LoadTeamsError -> {
                loadTeamsValidator.updateInfo(error.message?.let { ValidationInfo(it, teamComboBox) })
              }
              null -> {}
            }
          }
        }
        finally {
          Disposer.dispose(validatorsDisposable)
        }
      }
      launch {
        viewModel.elementStatusFlow.collect { elementStatus ->
          when (elementStatus) {
            is CreateProjectDialogViewModel.ElementStatus.OkButtonStatus ->
              okAction.isEnabled = elementStatus.isEnabled
            is CreateProjectDialogViewModel.ElementStatus.CreatingProjectIconStatus ->
              creatingProjectProgressIcon.isVisible = elementStatus.isVisible
            is CreateProjectDialogViewModel.ElementStatus.LoadingTeamsIconStatus ->
              loadingTeamsProgressIcon.isVisible = elementStatus.isVisible
          }
        }
      }
      launch {
        viewModel.finishFlow.collect {
          close(OK_EXIT_CODE)
        }
      }
      launch {
        viewModel.teamsListFlow.collect { teams ->
          teamComboBox.selectedItem = null
          teamComboBoxModel.removeAll()
          teamComboBoxModel.add(teams)
          if (!teamComboBoxModel.isEmpty)
            teamComboBox.selectedIndex = 0
        }
      }
      launch {
        viewModel.dialogDataFlow.collect {
          val view = when (it) {
            is CreateProjectDialogViewModel.DialogData -> buildCreateProjectDialog(it)
            else -> panel {}
          }
          viewOfCurrentState.setContentAndRepaint(view)
        }
      }
      launch {
        viewModel.projectNameFlow.collect { projectName ->
          if (projectName == projectNameField.text) return@collect
          projectNameField.text = projectName
        }
      }
      launch {
        viewModel.selectedItemFlow.collect { selectedItem ->
          if (selectedItem == teamComboBox.selectedItem) return@collect
          teamComboBox.selectedItem = selectedItem
        }
      }
    }
  }

  override fun createSouthPanel(): JComponent {
    val buttons = super.createSouthPanel()
    return JPanel(HorizontalLayout(8, SwingConstants.BOTTOM)).apply {
      creatingProjectProgressIcon.border = buttons.border
      add(creatingProjectProgressIcon, HorizontalLayout.RIGHT)
      add(buttons, HorizontalLayout.RIGHT)
    }
  }

  override fun createCenterPanel(): JComponent {
    return viewOfCurrentState
  }

  override fun doValidate(): ValidationInfo? {
    val info = errorWithoutComponent
    if (info == null) return null
    return ValidationInfo(info).apply { okEnabled = true }
  }

  override fun doOKAction() {
    if (!okAction.isEnabled) return
    applyFields()
    viewModel.createProject()
  }

  private fun buildCreateProjectDialog(dialogData: CreateProjectDialogViewModel.DialogData): JComponent {
    return panel {
      row(QodanaBundle.message("qodana.create.cloud.project.dialog.create.in.team")) {
        cell(teamComboBox)
          .resizableColumn()
          .align(AlignX.FILL)
          .gap(RightGap.SMALL)

        cell(FixedSizeButton())
          .applyToComponent {
            text = null
            icon = AllIcons.Actions.Refresh
            addActionListener {
              viewModel.refreshTeams(dialogData)
            }
          }
          .gap(RightGap.SMALL)

        cell(loadingTeamsProgressIcon)
          .gap(RightGap.SMALL)
      }

      row(QodanaBundle.message("qodana.create.cloud.project.dialog.project.name")) {
        cell(projectNameField)
          .align(AlignX.FILL)
      }
    }
  }
}