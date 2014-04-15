package com.jetbrains.lang.dart.sdk;

import com.intellij.icons.AllIcons;
import com.intellij.ide.browsers.BrowserSpecificSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.ui.impl.CheckboxTreeTable;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.ui.CellEditorComponentWithBrowseButton;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.LocalPathCellEditor;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;

public class DartConfigurable implements SearchableConfigurable {

  public static final String DART_SETTINGS_PAGE_NAME = DartBundle.message("dart.title");
  public static final String CUSTOM_PACKAGE_ROOTS_LIB_NAME = "Dart custom package roots";
  private static final String SEMICOLON = ";";

  private JPanel myMainPanel;
  private JBCheckBox myEnableDartSupportCheckBox;

  private JPanel mySettingsPanel;
  private TextFieldWithBrowseButton mySdkPathTextWithBrowse;
  private JBLabel myVersionLabel;

  private TextFieldWithBrowseButton myDartiumPathTextWithBrowse;
  private JButton myDartiumSettingsButton;

  private JPanel myCustomPackageRootsPanel;
  private JBCheckBox myCustomPackageRootsCheckBox;
  private TextFieldWithBrowseButton myCustomPackageRootsTextWithBrowse;

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
  private final @NotNull Map<Module, List<String>> myModuleToCustomPackageRootsCurrent = new THashMap<Module, List<String>>();

