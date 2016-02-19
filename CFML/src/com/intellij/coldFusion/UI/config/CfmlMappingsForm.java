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
import com.intellij.coldFusion.CfmlServerUtil;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.StateRestoringCheckBox;
import com.intellij.util.ui.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.*;
import java.util.List;

import static com.intellij.coldFusion.UI.config.CfmlMappingsConfig.mapEquals;

/**
 * @author vnikolaenko
 */
public class CfmlMappingsForm {
  private JPanel myContentPanel;
  private JPanel myTablePanel;
  private JComboBox myLanguageLevel;
  private JLabel myMessageLabel;
  private TextFieldWithBrowseButton myPathToCfusionFolder;
  private ValidatingTableEditor<Item> myTableEditor;
  private final Project myProject;

  public JComponent getContentPane() {
    return myContentPanel;
  }

  private static class Item {
    public String myDirectoryPath;
    public String myLogicalPath;

    public Item(String directoryPath, String logicalPath) {
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
    myTableEditor = new ValidatingTableEditor<Item>() {
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
    myTableEditor.getEmptyText().setText(CfmlBundle.message("no.mapping"));

    myTablePanel.add(myTableEditor.getContentPane(), BorderLayout.CENTER);

    myLanguageLevel.setRenderer(new ListCellRendererWrapper<String>() {
      @Override
      public void customize(JList list, String value, int index, boolean selected, boolean hasFocus) {
        if (CfmlLanguage.CF8.equals(value)) {
          setText("ColdFusion 8");
        }
        else if (CfmlLanguage.CF9.equals(value)) {
          setText("ColdFusion 9");
        }
        else if (CfmlLanguage.CF10.equals(value)) {
          setText("ColdFusion 10");
        }
        else if (CfmlLanguage.CF11.equals(value)) {
          setText("ColdFusion 11");
        }
        else if (CfmlLanguage.RAILO.equals(value)) {
          //noinspection SpellCheckingInspection
          setText("Railo");
        }
      }
    });
    myLanguageLevel.addItem(CfmlLanguage.CF8);
    myLanguageLevel.addItem(CfmlLanguage.CF9);
    myLanguageLevel.addItem(CfmlLanguage.CF10);
    myLanguageLevel.addItem(CfmlLanguage.CF11);
    myLanguageLevel.addItem(CfmlLanguage.RAILO);

    myMessageLabel.setIcon(UIUtil.getBalloonWarningIcon());
    myMessageLabel.setVisible(false);

    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    descriptor.setTitle(CfmlBundle.message("cfml.project.config.cfdir.description"));

    myPathToCfusionFolder.addBrowseFolderListener(new TextBrowseFolderListener(descriptor){
      @Override
      protected void onFileChosen(@NotNull VirtualFile chosenFile) {
        if(!chosenFile.getName().equals("cfusion")) {
          myMessageLabel.setText(CfmlBundle.message("cfml.project.config.cfdir.error"));
          myMessageLabel.setVisible(true);
        } else {
          super.onFileChosen(chosenFile);
          myMessageLabel.setText("");
          myMessageLabel.setVisible(false);
          Map<String, String> cfserverMapping = CfmlServerUtil.getMappingFromCfserver(myPathToCfusionFolder.getText());

          if (cfserverMapping != null) addItems(cfserverMapping);

        }
      }

    });
  }

  public void addItems(Map<String, String> serverMapping){
    Map<String, String> paths = getPaths();
    for (String ditPathKey : serverMapping.keySet()) {
      paths.put(ditPathKey, serverMapping.get(ditPathKey));
    }
    setItems(paths);
  }

  public void setItems(Map<String, String> paths) {
    List<Item> items = new ArrayList<Item>(paths.size());
    for (Map.Entry<String, String> entry : paths.entrySet()) {
      items.add(new Item(FileUtil.toSystemDependentName(entry.getValue()), entry.getKey()));
    }
    myTableEditor.setModel(new ColumnInfo[]{new DirectoryPathColumnInfo(), new LogicalPathColumnInfo()}, items);
  }

  private Map<String, String> getPaths() {
    List<Item> items = myTableEditor.getItems();
    Map<String, String> result = new HashMap<String, String>();
    for (Item item : items) {
      // TODO: to prettify logical path
      if (!item.isEmpty()) {
        result.put(item.myLogicalPath, FileUtil.toSystemIndependentName(item.myDirectoryPath));
      }
    }
    return result;
  }

  public void reset(CfmlProjectConfiguration.State state) {
    setItems(state != null ? state.getMapps().getServerMappings() : Collections.<String, String>emptyMap());
    String newLanguageLevel = state != null ? state.getLanguageLevel() : CfmlLanguage.CF10;
    myLanguageLevel.setSelectedItem(newLanguageLevel);
    if(state != null && state.getColdFusionDir() != null && !state.getColdFusionDir().isEmpty()) myPathToCfusionFolder.setText(state.getColdFusionDir());
  }

  public void applyTo(CfmlProjectConfiguration.State state, CfmlProjectConfigurable.ChangeState changeState) {
    if (changeState == CfmlProjectConfigurable.ChangeState.APPLIED &&
        myPathToCfusionFolder.getText() != null &&
        CfmlServerUtil.getMappingFromCfserver(myPathToCfusionFolder.getText()) != null &&
        !mapEquals(getPaths(), CfmlServerUtil.getMappingFromCfserver(myPathToCfusionFolder.getText()))) {
      CfmlServerUtil.saveMappingToCfserver(getPaths(), myPathToCfusionFolder.getText());
      try {
        askUserToRestartServer();
        CfmlServerUtil.restartColdfusionServer(myPathToCfusionFolder.getText());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    state.setMapps(new CfmlMappingsConfig(getPaths()));
    state.setLanguageLevel((String)myLanguageLevel.getSelectedItem());
    state.setColdFusionDir(myPathToCfusionFolder.getText());
  }

  private void askUserToRestartServer() {
    (new RestartServerDialog(ProjectUtil.guessCurrentProject(myPathToCfusionFolder))).show();
  }

  private static class LogicalPathColumnInfo extends ColumnInfo<Item, String> implements ValidatingTableEditor.RowHeightProvider {
    public LogicalPathColumnInfo() {
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
    public DirectoryPathColumnInfo() {
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

  class RestartServerDialog extends DialogWrapper {
    private final Project myProject;

    private StateRestoringCheckBox myCbDoNotAskAgain;


    public RestartServerDialog(Project project) {
      super(project, true);
      myProject = project;
      setTitle(CfmlBundle.message("cfml.project.config.dialog.restartserver.title"));
      init();
      getOKAction().putValue(Action.NAME, CfmlBundle.message("cfml.project.config.dialog.restartserver.yesButton"));
      getCancelAction().putValue(Action.NAME, CfmlBundle.message("cfml.project.config.dialog.restartserver.noButton"));
    }


    @Override
    @NotNull
    protected Action[] createActions() {
      return new Action[]{getOKAction(), getCancelAction()};
    }

    @Override
    protected JComponent createNorthPanel() {
      JLabel label = new JLabel(CfmlBundle.message("cfml.project.config.dialog.restartserver.message"));
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(label, BorderLayout.CENTER);
      Icon icon = UIUtil.getQuestionIcon();
      label.setIcon(icon);
      label.setIconTextGap(7);
      return panel;
    }



    @Override
    protected JComponent createCenterPanel() {
      return null;
    }


    @Override
    protected void doOKAction() {
      super.doOKAction();
      //TODO: add exception handler here
      try {
        CfmlServerUtil.restartColdfusionServer(myPathToCfusionFolder.getText());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

  }
}
