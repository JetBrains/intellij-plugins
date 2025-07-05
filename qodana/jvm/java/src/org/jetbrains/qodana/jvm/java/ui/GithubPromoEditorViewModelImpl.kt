package org.jetbrains.qodana.jvm.java.ui

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.observable.util.whenDisposed
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.projectRoots.JavaSdkType
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsContexts
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.util.coroutines.childScope
import com.intellij.ui.EditorNotifications
import com.intellij.vcsUtil.VcsFileUtil.addFilesToVcsWithConfirmation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.jvm.java.QodanaGithubPromoNotificationApplicationDismissalState
import org.jetbrains.qodana.jvm.java.QodanaGithubPromoNotificationProjectDismissalState
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.github.GitHubCIFileChecker

private const val JAVA_CHECK_DEBOUNCE = 1000L
internal val GITHUB_PROMO_EDITOR_VIEW_MODEL_KEY = Key.create<Lazy<GithubPromoEditorViewModelImpl>>("GithubPromoEditorViewModel")

class GithubPromoEditorViewModelImpl(
  private val project: Project,
  private val fileEditor: FileEditor
): GithubPromoEditorViewModel {

  private val scope: CoroutineScope = project.qodanaProjectScope.childScope("GithubPromoEditorViewModel")

  private val bannerViewModel: MutableStateFlow<GithubPromoBannerViewModelImpl?> = MutableStateFlow(null)
  @OptIn(FlowPreview::class)
  private val isProjectAJavaProjectFlow: Flow<Boolean> = flow {
    project.serviceAsync<WorkspaceModel>()
      .eventLog
      .debounce(JAVA_CHECK_DEBOUNCE)
      .collect { _ ->
        emit(isJavaProject())
      }
  }

  private val isNotificationEnabledFlow: Flow<Boolean> = project.service<QodanaGithubPromoNotificationProjectDismissalState>().dismissedState
      .combine(service<QodanaGithubPromoNotificationApplicationDismissalState>().dismissedState) { p, a -> !p && !a }

  private val isGithubWorkflowExistsAndWithoutQodana: Flow<Boolean> = GitHubCIFileChecker(project).ciFileFlow.map {
    it is CIFile.ExistingMultipleInstances
  }

  init {
    val disposable = Disposer.newDisposable("GithubPromoEditorViewModel")
    val isDisposableRegistered = Disposer.tryRegister(fileEditor, disposable)
    if (!isDisposableRegistered) {
      Disposer.dispose(disposable)
      scope.cancel()
    } else {
      disposable.whenDisposed {
        scope.cancel()
      }
      init()
    }
  }

  /**
   * Constructs the chain of boolean flows.
   * If any flow produces false, the result flow will produce false
   * If any flow collects the value different from the previous one,
   * the computation of flows for the previous value down the chain will be cancelled
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun constructControlFlow(
    conditionFlows: List<Flow<Boolean>>
  ): Flow<Boolean> {

    val controlFlow = conditionFlows.reduce { accumulatorFlow, nextBooleanFlow ->
      accumulatorFlow.distinctUntilChanged().flatMapLatest { check ->
        // if the previous flow produced true, compute the next one; otherwise, pass false until the end
        if (check) nextBooleanFlow else flowOf(false)
      }
    }

    return controlFlow.distinctUntilChanged()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun init() {
    scope.launch {
      val conditionFlows = listOf(
        isNotificationEnabledFlow,
        isProjectAJavaProjectFlow,
        isGithubWorkflowExistsAndWithoutQodana
      )
      constructControlFlow(conditionFlows).collect { check ->
        if (check) {
          bannerViewModel.value = GithubPromoBannerViewModelImpl(project, this@GithubPromoEditorViewModelImpl, scope)
        } else {
          bannerViewModel.value = null
        }
      }
    }
    scope.launch {
      bannerViewModel.collect {
        EditorNotifications.getInstance(project).updateNotifications(fileEditor.file)
      }
    }
  }

  override fun getNotificationBannerViewModel(): GithubPromoBannerViewModelImpl? = bannerViewModel.value

  override fun notifySuccessfulWorkflowAddition(files: GithubPromoEditorViewModel.CreatedFiles) {
    scope.launch {
      withContext(Dispatchers.EDT) {
        FileEditorManager.getInstance(project).openFile(files.workflowFile, true)
      }
      addFilesToVcsWithConfirmation(project, listOfNotNull(files.workflowFile, files.qodanaYamlFile))
    }
  }

  override fun notifyFailedWorkflowAddition(@NlsContexts.NotificationContent message: String) {
    scope.launch {
      val notification = QodanaNotifications.General.notification(
        QodanaBundle.message("qodana.github.promo.notification.bubble.qodana.github.workflow.not.added.title"),
        message,
        NotificationType.ERROR,
        withQodanaIcon = false
      )
      notification.notify(project)
    }
  }

  private fun isJavaProject(): Boolean {
    val isJavaSdk = { sdk: Sdk? -> sdk != null && sdk.sdkType is JavaSdkType }
    val projectSdk = ProjectRootManager.getInstance(project).projectSdk
    if (isJavaSdk(projectSdk)) {
      return true
    }
    val modules = project.modules
    for (module in modules) {
      val moduleSdk = ModuleRootManager.getInstance(module).sdk
      if (isJavaSdk(moduleSdk)) {
        return true
      }
    }
    return false
  }
}