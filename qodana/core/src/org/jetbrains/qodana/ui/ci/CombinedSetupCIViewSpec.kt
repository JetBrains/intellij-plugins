package org.jetbrains.qodana.ui.ci

import org.jetbrains.qodana.ui.run.wizard.QODANA_RUN_WIZARD_DIALOG_HEIGHT
import org.jetbrains.qodana.ui.run.wizard.QODANA_RUN_WIZARD_DIALOG_WIDTH

data class CombinedSetupCIViewSpec(
  val width: Int = QODANA_RUN_WIZARD_DIALOG_WIDTH,
  val height: Int = QODANA_RUN_WIZARD_DIALOG_HEIGHT,
  val providerListSpec: ProviderListSpec = ProviderListSpec(),
  val mainViewSpec: MainViewSpec = MainViewSpec()
) {
  data class ProviderListSpec(
    val listWidth: Int = 180,
    val iconSize: Int = 22,
    val insetBetweenIconAndText: Int = 6,
    val borderVertical: Int = 10,
    val borderHorizontal: Int = 10
  )

  data class MainViewSpec(
    val borderLeft: Int = 8,
    val borderRight: Int = 8,
    val borderTop: Int = 12,
    val borderBottom: Int = 12,
    val borderVertical: Int = 8,
    val borderHorizontal: Int = 12,
  )
}