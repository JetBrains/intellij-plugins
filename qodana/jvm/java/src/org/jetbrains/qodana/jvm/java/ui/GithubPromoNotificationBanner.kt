package org.jetbrains.qodana.jvm.java.ui

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.observable.util.whenDisposed
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.platform.util.coroutines.childScope
import com.intellij.ui.EditorNotificationPanel
import kotlinx.coroutines.cancel
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.qodanaProjectScope


class GithubPromoNotificationBanner(
  project: Project,
  fileEditor: FileEditor,
  fileEditorViewModel: GithubPromoEditorViewModel
): EditorNotificationPanel(Status.Info) {

  init {
    val disposable = Disposer.newDisposable("GithubPromoNotificationBanner")
    val isDisposableRegistered = Disposer.tryRegister(fileEditor, disposable)
    if (!isDisposableRegistered) {
      Disposer.dispose(disposable)
    }
    else {
      val scope = project.qodanaProjectScope.childScope("GithubPromoNotificationBanner")
      disposable.whenDisposed {
        scope.cancel()
      }
      val viewModel = GithubPromoBannerViewModelImpl(project, fileEditorViewModel, scope)
      init(viewModel)
    }
  }

  fun init(viewModel: GithubPromoEditorViewModel.GithubPromoBannerViewModel) {
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