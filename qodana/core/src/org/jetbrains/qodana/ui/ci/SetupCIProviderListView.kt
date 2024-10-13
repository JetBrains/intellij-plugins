package org.jetbrains.qodana.ui.ci

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.HelpTooltip
import com.intellij.ui.CollectionListModel
import com.intellij.ui.SingleSelectionModel
import com.intellij.ui.components.JBList
import com.intellij.util.IconUtil
import com.intellij.util.ui.*
import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridBagLayout
import javax.swing.*

class SetupCIProviderListView(
  providers: List<SetupCIProvider>,
  private val viewSpec: CombinedSetupCIViewSpec.ProviderListSpec
) : JPanel() {
  val providersList = SetupCIProviderList(CollectionListModel(providers.filterIsInstance<SetupCIProvider.Available>()), viewSpec)

  init {
    val innerPanel = JPanel().apply {
      layout = GridBagLayout()
      border = JBUI.Borders.empty()

      var gbc = GridBag().nextLine().next()
        .fillCellNone()
      add(providersList, gbc)
      providers.filterIsInstance<SetupCIProvider.Unavailable>().map { provider ->
        val component = getProviderComponent(providersList, provider, viewSpec, false, false)

        val icon = JLabel(AllIcons.General.ContextHelp)
        HelpTooltip()
          .setDescription(provider.tooltipText)
          .setLink(provider.helpPageText, { BrowserUtil.browse(provider.helpPageLink) }, true)
          .setNeverHideOnTimeout(true)
          .installOn(icon)
        component.add(icon)
        component
      }.forEach {
        gbc = gbc.nextLine()
          .insets(JBInsets.emptyInsets())
          .fillCellHorizontally()
        add(it, gbc)
      }
    }
    layout = BorderLayout()
    add(innerPanel, BorderLayout.NORTH)
  }
}

class SetupCIProviderList(
  listModel: ListModel<SetupCIProvider.Available>,
  private val viewSpec: CombinedSetupCIViewSpec.ProviderListSpec
) : JBList<SetupCIProvider.Available>(listModel) {
  init {
    border = JBUI.Borders.empty()
    selectionModel = SingleSelectionModel()
    val renderer = Renderer()
    cellRenderer = renderer
    preferredSize = JBDimension(viewSpec.listWidth, preferredSize.height)
  }

  private inner class Renderer : ListCellRenderer<SetupCIProvider> {
    override fun getListCellRendererComponent(list: JList<out SetupCIProvider>,
                                              value: SetupCIProvider?,
                                              index: Int,
                                              isSelected: Boolean,
                                              cellHasFocus: Boolean): Component {
      val provider = value as SetupCIProvider
      return getProviderComponent(list, provider, viewSpec, isSelected, true)
    }
  }
}

private fun getProviderComponent(
  list: JList<out SetupCIProvider>,
  provider: SetupCIProvider,
  viewSpec: CombinedSetupCIViewSpec.ProviderListSpec,
  isSelected: Boolean,
  enabled: Boolean
): JPanel {
  val mainComponent = JPanel(GridBagLayout()).apply {
    border = JBUI.Borders.empty(viewSpec.borderVertical, viewSpec.borderHorizontal)
    UIUtil.setBackgroundRecursively(this, ListUiUtil.WithTallRow.background(list, isSelected, true))
    isEnabled = enabled
  }

  val scale = viewSpec.iconSize.toFloat() / provider.icon.iconWidth.toFloat()
  val iconLabel = JLabel().apply {
    icon = IconUtil.scale(provider.icon, null, scale)
    isEnabled = true
  }

  val titleLabel = JLabel().apply {
    text = provider.text
    font = JBUI.Fonts.label().asBold()
    foreground = ListUiUtil.WithTallRow.foreground(isSelected, true)
    isEnabled = enabled
  }

  var gbc = GridBag().nextLine().next()
    .insets(JBUI.insetsRight(viewSpec.insetBetweenIconAndText))
    .weightx(0.0)
    .anchor(GridBag.LINE_START)
    .fillCellNone()
  mainComponent.add(iconLabel, gbc)

  gbc = gbc.next()
    .weightx(1.0)
    .insets(JBInsets.emptyInsets())
    .fillCellHorizontally()
  mainComponent.add(titleLabel, gbc)

  return mainComponent
}