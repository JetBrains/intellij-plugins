package org.jetbrains.idea.perforce.application

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.RememberCheckBoxState
import com.intellij.credentialStore.getTrimmedChars
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.ui.ColorUtil
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.NamedColorUtil
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.perforce.login.AttemptsStateMachine
import org.jetbrains.idea.perforce.perforce.login.LoginState
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JEditorPane

fun askUpdatePassword(project: Project, attemptsMachine: AttemptsStateMachine, attributes: CredentialAttributes): Boolean {
  return invokeAndWaitIfNeeded {
    val dialog = object : DialogWrapper(project) {
      lateinit var myOldPasswordField : JBPasswordField
      lateinit var myNewPasswordField : JBPasswordField
      lateinit var myRepeatNewPasswordField : JBPasswordField
      lateinit var myRememberCheckbox : JCheckBox
      lateinit var myErrorLabel : JEditorPane

      init {
        title = PerforceBundle.message("login.password.update.title")
        init()
      }

      override fun createCenterPanel(): JComponent {
        myRememberCheckbox = RememberCheckBoxState.createCheckBox(toolTip = PerforceBundle.message("login.password.remember.tooltip"))

        return panel {
          row(PerforceBundle.message("login.password.old.password")) {
            myOldPasswordField = passwordField().resizableColumn().align(AlignX.FILL)
              .onChanged { initValidation() }.component
          }
          row(PerforceBundle.message("login.password.new.password")) {
            myNewPasswordField = passwordField().resizableColumn().align(AlignX.FILL)
              .onChanged { initValidation() }.component
          }
          row(PerforceBundle.message("login.password.repeat.new.password")) {
            myRepeatNewPasswordField = passwordField().resizableColumn().align(AlignX.FILL)
              .onChanged { initValidation() }.component
          }
          row { cell(myRememberCheckbox) }
          row { myErrorLabel = comment("").component }
        }
      }

      override fun doValidate(): ValidationInfo? {
        if (!myNewPasswordField.password.contentEquals(myRepeatNewPasswordField.password)) {
          return ValidationInfo(PerforceBundle.message("login.password.passwords.not.match.error"), myRepeatNewPasswordField)
        }
        if (myOldPasswordField.password.contentEquals(myNewPasswordField.password)) {
          return ValidationInfo(PerforceBundle.message("login.password.password.not.changed.error"), myOldPasswordField)
        }

        return super.doValidate()
      }

      override fun getPreferredFocusedComponent(): JComponent {
        return myOldPasswordField
      }

      override fun doOKAction() {
        val passwords = getPasswords()
        val state = changePassUnderProgress(project, attemptsMachine, passwords.first, passwords.second)
        if (!state.isSuccess) {
          val builder = HtmlBuilder()
          val color = NamedColorUtil.getErrorForeground()
          builder
            .append(
              HtmlChunk.raw(PerforceBundle.message("login.perforce.error", state.error))
                .wrapWith("left")
                .wrapWith(HtmlChunk.font(ColorUtil.toHex(color)))
            )
          myErrorLabel.text = builder.toString()
          myErrorLabel.isVisible = true
          return
        }

        RememberCheckBoxState.update(myRememberCheckbox)

        val store = PasswordSafe.instance
        val credentials = Credentials(attributes.userName, myNewPasswordField.getTrimmedChars())
        if (myRememberCheckbox.isSelected) {
          ProgressManager.getInstance().runProcessWithProgressSynchronously({ store.set(attributes, credentials) }, title, false, project)
        }

        super.doOKAction()
      }

      private fun getPasswords(): Pair<String, String> {
        return Pair(String(myOldPasswordField.password), String(myNewPasswordField.password))
      }
    }

    return@invokeAndWaitIfNeeded dialog.showAndGet()
  }
}

private fun changePassUnderProgress(project: Project, machine: AttemptsStateMachine, oldPass: String?, newPassword: String?): LoginState {
  return ProgressManager.getInstance().runProcessWithProgressSynchronously<LoginState, RuntimeException>(
    {
      ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true)
      try {
        return@runProcessWithProgressSynchronously Computable<LoginState> {
          machine.changePass(oldPass, newPassword)
        }.compute()
      }
      catch (e: ProcessCanceledException) {
        return@runProcessWithProgressSynchronously null
      }
    }, PerforceBundle.message("login.password.update.title"), true, project)
}