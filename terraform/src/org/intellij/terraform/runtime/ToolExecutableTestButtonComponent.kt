// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.ui.TextFieldWithBrowseButton
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
import org.intellij.terraform.install.FailedInstallation
import org.intellij.terraform.install.InstallationResult
import org.intellij.terraform.install.SuccessfulInstallation
import org.intellij.terraform.install.TfToolType
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

private const val ICON_TEXT_HGAP: Int = 4

internal class ToolExecutableTestButtonComponent(
  private val toolType: TfToolType,
  parentDisposable: Disposable?,
  private val fieldToUpdate: TextFieldWithBrowseButton?,
  private val installAction: ((InstallationResult) -> Unit) -> Unit,
  private val validateAndTestAction: suspend CoroutineScope.() -> @NlsSafe String,
) : JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)) {

  private val button = JButton()
  private val resultLabel = JBLabel("", AllIcons.Empty, SwingConstants.LEFT)
  private val spinnerIcon: AsyncProcessIcon = createSpinnerIcon(parentDisposable)
  private val installButton: ActionLink = createInstallButton()

  var text: @NlsContexts.Label String
    get() = button.text
    set(value) {
      button.text = value
      button.revalidate()
    }

  init {

    resultLabel.border = JBUI.Borders.emptyLeft(UIUtil.DEFAULT_HGAP)

    add(button)
    add(spinnerIcon)
    add(resultLabel)
    if (!toolType.downloadServerUrl.isEmpty()) {
      add(installButton)
    }

    button.addActionListener {
      spinnerIcon.isVisible = true
      spinnerIcon.requestFocus()
      button.isEnabled = false
      resultLabel.text = HCLBundle.message("tool.testResultLabel.progressTitle")
      resultLabel.icon = AllIcons.Actions.BuildLoadChanges
      val (result, exception) = try {
        installButton.isVisible = false
        runTestBlocking(resultLabel.text) to null
      }
      catch (exception: Exception) {
        logger<ToolExecutableTestButtonComponent>().warnWithDebug(exception)
        null to exception.takeUnless { it is ProcessCanceledException }
      }

      resultLabel.text =
        if (result.isNullOrEmpty()) HCLBundle.message("tool.testResultLabel.not.found", toolType.displayName)
        else result
      resultLabel.icon =
        if (!result.isNullOrEmpty()) AllIcons.General.InspectionsOK
        else if (exception != null || result.isNullOrEmpty()) {
          installButton.isVisible = true
          AllIcons.General.BalloonWarning
        }
        else AllIcons.Empty

      spinnerIcon.isVisible = false
      button.isEnabled = true
      button.requestFocus()
    }
    updateTestButton(fieldToUpdate?.text)
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
      resultLabel.icon = AllIcons.Empty

      installAction(::handleInstallationResult)
    }

    return actionLink
  }

  private fun handleInstallationResult(success: InstallationResult) {
    spinnerIcon.isVisible = false
    resultLabel.border = JBUI.Borders.emptyLeft(UIUtil.DEFAULT_HGAP)
    when (success) {
      is SuccessfulInstallation -> {
        fieldToUpdate?.text = success.binary.toAbsolutePath().toString()
        resultLabel.text = HCLBundle.message("tool.testResultLabel.installed", toolType.displayName)
        resultLabel.icon = AllIcons.General.InspectionsOK
      }
      is FailedInstallation -> {
        resultLabel.text = HCLBundle.message("tool.testResultLabel.not.installed", toolType.displayName)
        resultLabel.icon = AllIcons.General.BalloonError
      }
    }
  }

  @RequiresEdt
  private fun runTestBlocking(title: @NlsContexts.ProgressTitle String): String = runWithModalProgressBlocking(
    owner = ModalTaskOwner.component(this),
    title = title,
  ) {
    val executionResult = validateAndTestAction()
    updateTestButton(executionResult)
  }

  fun updateTestButton(toolPath: String?): String {
    installButton.isVisible = false
    this.text =
      if (toolPath.isNullOrEmpty()) {
        resultLabel.icon = AllIcons.General.BalloonWarning
        resultLabel.text = HCLBundle.message("tool.testResultLabel.not.found", toolType.displayName)
        installButton.isVisible = true
        HCLBundle.message("tool.detectAndTestButton.text")
      }
      else {
        resultLabel.icon = AllIcons.Empty
        resultLabel.text = ""
        HCLBundle.message("tool.testButton.text")
      }
    return toolPath ?: ""
  }

}