@file:Suppress("DuplicatedCode") // Duplication is intentional to have separate stats from AnActions
// TODO : figure out how to conveniently send separate statistics from these AnActions without code duplications

package org.jetbrains.qodana.ui.problemsView

import com.intellij.collaboration.ui.CollaborationToolsUIUtil.isDefault
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.EmptyAction
import com.intellij.ui.components.BrowserLink
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.icons.scaleIconOrLoadCustomVersion
import com.intellij.util.Url
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.NamedColorUtil
import icons.QodanaIcons
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.stats.LearnMoreSource
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.ui.QODANA_PROMO_URL
import org.jetbrains.qodana.ui.problemsView.viewModel.*
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

fun qodanaPanelViewIfNotLoaded(uiState: QodanaProblemsViewModel.UiState): JComponent? {
  return when(uiState) {
    is QodanaProblemsViewModel.UiState.Loaded -> null
    is QodanaProblemsViewModel.UiState.LoadingReport -> loadingReportPanel(uiState)
    is QodanaProblemsViewModel.UiState.RunningQodana -> runningQodanaPanel(uiState)
    is QodanaProblemsViewModel.UiState.Authorizing -> authorizingPanel(uiState)
    is QodanaProblemsViewModel.UiState.NotAuthorized -> notAuthorizedPanel(uiState)
    is QodanaProblemsViewModel.UiState.NotLinked -> notLinkedPanel(uiState)
    is QodanaProblemsViewModel.UiState.Linked -> linkedPanel(uiState)
  }
}

private fun loadingReportPanel(loadingReport: QodanaProblemsViewModel.UiState.LoadingReport): JPanel {
  return wrapWithWeight(loadingIconView(QodanaBundle.message("qodana.panel.loading.results"), { loadingReport.cancel() }), DEFAULT_WEIGHT)
}

private fun runningQodanaPanel(runningQodana: QodanaProblemsViewModel.UiState.RunningQodana): JPanel {
  return wrapWithWeight(loadingIconView(QodanaBundle.message("qodana.panel.running"), { runningQodana.cancel() }), DEFAULT_WEIGHT)
}

private fun authorizingPanel(authorizing: QodanaProblemsViewModel.UiState.Authorizing): JPanel {
  val view = loadingIconView(
    QodanaBundle.message("qodana.panel.authorizing"),
    onCancel = { authorizing.cancel() },
    additionalRows = {
      row {
        text(QodanaBundle.message("qodana.settings.panel.check.license")) { authorizing.checkLicenseStatus() }
          .align(AlignX.CENTER)
          .applyToComponent {
            foreground = NamedColorUtil.getInactiveTextColor()
          }
      }
    })
  return wrapWithWeight(view, DEFAULT_WEIGHT)
}

private fun notAuthorizedPanel(
  notAuthorized: QodanaProblemsViewModel.UiState.NotAuthorized
): JComponent {
  return when(val ciState = notAuthorized.ciState) {
    is QodanaProblemsViewModel.CiState.NotPresent -> notAuthorizedCiNotPresentPanel(notAuthorized)
    is QodanaProblemsViewModel.CiState.Present -> notAuthorizedCiPresentPanel(notAuthorized, ciState)
  }
}

private fun notAuthorizedCiNotPresentPanel(
  notAuthorized: QodanaProblemsViewModel.UiState.NotAuthorized
): JPanel {
  val textPanel = JPanel(GridBagLayout())
  val icon = JBLabel(scaleIconOrLoadCustomVersion(QodanaIcons.Icons.Qodana, 1.75f))

  val gc = GridBagConstraints()
  gc.insets = JBUI.insetsBottom(LINE_INSETS)
  gc.anchor = GridBagConstraints.LINE_START
  gc.gridx = 0
  gc.gridy = 0
  textPanel.add(icon, gc)

  val label = JBLabel(QodanaBundle.message("qodana.panel.not.authorized.no.ci.label")).apply {
    border = JBUI.Borders.emptyLeft(12)
    font = JBFont.h3().asBold()
  }
  gc.gridx = 1
  textPanel.add(label, gc)

  val text = getSimpleHtmlPane(QodanaBundle.message("qodana.panel.not.authorized.no.ci.text"))
  gc.gridwidth = 3
  gc.gridx = 0
  gc.gridy = 1
  textPanel.add(text, gc)

  val panel = JPanel(GridBagLayout())
  gc.insets = JBUI.insetsRight(6)
  gc.gridwidth = 3
  gc.gridx = 0
  gc.gridy = 0
  panel.add(textPanel, gc)


  val tryLocallyButton = JButton(QodanaBundle.message("qodana.panel.try.locally.action")).apply {
    addActionListener { notAuthorized.showRunDialog() }
    isDefault = true
  }
  disableButtonIfNeeded(tryLocallyButton, localRunDisabledMessage())

  gc.gridwidth = 1
  gc.gridx = 0
  gc.gridy = 1
  panel.add(tryLocallyButton, gc)

  val logInButton = JButton(QodanaBundle.message("qodana.panel.login.action")).apply {
    addActionListener { notAuthorized.authorize() }
  }
  gc.gridx = 1
  gc.gridy = 1
  panel.add(logInButton, gc)

  val link = BrowserLink(QodanaBundle.message("qodana.panel.learn.more"), QODANA_PROMO_URL).apply {
    addActionListener {
      QodanaPluginStatsCounterCollector.LEARN_MORE_PRESSED.log(LearnMoreSource.PROBLEMS_PANEL_LINK)
    }
  }
  gc.gridx = 2
  gc.gridy = 1
  panel.add(link, gc)

  wrapWithWeight(panel, 0.33)
  return panel
}

