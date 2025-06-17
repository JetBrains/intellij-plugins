package org.jetbrains.qodana.jvm.java.ui

import com.intellij.ui.EditorNotificationPanel
import org.jetbrains.qodana.QodanaBundle


class GithubPromoNotificationBanner(
  viewModel: GithubPromoBannerViewModelImpl
): EditorNotificationPanel(Status.Info) {

  init {
    text = QodanaBundle.message("qodana.github.promo.notification.text")

    createActionLabel(QodanaBundle.message("qodana.github.promo.notification.explore.button.text")) {
      viewModel.openLandingPage()
    }

    createActionLabel(QodanaBundle.message("qodana.github.promo.notification.add.workflow.button.text")) {
      viewModel.addQodanaWorkflow()
    }

    createActionLabel(QodanaBundle.message("qodana.github.promo.notification.dismiss.button.text")) {
      viewModel.dismissPromo()
    }
  }
}