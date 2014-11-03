package com.jetbrains.lang.dart.sdk.listPackageDirs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

public class DartListPackageDirsDialog extends DialogWrapper {

  public static final int CONFIGURE_NONE_EXIT_CODE = NEXT_USER_EXIT_CODE;

  private JPanel myMainPanel;
  private JBTable myTable;

  private @NotNull final Map<String, List<File>> myPackageMap;

  protected DartListPackageDirsDialog(final @NotNull Project project, final @NotNull Map<String, List<File>> packageMap) {
    super(project);
    myPackageMap = packageMap;
    setTitle("Dart Package List");
    initTable();
    setOKButtonText("Configure all");

    init();
  }

  private void initTable() {
    final String[][] data = new String[myPackageMap.size()][2];

    int i = 0;
    for (Map.Entry<String, List<File>> entry : myPackageMap.entrySet()) {
      data[i][0] = entry.getKey();
      data[i][1] = FileUtil.toSystemDependentName(StringUtil.join(entry.getValue(), "; "));
      i++;
    }

    myTable.setModel(new DefaultTableModel(data, new String[]{"Package name", "Location"}) {
      public boolean isCellEditable(final int row, final int column) {
        return false;
      }
    });

    final int width = new JLabel("Package name").getPreferredSize().width * 4 / 3;
    myTable.getColumnModel().getColumn(0).setPreferredWidth(width);
    myTable.getColumnModel().getColumn(1).setPreferredWidth(500 - width);
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @NotNull
  protected Action[] createActions() {
    if (SystemInfo.isMac) {
      return new Action[]{getCancelAction(), new ConfigureNoneAction(), getOKAction()};
    }
    return new Action[]{getOKAction(), new ConfigureNoneAction(), getCancelAction()};
  }

  @Nullable
  protected String getDimensionServiceKey() {
    return "DartPackageListDialogDimensions";
  }

  private class ConfigureNoneAction extends DialogWrapperAction {
    protected ConfigureNoneAction() {
      super("Configure none");
    }

    protected void doAction(final ActionEvent e) {
      close(CONFIGURE_NONE_EXIT_CODE);
    }
  }
}
