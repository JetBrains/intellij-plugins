package com.jetbrains.lang.dart.sdk;

import com.intellij.icons.AllIcons;
import com.intellij.ide.browsers.BrowserSpecificSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.ui.impl.CheckboxTreeTable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.LocalPathCellEditor;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

public class DartConfigurable implements SearchableConfigurable {

  public static final String DART_SETTINGS_PAGE_NAME = DartBundle.message("dart.title");

  private JPanel myMainPanel;
  private JBCheckBox myEnableDartSupportCheckBox;

  private JPanel mySettingsPanel;
  private TextFieldWithBrowseButton mySdkPathTextWithBrowse;
  private JBLabel myVersionLabel;

  private TextFieldWithBrowseButton myDartiumPathTextWithBrowse;
  private JButton myDartiumSettingsButton;

  private JPanel myCustomPackageRootPanel;
  private JBCheckBox myCustomPackageRootCheckBox;
  private TextFieldWithBrowseButton myCustomPackageRootTextWithBrowse;

  private JPanel myModulesPanel;

  private CheckboxTreeTable myModulesCheckboxTreeTable;
  private JBLabel myErrorLabel;

  private final @NotNull Project myProject;

  private boolean myInReset = false;

  private boolean myDartSupportEnabledInitial;
  private @Nullable DartSdk mySdkInitial;
  private final @NotNull Collection<Module> myModulesWithDartSdkLibAttachedInitial = new THashSet<Module>();

  private @Nullable WebBrowser myDartiumInitial;
  private ChromeSettings myDartiumSettingsCurrent;
  private final @NotNull Map<Module, String> myModuleToCustomPackageRootCurrent = new THashMap<Module, String>();

  public DartConfigurable(final @NotNull Project project) {
    myProject = project;
    initEnableDartSupportCheckBox();
    initDartSdkAndDartiumControls();
    initCustomPackageRootPanel();
    initModulesPanel();
    myErrorLabel.setIcon(AllIcons.Actions.Lightning);
  }

