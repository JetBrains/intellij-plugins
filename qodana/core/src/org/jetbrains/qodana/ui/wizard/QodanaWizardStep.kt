package org.jetbrains.qodana.ui.wizard
interface QodanaWizardStep {
  val id: String

  val viewModel: QodanaWizardStepViewModel

  val viewProvider: QodanaWizardStepViewProvider
}

enum class QodanaWizardTransition {
  NEXT, PREVIOUS
}