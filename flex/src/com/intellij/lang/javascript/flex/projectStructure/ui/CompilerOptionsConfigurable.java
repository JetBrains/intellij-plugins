// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.compiler.options.CompilerUIConfigurable;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.CompilerOptionInfo;
import com.intellij.flex.model.bc.ValueSource;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptionsListener;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableCompilerOptions;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.ui.navigation.Place;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.EventDispatcher;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

public final class CompilerOptionsConfigurable extends NamedConfigurable<CompilerOptions> implements Place.Navigator {
  public static final String CONDITIONAL_COMPILER_DEFINITION_NAME = "FlexCompilerOptions.ConditionalCompilerDefinitionName";

  public enum Location {
    AdditionalConfigFile("additional-config-file"),
    FilesToIncludeInSwc("files-to-include-in-swc"),
    ConditionalCompilerDefinition("doesn't matter");

    public final String errorId;

    Location(final String errorId) {
      this.errorId = errorId;
    }
  }

  private JPanel myMainPanel;

  private TreeTable myTreeTable;
  private NonFocusableCheckBox myShowAllOptionsCheckBox;
  private JLabel myInheritProjectDefaultsLegend;
  private JLabel myInheritModuleDefaultsLegend;
  private JButton myProjectDefaultsButton;
  private JButton myModuleDefaultsButton;

  private JPanel myResourcesPanel;
  private JCheckBox myCopyResourceFilesCheckBox;
  private JRadioButton myCopyAllResourcesRadioButton;
  private JRadioButton myRespectResourcePatternsRadioButton;
  private HoverHyperlinkLabel myResourcePatternsHyperlink;

  private JPanel myIncludeInSWCPanel;
  private TextFieldWithBrowseButton myIncludeInSWCField;
  private Collection<String> myFilesToIncludeInSWC;

  private JLabel myConfigFileLabel;
  private TextFieldWithBrowseButton myConfigFileTextWithBrowse;
  private JLabel myInheritedOptionsLabel;
  private JTextField myInheritedOptionsField;
  private JLabel myAdditionalOptionsLabel;
  private RawCommandLineEditor myAdditionalOptionsField;
  private JLabel myNoteLabel;

  private final Mode myMode;
  private final Module myModule;
  private final BuildConfigurationNature myNature;
  private final DependenciesConfigurable myDependenciesConfigurable;
  private final Project myProject;
  private final String myName;
  private final FlexBuildConfigurationManager myBCManager;
  private final FlexProjectLevelCompilerOptionsHolder myProjectLevelOptionsHolder;
  private final ModifiableCompilerOptions myModel;
  private final Map<String, String> myCurrentOptions;
  private boolean myMapModified;

  private final EventDispatcher<UserActivityListener> myUserActivityDispatcher;
  private boolean myFreeze;

  private final Collection<OptionsListener> myListeners = ContainerUtil.createLockFreeCopyOnWriteList();
  private final Disposable myDisposable = Disposer.newDisposable();

  private static final String UNKNOWN_SDK_VERSION = "100";

  private enum Mode {BC, Module, Project}

  public interface OptionsListener extends EventListener {
    void configFileChanged(String additionalConfigFilePath);

    void additionalOptionsChanged(String additionalOptions);
  }


