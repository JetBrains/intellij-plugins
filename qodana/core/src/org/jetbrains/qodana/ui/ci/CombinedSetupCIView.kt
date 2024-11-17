package org.jetbrains.qodana.ui.ci

import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.SideBorder
import com.intellij.ui.components.panels.Wrapper
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.setContentAndRepaint
import javax.swing.JPanel
import javax.swing.event.ListSelectionListener

class CombinedSetupCIView(
  viewScope: CoroutineScope,
  val viewModel: CombinedSetupCIViewModel,
  private val viewSpec: CombinedSetupCIViewSpec
) {
  private val setupCiProviderListView = SetupCIProviderListView(viewModel.allSetupCIProviders, viewSpec.providerListSpec).apply {
    providersList.addListSelectionListener(ListSelectionListener { e ->
      val source = e.source as SetupCIProviderList
      viewModel.setActiveCIPanelProvider(source.selectedValue)
    })
  }

  private val mainPanelView = Wrapper().apply {
    val mainViewSpec = viewSpec.mainViewSpec
    border = JBUI.Borders.empty(mainViewSpec.borderTop, mainViewSpec.borderLeft, mainViewSpec.borderBottom, mainViewSpec.borderRight)
  }

  init {
    viewScope.launch(QodanaDispatchers.Ui, start = CoroutineStart.UNDISPATCHED) {
      val availableSetupCIProviders = viewModel.allSetupCIProviders.filterIsInstance<SetupCIProvider.Available>()
      val setupProvidersToView: Map<SetupCIProvider, Wrapper> = availableSetupCIProviders.associateWith { Wrapper() }

      availableSetupCIProviders.forEach { setupCIProvider ->
        launch(start = CoroutineStart.UNDISPATCHED) {
          setupCIProvider.viewFlow.collect {
            setupProvidersToView[setupCIProvider]!!.setContentAndRepaint(it)
          }
        }
      }

      viewModel.uiState
        .filterIsInstance<CombinedSetupCIViewModel.UiState.ActivePanelProvider>()
        .collectLatest { activePanelProvider ->
          val ciProvider = activePanelProvider.ciPanelProvider

          ScrollingUtil.selectItem(setupCiProviderListView.providersList, ciProvider)
          mainPanelView.setContentAndRepaint(setupProvidersToView[ciProvider]!!)
        }
    }
  }

  fun getView(): JPanel {
    val scrollableList = ScrollPaneFactory.createScrollPane(setupCiProviderListView, true).apply {
      border = IdeBorderFactory.createBorder(SideBorder.RIGHT)
    }
    return JBUI.Panels.simplePanel()
      .addToCenter(mainPanelView)
      .addToLeft(scrollableList).apply {
        val viewSize = JBUI.size(viewSpec.width, viewSpec.height)
        minimumSize = viewSize
        preferredSize = viewSize
      }
  }
}