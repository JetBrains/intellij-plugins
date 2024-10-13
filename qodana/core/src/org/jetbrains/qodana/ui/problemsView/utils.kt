package org.jetbrains.qodana.ui.problemsView

import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.HelpTooltip
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.BrowserHyperlinkListener
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBOptionButton
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.IconUtil
import com.intellij.util.ui.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.stats.LearnMoreSource
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.ui.ci.CIFile
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import javax.swing.*

internal const val LINE_INSETS = 12
private const val TOP_PANEL_GAP = 40
private const val ICON_BOTTOM_GAP = 6
internal const val DEFAULT_WEIGHT = 0.33

internal fun getCombinedPanel(
  @NlsSafe labelContent: String,
  @NlsSafe textContent: String,
  buttons: List<JButton>,
  ciFile: CIFile.ExistingWithQodana?
): JComponent {
  val textPanel = getTextPanel(labelContent, textContent, ciFile)

  val gc = GridBagConstraints()
  val panel = JPanel(GridBagLayout())

  gc.gridwidth = 3
  gc.insets = JBUI.emptyInsets()
  gc.gridx = 0
  gc.gridy = 0
  gc.fill = GridBagConstraints.NONE
  gc.anchor = GridBagConstraints.NORTH
  panel.add(textPanel, gc)

  gc.gridwidth = 1
  gc.insets = JBUI.insetsRight(6)
  gc.gridx = 0
  gc.gridy = 1
  buttons.forEach {
    panel.add(it, gc)
    gc.gridx++
  }

  return wrapWithWeight(panel, 0.33)
}

private fun getTextPanel(@NlsSafe labelText: String, content: String, ciFile: CIFile.ExistingWithQodana?): JPanel {
  val textPanel = JPanel(GridBagLayout())

  val gc = GridBagConstraints()
  gc.insets = JBUI.insetsBottom(LINE_INSETS)
  gc.gridx = 0
  gc.gridy = 0
  gc.anchor = GridBagConstraints.LINE_START

  val label = getSimpleHtmlPane(labelText).apply {
    font = JBFont.h3().asBold()
  }
  textPanel.add(label, gc)

  if (ciFile != null) {
    val tooltipIcon = getTooltip(ciFile)
    gc.gridx = 1
    textPanel.add(tooltipIcon, gc)
  }

  val text = getSimpleHtmlPane(content)
  gc.gridwidth = 2
  gc.gridx = 0
  gc.gridy = 1
  textPanel.add(text, gc)
  return textPanel
}

internal fun wrapWithWeight(panel: JPanel, weight: Double): JPanel {
  val mainPanel = JPanel(GridBagLayout())
  val gc = GridBagConstraints()
  gc.gridx = 0
  gc.gridy = 0
  gc.weighty = weight
  val filler = JPanel().apply { isOpaque = false }
  mainPanel.add(filler, gc)

  gc.gridy = 1
  gc.weighty = 1 - weight
  gc.anchor = GridBagConstraints.PAGE_START
  mainPanel.add(panel, gc)

  return mainPanel
}

private fun getTooltip(ciFile: CIFile.ExistingWithQodana): JBLabel {
  val tooltipIcon = JBLabel(AllIcons.General.ContextHelp).apply {
    border = JBUI.Borders.emptyLeft(8)
  }
  HelpTooltip()
    .setDescription(QodanaBundle.message("qodana.panel.ci.location.tooltip.text", ciFile.ciFileChecker.ciPart, ciFile.path))
    .setLink(QodanaBundle.message("qodana.panel.learn.more")) {
      BrowserUtil.browse(QodanaBundle.message("qodana.documentation.ci.url"))
      QodanaPluginStatsCounterCollector.LEARN_MORE_PRESSED.log(LearnMoreSource.TOOLTIP)
    }
    .setNeverHideOnTimeout(true)
    .installOn(tooltipIcon)
  return tooltipIcon
}

internal fun loadingIconView(
  @NlsContexts.Label text: String,
  onCancel: () -> Unit,
  additionalRows: (Panel.() -> Row)? = null
): DialogPanel {
  return panel {
    val progressIcon = AnimatedIcon.Default()
    row {
      icon(IconUtil.resizeSquared(progressIcon, 24))
        .align(AlignX.CENTER)
        .customize(UnscaledGaps(TOP_PANEL_GAP, 0, ICON_BOTTOM_GAP, 0))
    }
    row {
      label(text)
        .align(AlignX.CENTER)
        .applyToComponent {
          foreground = NamedColorUtil.getInactiveTextColor()
        }
    }.bottomGap(BottomGap.SMALL)
    row {
      button(CommonBundle.getCancelButtonText()) { onCancel.invoke() }
        .align(AlignX.CENTER)
        .applyToComponent {
          isOpaque = false
        }
    }
    additionalRows?.invoke(this)
  }.apply {
    isFocusable = true
  }
}

internal fun getSimpleHtmlPane(@NlsSafe content: String): JEditorPane {
  return JEditorPane().apply {
    background = UIUtil.getPanelBackground()
    addHyperlinkListener(BrowserHyperlinkListener.INSTANCE)
    isEditable = false
    editorKit = HTMLEditorKitBuilder.simple()
    text = content
    isFocusable = false
  }
}

@NlsContexts.Tooltip
internal fun localRunDisabledMessage(): String? {
  if (QodanaRegistry.isForceLocalRunEnabled) return null
  return when(ApplicationInfo.getInstance().build.productCode) {
    "CL" -> QodanaBundle.message("qodana.panel.local.run.disabled.clion")
    "AI" -> QodanaBundle.message("qodana.panel.local.run.disabled.androidstudio")
    else -> null
  }
}

@NlsContexts.Tooltip
internal fun setupCiDisabledMessage(): String? {
  if (QodanaRegistry.isForceSetupCIEnabled) return null
  return when(ApplicationInfo.getInstance().build.productCode) {
    "CL" -> QodanaBundle.message("qodana.panel.setup.ci.disabled.clion")
    else -> null
  }
}

internal fun isLocalRunEnabled(): Boolean {
  return localRunDisabledMessage() == null
}

internal fun isSetupCiEnabled(): Boolean {
  return setupCiDisabledMessage() == null
}

internal fun disableButtonIfNeeded(button: JButton, @NlsContexts.Tooltip disabledMessage: String?): JButton {
  if (disabledMessage != null) {
    button.isEnabled = false
    HelpTooltip()
      .setDescription(disabledMessage)
      .installOn(button)
  }
  return button
}

internal fun createMoreActionsButton(topActions: List<AnAction>, bottomActions: List<AnAction>): JButton {
  return JBOptionButton(
    null, null
  ).apply {
    setOptions(
      listOf(
        object : DefaultActionGroup(topActions) {},
        object : DefaultActionGroup(bottomActions) {}
      )
    )

    val moreActions = object : AbstractAction(QodanaBundle.message("qodana.panel.more.actions")){
      override fun actionPerformed(e: ActionEvent?) {
        this@apply.showPopup()
      }
    }
    action = moreActions
  }
}