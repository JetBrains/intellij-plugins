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

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.TableView;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.Location;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

public class DartProblemsViewPanel2 extends JPanel implements DataProvider {

  @NotNull private final Project myProject;
  @NotNull private final TableView<AnalysisError> myTable;
  @NotNull private JBLabel mySummaryLabel = new JBLabel();

  // may be remember settings in workspace.xml? (see ErrorTreeViewConfiguration)
  private boolean myAutoScrollToSource = false;

  public DartProblemsViewPanel2(@NotNull final Project project) {
    super(new BorderLayout());
    myProject = project;

    myTable = createTable();
    add(createToolbar(), BorderLayout.WEST);
    add(createCenterPanel(), BorderLayout.CENTER);
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

  @NotNull
  private JComponent createToolbar() {
    final DefaultActionGroup group = new DefaultActionGroup();

    addReanalyzeAndRestartActions(group);
    group.addSeparator();

    addAutoScrollToSourceAction(group, myTable);

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
    updateStatusBar();
  }

  public void clearAll() {
    ((DartProblemsTableModel)myTable.getModel()).removeAll();
    updateStatusBar();
  }
}


