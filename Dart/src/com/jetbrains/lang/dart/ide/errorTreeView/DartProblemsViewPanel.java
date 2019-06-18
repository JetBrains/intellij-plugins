// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.ui.table.TableView;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorFixes;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

  @NotNull private final Project myProject;
  @NotNull private final TableView<DartProblem> myTable;

  @NotNull private final DartProblemsPresentationHelper myPresentationHelper;

  private DartProblemsView.ToolWindowUpdater myToolWindowUpdater;

  public DartProblemsViewPanel(@NotNull final Project project,
                               @NotNull final DartProblemsPresentationHelper presentationHelper) {
    super(false, true);
    myProject = project;
    myPresentationHelper = presentationHelper;

    myTable = createTable();
    setToolbar(createToolbar());
    setContent(createCenterPanel());
  }

  @NotNull
  private TableView<DartProblem> createTable() {
    final TableView<DartProblem> table = new TableView<>(new DartProblemsTableModel(myProject, myPresentationHelper));

    table.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          navigate(false); // as in NewErrorTreeViewPanel
        }
      }
    });

    EditSourceOnDoubleClickHandler.install(table);

    table.addMouseListener(new PopupHandler() {
      @Override
      public void invokePopup(Component comp, int x, int y) {
        popupInvoked(comp, x, y);
      }
    });

    //noinspection unchecked
    ((DefaultRowSorter)table.getRowSorter()).setRowFilter(myPresentationHelper.getRowFilter());

    table.getRowSorter().addRowSorterListener(e -> {
      final List<? extends RowSorter.SortKey> sortKeys = myTable.getRowSorter().getSortKeys();
      assert sortKeys.size() == 1 : sortKeys;
      ((DartProblemsTableModel)myTable.getModel()).setSortKey(sortKeys.get(0));
    });

    new TableSpeedSearch(table, object -> object instanceof DartProblem
                                          ? ((DartProblem)object).getErrorMessage() + " " + ((DartProblem)object).getPresentableLocation()
                                          : "");

    table.setShowVerticalLines(false);
    table.setShowHorizontalLines(false);
    table.setStriped(true);
    table.setRowHeight(table.getRowHeight() + JBUIScale.scale(4));

    JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setPreferredSize(new Dimension(0, table.getRowHeight()));

    return table;
  }

  private void popupInvoked(final Component component, final int x, final int y) {
    final DefaultActionGroup group = new DefaultActionGroup();
    if (getData(CommonDataKeys.NAVIGATABLE.getName()) != null) {
      group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_COPY));

    final List<DartProblem> selectedProblems = myTable.getSelectedObjects();
    final DartProblem selectedProblem = selectedProblems.size() == 1 ? selectedProblems.get(0) : null;

    addQuickFixActions(group, selectedProblem);
    addDocumentationAction(group, selectedProblem);

    final ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.TOOLBAR, group);
    menu.getComponent().show(component, x, y);
  }

  private void addQuickFixActions(@NotNull final DefaultActionGroup group, @Nullable DartProblem problem) {
    final VirtualFile selectedVFile = problem != null ? problem.getFile() : null;
    if (selectedVFile == null) return;

    final List<SourceChange> selectedProblemSourceChangeFixes = new SmartList<>();
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

    for (final SourceChange sourceChangeFix : selectedProblemSourceChangeFixes) {
      if (sourceChangeFix == null) continue;
      group.add(new AnAction(sourceChangeFix.getMessage(), null, AllIcons.Actions.QuickfixBulb) {
        @Override
        public void actionPerformed(@NotNull final AnActionEvent event) {
          OpenSourceUtil.navigate(PsiNavigationSupport.getInstance().createNavigatable(myProject, selectedVFile, problem.getOffset()));
          try {
            WriteAction.run(() -> AssistUtils.applySourceChange(myProject, sourceChangeFix, true));
          }
          catch (DartSourceEditException ignored) {/**/}
        }
      });
    }
  }

  private static void addDocumentationAction(@NotNull final DefaultActionGroup group, @Nullable DartProblem problem) {
    final String url = problem != null ? problem.getUrl() : null;
    if (url == null) return;

    group.addSeparator();
    group.add(new DumbAwareAction("Open Documentation", "Open detailed problem description in browser", AllIcons.Ide.External_link_arrow) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        BrowserUtil.browse(url);
      }
    });
  }

  @NotNull
  private JComponent createToolbar() {
    final DefaultActionGroup group = new DefaultActionGroup();

    addReanalyzeActions(group);
    group.addAction(new AnalysisServerSettingsAction());
    group.addSeparator();

    addAutoScrollToSourceAction(group);
    addGroupBySeverityAction(group);
    group.addAction(new FilterProblemsAction());
    group.addSeparator();

    return ActionManager.getInstance().createActionToolbar("DartProblemsView", group, false).getComponent();
  }

  @NotNull
  private JPanel createCenterPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(ScrollPaneFactory.createScrollPane(myTable), BorderLayout.CENTER);
    return panel;
  }

  private void updateStatusDescription() {
    if (myToolWindowUpdater != null) {
      final DartProblemsTableModel model = (DartProblemsTableModel)myTable.getModel();
      myToolWindowUpdater.setHeaderText(model.getStatusText());
      myToolWindowUpdater.setIcon(model.hasErrors() ? DART_ERRORS_ICON : model.hasWarnings() ? DART_WARNINGS_ICON : DartIcons.Dart_13);
    }
  }

  private static void addReanalyzeActions(@NotNull final DefaultActionGroup group) {
    final AnAction restartAction = ActionManager.getInstance().getAction("Dart.Restart.Analysis.Server");
    if (restartAction != null) {
      group.add(restartAction);
    }
  }

  private void addAutoScrollToSourceAction(@NotNull final DefaultActionGroup group) {
    final AutoScrollToSourceHandler autoScrollToSourceHandler = new AutoScrollToSourceHandler() {
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

  private void addGroupBySeverityAction(@NotNull final DefaultActionGroup group) {
    final AnAction action = new DumbAwareToggleAction(DartBundle.message("group.by.severity"),
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
    final DartProblemsFilterForm filterForm = new DartProblemsFilterForm();
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

    createAndShowPopup("Dart Problems Filter", filterForm.getMainPanel());
  }

  private void showAnalysisServerSettingsPopup() {
    final DartAnalysisServerSettingsForm serverSettingsForm = new DartAnalysisServerSettingsForm(myProject);
    serverSettingsForm.reset(myPresentationHelper);

    serverSettingsForm.addListener(new DartAnalysisServerSettingsForm.ServerSettingsListener() {
      @Override
      public void settingsChanged() {
        myPresentationHelper.updateFromServerSettingsUI(serverSettingsForm);
        DartAnalysisServerService.getInstance(myProject).ensureAnalysisRootsUpToDate();
      }
    });

    createAndShowPopup(DartBundle.message("analysis.server.settings.title"), serverSettingsForm.getMainPanel());
  }

  private void createAndShowPopup(@NotNull final String title, @NotNull final JPanel jPanel) {
    final Rectangle visibleRect = myTable.getVisibleRect();
    final Point tableTopLeft = new Point(myTable.getLocationOnScreen().x + visibleRect.x, myTable.getLocationOnScreen().y + visibleRect.y);

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
    final List<DartProblem> selectedObjects = myTable.getSelectedObjects();
    final String s = StringUtil.join(selectedObjects, problem -> StringUtil.toLowerCase(problem.getSeverity()) +
                                                                 ": " +
                                                                 problem.getErrorMessage() +
                                                                 " (" +
                                                                 problem.getCode() + " at " + problem.getPresentableLocation() +
                                                                 ")", "\n");

    if (!s.isEmpty()) {
      CopyPasteManager.getInstance().setContents(new StringSelection(s));
    }
  }

  @Nullable
  @Override
  public Object getData(@NotNull @NonNls String dataId) {
    if (PlatformDataKeys.COPY_PROVIDER.is(dataId)) {
      return this;
    }
    if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
      return createNavigatable();
    }

    return null;
  }

  @Nullable
  private Navigatable createNavigatable() {
    final DartProblem problem = myTable.getSelectedObject();
    if (problem != null) {
      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(problem.getSystemIndependentPath());
      if (file != null) {
        final OpenFileDescriptor navigatable = new OpenFileDescriptor(myProject, file, problem.getOffset());
        navigatable.setScrollType(ScrollType.MAKE_VISIBLE);
        return navigatable;
      }
    }

    return null;
  }

  private void navigate(@SuppressWarnings("SameParameterValue") final boolean requestFocus) {
    final Navigatable navigatable = createNavigatable();
    if (navigatable != null && navigatable.canNavigateToSource()) {
      navigatable.navigate(requestFocus);
    }
  }

  public void setErrors(@NotNull final Map<String, List<AnalysisError>> filePathToErrors) {
    final DartProblemsTableModel model = (DartProblemsTableModel)myTable.getModel();
    final DartProblem oldSelectedProblem = myTable.getSelectedObject();

    final DartProblem updatedSelectedProblem = model.setErrorsAndReturnReplacementForSelection(filePathToErrors, oldSelectedProblem);

    if (updatedSelectedProblem != null) {
      myTable.setSelection(Collections.singletonList(updatedSelectedProblem));
    }

    updateStatusDescription();
  }

  public void clearAll() {
    ((DartProblemsTableModel)myTable.getModel()).removeAll();
    updateStatusDescription();
  }

  void setToolWindowUpdater(@NotNull final DartProblemsView.ToolWindowUpdater toolWindowUpdater) {
    myToolWindowUpdater = toolWindowUpdater;
  }

  private class FilterProblemsAction extends DumbAwareAction implements Toggleable {
    FilterProblemsAction() {
      super(DartBundle.message("filter.problems"), DartBundle.message("filter.problems.description"), AllIcons.General.Filter);
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
      // show icon as toggled on if any filter is active
      e.getPresentation().putClientProperty(Toggleable.SELECTED_PROPERTY, myPresentationHelper.areFiltersApplied());
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      showFiltersPopup();
    }
  }

  private class AnalysisServerSettingsAction extends DumbAwareAction implements Toggleable {
    AnalysisServerSettingsAction() {
      super(DartBundle.message("analysis.server.settings"), DartBundle.message("analysis.server.settings.description"),
            AllIcons.General.GearPlain);
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
      final boolean asByDefault = myPresentationHelper.getScopedAnalysisMode() == DartProblemsViewSettings.SCOPED_ANALYSIS_MODE_DEFAULT;
      e.getPresentation().putClientProperty(Toggleable.SELECTED_PROPERTY, !asByDefault);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      showAnalysisServerSettingsPopup();
    }
  }
}