  private void initEnableDartSupportCheckBox() {
    final Insets margin = myEnableDartSupportCheckBox.getMargin();
    myEnableDartSupportCheckBox.setMargin(new Insets(margin.top, 0, margin.bottom, margin.right));
    myEnableDartSupportCheckBox.setText(DartBundle.message("enable.dart.support.for.project.0", myProject.getName()));
    myEnableDartSupportCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControlsEnabledState();
        updateErrorLabel();
      }
    });
  }

  private void initDartSdkAndDartiumControls() {
    final Computable<Boolean> isResettingControlsComputable = new Computable<Boolean>() {
      public Boolean compute() {
        return myInReset;
      }
    };

    DartSdkUtil.initDartSdkAndDartiumControls(myProject, mySdkPathTextWithBrowse, myVersionLabel, myDartiumPathTextWithBrowse,
                                              isResettingControlsComputable);

    final DocumentAdapter documentListener = new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        updateErrorLabel();
      }
    };

    mySdkPathTextWithBrowse.getTextField().getDocument().addDocumentListener(documentListener);
    myDartiumPathTextWithBrowse.getTextField().getDocument().addDocumentListener(documentListener);

    myDartiumSettingsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        ShowSettingsUtil.getInstance().editConfigurable(myProject, myDartiumSettingsCurrent.createConfigurable());
      }
    });
  }

  private void initCustomPackageRootPanel() {
    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      myCustomPackageRootPanel.setVisible(false);
      return;
    }

    final Insets margin = myCustomPackageRootCheckBox.getMargin();
    myCustomPackageRootCheckBox.setMargin(new Insets(margin.top, -2, margin.bottom, margin.right));

    myCustomPackageRootCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myCustomPackageRootTextWithBrowse.setEnabled(myCustomPackageRootCheckBox.isSelected());
        updateErrorLabel();

        if (myCustomPackageRootCheckBox.isSelected() && myCustomPackageRootCheckBox.isEnabled()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myCustomPackageRootTextWithBrowse.getTextField(), true);
        }
      }
    });

    myCustomPackageRootTextWithBrowse.addBrowseFolderListener(DartBundle.message("select.custom.package.root"), null, myProject,
                                                               FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myCustomPackageRootTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        final Module module = myModuleToCustomPackageRootCurrent.entrySet().iterator().next().getKey();
        final String customPackagesPath = FileUtil.toSystemIndependentName(myCustomPackageRootTextWithBrowse.getText().trim());
        myModuleToCustomPackageRootCurrent.put(module, customPackagesPath);
        updateErrorLabel();
      }
    });
  }

  private void initModulesPanel() {
    if (!DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      myModulesPanel.setVisible(false);
      return;
    }

    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    Arrays.sort(modules, new Comparator<Module>() {
      @Override
      public int compare(final Module module1, final Module module2) {
        return module1.getName().toLowerCase().compareTo(module2.getName().toLowerCase());
      }
    });

    final CheckedTreeNode rootNode = new CheckedTreeNode(myProject);
    ((DefaultTreeModel)myModulesCheckboxTreeTable.getTree().getModel()).setRoot(rootNode);
    myModulesCheckboxTreeTable.getTree().setRootVisible(true);
    myModulesCheckboxTreeTable.getTree().setShowsRootHandles(false);

    for (final Module module : modules) {
      rootNode.add(new CheckedTreeNode(module));
    }

    ((DefaultTreeModel)myModulesCheckboxTreeTable.getTree().getModel()).reload(rootNode);
  }

  @Override
  @NotNull
  public String getId() {
    return "dart.settings";
  }

  @Override
  @Nullable
  public Runnable enableSearch(final String option) {
    return null;
  }

  @Override
  @Nls
  public String getDisplayName() {
    return DART_SETTINGS_PAGE_NAME;
  }

  @Override
  @Nullable
  public String getHelpTopic() {
    return "settings.dart.settings";
  }

  @Override
  @Nullable
  public JComponent createComponent() {
    return myMainPanel;
  }

  @Override
  public boolean isModified() {
    final String sdkHomePath = FileUtilRt.toSystemIndependentName(mySdkPathTextWithBrowse.getText().trim());
    final boolean sdkSelected = DartSdkUtil.isDartSdkHome(sdkHomePath);

    // was disabled, now disabled (or no sdk selected) => not modified, do not care about other controls
    if (!myDartSupportEnabledInitial && (!myEnableDartSupportCheckBox.isSelected() || !sdkSelected)) return false;
    // was enabled, now disabled => modified
    if (myDartSupportEnabledInitial && !myEnableDartSupportCheckBox.isSelected()) return true;
    // was disabled, now enabled or was enabled, now enabled => need to check further

    final String initialSdkHomePath = mySdkInitial == null ? "" : mySdkInitial.getHomePath();
    if (sdkSelected && !sdkHomePath.equals(initialSdkHomePath)) return true;

    final String dartiumPath = FileUtilRt.toSystemIndependentName(myDartiumPathTextWithBrowse.getText().trim());
    final String dartiumPathInitial = myDartiumInitial == null ? null : myDartiumInitial.getPath();
    if (!dartiumPath.isEmpty() && new File(dartiumPath).exists() && !dartiumPath.equals(dartiumPathInitial)) return true;

    if (myDartiumInitial != null && !myDartiumSettingsCurrent.equals(myDartiumInitial.getSpecificSettings())) return true;

    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final Module[] selectedModules = myModulesCheckboxTreeTable.getCheckedNodes(Module.class);
      if (selectedModules.length != myModulesWithDartSdkLibAttachedInitial.size()) return true;

      for (final Module module : selectedModules) {
        if (!myModulesWithDartSdkLibAttachedInitial.contains(module)) return true;

        final String currentCustomPackagesPath = myModuleToCustomPackageRootCurrent.get(module);
        if (!Comparing.equal(PubspecYamlUtil.getCustomPackagesPathForModule(module), currentCustomPackagesPath)) return true;
      }
    }
    else {
      if (myDartSupportEnabledInitial != myEnableDartSupportCheckBox.isSelected()) return true;

      final Map.Entry<Module, String> entry = myModuleToCustomPackageRootCurrent.entrySet().iterator().next();
      final String customPackagesPath = myCustomPackageRootCheckBox.isSelected() ? entry.getValue() : null;
      if (!Comparing.equal(PubspecYamlUtil.getCustomPackagesPathForModule(entry.getKey()), customPackagesPath)) return true;
    }

    return false;
  }

  @Override
  public void reset() {
    myInReset = true;

    // remember initial state
    mySdkInitial = DartSdk.getGlobalDartSdk();
    myModulesWithDartSdkLibAttachedInitial.clear();

    if (mySdkInitial != null) {
      myModulesWithDartSdkLibAttachedInitial.addAll(
        DartSdkGlobalLibUtil.getModulesWithDartSdkGlobalLibAttached(myProject, mySdkInitial.getGlobalLibName()));
    }

    myDartSupportEnabledInitial = !myModulesWithDartSdkLibAttachedInitial.isEmpty();

    myDartiumInitial = DartiumUtil.getDartiumBrowser();
    myDartiumSettingsCurrent = new ChromeSettings();
    if (myDartiumInitial != null) {
      final BrowserSpecificSettings browserSpecificSettings = myDartiumInitial.getSpecificSettings();
      if (browserSpecificSettings instanceof ChromeSettings) {
        myDartiumSettingsCurrent = (ChromeSettings)browserSpecificSettings.clone();
      }
    }

    myModuleToCustomPackageRootCurrent.clear();
    for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
      myModuleToCustomPackageRootCurrent.put(module, PubspecYamlUtil.getCustomPackagesPathForModule(module));
    }

    // reset UI
    myEnableDartSupportCheckBox.setSelected(myDartSupportEnabledInitial);
    mySdkPathTextWithBrowse.setText(mySdkInitial == null ? "" : FileUtilRt.toSystemDependentName(mySdkInitial.getHomePath()));
    myDartiumPathTextWithBrowse.setText(myDartiumInitial == null
                                        ? ""
                                        : FileUtilRt.toSystemDependentName(StringUtil.notNullize(myDartiumInitial.getPath())));

    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final CheckedTreeNode rootNode = (CheckedTreeNode)myModulesCheckboxTreeTable.getTree().getModel().getRoot();
      rootNode.setChecked(false);
      final Enumeration children = rootNode.children();
      while (children.hasMoreElements()) {
        final CheckedTreeNode node = (CheckedTreeNode)children.nextElement();
        node.setChecked(myModulesWithDartSdkLibAttachedInitial.contains((Module)node.getUserObject()));
      }
    }
    else {
      final Map.Entry<Module, String> entry = myModuleToCustomPackageRootCurrent.entrySet().iterator().next();
      myCustomPackageRootTextWithBrowse.setText(FileUtilRt.toSystemDependentName(StringUtil.notNullize(entry.getValue())));
    }

    updateControlsEnabledState();
    updateErrorLabel();

    myInReset = false;
  }

  @Override
  public void apply() throws ConfigurationException {
    // similar to DartApplicationGenerator.setupSdkAndDartium()
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        if (myEnableDartSupportCheckBox.isSelected()) {
          final String sdkHomePath = FileUtilRt.toSystemIndependentName(mySdkPathTextWithBrowse.getText().trim());
          final String initialSdkHomePath = mySdkInitial == null ? "" : mySdkInitial.getHomePath();

          if (DartSdkUtil.isDartSdkHome(sdkHomePath)) {
            final String dartSdkGlobalLibName;

            if (mySdkInitial == null) {
              dartSdkGlobalLibName = DartSdkGlobalLibUtil.createDartSdkGlobalLib(myProject, sdkHomePath);
            }
            else {
              dartSdkGlobalLibName = mySdkInitial.getGlobalLibName();

              if (!sdkHomePath.equals(initialSdkHomePath)) {
                DartSdkGlobalLibUtil.updateDartSdkGlobalLib(myProject, dartSdkGlobalLibName, sdkHomePath);
              }
            }

            final Module[] modules = DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()
                                     ? myModulesCheckboxTreeTable.getCheckedNodes(Module.class)
                                     : ModuleManager.getInstance(myProject).getModules();
            DartSdkGlobalLibUtil.updateDependencyOnDartSdkGlobalLib(myProject, modules, dartSdkGlobalLibName);

            for (Module module : ModuleManager.getInstance(myProject).getModules()) {
              if (ArrayUtil.contains(module, modules)) {
                final String customPackagesPath =
                  DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()
                  ? myModuleToCustomPackageRootCurrent.get(module)
                  : myCustomPackageRootCheckBox.isSelected()
                    ? myModuleToCustomPackageRootCurrent.get(module) // the same as myCustomPackageRootTextWithBrowse.getText()
                    : null;
                PubspecYamlUtil.setCustomPackagesPathForModule(module, customPackagesPath);
              }
              else {
                PubspecYamlUtil.setCustomPackagesPathForModule(module, null);
              }
            }
          }

          final String dartiumPath = FileUtilRt.toSystemIndependentName(myDartiumPathTextWithBrowse.getText().trim());
          DartiumUtil.applyDartiumSettings(dartiumPath, myDartiumSettingsCurrent);
        }
        else {
          if (myModulesWithDartSdkLibAttachedInitial.size() > 0 && mySdkInitial != null) {
            DartSdkGlobalLibUtil.detachDartSdkGlobalLib(myModulesWithDartSdkLibAttachedInitial, mySdkInitial.getGlobalLibName());
          }

          for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
            PubspecYamlUtil.setCustomPackagesPathForModule(module, null);
          }
        }
      }
    });

    reset(); // because we rely on remembering initial state
  }

  @Override
  public void disposeUIResources() {
    mySdkInitial = null;
    myModulesWithDartSdkLibAttachedInitial.clear();
    myDartiumInitial = null;
    myDartiumSettingsCurrent = null;
    myModuleToCustomPackageRootCurrent.clear();
  }

  private void updateControlsEnabledState() {
    UIUtil.setEnabled(mySettingsPanel, myEnableDartSupportCheckBox.isSelected(), true);

    if (!DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final Map.Entry<Module, String> entry = myModuleToCustomPackageRootCurrent.entrySet().iterator().next();
      myCustomPackageRootCheckBox.setSelected(!StringUtil.isEmptyOrSpaces(entry.getValue()));
      myCustomPackageRootTextWithBrowse.setEnabled(myCustomPackageRootCheckBox.isSelected() && myCustomPackageRootCheckBox.isEnabled());
    }
  }

  private void updateErrorLabel() {
    final String message = getErrorMessage();
    myErrorLabel.setText(
      XmlStringUtil.wrapInHtml("<font color='#" + ColorUtil.toHex(JBColor.RED) + "'><left>" + message + "</left></font>"));
    myErrorLabel.setVisible(message != null);
  }

  @Nullable
  private String getErrorMessage() {
    if (!myEnableDartSupportCheckBox.isSelected()) {
      return null;
    }

    String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(mySdkPathTextWithBrowse.getText().trim());
    if (message != null) return message;

    message = DartiumUtil.getErrorMessageIfWrongDartiumPath(myDartiumPathTextWithBrowse.getText().trim());
    if (message != null) return message;

    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final Module[] modules = myModulesCheckboxTreeTable.getCheckedNodes(Module.class);
      if (modules.length == 0) {
        return DartBundle.message("warning.no.modules.selected.dart.support.will.be.disabled");
      }

      for (final Module module : modules) {
        final String customPackagesPath = myModuleToCustomPackageRootCurrent.get(module);
        if (!StringUtil.isEmpty(customPackagesPath)) {
          final VirtualFile folder = LocalFileSystem.getInstance().findFileByPath(customPackagesPath);
          if (folder == null || !folder.isDirectory()) {
            return DartBundle.message("warning.custom.package.root.not.found", FileUtil.toSystemDependentName(customPackagesPath));
          }
        }
      }
    }
    else {
      if (myCustomPackageRootCheckBox.isSelected()) {
        final String customPackagesPath = myCustomPackageRootTextWithBrowse.getText().trim();
        if (customPackagesPath.isEmpty()) {
          return DartBundle.message("warning.custom.package.root.not.specified");
        }

        final VirtualFile folder = LocalFileSystem.getInstance().findFileByPath(customPackagesPath);
        if (folder == null || !folder.isDirectory()) {
          return DartBundle.message("warning.custom.package.root.not.found", FileUtil.toSystemDependentName(customPackagesPath));
        }
      }
    }

    return null;
  }

  private void createUIComponents() {
    final CheckboxTree.CheckboxTreeCellRenderer checkboxTreeCellRenderer = new CheckboxTree.CheckboxTreeCellRenderer() {
      @Override
      public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (!(value instanceof CheckedTreeNode)) return;

        final boolean dartSupportEnabled = myEnableDartSupportCheckBox.isSelected();

        final CheckedTreeNode node = (CheckedTreeNode)value;
        final Object userObject = node.getUserObject();

        if (userObject instanceof Project) {
          if (!dartSupportEnabled) {
            //disabled state is also used as partially selected, that's why we do not change 'enabled' state if dartSupportEnabled
            getCheckbox().setEnabled(false);
          }
          getTextRenderer().setEnabled(dartSupportEnabled);

          getTextRenderer().append(DartBundle.message("project.0", ((Project)userObject).getName()));
        }
        else if (userObject instanceof Module) {
          getCheckbox().setEnabled(dartSupportEnabled);
          getTextRenderer().setEnabled(dartSupportEnabled);

          final Icon moduleIcon = ModuleType.get((Module)userObject).getIcon();
          getTextRenderer().setIcon(dartSupportEnabled ? moduleIcon : IconLoader.getDisabledIcon(moduleIcon));
          getTextRenderer().append(((Module)userObject).getName());
        }
      }
    };

    final TableCellRenderer customPackagesCellRenderer = new TableCellRenderer() {
      private final JBLabel myLabel = new JBLabel();

      public Component getTableCellRendererComponent(final JTable table,
                                                     final Object value,
                                                     final boolean isSelected,
                                                     final boolean hasFocus,
                                                     final int row,
                                                     final int column) {
        myLabel.setText(value instanceof String ? (String)value : "");
        return myLabel;
      }
    };

    final String columnName = DartBundle.message("custom.package.root");
    final ColumnInfo<CheckedTreeNode, String> CUSTOM_PACKAGES_COLUMN = new ColumnInfo<CheckedTreeNode, String>(columnName) {
      @Nullable
      public String valueOf(final CheckedTreeNode node) {
        final Object userObject = node.getUserObject();
        if (node.isChecked() && userObject instanceof Module) {
          return FileUtil.toSystemDependentName(StringUtil.notNullize(myModuleToCustomPackageRootCurrent.get(userObject)));
        }
        return null;
      }

      public boolean isCellEditable(final CheckedTreeNode node) {
        return node.isChecked() && node.getUserObject() instanceof Module;
      }

      @Nullable
      public TableCellRenderer getRenderer(final CheckedTreeNode node) {
        return customPackagesCellRenderer;
      }

      @Nullable
      public TableCellEditor getEditor(final CheckedTreeNode node) {
        return new LocalPathCellEditor();
      }

      public void setValue(final CheckedTreeNode node, final String value) {
        final Object userObject = node.getUserObject();
        if (userObject instanceof Module) {
          myModuleToCustomPackageRootCurrent.put((Module)userObject, FileUtil.toSystemIndependentName(value.trim()));
          updateErrorLabel();
        }
      }

      public int getWidth(final JTable table) {
        return new JLabel(getName()).getMinimumSize().width * 3 / 2;
      }
    };

    myModulesCheckboxTreeTable =
      new CheckboxTreeTable(null, checkboxTreeCellRenderer, new ColumnInfo[]{new TreeColumnInfo(""), CUSTOM_PACKAGES_COLUMN}) {
        protected boolean toggleNode(final CheckedTreeNode node) {
          final boolean result = super.toggleNode(node);
          updateErrorLabel();
          return result;
        }
      };

    myModulesCheckboxTreeTable.setRowHeight(myModulesCheckboxTreeTable.getRowHeight() + 2);

    final DefaultActionGroup group = new DefaultActionGroup();
    group.add(new RemoveCustomPackagesRootAction(myModulesCheckboxTreeTable));
    PopupHandler.installPopupHandler(myModulesCheckboxTreeTable, group, ActionPlaces.UNKNOWN, ActionManager.getInstance());
  }

  private class RemoveCustomPackagesRootAction extends AnAction {
    private final CheckboxTreeTable myTable;

    public RemoveCustomPackagesRootAction(final CheckboxTreeTable table) {
      super(DartBundle.message("remove.custom.package.root"));
      myTable = table;
    }

    public void update(final AnActionEvent e) {
      TableUtil.stopEditing(myTable);

      boolean enabled = false;
      for (Object item : myTable.getSelection()) {
        if (item instanceof CheckedTreeNode && ((CheckedTreeNode)item).isChecked()) {
          final Object userObject = ((CheckedTreeNode)item).getUserObject();
          final String customPackagesPath = userObject instanceof Module ? myModuleToCustomPackageRootCurrent.get(userObject) : null;
          if (!StringUtil.isEmptyOrSpaces(customPackagesPath)) {
            enabled = true;
            break;
          }
        }
      }

      e.getPresentation().setEnabled(enabled);
    }

    public void actionPerformed(final AnActionEvent e) {
      for (Object item : myTable.getSelection()) {
        final Object userObject = item instanceof CheckedTreeNode ? ((CheckedTreeNode)item).getUserObject() : null;
        if (userObject instanceof Module) {
          myModuleToCustomPackageRootCurrent.put((Module)userObject, null);
        }
      }

      final TableModel model = myTable.getModel();
      if (model instanceof AbstractTableModel) {
        ((AbstractTableModel)model).fireTableDataChanged();
      }

      updateErrorLabel();
    }
  }
}