  public DartConfigurable(final @NotNull Project project) {
    myProject = project;
    initEnableDartSupportCheckBox();
    initDartSdkAndDartiumControls();
    initCustomPackageRootsPanel();
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

  private void initCustomPackageRootsPanel() {
    if (DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      myCustomPackageRootsPanel.setVisible(false);
      return;
    }

    final Insets margin = myCustomPackageRootsCheckBox.getMargin();
    myCustomPackageRootsCheckBox.setMargin(new Insets(margin.top, -2, margin.bottom, margin.right));

    myCustomPackageRootsCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myCustomPackageRootsTextWithBrowse.setEnabled(myCustomPackageRootsCheckBox.isSelected());
        updateErrorLabel();

        if (myCustomPackageRootsCheckBox.isSelected() && myCustomPackageRootsCheckBox.isEnabled()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myCustomPackageRootsTextWithBrowse.getTextField(), true);
        }
      }
    });

    final ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> bfListener =
      new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>(DartBundle.message("select.custom.package.roots"),
                                                                           null,
                                                                           myCustomPackageRootsTextWithBrowse,
                                                                           myProject,
                                                                           // this descriptor is not used because overridden in selectCustomPackageRoots()
                                                                           FileChooserDescriptorFactory.createMultipleFoldersDescriptor(),
                                                                           TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT) {
        public void actionPerformed(final ActionEvent e) {
          selectCustomPackageRoots(myCustomPackageRootsTextWithBrowse.getTextField());
        }
      };

    myCustomPackageRootsTextWithBrowse.addBrowseFolderListener(myProject, bfListener);

    myCustomPackageRootsTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        final Module module = myModuleToCustomPackageRootsCurrent.keySet().iterator().next();
        final String customPackageRoots = FileUtil.toSystemIndependentName(myCustomPackageRootsTextWithBrowse.getText().trim());
        myModuleToCustomPackageRootsCurrent.put(module, StringUtil.split(customPackageRoots, SEMICOLON));
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
        return module1.getName().toLowerCase(Locale.US).compareTo(module2.getName().toLowerCase(Locale.US));
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

        final List<String> currentPackageRootPaths = myModuleToCustomPackageRootsCurrent.get(module);
        if (!Comparing.equal(getCustomPackageRootPaths(module), currentPackageRootPaths)) return true;
      }
    }
    else {
      if (myDartSupportEnabledInitial != myEnableDartSupportCheckBox.isSelected()) return true;

      final Map.Entry<Module, List<String>> entry = myModuleToCustomPackageRootsCurrent.entrySet().iterator().next();
      final List<String> currentPackageRootPaths = myCustomPackageRootsCheckBox.isSelected() ? entry.getValue()
                                                                                             : Collections.<String>emptyList();
      if (!currentPackageRootPaths.equals(getCustomPackageRootPaths(entry.getKey()))) return true;
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

    myModuleToCustomPackageRootsCurrent.clear();
    for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
      myModuleToCustomPackageRootsCurrent.put(module, getCustomPackageRootPaths(module));
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
      final List<String> paths = myModuleToCustomPackageRootsCurrent.entrySet().iterator().next().getValue();
      myCustomPackageRootsTextWithBrowse.setText(StringUtil.join(paths, new Function<String, String>() {
        public String fun(final String s) {
          return FileUtil.toSystemDependentName(s);
        }
      }, SEMICOLON));
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
                final List<String> customPackageRoots =
                  DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport() || myCustomPackageRootsCheckBox.isSelected()
                  ? myModuleToCustomPackageRootsCurrent.get(module)
                  : Collections.<String>emptyList();
                setCustomPackageRootPaths(module, customPackageRoots);
              }
              else {
                setCustomPackageRootPaths(module, Collections.<String>emptyList());
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
            setCustomPackageRootPaths(module, Collections.<String>emptyList());
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
    myModuleToCustomPackageRootsCurrent.clear();
  }

  private void updateControlsEnabledState() {
    UIUtil.setEnabled(mySettingsPanel, myEnableDartSupportCheckBox.isSelected(), true);

    if (!DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()) {
      final List<String> paths = myModuleToCustomPackageRootsCurrent.entrySet().iterator().next().getValue();
      myCustomPackageRootsCheckBox.setSelected(!paths.isEmpty());
      myCustomPackageRootsTextWithBrowse.setEnabled(myCustomPackageRootsCheckBox.isSelected() && myCustomPackageRootsCheckBox.isEnabled());
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
        final List<String> customPackageRoots = myModuleToCustomPackageRootsCurrent.get(module);
        final String customPackagePathError = getErrorMessageForCustomPackageRoots(customPackageRoots);
        if (customPackagePathError != null) {
          return customPackagePathError;
        }
      }
    }
    else {
      if (myCustomPackageRootsCheckBox.isSelected()) {
        final List<String> customPackageRoots = myModuleToCustomPackageRootsCurrent.entrySet().iterator().next().getValue();
        if (customPackageRoots.isEmpty()) {
          return DartBundle.message("warning.custom.package.root.not.specified");
        }

        final String customPackagePathError = getErrorMessageForCustomPackageRoots(customPackageRoots);
        if (customPackagePathError != null) {
          return customPackagePathError;
        }
      }
    }

    return null;
  }

  private static String getErrorMessageForCustomPackageRoots(final @NotNull List<String> customPackageRootPaths) {
    for (String path : customPackageRootPaths) {
      final VirtualFile folder = LocalFileSystem.getInstance().findFileByPath(path);
      if (folder == null || !folder.isDirectory()) {
        return DartBundle.message("warning.custom.package.root.not.found", FileUtil.toSystemDependentName(path));
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

    final TableCellRenderer customPackageRootsCellRenderer = new TableCellRenderer() {
      private final JBLabel myLabel = new JBLabel();
      private final TextFieldWithBrowseButton myTextWithBrowse = new TextFieldWithBrowseButton() {
        public void setOpaque(final boolean isOpaque) {
          // never make this renderer opaque in order not to have cell selection background between text field and browse button
        }
      };

      public Component getTableCellRendererComponent(final JTable table,
                                                     final Object value,
                                                     final boolean isSelected,
                                                     final boolean hasFocus,
                                                     final int row,
                                                     final int column) {

        if (value instanceof List) {
          //noinspection unchecked
          final List<String> paths = (List<String>)value;

          final String text = StringUtil.join(paths, new Function<String, String>() {
            public String fun(final String s) {
              return FileUtil.toSystemDependentName(s);
            }
          }, SEMICOLON);

          if (isSelected) {
            myTextWithBrowse.setText(text);
            return myTextWithBrowse;
          }
          else {
            myLabel.setText(text);
            return myLabel;
          }
        }
        else {
          myLabel.setText("");
          return myLabel;
        }
      }
    };

    final LocalPathCellEditor customPackageRootsEditor = new LocalPathCellEditor(myProject) {
      public Object getCellEditorValue() {
        return StringUtil.split(FileUtil.toSystemIndependentName(myComponent.getChildComponent().getText()), SEMICOLON);
      }

      @Override
      public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, int column) {
        myComponent = new CellEditorComponentWithBrowseButton<JTextField>(new TextFieldWithBrowseButton(createActionListener(table)), this);
        //noinspection unchecked
        final String text = StringUtil.join((List<String>)value, new Function<String, String>() {
          public String fun(final String s) {
            return FileUtil.toSystemDependentName(s);
          }
        }, SEMICOLON);

        myComponent.getChildComponent().setText(text);
        return myComponent;
      }

      protected ActionListener createActionListener(final JTable table) {
        return new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            selectCustomPackageRoots(myComponent.getChildComponent());
          }
        };
      }
    }/*.normalizePath(true)*/;

    final String columnName = DartBundle.message("custom.package.roots");
    final ColumnInfo<CheckedTreeNode, List<String>> CUSTOM_PACKAGE_ROOTS_COLUMN =
      new ColumnInfo<CheckedTreeNode, List<String>>(columnName) {
        @Nullable
        public List<String> valueOf(final CheckedTreeNode node) {
          final Object userObject = node.getUserObject();
          if (node.isChecked() && userObject instanceof Module) {
            return myModuleToCustomPackageRootsCurrent.get(userObject);
          }
          return null;
        }

        public boolean isCellEditable(final CheckedTreeNode node) {
          return node.isChecked() && node.getUserObject() instanceof Module;
        }

        @Nullable
        public TableCellRenderer getRenderer(final CheckedTreeNode node) {
          return customPackageRootsCellRenderer;
        }

        @Nullable
        public TableCellEditor getEditor(final CheckedTreeNode node) {
          return customPackageRootsEditor;
        }

        public void setValue(final CheckedTreeNode node, final List<String> value) {
          final Object userObject = node.getUserObject();
          if (userObject instanceof Module) {
            myModuleToCustomPackageRootsCurrent.put((Module)userObject, value);
            updateErrorLabel();
          }
        }

        public int getWidth(final JTable table) {
          return new JLabel(getName()).getMinimumSize().width * 3 / 2;
        }
      };

    myModulesCheckboxTreeTable =
      new CheckboxTreeTable(null, checkboxTreeCellRenderer, new ColumnInfo[]{new TreeColumnInfo(""), CUSTOM_PACKAGE_ROOTS_COLUMN}) {
        protected boolean toggleNode(final CheckedTreeNode node) {
          final boolean result = super.toggleNode(node);
          updateErrorLabel();
          return result;
        }
      };

    myModulesCheckboxTreeTable.setRowHeight(myModulesCheckboxTreeTable.getRowHeight() + 2);

    myModulesCheckboxTreeTable.getTree().addTreeWillExpandListener(new TreeWillExpandListener() {
      public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {

      }

      public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException {
        throw new ExpandVetoException(event);
      }
    });

    final DefaultActionGroup group = new DefaultActionGroup();
    group.add(new RemoveCustomPackageRootsAction(myModulesCheckboxTreeTable));
    PopupHandler.installPopupHandler(myModulesCheckboxTreeTable, group, ActionPlaces.UNKNOWN, ActionManager.getInstance());
  }

  private void selectCustomPackageRoots(final JTextField textField) {
    final String currentCustomPackageRoots = textField.getText().trim();
    final int separatorIndex = currentCustomPackageRoots.indexOf(SEMICOLON);
    final String path = separatorIndex < 0 ? currentCustomPackageRoots
                                           : currentCustomPackageRoots.substring(0, separatorIndex).trim();
    final VirtualFile initialFile = StringUtil.isNotEmpty(path) ? LocalFileSystem.getInstance().findFileByPath(path) : null;

    final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createMultipleFoldersDescriptor();
    descriptor.setTitle(DartBundle.message("select.custom.package.roots"));

    FileChooser.chooseFiles(descriptor, myProject, initialFile, new Consumer<List<VirtualFile>>() {
      public void consume(final List<VirtualFile> files) {
        final StringBuilder buffer = new StringBuilder();
        for (VirtualFile file : files) {
          if (buffer.length() > 0) {
            buffer.append(SEMICOLON);
          }
          buffer.append(file.getPath());
        }

        textField.setText(FileUtil.toSystemDependentName(buffer.toString()));
      }
    });
  }

  private class RemoveCustomPackageRootsAction extends AnAction {
    private final CheckboxTreeTable myTable;

    public RemoveCustomPackageRootsAction(final CheckboxTreeTable table) {
      super(DartBundle.message("remove.custom.package.roots"));
      myTable = table;
    }

    public void update(final AnActionEvent e) {
      TableUtil.stopEditing(myTable);

      boolean enabled = false;
      for (Object item : myTable.getSelection()) {
        if (item instanceof CheckedTreeNode && ((CheckedTreeNode)item).isChecked()) {
          final Object userObject = ((CheckedTreeNode)item).getUserObject();
          if (userObject instanceof Module && !myModuleToCustomPackageRootsCurrent.get(userObject).isEmpty()) {
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
          myModuleToCustomPackageRootsCurrent.put((Module)userObject, Collections.<String>emptyList());
        }
      }

      final TableModel model = myTable.getModel();
      if (model instanceof AbstractTableModel) {
        ((AbstractTableModel)model).fireTableDataChanged();
      }

      updateErrorLabel();
    }
  }

  @NotNull
  private static List<String> getCustomPackageRootPaths(final @NotNull Module module) {
    for (OrderEntry entry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (entry instanceof LibraryOrderEntry && CUSTOM_PACKAGE_ROOTS_LIB_NAME.equals(((LibraryOrderEntry)entry).getLibraryName())) {
        final String[] urls = ((LibraryOrderEntry)entry).getRootUrls(OrderRootType.CLASSES);
        final List<String> paths = new ArrayList<String>(urls.length);
        for (final String url : urls) {
          paths.add(VfsUtilCore.urlToPath(url));
        }
        return paths;
      }
    }

    return Collections.emptyList();
  }

  private static void setCustomPackageRootPaths(final @NotNull Module module, final @NotNull List<String> paths) {
    if (Comparing.haveEqualElements(paths, getCustomPackageRootPaths(module))) return;

    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      for (final OrderEntry entry : modifiableModel.getOrderEntries()) {
        if (entry instanceof LibraryOrderEntry && CUSTOM_PACKAGE_ROOTS_LIB_NAME.equals(((LibraryOrderEntry)entry).getLibraryName())) {
          modifiableModel.removeOrderEntry(entry);
        }
      }

      if (paths.size() > 0) {
        final Library library = modifiableModel.getModuleLibraryTable().createLibrary(CUSTOM_PACKAGE_ROOTS_LIB_NAME);
        final Library.ModifiableModel libModel = library.getModifiableModel();
        for (String path : paths) {
          libModel.addRoot(VfsUtilCore.pathToUrl(path), OrderRootType.CLASSES);
        }
        libModel.commit();
      }

      modifiableModel.commit();
    }
    catch (Exception e) {
      if (!modifiableModel.isDisposed()) modifiableModel.dispose();
    }
  }
}
