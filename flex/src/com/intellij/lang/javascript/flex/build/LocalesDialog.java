package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;

public class LocalesDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JTable myLocalesTable;
  private JLabel pathToLocalesDirInSdkLabel;

  private LocaleInfo[] locales;

  public LocalesDialog(final @NotNull Project project,
                       final @NotNull VirtualFile flexSdkRoot,
                       final @NotNull String selectedLocalesCommaSeparated) {

    super(project, true);
    setTitle("Locales");

    pathToLocalesDirInSdkLabel.setText(FlexBundle.message("flex.sdk.frameworks.locale", flexSdkRoot.getPath()));

    setupLocalesTable(flexSdkRoot, selectedLocalesCommaSeparated);

    init();
  }

  private void setupLocalesTable(final @NotNull VirtualFile flexSdkRoot, final @NotNull String selectedLocalesCommaSeparated) {
    final VirtualFile localesDirInSdk = flexSdkRoot.findFileByRelativePath("frameworks/locale");
    if (localesDirInSdk != null) {

      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          localesDirInSdk.refresh(false, false);
        }
      });

      final String[] selectedLocales = selectedLocalesCommaSeparated.split(",");
      for (int i = 0; i < selectedLocales.length; i++) {
        selectedLocales[i] = selectedLocales[i].trim();
      }
      Arrays.sort(selectedLocales); // for binary search

      final VirtualFile[] localeDirsInSdk = localesDirInSdk.getChildren();
      locales = new LocaleInfo[localeDirsInSdk.length];
      for (int i = 0; i < localeDirsInSdk.length; i++) {
        locales[i] = new LocaleInfo(localeDirsInSdk[i].getName(), Arrays.binarySearch(selectedLocales, localeDirsInSdk[i].getName()) >= 0);
      }
      Arrays.sort(locales);

      myLocalesTable.setModel(new DefaultTableModel() {

        public String getColumnName(int column) {
          return null;
        }

        public int getColumnCount() {
          return 2;
        }

        public int getRowCount() {
          return locales.length;
        }

        public Class<?> getColumnClass(int columnIndex) {
          return columnIndex == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
          return column == 0;
        }

        public Object getValueAt(int row, int column) {
          return column == 0 ? locales[row].isSelected() : locales[row].getLocale();
        }

        public void setValueAt(Object aValue, int row, int column) {
          if (column == 0) {
            locales[row].setSelected(((Boolean)aValue).booleanValue());
          }
        }
      });

      myLocalesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      myLocalesTable.getColumnModel().getColumn(0).setMaxWidth(45);
      myLocalesTable.setDefaultRenderer(Boolean.class, new NoBackgroundBooleanTableCellRenderer());
      myLocalesTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
          setHorizontalAlignment(CENTER);
          return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
      });

    }
  }

  @NotNull
  public String getSelectedLocalesCommaSeparated() {
    final StringBuilder result = new StringBuilder();
    for (LocaleInfo locale : locales) {
      if (locale.isSelected()) {
        if (result.length() > 0) {
          result.append(',');
        }
        result.append(locale.getLocale());
      }
    }
    return result.toString();
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }


  private static class LocaleInfo implements Comparable<LocaleInfo> {
    private boolean isSelected;
    private final String locale;

    private LocaleInfo(final String locale, final boolean selected) {
      isSelected = selected;
      this.locale = locale;
    }

    public boolean isSelected() {
      return isSelected;
    }

    public void setSelected(boolean selected) {
      isSelected = selected;
    }

    public String getLocale() {
      return locale;
    }

    public int compareTo(final LocaleInfo another) {
      return locale.compareTo(another.locale);
    }
  }
}
