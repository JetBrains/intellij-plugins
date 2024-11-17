package org.jetbrains.qodana.ui.ci.providers.space

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBFont
import icons.QodanaIcons
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.ProjectVcsDataProvider
import org.jetbrains.qodana.ui.ci.SetupCIProvider
import org.jetbrains.qodana.ui.ci.SetupCIProviderFactory
import org.jetbrains.qodana.ui.ci.SetupCIViewModel
import org.jetbrains.qodana.ui.ci.providers.CIConfigFileState
import org.jetbrains.qodana.ui.ci.providers.bannerWithEditorComponent
import org.jetbrains.qodana.ui.ci.providers.withBottomInsetBeforeComment
import javax.swing.Icon
import javax.swing.JComponent
import kotlin.io.path.Path

internal const val SPACE_AUTOMATION_CI_FILE = ".space.kts"

private class SetupSpaceAutomationProviderFactory : SetupCIProviderFactory {
  override fun createSetupCIProvider(project: Project, dialogScope: CoroutineScope, projectVcsDataProvider: ProjectVcsDataProvider): SetupCIProvider? {
    val projectNioPath = project.guessProjectDir()?.toNioPath() ?: project.basePath?.let { Path(it) } ?: return null
    val viewModel = SetupSpaceAutomationViewModel(projectNioPath, project, dialogScope, projectVcsDataProvider)

    return object : SetupCIProvider.Available {
      override val viewModel: SetupCIViewModel = viewModel

      override val viewFlow: Flow<JComponent> = flow {
        coroutineScope {
          emit(setupSpaceAutomationView(this, viewModel))
          awaitCancellation()
        }
      }

      override val text: String get() = QodanaBundle.message("qodana.add.to.ci.space.automation")

      override val icon: Icon get() = QodanaIcons.Icons.CI.Space

      override val nextButtonText get() = QodanaBundle.message("qodana.run.wizard.finish.ci.button")
    }
  }
}

private fun setupSpaceAutomationView(scope: CoroutineScope, viewModel: SetupSpaceAutomationViewModel): DialogPanel {
  val mainPanel = bannerWithEditorComponent(
    scope,
    viewModel.baseSetupCIViewModel.bannerContentProviderFlow,
    viewModel.baseSetupCIViewModel.configEditorStateFlow.mapNotNull { it?.editor },
    viewModel.project,
  ).withBottomInsetBeforeComment()

  return panel {
    row {
      val labelComponent = label("").component
      labelComponent.font = JBFont.h2()

      scope.launch(QodanaDispatchers.Ui, CoroutineStart.UNDISPATCHED) {
        viewModel.baseSetupCIViewModel.configEditorStateFlow
          .mapNotNull { it?.ciConfigFileState }
          .collect { ciConfigFileState ->
            val text = if (ciConfigFileState is CIConfigFileState.InMemory) {
              QodanaBundle.message("qodana.add.to.ci.add.ci.file", SPACE_AUTOMATION_CI_FILE)
            }
            else {
              QodanaBundle.message("qodana.add.to.ci.edit.ci.file", SPACE_AUTOMATION_CI_FILE)
            }
            labelComponent.text = text
          }
      }
    }
    row {
      val descriptionLabel = text("")
      scope.launch(QodanaDispatchers.Ui, CoroutineStart.UNDISPATCHED) {
        viewModel.baseSetupCIViewModel.configEditorStateFlow
          .mapNotNull { it?.ciConfigFileState }
          .collect { ciConfigFileState ->
            val description = when (ciConfigFileState) {
              is CIConfigFileState.InMemory -> {
                QodanaBundle.message("qodana.add.to.ci.space.automation.description.new")
              }
              is CIConfigFileState.InMemoryPatchOfPhysicalFile -> {
                QodanaBundle.message("qodana.add.to.ci.space.automation.description.patch")
              }
              is CIConfigFileState.Physical -> {
                QodanaBundle.message("qodana.add.to.ci.space.automation.description.physical")
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
        .comment(QodanaBundle.message("qodana.add.to.ci.space.automation.about"))
    }.resizableRow().bottomGap(BottomGap.SMALL)
  }
}
