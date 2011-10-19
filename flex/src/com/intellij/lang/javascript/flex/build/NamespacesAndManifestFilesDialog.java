package com.intellij.lang.javascript.flex.build;

import static com.intellij.lang.javascript.flex.build.FlexBuildConfiguration.NamespaceAndManifestFileInfo;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class NamespacesAndManifestFilesDialog extends AddRemoveTableRowsDialog<NamespaceAndManifestFileInfo> {
  private final Project myProject;
  private final boolean myIsSwcLibrary;

  public NamespacesAndManifestFilesDialog(final Project project,
                                          final List<NamespaceAndManifestFileInfo> namespaceAndManifestFileInfoList,
                                          final boolean isSwcLibrary) {
    super(project, FlexBundle.message("namespaces.and.manifest.files.title"), cloneList(namespaceAndManifestFileInfoList));
    myProject = project;
    myIsSwcLibrary = isSwcLibrary;
    
    init();
  }

  private static List<NamespaceAndManifestFileInfo> cloneList(final List<NamespaceAndManifestFileInfo> namespaceAndManifestFileInfoList) {
    final List<NamespaceAndManifestFileInfo> clonedList = new ArrayList<NamespaceAndManifestFileInfo>();
    for (NamespaceAndManifestFileInfo namespaceAndManifestFileInfo : namespaceAndManifestFileInfoList) {
      clonedList.add(namespaceAndManifestFileInfo.clone());
    }
    return clonedList;
  }

  protected AddObjectDialog<NamespaceAndManifestFileInfo> createAddObjectDialog() {
    return new AddNamespaceAndManifestFileDialog(myProject, myIsSwcLibrary);
  }

  protected TableModelBase createTableModel() {
    return new TableModelBase() {

      public int getColumnCount() {
        return myIsSwcLibrary ? Column.values().length : Column.values().length - 1;
      }

      public String getColumnName(int column) {
        return Column.values()[column].getColumnName();
      }

      public Class getColumnClass(int column) {
        return Column.values()[column].getColumnClass();
      }

      protected Object getValue(final NamespaceAndManifestFileInfo namespaceAndManifestFileInfo, final int column) {
        return Column.values()[column].getValue(namespaceAndManifestFileInfo);
      }

      protected void setValue(final NamespaceAndManifestFileInfo namespaceAndManifestFileInfo, final int column, final Object aValue) {
        Column.values()[column].setValue(namespaceAndManifestFileInfo, aValue);
      }
    };
  }

  protected int getPreferredColumnWidth(final int columnIndex) {
    return columnIndex == Column.IncludeInSwc.ordinal() ? 80 : 235;
  }

  private enum Column {
    Namespace("Namespace", String.class) {
      Object getValue(final NamespaceAndManifestFileInfo info) {
        return info.NAMESPACE;
      }

      void setValue(final NamespaceAndManifestFileInfo info, final Object value) {
        info.NAMESPACE = (String)value;
      }
    },

    ManifestFilePath("Path to Manifest file", String.class) {
      Object getValue(final NamespaceAndManifestFileInfo info) {
        return FileUtil.toSystemDependentName(info.MANIFEST_FILE_PATH);
      }

      void setValue(final NamespaceAndManifestFileInfo info, final Object value) {
        info.MANIFEST_FILE_PATH = FileUtil.toSystemIndependentName((String)value);
      }
    },

    IncludeInSwc("Include in SWC", Boolean.class) {
      Object getValue(final NamespaceAndManifestFileInfo info) {
        return info.INCLUDE_IN_SWC;
      }

      void setValue(final NamespaceAndManifestFileInfo info, final Object value) {
        info.INCLUDE_IN_SWC = ((Boolean)value).booleanValue();
      }
    };

    private final String myColumnName;
    private final Class myColumnClass;

    private Column(final String columnName, final Class columnClass) {
      myColumnName = columnName;
      myColumnClass = columnClass;
    }

    public String getColumnName() {
      return myColumnName;
    }

    private Class getColumnClass() {
      return myColumnClass;
    }

    abstract Object getValue(final NamespaceAndManifestFileInfo info);

    abstract void setValue(final NamespaceAndManifestFileInfo info, final Object value);
  }
}
