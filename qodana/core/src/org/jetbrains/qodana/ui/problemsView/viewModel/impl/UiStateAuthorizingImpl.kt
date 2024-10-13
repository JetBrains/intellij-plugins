package org.jetbrains.qodana.ui.problemsView.viewModel.impl

import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel

class UiStateAuthorizingImpl(private val authorizing: UserState.Authorizing) : QodanaProblemsViewModel.UiState.Authorizing {
  override fun cancel() {
    authorizing.cancelAuthorization()
  }

  override fun checkLicenseStatus() {
    authorizing.checkLicenseStatus()
  }
}