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
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.TableView;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.jetbrains.lang.dart.analyzer.DartServerErrorsAnnotator;
import icons.DartIcons;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.dartlang.analysis.server.protocol.Location;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class DartProblemsViewPanel2 extends JPanel implements DataProvider {

  @NotNull private final Project myProject;
  @NotNull private final TableView<AnalysisError> myTable;

  // may be remember settings in workspace.xml? (see ErrorTreeViewConfiguration)
  private boolean myAutoScrollToSource = false;

  public DartProblemsViewPanel2(@NotNull final Project project) {
    super(new BorderLayout());
    myProject = project;

    myTable = createTable();
    add(createToolbar(), BorderLayout.WEST);
    add(ScrollPaneFactory.createScrollPane(myTable), BorderLayout.CENTER);
  }

  @NotNull
  private TableView<AnalysisError> createTable() {
    final TableView<AnalysisError> table = new TableView<AnalysisError>(new DartProblemsTableModel());

    table.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          navigate(false); // as in NewErrorTreeViewPanel
        }
      }
    });

    EditSourceOnDoubleClickHandler.install(table);

    return table;
  }

  private JComponent createToolbar() {
    final DefaultActionGroup group = new DefaultActionGroup();

    addReanalyzeAndRestartActions(group);
    group.addSeparator();

    addAutoScrollToSourceAction(group, myTable);

    return ActionManager.getInstance().createActionToolbar(ActionPlaces.COMPILER_MESSAGES_TOOLBAR, group, false).getComponent();
  }

  private void addAutoScrollToSourceAction(@NotNull final DefaultActionGroup group, @NotNull final TableView<AnalysisError> table) {
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

    autoScrollToSourceHandler.install(table);
    group.addAction(autoScrollToSourceHandler.createToggleAction());
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

  @Nullable
  @Override
  public Object getData(@NonNls String dataId) {
    if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
      return createNavigatable();
    }

    return null;
  }

  @Nullable
  private Navigatable createNavigatable() {
    final AnalysisError error = myTable.getSelectedObject();
    if (error != null) {
      final Location location = error.getLocation();
      final String filePath = FileUtil.toSystemDependentName(location.getFile());
      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
      if (file != null) {
        final OpenFileDescriptor navigatable = new OpenFileDescriptor(myProject, file, error.getLocation().getOffset());
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
  }

  public void clearAll() {
    ((DartProblemsTableModel)myTable.getModel()).removeAll();
  }
}

class AnalysisErrorComparator implements Comparator<AnalysisError> {
  public static final int MESSAGE_COLUMN_ID = 0;
  public static final int LOCATION_COLUMN_ID = 1;
  public static final int CORRECTION_COLUMN_ID = 2;
  final int myColumn;

  AnalysisErrorComparator(int column) {
    myColumn = column;
  }

  @Override
  public int compare(AnalysisError o1, AnalysisError o2) {
    {
      final int s1 = getSeverity(o1);
      final int s2 = getSeverity(o2);
      if (s1 != s2) {
        return s1 - s2;
      }
    }
    if (myColumn == MESSAGE_COLUMN_ID) {
      final String m1 = o1.getMessage();
      final String m2 = o2.getMessage();
      return StringUtil.compare(m1, m2, false);
    }
    if (myColumn == LOCATION_COLUMN_ID) {
      final Location l1 = o1.getLocation();
      final Location l2 = o2.getLocation();
      // file name
      final String n1 = getFileName(l1.getFile());
      final String n2 = getFileName(l2.getFile());
      final int c = StringUtil.compare(n1, n2, false);
      if (c != 0) {
        return c;
      }
      // line
      return l1.getStartLine() - l2.getStartLine();
    }
    if (myColumn == CORRECTION_COLUMN_ID) {
      final String c1 = o1.getCorrection();
      final String c2 = o2.getCorrection();
      return StringUtil.compare(c1, c2, false);
    }
    return 0;
  }

  private static String getFileName(String filePath) {
    return new File(filePath).getName();
  }

  private static int getSeverity(AnalysisError error) {
    final String severity = error.getSeverity();
    if (AnalysisErrorSeverity.ERROR.equals(severity)) {
      return 0;
    }
    if (AnalysisErrorSeverity.WARNING.equals(severity)) {
      return 1;
    }
    return 2;
  }
}

class AnalysisErrorMessageRenderer extends DefaultTableCellRenderer {
  @Override
  protected void setValue(Object value) {
    if (!(value instanceof AnalysisError)) {
      return;
    }
    final AnalysisError error = (AnalysisError)value;
    // set icon
    final String severity = error.getSeverity();
    if (AnalysisErrorSeverity.ERROR.equals(severity)) {
      setIcon(AllIcons.General.Error);
    }
    else if (AnalysisErrorSeverity.WARNING.equals(severity)) {
      setIcon(DartIcons.Dart_warning);
    }
    else if (AnalysisErrorSeverity.INFO.equals(severity)) {
      setIcon(AllIcons.General.Information);
    }
    else {
      setIcon(null);
    }
    // set text
    setText(error.getMessage());
  }
}

class DartProblemsTableModel extends ListTableModel<AnalysisError> {
  private static final TableCellRenderer MESSAGE_RENDERER = new AnalysisErrorMessageRenderer();

  // Kind of hack to keep a reference to the live collection used in a super class, but it allows to improve performance greatly.
  // Having it in hands we can do bulk rows removal with a single fireTableRowsDeleted() call afterwards
  private final List<AnalysisError> myItems;

  public DartProblemsTableModel() {
    super(new ColumnInfo<AnalysisError, AnalysisError>("Description") {
      final Comparator<AnalysisError> myComparator = new AnalysisErrorComparator(AnalysisErrorComparator.MESSAGE_COLUMN_ID);

      @Nullable
      @Override
      public Comparator<AnalysisError> getComparator() {
        return myComparator;
      }

      @Nullable
      @Override
      public String getPreferredStringValue() {
        return "012345678901234567890123456789012345678901234567890123456789";
      }

      @Nullable
      @Override
      public TableCellRenderer getRenderer(AnalysisError analysisError) {
        return MESSAGE_RENDERER;
      }

      @NotNull
      @Override
      public AnalysisError valueOf(AnalysisError o) {
        return o;
      }
    }, new ColumnInfo<AnalysisError, String>("Location") {
      final Comparator<AnalysisError> myComparator = new AnalysisErrorComparator(AnalysisErrorComparator.LOCATION_COLUMN_ID);

      @Nullable
      @Override
      public Comparator<AnalysisError> getComparator() {
        return myComparator;
      }

      @Nullable
      @Override
      public String getPreferredStringValue() {
        return "01234567890";
      }

      @NotNull
      @Override
      public String valueOf(AnalysisError o) {
        final Location location = o.getLocation();
        final String file = location.getFile();
        final String fileName = new File(file).getName();
        return fileName + ":" + location.getStartLine();
      }
    }, new ColumnInfo<AnalysisError, String>("Correction") {
      final Comparator<AnalysisError> myComparator = new AnalysisErrorComparator(AnalysisErrorComparator.CORRECTION_COLUMN_ID);

      @Nullable
      @Override
      public Comparator<AnalysisError> getComparator() {
        return myComparator;
      }

      @Nullable
      @Override
      public String getPreferredStringValue() {
        return "01234567890";
      }

      @Nullable
      @Override
      public String valueOf(AnalysisError o) {
        return o.getCorrection();
      }
    });

    myItems = new ArrayList<AnalysisError>();
    setItems(myItems);
  }

  @Override
  public boolean canExchangeRows(int oldIndex, int newIndex) {
    return false;
  }

  @Override
  public void exchangeRows(int idx1, int idx2) {
    throw new IllegalStateException();
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  public void removeRows(final int firstRow, final int lastRow) {
    assert lastRow >= firstRow;

    for (int i = lastRow; i >= firstRow; i--) {
      myItems.remove(i);
    }

    fireTableRowsDeleted(firstRow, lastRow);
  }

  public void removeAll() {
    final int rowCount = getRowCount();
    if (rowCount > 0) {
      myItems.clear();
      fireTableRowsDeleted(0, rowCount - 1);
    }
  }

  public void setErrors(@NotNull final Map<String, List<AnalysisError>> filePathToErrors) {
    final Set<String> systemDependentFilePaths = ContainerUtil.map2Set(filePathToErrors.keySet(), new Function<String, String>() {
      @Override
      public String fun(String filePath) {
        return FileUtil.toSystemDependentName(filePath);
      }
    });
    removeRowsForFilesInSet(systemDependentFilePaths);
    addErrors(filePathToErrors);
  }

  private void removeRowsForFilesInSet(@NotNull final Set<String> systemDependentFilePaths) {
    // Looks for regions in table items that should be removed and removes them.
    // For performance reasons we try to call removeRows() as rare as possible, that means with regions as big as possible.
    // Logic is based on the fact that all errors for each particular file are stored continuously in the myItems model

    for (int i = getRowCount() - 1; i >= 0; i--) {
      final AnalysisError error = getItem(i);
      if (systemDependentFilePaths.remove(error.getLocation().getFile())) {
        final int lastRowToDelete = i;

        AnalysisError lastErrorForCurrentFile = error;

        int j = i - 1;
        while (j >= 0) {
          final AnalysisError previousError = getItem(j);

          if (previousError.getLocation().getFile().equals(lastErrorForCurrentFile.getLocation().getFile())) {
            // previousError should be removed from the table as well
            j--;
            continue;
          }

          if (systemDependentFilePaths.remove(previousError.getLocation().getFile())) {
            // continue iterating the table because we met a range of errors for another file that also should be removed
            lastErrorForCurrentFile = previousError;
            j--;
            continue;
          }

          break;
        }

        final int firstRowToDelete = j + 1;
        removeRows(firstRowToDelete, lastRowToDelete);

        if (systemDependentFilePaths.isEmpty()) {
          break;
        }

        //noinspection AssignmentToForLoopParameter
        i = j + 1; // rewind according to the amount of removed rows
      }
    }
  }

  private void addErrors(@NotNull final Map<String, List<AnalysisError>> filePathToErrors) {
    final List<AnalysisError> errorsToAdd = new ArrayList<AnalysisError>();
    for (Map.Entry<String, List<AnalysisError>> entry : filePathToErrors.entrySet()) {
      final String filePath = entry.getKey();
      final List<AnalysisError> errors = entry.getValue();

      for (AnalysisError error : errors) {
        if (DartServerErrorsAnnotator.shouldIgnoreMessageFromDartAnalyzer(filePath, error)) continue;

        errorsToAdd.add(error);
      }
    }

    if (!errorsToAdd.isEmpty()) {
      addRows(errorsToAdd);
    }
  }
}
