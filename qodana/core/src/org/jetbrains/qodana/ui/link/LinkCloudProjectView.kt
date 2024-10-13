package org.jetbrains.qodana.ui.link

import com.intellij.collaboration.ui.CollaborationToolsUIUtil
import com.intellij.collaboration.ui.CollaborationToolsUIUtil.isDefault
import com.intellij.icons.AllIcons
import com.intellij.ide.ui.laf.darcula.ui.DarculaButtonPainter
import com.intellij.navigation.extractVcsOriginCanonicalPath
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.FixedSizeButton
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SearchTextField
import com.intellij.ui.SingleSelectionModel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.scroll.BoundedRangeModelThresholdListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.frontendUrl
import org.jetbrains.qodana.cloud.openBrowserWithCurrentQodanaCloudFrontend
import org.jetbrains.qodana.cloud.project.*
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.extensions.RepositoryInfoProvider
import org.jetbrains.qodana.settings.qodanaSettings
import org.jetbrains.qodana.stats.CreateProjectSource
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceLinkState
import org.jetbrains.qodana.ui.problemsView.LINE_INSETS
import org.jetbrains.qodana.ui.problemsView.getSimpleHtmlPane
import org.jetbrains.qodana.ui.setContentAndRepaint
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.event.ChangeEvent

private const val SCROLL_THRESHOLD_FRACTION = 0.7f

class LinkCloudProjectView(
  private val scope: CoroutineScope,
  private val project: Project,
  val viewModel: LinkCloudProjectViewModel,
  private val afterProjectCreation: () -> Unit = {}
) {
  private val viewOfCurrentState = Wrapper()

  private val _emptyStateFlow: MutableStateFlow<Flow<Boolean>> = MutableStateFlow(emptyFlow())
  val emptyStateFlow = _emptyStateFlow.asStateFlow()

  init {
    scope.launch(QodanaDispatchers.Ui) {
      viewModel.userAndLinkStatesFlow.collectLatest { (userState, linkState) ->
        viewModel.setSelectedProject(null)
        _emptyStateFlow.value = emptyFlow()
        coroutineScope {
          val view = getViewForCurrentStates(this, userState, linkState)
          viewOfCurrentState.setContentAndRepaint(view)
          awaitCancellation()
        }
      }
    }
  }

  private fun getViewForCurrentStates(scope: CoroutineScope, userState: UserState, linkState: LinkState): JComponent? {
    if (userState !is UserState.Authorized) return null
    return when(linkState) {
      is LinkState.Linked -> createLinkedView(scope, linkState)
      is LinkState.NotLinked -> createSelectCloudProjectView(scope, userState)
    }
  }

  private fun createSelectCloudProjectView(scope: CoroutineScope, authorized: UserState.Authorized): JComponent {
    val view = SelectCloudProjectView(scope, project, authorized, ::filterOrganizations, ::onProjectCreated, viewModel::setSelectedProject)
    _emptyStateFlow.value = view.emptyStateFlow
    return view.getView()
  }

  private fun createLinkedView(scope: CoroutineScope, linked: LinkState.Linked): JComponent {
    return panel {
      row {
        val projectId = linked.projectDataProvider.projectPrimaryData.id
        val linkedToLabel = label(QodanaBundle.message("qodana.link.project.dialog.linked", projectId))
          .comment(QodanaBundle.message("qodana.settings.pane.link.description"))

        scope.launch(QodanaDispatchers.Ui, start = CoroutineStart.UNDISPATCHED) {
          linked.projectDataProvider.projectProperties
            .map { it.lastLoadedValue?.asSuccess()?.name ?: projectId }
            .collect { projectName ->
              linkedToLabel.component.text = QodanaBundle.message("qodana.link.project.dialog.linked", projectName)
            }
        }
      }
      row {
        button(QodanaBundle.message("qodana.link.project.dialog.unlink.button")) {
          linked.unlink()
          logUnlinkStats()
        }.apply { component.putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY) }
      }
      row {
        val checkbox = checkBox(QodanaBundle.message("qodana.settings.panel.auto.load.linked.report"))
          .comment(QodanaBundle.message("qodana.settings.panel.auto.load.linked.report.description"))

        checkbox.actionListener { _, component ->
          viewModel.setAutoLoadReportEnabled(component.isSelected)
        }
        scope.launch(QodanaDispatchers.Ui, start = CoroutineStart.UNDISPATCHED) {
          viewModel.isAutoLoadReportEnabled.collect {
            checkbox.component.isSelected = it
          }
        }
      }
    }
  }

  private fun logUnlinkStats() {
    QodanaPluginStatsCounterCollector.UPDATE_CLOUD_LINK.log(
      project,
      false,
      SourceLinkState.LINK_VIEW
    )
  }

  // Organizations filter may be will be used later
  private fun filterOrganizations(qodanaCloudOrganizationResponse: QDCloudSchema.Organization): Boolean = true

  private fun onProjectCreated(cloudProjectData: CloudProjectData) {
    viewModel.linkWithCloudProject(scope, cloudProjectData)
    afterProjectCreation.invoke()
  }

  fun getView(): JPanel = viewOfCurrentState
}

