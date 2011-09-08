package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.ValueSource;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.options.CompilerOptions;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.NonFocusableCheckBox;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.TableUtil;
import com.intellij.ui.TreeTableSpeedSearch;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

public class CompilerOptionsConfigurable extends NamedConfigurable<CompilerOptions> {
  private JPanel myMainPanel;
  private TreeTable myTreeTable;
  private NonFocusableCheckBox myShowAllOptionsCheckBox;
  private JLabel myInheritProjectDefaultsLegend;
  private JLabel myInheritModuleDefaultsLegend;
  private JButton myProjectDefaultsButton;
  private JButton myModuleDefaultsButton;

  private final Mode myMode;
  private final Module myModule;
  private final Project myProject;
  private final String myName;
  private final FlexIdeBuildConfigurationManager myBCManager;
  private final FlexIdeProjectLevelCompilerOptionsHolder myProjectLevelOptionsHolder;
  private final CompilerOptions myCompilerOptions;
  private final Map<String, String> myCurrentOptions;
  private boolean myModified;

  private enum Mode {BC, Module, Project}

  public CompilerOptionsConfigurable(final Module module, final CompilerOptions compilerOptions) {
    this(Mode.BC, module, module.getProject(), compilerOptions);
  }

  private CompilerOptionsConfigurable(final Mode mode, final Module module, final Project project, final CompilerOptions compilerOptions) {
    myMode = mode;
    myModule = module;
    myProject = project;
    myName = myMode == Mode.BC
             ? "Compiler Options"
             : myMode == Mode.Module
               ? MessageFormat.format("Default Compiler Options For Module ''{0}''", module.getName())
               : MessageFormat.format("Default Compiler Options For Project ''{0}''", project.getName());
    myBCManager = myMode == Mode.BC ? FlexIdeBuildConfigurationManager.getInstance(module) : null;
    myProjectLevelOptionsHolder = FlexIdeProjectLevelCompilerOptionsHolder.getInstance(project);
    myCompilerOptions = compilerOptions;
    myCurrentOptions = new THashMap<String, String>();

    myShowAllOptionsCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateTreeTable();
      }
    });

    myInheritModuleDefaultsLegend.setVisible(myMode == Mode.BC);
    myInheritProjectDefaultsLegend.setVisible(myMode == Mode.BC || myMode == Mode.Module);

    initButtons();
  }

  private void initButtons() {
    if (myMode == Mode.Project) {
      myProjectDefaultsButton.setVisible(false);
    }
    else {
      myProjectDefaultsButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          final CompilerOptions compilerOptions = myProjectLevelOptionsHolder.getProjectLevelCompilerOptions();
          final boolean changed = ShowSettingsUtil.getInstance()
            .editConfigurable(myProject, new CompilerOptionsConfigurable(Mode.Project, null, myProject, compilerOptions));
          if (changed) {
            updateTreeTable();
          }
        }
      });
    }

    if (myMode == Mode.Project || myMode == Mode.Module) {
      myModuleDefaultsButton.setVisible(false);
    }
    else {
      myModuleDefaultsButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          final CompilerOptions compilerOptions = myBCManager.getModuleLevelCompilerOptions();
          final boolean changed = ShowSettingsUtil.getInstance()
            .editConfigurable(myProject, new CompilerOptionsConfigurable(Mode.Module, myModule, myProject, compilerOptions));
          if (changed) {
            updateTreeTable();
          }
        }
      });
    }
  }

  @Nls
  public String getDisplayName() {
    return myName;
  }

  public void setDisplayName(final String name) {
  }

  public String getBannerSlogan() {
    return "Compiler Options";
  }

  public Icon getIcon() {
    return null;
  }

  public CompilerOptions getEditableObject() {
    return myCompilerOptions;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    TreeUtil.expandAll(myTreeTable.getTree());

    return myMainPanel;
  }

  public boolean isModified() {
    return myModified;
  }

  public void apply() throws ConfigurationException {
    applyTo(myCompilerOptions);
  }

  public void applyTo(final CompilerOptions compilerOptions) {
    TableUtil.stopEditing(myTreeTable);
    compilerOptions.OPTIONS.clear();
    compilerOptions.OPTIONS.putAll(myCurrentOptions);
    if (compilerOptions == myCompilerOptions) {
      myModified = false;
    }
  }

  public void reset() {
    myCurrentOptions.clear();
    myCurrentOptions.putAll(myCompilerOptions.OPTIONS);
    myModified = false;
    updateTreeTable();
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {
    myTreeTable = createTreeTable();
  }

  private TreeTable createTreeTable() {
    final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
    final TreeTableModel model = new ListTreeTableModel(rootNode, createColumns());

    final TreeTable treeTable = new TreeTable(model);

    treeTable.getColumnModel().getColumn(1).setCellRenderer(createValueRenderer());
    treeTable.getColumnModel().getColumn(1).setCellEditor(createValueEditor());

    final TreeTableTree tree = treeTable.getTree();

    tree.setRootVisible(false);
    tree.setLineStyleAngled();
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(createTreeCellRenderer());
    treeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    treeTable.setRowHeight(new JTextField("Fake").getPreferredSize().height + 2);
    //treeTable.setTableHeader(null);

    final DefaultActionGroup group = new DefaultActionGroup();
    group.add(new RestoreDefaultValueAction(tree));
    PopupHandler.installPopupHandler(treeTable, group, ActionPlaces.UNKNOWN, ActionManager.getInstance());

    new TreeTableSpeedSearch(treeTable, new Convertor<TreePath, String>() {
      public String convert(final TreePath o) {
        final Object userObject = ((DefaultMutableTreeNode)o.getLastPathComponent()).getUserObject();
        return userObject instanceof CompilerOptionInfo ? ((CompilerOptionInfo)userObject).DISPLAY_NAME : "";
      }
    });
    return treeTable;
  }

  private TreeCellRenderer createTreeCellRenderer() {
    return new TreeCellRenderer() {
      private final JLabel myLabel = new JLabel();

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
        renderAccordingToSource(myLabel, valueSource);

        myLabel.setForeground(selected ? UIUtil.getTableSelectionForeground() : UIUtil.getTableForeground());

        return myLabel;
      }
    };
  }

  private static void renderAccordingToSource(final Component component, final ValueSource valueSource) {
    final int boldOrPlain = valueSource == ValueSource.BC || valueSource == ValueSource.ProjectDefault ? Font.BOLD : Font.PLAIN;
    component.setFont(component.getFont().deriveFont(boldOrPlain));
    component.setEnabled(valueSource == ValueSource.BC || valueSource == ValueSource.ModuleDefault);
  }

  private TableCellRenderer createValueRenderer() {
    return new TableCellRenderer() {
      private final JLabel myLabel = new JLabel();
      private final JCheckBox myCheckBox = new JCheckBox();
      private final ComponentWithBrowseButton<JLabel> myLabelWithBrowse = new ComponentWithBrowseButton<JLabel>(new JLabel(), null);

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
            myLabel.setText(getPresentableSummary(valueAndSource.first, info.TYPE));
            renderAccordingToSource(myLabel, valueAndSource.second);
            return myLabel;
          case File:
            final JLabel label = myLabelWithBrowse.getChildComponent();
            myLabelWithBrowse.setBackground(table.getBackground());
            label.setText(valueAndSource.first);
            renderAccordingToSource(label, valueAndSource.second);
            return myLabelWithBrowse;
          case Group:
          default:
            assert false;
            return null;
        }
      }
    };
  }

  private static String getPresentableSummary(final String rawValue, final CompilerOptionInfo.OptionType type) {
    if (type == CompilerOptionInfo.OptionType.List) {
      final int listSize = StringUtil.countChars(rawValue, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR) + 1;
      return listSize < 2 ? rawValue : MessageFormat.format("({0} entries)", listSize);
    }
    return rawValue;
  }

  private TableCellEditor createValueEditor() {
    return new AbstractTableCellEditor() {
      //private CellEditorComponentWithBrowseButton<JTextField> myTextWithBrowse;
      //private LocalPathCellEditor myLocalPathCellEditor;
      private final JTextField myTextField = new JTextField();
      private final TextFieldWithBrowseButton myTextWithBrowse = new TextFieldWithBrowseButton();
      private final ExtensionAwareFileChooserDescriptor myFileChooserDescriptor = new ExtensionAwareFileChooserDescriptor();
      private final JCheckBox myCheckBox = new JCheckBox();

      {
        myTextWithBrowse.addBrowseFolderListener(null, null, myProject, myFileChooserDescriptor);
        myCheckBox.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent e) {
            TableUtil.stopEditing(myTreeTable); // apply new check box state immediately
          }
        });
      }

      private Component myCurrentEditor;

      public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, int column) {
        assert value instanceof CompilerOptionInfo;

        final CompilerOptionInfo info = (CompilerOptionInfo)value;
        final String optionValue = getValueAndSource(info).first;

        switch (info.TYPE) {
          case Boolean:
            myCheckBox.setBackground(table.getBackground());
            myCheckBox.setSelected("true".equalsIgnoreCase(optionValue));
            myCurrentEditor = myCheckBox;
            break;
          case String:
          case Int:   // todo dedicated renderers. Move them to CompilerOptionInfo class?
          case List:
          case IncludeClasses:
          case IncludeFiles:
            myTextField.setText(optionValue);
            myCurrentEditor = myTextField;
            break;
          case File:
            myFileChooserDescriptor.setAllowedExtension(info.FILE_EXTENSION);
            myTextWithBrowse.setText(optionValue);
            myCurrentEditor = myTextWithBrowse;
            break;
          case Group:
          default:
            assert false;
        }

        return myCurrentEditor;
      }

      public Object getCellEditorValue() {
        if (myCurrentEditor == myCheckBox) {
          return String.valueOf(myCheckBox.isSelected());
        }
        if (myCurrentEditor == myTextField) {
          return myTextField.getText().trim();
        }
        if (myCurrentEditor == myTextWithBrowse) {
          return myTextWithBrowse.getText().trim();
        }
        assert false;
        return null;
      }
    };
  }

  private ColumnInfo[] createColumns() {
    final ColumnInfo optionColumn = new ColumnInfo("Option") {
      public Object valueOf(final Object o) {
        final Object userObject = ((DefaultMutableTreeNode)o).getUserObject();
        return userObject instanceof CompilerOptionInfo ? userObject : o;
      }

      public Class getColumnClass() {
        return TreeTableModel.class;
      }
    };

    final ColumnInfo valueColumn = new ColumnInfo("Value") {

      public Object valueOf(Object o) {
        final Object userObject = ((DefaultMutableTreeNode)o).getUserObject();
        return userObject instanceof CompilerOptionInfo && !((CompilerOptionInfo)userObject).isGroup()
               ? userObject : null;
      }

      public Class getColumnClass() {
        return CompilerOptionInfo.class;
      }

      //public TableCellRenderer getRenderer(Object o) {
      //  return myValueRenderer;
      //}

      //public TableCellEditor getEditor(Object item) {
      //  return myEditor;
      //}

      public boolean isCellEditable(Object o) {
        final Object userObject = ((DefaultMutableTreeNode)o).getUserObject();
        return userObject instanceof CompilerOptionInfo && !((CompilerOptionInfo)userObject).isGroup();
      }

      public void setValue(Object node, Object value) {
        final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)node;
        final CompilerOptionInfo info = (CompilerOptionInfo)treeNode.getUserObject();
        final Pair<String, ValueSource> valueAndSource = getValueAndSource(info);

        // don't apply if user just clicked through the table without typing anything
        if (valueAndSource.second == ValueSource.BC || !value.equals(valueAndSource.first)) {
          myModified = true;
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
                           hasCustomValue(info) ||
                           ((showAll || !info.ADVANCED) &&
                            info.isApplicable("4.5.0", TargetPlatform.Web, false, OutputType.Application)); // todo fix arguments

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
  private static DefaultMutableTreeNode findChildNodeWithInfo(final DefaultMutableTreeNode node,
                                                              final CompilerOptionInfo info) {
    for (int i = 0; i < node.getChildCount(); i++) {
      final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
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
        if (childInfo.isApplicable("4.5.0", TargetPlatform.Web, false, OutputType.Application)) {  // todo fix arguments
          final ValueSource childSource = getValueAndSource(childInfo).second;
          if (childSource.ordinal() > groupValueSource.ordinal()) {
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
      final String moduleDefaultValue = myBCManager.getModuleLevelCompilerOptions().OPTIONS.get(info.ID);
      if (moduleDefaultValue != null) {
        return Pair.create(moduleDefaultValue, ValueSource.ModuleDefault);
      }
    }

    if (myMode == Mode.BC || myMode == Mode.Module) {
      final String projectDefaultValue = myProjectLevelOptionsHolder.getProjectLevelCompilerOptions().OPTIONS.get(info.ID);
      if (projectDefaultValue != null) {
        return Pair.create(projectDefaultValue, ValueSource.ProjectDefault);
      }
    }

    // todo pass live parameters
    return Pair.create(info.getDefaultValue("4.5", TargetPlatform.Web), ValueSource.GlobalDefault);
  }

  private static class ExtensionAwareFileChooserDescriptor extends FileChooserDescriptor {
    private String myAllowedExtension;

    public ExtensionAwareFileChooserDescriptor() {
      super(true, false, false, true, false, false);
    }

    public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
      return super.isFileVisible(file, showHiddenFiles) &&
             (file.isDirectory() || myAllowedExtension == null || myAllowedExtension.equals(file.getExtension()));
    }

    public void setAllowedExtension(final String allowedExtension) {
      myAllowedExtension = allowedExtension;
    }
  }

  private class RestoreDefaultValueAction extends AnAction {
    private final JTree myTree;

    public RestoreDefaultValueAction(final JTree tree) {
      super("Restore Default Value");
      myTree = tree;
    }

    public void update(final AnActionEvent e) {
      TableUtil.stopEditing(myTreeTable);
      final CompilerOptionInfo info = getNodeAndInfo().second;
      e.getPresentation().setEnabled(info != null && hasCustomValue(info));
    }

    public void actionPerformed(final AnActionEvent e) {
      final Pair<DefaultMutableTreeNode, CompilerOptionInfo> nodeAndInfo = getNodeAndInfo();
      if (nodeAndInfo.second != null) {
        myModified = true;
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
             : Pair.<DefaultMutableTreeNode, CompilerOptionInfo>empty();
    }
  }
}

