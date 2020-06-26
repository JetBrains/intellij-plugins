// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.ComponentSet;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexLibraryIdGenerator;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectModelExternalSource;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.*;
import com.intellij.openapi.roots.ui.OrderEntryAppearanceService;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.LibraryTableModifiableModelProvider;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.UIRootConfigurationAccessor;
import com.intellij.openapi.roots.ui.configuration.classpath.CreateModuleLibraryChooser;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.EditExistingLibraryDialog;
import com.intellij.openapi.roots.ui.configuration.projectRoot.*;
import com.intellij.openapi.ui.ComboBoxTableRenderer;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.projectModel.ProjectModelBundle;
import com.intellij.ui.*;
import com.intellij.ui.components.editors.JBComboBoxTableCellEditorComponent;
import com.intellij.ui.navigation.Place;
import com.intellij.util.EventDispatcher;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.FilteringIterator;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;

public class DependenciesConfigurable extends NamedConfigurable<Dependencies> implements Place.Navigator {

  private static final Logger LOG = Logger.getInstance(DependenciesConfigurable.class.getName());

  private static final Icon MISSING_BC_ICON = null;

  public static abstract class Location {
    public final String errorId;

    protected Location(final String id) {
      errorId = id;
    }

    public static final Location SDK = new Location("sdk") {
    };

    public static final class TableEntry extends Location {
      private final String locationString;

      private TableEntry(final String locationString) {
        super(locationString);
        this.locationString = locationString;
      }

      public static TableEntry forSdkRoot(final String url) {
        return new TableEntry("sdkroot\t" + url);
      }

      public static TableEntry forSharedLibrary(final String libraryLevel, final String libraryName) {
        return new TableEntry("sharedlib\t" + libraryLevel + "\t" + libraryName);
      }

      public static TableEntry forSharedLibrary(final Library liveLibrary) {
        return new TableEntry("sharedlib\t" + liveLibrary.getTable().getTableLevel() + "\t" + liveLibrary.getName());
      }

      public static TableEntry forModuleLibrary(final String libraryId) {
        return new TableEntry("modulelib\t" + libraryId);
      }

      public static TableEntry forBc(FlexBCConfigurable configurable) {
        return new TableEntry("bc\t" + configurable.getModuleName() + "\t" + configurable.getDisplayName());
      }

      public static TableEntry forBc(String moduleName, String bcName) {
        return new TableEntry("bc\t" + moduleName + "\t" + bcName);
      }

      public static final TableEntry SDK_ENTRY = new TableEntry("sdk_entry");

      @Override
      public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TableEntry that = (TableEntry)o;

        if (!locationString.equals(that.locationString)) return false;

        return true;
      }

