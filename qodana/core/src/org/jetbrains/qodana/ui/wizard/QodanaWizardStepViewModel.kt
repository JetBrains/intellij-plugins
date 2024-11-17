package org.jetbrains.qodana.ui.wizard

import kotlinx.coroutines.flow.Flow

interface QodanaWizardStepViewModel {
  val stepTransitionFlow: Flow<Pair<QodanaWizardTransition, String?>>
}