package org.jetbrains.qodana.ui.wizard

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.util.ui.update.UiNotifyConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.help.QodanaWebHelpProvider
import java.awt.Dimension
import java.awt.Insets
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

// abstract to log statistics about exact dialogs
abstract class QodanaWizardDialog(
  project: Project,
  private val scope: CoroutineScope,
  private val viewModel: QodanaWizardViewModel,
  centerPanelSize: Dimension,
  isModal: Boolean
) : DialogWrapper(project) {
  private val centerPanel: JPanel = qodanaWizardMainView(scope, viewModel).apply {
    preferredSize = centerPanelSize
    minimumSize = centerPanelSize
  }

  init {
    this.isModal = isModal
    init()
    buttonMap[okAction]!!.isVisible = false
    buttonMap[cancelAction]!!.isVisible = false
    IdeFocusManager.getGlobalInstance().requestFocus(centerPanel, false)

    scope.launch(QodanaDispatchers.Ui, start = CoroutineStart.UNDISPATCHED) {
      launch {
        viewModel.finishFlow.collect {
          applyFields()
          close(OK_EXIT_CODE)
        }
      }
      launch(start = CoroutineStart.UNDISPATCHED) {
        qodanaWizardViewProviderItemFlow(viewModel) { it.titleFlow }.collect {
          title = it
        }
      }
      launch(start = CoroutineStart.UNDISPATCHED) {
        var lastVisibleStatus: Boolean? = null
        qodanaWizardViewProviderItemFlow(viewModel) { it.nextButtonDescriptorFlow }.collect { buttonDescriptor ->
          buttonMap[okAction]!!.configureByButtonDescriptor(buttonDescriptor)
          val isVisible = (buttonDescriptor != null)
          if (lastVisibleStatus != isVisible) {
            lastVisibleStatus = isVisible
            updateFocus()
          }
        }
      }
      launch(start = CoroutineStart.UNDISPATCHED) {
        var lastVisible: Boolean? = null
        qodanaWizardViewProviderItemFlow(viewModel) { it.previousButtonDescriptorFlow }.collect { buttonDescriptor ->
          buttonMap[cancelAction]!!.configureByButtonDescriptor(buttonDescriptor)
          val isVisible = (buttonDescriptor != null)
          if (lastVisible != isVisible) {
            lastVisible = isVisible
            updateFocus()
          }
        }
      }
      launch {
        viewModel.currentStepWithSourceTransitionFlow.collect {
          updateFocus()
        }
      }
    }
  }

  private fun updateFocus() {
    val focusComponent = preferredFocusedComponent
    UiNotifyConnector.doWhenFirstShown(focusComponent) {
      val focusManager = IdeFocusManager.findInstanceByComponent(focusComponent)
      focusManager.requestFocus(focusComponent, false)
    }
  }

  override fun createHelpButton(insets: Insets): JButton {
    return super.createHelpButton(insets).apply {
      isFocusable = false
    }
  }

  override fun createCenterPanel(): JComponent = centerPanel

  override fun getPreferredFocusedComponent(): JComponent {
    val lastButton = when(viewModel.currentStepWithSourceTransitionFlow.value.first) {
      QodanaWizardTransition.NEXT -> buttonMap[okAction]
      QodanaWizardTransition.PREVIOUS -> buttonMap[cancelAction]
      null -> null
    }
    return if (lastButton?.isVisible == true && lastButton.isEnabled) lastButton else centerPanel
  }

  override fun getHelpId(): String = QodanaWebHelpProvider.WEBSITE_ID

  override fun dispose() {
    scope.cancel()
    super.dispose()
  }
}

private fun JButton.configureByButtonDescriptor(buttonDescriptor: QodanaWizardStepViewProvider.ButtonDescriptor?) {
  if (buttonDescriptor == null) {
    this.isVisible = false
    return
  }
  this.isVisible = true
  this.action = object : AbstractAction(buttonDescriptor.text) {
    override fun actionPerformed(e: ActionEvent?) {
      buttonDescriptor.action.invoke()
    }

    override fun isEnabled(): Boolean = buttonDescriptor.isEnabled
  }
}