private fun notAuthorizedCiPresentPanel(
  notAuthorized: QodanaProblemsViewModel.UiState.NotAuthorized,
  ciPresent: QodanaProblemsViewModel.CiState.Present
): JComponent {
  val logInButton = JButton(QodanaBundle.message("qodana.panel.login.action")).apply {
    addActionListener { notAuthorized.authorize() }
    isDefault = true
  }

  val tryLocallyButton = JButton(QodanaBundle.message("qodana.panel.run.locally.action")).apply {
    addActionListener { notAuthorized.showRunDialog() }
  }
  disableButtonIfNeeded(tryLocallyButton, localRunDisabledMessage())

  return getCombinedPanel(
    QodanaBundle.message("qodana.panel.not.authorized.ci.present.label"),
    QodanaBundle.message("qodana.panel.not.authorized.ci.present.text"),
    listOf(logInButton, tryLocallyButton),
    ciPresent.ciFile
  )
}

fun notLinkedPanel(
  notLinked: QodanaProblemsViewModel.UiState.NotLinked
): JComponent {
  val authorized = notLinked.authorized
  val linkProjectButton = JButton(QodanaBundle.message("qodana.panel.link.project.action")).apply {
    addActionListener { notLinked.showLinkDialog() }
    isDefault = true
  }

  val ciNotPresent = notLinked.ciState as? QodanaProblemsViewModel.CiState.NotPresent
  val setupInCIAction = if (ciNotPresent != null && isSetupCiEnabled()) {
    object : AnAction(QodanaBundle.message("qodana.panel.setup.ci.action")) {
      override fun actionPerformed(e: AnActionEvent) {
        ciNotPresent.showSetupCIDialog()
      }
    }
  } else {
    null
  }

  val runLocallyAction = if (isLocalRunEnabled()){
    object : AnAction(QodanaBundle.message("qodana.panel.run.locally.action")) {
      override fun actionPerformed(e: AnActionEvent) {
        authorized.showRunDialog()
      }
    }
  } else {
    EmptyAction()
  }
  val documentationAction = object : AnAction(QodanaBundle.message("qodana.panel.documentation.action")) {
    override fun actionPerformed(e: AnActionEvent) {
      authorized.openDocumentation()
    }
  }
  val logoutAction = object : AnAction(QodanaBundle.message("qodana.panel.logout.action", authorized.userName)) {
    override fun actionPerformed(e: AnActionEvent) {
      authorized.logOut()
    }
  }

  val moreActionsButton = createMoreActionsButton(
    listOfNotNull(setupInCIAction, runLocallyAction),
    listOf(documentationAction, logoutAction)
  )

  return getCombinedPanel(
    QodanaBundle.message("qodana.panel.authorized.not.linked.no.ci.label"),
    QodanaBundle.message("qodana.panel.authorized.not.linked.no.ci.text", notLinked.authorized.qodanaCloudUrl.toString()),
    listOf(linkProjectButton, moreActionsButton),
    null
  )
}

private fun linkedPanel(
  linked: QodanaProblemsViewModel.UiState.Linked
): JComponent {
  val availableReportUrl = linked.availableReportUrl
  return if (availableReportUrl != null) {
    linkedReportAvailablePanel(linked, availableReportUrl)
  } else {
    val ciState = linked.ciState
    when(ciState) {
      is QodanaProblemsViewModel.CiState.NotPresent -> linkedNoReportAvailableCiNotPresentPanel(linked, ciState)
      is QodanaProblemsViewModel.CiState.Present -> linkedNoReportAvailableCiPresentPanel(linked, ciState)
    }
  }
}

