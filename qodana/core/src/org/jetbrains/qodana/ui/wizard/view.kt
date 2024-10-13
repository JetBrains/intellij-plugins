@file:OptIn(ExperimentalCoroutinesApi::class)

package org.jetbrains.qodana.ui.wizard

import com.intellij.ui.JBCardLayout
import com.intellij.ui.JBCardLayout.SwipeDirection
import com.intellij.ui.components.panels.Wrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.setContentAndRepaint
import javax.swing.JPanel

fun <T> qodanaWizardViewProviderItemFlow(
  viewModel: QodanaWizardViewModel,
  itemProvider: (QodanaWizardStepViewProvider) -> Flow<T>
): Flow<T> {
  return viewModel.currentStepWithSourceTransitionFlow
    .mapNotNull { it.second }
    .flatMapLatest { itemProvider.invoke(it.viewProvider) }
}

fun qodanaWizardMainView(viewScope: CoroutineScope, viewModel: QodanaWizardViewModel): JPanel {
  val stepIdToStepMainViewWrapper: Map<String, Wrapper> = viewModel.steps.associate { it.id to Wrapper() }

  val cardLayout = JBCardLayout()
  val mainPanel = JPanel(cardLayout)
  stepIdToStepMainViewWrapper.forEach { (stepId, viewWrapper) ->
    mainPanel.add(viewWrapper, stepId)
  }

  viewScope.launch(QodanaDispatchers.Ui) {
    viewModel.currentStepWithSourceTransitionFlow.collectLatest { (direction, step) ->
      if (step == null) return@collectLatest
      val currentStepMainViewWrapper = stepIdToStepMainViewWrapper[step.id]!!

      coroutineScope {
        launch(start = CoroutineStart.UNDISPATCHED) {
          step.viewProvider.mainViewFlow.collect {
            currentStepMainViewWrapper.setContentAndRepaint(it)
          }
        }
        val swipeDirection = when(direction) {
          QodanaWizardTransition.NEXT -> SwipeDirection.FORWARD
          QodanaWizardTransition.PREVIOUS -> SwipeDirection.BACKWARD
          null -> SwipeDirection.AUTO
        }
        cardLayout.swipe(mainPanel, step.id, swipeDirection)
      }
    }
  }

  return mainPanel
}