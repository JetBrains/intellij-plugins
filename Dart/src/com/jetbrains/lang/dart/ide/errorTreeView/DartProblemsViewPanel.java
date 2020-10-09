// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.CopyProvider;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbAwareToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.table.TableView;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.EditSourceOnEnterKeyHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DartProblemsViewPanel extends SimpleToolWindowPanel implements DataProvider, CopyProvider {
  private static final LayeredIcon DART_ERRORS_ICON;
  private static final LayeredIcon DART_WARNINGS_ICON;

  static {
    DART_ERRORS_ICON = new LayeredIcon(2);
    DART_ERRORS_ICON.setIcon(DartIcons.Dart_13, 0);
    DART_ERRORS_ICON.setIcon(AllIcons.Ide.ErrorPoint, 1, SwingConstants.SOUTH_WEST);

    DART_WARNINGS_ICON = new LayeredIcon(2);
    DART_WARNINGS_ICON.setIcon(DartIcons.Dart_13, 0);
    DART_WARNINGS_ICON.setIcon(DartIcons.Warning_point, 1, SwingConstants.SOUTH_WEST);
  }

  private final @NotNull Project myProject;
  private final @NotNull TableView<DartProblem> myTable;

  private final @NotNull DartProblemsPresentationHelper myPresentationHelper;

  DartProblemsViewPanel(@NotNull Project project,
                        @NotNull DartProblemsPresentationHelper presentationHelper) {
    super(false, true);
    myProject = project;
    myPresentationHelper = presentationHelper;

    myTable = createTable();
    setToolbar(createToolbar());
    setContent(createCenterPanel());
  }

  private @NotNull TableView<DartProblem> createTable() {
    TableView<DartProblem> table = new TableView<>(new DartProblemsTableModel(myProject, myPresentationHelper));

    EditSourceOnDoubleClickHandler.install(table);
    EditSourceOnEnterKeyHandler.install(table);

    table.addMouseListener(new PopupHandler() {
      @Override
      public void invokePopup(Component comp, int x, int y) {
        popupInvoked(comp, x, y);
      }
    });

    //noinspection unchecked
    ((DefaultRowSorter<DartProblemsTableModel, Integer>)table.getRowSorter()).setRowFilter(myPresentationHelper.getRowFilter());

    table.getRowSorter().addRowSorterListener(e -> {
      List<? extends RowSorter.SortKey> sortKeys = myTable.getRowSorter().getSortKeys();
      assert sortKeys.size() == 1 : sortKeys;
      ((DartProblemsTableModel)myTable.getModel()).setSortKey(sortKeys.get(0));
    });

    new TableSpeedSearch(table, object -> object instanceof DartProblem
                                          ? ((DartProblem)object).getErrorMessage() + " " + ((DartProblem)object).getPresentableLocation()
                                          : "");

    table.setShowGrid(false);
    table.setRowHeight(table.getRowHeight() + JBUIScale.scale(4));

    JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setPreferredSize(new Dimension(0, table.getRowHeight()));

    return table;
  }

  private void popupInvoked(Component component, int x, int y) {
    DefaultActionGroup group = new DefaultActionGroup();
    if (getData(CommonDataKeys.NAVIGATABLE.getName()) != null) {
      group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_COPY));

    List<DartProblem> selectedProblems = myTable.getSelectedObjects();
    DartProblem selectedProblem = selectedProblems.size() == 1 ? selectedProblems.get(0) : null;

    addQuickFixActions(group, selectedProblem);
    addDiagnosticMessageActions(group, selectedProblem);
    addDocumentationAction(group, selectedProblem);

    ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.TOOLBAR, group);
    menu.getComponent().show(component, x, y);
  }

  private void addQuickFixActions(@NotNull DefaultActionGroup group, @Nullable DartProblem problem) {
    VirtualFile selectedVFile = problem != null ? problem.getFile() : null;
    if (selectedVFile == null) return;

    List<SourceChange> selectedProblemSourceChangeFixes = new SmartList<>();
    DartAnalysisServerService.getInstance(myProject)
      .askForFixesAndWaitABitIfReceivedQuickly(selectedVFile, problem.getOffset(), fixes -> {
        for (AnalysisErrorFixes fix : fixes) {
          if (fix.getError().getCode().equals(problem.getCode())) {
            selectedProblemSourceChangeFixes.addAll(fix.getFixes());
          }
        }
      });

    if (selectedProblemSourceChangeFixes.isEmpty()) return;

    group.addSeparator();

    for (SourceChange sourceChangeFix : selectedProblemSourceChangeFixes) {
      if (sourceChangeFix == null) continue;
      @NlsSafe String fixMessage = sourceChangeFix.getMessage();
      group.add(new AnAction(fixMessage, null, AllIcons.Actions.QuickfixBulb) {
        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
          OpenSourceUtil.navigate(PsiNavigationSupport.getInstance().createNavigatable(myProject, selectedVFile, problem.getOffset()));
          try {
            WriteAction.run(() -> AssistUtils.applySourceChange(myProject, sourceChangeFix, true));
          }
          catch (DartSourceEditException ignored) {/**/}
        }
      });
    }
  }

  private void addDiagnosticMessageActions(@NotNull DefaultActionGroup group, @Nullable DartProblem problem) {
    List<DiagnosticMessage> diagnosticMessages = problem != null ? problem.getDiagnosticMessages() : null;
    if (diagnosticMessages == null || diagnosticMessages.isEmpty()) return;

    group.addSeparator();
    // Reference the icon for "Jump to Source", higher in this menu group, to indicate that the action will have the same behavior
    Icon jumpToSourceIcon = ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE).getTemplatePresentation().getIcon();
    for (DiagnosticMessage diagnosticMessage : diagnosticMessages) {
      // Reference the message, trim, non-nullize, and remove a trailing period, if one exists
      @NlsSafe String message = StringUtil.notNullize(diagnosticMessage.getMessage());
      message = StringUtil.trimEnd(StringUtil.trim(message), ".");

      // Reference the Location, compute the VirtualFile
      Location location = diagnosticMessage.getLocation();
      String filePath = location == null ? null : FileUtil.toSystemIndependentName(location.getFile());
      VirtualFile vFile = filePath == null ? null : LocalFileSystem.getInstance().findFileByPath(filePath);

      // Create the action for this DiagnosticMessage
      if (StringUtil.isNotEmpty(message) && vFile != null) {
        group.add(new DumbAwareAction(message, null, jumpToSourceIcon) {
          @Override
          public void actionPerformed(@NotNull AnActionEvent e) {
            int offset = DartAnalysisServerService.getInstance(myProject).getConvertedOffset(vFile, location.getOffset());
            OpenSourceUtil.navigate(PsiNavigationSupport.getInstance().createNavigatable(myProject, vFile, offset));
          }
        });
      }
    }
  }

  private static void addDocumentationAction(@NotNull DefaultActionGroup group, @Nullable DartProblem problem) {
    String url = problem != null ? problem.getUrl() : null;
    if (url == null) return;

    group.addSeparator();
    group.add(new DumbAwareAction(DartBundle.messagePointer("action.DartProblemsViewPanel.open.documentation.text"),
                                  DartBundle.messagePointer("action.DartProblemsViewPanel.open.documentation.description"),
                                  AllIcons.Ide.External_link_arrow) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        BrowserUtil.browse(url);
      }
    });
  }

  private @NotNull JComponent createToolbar() {
    DefaultActionGroup group = new DefaultActionGroup();

    addReanalyzeActions(group);
    group.addAction(new AnalysisServerSettingsAction());
    group.addSeparator();

    addAutoScrollToSourceAction(group);
    addGroupBySeverityAction(group);
    group.addAction(new FilterProblemsAction());
    group.addSeparator();

    return ActionManager.getInstance().createActionToolbar("DartProblemsView", group, false).getComponent();
  }

  private @NotNull JPanel createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(ScrollPaneFactory.createScrollPane(myTable), BorderLayout.CENTER);
    return panel;
  }

  private void updateStatusDescription() {
    DartProblemsTableModel model = (DartProblemsTableModel)myTable.getModel();
    DartProblemsView problemsView = DartProblemsView.getInstance(myProject);
    problemsView.setTabTitle(model.getTabTitleText());
    problemsView.setToolWindowIcon(model.hasErrors() ? DART_ERRORS_ICON : model.hasWarnings() ? DART_WARNINGS_ICON : DartIcons.Dart_13);
  }

  private static void addReanalyzeActions(@NotNull DefaultActionGroup group) {
    AnAction restartAction = ActionManager.getInstance().getAction("Dart.Restart.Analysis.Server");
    if (restartAction != null) {
      group.add(restartAction);
    }
  }

  private void addAutoScrollToSourceAction(@NotNull DefaultActionGroup group) {
    AutoScrollToSourceHandler autoScrollToSourceHandler = new AutoScrollToSourceHandler() {
      @Override
      protected boolean isAutoScrollMode() {
        return myPresentationHelper.isAutoScrollToSource();
      }

      @Override
      protected void setAutoScrollMode(boolean autoScrollToSource) {
        myPresentationHelper.setAutoScrollToSource(autoScrollToSource);
      }
    };

    autoScrollToSourceHandler.install(myTable);
    group.addAction(autoScrollToSourceHandler.createToggleAction());
  }

  private void addGroupBySeverityAction(@NotNull DefaultActionGroup group) {
    AnAction action = new DumbAwareToggleAction(DartBundle.message("group.by.severity"),
                                                DartBundle.message("group.by.severity.description"),
                                                AllIcons.Nodes.SortBySeverity) {
      @Override
      public boolean isSelected(@NotNull AnActionEvent e) {
        return myPresentationHelper.isGroupBySeverity();
      }

      @Override
      public void setSelected(@NotNull AnActionEvent e, boolean groupBySeverity) {
        myPresentationHelper.setGroupBySeverity(groupBySeverity);
        fireGroupingOrFilterChanged();
      }
    };

    group.addAction(action);
  }

  void fireGroupingOrFilterChanged() {
    myTable.getRowSorter().allRowsChanged();
    ((DartProblemsTableModel)myTable.getModel()).onFilterChanged();
    updateStatusDescription();
  }

  private void showFiltersPopup() {
    DartProblemsFilterForm filterForm = new DartProblemsFilterForm();
    filterForm.reset(myPresentationHelper);
    filterForm.addListener(new DartProblemsFilterForm.FilterListener() {
      @Override
      public void filtersChanged() {
        myPresentationHelper.updateFromFilterSettingsUI(filterForm);
        fireGroupingOrFilterChanged();
      }

      @Override
      public void filtersResetRequested() {
        myPresentationHelper.resetAllFilters();
        filterForm.reset(myPresentationHelper);
        fireGroupingOrFilterChanged();
      }
    });

    createAndShowPopup(DartBundle.message("dart.problems.view.popup.title.dart.problems.filter"), filterForm.getMainPanel());
  }

  private void showAnalysisServerSettingsPopup() {
    DartAnalysisServerSettingsForm serverSettingsForm = new DartAnalysisServerSettingsForm(myProject);
    serverSettingsForm.reset(myPresentationHelper);

    serverSettingsForm.addListener(() -> {
      myPresentationHelper.updateFromServerSettingsUI(serverSettingsForm);
      DartAnalysisServerService.getInstance(myProject).ensureAnalysisRootsUpToDate();
    });

    createAndShowPopup(DartBundle.message("dart.problems.view.popup.title.analysis.server.settings"), serverSettingsForm.getMainPanel());
  }

  private void createAndShowPopup(@NotNull @NlsContexts.PopupTitle String title, @NotNull JPanel jPanel) {
    Rectangle visibleRect = myTable.getVisibleRect();
    Point tableTopLeft = new Point(myTable.getLocationOnScreen().x + visibleRect.x, myTable.getLocationOnScreen().y + visibleRect.y);

    JBPopupFactory.getInstance()
      .createComponentPopupBuilder(jPanel, null)
      .setProject(myProject)
      .setTitle(title)
      .setMovable(true)
      .setRequestFocus(true)
      .createPopup().show(RelativePoint.fromScreen(tableTopLeft));
  }

  @Override
  public boolean isCopyVisible(@NotNull DataContext dataContext) {
    return true;
  }

  @Override
  public boolean isCopyEnabled(@NotNull DataContext dataContext) {
    return myTable.getSelectedObject() != null;
  }

  @Override
  public void performCopy(@NotNull DataContext dataContext) {
    List<DartProblem> selectedObjects = myTable.getSelectedObjects();
    String s = StringUtil.join(selectedObjects, problem -> StringUtil.toLowerCase(problem.getSeverity()) +
                                                           ": " +
                                                           problem.getErrorMessage() +
                                                           " (" +
                                                           problem.getCode() + " at " + problem.getPresentableLocation() +
                                                           ")", "\n");

    if (!s.isEmpty()) {
      CopyPasteManager.getInstance().setContents(new StringSelection(s));
    }
  }

  @Override
  public @Nullable Object getData(@NotNull @NonNls String dataId) {
    if (PlatformDataKeys.COPY_PROVIDER.is(dataId)) {
      return this;
    }
    if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
      return createNavigatable();
    }

    return null;
  }

  private @Nullable Navigatable createNavigatable() {
    DartProblem problem = myTable.getSelectedObject();
    if (problem != null) {
      VirtualFile file = LocalFileSystem.getInstance().findFileByPath(problem.getSystemIndependentPath());
      if (file != null) {
        OpenFileDescriptor navigatable = new OpenFileDescriptor(myProject, file, problem.getOffset());
        navigatable.setScrollType(ScrollType.MAKE_VISIBLE);
        return navigatable;
      }
    }

    return null;
  }

  public void setErrors(@NotNull Map<String, List<? extends AnalysisError>> filePathToErrors) {
    DartProblemsTableModel model = (DartProblemsTableModel)myTable.getModel();
    DartProblem oldSelectedProblem = myTable.getSelectedObject();

    DartProblem updatedSelectedProblem = model.setProblemsAndReturnReplacementForSelection(filePathToErrors, oldSelectedProblem);

    if (updatedSelectedProblem != null) {
      myTable.setSelection(Collections.singletonList(updatedSelectedProblem));
    }

    updateStatusDescription();
  }

  void clearAll() {
    ((DartProblemsTableModel)myTable.getModel()).removeAll();
    updateStatusDescription();
  }

  private class FilterProblemsAction extends DumbAwareAction implements Toggleable {
    FilterProblemsAction() {
      super(DartBundle.messagePointer("dart.problems.view.action.name.filter.problems"),
            DartBundle.messagePointer("dart.problems.view.action.description.filter.problems"),
            AllIcons.General.Filter);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      // show icon as toggled on if any filter is active
      Toggleable.setSelected(e.getPresentation(), myPresentationHelper.areFiltersApplied());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      showFiltersPopup();
    }
  }

  private class AnalysisServerSettingsAction extends DumbAwareAction implements Toggleable {
    AnalysisServerSettingsAction() {
      //noinspection DialogTitleCapitalization
      super(DartBundle.messagePointer("dart.problems.view.action.name.analysis.server.settings"),
            DartBundle.messagePointer("dart.problems.view.action.description.analysis.server.settings"),
            AllIcons.General.GearPlain);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      boolean asByDefault = myPresentationHelper.getScopedAnalysisMode() == DartProblemsViewSettings.SCOPED_ANALYSIS_MODE_DEFAULT;
      Toggleable.setSelected(e.getPresentation(), !asByDefault);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      showAnalysisServerSettingsPopup();
    }
  }
}
