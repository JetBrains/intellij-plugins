package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryRootsComponentDescriptor;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeModuleStructureExtension;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.options.*;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablePresentation;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.roots.ui.configuration.LibraryTableModifiableModelProvider;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.classpath.CreateModuleLibraryChooser;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.EditExistingLibraryDialog;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.openapi.roots.ui.util.OrderEntryCellAppearanceUtils;
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
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.*;
import com.intellij.ui.navigation.Place;
import com.intellij.util.IconUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import static com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration.ComponentSet;

public class DependenciesConfigurable extends NamedConfigurable<Dependencies> {

  private static final Icon MISSING_BC_ICON = null;

  private JPanel myMainPanel;
  private FlexSdkPanel mySdkPanel;
  private JLabel myTargetPlayerLabel;
  private JComboBox myTargetPlayerCombo;
  private JLabel myComponentSetLabel;
  private JComboBox myComponentSetCombo;
  private JComboBox myFrameworkLinkageCombo;
  private JPanel myTablePanel;
  private final EditableTreeTable<MyTableItem> myTable;

  private final Dependencies myDependencies;
  private final Project myProject;
  private AddItemPopupAction[] myPopupActions;

  private final Disposable myDisposable;
  private final AnActionButton myEditAction;
  private final AnActionButton myRemoveButton;
  private final BuildConfigurationNature myNature;
  private final FlexSdksModifiableModel mySdksModel;
  private final ModifiableRootModel myModifiableRootModel;

  private abstract static class MyTableItem {
    public abstract String getText();

    public abstract Icon getIcon();

    public abstract boolean showLinkage();

    public abstract boolean isLinkageEditable();

    public abstract LinkageType getLinkageType();

    public abstract void setLinkageType(LinkageType linkageType);

    public abstract boolean isValid();

    public abstract void onRemove(ModifiableRootModel modifiableModel);
  }

  private static class BCItem extends MyTableItem {
    public final DependencyType dependencyType = new DependencyType();
    public final FlexIdeBCConfigurable configurable;
    public final String moduleName;
    public final String bcName;

    public BCItem(@NotNull String moduleName,
                  @NotNull String bcName) {
      this.moduleName = moduleName;
      this.bcName = bcName;
      this.configurable = null;
    }

    public BCItem(@NotNull FlexIdeBCConfigurable configurable) {
      this.moduleName = null;
      this.bcName = null;
      this.configurable = configurable;
      if (configurable.getOutputType() == FlexIdeBuildConfiguration.OutputType.RuntimeLoadedModule) {
        dependencyType.setLinkageType(LinkageType.LoadInRuntime);
      }
    }

    @Override
    public String getText() {
      if (configurable != null) {
        return MessageFormat.format("{0} ({1})", configurable.getTreeNodeText(), configurable.getModuleName());
      }
      else {
        return MessageFormat.format("{0} ({1})", bcName, moduleName);
      }
    }

    @Override
    public Icon getIcon() {
      return configurable != null ? configurable.getIcon() : MISSING_BC_ICON;
    }

    @Override
    public boolean showLinkage() {
      return configurable != null;
    }

    @Override
    public boolean isLinkageEditable() {
      return configurable != null && configurable.getOutputType() != FlexIdeBuildConfiguration.OutputType.RuntimeLoadedModule;
    }

    @Override
    public LinkageType getLinkageType() {
      return dependencyType.getLinkageType();
    }

    @Override
    public void setLinkageType(LinkageType linkageType) {
      dependencyType.setLinkageType(linkageType);
    }

    @Override
    public boolean isValid() {
      return configurable != null;
    }

    @Override
    public void onRemove(ModifiableRootModel modifiableModel) {
    }
  }

  private static class ModuleLibraryItem extends MyTableItem {
    public final DependencyType dependencyType = new DependencyType();
    public final String libraryId;
    @Nullable
    public final LibraryOrderEntry orderEntry;

    public ModuleLibraryItem(@NotNull String libraryId, @Nullable LibraryOrderEntry orderEntry) {
      this.libraryId = libraryId;
      this.orderEntry = orderEntry;
    }