private fun linkedNoReportAvailableCiNotPresentPanel(
  linked: QodanaProblemsViewModel.UiState.Linked,
  ciNotPresent: QodanaProblemsViewModel.CiState.NotPresent
): JComponent {
  val authorized = linked.authorized
  val setupCIButton = JButton(QodanaBundle.message("qodana.panel.setup.ci.action")).apply {
    addActionListener { ciNotPresent.showSetupCIDialog() }
    isDefault = true
  }

  disableButtonIfNeeded(setupCIButton, setupCiDisabledMessage())

  val runLocallyAction = if (isLocalRunEnabled()){
    object : AnAction(QodanaBundle.message("qodana.panel.run.locally.action")) {
      override fun actionPerformed(e: AnActionEvent) {
        authorized.showRunDialog()
      }
    }
  } else {
    EmptyAction()
  }
  val documentationAction = object : AnAction(QodanaBundle.message("qodana.panel.documentation.action")) {
    override fun actionPerformed(e: AnActionEvent) {
      authorized.openDocumentation()
    }
  }
  val unlinkAction = object : AnAction(QodanaBundle.message("qodana.panel.unlink.project.action", linked.cloudProjectName)) {
    override fun actionPerformed(e: AnActionEvent) {
      linked.unlink()
    }
  }
  val logoutAction = object : AnAction(QodanaBundle.message("qodana.panel.logout.action", authorized.userName)) {
    override fun actionPerformed(e: AnActionEvent) {
      authorized.logOut()
    }
  }

  val moreActionsButton = createMoreActionsButton(
    listOf(runLocallyAction, documentationAction),
    listOf(unlinkAction, logoutAction)
  )

  val labelContent = QodanaBundle.message("qodana.panel.authorized.linked.no.ci.label.with.link", linked.cloudProjectUrl, linked.cloudProjectName)

  return getCombinedPanel(
    labelContent,
    QodanaBundle.message("qodana.panel.authorized.linked.no.ci.text"),
    listOf(setupCIButton, moreActionsButton),
    null
  )
}

private fun linkedNoReportAvailableCiPresentPanel(
  linked: QodanaProblemsViewModel.UiState.Linked,
  ciPresent: QodanaProblemsViewModel.CiState.Present
): JComponent {
  val authorized = linked.authorized
  val openReportButton = JButton(QodanaBundle.message("qodana.panel.open.report.action")).apply {
    addActionListener { linked.openReport() }
    isDefault = true
  }

  val documentationAction = object : AnAction(QodanaBundle.message("qodana.panel.documentation.action")) {
    override fun actionPerformed(e: AnActionEvent) {
      authorized.openDocumentation()
    }
  }
  val runLocallyAction = if (isLocalRunEnabled()){
    object : AnAction(QodanaBundle.message("qodana.panel.run.locally.action")) {
      override fun actionPerformed(e: AnActionEvent) {
        authorized.showRunDialog()
      }
    }
  } else {
    EmptyAction()
  }
  val unlinkAction = object : AnAction(QodanaBundle.message("qodana.panel.unlink.project.action", linked.cloudProjectName)) {
    override fun actionPerformed(e: AnActionEvent) {
      linked.unlink()
    }
  }
  val logoutAction = object : AnAction(QodanaBundle.message("qodana.panel.logout.action", authorized.userName)) {
    override fun actionPerformed(e: AnActionEvent) {
      authorized.logOut()
    }
  }

  val moreActionsButton = createMoreActionsButton(
    listOfNotNull(documentationAction, runLocallyAction),
    listOf(unlinkAction, logoutAction)
  )

  return getCombinedPanel(
    QodanaBundle.message ("qodana.panel.authorized.linked.ci.present.label"),
    QodanaBundle.message("qodana.panel.authorized.linked.ci.present.text"),
    listOf(openReportButton, moreActionsButton),
    ciPresent.ciFile
  )
}

private fun linkedReportAvailablePanel(
  linked: QodanaProblemsViewModel.UiState.Linked,
  reportUrl: Url,
): JComponent {
  val authorized = linked.authorized
  val openReportButton = JButton(QodanaBundle.message("qodana.panel.open.report.action")).apply {
    addActionListener { linked.openReport() }
    isDefault = true
  }

  val runLocallyAction = if (isLocalRunEnabled()){
    object : AnAction(QodanaBundle.message("qodana.panel.run.locally.action")) {
      override fun actionPerformed(e: AnActionEvent) {
        authorized.showRunDialog()
      }
    }
  } else {
    EmptyAction()
  }
  val documentationAction = object : AnAction(QodanaBundle.message("qodana.panel.documentation.action")) {
    override fun actionPerformed(e: AnActionEvent) {
      authorized.openDocumentation()
    }
  }
  val unlinkAction = object : AnAction(QodanaBundle.message("qodana.panel.unlink.project.action", linked.cloudProjectName)) {
    override fun actionPerformed(e: AnActionEvent) {
      linked.unlink()
    }
  }
  val logoutAction = object : AnAction(QodanaBundle.message("qodana.panel.logout.action", authorized.userName)) {
    override fun actionPerformed(e: AnActionEvent) {
      authorized.logOut()
    }
  }

  val moreActionsButton = createMoreActionsButton(
    listOf(runLocallyAction, documentationAction),
    listOf(unlinkAction, logoutAction)
  )

  val labelContent = QodanaBundle.message("qodana.panel.authorized.linked.no.ci.label.with.link", linked.cloudProjectUrl, linked.cloudProjectName)

  return getCombinedPanel(
    labelContent,
    QodanaBundle.message("qodana.panel.authorized.linked.with.report.text", reportUrl.toString()),
    listOf(openReportButton, moreActionsButton),
    null
  )
}