class LinkCloudProjectViewModel(private val project: Project, scope: CoroutineScope) {
  val userAndLinkStatesFlow: Flow<Pair<UserState, LinkState>> = createUserAndLinkStatesFlow()

  private val _selectedProject = MutableStateFlow<CloudProjectData?>(null)
  val selectedProject = _selectedProject.asStateFlow()

  private val _isAutoLoadReportEnabled = MutableStateFlow(project.qodanaSettings().loadMatchingCloudReportAutomatically.value)
  val isAutoLoadReportEnabled: StateFlow<Boolean> = _isAutoLoadReportEnabled.asStateFlow()

  init {
    scope.launch(QodanaDispatchers.Default) {
      project.qodanaSettings().loadMatchingCloudReportAutomatically.collect {
        _isAutoLoadReportEnabled.value = it
      }
    }
  }

  fun setAutoLoadReportEnabled(isEnabled: Boolean) {
    _isAutoLoadReportEnabled.value = isEnabled
  }

  fun isModified(): Boolean {
    return selectedProject.value != null ||
           project.qodanaSettings().loadMatchingCloudReportAutomatically.value != isAutoLoadReportEnabled.value
  }

  fun setSelectedProject(selectedProject: CloudProjectData?) {
    _selectedProject.value = selectedProject
  }

  fun finishAndLinkWithSelectedCloudProject() {
    project.qodanaSettings().setLoadMatchingCloudReportAutomatically(isAutoLoadReportEnabled.value)
    val selectedQodanaCloudProject = selectedProject.value ?: return
    linkWithCloudProject(project.qodanaProjectScope, selectedQodanaCloudProject)
  }

  fun linkWithCloudProject(scope: CoroutineScope, cloudProjectData: CloudProjectData) {
    scope.launch(QodanaDispatchers.Default) {
      linkWithCloudProjectAndApply(
        project,
        cloudProjectData,
        SourceLinkState.LINK_VIEW
      )
    }
  }

  private fun createUserAndLinkStatesFlow(): Flow<Pair<UserState, LinkState>> {
    val userState = QodanaCloudStateService.getInstance().userState
    val linkState = QodanaCloudProjectLinkService.getInstance(project).linkState
    return combine(userState, linkState, ::Pair)
  }
}

