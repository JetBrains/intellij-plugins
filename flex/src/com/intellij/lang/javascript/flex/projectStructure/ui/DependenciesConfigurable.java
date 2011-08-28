package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.facet.impl.ui.libraries.EditLibraryDialog;
import com.intellij.facet.impl.ui.libraries.LibraryCompositionSettings;
import com.intellij.framework.library.FrameworkLibraryVersion;
import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.ide.util.frameworkSupport.CustomLibraryDescriptionBase;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeModuleStructureExtension;
import com.intellij.lang.javascript.flex.projectStructure.options.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.ex.ProjectRoot;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.LibraryKind;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.navigation.Place;
import com.intellij.util.IconUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public class DependenciesConfigurable extends NamedConfigurable<Dependencies> {

  private static final Logger LOG = Logger.getInstance(DependenciesConfigurable.class.getName());

  private JPanel myMainPanel;
  private FlexSdkChooserPanel mySdkPanel;
  private JComboBox myComponentSetCombo;
  private JComboBox myFrameworkLinkageCombo;
  private JLabel myComponentSetLabel;
  private JPanel myTablePanel;
  private final EditableTreeTable<MyTableItem> myTable;

  private final Dependencies myDependencies;
  private final Project myProject;
  private final ModifiableRootModel myRootModel;
  private AddItemPopupAction[] myPopupActions;

  private final Disposable myDisposable;
  private final AnActionButton myEditAction;

  private abstract static class MyTableItem {
    public final DependencyType dependencyType = new DependencyType();

    public abstract String getText();

    public abstract Icon getIcon();
  }

  private static class BCItem extends MyTableItem {
    public final FlexIdeBCConfigurable configurable;

    public BCItem(FlexIdeBCConfigurable configurable) {
      this.configurable = configurable;
      if (configurable.getOutputType() == FlexIdeBuildConfiguration.OutputType.RuntimeLoadedModule) {
        dependencyType.setLinkageType(LinkageType.LoadInRuntime);
      }
    }

    @Override
    public String getText() {
      return MessageFormat.format("{0} ({1})", configurable.getTreeNodeText(), configurable.getModuleName());
    }

    @Override
    public Icon getIcon() {
      return configurable.getIcon();
    }
  }

  private static class ModuleLibraryItem extends MyTableItem {
    public final ModuleLibraryEntry libraryEntry;

    public ModuleLibraryItem(ModuleLibraryEntry libraryEntry) {
      this.libraryEntry = libraryEntry;
    }

    @Override
    public String getText() {
      ProjectRoot[] roots = libraryEntry.getRoots(OrderRootType.CLASSES);
      if (roots.length == 1) {
        VirtualFile firstFile = roots[0].getVirtualFiles()[0];
        if (!firstFile.isDirectory()) {
          firstFile = PathUtil.getLocalFile(firstFile);
          return MessageFormat.format("{0} ({1})", firstFile.getName(), firstFile.getParent().getPresentableUrl());
        }
      }
      return libraryEntry.getName();
    }

    @Override
    public Icon getIcon() {
      return PlatformIcons.LIBRARY_ICON;
    }
  }

  private static final DefaultTableCellRenderer LINKAGE_TYPE_RENDERER = new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
      Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      ((JLabel)component).setText(((LinkageType)value).getShortText());
      return component;
    }
  };

  private static class LinkageTypeEditor extends AbstractTableCellEditor {
    private ComboBox myCombo;
    private final MyTableItem myItem;

    public LinkageTypeEditor(MyTableItem tableItem) {
      myItem = tableItem;
    }

    public Object getCellEditorValue() {
      return myCombo.getSelectedItem();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      LinkageType[] linkageTypes;
      if (myItem instanceof BCItem &&
          ((BCItem)myItem).configurable.getOutputType() == FlexIdeBuildConfiguration.OutputType.RuntimeLoadedModule) {
        linkageTypes = new LinkageType[]{LinkageType.LoadInRuntime};
      }
      else {
        linkageTypes = LinkageType.getSwcLinkageValues();
      }
      ComboBoxModel model = new CollectionComboBoxModel(Arrays.asList(linkageTypes), value);
      model.setSelectedItem(value);
      myCombo = new ComboBox(model, table.getColumnModel().getColumn(1).getWidth());
      myCombo.setRenderer(new ListCellRendererWrapper(myCombo.getRenderer()) {
        @Override
        public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
          setText(((LinkageType)value).getShortText());
        }
      });
      return myCombo;
    }
  }

  private static ColumnInfo<MyTableItem, LinkageType> DEPENDENCY_TYPE_COLUMN = new ColumnInfo<MyTableItem, LinkageType>("Type") {

    @Override
    public LinkageType valueOf(MyTableItem item) {
      return item.dependencyType.getLinkageType();
    }

    @Override
    public void setValue(MyTableItem item, LinkageType linkageType) {
      item.dependencyType.setLinkageType(linkageType);
    }

    @Override
    public TableCellRenderer getRenderer(MyTableItem myTableItem) {
      return LINKAGE_TYPE_RENDERER;
    }

    @Override
    public TableCellEditor getEditor(MyTableItem item) {
      return new LinkageTypeEditor(item);
    }

    @Override
    public boolean isCellEditable(MyTableItem item) {
      return !(item instanceof BCItem) ||
             ((BCItem)item).configurable.getOutputType() != FlexIdeBuildConfiguration.OutputType.RuntimeLoadedModule;
    }

    @Override
    public int getWidth(JTable table) {
      return 100;
    }
  };

  public DependenciesConfigurable(final FlexIdeBuildConfiguration bc, Project project, ModifiableRootModel rootModel) {
    myDependencies = bc.DEPENDENCIES;
    myProject = project;
    myRootModel = rootModel;

    myDisposable = new Disposable() {
      @Override
      public void dispose() {
      }
    };
    Disposer.register(myDisposable, mySdkPanel);
    final boolean mobilePlatform = bc.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Mobile;

    myComponentSetLabel.setVisible(!mobilePlatform && !bc.PURE_ACTION_SCRIPT);
    myComponentSetCombo.setVisible(!mobilePlatform && !bc.PURE_ACTION_SCRIPT);

    myComponentSetCombo.setModel(new DefaultComboBoxModel(FlexIdeBuildConfiguration.ComponentSet.values()));
    myComponentSetCombo.setRenderer(new ListCellRendererWrapper<FlexIdeBuildConfiguration.ComponentSet>(myComponentSetCombo.getRenderer()) {
      public void customize(JList list, FlexIdeBuildConfiguration.ComponentSet value, int index, boolean selected, boolean hasFocus) {
        setText(value.PRESENTABLE_TEXT);
      }
    });

    final LinkageType defaultLinkage = BCUtils.getDefaultFrameworkLinkage(bc.TARGET_PLATFORM, bc.PURE_ACTION_SCRIPT, bc.OUTPUT_TYPE);
    myFrameworkLinkageCombo
      .setRenderer(new ListCellRendererWrapper<LinkageType>(myFrameworkLinkageCombo.getRenderer()) {
        public void customize(JList list, LinkageType value, int index, boolean selected, boolean hasFocus) {
          if (value == LinkageType.Default) {
            setText(MessageFormat.format("Default ({0})", defaultLinkage.getLongText()));
          }
          else {
            setText(value.getLongText());
          }
        }
      });

    myFrameworkLinkageCombo.setModel(new DefaultComboBoxModel(BCUtils.getSuitableFrameworkLinkages(bc.TARGET_PLATFORM,
                                                                                                   bc.PURE_ACTION_SCRIPT, bc.OUTPUT_TYPE)));
    myTable = new EditableTreeTable<MyTableItem>("", DEPENDENCY_TYPE_COLUMN) {
      @Override
      protected void render(SimpleColoredComponent c, MyTableItem item) {
        if (item != null) {
          c.append(item.getText());
          c.setIcon(item.getIcon());
        }
      }
    };
    myTable.setRootVisible(false);
    myTable.getTree().setLineStyleAngled();

    ToolbarDecorator d = ToolbarDecorator.createDecorator(myTable);
    d.setAddAction(new AnActionButtonRunnable() {
      @Override
      public void run(AnActionButton button) {
        addItem(button);
      }
    });
    d.setRemoveAction(new AnActionButtonRunnable() {
      @Override
      public void run(AnActionButton anActionButton) {
        removeSelection();
      }
    });
    d.setUpAction(new AnActionButtonRunnable() {
      @Override
      public void run(AnActionButton anActionButton) {
        moveSelection(-1);
      }
    });
    d.setDownAction(new AnActionButtonRunnable() {
      @Override
      public void run(AnActionButton anActionButton) {
        moveSelection(1);
      }
    });
    myEditAction = new AnActionButton(ProjectBundle.message("module.classpath.button.edit"), IconUtil.getEditIcon()) {
      @Override
      public void actionPerformed(AnActionEvent e) {
        MyTableItem item = myTable.getItemAt(myTable.getSelectedRow());
        editLibrary(((ModuleLibraryItem)item).libraryEntry);
      }
    };
    d.addExtraAction(myEditAction);
    myTablePanel.add(d.createPanel(), BorderLayout.CENTER);

    myTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        updateEditButton();
      }
    });

    myTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          if (myTable.getSelectedRowCount() == 1) {
            MyTableItem item = myTable.getItemAt(myTable.getSelectedRow());
            if (item instanceof BCItem) {
              Place place = new Place().putPath(ProjectStructureConfigurable.CATEGORY, ModuleStructureConfigurable.getInstance(myProject))
                .putPath(MasterDetailsComponent.TREE_OBJECT, ((BCItem)item).configurable.getEditableObject());
              ProjectStructureConfigurable.getInstance(myProject).navigateTo(place, true);
            }
            else if (item instanceof ModuleLibraryItem) {
              editLibrary(((ModuleLibraryItem)item).libraryEntry);
            }
          }
        }
      }
    });

    FlexIdeModuleStructureExtension.getInstance().getConfigurator().addListener(new FlexIdeBCConfigurator.Listener() {
      @Override
      public void moduleRemoved(Module module) {
        List<Integer> rowsToRemove = new ArrayList<Integer>();
        for (int row = 0; row < myTable.getRowCount(); row++) {
          MyTableItem item = myTable.getItemAt(row);
          if (item instanceof BCItem && ((BCItem)item).configurable.getModifiableRootModel().getModule() == module) {
            rowsToRemove.add(row);
          }
        }

        if (!rowsToRemove.isEmpty()) {
          DefaultMutableTreeNode root = myTable.getRoot();
          for (int i = 0; i < rowsToRemove.size(); i++) {
            root.remove(rowsToRemove.get(i) - i);
          }
          myTable.refresh();
        }
      }

      @Override
      public void buildConfigurationRemoved(FlexIdeBCConfigurable configurable) {
        if (configurable.getDependenciesConfigurable() == DependenciesConfigurable.this) {
          return;
        }

        for (int row = 0; row < myTable.getRowCount(); ) {
          MyTableItem item = myTable.getItemAt(row);
          if (item instanceof BCItem && ((BCItem)item).configurable == configurable) {
            myTable.getRoot().remove(row);
            myTable.refresh();
            // there may be only one dependency on a BC
            break;
          }
        }
      }
    }, myDisposable);
  }

  private void updateEditButton() {
    boolean librarySelected =
      myTable.getSelectedRowCount() == 1 && myTable.getItemAt(myTable.getSelectedRow()) instanceof ModuleLibraryItem;
    myEditAction.setEnabled(librarySelected);
  }

  private void editLibrary(ModuleLibraryEntry entry) {
    ModuleLibraryEditor editor = new ModuleLibraryEditor(entry);
    try {
      LibraryCompositionSettings settings = new LibraryCompositionSettings(new CustomLibraryDescriptionBase("") {
        @NotNull
        @Override
        public Set<? extends LibraryKind<?>> getSuitableLibraryKinds() {
          return Collections.emptySet();
        }
      }, "", null, Collections.<FrameworkLibraryVersion>emptyList());

      EditLibraryDialog d = new EditLibraryDialog(myMainPanel, settings, editor);
      d.show();
      if (d.isOK()) {
        editor.applyTo(entry);
      }
    }
    finally {
      Disposer.dispose(editor);
    }
  }

  private void addItem(AnActionButton button) {
    initPopupActions();
    final JBPopup popup = JBPopupFactory.getInstance().createListPopup(
      new BaseListPopupStep<AddItemPopupAction>(null, myPopupActions) {
        @Override
        public Icon getIconFor(AddItemPopupAction aValue) {
          return aValue.getIcon();
        }

        @Override
        public boolean hasSubstep(AddItemPopupAction selectedValue) {
          return selectedValue.hasSubStep();
        }

        public boolean isMnemonicsNavigationEnabled() {
          return true;
        }

        public PopupStep onChosen(final AddItemPopupAction selectedValue, final boolean finalChoice) {
          if (selectedValue.hasSubStep()) {
            return selectedValue.createSubStep();
          }
          return doFinalStep(new Runnable() {
            public void run() {
              selectedValue.run();
            }
          });
        }

        @NotNull
        public String getTextFor(AddItemPopupAction value) {
          return "&" + value.getIndex() + "  " + value.getTitle();
        }
      });
    popup.show(button.getPreferredPopupPoint());
  }

  private void removeSelection() {
    int[] selectedRows = myTable.getSelectedRows();
    Arrays.sort(selectedRows);
    DefaultMutableTreeNode root = myTable.getRoot();
    for (int i = 0; i < selectedRows.length; i++) {
      root.remove(selectedRows[i] - i);
    }
    myTable.refresh();
    if (myTable.getRowCount() > 0) {
      int toSelect = Math.min(myTable.getRowCount() - 1, selectedRows[0]);
      myTable.clearSelection();
      myTable.getSelectionModel().addSelectionInterval(toSelect, toSelect);
    }
  }

  private void moveSelection(int delta) {
    int[] selectedRows = myTable.getSelectedRows();
    Arrays.sort(selectedRows);
    DefaultMutableTreeNode root = myTable.getRoot();

    if (delta < 0) {
      for (int i = 0; i < selectedRows.length; i++) {
        int row = selectedRows[i];
        DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(row);
        root.remove(row);
        root.insert(child, row + delta);
      }
    }
    else {
      for (int i = selectedRows.length - 1; i >= 0; i--) {
        int row = selectedRows[i];
        DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(row);
        root.remove(row);
        root.insert(child, row + delta);
      }
    }
    myTable.refresh();
    myTable.clearSelection();
    for (int selectedRow : selectedRows) {
      myTable.getSelectionModel().addSelectionInterval(selectedRow + delta, selectedRow + delta);
    }
  }

  @Nls
  public String getDisplayName() {
    return "Dependencies";
  }

  public void setDisplayName(final String name) {
  }

  public String getBannerSlogan() {
    return "Dependencies";
  }

  public Icon getIcon() {
    return null;
  }

  public Dependencies getEditableObject() {
    return myDependencies;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public boolean isModified() {
    if (mySdkPanel.isModified()) return true;
    if (myDependencies.COMPONENT_SET != myComponentSetCombo.getSelectedItem()) return true;
    if (myDependencies.FRAMEWORK_LINKAGE != myFrameworkLinkageCombo.getSelectedItem()) return true;

    List<MyTableItem> items = myTable.getItems();
    List<DependencyEntry> entries = myDependencies.getEntries();
    if (items.size() != entries.size()) return true;
    for (int i = 0; i < items.size(); i++) {
      MyTableItem item = items.get(i);
      DependencyEntry entry = entries.get(i);
      if (item instanceof BCItem) {
        if (!(entry instanceof BuildConfigurationEntry)) {
          return true;
        }
        BCItem bcItem = (BCItem)item;
        BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;
        if (bcItem.configurable.getModifiableRootModel().getModule() != bcEntry.getModule()) return true;
        if (!bcItem.configurable.getDisplayName().equals(bcEntry.getBcName())) return true;
      }
      else if (item instanceof ModuleLibraryItem) {
        if (!(entry instanceof ModuleLibraryEntry)) {
          return true;
        }
        ModuleLibraryItem libraryItem = (ModuleLibraryItem)item;
        ModuleLibraryEntry libraryEntry = (ModuleLibraryEntry)entry;
        if (!libraryEntry.isEqual(libraryItem.libraryEntry)) {
          return true;
        }
      }
      if (!item.dependencyType.isEqual(entry.getDependencyType())) return true;
    }
    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myDependencies);
    mySdkPanel.apply();
  }

  public void applyTo(final Dependencies dependencies) {
    dependencies.COMPONENT_SET = (FlexIdeBuildConfiguration.ComponentSet)myComponentSetCombo.getSelectedItem();
    dependencies.FRAMEWORK_LINKAGE = (LinkageType)myFrameworkLinkageCombo.getSelectedItem();
    dependencies.getEntries().clear();
    List<MyTableItem> items = myTable.getItems();
    for (MyTableItem item : items) {
      DependencyEntry entry;
      if (item instanceof BCItem) {
        FlexIdeBCConfigurable configurable = ((BCItem)item).configurable;
        entry =
          new BuildConfigurationEntry(configurable.getModifiableRootModel().getModule(), configurable.getDisplayName());
      }
      else if (item instanceof ModuleLibraryItem) {
        entry = new ModuleLibraryEntry();
        ((ModuleLibraryItem)item).libraryEntry.applyTo((ModuleLibraryEntry)entry);
      }
      else {
        throw new IllegalArgumentException("unexpected item type: " + item);
      }
      item.dependencyType.applyTo(entry.getDependencyType());
      dependencies.getEntries().add(entry);
    }
  }

  public void reset() {
    mySdkPanel.reset();
    myComponentSetCombo.setSelectedItem(myDependencies.COMPONENT_SET);
    myFrameworkLinkageCombo.setSelectedItem(myDependencies.FRAMEWORK_LINKAGE);

    DefaultMutableTreeNode root = myTable.getRoot();
    root.removeAllChildren();
    FlexIdeBCConfigurator configurator = FlexIdeModuleStructureExtension.getInstance().getConfigurator();
    for (DependencyEntry entry : myDependencies.getEntries()) {
      MyTableItem item = null;
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;
        FlexIdeBCConfigurable configurable =
          ContainerUtil.find(configurator.getBCConfigurables(bcEntry.getModule()), new Condition<FlexIdeBCConfigurable>() {
            @Override
            public boolean value(FlexIdeBCConfigurable configurable) {
              return configurable.getEditableObject().NAME.equals(bcEntry.getBcName());
            }
          });
        if (configurable == null) {
          // broken project file?
          LOG.error("configurable not found for " + bcEntry.getBcName());
        }
        else {
          item = new BCItem(configurable);
        }
      }
      else if (entry instanceof ModuleLibraryEntry) {
        item = new ModuleLibraryItem(((ModuleLibraryEntry)entry).getCopy());
      }
      if (item != null) {
        entry.getDependencyType().applyTo(item.dependencyType);
        root.add(new DefaultMutableTreeNode(item, false));
      }
    }
    myTable.refresh();
    updateEditButton();
  }

  public void disposeUIResources() {
    Disposer.dispose(myDisposable);
  }

  private void createUIComponents() {
    mySdkPanel = new FlexSdkChooserPanel(myProject, myRootModel);
  }

  public FlexSdkChooserPanel getSdkChooserPanel() {
    return mySdkPanel;
  }

  public void addFlexSdkListener(ChangeListener listener) {
    mySdkPanel.addListener(listener);
  }

  public void removeFlexSdkListener(ChangeListener listener) {
    mySdkPanel.removeListener(listener);
  }

  private void initPopupActions() {
    if (myPopupActions == null) {
      int actionIndex = 1;
      final List<AddItemPopupAction> actions = new ArrayList<AddItemPopupAction>();
      actions.add(new AddBuildConfigurationDependencyAction(actionIndex++));
      actions.add(new AddFilesAction(actionIndex++));
      myPopupActions = actions.toArray(new AddItemPopupAction[actions.size()]);
    }
  }

  private class AddBuildConfigurationDependencyAction extends AddItemPopupAction {
    public AddBuildConfigurationDependencyAction(int index) {
      super(index, "Build Configuration...", null);
    }

    @Override
    public void run() {
      Collection<FlexIdeBCConfigurable> dependencies = new ArrayList<FlexIdeBCConfigurable>();
      List<MyTableItem> items = myTable.getItems();
      for (MyTableItem item : items) {
        if (item instanceof BCItem) {
          dependencies.add(((BCItem)item).configurable);
        }
      }

      Map<Module, List<FlexIdeBCConfigurable>> treeItems = new HashMap<Module, List<FlexIdeBCConfigurable>>();
      FlexIdeBCConfigurator configurator = FlexIdeModuleStructureExtension.getInstance().getConfigurator();
      for (Module module : ModuleStructureConfigurable.getInstance(myProject).getModules()) {
        if (ModuleType.get(module) != FlexModuleType.getInstance()) {
          continue;
        }
        for (FlexIdeBCConfigurable configurable : configurator.getBCConfigurables(module)) {
          if (dependencies.contains(configurable) || configurable.getDependenciesConfigurable() == DependenciesConfigurable.this) {
            continue;
          }
          FlexIdeBuildConfiguration.OutputType outputType = configurable.getOutputType();
          if (outputType != FlexIdeBuildConfiguration.OutputType.Library &&
              outputType != FlexIdeBuildConfiguration.OutputType.RuntimeLoadedModule) {
            continue;
          }

          List<FlexIdeBCConfigurable> list = treeItems.get(module);
          if (list == null) {
            list = new ArrayList<FlexIdeBCConfigurable>();
            treeItems.put(module, list);
          }
          list.add(configurable);
        }
      }

      if (treeItems.isEmpty()) {
        Messages.showInfoMessage(myProject, "No applicable build configurations found", "Add Dependency");
        return;
      }

      ChooseBuildConfigurationDialog d = new ChooseBuildConfigurationDialog(myProject, treeItems);
      d.show();
      if (!d.isOK()) {
        return;
      }

      FlexIdeBCConfigurable[] configurables = d.getSelectedConfigurables();
      DefaultMutableTreeNode root = myTable.getRoot();
      for (FlexIdeBCConfigurable configurable : configurables) {
        root.add(new DefaultMutableTreeNode(new BCItem(configurable), false));
      }
      myTable.refresh();
      myTable.getSelectionModel().clearSelection();
      int rowCount = myTable.getRowCount();
      myTable.getSelectionModel().addSelectionInterval(rowCount - configurables.length, rowCount - 1);
    }
  }

  private class AddFilesAction extends AddItemPopupAction {
    public AddFilesAction(int index) {
      super(index, "Files...", null);
    }

    @Override
    public void run() {
      FileChooserDescriptor d = new FileChooserDescriptor(true, false, true, false, false, true) {
        @Override
        public boolean isFileSelectable(VirtualFile file) {
          return !file.isDirectory() && "swc".equals(FileUtil.getExtension(file.getName()));
        }
      };
      d.setTitle("Select Libraries");
      VirtualFile[] files = FileChooser.chooseFiles(myProject, d);
      if (files.length == 0) {
        return;
      }

      DefaultMutableTreeNode rootNode = myTable.getRoot();
      // TODO filter out roots that point into roots of another libraries
      for (VirtualFile file : files) {
        ModuleLibraryEntry e = new ModuleLibraryEntry();
        e.setName(file.getName());
        e.addRoot(OrderRootType.CLASSES, file);
        rootNode.add(new DefaultMutableTreeNode(new ModuleLibraryItem(e), false));
      }
      myTable.refresh();
      myTable.getSelectionModel().clearSelection();
      int rowCount = myTable.getRowCount();
      myTable.getSelectionModel().addSelectionInterval(rowCount - files.length, rowCount - 1);
    }
  }
}
