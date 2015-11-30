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
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
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
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.TableView;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.Function;
import com.intellij.util.containers.Convertor;
import com.jetbrains.lang.dart.DartBundle;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

public class DartProblemsViewPanel extends JPanel implements DataProvider, CopyProvider {

  @NotNull private final Project myProject;
  @NotNull private final TableView<DartProblem> myTable;
  @NotNull private JBLabel mySummaryLabel = new JBLabel();

  // may be remember settings and filters in workspace.xml? (see ErrorTreeViewConfiguration)
  private boolean myAutoScrollToSource = false;

  @NotNull private final DartProblemsFilter myFilter;

  public DartProblemsViewPanel(@NotNull final Project project, @NotNull final DartProblemsFilter filter) {
    super(new BorderLayout());
    myProject = project;
    myFilter = filter;

    myTable = createTable();
    add(createToolbar(), BorderLayout.WEST);
    add(createCenterPanel(), BorderLayout.CENTER);
  }

  @NotNull
  private TableView<DartProblem> createTable() {
    final TableView<DartProblem> table = new TableView<DartProblem>(new DartProblemsTableModel(myProject));

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

    table.getRowSorter().addRowSorterListener(new RowSorterListener() {
      @Override
      public void sorterChanged(RowSorterEvent e) {
        final List<? extends RowSorter.SortKey> sortKeys = myTable.getRowSorter().getSortKeys();
        assert sortKeys.size() == 1 : sortKeys;
        ((DartProblemsTableModel)myTable.getModel()).setSortKey(sortKeys.get(0));
      }
    });

    new TableSpeedSearch(table, new Convertor<Object, String>() {
      @Override
      public String convert(Object object) {
        return object instanceof DartProblem
               ? ((DartProblem)object).getErrorMessage() + " " + ((DartProblem)object).getPresentableLocation()
               : "";
      }
    });

    return table;
  }

  private void popupInvoked(final Component component, final int x, final int y) {
    final DefaultActionGroup group = new DefaultActionGroup();
    if (getData(CommonDataKeys.NAVIGATABLE.getName()) != null) {
      group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
    }

    group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_COPY));

    final ActionPopupMenu menu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.COMPILER_MESSAGES_POPUP, group);
    menu.getComponent().show(component, x, y);
  }

  @NotNull
  private JComponent createToolbar() {
    final DefaultActionGroup group = new DefaultActionGroup();

    addReanalyzeAndRestartActions(group);
    group.addSeparator();

    addAutoScrollToSourceAction(group);
    // may be add 'Scroll from source' or 'Autoscroll from source' action (WEB-15792)
    group.addSeparator();

    addGroupBySeverityAction(group);
    group.addAction(new FilterProblemsAction());

    return ActionManager.getInstance().createActionToolbar(ActionPlaces.COMPILER_MESSAGES_TOOLBAR, group, false).getComponent();
  }

  @NotNull
  private JPanel createCenterPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(ScrollPaneFactory.createScrollPane(myTable), BorderLayout.CENTER);
    panel.add(createStatusBar(), BorderLayout.SOUTH);
    return panel;
  }

  @NotNull
  private JPanel createStatusBar() {
    final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(mySummaryLabel);
    return panel;
  }

  private void updateStatusBar() {
    final DartProblemsTableModel model = (DartProblemsTableModel)myTable.getModel();
    final int errorCount = model.getErrorCount();
    final int warningCount = model.getWarningCount();
    final int hintCount = model.getHintCount();
    final String errorText = errorCount == 0 ? "No errors" : errorCount == 1 ? "1 error" : errorCount + " errors";
    final String warningText = warningCount == 0 ? "no warnings" : warningCount == 1 ? "1 warning" : warningCount + " warnings";
    final String hintText = hintCount == 0 ? "no hints" : hintCount == 1 ? "1 hint" : hintCount + " hints";
    mySummaryLabel.setText(errorText + ", " + warningText + ", " + hintText + ".");
  }

  private static void addReanalyzeAndRestartActions(@NotNull final DefaultActionGroup group) {
    final AnAction reanalyzeAction = ActionManager.getInstance().getAction("Dart.Reanalyze");
    if (reanalyzeAction != null) {
      group.add(reanalyzeAction);
    }

    final AnAction restartAction = ActionManager.getInstance().getAction("Dart.Restart.Analysis.Server");
    if (restartAction != null) {
      group.add(restartAction);
    }
  }

  private void addAutoScrollToSourceAction(@NotNull final DefaultActionGroup group) {
    final AutoScrollToSourceHandler autoScrollToSourceHandler = new AutoScrollToSourceHandler() {
      @Override
      protected boolean isAutoScrollMode() {
        return myAutoScrollToSource;
      }

      @Override
      protected void setAutoScrollMode(boolean autoScrollToSource) {
        myAutoScrollToSource = autoScrollToSource;
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
        fireSortingOrFilterChanged();
      }
    };

    group.addAction(action);
  }

  void fireSortingOrFilterChanged() {
    myTable.getRowSorter().allRowsChanged();
  }

  private void showFiltersPopup() {
    final DartProblemsFilterForm form = new DartProblemsFilterForm();
    form.reset(myFilter);

    form.addListener(new DartProblemsFilterForm.FilterListener() {
      @Override
      public void filtersChanged() {
        myFilter.updateFromUI(form);
        fireSortingOrFilterChanged();
      }

      @Override
      public void filtersResetRequested() {
        myFilter.resetAllFilters();
        form.reset(myFilter);
        fireSortingOrFilterChanged();
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
    final String s = StringUtil.join(selectedObjects, new Function<DartProblem, String>() {
      @Override
      public String fun(final DartProblem problem) {
        return problem.getSeverity() + ": " + problem.getErrorMessage() + " (" + problem.getPresentableLocation() + ")";
      }
    }, "\n");

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

  private void navigate(final boolean requestFocus) {
    final Navigatable navigatable = createNavigatable();
    if (navigatable != null && navigatable.canNavigateToSource()) {
      navigatable.navigate(requestFocus);
    }
  }

  public void setErrors(@NotNull final Map<String, List<AnalysisError>> filePathToErrors) {
    ((DartProblemsTableModel)myTable.getModel()).setErrors(filePathToErrors);
    updateStatusBar();
  }

  public void clearAll() {
    ((DartProblemsTableModel)myTable.getModel()).removeAll();
    updateStatusBar();
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


