// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.ide.CopyProvider;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.table.TableView;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.SmartList;
import com.intellij.util.ui.JBUI;
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
import java.util.Locale;
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
    table.setRowHeight(table.getRowHeight() + JBUI.scale(4));

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

    addQuickFixActions(group);

    final ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.TOOLBAR, group);
    menu.getComponent().show(component, x, y);
  }

  private void addQuickFixActions(@NotNull final DefaultActionGroup group) {
    // Reference the list of selected DartProblems
    final List<DartProblem> selectedProblems = myTable.getSelectedObjects();

    // Reference the single DartProblem selected
    final DartProblem selectedProblem = selectedProblems.size() == 1 ? selectedProblems.get(0) : null;

    // Return if there is not a single selected DartProblem
    if (selectedProblem == null) return;

    // Reference the selected VirtualFile
    final VirtualFile selectedVFile = selectedProblem.getFile();

    // Return if the VirtualFile is null or the DartProblem does not have a fix
    if (selectedVFile == null) return;

    // Reference the DAS
    final DartAnalysisServerService das = DartAnalysisServerService.getInstance(myProject);

    //
    // Reference the set of fixes for the selected DartProblem and add the fixes as actions onto the passed action group
    //
    final List<SourceChange> selectedProblemSourceChangeFixes = new SmartList<>();
    das.askForFixesAndWaitABitIfReceivedQuickly(selectedVFile, selectedProblem.getOffset(), fixes -> {
      for (AnalysisErrorFixes fix : fixes) {
        if (fix.getError().getCode().equals(selectedProblem.getCode())) {
          selectedProblemSourceChangeFixes.addAll(fix.getFixes());
        }
      }
    });

    // Return if there are no quick fixes for this problem
    if (selectedProblemSourceChangeFixes.isEmpty()) return;

    // Add a separator and proceed to add the action for each fix
    group.addSeparator();
    for (final SourceChange sourceChangeFix : selectedProblemSourceChangeFixes) {
      if(sourceChangeFix == null) continue;
      group.add(new AnAction(sourceChangeFix.getMessage()) {
        @Override
        public void actionPerformed(final AnActionEvent event) {
          ApplicationManager.getApplication().runWriteAction(() ->
                                                             {
                                                               try {
                                                                 AssistUtils.applySourceChange(myProject, sourceChangeFix, true);
                                                               }
                                                               catch (DartSourceEditException ignored) {
                                                               }
                                                             }
          );
        }
      });
    }
  }

  @NotNull
  private JComponent createToolbar() {
    final DefaultActionGroup group = new DefaultActionGroup();

    addReanalyzeActions(group);
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
    final AnAction action = new ToggleAction(DartBundle.message("group.by.severity"),
                                             DartBundle.message("group.by.severity.description"),
                                             AllIcons.Nodes.SortBySeverity) {
      @Override
      public boolean isSelected(AnActionEvent e) {
        return myPresentationHelper.isGroupBySeverity();
      }

      @Override
      public void setSelected(AnActionEvent e, boolean groupBySeverity) {
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
    final DartProblemsFilterForm form = new DartProblemsFilterForm();
    form.reset(myPresentationHelper);

    form.addListener(new DartProblemsFilterForm.FilterListener() {
      @Override
      public void filtersChanged() {
        myPresentationHelper.updateFromUI(form);
        fireGroupingOrFilterChanged();
      }

      @Override
      public void filtersResetRequested() {
        myPresentationHelper.resetAllFilters();
        form.reset(myPresentationHelper);
        fireGroupingOrFilterChanged();
      }
    });

    final Rectangle visibleRect = myTable.getVisibleRect();
    final Point tableTopLeft = new Point(myTable.getLocationOnScreen().x + visibleRect.x, myTable.getLocationOnScreen().y + visibleRect.y);

    JBPopupFactory.getInstance()
      .createComponentPopupBuilder(form.getMainPanel(), form.getMainPanel())
      .setProject(myProject)
      .setTitle("Dart Problems Filter")
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
    final String s = StringUtil.join(selectedObjects, problem -> problem.getSeverity().toLowerCase(Locale.US) +
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
  public Object getData(@NonNls String dataId) {
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
    public FilterProblemsAction() {
      super(DartBundle.message("filter.problems"), DartBundle.message("filter.problems.description"), AllIcons.General.Filter);
    }

    @Override
    public void update(final AnActionEvent e) {
      // show icon as toggled on if any filter is active
      e.getPresentation().putClientProperty(Toggleable.SELECTED_PROPERTY, myPresentationHelper.areFiltersApplied());
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
      showFiltersPopup();
    }
  }
}