    @Override
    public String getText() {
      if (orderEntry != null) {
        Library library = orderEntry.getLibrary();
        if (library != null) {
          boolean hasInvalidRoots = !((LibraryEx)library).getInvalidRootUrls(OrderRootType.CLASSES).isEmpty();
          return OrderEntryCellAppearanceUtils.forLibrary(library, hasInvalidRoots).getText();
        }
      }
      return "<unknown>";
    }

    @Override
    public Icon getIcon() {
      return PlatformIcons.LIBRARY_ICON;
    }

    @Override
    public boolean showLinkage() {
      return true;
    }

    @Override
    public boolean isLinkageEditable() {
      return true;
    }

    @Override
    public LinkageType getLinkageType() {
      return dependencyType.getLinkageType();
    }

    @Override
    public void setLinkageType(LinkageType linkageType) {
      dependencyType.setLinkageType(linkageType);
    }

    @Override
    public boolean isValid() {
      return orderEntry != null;
    }

    @Override
    public void onRemove(ModifiableRootModel modifiableModel) {
      if (orderEntry != null) {
        modifiableModel.removeOrderEntry(orderEntry);
      }
    }
  }

  private static class SdkItem extends MyTableItem {
    private final FlexSdk mySdk;

    public SdkItem(FlexSdk sdk) {
      mySdk = sdk;
    }

    @Override
    public String getText() {
      return MessageFormat.format("Flex SDK {0}", mySdk.getFlexVersion());
    }

    @Override
    public Icon getIcon() {
      return FlexSdkType.getInstance().getIcon();
    }

    @Override
    public boolean showLinkage() {
      return false;
    }

    @Override
    public LinkageType getLinkageType() {
      return null;
    }

    @Override
    public boolean isLinkageEditable() {
      return false;
    }

    @Override
    public void setLinkageType(LinkageType linkageType) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid() {
      return mySdk.isValid();
    }

    @Override
    public void onRemove(ModifiableRootModel modifiableModel) {
    }
  }

  private static class SdkEntryItem extends MyTableItem {
    private final String url;
    private final LinkageType linkageType;

    private SdkEntryItem(String url, LinkageType linkageType) {
      this.url = url;
      this.linkageType = linkageType;
    }

    @Override
    public String getText() {
      return url;
    }

    @Override
    public Icon getIcon() {
      return PlatformIcons.LIBRARY_ICON;
    }

    @Override
    public boolean showLinkage() {
      return true;
    }

    @Override
    public boolean isLinkageEditable() {
      return false;
    }

    @Override
    public LinkageType getLinkageType() {
      return linkageType;
    }

    @Override
    public void setLinkageType(LinkageType linkageType) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public void onRemove(ModifiableRootModel modifiableModel) {
      throw new UnsupportedOperationException();
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

  private static final DefaultTableCellRenderer EMPTY_RENDERER = new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
      Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      ((JLabel)component).setText("");
      return component;
    }
  };

