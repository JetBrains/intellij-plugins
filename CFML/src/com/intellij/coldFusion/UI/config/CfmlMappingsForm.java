/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion.UI.config;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.ui.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author vnikolaenko
 */
public class CfmlMappingsForm {
  private JPanel myContentPanel;
  private JPanel myTablePanel;
  private JComboBox myLanguageLevel;
  private JLabel myMessageLabel;
  private final ValidatingTableEditor<Item> myTableEditor;
  private final Project myProject;


  public JComponent getContentPane() {
    return myContentPanel;
  }

  private static class Item {
    public String myDirectoryPath;
    public String myLogicalPath;

    Item(String directoryPath, String logicalPath) {
      myDirectoryPath = FileUtil.toSystemDependentName(directoryPath);
      myLogicalPath = logicalPath;
    }

    public boolean isEmpty() {
      return StringUtil.isEmpty(myDirectoryPath) && StringUtil.isEmpty(myLogicalPath);
    }
  }

  public CfmlMappingsForm(Map<String, String> defaultMappings, Project project) {
    this(project);
    setItems(defaultMappings);
  }

  @SuppressWarnings("unchecked")
  public CfmlMappingsForm(Project project) {
    myProject = project;
    myTableEditor = new ValidatingTableEditor<>() {
      @Override
      protected Item cloneOf(Item item) {
        return new Item(item.myDirectoryPath, item.myLogicalPath);
      }

      @Override
      @Nullable
      protected String validate(Item item) {
        if (StringUtil.isEmpty(item.myDirectoryPath)) {
          return CfmlBundle.message("directory.path.is.empty");
        }
        if (StringUtil.isEmpty(item.myLogicalPath)) {
          return CfmlBundle.message("logical.path.is.empty");
        }
        if (!item.myLogicalPath.startsWith("/") && !item.myLogicalPath.startsWith("\\")) {
          return CfmlBundle.message("incorrect.logical.path");
        }
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(item.myDirectoryPath);
        if (file == null || !file.isValid() || !file.isDirectory()) {
          return CfmlBundle.message("directory.path.is.not.found", item.myDirectoryPath);
        }
        return null;
      }

      @Override
      protected Item createItem() {
        return new Item("", "");
      }
    };
    myTableEditor.setShowGrid(false);
    myTableEditor.getEmptyText().setText(CfmlBundle.message("no.mapping"));

    myTablePanel.setBorder(IdeBorderFactory.createTitledBorder(CfmlBundle.message("border.text.server.mappings"), false, JBUI.insetsTop(8)).setShowLine(false));
    myTablePanel.add(myTableEditor.getContentPane(), BorderLayout.CENTER);

    //noinspection SpellCheckingInspection
    myLanguageLevel.setRenderer(SimpleListCellRenderer.create(
      "", value -> CfmlLanguage.CF8.equals(value) ? "ColdFusion 8" : //NON-NLS
                   CfmlLanguage.CF9.equals(value) ? "ColdFusion 9" : //NON-NLS
                   CfmlLanguage.CF10.equals(value) ? "ColdFusion 10" : //NON-NLS
                   CfmlLanguage.CF11.equals(value) ? "ColdFusion 11" : //NON-NLS
                   CfmlLanguage.RAILO.equals(value) ? "Railo" : //NON-NLS
                   CfmlLanguage.LUCEE.equals(value) ? "Lucee" : "")); //NON-NLS
    myLanguageLevel.addItem(CfmlLanguage.CF8);
    myLanguageLevel.addItem(CfmlLanguage.CF9);
    myLanguageLevel.addItem(CfmlLanguage.CF10);
    myLanguageLevel.addItem(CfmlLanguage.CF11);
    myLanguageLevel.addItem(CfmlLanguage.RAILO);
    myLanguageLevel.addItem(CfmlLanguage.LUCEE);

    myMessageLabel.setIcon(UIUtil.getBalloonWarningIcon());
    myMessageLabel.setVisible(false);
  }

  public void setItems(Map<String, String> paths) {
    List<Item> items = new ArrayList<>(paths.size());
    for (Map.Entry<String, String> entry : paths.entrySet()) {
      items.add(new Item(FileUtil.toSystemDependentName(entry.getValue()), entry.getKey()));
    }
    myTableEditor.setModel(new ColumnInfo[]{new DirectoryPathColumnInfo(), new LogicalPathColumnInfo()}, items);
  }

  private Map<String, String> getPaths() {
    List<Item> items = myTableEditor.getItems();
    Map<String, String> result = new HashMap<>();
    for (Item item : items) {
      // TODO: to prettify logical path
      if (!item.isEmpty()) {
        result.put(item.myLogicalPath, FileUtil.toSystemIndependentName(item.myDirectoryPath));
      }
    }
    return result;
  }

  public void reset(CfmlProjectConfiguration.State state) {
    setItems(state != null ? state.getMapps().getServerMappings() : Collections.emptyMap());
    String newLanguageLevel = state != null ? state.getLanguageLevel() : CfmlLanguage.CF10;
    myLanguageLevel.setSelectedItem(newLanguageLevel);
  }

  public void applyTo(CfmlProjectConfiguration.State state) {
    state.setMapps(new CfmlMappingsConfig(getPaths()));
    state.setLanguageLevel((String)myLanguageLevel.getSelectedItem());
  }

  private static class LogicalPathColumnInfo extends ColumnInfo<Item, String> implements ValidatingTableEditor.RowHeightProvider {
    LogicalPathColumnInfo() {
      super(CfmlBundle.message("logical.path.column.info"));
    }

    @Override
    public String valueOf(Item item) {
      return item.myLogicalPath;
    }

    @Override
    public void setValue(Item item, String value) {
      item.myLogicalPath = value;
    }

    @Override
    public boolean isCellEditable(Item item) {
      return true;
    }

    @Override
    public int getRowHeight() {
      return new JTextField().getPreferredSize().height + 1;
    }

    @Override
    public TableCellEditor getEditor(Item o) {
      return new AbstractTableCellEditor() {
        JTextField myComponent;

        @Override
        @NonNls
        public Object getCellEditorValue() {
          return myComponent.getText();
        }

        @Override
        public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, int column) {
          myComponent = new JTextField();
          myComponent.setText((String)value);
          return myComponent;
        }
      };
    }
  }

  private class DirectoryPathColumnInfo extends ColumnInfo<Item, String> implements ValidatingTableEditor.RowHeightProvider {
    DirectoryPathColumnInfo() {
      super(CfmlBundle.message("directory.path.column.info"));
    }

    @Override
    public String valueOf(Item item) {
      return item.myDirectoryPath;
    }

    @Override
    public void setValue(Item item, String value) {
      item.myDirectoryPath = FileUtil.toSystemDependentName(value);
    }

    @Override
    public boolean isCellEditable(Item item) {
      return true;
    }

    @Override
    public TableCellEditor getEditor(Item o) {
      return new LocalPathCellEditor(myProject);
    }

    @Override
    public int getRowHeight() {
      return new JTextField().getPreferredSize().height + 1;
    }
  }
}
