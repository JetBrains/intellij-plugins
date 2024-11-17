// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBLabel
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import org.jetbrains.annotations.Nls
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel

private const val ICON_TEXT_HGAP: Int = 4

internal class ToolExecutableTestButtonComponent(
  private val pathDetector: ToolPathDetector,
  private val toolType: TfToolType,
  buttonText: @Nls String,
  parentDisposable: Disposable?,
  private val installAction: ((Boolean) -> Unit) -> Unit,
  private val test: suspend CoroutineScope.() -> @NlsSafe String,
) : JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)) {

  private val button = JButton(buttonText)
  private val resultLabel = JBLabel()
  private val spinnerIcon: AsyncProcessIcon = createSpinnerIcon(parentDisposable)
  private val installButton: ActionLink = createInstallButton()

  var text: @NlsContexts.Label String
    get() = button.text
    set(value) {
      button.text = value
    }

  init {

    resultLabel.isAllowAutoWrapping = true

    resultLabel.setCopyable(true)

    resultLabel.border = JBUI.Borders.emptyLeft(UIUtil.DEFAULT_HGAP)

    add(button)
    add(spinnerIcon)
    add(resultLabel)
    if (!toolType.downloadServerUrl.isEmpty()) {
      add(installButton)
    }

    button.addActionListener {
      button.isEnabled = false
      resultLabel.text = HCLBundle.message("tool.testResultLabel.progressTitle")
      resultLabel.icon = AllIcons.Actions.BuildLoadChanges

      val (result, exception) = try {
        installButton.isVisible = false
        runTestBlocking(resultLabel.text) to null
      }
      catch (exception: Exception) {
        installButton.isVisible = true
        null to exception.takeUnless { it is ProcessCanceledException }
      }

      resultLabel.text = result ?: HCLBundle.message("tool.testResultLabel.not.found", toolType.displayName)
      resultLabel.icon = if (result != null) AllIcons.General.InspectionsOK
      else if (exception != null) AllIcons.General.BalloonWarning
      else null

      button.isEnabled = true
      button.requestFocus()
    }
  }

  private fun createSpinnerIcon(parentDisposable: Disposable?): AsyncProcessIcon {
    return AsyncProcessIcon("TerraformToolInstallationProgress").apply {
      border = JBUI.Borders.emptyLeft(UIUtil.DEFAULT_HGAP)
      isVisible = false
      parentDisposable?.let { Disposer.register(it, this) }
    }
  }

  private fun createInstallButton(): ActionLink {
    val actionLink = ActionLink(HCLBundle.message("tool.installButton.text"))
    actionLink.border = JBUI.Borders.emptyLeft(UIUtil.DEFAULT_HGAP)
    actionLink.isVisible = false

    actionLink.addActionListener {
      actionLink.isVisible = false

      spinnerIcon.isVisible = true
      resultLabel.border = JBUI.Borders.emptyLeft(ICON_TEXT_HGAP)

      resultLabel.text = HCLBundle.message("tool.testResultLabel.download.progress.title", toolType.displayName)
      resultLabel.icon = null

      installAction(::handleInstallationResult)
    }

    return actionLink
  }

  private fun handleInstallationResult(success: Boolean) {
    spinnerIcon.isVisible = false
    resultLabel.border = JBUI.Borders.emptyLeft(UIUtil.DEFAULT_HGAP)

    if (success) {
      resultLabel.text = HCLBundle.message("tool.testResultLabel.installed", toolType.displayName)
      resultLabel.icon = AllIcons.General.InspectionsOK
    }
    else {
      resultLabel.text = HCLBundle.message("tool.testResultLabel.not.installed", toolType.displayName)
      resultLabel.icon = AllIcons.General.BalloonError
    }
  }

  @RequiresEdt
  private fun runTestBlocking(title: @NlsContexts.ProgressTitle String): String = runWithModalProgressBlocking(
    owner = ModalTaskOwner.component(this),
    title = title,
  ) { updateTestButton(test()) }

  private fun updateTestButton(toolPath: String): String {
    this.text =
      if (toolPath.isEmpty() && pathDetector.detectedPath() == null)
        HCLBundle.message("tool.detectAndTestButton.text")
      else
        HCLBundle.message("tool.testButton.text")
    return toolPath
  }

}