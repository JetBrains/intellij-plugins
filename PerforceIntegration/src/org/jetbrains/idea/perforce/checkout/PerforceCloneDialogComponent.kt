// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("IO_FILE_USAGE")

package org.jetbrains.idea.perforce.checkout

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vcs.CheckoutProvider
import com.intellij.openapi.vcs.ui.VcsCloneComponent
import com.intellij.openapi.vcs.ui.cloneDialog.VcsCloneDialogComponentStateListener

import com.intellij.openapi.wm.impl.welcomeScreen.cloneableProjects.CloneableProjectsService
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.COLUMNS_MEDIUM
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.perforce.ConnectionId
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.jetbrains.idea.perforce.perforce.connections.P4ConnectionParameters
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionProblemsNotifier
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionTester
import org.jetbrains.idea.perforce.perforce.connections.TestParametersConnection
import java.io.File
import java.nio.file.Path
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

internal class PerforceCloneDialogComponent(
  private val project: Project,
  private val dialogStateListener: VcsCloneDialogComponentStateListener,
) : VcsCloneComponent {

  private lateinit var serverField: JBTextField
  private lateinit var userField: JBTextField
  private lateinit var passwordField: JBPasswordField
  private lateinit var clientField: JBTextField
  private lateinit var directoryField: TextFieldWithBrowseButton

  private val panel = panel {
    row(PerforceBundle.message("checkout.server.label")) {
      serverField = textField()
        .columns(COLUMNS_MEDIUM)
        .applyToComponent {
          emptyText.text = PerforceBundle.message("settings.port.placeholder")
        }
        .component
    }
    row(PerforceBundle.message("checkout.user.label")) {
      userField = textField()
        .columns(COLUMNS_MEDIUM)
        .component
    }
    row(PerforceBundle.message("checkout.password.label")) {
      passwordField = cell(JBPasswordField())
        .columns(COLUMNS_MEDIUM)
        .component
    }
    row(PerforceBundle.message("checkout.client.label")) {
      clientField = textField()
        .columns(COLUMNS_MEDIUM)
        .component
    }
    row(PerforceBundle.message("checkout.directory.label")) {
      directoryField = textFieldWithBrowseButton(
        FileChooserDescriptorFactory.createSingleFolderDescriptor()
          .withTitle(PerforceBundle.message("checkout.directory.chooser.title")),
        project
      )
        .columns(COLUMNS_MEDIUM)
        .component
    }
    row {
      button(PerforceBundle.message("checkout.test.connection")) { testConnectionAndShowResult() }
        .align(AlignX.LEFT)
    }
  }.apply {
    border = UIUtil.PANEL_REGULAR_INSETS.let {
      javax.swing.BorderFactory.createEmptyBorder(0, it.left, it.bottom, it.right)
    }
  }

  init {
    val listener = object : DocumentAdapter() {
      override fun textChanged(e: DocumentEvent) {
        dialogStateListener.onOkActionEnabled(isOkEnabled())
      }
    }
    serverField.document.addDocumentListener(listener)
    userField.document.addDocumentListener(listener)
    clientField.document.addDocumentListener(listener)
    directoryField.textField.document.addDocumentListener(listener)
  }

  override fun getView(): JComponent = panel

  override fun doClone(listener: CheckoutProvider.Listener) {
    val params = PerforceCloneParams(
      server = serverField.text.trim(),
      user = userField.text.trim(),
      password = String(passwordField.password),
      client = clientField.text.trim(),
      directory = directoryField.text.trim(),
    )

    val projectPath = Path.of(params.directory).toAbsolutePath()
    val cloneTask = PerforceCloneTask(project, params, listener)

    CloneableProjectsService.getInstance().runCloneTask(projectPath, cloneTask)
  }

  override fun isOkEnabled(): Boolean {
    return serverField.text.isNotBlank() &&
           userField.text.isNotBlank() &&
           clientField.text.isNotBlank() &&
           directoryField.text.isNotBlank()
  }

  override fun doValidateAll(): List<ValidationInfo> {
    val result = mutableListOf<ValidationInfo>()

    if (serverField.text.isBlank()) {
      result.add(ValidationInfo(PerforceBundle.message("checkout.validation.server.empty"), serverField))
    }
    if (userField.text.isBlank()) {
      result.add(ValidationInfo(PerforceBundle.message("checkout.validation.user.empty"), userField))
    }
    if (clientField.text.isBlank()) {
      result.add(ValidationInfo(PerforceBundle.message("checkout.validation.client.empty"), clientField))
    }
    if (directoryField.text.isBlank()) {
      result.add(ValidationInfo(PerforceBundle.message("checkout.validation.directory.empty"), directoryField))
    }
    else {
      val dir = File(directoryField.text)
      if (dir.exists() && !dir.isDirectory) {
        result.add(ValidationInfo(PerforceBundle.message("checkout.validation.directory.not.dir"), directoryField))
      }
    }

    if (result.isEmpty()) {
      return listOfNotNull(testConnection())
    }

    return result
  }

  override fun getOkButtonText(): String = PerforceBundle.message("checkout.button.sync")

  override fun getPreferredFocusedComponent(): JComponent = serverField

  override fun dispose() {}

  private fun testConnectionAndShowResult() {
    val server = serverField.text.trim()
    val user = userField.text.trim()

    if (server.isBlank() || user.isBlank()) {
      Messages.showWarningDialog(
        panel,
        PerforceBundle.message("checkout.test.connection.fill.fields"),
        PerforceBundle.message("checkout.test.connection.title")
      )
      return
    }

    val result = doTestConnection()

    if (result.isCancelled) {
      Messages.showMessageDialog(
        project,
        PerforceBundle.message("connection.cancelled"),
        PerforceBundle.message("connection.state.title"),
        Messages.getErrorIcon()
      )
      return
    }

    if (result.errorMessage != null) {
      Messages.showErrorDialog(
        project,
        result.errorMessage,
        PerforceBundle.message("connection.state.title")
      )
      return
    }

    val info = result.info ?: return
    PerforceConnectionProblemsNotifier.showSingleConnectionState(project, info)
  }

  private fun testConnection(): ValidationInfo? {
    val result = doTestConnection()

    if (result.isCancelled) {
      return null
    }

    if (result.errorMessage != null) {
      return ValidationInfo(result.errorMessage)
    }

    val info = result.info ?: return null
    val errorMessage = PerforceConnectionProblemsNotifier.getSingleConnectionState(info)
    return if (errorMessage.isBlank()) null else ValidationInfo(errorMessage)
  }

  private fun doTestConnection(): PerforceConnectionTester.TestConnectionResult {
    val server = serverField.text.trim()
    val user = userField.text.trim()
    val password = String(passwordField.password)
    val client = clientField.text.trim()
    val directory = directoryField.text.trim()

    val clientRoot = Path.of(directory)
    if (!clientRoot.toFile().isDirectory) {
      return PerforceConnectionTester.TestConnectionResult(
        isSuccess = false,
        isCancelled = false,
        info = null,
        errorMessage = PerforceBundle.message("checkout.validation.directory.not.exist", directory)
      )
    }

    val settings = createTestConnectionSettings(server, user, client)

    val parameters = P4ConnectionParameters().apply {
      setServer(server)
      setUser(user)
      setClient(client)
      setPassword(password)
    }
    val connection = TestParametersConnection(parameters, ConnectionId())

    val tester = PerforceConnectionTester(project, settings)
    return tester.testConnection(connection = connection, clientRoot = clientRoot)
  }

  private fun createTestConnectionSettings(server: String, user: String, client: String): PerforceSettings {
    return PerforceSettings(project).apply {
      setCanGoOffline(false)
      useP4CONFIG = false
      USE_LOGIN = true
      port = server
      this.user = user
      this.client = client
    }
  }

}
