package org.jetbrains.qodana.ui.wizard

import com.intellij.openapi.util.NlsContexts
import kotlinx.coroutines.flow.Flow
import javax.swing.JComponent

interface QodanaWizardStepViewProvider {
  val titleFlow: Flow<@NlsContexts.DialogTitle String>

  val mainViewFlow: Flow<JComponent>

  val nextButtonDescriptorFlow: Flow<ButtonDescriptor?>

  val previousButtonDescriptorFlow: Flow<ButtonDescriptor?>

  class ButtonDescriptor(val isEnabled: Boolean, val text: @NlsContexts.Button String, val action: () -> Unit)
}