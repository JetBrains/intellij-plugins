package org.jetbrains.qodana.ui.run.wizard

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.ui.util.preferredWidth
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBFont
import icons.QodanaIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.run.QodanaRunInIdeService
import org.jetbrains.qodana.run.QodanaRunState
import org.jetbrains.qodana.coroutines.isInDumbModeFlow
import org.jetbrains.qodana.ui.wizard.QodanaWizardStep
import org.jetbrains.qodana.ui.wizard.QodanaWizardStepViewModel
import org.jetbrains.qodana.ui.wizard.QodanaWizardStepViewProvider
import org.jetbrains.qodana.ui.wizard.QodanaWizardTransition
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class WelcomeRunQodanaStep(override val viewModel: WelcomeRunQodanaStepViewModel) : QodanaWizardStep {
  companion object {
    const val ID = "WelcomeRunQodana"
  }

  override val id: String get() = ID

  override val viewProvider: QodanaWizardStepViewProvider = WelcomeRunQodanaStepViewProvider(viewModel)
}

class WelcomeRunQodanaStepViewModel(project: Project) : QodanaWizardStepViewModel {
  private val _transitionToNextFlow = MutableSharedFlow<String>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  override val stepTransitionFlow: Flow<Pair<QodanaWizardTransition, String?>> = _transitionToNextFlow.map { QodanaWizardTransition.NEXT to it }

  val dumbModeFlow: Flow<Boolean> = isInDumbModeFlow(project)

  val isRunningFlow: Flow<Boolean> = QodanaRunInIdeService.getInstance(project).runState.map { it is QodanaRunState.Running }

  fun goToSetupCI() {
    _transitionToNextFlow.tryEmit(EditYamlBeforeSetupCIStep.ID)
  }

  fun goToLocalRun() {
    _transitionToNextFlow.tryEmit(EditYamlAndRunQodanaStep.ID)
  }
}

private class WelcomeRunQodanaStepViewProvider(val viewModel: WelcomeRunQodanaStepViewModel) : QodanaWizardStepViewProvider {
  override val titleFlow: Flow<String> = flowOf("Run Qodana")

  override val mainViewFlow: Flow<JComponent>
    get() {
      return flow {
        coroutineScope {
          emit(RunQodanaView(this, viewModel).getView())
          awaitCancellation()
        }
      }
    }

  override val nextButtonDescriptorFlow: Flow<QodanaWizardStepViewProvider.ButtonDescriptor?> = flowOf(null)

  override val previousButtonDescriptorFlow: Flow<QodanaWizardStepViewProvider.ButtonDescriptor?> = flowOf(null)
}

private class RunQodanaView(viewScope: CoroutineScope, viewModel: WelcomeRunQodanaStepViewModel) {
  val localRunScenario = RunQodanaScenario(
    viewScope,
    QodanaBundle.message("qodana.run.wizard.step.initial.local.scenario.title"),
    QodanaIcons.Icons.LocalRun_128,
    listOf(
      QodanaBundle.message("qodana.run.wizard.step.initial.local.scenario.line1"),
      QodanaBundle.message("qodana.run.wizard.step.initial.local.scenario.line2")
    ),
    QodanaBundle.message("qodana.run.wizard.step.initial.local.scenario.button"),
    loadingButtonTextFlow = merge(
      viewModel.dumbModeFlow.map { if (it) "Indexing" else null },
      viewModel.isRunningFlow.map { if (it) "Running" else null }
    )
  ) {
    viewModel.goToLocalRun()
  }

  val setupCIScenario = RunQodanaScenario(
    viewScope,
    QodanaBundle.message("qodana.run.wizard.step.initial.ci.scenario.title"),
    QodanaIcons.Icons.CloudRun_128,
    listOf(
      QodanaBundle.message("qodana.run.wizard.step.initial.ci.scenario.line1"),
      QodanaBundle.message("qodana.run.wizard.step.initial.ci.scenario.line2")
    ),
    QodanaBundle.message("qodana.run.wizard.step.initial.ci.scenario.button"),
    loadingButtonTextFlow = flowOf(null)
  ) {
    viewModel.goToSetupCI()
  }

  fun getView(): JPanel {
    return JPanel(GridBagLayout()).apply {
      val rightPanel = localRunScenario.buildPanel(otherScenario = setupCIScenario)
      val leftPanel = setupCIScenario.buildPanel(otherScenario = localRunScenario)

      val leftPanelWidth = leftPanel.preferredWidth.toDouble()
      val rightPanelWidth = rightPanel.preferredWidth.toDouble()

      var gbc = GridBag()
      gbc = gbc.nextLine().next()
        .weightx(rightPanelWidth).anchor(GridBagConstraints.CENTER)
      add(leftPanel, gbc)

      gbc = gbc.next()
        .weightx(0.0).weighty(1.0).fillCellVertically().coverColumn()
      add(JSeparator(JSeparator.VERTICAL), gbc)

      gbc = gbc.next()
        .weightx(leftPanelWidth).anchor(GridBagConstraints.CENTER)
      add(rightPanel, gbc)
    }
  }
}

private class RunQodanaScenario(
  val viewScope: CoroutineScope,
  @NlsContexts.Label val title: String,
  val icon: Icon,
  val rowsTexts: List<@NlsContexts.Label String>,
  @NlsContexts.Button val buttonText: String,
  val loadingButtonTextFlow: Flow<@NlsContexts.Label String?>,
  val buttonAction: () -> Unit
) {
  fun buildPanel(otherScenario: RunQodanaScenario): DialogPanel {
    val emptyRowsForAlignmentCount = maxOf(0, otherScenario.rowsTexts.size - rowsTexts.size)
    val allRowsTexts = rowsTexts + List(emptyRowsForAlignmentCount) { "" }

    var builtPanel: DialogPanel? = null
    builtPanel = panel {
      row {
        label(title).align(AlignX.CENTER).applyToComponent {
          font = JBFont.h3()
        }
      }.bottomGap(BottomGap.MEDIUM)

      row {
        icon(icon).align(AlignX.CENTER)
      }.topGap(TopGap.MEDIUM)

      val textRows = allRowsTexts.map {
        row {
          text(it).align(AlignX.CENTER).customize(UnscaledGaps())
        }
      }
      textRows.lastOrNull()?.bottomGap(BottomGap.SMALL)

      lateinit var button: JButton
      row {
        button = button(buttonText) {
          buttonAction.invoke()
        }.align(AlignX.CENTER).component
      }

      row {
        val loadingLabel = JLabel("")
        val loadingIcon = AsyncProcessIcon("").apply {
          isVisible = false
        }
        val loadingPanel = JPanel().apply {
          add(loadingLabel)
          add(loadingIcon)
        }
        cell(loadingPanel).align(AlignX.CENTER)

        viewScope.launch(QodanaDispatchers.Ui) {
          loadingButtonTextFlow.collect { loadingText ->
            if (loadingText == null) {
              loadingLabel.text = ""
              loadingIcon.isVisible = false
              button.isEnabled = true
              builtPanel?.revalidate()
              builtPanel?.repaint()
            }
            else {
              loadingLabel.text = loadingText
              loadingIcon.isVisible = true
              button.isEnabled = false
              builtPanel?.revalidate()
              builtPanel?.repaint()
            }
          }
        }
      }
    }
    return builtPanel
  }
}