  private static final AbstractTableCellEditor LINKAGE_TYPE_EDITOR = new AbstractTableCellEditor() {
    private ComboBox myCombo;

    public Object getCellEditorValue() {
      return myCombo.getSelectedItem();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      ComboBoxModel model = new CollectionComboBoxModel(Arrays.asList(LinkageType.getSwcLinkageValues()), value);
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
  };

  private static ColumnInfo<MyTableItem, LinkageType> DEPENDENCY_TYPE_COLUMN = new ColumnInfo<MyTableItem, LinkageType>("Type") {

    @Override
    public LinkageType valueOf(MyTableItem item) {
      return item.getLinkageType();
    }

    @Override
    public void setValue(MyTableItem item, LinkageType linkageType) {
      item.setLinkageType(linkageType);
    }

    @Override
    public TableCellRenderer getRenderer(MyTableItem item) {
      return item.showLinkage() ? LINKAGE_TYPE_RENDERER : EMPTY_RENDERER;
    }

    @Override
    public TableCellEditor getEditor(MyTableItem item) {
      return LINKAGE_TYPE_EDITOR;
    }

    @Override
    public boolean isCellEditable(MyTableItem item) {
      return item.isLinkageEditable();
    }

    @Override
    public int getWidth(JTable table) {
      return 100;
    }
  };

  public DependenciesConfigurable(final FlexIdeBuildConfiguration bc,
                                  Project project,
                                  FlexSdksModifiableModel sdksModel,
                                  ModifiableRootModel modifiableRootModel) {
    mySdksModel = sdksModel;
    myModifiableRootModel = modifiableRootModel;
    myDependencies = bc.DEPENDENCIES;
    myProject = project;
    myNature = bc.getNature();

    myDisposable = Disposer.newDisposable();
    Disposer.register(myDisposable, mySdkPanel);

    myTargetPlayerLabel.setVisible(myNature.isWebPlatform());
    myTargetPlayerCombo.setVisible(myNature.isWebPlatform());

    mySdkPanel.addListener(new ChangeListener() {
      public void stateChanged(final ChangeEvent e) {
        updateAvailableTargetPlayers();
        updateComponentSetCombo();
        updateSdkTableItem(mySdkPanel.getCurrentSdk());
        myTable.refresh();
      }
    }, myDisposable);

    myComponentSetCombo.setModel(new DefaultComboBoxModel(FlexIdeBuildConfiguration.ComponentSet.values()));
    myComponentSetCombo.setRenderer(new ListCellRendererWrapper<FlexIdeBuildConfiguration.ComponentSet>(myComponentSetCombo.getRenderer()) {
      public void customize(JList list, FlexIdeBuildConfiguration.ComponentSet value, int index, boolean selected, boolean hasFocus) {
        setText(value.PRESENTABLE_TEXT);
      }
    });

    final LinkageType defaultLinkage = BCUtils.getDefaultFrameworkLinkage(myNature);
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

    myFrameworkLinkageCombo.setModel(new DefaultComboBoxModel(BCUtils.getSuitableFrameworkLinkages(myNature)));

    ItemListener updateSdkItemsListener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        DefaultMutableTreeNode sdkNode = findSdkNode();
        FlexSdk currentSdk = mySdkPanel.getCurrentSdk();
        if (sdkNode != null && currentSdk != null) {
          updateSdkEntries(sdkNode, currentSdk);
          myTable.refresh();
        }
      }
    };

    myTargetPlayerCombo.addItemListener(updateSdkItemsListener);
    myComponentSetCombo.addItemListener(updateSdkItemsListener);
    myFrameworkLinkageCombo.addItemListener(updateSdkItemsListener);

    myTable = new EditableTreeTable<MyTableItem>("", DEPENDENCY_TYPE_COLUMN) {
      @Override
      protected void render(SimpleColoredComponent c, MyTableItem item) {
        if (item != null) {
          c.append(item.getText(), item.isValid() ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.ERROR_ATTRIBUTES);
          c.setIcon(item.getIcon());
        }
      }
    };
    myTable.setRootVisible(false);
    myTable.getTree().setShowsRootHandles(true);
    myTable.getTree().setLineStyleAngled();

