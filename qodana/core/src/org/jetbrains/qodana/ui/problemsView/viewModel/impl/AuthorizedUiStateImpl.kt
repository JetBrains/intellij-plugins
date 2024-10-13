package org.jetbrains.qodana.ui.problemsView.viewModel.impl

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.util.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.name
import org.jetbrains.qodana.cloud.frontendUrl
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel
import org.jetbrains.qodana.ui.run.wizard.RunQodanaWizard

class AuthorizedUiStateImpl(
  private val project: Project,
  private val viewModelScope: CoroutineScope,
  private val authorized: UserState.Authorized,
) : QodanaProblemsViewModel.AuthorizedState {
  override val qodanaCloudUrl: Url
    get() = authorized.frontendUrl

  override val userName: String
    get() = authorized.userDataProvider.userInfo.value.lastLoadedValue?.asSuccess()?.name ?: ""

  override fun showRunDialog() {
    viewModelScope.launch(QodanaDispatchers.Ui) {
      RunQodanaWizard.create(project).show()
    }
  }

  override fun logOut() {
    authorized.logOut()
  }

  override fun openDocumentation() {
    BrowserUtil.browse(QodanaBundle.message("qodana.documentation.website.url"))
  }
}