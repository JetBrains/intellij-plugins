package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryRootsComponentDescriptor;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.FlexLibraryConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexLibraryIdGenerator;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablePresentation;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.roots.ui.OrderEntryAppearanceService;
import com.intellij.openapi.roots.ui.configuration.LibraryTableModifiableModelProvider;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.classpath.CreateModuleLibraryChooser;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.EditExistingLibraryDialog;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.*;
import com.intellij.ui.components.editors.JBComboBoxTableCellEditorComponent;
import com.intellij.ui.navigation.Place;
import com.intellij.util.Function;
import com.intellij.util.IconUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.FilteringIterator;
import com.intellij.util.containers.HashMap;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.classpath.ChooseLibrariesFromTablesDialog;
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
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public class DependenciesConfigurable extends NamedConfigurable<Dependencies> {
  private static final Icon MISSING_BC_ICON = null;

  private JPanel myMainPanel;
  private FlexSdkPanel mySdkPanel;
  private JLabel myTargetPlayerLabel;
  private JComboBox myTargetPlayerCombo;
  private JLabel myTargetPlayerWarning;
  private JLabel myComponentSetLabel;
  private JComboBox myComponentSetCombo;
  private JLabel myFrameworkLinkageLabel;
  private JComboBox myFrameworkLinkageCombo;
  private JLabel myWarning;
  private JPanel myTablePanel;

  private final EditableTreeTable<MyTableItem> myTable;

  private final Project myProject;
  private final FlexProjectConfigurationEditor myConfigEditor;
  private final BuildConfigurationNature myNature;
  private final ModifiableDependencies myDependencies;
  private AddItemPopupAction[] myPopupActions;
  private final AnActionButton myEditAction;
  private final AnActionButton myRemoveButton;
  private final Disposable myDisposable;

  private abstract static class MyTableItem {
    public abstract String getText();

    @Nullable
    public abstract Icon getIcon();

    public abstract boolean showLinkage();

    public abstract boolean isLinkageEditable();

    public abstract LinkageType getLinkageType();

    public abstract void setLinkageType(LinkageType linkageType);

    public abstract boolean isValid();

    public abstract void onDoubleClick();

    @Nullable
    public abstract ModifiableDependencyEntry apply(ModifiableDependencies dependencies);

    public abstract boolean isModified(DependencyEntry entry);

    public abstract boolean canEdit();
  }

  private class BCItem extends MyTableItem {
    public final ModifiableDependencyType dependencyType = Factory.createDependencyTypeInstance();
    public final FlexIdeBCConfigurable configurable;
    public final String moduleName;
    public final String bcName;

    public BCItem(@NotNull String moduleName, @NotNull String bcName) {
      this.moduleName = moduleName;
      this.bcName = bcName;
      this.configurable = null;
    }

    public BCItem(@NotNull FlexIdeBCConfigurable configurable) {
      this.moduleName = null;
      this.bcName = null;
      this.configurable = configurable;
      if (configurable.getOutputType() == OutputType.RuntimeLoadedModule) {
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

    @Nullable
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
      return configurable != null && configurable.getOutputType() != OutputType.RuntimeLoadedModule;
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

    public void onDoubleClick() {
      if (configurable != null) {
        Project project = configurable.getModule().getProject();
        Place place = new Place().putPath(ProjectStructureConfigurable.CATEGORY, ModuleStructureConfigurable.getInstance(project))
          .putPath(MasterDetailsComponent.TREE_OBJECT, configurable.getEditableObject());
        ProjectStructureConfigurable.getInstance(project).navigateTo(place, true);
      }
    }

    public ModifiableDependencyEntry apply(ModifiableDependencies dependencies) {
      ModifiableDependencyEntry entry;
      if (configurable != null) {
        // configurable may be not yet applied at the moment
        entry = myConfigEditor.createBcEntry(dependencies, configurable.getEditableObject(), configurable.getDisplayName());
      }
      else {
        entry = myConfigEditor.createBcEntry(dependencies, moduleName, bcName);
      }
      entry.getDependencyType().copyFrom(dependencyType);
      return entry;
    }

    public boolean isModified(final DependencyEntry entry) {
      if (!(entry instanceof BuildConfigurationEntry)) {
        return true;
      }
      BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;
      if (configurable != null) {
        if (configurable.getModule() != bcEntry.findModule()) return true;
        if (!configurable.getDisplayName().equals(bcEntry.getBcName())) return true;
      }
      else {
        if (moduleName.equals(bcEntry.getModuleName())) return true;
        if (bcName.equals(bcEntry.getBcName())) return true;
      }
      if (!dependencyType.isEqual(entry.getDependencyType())) return true;

      return false;
    }

    public boolean canEdit() {
      return false;
    }
  }

  private class ModuleLibraryItem extends MyTableItem {
    public final ModifiableDependencyType dependencyType = Factory.createDependencyTypeInstance();
    public final String libraryId;
    @Nullable
    public final LibraryOrderEntry orderEntry;

    private final Project project;

    public ModuleLibraryItem(@NotNull String libraryId, @Nullable LibraryOrderEntry orderEntry, @NotNull Project project) {
      this.libraryId = libraryId;
      this.orderEntry = orderEntry;
      this.project = project;
    }

    @Override
    public String getText() {
      if (orderEntry != null) {
        Library library = orderEntry.getLibrary();
        if (library != null) {
          boolean hasInvalidRoots = !((LibraryEx)library).getInvalidRootUrls(OrderRootType.CLASSES).isEmpty();
          return OrderEntryAppearanceService.getInstance().forLibrary(project, library, hasInvalidRoots).getText();
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

    public void onDoubleClick() {
      if (canEdit()) {
        editLibrary(orderEntry);
      }
    }

    public ModifiableDependencyEntry apply(final ModifiableDependencies dependencies) {
      ModifiableDependencyEntry entry = myConfigEditor.createModuleLibraryEntry(dependencies, libraryId);
      entry.getDependencyType().copyFrom(dependencyType);
      return entry;
    }

    public boolean isModified(final DependencyEntry entry) {
      if (!(entry instanceof ModuleLibraryEntry)) {
        return true;
      }
      ModuleLibraryEntry libraryEntry = (ModuleLibraryEntry)entry;
      if (!libraryEntry.getLibraryId().equals(libraryId)) {
        return true;
      }
      if (!dependencyType.isEqual(entry.getDependencyType())) return true;

      return false;
    }

    public boolean canEdit() {
      return isValid();
    }
  }

  private class SharedLibraryItem extends MyTableItem {
    public final ModifiableDependencyType dependencyType = Factory.createDependencyTypeInstance();
    public final String libraryName;
    public final String libraryLevel;
    @Nullable
    public final FlexLibraryConfigurable configurable;

    private final Project project;

    public SharedLibraryItem(@NotNull String libraryName,
                             @NotNull String libraryLevel,
                             @Nullable FlexLibraryConfigurable configurable,
                             @NotNull Project project) {
      this.libraryName = libraryName;
      this.libraryLevel = libraryLevel;
      this.configurable = configurable;
      this.project = project;
    }

    @Override
    public String getText() {
      if (configurable != null) {
        final LibraryEx library = configurable.getLibraryForPresentation();
        return OrderEntryAppearanceService.getInstance().forLibrary(project, library, false).getText();
      }
      return libraryName;
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
      return configurable != null;
    }

    public void onDoubleClick() {
      if (configurable != null) {
        Place place = new Place().putPath(ProjectStructureConfigurable.CATEGORY, ModuleStructureConfigurable.getInstance(project))
          .putPath(MasterDetailsComponent.TREE_OBJECT, configurable.getEditableObject());
        ProjectStructureConfigurable.getInstance(project).navigateTo(place, true);
      }
    }

    public ModifiableDependencyEntry apply(final ModifiableDependencies dependencies) {
      ModifiableDependencyEntry entry;
      if (configurable != null) {
        entry = myConfigEditor.createSharedLibraryEntry(dependencies, configurable.getDisplayName(),
                                                        configurable.getEditableObject().getTable().getTableLevel());
      }
      else {
        entry = myConfigEditor.createSharedLibraryEntry(dependencies, libraryName, libraryLevel);
      }
      entry.getDependencyType().copyFrom(dependencyType);
      return entry;
    }

    public boolean isModified(final DependencyEntry entry) {
      if (!(entry instanceof ModifiableSharedLibraryEntry)) {
        return true;
      }
      ModifiableSharedLibraryEntry libraryEntry = (ModifiableSharedLibraryEntry)entry;
      if (configurable != null) {
        if (!configurable.getDisplayName().equals(libraryEntry.getLibraryName())) return true;
        if (!configurable.getEditableObject().getTable().getTableLevel().equals(libraryEntry.getLibraryLevel())) return true;
      }
      else {
        if (!libraryName.equals(libraryEntry.getLibraryName())) return true;
        if (!libraryLevel.equals(libraryEntry.getLibraryLevel())) return true;
      }

      if (!dependencyType.isEqual(entry.getDependencyType())) return true;
      return false;
    }

    public boolean canEdit() {
      return false;
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
      return true; // we just don't add Flex SDK table item for invalid SDKs
    }

    public void onDoubleClick() {
      // ignore
    }

    public ModifiableDependencyEntry apply(final ModifiableDependencies dependencies) {
      // ignore
      return null;
    }

    public boolean isModified(final DependencyEntry entry) {
      return false;
    }

    public boolean canEdit() {
      return false;
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

    public void onDoubleClick() {
      // ignore
    }

    public ModifiableDependencyEntry apply(final ModifiableDependencies dependencies) {
      return null; // ignore
    }

    public boolean isModified(final DependencyEntry entry) {
      return false;
    }

    public boolean canEdit() {
      return false;
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
      ((JLabel)component).setHorizontalAlignment(SwingConstants.CENTER);
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
    private JBComboBoxTableCellEditorComponent myCombo;

    public Object getCellEditorValue() {
      return myCombo.getEditorValue();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      myCombo = new JBComboBoxTableCellEditorComponent(table);
      myCombo.setCell(table, row, column);
      myCombo.setOptions(LinkageType.getSwcLinkageValues());
      myCombo.setDefaultValue(value);
      myCombo.setToString(new Function<Object, String>() {
        public String fun(final Object o) {
          return ((LinkageType)o).getShortText();
        }
      });
      return myCombo;
    }
  };

  private static final ColumnInfo<MyTableItem, LinkageType> DEPENDENCY_TYPE_COLUMN = new ColumnInfo<MyTableItem, LinkageType>("Linkage") {

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
      return new JLabel(LinkageType.External.getShortText()).getPreferredSize().width + 20;
    }
  };

  public DependenciesConfigurable(final ModifiableFlexIdeBuildConfiguration bc,
                                  Project project,
                                  @NotNull FlexProjectConfigurationEditor configEditor) {
    myConfigEditor = configEditor;
    myDependencies = bc.getDependencies();
    myProject = project;
    myNature = bc.getNature();

    myDisposable = Disposer.newDisposable();
    Disposer.register(myDisposable, mySdkPanel);

    myTargetPlayerLabel.setVisible(myNature.isWebPlatform());
    myTargetPlayerCombo.setVisible(myNature.isWebPlatform());

    myFrameworkLinkageLabel.setVisible(!myNature.pureAS);
    myFrameworkLinkageCombo.setVisible(!myNature.pureAS);

    mySdkPanel.addListener(new ChangeListener() {
      public void stateChanged(final ChangeEvent e) {
        BCUtils.updateAvailableTargetPlayers(mySdkPanel, myTargetPlayerCombo);
        updateComponentSetCombo();
        updateSdkTableItem(mySdkPanel.getCurrentSdk());
        myTable.refresh();
      }
    }, myDisposable);

    myComponentSetCombo.setModel(new DefaultComboBoxModel(ComponentSet.values()));
    myComponentSetCombo.setRenderer(new ListCellRendererWrapper<ComponentSet>(myComponentSetCombo.getRenderer()) {
      public void customize(JList list, ComponentSet value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPresentableText());
      }
    });

    myFrameworkLinkageCombo
      .setRenderer(new ListCellRendererWrapper<LinkageType>(myFrameworkLinkageCombo.getRenderer()) {
        public void customize(JList list, LinkageType value, int index, boolean selected, boolean hasFocus) {
          if (value == LinkageType.Default) {
            final FlexSdk sdk = mySdkPanel.getCurrentSdk();
            final String sdkVersion = sdk != null ? sdk.getFlexVersion() : null;
            setText(sdkVersion == null
                    ? "Default"
                    : MessageFormat.format("Default ({0})", BCUtils.getDefaultFrameworkLinkage(sdkVersion, myNature).getLongText()));
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

    myTargetPlayerWarning.setIcon(IconLoader.getIcon("smallWarning.png"));
    myWarning.setIcon(UIUtil.getBalloonWarningIcon());

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
            myTable.getItemAt(myTable.getSelectedRow()).onDoubleClick();
          }
        }
      }
    });

    FlexIdeBuildConfigurationsExtension.getInstance().getConfigurator().addListener(new FlexIdeBCConfigurator.Listener() {
      @Override
      public void moduleRemoved(Module module) {
        // TODO return if module == this module
        Set<MyTableItem> itemsToRemove = new HashSet<MyTableItem>();
        // 1st-level nodes are always visible
        // 2nd-level nodes cannot refer to BC
        for (int row = 0; row < myTable.getRowCount(); row++) {
          MyTableItem item = myTable.getItemAt(row);
          if (item instanceof BCItem) {
            FlexIdeBCConfigurable configurable = ((BCItem)item).configurable;
            if (configurable != null && configurable.getModule() == module) {
              itemsToRemove.add(item);
            }
          }
        }

        removeItems(itemsToRemove);
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
            removeItems(Collections.singleton(item));
            // there may be only one dependency on a BC
            break;
          }
        }
      }
    }, myDisposable);

    myConfigEditor.addModulesModelChangeListener(new FlexProjectConfigurationEditor.ModulesModelChangeListener() {
      @Override
      public void modulesModelsChanged(Collection<Module> modules) {
        FlexIdeBCConfigurator configurator = FlexIdeBuildConfigurationsExtension.getInstance().getConfigurator();
        for (Module module : modules) {
          for (CompositeConfigurable configurable : configurator.getBCConfigurables(module)) {
            FlexIdeBCConfigurable flexIdeBCConfigurable = FlexIdeBCConfigurable.unwrap(configurable);
            if (flexIdeBCConfigurable.isParentFor(DependenciesConfigurable.this)) {
              resetTable(myDependencies.getSdkEntry(), true);
            }
          }
        }
      }
    }, myDisposable);
  }

  @Nullable
  FlexSdk getCurrentSdk() {
    return mySdkPanel.getCurrentSdk();
  }

  void addSdkChangeListener(final ChangeListener changeListener) {
    mySdkPanel.addListener(changeListener, myDisposable);
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
      myEditAction.setEnabled(item != null && item.canEdit());
      return;
    }
    myEditAction.setEnabled(false);
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
        return myConfigEditor.getLibraryModel(myDependencies);
      }
    };

    StructureConfigurableContext context = ModuleStructureConfigurable.getInstance(myProject).getContext();
    EditExistingLibraryDialog dialog =
      EditExistingLibraryDialog.createDialog(myMainPanel, provider, library, myProject, presentation, context);
    dialog.setContextModule(myConfigEditor.getModule(myDependencies));
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
    TableUtil.stopEditing(myTable);
    int[] selectedRows = myTable.getSelectedRows();
    Set<MyTableItem> itemsToRemove = new HashSet<MyTableItem>(selectedRows.length);
    for (int row : selectedRows) {
      itemsToRemove.add(myTable.getItemAt(row));
    }
    removeItems(itemsToRemove);
    if (myTable.getRowCount() > 0) {
      int toSelect = Math.min(myTable.getRowCount() - 1, selectedRows[0]);
      myTable.clearSelection();
      myTable.getSelectionModel().addSelectionInterval(toSelect, toSelect);
    }
  }

  private void removeItems(Set<MyTableItem> itemsToDelete) {
    if (itemsToDelete.isEmpty()) return;

    DefaultMutableTreeNode root = myTable.getRoot();

    for (int i = 0; i < root.getChildCount(); ) {
      Object item = ((DefaultMutableTreeNode)root.getChildAt(i)).getUserObject();
      if (itemsToDelete.contains(((MyTableItem)item))) {
        root.remove(i);
      }
      else {
        i++;
      }
    }
    myTable.refresh();
  }

  private void moveSelection(int delta) {
    // TODO check the case when SDK item is expanded!
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
    String currentSdkId = mySdkPanel.getCurrentSdkId();
    SdkEntry sdkEntry = myDependencies.getSdkEntry();
    if (currentSdkId != null) {
      if (sdkEntry == null) {
        return mySdkPanel.getCurrentSdk() != null;
      }
      else if (!currentSdkId.equals(sdkEntry.getLibraryId())) {
        return true;
      }
    }
    else {
      if (sdkEntry != null) return true;
    }

    final String targetPlayer = (String)myTargetPlayerCombo.getSelectedItem();
    if (myTargetPlayerCombo.isVisible() && targetPlayer != null && !myDependencies.getTargetPlayer().equals(targetPlayer)) return true;
    if (myComponentSetCombo.isVisible() && myDependencies.getComponentSet() != myComponentSetCombo.getSelectedItem()) return true;
    if (myDependencies.getFrameworkLinkage() != myFrameworkLinkageCombo.getSelectedItem()) return true;

    List<MyTableItem> items = myTable.getItems();
    items = ContainerUtil.filter(items, new Condition<MyTableItem>() {
      @Override
      public boolean value(MyTableItem item) {
        return !(item instanceof SdkItem || item instanceof SdkEntryItem);
      }
    });

    DependencyEntry[] entries = myDependencies.getEntries();
    if (items.size() != entries.length) return true;
    for (int i = 0; i < items.size(); i++) {
      MyTableItem item = items.get(i);
      DependencyEntry entry = entries[i];
      if (item.isModified(entry)) return true;
    }
    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myDependencies);
  }

  public void applyTo(final ModifiableDependencies dependencies) {
    final Object targetPlayer = myTargetPlayerCombo.getSelectedItem();
    if (targetPlayer != null) {
      dependencies.setTargetPlayer((String)targetPlayer);
    }
    dependencies.setComponentSet((ComponentSet)myComponentSetCombo.getSelectedItem());
    dependencies.setFrameworkLinkage((LinkageType)myFrameworkLinkageCombo.getSelectedItem());

    List<MyTableItem> items = myTable.getItems();
    List<ModifiableDependencyEntry> newEntries = new ArrayList<ModifiableDependencyEntry>();
    for (MyTableItem item : items) {
      ModifiableDependencyEntry entry = item.apply(dependencies);
      if (entry != null) {
        newEntries.add(entry);
      }
    }
    myConfigEditor.setEntries(dependencies, newEntries);

    FlexSdk currentSdk = mySdkPanel.getCurrentSdk();
    if (currentSdk != null) {
      SdkEntry sdkEntry = Factory.createSdkEntry(currentSdk.getLibraryId(), currentSdk.getHomePath());
      dependencies.setSdkEntry(sdkEntry);
    }
    else {
      dependencies.setSdkEntry(null);
    }
  }

  public void reset() {
    SdkEntry sdkEntry = myDependencies.getSdkEntry();
    mySdkPanel.reset();
    mySdkPanel.setCurrentSdk(sdkEntry);

    BCUtils.updateAvailableTargetPlayers(mySdkPanel, myTargetPlayerCombo);
    myTargetPlayerCombo.setSelectedItem(myDependencies.getTargetPlayer());
    overriddenTargetPlayerChanged(null); // no warning initially

    updateComponentSetCombo();
    myComponentSetCombo.setSelectedItem(myDependencies.getComponentSet());

    myFrameworkLinkageCombo.setSelectedItem(myDependencies.getFrameworkLinkage());

    resetTable(sdkEntry, false);
  }

  private void resetTable(SdkEntry sdkEntry, boolean keepSelection) {
    int[] selectedRows = keepSelection ? myTable.getSelectedRows() : new int[0];

    DefaultMutableTreeNode root = myTable.getRoot();
    root.removeAllChildren();

    if (sdkEntry != null) {
      FlexSdk flexSdk = myConfigEditor.findSdk(sdkEntry.getLibraryId());
      if (flexSdk != null) {
        DefaultMutableTreeNode sdkNode = new DefaultMutableTreeNode(new SdkItem(flexSdk), true);
        myTable.getRoot().insert(sdkNode, 0);
        updateSdkEntries(sdkNode, flexSdk);
      }
    }
    FlexIdeBCConfigurator configurator = FlexIdeBuildConfigurationsExtension.getInstance().getConfigurator();
    for (DependencyEntry entry : myDependencies.getEntries()) {
      MyTableItem item = null;
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;
        Module module = bcEntry.findModule();
        CompositeConfigurable configurable =
          module != null ? ContainerUtil.find(configurator.getBCConfigurables(module), new Condition<CompositeConfigurable>() {
            @Override
            public boolean value(CompositeConfigurable configurable) {
              return configurable.getDisplayName().equals(bcEntry.getBcName());
            }
          }) : null;
        if (configurable == null) {
          item = new BCItem(bcEntry.getModuleName(), bcEntry.getBcName());
        }
        else {
          item = new BCItem(FlexIdeBCConfigurable.unwrap(configurable));
        }
        ((BCItem)item).dependencyType.copyFrom(entry.getDependencyType());
      }
      else if (entry instanceof ModuleLibraryEntry) {
        ModuleLibraryEntry moduleLibraryEntry = (ModuleLibraryEntry)entry;
        item = new ModuleLibraryItem(moduleLibraryEntry.getLibraryId(),
                                     myConfigEditor.findLibraryOrderEntry(myDependencies, moduleLibraryEntry),
                                     myProject);
        ((ModuleLibraryItem)item).dependencyType.copyFrom(entry.getDependencyType());
      }
      else if (entry instanceof SharedLibraryEntry) {
        SharedLibraryEntry moduleLibraryEntry = (SharedLibraryEntry)entry;
        LibrariesModifiableModel model = ProjectStructureConfigurable.getInstance(myProject).getContext()
          .createModifiableModelProvider(moduleLibraryEntry.getLibraryLevel()).getModifiableModel();
        Library library = model.getLibraryByName(moduleLibraryEntry.getLibraryName());
        item = new SharedLibraryItem(moduleLibraryEntry.getLibraryName(), moduleLibraryEntry.getLibraryLevel(),
                                     library != null ? findConfigurable(library) : null,
                                     myProject);
        ((SharedLibraryItem)item).dependencyType.copyFrom(entry.getDependencyType());
      }
      if (item != null) {
        root.add(new DefaultMutableTreeNode(item, false));
      }
    }
    myTable.refresh();

    myTable.clearSelection();
    for (int row : selectedRows) {
      myTable.getSelectionModel().addSelectionInterval(row, row);
    }
    updateEditButton();
  }

  @Nullable
  private FlexLibraryConfigurable findConfigurable(@NotNull Library library) {
    ModuleStructureConfigurable modulesConfig = ProjectStructureConfigurable.getInstance(myProject).getModulesConfig();
    TreeNode root = (TreeNode)modulesConfig.getTree().getModel().getRoot();
    MasterDetailsComponent.MyNode configurableNode = MasterDetailsComponent.findNodeByObject(root, library);
    return configurableNode != null ? (FlexLibraryConfigurable)configurableNode.getConfigurable() : null;
  }

  /**
   * Called when {@link CompilerOptionsConfigurable} is initialized and when path to additional config file is changed
   *
   * @param targetPlayer <code>null</code> means that the value is not overridden in additional config file
   */
  public void overriddenTargetPlayerChanged(final @Nullable String targetPlayer) {
    myTargetPlayerWarning.setToolTipText(FlexBundle.message("actual.value.from.config.file.0", targetPlayer));
    myWarning.setText(FlexBundle.message("overridden.in.config.file", "Target player", targetPlayer));

    final boolean visible = myTargetPlayerCombo.isVisible() && targetPlayer != null;
    myTargetPlayerWarning.setVisible(visible);
    myWarning.setVisible(visible);
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
          linkageType = BCUtils.getDefaultFrameworkLinkage(sdk.getFlexVersion(), myNature);
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
    mySdkPanel = new FlexSdkPanel(myConfigEditor);
  }

  private void initPopupActions() {
    if (myPopupActions == null) {
      int actionIndex = 1;
      final List<AddItemPopupAction> actions = new ArrayList<AddItemPopupAction>();
      actions.add(new AddBuildConfigurationDependencyAction(actionIndex++));
      actions.add(new AddFilesAction(actionIndex++));
      actions.add(new AddGlobalLibraryAction(actionIndex++));
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
      FlexIdeBCConfigurator configurator = FlexIdeBuildConfigurationsExtension.getInstance().getConfigurator();
      for (Module module : ModuleStructureConfigurable.getInstance(myProject).getModules()) {
        if (ModuleType.get(module) != FlexModuleType.getInstance()) {
          continue;
        }
        for (CompositeConfigurable configurable : configurator.getBCConfigurables(module)) {
          FlexIdeBCConfigurable flexIdeBCConfigurable = FlexIdeBCConfigurable.unwrap(configurable);
          if (dependencies.contains(flexIdeBCConfigurable) || flexIdeBCConfigurable.isParentFor(DependenciesConfigurable.this)) {
            continue;
          }

          if (!BCUtils.isApplicableForDependency(myNature, flexIdeBCConfigurable.getOutputType())) {
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
      // TODO we can have problems if we add the same library twice for one module?
      final Collection<Library> usedLibraries = new ArrayList<Library>();
      List<MyTableItem> items = myTable.getItems();
      for (MyTableItem item : items) {
        if (item instanceof ModuleLibraryItem) {
          LibraryOrderEntry orderEntry = ((ModuleLibraryItem)item).orderEntry;
          if (orderEntry != null) {
            Library library = orderEntry.getLibrary();
            if (library != null) {
              usedLibraries.add(orderEntry.getLibrary());
            }
          }
        }
      }
      Condition<Library> filter = new Condition<Library>() {
        @Override
        public boolean value(Library library) {
          return usedLibraries.contains(library);
        }
      };

      LibraryTableBase.ModifiableModelEx modifiableModel = myConfigEditor.getLibraryModel(myDependencies);
      LibraryTable.ModifiableModel librariesModelWrapper = new LibraryTableModifiableModelWrapper(modifiableModel, filter);

      Module module = myConfigEditor.getModule(myDependencies);
      List<? extends FlexLibraryType> libraryTypes = Collections.singletonList(new FlexLibraryType() {
        @Override
        @NotNull
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
          return new FlexLibraryProperties(FlexLibraryIdGenerator.generateId());
        }
      });
      CreateModuleLibraryChooser c = new CreateModuleLibraryChooser(libraryTypes, myMainPanel, module, librariesModelWrapper);
      final List<Library> libraries = c.chooseElements();
      if (libraries.isEmpty()) {
        return;
      }

      DefaultMutableTreeNode rootNode = myTable.getRoot();
      for (Library library : libraries) {
        String libraryId = FlexProjectRootsUtil.getLibraryId(library);
        LibraryOrderEntry libraryEntry = myConfigEditor.findLibraryOrderEntry(myDependencies, library);
        rootNode.add(new DefaultMutableTreeNode(new ModuleLibraryItem(libraryId, libraryEntry, myProject), false));
      }
      updateTableOnItemsAdded(libraries.size());
    }
  }

  private class AddGlobalLibraryAction extends AddItemPopupAction {

    public AddGlobalLibraryAction(int index) {
      super(index, "Shared Library...", null);
    }

    public void run() {
      final Collection<Library> usedLibraries = new HashSet<Library>();
      List<MyTableItem> items = myTable.getItems();
      for (MyTableItem item : items) {
        if (item instanceof SharedLibraryItem) {
          FlexLibraryConfigurable libraryConfigurable = ((SharedLibraryItem)item).configurable;
          if (libraryConfigurable != null) {
            usedLibraries.add(libraryConfigurable.getEditableObject());
          }
        }
      }

      ChooseLibrariesDialog d = new ChooseLibrariesDialog(new Condition<Library>() {
        public boolean value(final Library library) {
          return !usedLibraries.contains(library);
        }
      });
      d.show();
      if (!d.isOK()) return;

      final List<Library> libraries = d.getSelectedLibraries();
      DefaultMutableTreeNode rootNode = myTable.getRoot();
      for (Library library : libraries) {
        SharedLibraryItem item = new SharedLibraryItem(library.getName(), library.getTable().getTableLevel(),
                                                       findConfigurable(library), myProject);
        rootNode.add(new DefaultMutableTreeNode(item, false));
      }
      updateTableOnItemsAdded(libraries.size());
    }
  }

  private void updateTableOnItemsAdded(int count) {
    myTable.refresh();
    myTable.getSelectionModel().clearSelection();
    int rowCount = myTable.getRowCount();
    myTable.getSelectionModel().addSelectionInterval(rowCount - count, rowCount - 1);
  }

  private static class LibraryTableModifiableModelWrapper implements LibraryTableBase.ModifiableModelEx {
    private final LibraryTableBase.ModifiableModelEx myDelegate;
    private final Condition<Library> myLibraryFilter;

    private LibraryTableModifiableModelWrapper(LibraryTableBase.ModifiableModelEx delegate, Condition<Library> libraryFilter) {
      myDelegate = delegate;
      myLibraryFilter = libraryFilter;
    }

    @Override
    public Library createLibrary(String name) {
      return myDelegate.createLibrary(name);
    }

    @Override
    public void removeLibrary(@NotNull Library library) {
      myDelegate.removeLibrary(library);
    }

    @Override
    public void commit() {
      myDelegate.commit();
    }

    @Override
    @NotNull
    public Iterator<Library> getLibraryIterator() {
      return new FilteringIterator<Library, Library>(myDelegate.getLibraryIterator(), myLibraryFilter);
    }

    @Override
    @Nullable
    public Library getLibraryByName(@NotNull String name) {
      Library library = myDelegate.getLibraryByName(name);
      return myLibraryFilter.value(library) ? library : null;
    }

    @Override
    @NotNull
    public Library[] getLibraries() {
      List<Library> filtered = ContainerUtil.filter(myDelegate.getLibraries(), myLibraryFilter);
      return filtered.toArray(new Library[filtered.size()]);
    }

    @Override
    public boolean isChanged() {
      return myDelegate.isChanged();
    }

    @Override
    public Library createLibrary(String name, @Nullable LibraryType type) {
      return myDelegate.createLibrary(name, type);
    }
  }

  private class ChooseLibrariesDialog extends ChooseLibrariesFromTablesDialog {
    private final Condition<Library> myFilter;

    public ChooseLibrariesDialog(Condition<Library> filter) {
      super(myMainPanel, "Choose Libraries", myProject, false);
      myFilter = filter;
      init();
    }

    public void show() {
      if (isEmpty()) {
        Disposer.dispose(getDisposable());
        dispose();
        Messages.showInfoMessage(myProject, "No applicable libraries found", "Add Dependency");
      }
      else {
        super.show();
      }
    }

    @NotNull
    protected Library[] getLibraries(@NotNull final LibraryTable table) {
      final List<Library> filtered = ContainerUtil.filter(super.getLibraries(table), new Condition<Library>() {
        public boolean value(final Library library) {
          if (!FlexProjectRootsUtil.isFlexLibrary(library) || !myFilter.value(library)) return false;
          return true;
        }
      });
      return filtered.toArray(new Library[filtered.size()]);
    }
  }
}