    // we need to add listener *before* ToolbarDecorator's, so our listener is invoked after it
    myTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        updateEditButton();
        updateRemoveButton();
      }
    });

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
    //d.setUpAction(new AnActionButtonRunnable() {
    //  @Override
    //  public void run(AnActionButton anActionButton) {
    //    moveSelection(-1);
    //  }
    //});
    //d.setDownAction(new AnActionButtonRunnable() {
    //  @Override
    //  public void run(AnActionButton anActionButton) {
    //    moveSelection(1);
    //  }
    //});
    myEditAction = new AnActionButton(ProjectBundle.message("module.classpath.button.edit"), IconUtil.getEditIcon()) {
      @Override
      public void actionPerformed(AnActionEvent e) {
        MyTableItem item = myTable.getItemAt(myTable.getSelectedRow());
        editLibrary(((ModuleLibraryItem)item).orderEntry);
      }
    };
    d.addExtraAction(myEditAction);
    JPanel panel = d.createPanel();
    myTablePanel.add(panel, BorderLayout.CENTER);
    myRemoveButton = ToolbarDecorator.findRemoveButton(panel);

    myTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          if (myTable.getSelectedRowCount() == 1) {
            MyTableItem item = myTable.getItemAt(myTable.getSelectedRow());
            if (item instanceof BCItem) {
              FlexIdeBCConfigurable configurable = ((BCItem)item).configurable;
              if (configurable != null) {
                Place place = new Place().putPath(ProjectStructureConfigurable.CATEGORY, ModuleStructureConfigurable.getInstance(myProject))
                  .putPath(MasterDetailsComponent.TREE_OBJECT, configurable.getEditableObject());
                ProjectStructureConfigurable.getInstance(myProject).navigateTo(place, true);
              }
            }
            else if (item instanceof ModuleLibraryItem && canEditLibrary((ModuleLibraryItem)item)) {
              editLibrary(((ModuleLibraryItem)item).orderEntry);
            }
          }
        }
      }
    });

    FlexIdeModuleStructureExtension.getInstance().getConfigurator().addListener(new FlexIdeBCConfigurator.Listener() {
      @Override
      public void moduleRemoved(Module module) {
        // TODO return if module == this module
        List<Integer> rowsToRemove = new ArrayList<Integer>();
        // 1st-level nodes are always visible
        // 2nd-level nodes cannot refer to BC
        for (int row = 0; row < myTable.getRowCount(); row++) {
          MyTableItem item = myTable.getItemAt(row);
          if (item instanceof BCItem) {
            FlexIdeBCConfigurable configurable = ((BCItem)item).configurable;
            if (configurable != null && configurable.getModule() == module) {
              rowsToRemove.add(row);
            }
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
        if (configurable.isParentFor(DependenciesConfigurable.this)) {
          return;
        }

        // 1st-level nodes are always visible
        // 2nd-level nodes cannot refer to BC
        for (int row = 0; row < myTable.getRowCount(); row++) {
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

  @Nullable
  private DefaultMutableTreeNode findSdkNode() {
    for (int row = 0; row < myTable.getRowCount(); row++) {
      MyTableItem item = myTable.getItemAt(row);
      if (myTable.getItemAt(row) instanceof SdkItem) {
        if (item instanceof SdkItem) {
          return (DefaultMutableTreeNode)myTable.getRoot().getChildAt(row);
        }
      }
    }
    return null;
  }

  private void updateEditButton() {
    if (myTable.getSelectedRowCount() == 1) {
      MyTableItem item = myTable.getItemAt(myTable.getSelectedRow());
      if (item instanceof ModuleLibraryItem && canEditLibrary((ModuleLibraryItem)item)) {
        myEditAction.setEnabled(true);
        return;
      }
    }
    myEditAction.setEnabled(false);
  }

  private static boolean canEditLibrary(ModuleLibraryItem item) {
    return item.orderEntry != null;
  }

  private void updateRemoveButton() {
    myRemoveButton.setEnabled(canDeleteSelection());
  }

  private boolean canDeleteSelection() {
    if (myTable.getSelectedRowCount() == 0) return false;
    for (int row : myTable.getSelectedRows()) {
      MyTableItem item = myTable.getItemAt(row);
      if (item instanceof SdkItem || item instanceof SdkEntryItem) return false;
    }
    return true;
  }

  private void editLibrary(LibraryOrderEntry entry) {
    Library library = entry.getLibrary();
    if (library == null) {
      return;
    }

    LibraryTablePresentation presentation = new LibraryTablePresentation() {
      @Override
      public String getDisplayName(boolean plural) {
        return plural ? "Flex Libraries" : "Flex Library";
      }

      @Override
      public String getDescription() {
        return ProjectBundle.message("libraries.node.text.module");
      }

      @Override
      public String getLibraryTableEditorTitle() {
        return "Configure Flex Library";
      }
    };

    LibraryTableModifiableModelProvider provider = new LibraryTableModifiableModelProvider() {
      public LibraryTable.ModifiableModel getModifiableModel() {
        return myModifiableRootModel.getModuleLibraryTable().getModifiableModel();
      }
    };

    StructureConfigurableContext context = ModuleStructureConfigurable.getInstance(myProject).getContext();
    EditExistingLibraryDialog dialog =
      EditExistingLibraryDialog.createDialog(myMainPanel, provider, library, myProject, presentation, context);
    dialog.setContextModule(myModifiableRootModel.getModule());
    dialog.show();
    myTable.refresh();
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
      int index = selectedRows[i] - i;
      myTable.getItemAt(index).onRemove(myModifiableRootModel);
      root.remove(index);
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
    if (myModifiableRootModel.isChanged()) return true;

    FlexSdk currentSdk = mySdkPanel.getCurrentSdk();
    SdkEntry sdkEntry = myDependencies.getSdkEntry();
    if (currentSdk != null) {
      if (sdkEntry == null || !currentSdk.getHomePath().equals(sdkEntry.getHomePath())) return true;
    }
    else {
      if (sdkEntry != null) return true;
    }

    final String targetPlayer = (String)myTargetPlayerCombo.getSelectedItem();
    if (myTargetPlayerCombo.isVisible() && targetPlayer != null && !myDependencies.TARGET_PLAYER.equals(targetPlayer)) return true;
    if (myComponentSetCombo.isVisible() && myDependencies.COMPONENT_SET != myComponentSetCombo.getSelectedItem()) return true;
    if (myDependencies.getFrameworkLinkage() != myFrameworkLinkageCombo.getSelectedItem()) return true;

    List<MyTableItem> items = myTable.getItems();
    items = ContainerUtil.filter(items, new Condition<MyTableItem>() {
      @Override
      public boolean value(MyTableItem item) {
        return !(item instanceof SdkItem || item instanceof SdkEntryItem);
      }
    });

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
        if (bcItem.configurable != null) {
          if (bcItem.configurable.getModule() != bcEntry.getModule()) return true;
          if (!bcItem.configurable.getDisplayName().equals(bcEntry.getBcName())) return true;
        }
        else {
          if (bcItem.moduleName.equals(bcEntry.getModuleName())) return true;
          if (bcItem.bcName.equals(bcEntry.getBcName())) return true;
        }
        if (!bcItem.dependencyType.isEqual(entry.getDependencyType())) return true;
      }
      else if (item instanceof ModuleLibraryItem) {
        if (!(entry instanceof ModuleLibraryEntry)) {
          return true;
        }
        ModuleLibraryItem libraryItem = (ModuleLibraryItem)item;
        ModuleLibraryEntry libraryEntry = (ModuleLibraryEntry)entry;
        if (!libraryEntry.getLibraryId().equals(libraryItem.libraryId)) {
          return true;
        }
        if (!libraryItem.dependencyType.isEqual(entry.getDependencyType())) return true;
      }
    }
    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myDependencies);

    if (myModifiableRootModel.isChanged()) {
      ModuleStructureConfigurable.getInstance(myProject).getContext().getModulesConfigurator().apply();
    }
  }

  public void applyTo(final Dependencies dependencies) {
    final Object targetPlayer = myTargetPlayerCombo.getSelectedItem();
    if (targetPlayer != null) {
      dependencies.TARGET_PLAYER = (String)targetPlayer;
    }
    dependencies.COMPONENT_SET = (FlexIdeBuildConfiguration.ComponentSet)myComponentSetCombo.getSelectedItem();
    dependencies.setFrameworkLinkage((LinkageType)myFrameworkLinkageCombo.getSelectedItem());

    dependencies.getEntries().clear();
    List<MyTableItem> items = myTable.getItems();
    for (MyTableItem item : items) {
      DependencyEntry entry;
      if (item instanceof BCItem) {
        FlexIdeBCConfigurable configurable = ((BCItem)item).configurable;
        if (configurable != null) {
          entry = new BuildConfigurationEntry(configurable.getModule(), configurable.getDisplayName());
        }
        else {
          entry = new BuildConfigurationEntry(myProject, ((BCItem)item).moduleName, ((BCItem)item).bcName);
        }
        ((BCItem)item).dependencyType.applyTo(entry.getDependencyType());
        dependencies.getEntries().add(entry);
      }
      else if (item instanceof ModuleLibraryItem) {
        entry = new ModuleLibraryEntry(((ModuleLibraryItem)item).libraryId);
        ((ModuleLibraryItem)item).dependencyType.applyTo(entry.getDependencyType());
        dependencies.getEntries().add(entry);
      }
      else if (item instanceof SdkItem || item instanceof SdkEntryItem) {
        // ignore
      }
      else {
        throw new IllegalArgumentException("unexpected item type: " + item);
      }
    }

    FlexSdk currentSdk = mySdkPanel.getCurrentSdk();
    if (currentSdk != null) {
      SdkEntry sdkEntry = new SdkEntry();
      sdkEntry.setHomePath(currentSdk.getHomePath());
      dependencies.setSdkEntry(sdkEntry);
    }
    else {
      dependencies.setSdkEntry(null);
    }
  }

  public void reset() {
    SdkEntry sdkEntry = myDependencies.getSdkEntry();
    mySdkPanel.reset();
    mySdkPanel.setCurrentHomePath(sdkEntry != null ? sdkEntry.getHomePath() : null);

    updateAvailableTargetPlayers();
    myTargetPlayerCombo.setSelectedItem(myDependencies.TARGET_PLAYER);

    updateComponentSetCombo();
    myComponentSetCombo.setSelectedItem(myDependencies.COMPONENT_SET);

    myFrameworkLinkageCombo.setSelectedItem(myDependencies.getFrameworkLinkage());

    DefaultMutableTreeNode root = myTable.getRoot();
    root.removeAllChildren();

    if (sdkEntry != null) {
      FlexSdk flexSdk = mySdksModel.findOrCreateSdk(sdkEntry.getHomePath());
      if (flexSdk != null) {
        DefaultMutableTreeNode sdkNode = new DefaultMutableTreeNode(new SdkItem(flexSdk), true);
        myTable.getRoot().insert(sdkNode, 0);
        updateSdkEntries(sdkNode, flexSdk);
      }
    }
    FlexIdeBCConfigurator configurator = FlexIdeModuleStructureExtension.getInstance().getConfigurator();
    for (DependencyEntry entry : myDependencies.getEntries()) {
      MyTableItem item = null;
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;
        Module module = bcEntry.getModule();
        NamedConfigurable<FlexIdeBuildConfiguration> configurable =
          module != null ? ContainerUtil
            .find(configurator.getBCConfigurables(module), new Condition<NamedConfigurable<FlexIdeBuildConfiguration>>() {
              @Override
              public boolean value(NamedConfigurable<FlexIdeBuildConfiguration> configurable) {
                return configurable.getEditableObject().NAME.equals(bcEntry.getBcName());
              }
            }) : null;
        if (configurable == null) {
          item = new BCItem(bcEntry.getModuleName(), bcEntry.getBcName());
        }
        else {
          item = new BCItem(FlexIdeBCConfigurable.unwrapIfNeeded(configurable));
        }
        entry.getDependencyType().applyTo(((BCItem)item).dependencyType);
      }
      else if (entry instanceof ModuleLibraryEntry) {
        ModuleLibraryEntry moduleLibraryEntry = (ModuleLibraryEntry)entry;
        item = new ModuleLibraryItem(moduleLibraryEntry.getLibraryId(), moduleLibraryEntry.findOrderEntry(myModifiableRootModel));
        entry.getDependencyType().applyTo(((ModuleLibraryItem)item).dependencyType);
      }
      if (item != null) {
        root.add(new DefaultMutableTreeNode(item, false));
      }
    }
    myTable.refresh();
    updateEditButton();
  }

  private void updateAvailableTargetPlayers() {
    final FlexSdk currentSdk = mySdkPanel.getCurrentSdk();
    final String sdkHome = currentSdk == null ? null : currentSdk.getHomePath();
    final String playerFolderPath = sdkHome == null ? null : sdkHome + "/frameworks/libs/player";
    if (playerFolderPath != null) {
      final VirtualFile playerDir = ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
        public VirtualFile compute() {
          final VirtualFile playerFolder = LocalFileSystem.getInstance().refreshAndFindFileByPath(playerFolderPath);
          if (playerFolder != null && playerFolder.isDirectory()) {
            playerFolder.refresh(false, true);
            return playerFolder;
          }
          return null;
        }
      });

      if (playerDir != null) {
        final Collection<String> availablePlayers = new ArrayList<String>(2);
        FlexSdkUtils.processPlayerglobalSwcFiles(playerDir, new Processor<VirtualFile>() {
          public boolean process(final VirtualFile playerglobalSwcFile) {
            availablePlayers.add(playerglobalSwcFile.getParent().getName());
            return true;
          }
        });

        final Object selectedItem = myTargetPlayerCombo.getSelectedItem();
        myTargetPlayerCombo.setModel(new DefaultComboBoxModel(availablePlayers.toArray(new String[availablePlayers.size()])));
        if (selectedItem != null) {
          myTargetPlayerCombo.setSelectedItem(selectedItem);
        }
      }
    }
  }

  private void updateComponentSetCombo() {
    final FlexSdk sdkEntry = mySdkPanel.getCurrentSdk();
    final boolean visible = sdkEntry != null &&
                            StringUtil.compareVersionNumbers(sdkEntry.getFlexVersion(), "4") >= 0 &&
                            !myNature.isMobilePlatform() &&
                            !myNature.pureAS;
    myComponentSetLabel.setVisible(visible);
    myComponentSetCombo.setVisible(visible);
    if (visible) {
      final Object selectedItem = myComponentSetCombo.getSelectedItem();
      final ComponentSet[] values = StringUtil.compareVersionNumbers(sdkEntry.getFlexVersion(), "4.5") >= 0
                                    ? ComponentSet.values()
                                    : new ComponentSet[]{ComponentSet.SparkAndMx, ComponentSet.MxOnly};
      myComponentSetCombo.setModel(new DefaultComboBoxModel(values));
      myComponentSetCombo.setSelectedItem(selectedItem);
    }
  }

  private void updateSdkTableItem(@Nullable FlexSdk sdk) {
    DefaultMutableTreeNode sdkNode = findSdkNode();
    if (sdk != null) {
      SdkItem sdkItem = new SdkItem(sdk);
      if (sdkNode != null) {
        sdkNode.setUserObject(sdkItem);
      }
      else {
        sdkNode = new DefaultMutableTreeNode(sdkItem, true);
        myTable.getRoot().insert(sdkNode, 0);
      }
      updateSdkEntries(sdkNode, sdk);
    }
    else if (sdkNode != null) {
      myTable.getRoot().remove(sdkNode);
    }
  }

  private void updateSdkEntries(DefaultMutableTreeNode sdkNode, FlexSdk sdk) {
    sdkNode.removeAllChildren();
    ComponentSet componentSet = (ComponentSet)myComponentSetCombo.getSelectedItem();
    String targetPlayer = (String)myTargetPlayerCombo.getSelectedItem();

    for (String url : sdk.getRoots(OrderRootType.CLASSES)) {
      url = VirtualFileManager.extractPath(StringUtil.trimEnd(url, JarFileSystem.JAR_SEPARATOR));
      LinkageType linkageType = BCUtils.getSdkEntryLinkageType(url, myNature, targetPlayer, componentSet);
      if (linkageType == null) {
        // this url is not applicable
        continue;
      }

      if (linkageType == LinkageType.Default) {
        linkageType = (LinkageType)myFrameworkLinkageCombo.getSelectedItem();
        if (linkageType == LinkageType.Default) {
          linkageType = BCUtils.getDefaultFrameworkLinkage(myNature);
        }
      }

      SdkEntryItem item = new SdkEntryItem(FileUtil.toSystemDependentName(url), linkageType);
      sdkNode.add(new DefaultMutableTreeNode(item, false));
    }
  }

  public void disposeUIResources() {
    Disposer.dispose(myDisposable);
  }

  private void createUIComponents() {
    mySdkPanel = new FlexSdkPanel(mySdksModel);
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
          FlexIdeBCConfigurable configurable = ((BCItem)item).configurable;
          if (configurable != null) {
            dependencies.add(configurable);
          }
        }
      }

      Map<Module, List<FlexIdeBCConfigurable>> treeItems = new HashMap<Module, List<FlexIdeBCConfigurable>>();
      FlexIdeBCConfigurator configurator = FlexIdeModuleStructureExtension.getInstance().getConfigurator();
      for (Module module : ModuleStructureConfigurable.getInstance(myProject).getModules()) {
        if (ModuleType.get(module) != FlexModuleType.getInstance()) {
          continue;
        }
        for (NamedConfigurable<FlexIdeBuildConfiguration> configurable : configurator.getBCConfigurables(module)) {
          FlexIdeBCConfigurable flexIdeBCConfigurable = FlexIdeBCConfigurable.unwrapIfNeeded(configurable);
          if (dependencies.contains(flexIdeBCConfigurable) || flexIdeBCConfigurable.isParentFor(DependenciesConfigurable.this)) {
            continue;
          }
          FlexIdeBuildConfiguration.OutputType outputType = flexIdeBCConfigurable.getOutputType();
          if (outputType != FlexIdeBuildConfiguration.OutputType.Library &&
              outputType != FlexIdeBuildConfiguration.OutputType.RuntimeLoadedModule) {
            continue;
          }

          List<FlexIdeBCConfigurable> list = treeItems.get(module);
          if (list == null) {
            list = new ArrayList<FlexIdeBCConfigurable>();
            treeItems.put(module, list);
          }
          list.add(flexIdeBCConfigurable);
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
      LibraryTable.ModifiableModel librariesModel = myModifiableRootModel.getModuleLibraryTable().getModifiableModel();
      Module module = myModifiableRootModel.getModule();
      List<? extends FlexLibraryType> libraryTypes = Collections.singletonList(new FlexLibraryType() {
        @Override
        public LibraryRootsComponentDescriptor createLibraryRootsComponentDescriptor() {
          return new FlexLibraryRootsComponentDescriptor() {
            @NotNull
            @Override
            public List<? extends RootDetector> getRootDetectors() {
              return Arrays.asList(SWC_LIBRARY_DETECTOR);
            }
          };
        }

        @NotNull
        @Override
        public FlexLibraryProperties createDefaultProperties() {
          return new FlexLibraryProperties(UUID.randomUUID().toString());
        }
      });
      CreateModuleLibraryChooser c = new CreateModuleLibraryChooser(libraryTypes, myMainPanel, module, librariesModel);
      try {
        c.doChoose();
        if (!c.isOK()) {
          return;
        }
        final List<Library> libraries = c.getChosenElements();
        if (libraries.isEmpty()) {
          return;
        }

        DefaultMutableTreeNode rootNode = myTable.getRoot();
        for (Library library : libraries) {
          String libraryId = ((FlexLibraryProperties)((LibraryEx)library).getProperties()).getId();
          LibraryOrderEntry libraryEntry = myModifiableRootModel.findLibraryOrderEntry(library);
          rootNode.add(new DefaultMutableTreeNode(new ModuleLibraryItem(libraryId, libraryEntry), false));
        }
        myTable.refresh();
        myTable.getSelectionModel().clearSelection();
        int rowCount = myTable.getRowCount();
        myTable.getSelectionModel().addSelectionInterval(rowCount - libraries.size(), rowCount - 1);
      }
      finally {
        Disposer.dispose(c);
      }
    }
  }
}