private class SelectCloudProjectView(
  private val scope: CoroutineScope,
  private val project: Project,
  private val authorized: UserState.Authorized,
  private val organizationsFilter: (QDCloudSchema.Organization) -> Boolean,
  private val onProjectCreated: (CloudProjectData) -> Unit,
  private val onProjectSelected: (CloudProjectData?) -> Unit
) {
  private val _emptyStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val emptyStateFlow = _emptyStateFlow.asStateFlow()

  private val filterAppliedFlow = MutableStateFlow(false)

  private val projectPagedLoader = QodanaCloudProjectsPagedLoader(project, scope, authorized, filterAppliedFlow)

  private val searchTextField = SearchTextField(false).apply {
    border = object : DarculaButtonPainter() {
      override fun getBorderInsets(c: Component): Insets {
        return JBUI.emptyInsets()
      }
    }
  }

  private val emptyPanel: JPanel = createEmptyPanel()

  private fun createEmptyPanel(): JPanel {
    val panel = JPanel(GridBagLayout())
    val gc = GridBagConstraints()
    val htmlPane = getSimpleHtmlPane(QodanaBundle.message("qodana.link.project.dialog.no.projects.found", authorized.frontendUrl))
    gc.insets = JBUI.insetsBottom(LINE_INSETS)
    gc.gridx = 0
    gc.gridy = 0
    gc.anchor = GridBagConstraints.CENTER
    panel.add(htmlPane, gc)

    val createButton = JButton(QodanaBundle.message("qodana.link.project.dialog.create.project")).apply {
      isDefault = true
      addActionListener {
        openBrowserWithCurrentQodanaCloudFrontend()
        QodanaPluginStatsCounterCollector.CREATE_PROJECT_PRESSED.log(CreateProjectSource.NO_PROJECTS_VIEW)
      }
    }
    gc.gridy = 1
    panel.add(createButton, gc)

    val mainPanel = JPanel(GridBagLayout())
    gc.weighty = 0.2
    gc.gridy = 0
    mainPanel.add(panel, gc)

    val filler = JPanel().apply { isOpaque = false }
    gc.weighty = 0.8
    gc.gridy = 1
    mainPanel.add(filler, gc)

    return mainPanel
  }

  private val listModel = CollectionListModel<QodanaCloudProjectsPagedLoader.ProjectData>()

  private val list = JBList(listModel).apply {
    cellRenderer = QodanaCloudProjectRenderer()
    isFocusable = true
    selectionModel = SingleSelectionModel()
    addListSelectionListener {
      if (it.valueIsAdjusting) return@addListSelectionListener

      val selectedCloudProjectData = selectedValue?.let { projectData ->
        CloudProjectData(
          CloudProjectPrimaryData(projectData.id, CloudOrganizationPrimaryData(projectData.team.organizationId)),
          CloudProjectProperties(projectData.name)
        )
      }
      onProjectSelected.invoke(selectedCloudProjectData)
    }
    setExpandableItemsEnabled(false)
  }

  private val mainPanelWrapper = Wrapper()
  private val scroll: JScrollPane = createScrollPane()
  private val scrollThresholdListener: BoundedRangeModelThresholdListener = bindScrollToPagedLoader()

  init {
    CollaborationToolsUIUtil.attachSearch(list, searchTextField) {
      listOfNotNull(it.name, it.team.name, extractVcsOriginCanonicalPath(it.vcsUrl)).joinToString("\n")
    }
    mainPanelWrapper.setContent(scroll)
    scope.launch(QodanaDispatchers.Ui) {
      launch {
        projectPagedLoader.latestPageResponseFlow.collect { projectPageResponse ->
          mainPanelWrapper.setContent(scroll)
          _emptyStateFlow.value = false
          when (projectPageResponse) {
            is QDCloudResponse.Success -> {
              if (projectPageResponse.value.isEmpty() && listModel.isEmpty) {
                showEmptyView()
                _emptyStateFlow.value = true
                return@collect
              }
              addProjectsPage(projectPageResponse.value)
              notifyScrollThresholdListenerLater()
            }
            is QDCloudResponse.Error -> {
              showError(projectPageResponse)
            }
            null -> {
              listModel.removeAll()
              projectPagedLoader.loadMore()
            }
          }
        }
      }
      launch {
        projectPagedLoader.isLoadingStateFlow.collect { isLoading ->
          list.setPaintBusy(isLoading)
          list.emptyText.apply {
            clear()
            if (isLoading) {
              appendLine(QodanaBundle.message("qodana.link.project.dialog.loading"))
            }
          }
        }
      }
    }
  }

  private fun createScrollPane(): JScrollPane {
    return ScrollPaneFactory.createScrollPane(list).apply {
      horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
      verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED

      isOpaque = false
      viewport.isOpaque = false
    }
  }

  private fun bindScrollToPagedLoader(): BoundedRangeModelThresholdListener {
    val model = scroll.verticalScrollBar.model
    val thresholdListener = object : BoundedRangeModelThresholdListener(model, SCROLL_THRESHOLD_FRACTION) {
      override fun onThresholdReached() = projectPagedLoader.loadMore()
    }

    model.addChangeListener(thresholdListener)
    return thresholdListener
  }

  private fun addProjectsPage(projects: List<QodanaCloudProjectsPagedLoader.ProjectData>) {
    val selection = list.selectedIndex
    listModel.add(projects)
    list.selectedIndex = selection
  }

  private fun notifyScrollThresholdListenerLater() {
    SwingUtilities.invokeLater {
      scrollThresholdListener.stateChanged(ChangeEvent(this))
    }
  }

  private fun showEmptyView() {
    listModel.removeAll()
    mainPanelWrapper.setContent(emptyPanel)
  }

  private fun showError(error: QDCloudResponse.Error) {
    listModel.removeAll()
    list.emptyText.apply {
      clear()
      appendLine(getErrorMessage(error))
    }
  }

  fun getView() = panel {
    row {
      label(QodanaBundle.message("qodana.settings.panel.link.description"))
        .comment(QodanaBundle.message("qodana.settings.pane.link.description"))
    }
    row {
      cell(searchTextField.textEditor)
        .resizableColumn()
        .align(AlignX.FILL)
        .gap(RightGap.SMALL)
      cell(FixedSizeButton())
        .applyToComponent {
          text = null
          icon = AllIcons.Actions.Refresh
          addActionListener {
            projectPagedLoader.reset()
          }
        }.gap(RightGap.SMALL)
      if (RepositoryInfoProvider.getProjectOriginUrl(project) != null) {
        checkBox(QodanaBundle.message("qodana.settings.panel.link.only.related.projects")).apply {
          onChanged {
            filterAppliedFlow.value = it.isSelected
          }
        }
      }
      button(QodanaBundle.message("qodana.link.project.dialog.new.project.button")) {
        CreateProjectDialog(project, organizationsFilter, onProjectCreated).show()
      }.applyToComponent {
        // In the future, we may want to support creation of projects in IDE but not now
        isEnabled = false
        isVisible = false
      }
    }
    row {
      cell(mainPanelWrapper)
        .resizableColumn()
        .align(Align.FILL)
    }.resizableRow()
  }.apply { putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY) }

  private fun getErrorMessage(error: QDCloudResponse.Error): @NlsSafe String {
    return when(error) {
      is QDCloudResponse.Error.Offline ->
        QodanaBundle.message("qodana.cloud.offline")
      is QDCloudResponse.Error.ResponseFailure ->
        QodanaBundle.message("qodana.link.project.dialog.error", error.errorMessage)
    }
  }
}