  public CompilerOptionsConfigurable(final Module module,
                                     final BuildConfigurationNature nature,
                                     final DependenciesConfigurable dependenciesConfigurable,
                                     final ModifiableCompilerOptions model) {
    this(Mode.BC, module, module.getProject(), nature, dependenciesConfigurable, model);

    dependenciesConfigurable.addSdkChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        updateTreeTable();
      }
    });
  }

  private CompilerOptionsConfigurable(final Mode mode,
                                      final Module module,
                                      final Project project,
                                      final BuildConfigurationNature nature,
                                      final DependenciesConfigurable dependenciesConfigurable,
                                      final ModifiableCompilerOptions model) {
    myMode = mode;
    myModule = module;
    myProject = project;
    myNature = nature;
    myDependenciesConfigurable = dependenciesConfigurable;
    myName = myMode == Mode.BC
             ? getTabName()
             : myMode == Mode.Module
               ? FlexBundle.message("default.compiler.options.for.module.title", module.getName())
               : FlexBundle.message("default.compiler.options.for.project.title", project.getName());
    myBCManager = myMode == Mode.BC ? FlexBuildConfigurationManager.getInstance(module) : null;
    myProjectLevelOptionsHolder = FlexProjectLevelCompilerOptionsHolder.getInstance(project);
    myModel = model;
    myCurrentOptions = new THashMap<>();
    myFilesToIncludeInSWC = Collections.emptyList();

    myShowAllOptionsCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateTreeTable();
      }
    });

    myInheritModuleDefaultsLegend.setVisible(myMode == Mode.BC);
    myInheritProjectDefaultsLegend.setVisible(myMode == Mode.BC || myMode == Mode.Module);

    myResourcesPanel.setVisible(myMode == Mode.BC && BCUtils.canHaveResourceFiles(myNature));
    myCopyResourceFilesCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateResourcesControls();
      }
    });
    myResourcePatternsHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      protected void hyperlinkActivated(final HyperlinkEvent e) {
        ShowSettingsUtil.getInstance().editConfigurable(project, new CompilerUIConfigurable(module.getProject()));
      }
    });

    myIncludeInSWCPanel.setVisible(myMode == Mode.BC && myNature.isLib());
    myIncludeInSWCField.getTextField().setEditable(false);
    myIncludeInSWCField.setButtonIcon(AllIcons.Actions.ShowViewer);
    myIncludeInSWCField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final List<StringBuilder> value = new ArrayList<>();
        for (String path : myFilesToIncludeInSWC) {
          value.add(new StringBuilder(path));
        }
        final RepeatableValueDialog dialog =
          new RepeatableValueDialog(module.getProject(), FlexBundle.message("items.to.include.in.swc.dialog.title"), value,
                                    CompilerOptionInfo.INCLUDE_FILE_INFO_FOR_UI);
        if (dialog.showAndGet()) {
          final List<StringBuilder> newValue = dialog.getCurrentList();
          myFilesToIncludeInSWC = new ArrayList<>(newValue.size());
          for (StringBuilder path : newValue) {
            myFilesToIncludeInSWC.add(path.toString());
          }
          updateFilesToIncludeInSWCText();
        }
      }
    });


    initButtonsAndAdditionalOptions();

    // seems we don't need it for small amount of options
    myShowAllOptionsCheckBox.setSelected(true);
    myShowAllOptionsCheckBox.setVisible(false);

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.register(myMainPanel);
    myUserActivityDispatcher = EventDispatcher.create(UserActivityListener.class);
    watcher.addUserActivityListener(new UserActivityListener() {
      @Override
      public void stateChanged() {
        if (myFreeze) {
          return;
        }
        myUserActivityDispatcher.getMulticaster().stateChanged();
      }
    }, myDisposable);
  }

  public void addUserActivityListener(final UserActivityListener listener, final Disposable disposable) {
    myUserActivityDispatcher.addListener(listener, disposable);
  }

  public void removeUserActivityListeners() {
    for (UserActivityListener listener : myUserActivityDispatcher.getListeners()) {
      myUserActivityDispatcher.removeListener(listener);
    }
  }

  private void updateFilesToIncludeInSWCText() {
    final String s = StringUtil.join(myFilesToIncludeInSWC, path -> PathUtil.getFileName(path), ", ");
    myIncludeInSWCField.setText(s);
  }

  private void initButtonsAndAdditionalOptions() {
    if (myMode == Mode.BC || myMode == Mode.Module) {
      final CompilerOptionsListener optionsListener = new CompilerOptionsListener() {
        @Override
        public void optionsInTableChanged() {
          updateTreeTable();
        }

        @Override
        public void additionalOptionsChanged() {
          updateAdditionalOptionsControls();
        }
      };

      if (myMode == Mode.BC) {
        myBCManager.getModuleLevelCompilerOptions().addOptionsListener(optionsListener, myDisposable);
      }
      myProjectLevelOptionsHolder.getProjectLevelCompilerOptions().addOptionsListener(optionsListener, myDisposable);
    }

    final ActionListener projectDefaultsListener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        ModifiableCompilerOptions compilerOptions = myProjectLevelOptionsHolder.getProjectLevelCompilerOptions();
        final CompilerOptionsConfigurable configurable =
          new CompilerOptionsConfigurable(Mode.Project, null, myProject, myNature, myDependenciesConfigurable, compilerOptions);
        ShowSettingsUtil.getInstance().editConfigurable(myProject, configurable);
      }
    };

    final ActionListener moduleDefaultsListener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        ModifiableCompilerOptions compilerOptions = myBCManager.getModuleLevelCompilerOptions();
        final CompilerOptionsConfigurable configurable =
          new CompilerOptionsConfigurable(Mode.Module, myModule, myProject, myNature, myDependenciesConfigurable, compilerOptions);
        ShowSettingsUtil.getInstance().editConfigurable(myProject, configurable);
      }
    };

    myConfigFileTextWithBrowse.addBrowseFolderListener(null, null, myProject, FlexUtils.createFileChooserDescriptor("xml"));
    myConfigFileTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull final DocumentEvent e) {
        updateAdditionalOptionsControls();
        fireConfigFileChanged();
      }
    });
    myConfigFileLabel.setVisible(myMode == Mode.BC);
    myConfigFileTextWithBrowse.setVisible(myMode == Mode.BC);

    myInheritedOptionsLabel.setVisible(myMode == Mode.BC || myMode == Mode.Module);
    myInheritedOptionsField.setVisible(myMode == Mode.BC || myMode == Mode.Module);

    final String labelText = myMode == Mode.BC ? "Additional compiler options:"
                                               : myMode == Mode.Module
                                                 ? "Default options for module:"
                                                 : "Default options for project:";
    myAdditionalOptionsLabel.setText(labelText);
    myAdditionalOptionsField.setDialogCaption(StringUtil.capitalizeWords(labelText, true));
    myAdditionalOptionsField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull final DocumentEvent e) {
        updateAdditionalOptionsControls();
        fireAdditionalOptionsChanged();
      }
    });

    myNoteLabel.setIcon(UIUtil.getBalloonInformationIcon());

    myProjectDefaultsButton.addActionListener(projectDefaultsListener);
    myProjectDefaultsButton.setVisible(myMode == Mode.BC || myMode == Mode.Module);

    myModuleDefaultsButton.addActionListener(moduleDefaultsListener);
    myModuleDefaultsButton.setVisible(myMode == Mode.BC);
  }

  public void addAdditionalOptionsListener(final OptionsListener listener) {
    myListeners.add(listener);
  }

  private void fireConfigFileChanged() {
    for (OptionsListener listener : myListeners) {
      listener.configFileChanged(FileUtil.toSystemIndependentName(myConfigFileTextWithBrowse.getText().trim()));
    }
  }

  private void fireAdditionalOptionsChanged() {
    for (OptionsListener listener : myListeners) {
      listener.additionalOptionsChanged(myAdditionalOptionsField.getText().trim());
    }
  }

  @Override
  @Nls
  public String getDisplayName() {
    return myName;
  }

  @Override
  public void setDisplayName(final String name) {
  }

  @Override
  public String getBannerSlogan() {
    return getDisplayName();
  }

  @Override
  public CompilerOptions getEditableObject() {
    return myModel;
  }

  @Override
  public String getHelpTopic() {
    return "BuildConfigurationPage.CompilerOptions";
  }

  @Override
  public JComponent createOptionsPanel() {
    //TreeUtil.expandAll(myTreeTable.getTree());
    return myMainPanel;
  }

  @Override
  public boolean isModified() {
    if (myMapModified) return true;
    if (myModel.getResourceFilesMode() != getResourceFilesMode()) return true;
    if (!myModel.getFilesToIncludeInSWC().equals(myFilesToIncludeInSWC)) return true;
    if (!myModel.getAdditionalConfigFilePath().equals(FileUtil.toSystemIndependentName(myConfigFileTextWithBrowse.getText().trim()))) {
      return true;
    }
    if (!myModel.getAdditionalOptions().equals(myAdditionalOptionsField.getText().trim())) return true;
    return false;
  }

  private CompilerOptions.ResourceFilesMode getResourceFilesMode() {
    return !myCopyResourceFilesCheckBox.isVisible() || !myCopyResourceFilesCheckBox.isSelected()
           ? CompilerOptions.ResourceFilesMode.None
           : myCopyAllResourcesRadioButton.isSelected()
             ? CompilerOptions.ResourceFilesMode.All
             : CompilerOptions.ResourceFilesMode.ResourcePatterns;
  }

  @Override
  public void apply() throws ConfigurationException {
    applyTo(myModel);
  }

  public void applyTo(final ModifiableCompilerOptions compilerOptions) {
    TableUtil.stopEditing(myTreeTable);
    compilerOptions.setAllOptions(myCurrentOptions);
    if (compilerOptions == myModel) {
      myMapModified = false;
    }

    compilerOptions.setResourceFilesMode(getResourceFilesMode());
    compilerOptions.setFilesToIncludeInSWC(myFilesToIncludeInSWC);
    compilerOptions.setAdditionalConfigFilePath(FileUtil.toSystemIndependentName(myConfigFileTextWithBrowse.getText().trim()));
    compilerOptions.setAdditionalOptions(myAdditionalOptionsField.getText().trim());
  }

  @Override
  public void reset() {
    myFreeze = true;
    try {
      myCurrentOptions.clear();
      myCurrentOptions.putAll(myModel.getAllOptions());
      myMapModified = false;
      updateTreeTable();

      final CompilerOptions.ResourceFilesMode mode = myModel.getResourceFilesMode();
      myCopyResourceFilesCheckBox.setSelected(mode != CompilerOptions.ResourceFilesMode.None);
      myCopyAllResourcesRadioButton.setSelected(mode == CompilerOptions.ResourceFilesMode.None || mode == CompilerOptions.ResourceFilesMode.All);
      myRespectResourcePatternsRadioButton.setSelected(mode == CompilerOptions.ResourceFilesMode.ResourcePatterns);
      updateResourcesControls();

      myFilesToIncludeInSWC = myModel.getFilesToIncludeInSWC();
      updateFilesToIncludeInSWCText();

      myConfigFileTextWithBrowse.setText(FileUtil.toSystemDependentName(myModel.getAdditionalConfigFilePath()));
      myAdditionalOptionsField.setText(myModel.getAdditionalOptions());
      updateAdditionalOptionsControls();
    }
    finally {
      myFreeze = false;
    }
  }

  @Override
  public void disposeUIResources() {
    myListeners.clear();
    Disposer.dispose(myDisposable);
  }

  private void createUIComponents() {
    myTreeTable = createTreeTable();
    myResourcePatternsHyperlink = new HoverHyperlinkLabel("resource patterns");
  }

  private TreeTable createTreeTable() {
    final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    final TreeTableModel model = new ListTreeTableModel(rootNode, createColumns());

    final TreeTable treeTable = new TreeTable(model);

    treeTable.getColumnModel().getColumn(1).setCellRenderer(createValueRenderer());
    treeTable.getColumnModel().getColumn(1).setCellEditor(createValueEditor());

    final TreeTableTree tree = treeTable.getTree();

    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(createTreeCellRenderer());
    treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    treeTable.setRowHeight(new JTextField("Fake").getPreferredSize().height + 3);
    //treeTable.setTableHeader(null);

    final DefaultActionGroup group = new DefaultActionGroup();
    group.add(new RestoreDefaultValueAction(tree));
    PopupHandler.installPopupMenu(treeTable, group, "FlexCompilerOptionsTreePopup");

    new TreeTableSpeedSearch(treeTable, o -> {
      final Object userObject = ((DefaultMutableTreeNode)o.getLastPathComponent()).getUserObject();
      return userObject instanceof CompilerOptionInfo ? ((CompilerOptionInfo)userObject).DISPLAY_NAME : "";
    }).setComparator(new SpeedSearchComparator(false));

    return treeTable;
  }

  private TreeCellRenderer createTreeCellRenderer() {
    return new TreeCellRenderer() {
      private final JLabel myLabel = new JLabel();

      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                    boolean leaf, int row, boolean hasFocus) {
        final Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
        if (!(userObject instanceof CompilerOptionInfo)) {
          // invisible root node
          return myLabel;
        }

        final CompilerOptionInfo info = (CompilerOptionInfo)userObject;
        myLabel.setText(info.DISPLAY_NAME);

        final ValueSource valueSource = getValueAndSource(info).second;
        renderAccordingToSource(myLabel, valueSource, selected);

        myLabel.setForeground(selected ? UIUtil.getTableSelectionForeground() : UIUtil.getTableForeground());

        return myLabel;
      }
    };
  }

  private static void renderAccordingToSource(final Component component, final ValueSource valueSource, final boolean selected) {
    final int boldOrPlain = valueSource == ValueSource.BC || valueSource == ValueSource.ProjectDefault ? Font.BOLD : Font.PLAIN;
    component.setFont(component.getFont().deriveFont(boldOrPlain));
    component.setEnabled(selected || valueSource == ValueSource.BC || valueSource == ValueSource.ModuleDefault);
  }

  private TableCellRenderer createValueRenderer() {
    return new TableCellRenderer() {
      private final JLabel myLabel = new JLabel();
      private final JCheckBox myCheckBox = new JCheckBox();
      private final ComponentWithBrowseButton<JLabel> myLabelWithBrowse = new ComponentWithBrowseButton<>(new JLabel(), null);

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        if (!(value instanceof CompilerOptionInfo)) {
          // invisible root node or group node
          myLabel.setText("");
          return myLabel;
        }

        final CompilerOptionInfo info = (CompilerOptionInfo)value;
        final Pair<String, ValueSource> valueAndSource = getValueAndSource(info);

        switch (info.TYPE) {
          case Boolean:
            myCheckBox.setBackground(table.getBackground());
            //myCheckBox.setForeground(UIUtil.getTableForeground());
            //myCheckBox.setEnabled(moduleDefault || fromConfigFile || custom);
            myCheckBox.setSelected("true".equalsIgnoreCase(valueAndSource.first));
            return myCheckBox;
          case String:
          case Int:
          case List:
          case IncludeClasses:
          case IncludeFiles:
            myLabel.setBackground(table.getBackground());
            myLabel.setText(getPresentableSummary(valueAndSource.first, info));
            renderAccordingToSource(myLabel, valueAndSource.second, false);
            return myLabel;
          case File:
            final JLabel label = myLabelWithBrowse.getChildComponent();
            myLabelWithBrowse.setBackground(table.getBackground());
            label.setText(FileUtil.toSystemDependentName(valueAndSource.first));
            renderAccordingToSource(label, valueAndSource.second, false);
            return myLabelWithBrowse;
          case Group:
          default:
            assert false;
            return null;
        }
      }
    };
  }

  private static String getPresentableSummary(final String rawValue, final CompilerOptionInfo info) {
    if (info.TYPE == CompilerOptionInfo.OptionType.List) {
      if (info.LIST_ELEMENTS.length == 1) {
        final String fixedSlashes = info.LIST_ELEMENTS[0].LIST_ELEMENT_TYPE == CompilerOptionInfo.ListElementType.File
                                    ? FileUtil.toSystemDependentName(rawValue)
                                    : rawValue;
        return fixedSlashes.replace(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR, ", ");
      }
      if ("compiler.define".equals(info.ID)) {
        return rawValue.replace(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR, ", ")
          .replace(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, "=");
      }
      final StringBuilder b = new StringBuilder();
      for (String entry : StringUtil.split(rawValue, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR)) {
        if (b.length() > 0) b.append(", ");
        b.append(entry, 0, entry.indexOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR));
      }

      return b.toString();
    }
    return rawValue;
  }

  private TableCellEditor createValueEditor() {
    return new AbstractTableCellEditor() {
      //private CellEditorComponentWithBrowseButton<JTextField> myTextWithBrowse;
      //private LocalPathCellEditor myLocalPathCellEditor;
      private final JTextField myTextField = new JTextField();
      private final TextFieldWithBrowseButton myTextWithBrowse = new TextFieldWithBrowseButton();
      private final RepeatableValueEditor myRepeatableValueEditor = new RepeatableValueEditor(myProject);
      private final ExtensionAwareFileChooserDescriptor myFileChooserDescriptor = new ExtensionAwareFileChooserDescriptor();
      private final JCheckBox myCheckBox = new JCheckBox();

      {
        myTextWithBrowse.addBrowseFolderListener(null, null, myProject, myFileChooserDescriptor);

        myCheckBox.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            TableUtil.stopEditing(myTreeTable); // apply new check box state immediately
          }
        });
      }

      private Component myCurrentEditor;

      @Override
      public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, int column) {
        assert value instanceof CompilerOptionInfo;

        final CompilerOptionInfo info = (CompilerOptionInfo)value;

        final Sdk sdk = myDependenciesConfigurable.getCurrentSdk();
        final String sdkHome = sdk == null || sdk.getSdkType() == FlexmojosSdkType.getInstance() ? null : sdk.getHomePath();

        final String optionValue = sdkHome == null
                                   ? getValueAndSource(info).first
                                   : getValueAndSource(info).first.replace(CompilerOptionInfo.FLEX_SDK_MACRO, sdkHome);


        switch (info.TYPE) {
          case Boolean:
            myCheckBox.setBackground(table.getBackground());
            myCheckBox.setSelected("true".equalsIgnoreCase(optionValue));
            myCurrentEditor = myCheckBox;
            break;
          case List:
            myRepeatableValueEditor.setInfoAndValue(info, optionValue);
            myCurrentEditor = myRepeatableValueEditor;
            break;
          case String:
          case Int:
          case IncludeClasses:
          case IncludeFiles:
            myTextField.setText(optionValue);
            myCurrentEditor = myTextField;
            break;
          case File:
            myFileChooserDescriptor.setAllowedExtensions(info.FILE_EXTENSION);
            myTextWithBrowse.setText(FileUtil.toSystemDependentName(optionValue));
            myCurrentEditor = myTextWithBrowse;
            break;
          case Group:
          default:
            assert false;
        }

        return myCurrentEditor;
      }

      @Override
      public Object getCellEditorValue() {
        if (myCurrentEditor == myCheckBox) {
          return String.valueOf(myCheckBox.isSelected());
        }
        if (myCurrentEditor == myTextField) {
          return myTextField.getText().trim();
        }
        if (myCurrentEditor == myTextWithBrowse) {
          final Sdk sdk = myDependenciesConfigurable.getCurrentSdk();
          final String sdkHome = sdk == null || sdk.getSdkType() == FlexmojosSdkType.getInstance() ? null : sdk.getHomePath();

          final String path = FileUtil.toSystemIndependentName(myTextWithBrowse.getText().trim());
          return sdkHome == null ? path : path.replace(sdkHome, CompilerOptionInfo.FLEX_SDK_MACRO);
        }
        if (myCurrentEditor == myRepeatableValueEditor) {
          final Sdk sdk = myDependenciesConfigurable.getCurrentSdk();
          final String sdkHome = sdk == null ? null : sdk.getHomePath();

          final String value = myRepeatableValueEditor.getValue();
          return sdkHome == null ? value : value.replace(sdkHome, CompilerOptionInfo.FLEX_SDK_MACRO);
        }
        assert false;
        return null;
      }
    };
  }

  private ColumnInfo[] createColumns() {
    final ColumnInfo optionColumn = new ColumnInfo("Option") {
      @Override
      public Object valueOf(final Object o) {
        final Object userObject = ((DefaultMutableTreeNode)o).getUserObject();
        return userObject instanceof CompilerOptionInfo ? userObject : o;
      }

      @Override
      public Class getColumnClass() {
        return TreeTableModel.class;
      }
    };

    final ColumnInfo valueColumn = new ColumnInfo("Value") {

      @Override
      public Object valueOf(Object o) {
        final Object userObject = ((DefaultMutableTreeNode)o).getUserObject();
        return userObject instanceof CompilerOptionInfo && !((CompilerOptionInfo)userObject).isGroup()
               ? userObject : null;
      }

      @Override
      public Class getColumnClass() {
        return CompilerOptionInfo.class;
      }

      //public TableCellRenderer getRenderer(Object o) {
      //  return myValueRenderer;
      //}

      //public TableCellEditor getEditor(Object item) {
      //  return myEditor;
      //}

      @Override
      public boolean isCellEditable(Object o) {
        final Object userObject = ((DefaultMutableTreeNode)o).getUserObject();
        return userObject instanceof CompilerOptionInfo && !((CompilerOptionInfo)userObject).isGroup();
      }

      @Override
      public void setValue(Object node, Object value) {
        final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)node;
        final CompilerOptionInfo info = (CompilerOptionInfo)treeNode.getUserObject();
        final Pair<String, ValueSource> valueAndSource = getValueAndSource(info);

        // don't apply if user just clicked through the table without typing anything
        if (!value.equals(valueAndSource.first)) {
          myMapModified = true;
          myCurrentOptions.put(info.ID, (String)value);
          reloadNodeOrGroup(myTreeTable.getTree(), treeNode);
        }
      }
    };

    return new ColumnInfo[]{optionColumn, valueColumn};
  }

  private static void reloadNodeOrGroup(final JTree tree, final DefaultMutableTreeNode treeNode) {
    DefaultMutableTreeNode nodeToRefresh = treeNode;
    DefaultMutableTreeNode parent;
    while ((parent = ((DefaultMutableTreeNode)nodeToRefresh.getParent())).getUserObject() instanceof CompilerOptionInfo) {
      nodeToRefresh = parent;
    }
    ((DefaultTreeModel)tree.getModel()).reload(nodeToRefresh);
  }

  private void updateResourcesControls() {
    myCopyAllResourcesRadioButton.setEnabled(myCopyResourceFilesCheckBox.isSelected());
    myRespectResourcePatternsRadioButton.setEnabled(myCopyResourceFilesCheckBox.isSelected());
    myResourcePatternsHyperlink.setEnabled(myCopyResourceFilesCheckBox.isSelected());
  }

  private void updateAdditionalOptionsControls() {
    final String projectOptions = myProjectLevelOptionsHolder.getProjectLevelCompilerOptions().getAdditionalOptions();
    if (myMode == Mode.BC) {
      myInheritedOptionsField.setText(projectOptions + (projectOptions.isEmpty() ? "" : " ") +
                                      myBCManager.getModuleLevelCompilerOptions().getAdditionalOptions());
    }
    else if (myMode == Mode.Module) {
      myInheritedOptionsField.setText(projectOptions);
    }

    myNoteLabel.setVisible(myMode == Mode.BC &&
                           (!myConfigFileTextWithBrowse.getText().trim().isEmpty() ||
                            !myInheritedOptionsField.getText().trim().isEmpty() ||
                            !myAdditionalOptionsField.getText().trim().isEmpty()));
  }

  private void updateTreeTable() {
    final TreeTableTree tree = myTreeTable.getTree();
    final TreePath selectionPath = tree.getSelectionPath();
    final List<TreePath> expandedPaths = TreeUtil.collectExpandedPaths(tree);
    final DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
    final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();

    final CompilerOptionInfo[] optionInfos = CompilerOptionInfo.getRootInfos();
    final boolean showAll = myShowAllOptionsCheckBox.isSelected();

    updateChildNodes(rootNode, optionInfos, showAll);

    treeModel.reload(rootNode);
    TreeUtil.restoreExpandedPaths(tree, expandedPaths);
    tree.setSelectionPath(selectionPath);
  }

  private void updateChildNodes(final DefaultMutableTreeNode rootNode,
                                final CompilerOptionInfo[] optionInfos, final boolean showAll) {
    int currentNodeNumber = 0;

    for (final CompilerOptionInfo info : optionInfos) {
      final boolean show = info.isGroup() || // empty group will be hidden later
                           ((myMode != Mode.BC || info.isApplicable(getSdkVersion(), myNature))
                            &&
                            (showAll || !info.ADVANCED || hasCustomValue(info))
                           );

      DefaultMutableTreeNode node = findChildNodeWithInfo(rootNode, info);

      if (show) {
        if (node == null) {
          node = new DefaultMutableTreeNode(info, info.isGroup());
          rootNode.insert(node, currentNodeNumber);
        }
        currentNodeNumber++;

        if (info.isGroup()) {
          updateChildNodes(node, info.getChildOptionInfos(), showAll);

          if (node.getChildCount() == 0) {
            node.removeFromParent();
            currentNodeNumber--;
          }
        }
      }
      else {
        if (node != null) {
          node.removeFromParent();
        }
      }
    }
  }

  @Nullable
  private static DefaultMutableTreeNode findChildNodeWithInfo(final DefaultMutableTreeNode rootNode,
                                                              final CompilerOptionInfo info) {
    for (int i = 0; i < rootNode.getChildCount(); i++) {
      final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)rootNode.getChildAt(i);
      if (info.equals(childNode.getUserObject())) {
        return childNode;
      }
    }
    return null;
  }

  private boolean hasCustomValue(final CompilerOptionInfo info) {
    return myCurrentOptions.get(info.ID) != null;
  }

  @NotNull
  private Pair<String, ValueSource> getValueAndSource(final CompilerOptionInfo info) {
    if (info.isGroup()) {
      // choose "the most custom" subnode of a group a group source
      ValueSource groupValueSource = ValueSource.GlobalDefault;
      for (final CompilerOptionInfo childInfo : info.getChildOptionInfos()) {
        if (myMode != Mode.BC || childInfo.isApplicable(getSdkVersion(), myNature)) {
          final ValueSource childSource = getValueAndSource(childInfo).second;
          if (childSource.compareTo(groupValueSource) >0) {
            groupValueSource = childSource;
          }
        }
      }
      return Pair.create(null, groupValueSource);
    }

    final String customValue = myCurrentOptions.get(info.ID);
    if (customValue != null) {
      return Pair.create(customValue, ValueSource.BC);
    }

    if (myMode == Mode.BC) {
      final String moduleDefaultValue = myBCManager.getModuleLevelCompilerOptions().getOption(info.ID);
      if (moduleDefaultValue != null) {
        return Pair.create(moduleDefaultValue, ValueSource.ModuleDefault);
      }
    }

    if (myMode == Mode.BC || myMode == Mode.Module) {
      final String projectDefaultValue = myProjectLevelOptionsHolder.getProjectLevelCompilerOptions().getOption(info.ID);
      if (projectDefaultValue != null) {
        return Pair.create(projectDefaultValue, ValueSource.ProjectDefault);
      }
    }

    return Pair.create(info.getDefaultValue(getSdkVersion(), myNature, myDependenciesConfigurable.getCurrentComponentSet()),
                       ValueSource.GlobalDefault);
  }

  private String getSdkVersion() {
    final Sdk sdk = myDependenciesConfigurable.getCurrentSdk();
    final String sdkVersion = sdk == null ? null : sdk.getVersionString();
    return sdkVersion == null ? UNKNOWN_SDK_VERSION : sdkVersion;
  }

  private final class RepeatableValueEditor extends TextFieldWithBrowseButton {
    private final Project myProject;
    private CompilerOptionInfo myInfo;
    private String myValue;
    private String myAddedConditionalCompilerDefinition = null;

    private RepeatableValueEditor(final Project project) {
      myProject = project;

      getTextField().setEditable(false);
      setButtonIcon(AllIcons.Actions.ShowViewer);

      addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          final List<String> entries = StringUtil.split(myValue, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
          final List<StringBuilder> buffers = new ArrayList<>(entries.size());
          for (String entry : entries) {
            buffers.add(new StringBuilder(entry));
          }

          Sdk sdk;
          if (myInfo.ID.equals("compiler.locale") &&
              (sdk = myDependenciesConfigurable.getCurrentSdk()) != null &&
              sdk.getSdkType() == FlexSdkType2.getInstance()) {

            final LocalesDialog dialog =
              new LocalesDialog(myProject, sdk, StringUtil.split(myValue, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR));
            if (dialog.showAndGet()) {
              myValue = StringUtil.join(dialog.getLocales(), CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
            }
          }
          else {
            final RepeatableValueDialog dialog =
              new RepeatableValueDialog(myProject, StringUtil.capitalizeWords(myInfo.DISPLAY_NAME, true), buffers, myInfo,
                                        myAddedConditionalCompilerDefinition);
            if (dialog.showAndGet()) {
              myValue = StringUtil.join(dialog.getCurrentList(), stringBuilder -> stringBuilder.toString(), CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
            }
          }
          TableUtil.stopEditing(myTreeTable);
        }
      });
    }

    public void setInfoAndValue(final CompilerOptionInfo info, final String value) {
      myInfo = info;
      myValue = value;
      myAddedConditionalCompilerDefinition = null;
      setText(getPresentableSummary(value, info));
    }

    public void setAutoAddConditionalCompilerDefinition(final String ccdName) {
      myAddedConditionalCompilerDefinition = ccdName;
    }

    public String getValue() {
      return myValue;
    }
  }

  static class ExtensionAwareFileChooserDescriptor extends FileChooserDescriptor {
    private String @Nullable [] myAllowedExtensions;

    ExtensionAwareFileChooserDescriptor() {
      super(true, false, true, true, false, false);
    }

    @Override
    public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
      return super.isFileVisible(file, showHiddenFiles) &&
             (file.isDirectory() || isAllowedExtension(file.getExtension()));
    }

    private boolean isAllowedExtension(final String extension) {
      if (myAllowedExtensions == null) return true;

      for (String allowedExtension : myAllowedExtensions) {
        if (allowedExtension.equalsIgnoreCase(extension)) return true;
      }

      return false;
    }

    public void setAllowedExtensions(final String @Nullable ... allowedExtensions) {
      myAllowedExtensions = allowedExtensions;
    }
  }

  private class RestoreDefaultValueAction extends AnAction {
    private final JTree myTree;

    RestoreDefaultValueAction(final JTree tree) {
      super("Restore Default Value");
      myTree = tree;
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
      TableUtil.stopEditing(myTreeTable);
      final CompilerOptionInfo info = getNodeAndInfo().second;
      e.getPresentation().setEnabled(info != null && hasCustomValue(info));
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      final Pair<DefaultMutableTreeNode, CompilerOptionInfo> nodeAndInfo = getNodeAndInfo();
      if (nodeAndInfo.second != null) {
        myMapModified = true;
        myCurrentOptions.remove(nodeAndInfo.second.ID);
        reloadNodeOrGroup(myTree, nodeAndInfo.first);
      }
    }

    private Pair<DefaultMutableTreeNode, CompilerOptionInfo> getNodeAndInfo() {
      final TreePath path = myTree.getSelectionPath();
      final DefaultMutableTreeNode node = path == null ? null : (DefaultMutableTreeNode)path.getLastPathComponent();
      final Object userObject = node == null ? null : node.getUserObject();
      return userObject instanceof CompilerOptionInfo
             ? Pair.create(node, (CompilerOptionInfo)userObject)
             : Pair.empty();
    }
  }

  @Override
  public ActionCallback navigateTo(@Nullable final Place place, final boolean requestFocus) {
    if (place != null) {
      final Object location = place.getPath(FlexBCConfigurable.LOCATION_ON_TAB);
      if (location instanceof Location) {
        switch ((Location)location) {
          case AdditionalConfigFile:
            return IdeFocusManager.findInstance().requestFocus(myConfigFileTextWithBrowse.getChildComponent(), true);

          case FilesToIncludeInSwc:
            return IdeFocusManager.findInstance().requestFocus(myIncludeInSWCField.getChildComponent(), true);

          case ConditionalCompilerDefinition:
            final DefaultMutableTreeNode root = (DefaultMutableTreeNode)myTreeTable.getTree().getModel().getRoot();
            final CompilerOptionInfo info = CompilerOptionInfo.getOptionInfo("compiler.define");
            final DefaultMutableTreeNode node = findChildNodeWithInfo(root, info);

            if (node != null) {
              myTreeTable.clearSelection();
              myTreeTable.addSelectedPath(TreeUtil.getPath(root, node));

              final Object ccdName = place.getPath(CONDITIONAL_COMPILER_DEFINITION_NAME);
              if (ccdName instanceof String) {
                TableUtil.editCellAt(myTreeTable, myTreeTable.getSelectedRow(), 1);
                final Component editor = myTreeTable.getEditorComponent();
                if (editor instanceof RepeatableValueEditor) {
                  ((RepeatableValueEditor)editor).setAutoAddConditionalCompilerDefinition((String)ccdName);
                  ((RepeatableValueEditor)editor).getButton().doClick();
                }
              }
            }
            break;
        }
      }
    }
    return ActionCallback.DONE;
  }

  public static String getTabName() {
    return FlexBundle.message("bc.tab.compiler.options.display.name");
  }
}

