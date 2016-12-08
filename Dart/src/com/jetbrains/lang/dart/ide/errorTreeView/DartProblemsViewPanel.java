/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.ide.CopyProvider;
import com.intellij.ide.actions.ContextHelpAction;
import com.intellij.openapi.actionSystem.*;
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
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.content.Content;
import com.intellij.ui.table.TableView;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DartProblemsViewPanel extends SimpleToolWindowPanel implements DataProvider, CopyProvider {

  private static final long DEFAULT_SERVER_WAIT_MILLIS = 5000L; // Switch to UNKNOWN after 5s with no response.

  @NotNull private final Project myProject;
  @NotNull private final TableView<DartProblem> myTable;

  @NotNull private DartProblemsViewSettings mySettings;
  @NotNull private final DartProblemsFilter myFilter;

  private Content myContent;

  public DartProblemsViewPanel(@NotNull final Project project,
                               @NotNull final DartProblemsFilter filter,
                               @NotNull DartProblemsViewSettings settings) {
    super(false, true);
    myProject = project;
    myFilter = filter;
    mySettings = settings;

    myTable = createTable();
    setToolbar(createToolbar());
    setContent(createCenterPanel());

    DartAnalysisServerService.getInstance().maxMillisToWaitForServerResponse = DEFAULT_SERVER_WAIT_MILLIS;
  }

  @NotNull
  private TableView<DartProblem> createTable() {
    final TableView<DartProblem> table = new TableView<>(new DartProblemsTableModel(myProject, myFilter));

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
    ((DefaultRowSorter)table.getRowSorter()).setRowFilter(myFilter);

    table.getRowSorter().addRowSorterListener(e -> {
      final List<? extends RowSorter.SortKey> sortKeys = myTable.getRowSorter().getSortKeys();
      assert sortKeys.size() == 1 : sortKeys;
      ((DartProblemsTableModel)myTable.getModel()).setSortKey(sortKeys.get(0));
    });

    new TableSpeedSearch(table, object -> object instanceof DartProblem
                                          ? ((DartProblem)object).getErrorMessage() + " " + ((DartProblem)object).getPresentableLocation()
                                          : "");

    table.setShowVerticalLines(false);
    return table;
  }

  private void popupInvoked(final Component component, final int x, final int y) {
    final DefaultActionGroup group = new DefaultActionGroup();
    if (getData(CommonDataKeys.NAVIGATABLE.getName()) != null) {
      group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_COPY));

    final ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.TOOLBAR, group);
    menu.getComponent().show(component, x, y);
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

    group.addAction(new ContextHelpAction("reference.toolWindow.DartAnalysis"));

    return ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, false).getComponent();
  }

  @NotNull
  private JPanel createCenterPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(ScrollPaneFactory.createScrollPane(myTable), BorderLayout.CENTER);
    return panel;
  }

  private void updateStatusDescription() {
    if (myContent != null) {
      myContent.setDisplayName(((DartProblemsTableModel)myTable.getModel()).getStatusText());
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
        return mySettings.autoScrollToSource;
      }

      @Override
      protected void setAutoScrollMode(boolean autoScrollToSource) {
        mySettings.autoScrollToSource = autoScrollToSource;
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
        return ((DartProblemsTableModel)myTable.getModel()).isGroupBySeverity();
      }

      @Override
      public void setSelected(AnActionEvent e, boolean groupBySeverity) {
        ((DartProblemsTableModel)myTable.getModel()).setGroupBySeverity(groupBySeverity);
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
    form.reset(myFilter);

    form.addListener(new DartProblemsFilterForm.FilterListener() {
      @Override
      public void filtersChanged() {
        myFilter.updateFromUI(form);
        fireGroupingOrFilterChanged();
      }

      @Override
      public void filtersResetRequested() {
        myFilter.resetAllFilters();
        form.reset(myFilter);
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
    final String s = StringUtil.join(selectedObjects, problem -> problem.getSeverity() +
                                                                 ": " +
                                                                 problem.getErrorMessage() +
                                                                 " (" +
                                                                 problem.getPresentableLocation() +
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

  void setContent(Content content) {
    myContent = content;
  }

  void updateFromSettings(DartProblemsViewSettings settings) {
    mySettings = settings;
  }

  private class FilterProblemsAction extends DumbAwareAction implements Toggleable {
    public FilterProblemsAction() {
      super(DartBundle.message("filter.problems"), DartBundle.message("filter.problems.description"), AllIcons.General.Filter);
    }

    @Override
    public void update(final AnActionEvent e) {
      // show icon as toggled on if any filter is active
      e.getPresentation().putClientProperty(Toggleable.SELECTED_PROPERTY, myFilter.areFiltersApplied());
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
      showFiltersPopup();
    }
  }
}
