package org.jetbrains.qodana.ui.wizard

import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector

class QodanaWizardViewModel(
  private val wizardStatsId: String,
  private val project: Project,
  val scope: CoroutineScope,
  val steps: List<QodanaWizardStep>,
  private val firstStep: QodanaWizardStep
) {
  val currentStepWithSourceTransitionFlow: StateFlow<Pair<QodanaWizardTransition?, QodanaWizardStep?>> =
    createCurrentStepWithSourceTransitionFlow()

  val finishFlow: Flow<Unit> = currentStepWithSourceTransitionFlow.map { it.second }.filter { it == null }.map {  }

  private fun createCurrentStepWithSourceTransitionFlow(): StateFlow<Pair<QodanaWizardTransition?, QodanaWizardStep?>> {
    val currentStepWithSourceTransitionFlow = MutableStateFlow<Pair<QodanaWizardTransition?, QodanaWizardStep?>>(null to firstStep)
    val stepIdToStep: Map<String, QodanaWizardStep> = steps.associateBy { it.id }

    logStepTransitionStats(
      oldStepId = null,
      newStepId = firstStep.id,
      oldStepDuration = 0L,
      org.jetbrains.qodana.stats.QodanaWizardTransition.OPEN
    )

    scope.launch(QodanaDispatchers.Default) {
      var timeCurrentStepWasOpened = System.currentTimeMillis()
      try {
        currentStepWithSourceTransitionFlow.mapNotNull { it.second }.collectLatest { currentStep ->
          currentStep.viewModel.stepTransitionFlow.collect { (direction, newStepId) ->
            val newWizardPartViewModel = newStepId?.let { stepIdToStep[newStepId]!! }

            val timeNewStepWasOpened = System.currentTimeMillis()
            logStepTransitionStats(
              oldStepId = currentStep.id,
              newStepId = newStepId,
              oldStepDuration = timeNewStepWasOpened - timeCurrentStepWasOpened,
              transition = direction.toStatsTransition()
            )
            timeCurrentStepWasOpened = timeNewStepWasOpened

            currentStepWithSourceTransitionFlow.value = direction to newWizardPartViewModel
          }
        }
        awaitCancellation()
      }
      finally {
        val currentStepId = currentStepWithSourceTransitionFlow.value.second?.id
        if (currentStepId != null) {
          logStepTransitionStats(
            oldStepId = currentStepId,
            newStepId = null,
            oldStepDuration = System.currentTimeMillis() - timeCurrentStepWasOpened,
            org.jetbrains.qodana.stats.QodanaWizardTransition.CLOSE
          )
        }
      }
    }
    return currentStepWithSourceTransitionFlow.asStateFlow()
  }

  private fun logStepTransitionStats(
    oldStepId: String?,
    newStepId: String?,
    oldStepDuration: Long,
    transition: org.jetbrains.qodana.stats.QodanaWizardTransition
  ) {
    QodanaPluginStatsCounterCollector.QODANA_WIZARD_DIALOG_TRANSITION.log(
      project,
      QodanaPluginStatsCounterCollector.QODANA_WIZARD_ID.with(wizardStatsId),
      QodanaPluginStatsCounterCollector.QODANA_WIZARD_CURRENT_STEP_ID.with(oldStepId),
      QodanaPluginStatsCounterCollector.QODANA_WIZARD_NEXT_STEP_ID.with(newStepId),
      QodanaPluginStatsCounterCollector.QODANA_WIZARD_TRANSITION_TYPE.with(transition),
      EventFields.DurationMs.with(oldStepDuration),
    )
  }

  private fun QodanaWizardTransition.toStatsTransition(): org.jetbrains.qodana.stats.QodanaWizardTransition {
    return when(this) {
      QodanaWizardTransition.NEXT -> org.jetbrains.qodana.stats.QodanaWizardTransition.NEXT
      QodanaWizardTransition.PREVIOUS -> org.jetbrains.qodana.stats.QodanaWizardTransition.PREVIOUS
    }
  }
}
