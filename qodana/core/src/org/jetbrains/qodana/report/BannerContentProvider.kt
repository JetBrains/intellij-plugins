package org.jetbrains.qodana.report

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.stats.PanelActions
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SetupCiDialogSource
import org.jetbrains.qodana.ui.ci.QodanaCIConfigService
import org.jetbrains.qodana.ui.ci.showSetupCIDialogOrWizardWithYaml

class BannerContentProvider(
  @NlsContexts.Label val text: String,
  val actions: List<Action>,
  val onClose: () -> Unit,
) {
  companion object {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun openBrowserAndSetupCIBannerFlow(
      project: Project,
      @NlsContexts.Label text: String,
      @NlsContexts.LinkLabel browserText: String,
      browserViewProviderFlow: Flow<BrowserViewProvider?>
    ): Flow<BannerContentProvider?> {
      val isBannerVisible = MutableStateFlow(true)
      val presentCIFileFlow = QodanaCIConfigService.getInstance(project).presentCIFile
      return isBannerVisible.flatMapLatest { isVisible ->
        if (!isVisible) return@flatMapLatest flowOf(null)

        combine(browserViewProviderFlow, presentCIFileFlow) { browserViewProvider, ciFile ->
          browserViewProvider?.let {
            openBrowserAndSetupCIBanner(project, text, browserText, it, showSetupCIAction = ciFile == null, onClose = {
              isBannerVisible.value = false
            })
          }
        }
      }
    }

    private fun openBrowserAndSetupCIBanner(
      project: Project,
      @NlsContexts.Label text: String,
      @NlsContexts.LinkLabel browserText: String,
      browserViewProvider: BrowserViewProvider,
      showSetupCIAction: Boolean,
      onClose: () -> Unit,
    ): BannerContentProvider {
      val openBrowserAction = Action(browserText) {
        browserViewProvider.openBrowserView()
        logOpenQodanaUiInBrowserStats(project)
      }
      val setupCIAction = Action(QodanaBundle.message("problems.toolwindow.banner.add.qodana.to.ci")) {
        showSetupCIDialogOrWizardWithYaml(project, SetupCiDialogSource.BANNER)
      }
      val onCloseWithStats = {
        onClose.invoke()
        logCloseBannerStats(project)
      }
      val actions = listOfNotNull(openBrowserAction, setupCIAction.takeIf { showSetupCIAction })
      return BannerContentProvider(text, actions, onCloseWithStats)
    }
  }

  class Action(
    @NlsContexts.LinkLabel val text: String,
    val callback: suspend () -> Unit
  )
}

private fun logCloseBannerStats(project: Project) {
  QodanaPluginStatsCounterCollector.PANEL_ACTIONS.log(project, PanelActions.CLOSE_BANNER)
}

private fun logOpenQodanaUiInBrowserStats(project: Project) {
  QodanaPluginStatsCounterCollector.PANEL_ACTIONS.log(project, PanelActions.OPEN_QODANA_BROWSER_UI_FROM_BANNER)
}