      @Override
      public int hashCode() {
        return locationString.hashCode();
      }
    }
  }

  private JPanel myMainPanel;
  private JdkComboBox mySdkCombo;
  private JLabel myTargetPlayerLabel;
  private JComboBox myTargetPlayerCombo;
  private JLabel myTargetPlayerWarning;
  private JLabel myComponentSetLabel;
  private JComboBox<ComponentSet> myComponentSetCombo;
  private JLabel myFrameworkLinkageLabel;
  private JComboBox<LinkageType> myFrameworkLinkageCombo;
  private JLabel myWarning;
  private JPanel myTablePanel;
  private JButton myEditButton;
  private JLabel mySdkLabel;
  private final EditableTreeTable<MyTableItem> myTable;

  private final Project myProject;
  private final ModifiableDependencies myDependencies;
  private AddItemPopupAction[] myPopupActions;
  private final Disposable myDisposable;
  private final BuildConfigurationNature myNature;

  private final FlexProjectConfigurationEditor myConfigEditor;
  private final ProjectSdksModel mySkdsModel;
  private boolean myFreeze;
  private final EventDispatcher<ChangeListener> mySdkChangeDispatcher;

  private final EventDispatcher<UserActivityListener> myUserActivityDispatcher;

  private boolean myReset;

  private abstract static class MyTableItem {
    @Nullable
    public Icon getIcon() {
      return PlatformIcons.LIBRARY_ICON;
    }

    public boolean showLinkage() {
      return true;
    }

    public abstract boolean isLinkageEditable();

    public boolean isANE() {
      return false;
    }

    public abstract LinkageType getLinkageType();

    public abstract void setLinkageType(LinkageType linkageType);

    public void onDoubleClick() {
    }

    @Nullable
    public ModifiableDependencyEntry apply(ModifiableDependencies dependencies) {
      return null;
    }

    public boolean isModified(DependencyEntry entry) {
      return false;
    }

    public boolean canEdit() {
      return false;
    }

    public abstract Location.TableEntry getLocation();

    public abstract SimpleColoredText getPresentableText();
  }

  private class BCItem extends MyTableItem {
    public final ModifiableDependencyType dependencyType = Factory.createDependencyTypeInstance();
    public final FlexBCConfigurable configurable;
    public final String moduleName;
    public final String bcName;

    BCItem(@NotNull String moduleName, @NotNull String bcName) {
      this.moduleName = moduleName;
      this.bcName = bcName;
      this.configurable = null;
    }

    BCItem(@NotNull FlexBCConfigurable configurable) {
      this.moduleName = null;
      this.bcName = null;
      this.configurable = configurable;
      if (configurable.getOutputType() != OutputType.Library) {
        dependencyType.setLinkageType(LinkageType.LoadInRuntime);
      }
    }

    @Override
    public SimpleColoredText getPresentableText() {
      if (configurable != null) {
        return BCUtils.renderBuildConfiguration(configurable.getEditableObject(), configurable.getModuleName());
      }
      else {
        return BCUtils.renderMissingBuildConfiguration(bcName, moduleName);
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
      return configurable != null && configurable.getOutputType() == OutputType.Library;
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
    public void onDoubleClick() {
      if (configurable != null) {
        Project project = configurable.getModule().getProject();
        ProjectStructureConfigurable.getInstance(project).navigateTo(FlexProjectStructureUtil.createPlace(configurable, null), true);
      }
    }

    @Override
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

    @Override
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
        if (!moduleName.equals(bcEntry.getModuleName())) return true;
        if (!bcName.equals(bcEntry.getBcName())) return true;
      }
      if (!dependencyType.isEqual(entry.getDependencyType())) return true;

      return false;
    }

    @Override
    public boolean canEdit() {
      return false;
    }

    @Override
    public Location.TableEntry getLocation() {
      return configurable != null ? Location.TableEntry.forBc(configurable) : Location.TableEntry.forBc(moduleName, bcName);
    }
  }

  private class ModuleLibraryItem extends MyTableItem {
    public final ModifiableDependencyType dependencyType = Factory.createDependencyTypeInstance();
    public final String libraryId;
    @Nullable
    public final LibraryOrderEntry orderEntry;

    private final Project project;

    ModuleLibraryItem(@NotNull String libraryId, @Nullable LibraryOrderEntry orderEntry, @NotNull Project project) {
      this.libraryId = libraryId;
      this.orderEntry = orderEntry;
      this.project = project;
    }

    @Override
    public SimpleColoredText getPresentableText() {
      if (orderEntry != null) {
        Library library = orderEntry.getLibrary();
        if (library != null) {
          if (((LibraryEx)library).isDisposed()) {
            Pair<String, String> moduleAndBcName = getModuleAndBcName();
            LOG.error("Module library '" +
                      library.getName() +
                      "' is disposed, used in BC: " +
                      moduleAndBcName.second +
                      " of module " +
                      moduleAndBcName.first);
            return new SimpleColoredText("<unknown>", SimpleTextAttributes.ERROR_ATTRIBUTES);
          }
          boolean hasInvalidRoots = !((LibraryEx)library).getInvalidRootUrls(OrderRootType.CLASSES).isEmpty();
          String text = OrderEntryAppearanceService.getInstance().forLibrary(project, library, hasInvalidRoots).getText();
          return new SimpleColoredText(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
      }
      return new SimpleColoredText("<unknown>", SimpleTextAttributes.ERROR_ATTRIBUTES);
    }

    @Override
    public boolean isLinkageEditable() {
      return !isANE();
    }

    @Override
    public boolean isANE() {
      final Library library = orderEntry == null ? null : orderEntry.getLibrary();
      final VirtualFile[] files = library == null ? VirtualFile.EMPTY_ARRAY : library.getFiles(OrderRootType.CLASSES);
      for (VirtualFile file : files) {
        if ("ane".equalsIgnoreCase(file.getExtension())) return true;
      }
      return false;
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
    public void onDoubleClick() {
      if (canEdit()) {
        editLibrary(this);
      }
    }

    @Override
    public ModifiableDependencyEntry apply(final ModifiableDependencies dependencies) {
      ModifiableDependencyEntry entry = myConfigEditor.createModuleLibraryEntry(dependencies, libraryId);
      entry.getDependencyType().copyFrom(dependencyType);
      return entry;
    }

    @Override
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

    @Override
    public boolean canEdit() {
      return orderEntry != null;
    }

    @Override
    public Location.TableEntry getLocation() {
      return Location.TableEntry.forModuleLibrary(libraryId);
    }
  }

  private class SharedLibraryItem extends MyTableItem {
    public final ModifiableDependencyType dependencyType = Factory.createDependencyTypeInstance();
    public final String libraryName;
    public final String libraryLevel;
    @Nullable
    public final Library liveLibrary;

    private final Project project;

    SharedLibraryItem(@NotNull String libraryName,
                      @NotNull String libraryLevel,
                      @Nullable Library liveLibrary,
                      @NotNull Project project) {
      this.libraryName = libraryName;
      this.libraryLevel = libraryLevel;
      this.liveLibrary = liveLibrary;
      this.project = project;
    }

    @Override
    public SimpleColoredText getPresentableText() {
      Library liveLibrary = findLiveLibrary();
      if (liveLibrary != null) {
        String text = OrderEntryAppearanceService.getInstance().forLibrary(project, liveLibrary, false).getText();
        return new SimpleColoredText(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
      }
      else {
        return new SimpleColoredText(libraryName, SimpleTextAttributes.ERROR_ATTRIBUTES);
      }
    }

    @Nullable
    public Library findLiveLibrary() {
      // TODO call myConfigEditor.findLiveLibrary(library, libraryName, libraryLevel);
      return new UIRootConfigurationAccessor(project).getLibrary(liveLibrary, libraryName, libraryLevel);
    }

    @Override
    public boolean isLinkageEditable() {
      return !isANE();
    }

    @Override
    public boolean isANE() {
      final VirtualFile[] files = liveLibrary == null ? VirtualFile.EMPTY_ARRAY : liveLibrary.getFiles(OrderRootType.CLASSES);
      for (VirtualFile file : files) {
        if ("ane".equalsIgnoreCase(file.getExtension())) return true;
      }
      return false;
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
    public void onDoubleClick() {
      editLibrary(this);
    }

    @Override
    public ModifiableDependencyEntry apply(final ModifiableDependencies dependencies) {
      ModifiableDependencyEntry entry;
      Library liveLibrary = findLiveLibrary();
      if (liveLibrary != null) {
        entry = myConfigEditor.createSharedLibraryEntry(dependencies, liveLibrary.getName(), liveLibrary.getTable().getTableLevel());
      }
      else {
        entry = myConfigEditor.createSharedLibraryEntry(dependencies, libraryName, libraryLevel);
      }
      entry.getDependencyType().copyFrom(dependencyType);
      return entry;
    }

    @Override
    public boolean isModified(final DependencyEntry entry) {
      if (!(entry instanceof ModifiableSharedLibraryEntry)) {
        return true;
      }
      ModifiableSharedLibraryEntry libraryEntry = (ModifiableSharedLibraryEntry)entry;
      Library liveLibrary = findLiveLibrary();
      if (liveLibrary != null) {
        if (!libraryEntry.getLibraryName().equals(liveLibrary.getName())) return true;
        if (!liveLibrary.getTable().getTableLevel().equals(libraryEntry.getLibraryLevel())) return true;
      }
      else {
        if (!libraryName.equals(libraryEntry.getLibraryName())) return true;
        if (!libraryLevel.equals(libraryEntry.getLibraryLevel())) return true;
      }

      if (!dependencyType.isEqual(entry.getDependencyType())) return true;
      return false;
    }

    @Override
    public boolean canEdit() {
      return true;
    }

    @Override
    public Location.TableEntry getLocation() {
      Library liveLibrary = findLiveLibrary();
      return liveLibrary != null
             ? Location.TableEntry.forSharedLibrary(liveLibrary)
             : Location.TableEntry.forSharedLibrary(libraryLevel, libraryName);
    }
  }

  private static class SdkItem extends MyTableItem {
    private final Sdk mySdk;
    private final SdkType mySdkType;

    SdkItem(Sdk sdk) {
      mySdk = sdk;
      mySdkType = (SdkType)sdk.getSdkType();
    }

    @Override
    public SimpleColoredText getPresentableText() {
      SimpleColoredText text = new SimpleColoredText();
      final String sdkVersion = StringUtil.notNullize(mySdk.getVersionString(), FlexBundle.message("flex.sdk.version.unknown"));
      if (sdkVersion.startsWith(FlexCommonUtils.AIR_SDK_VERSION_PREFIX)) {
        text.append(sdkVersion, SimpleTextAttributes.REGULAR_ATTRIBUTES);
      }
      else {
        text.append("Flex SDK " + sdkVersion, SimpleTextAttributes.REGULAR_ATTRIBUTES);

        final String airSdkVersion = FlexCommonUtils.getVersionOfAirSdkIncludedInFlexSdk(mySdk.getHomePath());
        if (airSdkVersion != null) {
          text.append(", AIR SDK " + airSdkVersion, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
      }
      return text;
    }

    @Override
    public Icon getIcon() {
      return mySdkType.getIcon();
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
    public Location.TableEntry getLocation() {
      return Location.TableEntry.SDK_ENTRY;
    }
  }

  private static final class SdkEntryItem extends MyTableItem {
    private final String url;
    private final LinkageType linkageType;

    private SdkEntryItem(String url, LinkageType linkageType) {
      this.url = url;
      this.linkageType = linkageType;
    }

    @Override
    public SimpleColoredText getPresentableText() {
      return new SimpleColoredText(url, SimpleTextAttributes.REGULAR_ATTRIBUTES);
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
    public Location.TableEntry getLocation() {
      return Location.TableEntry.forSdkRoot(url);
    }
  }

  private static final TableCellRenderer LINKAGE_TYPE_RENDERER = new DefaultTableCellRenderer() {
    private final ComboBoxTableRenderer<LinkageType> myComboBoxTableRenderer = new ComboBoxTableRenderer<>(null);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      final Object tableItem = ((EditableTreeTable)table).getItemAt(row);
      if (tableItem instanceof MyTableItem && ((MyTableItem)tableItem).isLinkageEditable()) {
        myComboBoxTableRenderer.setFont(table.getFont());
        return myComboBoxTableRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
      else {
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
    }
  };

  private static final DefaultTableCellRenderer ANE_RENDERER = new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      final JLabel component = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      component.setText("ANE");
      return component;
    }
  };

  private static final DefaultTableCellRenderer EMPTY_RENDERER = new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      final JLabel component = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      component.setText("");
      return component;
    }
  };

  private static final AbstractTableCellEditor LINKAGE_TYPE_EDITOR = new AbstractTableCellEditor() {
    private JBComboBoxTableCellEditorComponent myCombo;

    @Override
    public Object getCellEditorValue() {
      return myCombo.getEditorValue();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      myCombo = new JBComboBoxTableCellEditorComponent(table);
      myCombo.setCell(table, row, column);
      myCombo.setOptions((Object[])LinkageType.getSwcLinkageValues());
      myCombo.setDefaultValue(value);
      myCombo.setToString(o -> ((LinkageType)o).getShortText());
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
      return item.showLinkage() ? item.isANE() ? ANE_RENDERER
                                               : LINKAGE_TYPE_RENDERER
                                : EMPTY_RENDERER;
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

  public DependenciesConfigurable(final ModifiableFlexBuildConfiguration bc,
                                  Project project,
                                  @NotNull FlexProjectConfigurationEditor configEditor,
                                  final ProjectSdksModel sdksModel) {
    mySkdsModel = sdksModel;
    myConfigEditor = configEditor;
    myDependencies = bc.getDependencies();
    myProject = project;
    myNature = bc.getNature();

    mySdkChangeDispatcher = EventDispatcher.create(ChangeListener.class);
    myDisposable = Disposer.newDisposable();

    final SdkModel.Listener listener = new SdkModel.Listener() {
      @Override
      public void sdkAdded(@NotNull final Sdk sdk) {
        rebuildSdksModel();
      }

      @Override
      public void beforeSdkRemove(@NotNull final Sdk sdk) {
        rebuildSdksModel();
      }

      @Override
      public void sdkChanged(@NotNull final Sdk sdk, final String previousName) {
        rebuildSdksModel();
      }

      @Override
      public void sdkHomeSelected(@NotNull final Sdk sdk, @NotNull final String newSdkHome) {
        rebuildSdksModel();
      }
    };
    sdksModel.addListener(listener);
    Disposer.register(myDisposable, () -> sdksModel.removeListener(listener));

    mySdkCombo.showNoneSdkItem();
    mySdkCombo.setEditButton(myEditButton, myProject, () -> mySdkCombo.getSelectedJdk());

    mySdkLabel.setLabelFor(mySdkCombo);

    mySdkCombo.addActionListener(e -> {
      if (myFreeze) {
        return;
      }
      updateOnSelectedSdkChange();
    });

    myComponentSetCombo.setModel(new DefaultComboBoxModel<>(ComponentSet.values()));
    myComponentSetCombo.setRenderer(SimpleListCellRenderer.create("", ComponentSet::getPresentableText));

    myFrameworkLinkageCombo.setRenderer(SimpleListCellRenderer.create("", value -> {
      if (value == LinkageType.Default) {
        Sdk sdk = mySdkCombo.getSelectedJdk();
        String sdkVersion = sdk != null ? sdk.getVersionString() : null;
        return sdkVersion == null ? "Default" : MessageFormat.format(
          "Default ({0})", FlexCommonUtils.getDefaultFrameworkLinkage(sdkVersion, myNature).getLongText());
      }
      else {
        return value.getLongText();
      }
    }));

    myFrameworkLinkageCombo.setModel(new DefaultComboBoxModel<>(BCUtils.getSuitableFrameworkLinkages(myNature)));

    ItemListener updateSdkItemsListener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (myFreeze) {
          return;
        }
        DefaultMutableTreeNode sdkNode = findSdkNode();
        Sdk currentSdk = mySdkCombo.getSelectedJdk();
        if (sdkNode != null && currentSdk != null) {
          updateSdkEntries(sdkNode, currentSdk);
          myTable.refresh();
        }
      }
    };

    myTargetPlayerCombo.addItemListener(updateSdkItemsListener);
    myComponentSetCombo.addItemListener(updateSdkItemsListener);
    myFrameworkLinkageCombo.addItemListener(updateSdkItemsListener);

    myTargetPlayerWarning.setIcon(AllIcons.General.BalloonWarning12);
    myWarning.setIcon(UIUtil.getBalloonWarningIcon());

    myTable = new EditableTreeTable<MyTableItem>("", DEPENDENCY_TYPE_COLUMN) {
      @Override
      protected void render(SimpleColoredComponent c, MyTableItem item) {
        if (item != null) {
          item.getPresentableText().appendToComponent(c);
          c.setIcon(item.getIcon());
        }
      }
    };
    myTable.setRootVisible(false);
    myTable.getTree().setShowsRootHandles(true);

    myTablePanel.add(
      ToolbarDecorator.createDecorator(myTable)
        .setAddAction(this::addItem).setAddActionName(FlexBundle.message("add.dependency.action.name"))
        .setRemoveAction(anActionButton -> removeSelection()).setEditAction(button -> {
          MyTableItem item = myTable.getItemAt(myTable.getSelectedRow());
          if (item instanceof SharedLibraryItem) {
            editLibrary((SharedLibraryItem)item);
          }
          if (item instanceof ModuleLibraryItem) {
            editLibrary(((ModuleLibraryItem)item));
          }
        }).setRemoveActionUpdater(e -> {
          if (myTable.getSelectedRowCount() == 0) return false;
          for (int row : myTable.getSelectedRows()) {
            MyTableItem item = myTable.getItemAt(row);
            if (item instanceof SdkItem || item instanceof SdkEntryItem) return false;
          }
          return true;
        }).setEditActionUpdater(e -> {
          MyTableItem item = myTable.getItemAt(myTable.getSelectedRow());
          return item != null && item.canEdit();
        }).disableUpDownActions().createPanel(), BorderLayout.CENTER);

    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(@NotNull MouseEvent e) {
        if (myTable.getSelectedRowCount() == 1) {
          myTable.getItemAt(myTable.getSelectedRow()).onDoubleClick();
          return true;
        }
        return false;
      }
    }.installOn(myTable);

    FlexBuildConfigurationsExtension.getInstance().getConfigurator().addListener(new FlexBCConfigurator.Listener() {
      @Override
      public void moduleRemoved(Module module) {
        // TODO return if module == this module
        Set<MyTableItem> itemsToRemove = new HashSet<>();
        // 1st-level nodes are always visible
        // 2nd-level nodes cannot refer to BC
        for (int row = 0; row < myTable.getRowCount(); row++) {
          MyTableItem item = myTable.getItemAt(row);
          if (item instanceof BCItem) {
            FlexBCConfigurable configurable = ((BCItem)item).configurable;
            if (configurable != null && configurable.getModule() == module) {
              itemsToRemove.add(item);
            }
          }
        }

        removeItems(itemsToRemove, true);
      }

      @Override
      public void buildConfigurationRemoved(FlexBCConfigurable configurable) {
        Pair<BCItem, Integer> item = findDependencyItem(configurable);
        if (item != null) {
          removeItems(Collections.singleton(item.first), true);
        }
      }

      @Override
      public void buildConfigurationRenamed(final FlexBCConfigurable configurable) {
        Pair<BCItem, Integer> item = findDependencyItem(configurable);
        if (item != null) {
          myTable.refreshItemAt(item.second);
        }
      }

      @Override
      public void natureChanged(final FlexBCConfigurable configurable) {
        Pair<BCItem, Integer> item = findDependencyItem(configurable);
        if (item != null) {
          final BuildConfigurationNature dependencyNature = item.first.configurable.getEditableObject().getNature();
          if (!FlexCommonUtils.checkDependencyType(myNature.outputType, dependencyNature.outputType, item.first.getLinkageType())) {
            removeItems(Collections.singleton(item.first), true);
          }
        }
      }

      @Nullable
      private Pair<BCItem, Integer> findDependencyItem(FlexBCConfigurable configurable) {
        if (configurable.isParentFor(DependenciesConfigurable.this)) {
          return null;
        }

        // 1st-level nodes are always visible
        // 2nd-level nodes cannot refer to BC
        for (int row = 0; row < myTable.getRowCount(); row++) {
          final MyTableItem item = myTable.getItemAt(row);

          if (item instanceof BCItem && ((BCItem)item).configurable == configurable) {
            // there may be only one dependency on a BC
            return Pair.create((BCItem)item, row);
          }
        }
        return null;
      }
    }, myDisposable);

    myConfigEditor.addModulesModelChangeListener(modules -> {
      FlexBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
      for (Module module : modules) {
        for (CompositeConfigurable configurable : configurator.getBCConfigurables(module)) {
          FlexBCConfigurable flexBCConfigurable = FlexBCConfigurable.unwrap(configurable);
          if (flexBCConfigurable.isParentFor(this)) {
            resetTable(myDependencies.getSdkEntry(), true);
          }
        }
      }
    }, myDisposable);

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.register(myMainPanel);
    myUserActivityDispatcher = EventDispatcher.create(UserActivityListener.class);
    watcher.addUserActivityListener(() -> {
      if (myFreeze) {
        return;
      }
      myUserActivityDispatcher.getMulticaster().stateChanged();
    }, myDisposable);
  }

  private void rebuildSdksModel() {
    final JdkComboBox.JdkComboBoxItem selectedItem = mySdkCombo.getSelectedItem();
    JdkComboBox.NoneJdkComboBoxItem noneSdkItem = new JdkComboBox.NoneJdkComboBoxItem();
    myFreeze = true;
    try {
      mySdkCombo.reloadModel();
    }
    finally {
      myFreeze = false;
    }

    if (selectedItem instanceof JdkComboBox.NoneJdkComboBoxItem) {
      mySdkCombo.setSelectedItem(noneSdkItem);
    }
    else {
      String selectedSdkName = selectedItem != null ? selectedItem.getSdkName() : null;
      if (selectedSdkName != null) {
        Sdk sdk = mySkdsModel.findSdk(selectedSdkName);
        if (sdk != null) {
          mySdkCombo.setSelectedJdk(sdk);
        }
        else {
          mySdkCombo.setInvalidJdk(selectedSdkName);
        }
      }
      else {
        mySdkCombo.setSelectedItem(noneSdkItem);
      }
    }

    if (selectedItem != null && mySdkCombo.getSelectedJdk() != selectedItem.getJdk()) {
      updateOnSelectedSdkChange();
    }
    mySdkChangeDispatcher.getMulticaster().stateChanged(new ChangeEvent(this));
  }

  @Nullable
  Sdk getCurrentSdk() {
    return mySdkCombo.getSelectedJdk();
  }

  ComponentSet getCurrentComponentSet() {
    return (ComponentSet)myComponentSetCombo.getSelectedItem();
  }

  void addSdkChangeListener(final ChangeListener changeListener) {
    mySdkChangeDispatcher.addListener(changeListener);
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

  private void editLibrary(final SharedLibraryItem item) {
    final Library liveLibrary = item.findLiveLibrary();
    if (liveLibrary != null) {
      final BaseLibrariesConfigurable librariesConfigurable =
        LibraryTablesRegistrar.APPLICATION_LEVEL.equals(liveLibrary.getTable().getTableLevel())
        ? GlobalLibrariesConfigurable.getInstance(myProject)
        : ProjectLibrariesConfigurable.getInstance(myProject);
      final Place place = new Place()
        .putPath(ProjectStructureConfigurable.CATEGORY, librariesConfigurable)
        .putPath(MasterDetailsComponent.TREE_OBJECT, liveLibrary);
      ProjectStructureConfigurable.getInstance(myProject).navigateTo(place, true);
    }
  }

  private void editLibrary(ModuleLibraryItem item) {
    if (!item.canEdit()) return;

    final LibraryOrderEntry entry = item.orderEntry;
    assert entry != null;

    Library library = entry.getLibrary();
    if (library == null) {
      return;
    }

    LibraryTablePresentation presentation = new LibraryTablePresentation() {
      @NotNull
      @Override
      public String getDisplayName(boolean plural) {
        return FlexBundle.message(plural ? "library.editor.title.plural" : "library.editor.title.singular");
      }

      @NotNull
      @Override
      public String getDescription() {
        return ProjectModelBundle.message("libraries.node.text.module");
      }

      @NotNull
      @Override
      public String getLibraryTableEditorTitle() {
        return "Configure Library"; // not used as far as I see
      }
    };

    LibraryTableModifiableModelProvider provider = () -> myConfigEditor.getLibraryModel(myDependencies);

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
      new BaseListPopupStep<AddItemPopupAction>(FlexBundle.message("add.dependency.popup.title"), myPopupActions) {
        @Override
        public Icon getIconFor(AddItemPopupAction aValue) {
          return aValue.getIcon();
        }

        @Override
        public boolean hasSubstep(AddItemPopupAction selectedValue) {
          return selectedValue.hasSubStep();
        }

        @Override
        public boolean isMnemonicsNavigationEnabled() {
          return true;
        }

        @Override
        public PopupStep onChosen(final AddItemPopupAction selectedValue, final boolean finalChoice) {
          if (selectedValue.hasSubStep()) {
            return selectedValue.createSubStep();
          }
          return doFinalStep(selectedValue);
        }

        @Override
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
    Set<MyTableItem> itemsToRemove = new HashSet<>(selectedRows.length);
    for (int row : selectedRows) {
      itemsToRemove.add(myTable.getItemAt(row));
    }
    removeItems(itemsToRemove, true);
    if (myTable.getRowCount() > 0) {
      int toSelect = Math.min(myTable.getRowCount() - 1, selectedRows[0]);
      myTable.clearSelection();
      myTable.getSelectionModel().addSelectionInterval(toSelect, toSelect);
    }
  }

  private void removeItems(Set<? extends MyTableItem> itemsToDelete, boolean refresh) {
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

    if (refresh) {
      myTable.refresh();
    }
  }

  @Override
  @Nls
  public String getDisplayName() {
    return getTabName();
  }

  @Override
  public void setDisplayName(final String name) {
  }

  @Override
  public String getBannerSlogan() {
    return getDisplayName();
  }

  @Override
  public Dependencies getEditableObject() {
    return myDependencies;
  }

  @Override
  public String getHelpTopic() {
    return "BuildConfigurationPage.Dependencies";
  }

  @Override
  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  @Override
  public boolean isModified() {
    final JdkComboBox.JdkComboBoxItem selectedItem = mySdkCombo.getSelectedItem();
    String currentSdkName = selectedItem == null ? null : selectedItem.getSdkName();
    SdkEntry sdkEntry = myDependencies.getSdkEntry();
    if (currentSdkName != null) {
      if (sdkEntry == null) {
        return mySdkCombo.getSelectedJdk() != null;
      }
      else if (!currentSdkName.equals(sdkEntry.getName())) {
        return true;
      }
    }
    else {
      if (sdkEntry != null) return true;
    }

    final String targetPlayer = (String)myTargetPlayerCombo.getSelectedItem();
    if (myTargetPlayerCombo.isVisible() && targetPlayer != null && !myDependencies.getTargetPlayer().equals(targetPlayer)) return true;
    if (myComponentSetCombo.isVisible() && myDependencies.getComponentSet() != myComponentSetCombo.getSelectedItem()) return true;
    if (myFrameworkLinkageCombo.isVisible() && myDependencies.getFrameworkLinkage() != myFrameworkLinkageCombo.getSelectedItem()) {
      return true;
    }

    List<MyTableItem> items = myTable.getItems();
    items = ContainerUtil.filter(items, item -> !(item instanceof SdkItem || item instanceof SdkEntryItem));

    DependencyEntry[] entries = myDependencies.getEntries();
    if (items.size() != entries.length) return true;
    for (int i = 0; i < items.size(); i++) {
      MyTableItem item = items.get(i);
      DependencyEntry entry = entries[i];
      if (item.isModified(entry)) return true;
    }
    return false;
  }

  @Override
  public void apply() {
    final Object targetPlayer = myTargetPlayerCombo.getSelectedItem();
    if (myTargetPlayerCombo.isVisible() && targetPlayer != null) {
      myDependencies.setTargetPlayer((String)targetPlayer);
    }
    if (myComponentSetCombo.isVisible()) {
      myDependencies.setComponentSet((ComponentSet)myComponentSetCombo.getSelectedItem());
    }
    if (myFrameworkLinkageCombo.isVisible()) {
      myDependencies.setFrameworkLinkage((LinkageType)myFrameworkLinkageCombo.getSelectedItem());
    }

    List<MyTableItem> items = myTable.getItems();
    List<ModifiableDependencyEntry> newEntries = new ArrayList<>();
    for (MyTableItem item : items) {
      ModifiableDependencyEntry entry = item.apply(myDependencies);
      if (entry != null) {
        newEntries.add(entry);
      }
    }
    myConfigEditor.setEntries(myDependencies, newEntries);

    JdkComboBox.JdkComboBoxItem currentSdk = mySdkCombo.getSelectedItem();
    if (currentSdk != null) {
      final String sdkName = currentSdk.getSdkName();
      if (sdkName != null) {
        SdkEntry sdkEntry = Factory.createSdkEntry(sdkName);
        myDependencies.setSdkEntry(sdkEntry);
      }
      else {
        myDependencies.setSdkEntry(null);
      }
    }
    else {
      myDependencies.setSdkEntry(null);
    }
  }

  @Override
  public void reset() {
    myReset = true;
    SdkEntry sdkEntry = myDependencies.getSdkEntry();
    myFreeze = true;
    try {
      mySdkCombo.reloadModel();

      if (sdkEntry != null) {
        final Sdk sdk = mySkdsModel.findSdk(sdkEntry.getName());
        if (sdk != null && (sdk.getSdkType() == FlexSdkType2.getInstance() || sdk.getSdkType() == FlexmojosSdkType.getInstance())) {
          // technically, non-flex item won't appear in SDK combo model anyway
          mySdkCombo.setSelectedJdk(sdk);
        }
        else {
          mySdkCombo.setInvalidJdk(sdkEntry.getName());
        }
      }
      else {
        mySdkCombo.setSelectedJdk(null);
      }

      BCUtils.updateAvailableTargetPlayers(mySdkCombo.getSelectedJdk(), myTargetPlayerCombo);
      myTargetPlayerCombo.setSelectedItem(myDependencies.getTargetPlayer());
      overriddenTargetPlayerChanged(null); // no warning initially

      updateComponentSetCombo();
      myComponentSetCombo.setSelectedItem(myDependencies.getComponentSet());

      myFrameworkLinkageCombo.setSelectedItem(myDependencies.getFrameworkLinkage());

      resetTable(sdkEntry, false);
      updateControls();
    }
    finally {
      myFreeze = false;
    }
  }

  private void updateControls() {
    final Sdk sdk = mySdkCombo.getSelectedJdk();
    final boolean flexmojos = sdk != null && sdk.getSdkType() instanceof FlexmojosSdkType;

    myTargetPlayerLabel.setVisible(myNature.isWebPlatform() && !flexmojos);
    myTargetPlayerCombo.setVisible(myNature.isWebPlatform() && !flexmojos);

    if (!myTargetPlayerCombo.isVisible()) {
      myTargetPlayerWarning.setVisible(false);
      myWarning.setVisible(false);
    }

    final boolean airSdk = FlexSdkUtils.isAirSdkWithoutFlex(sdk);
    final boolean visible = sdk != null && !flexmojos && !myNature.isMobilePlatform() && !myNature.pureAS && !airSdk &&
                            StringUtil.compareVersionNumbers(sdk.getVersionString(), "4") >= 0;
    myComponentSetLabel.setVisible(visible);
    myComponentSetCombo.setVisible(visible);

    myFrameworkLinkageLabel.setVisible(!myNature.pureAS && !flexmojos && !airSdk);
    myFrameworkLinkageCombo.setVisible(!myNature.pureAS && !flexmojos && !airSdk);
  }

  private void resetTable(SdkEntry sdkEntry, boolean keepSelection) {
    int[] selectedRows = keepSelection ? myTable.getSelectedRows() : new int[0];

    DefaultMutableTreeNode root = myTable.getRoot();
    root.removeAllChildren();

    if (sdkEntry != null) {
      Sdk flexSdk = FlexSdkUtils.findFlexOrFlexmojosSdk(sdkEntry.getName());
      if (flexSdk != null) {
        DefaultMutableTreeNode sdkNode = new DefaultMutableTreeNode(new SdkItem(flexSdk), true);
        myTable.getRoot().insert(sdkNode, 0);
        updateSdkEntries(sdkNode, flexSdk);
      }
    }
    FlexBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
    for (DependencyEntry entry : myDependencies.getEntries()) {
      MyTableItem item = null;
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;
        Module module = bcEntry.findModule();
        CompositeConfigurable configurable =
          module != null ? ContainerUtil.find(configurator.getBCConfigurables(module),
                                              configurable1 -> configurable1.getDisplayName().equals(bcEntry.getBcName())) : null;
        if (configurable == null) {
          item = new BCItem(bcEntry.getModuleName(), bcEntry.getBcName());
        }
        else {
          item = new BCItem(FlexBCConfigurable.unwrap(configurable));
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
        SharedLibraryEntry sharedLibraryEntry = (SharedLibraryEntry)entry;
        LibrariesModifiableModel model = ProjectStructureConfigurable.getInstance(myProject).getContext()
          .createModifiableModelProvider(sharedLibraryEntry.getLibraryLevel()).getModifiableModel();
        LibraryEx library = (LibraryEx)model.getLibraryByName(sharedLibraryEntry.getLibraryName());
        item = new SharedLibraryItem(sharedLibraryEntry.getLibraryName(), sharedLibraryEntry.getLibraryLevel(), library, myProject);
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
  }

  /**
   * Called when {@link CompilerOptionsConfigurable} is initialized and when path to additional config file is changed
   *
   * @param targetPlayer {@code null} means that the value is not overridden in additional config file
   */
  public void overriddenTargetPlayerChanged(final @Nullable String targetPlayer) {
    myTargetPlayerWarning.setToolTipText(FlexBundle.message("actual.value.from.config.file.0", targetPlayer));
    myWarning.setText(FlexBundle.message("overridden.in.config.file", "Target player", targetPlayer));

    final boolean visible = myTargetPlayerCombo.isVisible() && targetPlayer != null;
    myTargetPlayerWarning.setVisible(visible);
    myWarning.setVisible(visible);
  }

  private void updateComponentSetCombo() {
    updateControls();
    final Sdk sdk = mySdkCombo.getSelectedJdk();
    if (sdk != null && myComponentSetCombo.isVisible()) {
      final Object selectedItem = myComponentSetCombo.getSelectedItem();
      final ComponentSet[] values = StringUtil.compareVersionNumbers(sdk.getVersionString(), "4.5") >= 0
                                    ? ComponentSet.values()
                                    : new ComponentSet[]{ComponentSet.SparkAndMx, ComponentSet.MxOnly};
      myComponentSetCombo.setModel(new DefaultComboBoxModel(values));
      myComponentSetCombo.setSelectedItem(selectedItem);
    }
  }

  private void updateSdkTableItem(@Nullable Sdk sdk) {
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

  private void updateSdkEntries(DefaultMutableTreeNode sdkNode, Sdk sdk) {
    sdkNode.removeAllChildren();
    ComponentSet componentSet = (ComponentSet)myComponentSetCombo.getSelectedItem();
    String targetPlayer = (String)myTargetPlayerCombo.getSelectedItem();

    for (String url : sdk.getRootProvider().getUrls(OrderRootType.CLASSES)) {
      final String swcPath = VirtualFileManager.extractPath(StringUtil.trimEnd(url, JarFileSystem.JAR_SEPARATOR));
      LinkageType linkageType = FlexCommonUtils.getSdkEntryLinkageType(sdk.getHomePath(), swcPath, myNature, targetPlayer, componentSet);
      if (linkageType == null) {
        // this swc is not applicable
        continue;
      }

      if (linkageType == LinkageType.Default) {
        linkageType = (LinkageType)myFrameworkLinkageCombo.getSelectedItem();
        if (linkageType == LinkageType.Default) {
          linkageType = FlexCommonUtils.getDefaultFrameworkLinkage(sdk.getVersionString(), myNature);
        }
      }

      SdkEntryItem item = new SdkEntryItem(FileUtil.toSystemDependentName(swcPath), linkageType);
      sdkNode.add(new DefaultMutableTreeNode(item, false));
    }
  }

  @Override
  public void disposeUIResources() {
    Disposer.dispose(myDisposable);
  }

  private void createUIComponents() {
    final Condition<SdkTypeId> sdkTypeFilter = Conditions.oneOf(FlexSdkType2.getInstance(), FlexmojosSdkType.getInstance());
    Condition<Sdk> sdkCondition =
      JdkComboBox.getSdkFilter(sdkTypeFilter);

    mySdkCombo = new JdkComboBox(myProject, mySkdsModel, sdkTypeFilter, sdkCondition, Conditions.is(FlexSdkType2.getInstance()), null);
  }

  private void initPopupActions() {
    if (myPopupActions == null) {
      int actionIndex = 1;
      final List<AddItemPopupAction> actions = new ArrayList<>();
      actions.add(new AddBuildConfigurationDependencyAction(actionIndex++));
      actions.add(new AddFilesAction(actionIndex++));
      actions.add(new AddSharedLibraryAction(actionIndex++));
      myPopupActions = actions.toArray(new AddItemPopupAction[0]);
    }
  }

  private class AddBuildConfigurationDependencyAction extends AddItemPopupAction {
    AddBuildConfigurationDependencyAction(int index) {
      super(index, "Build Configuration...", null);
    }

    @Override
    public void run() {
      final Collection<FlexBCConfigurable> dependencies = new ArrayList<>();
      List<MyTableItem> items = myTable.getItems();
      for (MyTableItem item : items) {
        if (item instanceof BCItem) {
          FlexBCConfigurable configurable = ((BCItem)item).configurable;
          if (configurable != null) {
            dependencies.add(configurable);
          }
        }
      }

      ChooseBuildConfigurationDialog d = ChooseBuildConfigurationDialog.createForApplicableBCs(
        FlexBundle.message("add.bc.dependency.dialog.title"), FlexBundle.message("add.dependency.bc.dialog.label"), myProject, false,
        configurable -> {
          if (dependencies.contains(configurable) || configurable.isParentFor(DependenciesConfigurable.this)) {
            return false;
          }

          if (!BCUtils.isApplicableForDependency(myNature, configurable.getOutputType())) {
            return false;
          }
          return true;
        });

      if (d == null) {
        Messages.showInfoMessage(myProject, FlexBundle.message("no.applicable.bcs"), FlexBundle.message("add.bc.dependency.dialog.title"));
        return;
      }

      if (!d.showAndGet()) {
        return;
      }

      FlexBCConfigurable[] configurables = d.getSelectedConfigurables();
      DefaultMutableTreeNode root = myTable.getRoot();
      for (FlexBCConfigurable configurable : configurables) {
        root.add(new DefaultMutableTreeNode(new BCItem(configurable), false));
      }
      myTable.refresh();
      myTable.getSelectionModel().clearSelection();
      int rowCount = myTable.getRowCount();
      myTable.getSelectionModel().addSelectionInterval(rowCount - configurables.length, rowCount - 1);
    }
  }

  private class AddFilesAction extends AddItemPopupAction {
    AddFilesAction(int index) {
      super(index, FlexBundle.message("add.module.library.action.text"), null);
    }

    @Override
    public void run() {
      // TODO we can have problems if we add the same library twice for one module?
      final Collection<Library> usedLibraries = new ArrayList<>();
      List<MyTableItem> items = myTable.getItems();
      for (MyTableItem item : items) {
        if (item instanceof ModuleLibraryItem) {
          LibraryOrderEntry orderEntry = ((ModuleLibraryItem)item).orderEntry;
          if (orderEntry != null) {
            ContainerUtil.addIfNotNull(usedLibraries, orderEntry.getLibrary());
          }
        }
      }
      Condition<Library> filter = usedLibraries::contains;

      LibraryTable.ModifiableModel modifiableModel = myConfigEditor.getLibraryModel(myDependencies);
      LibraryTable.ModifiableModel librariesModelWrapper = new LibraryTableModifiableModelWrapper(modifiableModel, filter);

      Module module = myConfigEditor.getModule(myDependencies);
      List<? extends FlexLibraryType> libraryTypes = Collections.singletonList(FlexLibraryType.getInstance());
      CreateModuleLibraryChooser c = new CreateModuleLibraryChooser(libraryTypes, myMainPanel, module, librariesModelWrapper,
                                                                    type -> new FlexLibraryProperties(
                                                                      FlexLibraryIdGenerator.generateId()));
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

  private class AddSharedLibraryAction extends AddItemPopupAction {

    AddSharedLibraryAction(int index) {
      super(index, FlexBundle.message("add.shared.library.dependency.action.text"), null);
    }

    @Override
    public void run() {
      final Collection<Library> usedLibraries = new HashSet<>();
      List<MyTableItem> items = myTable.getItems();
      for (MyTableItem item : items) {
        if (item instanceof SharedLibraryItem) {
          LibraryEx library = (LibraryEx)((SharedLibraryItem)item).findLiveLibrary();
          if (library != null) {
            usedLibraries.add(library);
          }
        }
      }

      ChooseLibrariesDialog d = new ChooseLibrariesDialog(library -> !usedLibraries.contains(library));
      if (!d.showAndGet()) {
        return;
      }

      final List<Library> libraries = d.getSelectedLibraries();
      addSharedLibraries(libraries);
    }
  }

  public void addSharedLibraries(final List<Library> libraries) {
    DefaultMutableTreeNode rootNode = myTable.getRoot();
    for (Library library : libraries) {
      SharedLibraryItem item = new SharedLibraryItem(library.getName(), library.getTable().getTableLevel(), library, myProject);
      rootNode.add(new DefaultMutableTreeNode(item, false));
    }
    updateTableOnItemsAdded(libraries.size());
  }

  public void addBCDependency(final FlexBCConfigurable dependencyConfigurable, final LinkageType linkageType) {
    final DefaultMutableTreeNode rootNode = myTable.getRoot();

    final Enumeration children = rootNode.children();
    while (children.hasMoreElements()) {
      final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)children.nextElement();
      final Object userObject = childNode.getUserObject();
      if (userObject instanceof BCItem && ((BCItem)userObject).configurable == dependencyConfigurable) {
        return;
      }
    }

    final BCItem item = new BCItem(dependencyConfigurable);
    item.setLinkageType(linkageType);
    // todo let BC-on-BC dependency be before BC-on-lib dependencies. Need also to fix FlexProjectConfigurationEditor.setEntries()
    rootNode.add(new DefaultMutableTreeNode(item, false));
    updateTableOnItemsAdded(1);
  }

  public void removeDependency(final String moduleLibraryId) {
    final DefaultMutableTreeNode rootNode = myTable.getRoot();

    final Enumeration children = rootNode.children();
    while (children.hasMoreElements()) {
      final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)children.nextElement();
      final Object userObject = childNode.getUserObject();
      if (userObject instanceof ModuleLibraryItem && moduleLibraryId.equals(((ModuleLibraryItem)userObject).libraryId)) {
        childNode.removeFromParent();
        myTable.refresh();
        return;
      }
    }
  }

  public void removeDependency(final String sharedLibraryLevel, final String sharedLibraryName) {
    final DefaultMutableTreeNode rootNode = myTable.getRoot();

    final Enumeration children = rootNode.children();
    while (children.hasMoreElements()) {
      final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)children.nextElement();
      final Object userObject = childNode.getUserObject();
      if (userObject instanceof SharedLibraryItem &&
          sharedLibraryLevel.equals(((SharedLibraryItem)userObject).libraryLevel) &&
          sharedLibraryName.equals(((SharedLibraryItem)userObject).libraryName)) {
        childNode.removeFromParent();
        myTable.refresh();
        return;
      }
    }
  }

  private void updateTableOnItemsAdded(int count) {
    myTable.refresh();
    myTable.getSelectionModel().clearSelection();
    int rowCount = myTable.getRowCount();
    myTable.getSelectionModel().addSelectionInterval(rowCount - count, rowCount - 1);
  }

  private void updateOnSelectedSdkChange() {
    Sdk sdk = mySdkCombo.getSelectedJdk();
    if (sdk != null && (sdk.getSdkType() != FlexSdkType2.getInstance() && sdk.getSdkType() != FlexmojosSdkType.getInstance())) {
      sdk = null; // TODO remove this when SDK filters out non-Flex items
    }
    BCUtils.updateAvailableTargetPlayers(sdk, myTargetPlayerCombo);
    updateComponentSetCombo();
    updateSdkTableItem(sdk);
    myTable.refresh();
    mySdkChangeDispatcher.getMulticaster().stateChanged(new ChangeEvent(this));
  }

  @Override
  public ActionCallback navigateTo(@Nullable final Place place, final boolean requestFocus) {
    if (place != null) {
      final Object location = place.getPath(FlexBCConfigurable.LOCATION_ON_TAB);
      if (location == Location.SDK) {
        if (requestFocus) {
          return IdeFocusManager.findInstance().requestFocus(mySdkCombo, true);
        }
      }
      else if (location instanceof Location.TableEntry) {
        for (int row = 0; row < myTable.getRowCount(); row++) {
          MyTableItem item = myTable.getItemAt(row);
          Location.TableEntry loc = item.getLocation();
          if (loc.equals(location)) {
            myTable.clearSelection();
            myTable.getSelectionModel().addSelectionInterval(row, row);
            TableUtil.scrollSelectionToVisible(myTable);
            break;
          }
        }
        if (requestFocus) {
          return IdeFocusManager.findInstance().requestFocus(myTable, true);
        }
      }
    }
    return ActionCallback.DONE;
  }

  public void libraryReplaced(@NotNull final Library library, @Nullable final Library replacement) {
    assert myReset;
    // look in UI as there is no way to find just-created-and-then-renamed library in model
    List<MyTableItem> items = myTable.getItems();
    for (int i = 0; i < items.size(); i++) {
      MyTableItem item = items.get(i);
      if (item instanceof SharedLibraryItem && ((SharedLibraryItem)item).findLiveLibrary() == library) {
        removeItems(Collections.singleton(item), replacement == null);
        break;
      }
    }
    if (replacement != null) {
      addSharedLibraries(Collections.singletonList(replacement));
    }
  }

  public void addUserActivityListener(final UserActivityListener listener, final Disposable disposable) {
    myUserActivityDispatcher.addListener(listener, disposable);
  }

  public void removeUserActivityListeners() {
    for (UserActivityListener listener : myUserActivityDispatcher.getListeners()) {
      myUserActivityDispatcher.removeListener(listener);
    }
  }

  private static final class LibraryTableModifiableModelWrapper implements LibraryTable.ModifiableModel {
    private final LibraryTable.ModifiableModel myDelegate;
    private final Condition<Library> myLibraryFilter;

    private LibraryTableModifiableModelWrapper(LibraryTable.ModifiableModel delegate, Condition<Library> libraryFilter) {
      myDelegate = delegate;
      myLibraryFilter = libraryFilter;
    }

    @NotNull
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
      return new FilteringIterator<>(myDelegate.getLibraryIterator(), myLibraryFilter);
    }

    @Override
    @Nullable
    public Library getLibraryByName(@NotNull String name) {
      Library library = myDelegate.getLibraryByName(name);
      return myLibraryFilter.value(library) ? library : null;
    }

    @Override
    public Library @NotNull [] getLibraries() {
      List<Library> filtered = ContainerUtil.filter(myDelegate.getLibraries(), myLibraryFilter);
      return filtered.toArray(Library.EMPTY_ARRAY);
    }

    @Override
    public boolean isChanged() {
      return myDelegate.isChanged();
    }

    @NotNull
    @Override
    public Library createLibrary(String name, @Nullable PersistentLibraryKind<?> kind) {
      return myDelegate.createLibrary(name, kind);
    }

    @NotNull
    @Override
    public Library createLibrary(String name, @Nullable PersistentLibraryKind<?> type, @Nullable ProjectModelExternalSource externalSource) {
      return myDelegate.createLibrary(name, type, externalSource);
    }

    @Override
    public void dispose() {
      Disposer.dispose(myDelegate);
    }
  }

  private class ChooseLibrariesDialog extends ChooseLibrariesFromTablesDialog {
    private final Condition<Library> myFilter;

    ChooseLibrariesDialog(Condition<Library> liveLibraryFilter) {
      super(myMainPanel, "Choose Libraries", myProject, false);
      myFilter = liveLibraryFilter;
      init();
    }

    @Override
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

    @Override
    protected Library @NotNull [] getLibraries(@NotNull final LibraryTable table) {
      final StructureConfigurableContext context = ProjectStructureConfigurable.getInstance(myProject).getContext();
      final Library[] libraries = context.createModifiableModelProvider(table.getTableLevel()).getModifiableModel().getLibraries();
      final List<Library> filtered = ContainerUtil.mapNotNull(libraries, library -> {
        Library liveLibrary = context.getLibraryModel(library);
        if (liveLibrary == null || !FlexProjectRootsUtil.isFlexLibrary(liveLibrary) || !myFilter.value(liveLibrary)) {
          return null;
        }
        return liveLibrary;
      });
      return filtered.toArray(Library.EMPTY_ARRAY);
    }
  }

  private Pair<String, String> getModuleAndBcName() {
    FlexBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
    for (Module module : ProjectStructureConfigurable.getInstance(myProject).getModulesConfig().getModules()) {
      for (CompositeConfigurable configurable : configurator.getBCConfigurables(module)) {
        FlexBCConfigurable flexBCConfigurable = FlexBCConfigurable.unwrap(configurable);
        if (flexBCConfigurable.isParentFor(this)) {
          return Pair.create(module.getName(), flexBCConfigurable.getDisplayName());
        }
      }
    }
    return Pair.create("?", "?");
  }

  public static String getTabName() {
    return FlexBundle.message("bc.tab.dependencies.display.name");
  }
}
