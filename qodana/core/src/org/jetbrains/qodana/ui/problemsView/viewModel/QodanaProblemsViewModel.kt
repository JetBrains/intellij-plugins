package org.jetbrains.qodana.ui.problemsView.viewModel

import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.pom.Navigatable
import com.intellij.util.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.InspectionInfoProvider
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.report.BrowserViewProvider
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.stats.SetupCiDialogSource
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.showSetupCIDialogOrWizardWithYaml
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeNode
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreePath
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeRoot
import java.awt.Component
import java.awt.event.InputEvent

interface QodanaProblemsViewModel {
  companion object {
    val DATA_KEY: DataKey<QodanaProblemsViewModel> = DataKey.create("QodanaProblemsViewModel")
  }

  sealed interface UiState {

    interface Loaded : UiState {
      val treeRootStateFlow: StateFlow<QodanaTreeRoot>

      val uiTreeEventsFlow: Flow<UiTreeEvent>

      val noProblemsContentFlow: Flow<NoProblemsContent>

      val selectedTreeNodeFlow: Flow<QodanaTreeNode<*, *, *>?>

      val inspectionInfoProvider: InspectionInfoProvider

      val jobUrl: String?

      fun treeNodeSelectionChanged(node: QodanaTreeNode<*, *, *>?)

      fun exclude(configExcludeItem: ConfigExcludeItem)

      fun refreshReport()

      fun closeReport()
    }

    interface LoadingReport : UiState {
      fun cancel()

      fun refreshReport()

      fun closeReport()
    }

    interface RunningQodana : UiState {
      fun cancel()
    }

    interface Authorizing : UiState {
      fun cancel()

      fun checkLicenseStatus()
    }

    interface NotAuthorized : UiState {
      val ciState: CiState

      fun authorize()

      fun showRunDialog()
    }

    interface NotLinked : UiState {
      val authorized: AuthorizedState

      val ciState: CiState

      fun showLinkDialog()
    }

    interface Linked : UiState {
      val authorized: AuthorizedState

      val cloudProjectName: String

      val cloudProjectUrl: Url

      val availableReportUrl: Url?

      val ciState: CiState

      fun openReport()

      fun unlink()
    }
  }

  interface AuthorizedState {
    val qodanaCloudUrl: Url

    val userName: String

    fun showRunDialog()

    fun logOut()

    fun openDocumentation()
  }

  sealed interface CiState {
    class Present(val ciFile: CIFile.ExistingWithQodana) : CiState

    class NotPresent(private val project: Project) : CiState {
      fun showSetupCIDialog() {
        project.qodanaProjectScope.launch(QodanaDispatchers.Ui) {
          showSetupCIDialogOrWizardWithYaml(project, SetupCiDialogSource.PROBLEMS_VIEW_AUTHORIZED_LINKED)
        }
      }
    }
  }

  sealed interface UiTreeEvent {
    class Update(val parentNodePath: QodanaTreePath) : UiTreeEvent

    class Navigate(val nodePath: QodanaTreePath, val navigatable: Navigatable) : UiTreeEvent
  }

  val showPreviewFlow: StateFlow<Boolean>

  val uiStateFlow: StateFlow<UiState>

  val problemsViewState: StateFlow<QodanaProblemsViewState>

  val browserViewProviderStateFlow: StateFlow<BrowserViewProvider?>

  val bannersContentProvidersFlow: Flow<List<BannerContentProvider>>

  val descriptionPanelContentProviderFlow: Flow<SecondPanelContent?>

  fun tabSelectionChanged(isSelected: Boolean)

  fun updateProblemsViewState(update: (QodanaProblemsViewState) -> QodanaProblemsViewState)

  fun updateShowPreviewFlow(newValue: Boolean)

  class NoProblemsContent(
    @NlsContexts.StatusText val title: String,
    @NlsContexts.StatusText val description: String?,
    val actions: Pair<ActionDescriptor, ActionDescriptor?>?
  ) {
    class ActionDescriptor(
      @NlsContexts.StatusText val text: String,
      val action: (component: Component, inputEvent: InputEvent?) -> Unit
    )
  }

  sealed interface SecondPanelContent

  data class EditorPanelContent(
    val modelTreeNode: QodanaTreeNode<*, *, *>
  ) : SecondPanelContent

  data class DescriptionPanelContent(
    @NlsContexts.Label val inspectionId: String,
    val info: String
  ) : SecondPanelContent {
    companion object {
      fun createFromId(inspectionId: String, inspectionInfoProvider: InspectionInfoProvider): DescriptionPanelContent? {
        val inspectionName = inspectionInfoProvider.getName(inspectionId) ?: return null
        val info = inspectionInfoProvider.getDescription(inspectionId) ?: return null
        return DescriptionPanelContent(inspectionName, info)
      }
    }
  }
}