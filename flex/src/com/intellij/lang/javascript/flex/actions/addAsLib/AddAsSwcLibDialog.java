package com.intellij.lang.javascript.flex.actions.addAsLib;

import com.intellij.flex.model.bc.LinkageType;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.FlexBCTree;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.DependenciesConfigurable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.navigation.Place;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class AddAsSwcLibDialog extends DialogWrapper {
  private final Project myProject;
  private final List<VirtualFile> myRoots;

  private JPanel myMainPanel;
  private FlexBCTree myBCTree;
  private JBList myLibComponentsList;
  private JCheckBox myOpenProjectStructureCheckBox;

  public AddAsSwcLibDialog(final @NotNull Project project, final @Nullable Module preferredModule, final List<VirtualFile> roots) {
    super(project);
    myProject = project;
    myRoots = roots;

    setTitle(FlexBundle.message("add.as.library.title"));

    if (preferredModule != null) {
      final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(preferredModule).getActiveConfiguration();
      myBCTree.selectRow(preferredModule, bc);
    }

    myLibComponentsList.setModel(JBList.createDefaultListModel(roots.toArray(VirtualFile.EMPTY_ARRAY)));
    myLibComponentsList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        final JLabel component = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        final VirtualFile file = (VirtualFile)value;
        component.setText(FileUtil.toSystemDependentName(file.getPath()));
        if (file.isDirectory() && file.isInLocalFileSystem()) {
          component.setIcon(AllIcons.Nodes.JarDirectory);
        }
        else {
          final VirtualFile localFile = JarFileSystem.getInstance().getLocalByEntry(file);
          if (localFile != null) {
            component.setText(FileUtil.toSystemDependentName(localFile.getPath()));
            component.setIcon(IconUtil.getIcon(localFile, 0, null));
          }
        }

        return component;
      }
    });
    myLibComponentsList.setVisibleRowCount(myLibComponentsList.getItemsCount());

    init();
  }

  @Override
  @Nullable
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  @Nullable
  public JComponent getPreferredFocusedComponent() {
    return myBCTree;
  }

  private void createUIComponents() {
    myBCTree = new FlexBCTree(myProject);
    myBCTree.setCheckedStatusForAll(false);
    myBCTree.setVisibleRowCount(Math.min(myBCTree.getRowCount(), 15));
  }

  @Override
  @Nullable
  protected ValidationInfo doValidate() {
    if (myBCTree.getSelectedBCs().isEmpty()) {
      return new ValidationInfo("No build configurations selected");
    }
    return null;
  }

  @Override
  protected void doOKAction() {
    final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCs = myBCTree.getSelectedBCs();

    final Map<Module, ModifiableRootModel> moduleToModifiableModelMap = new HashMap<>();
    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : modulesAndBCs) {
      moduleToModifiableModelMap.put(moduleAndBC.first, ModuleRootManager.getInstance(moduleAndBC.first).getModifiableModel());
    }

    final LibraryTable.ModifiableModel projectLibsModel = LibraryTablesRegistrar.getInstance().getLibraryTable(myProject).getModifiableModel();

    final LibraryTable.ModifiableModel globalLibsModel = LibraryTablesRegistrar.getInstance().getLibraryTable().getModifiableModel();

    final FlexProjectConfigurationEditor flexConfigEditor =
      FlexProjectConfigurationEditor.createEditor(myProject, moduleToModifiableModelMap, projectLibsModel, globalLibsModel);

    addLib(flexConfigEditor, modulesAndBCs, myRoots);

    try {
      flexConfigEditor.commit();
    }
    catch (ConfigurationException e) {
      Logger.getInstance(AddAsSwcLibDialog.class).error(e);
    }

    ApplicationManager.getApplication().runWriteAction(() -> {
      globalLibsModel.commit();
      projectLibsModel.commit();

      for (ModifiableRootModel modifiableRootModel : moduleToModifiableModelMap.values()) {
        modifiableRootModel.commit();
      }
    });

    if (myOpenProjectStructureCheckBox.isSelected()) {
      final Pair<Module, FlexBuildConfiguration> moduleAndBc = modulesAndBCs.iterator().next();
      openProjectStructure(moduleAndBc.first, moduleAndBc.second);
    }

    super.doOKAction();
  }

  private static void addLib(final FlexProjectConfigurationEditor flexConfigEditor,
                             final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCs,
                             final List<VirtualFile> roots) {
    for (Pair<Module, FlexBuildConfiguration> moduleAndBc : modulesAndBCs) {
      final Module module = moduleAndBc.first;
      ModifiableFlexBuildConfiguration bc = null;

      final ModifiableFlexBuildConfiguration[] bcs = flexConfigEditor.getConfigurations(module);
      for (ModifiableFlexBuildConfiguration each : bcs) {
        if (each.getName().equals(moduleAndBc.second.getName())) {
          bc = each;
          break;
        }
      }

      if (bc == null) continue;

      final Collection<VirtualFile> filteredRoots = filterAlreadyExistingRoots(roots, flexConfigEditor, module, bc);

      final LibraryTable.ModifiableModel libraryModel = flexConfigEditor.getLibraryModel(bc.getDependencies());

      for (VirtualFile file : filteredRoots) {
        final Library library = libraryModel.createLibrary(null, FlexLibraryType.FLEX_LIBRARY);
        final LibraryEx.ModifiableModelEx libraryModifiableModel = ((LibraryEx.ModifiableModelEx)library.getModifiableModel());
        final String libraryId = UUID.randomUUID().toString();
        libraryModifiableModel.setProperties(new FlexLibraryProperties(libraryId));

        if (file.isInLocalFileSystem() && file.isDirectory()) {
          libraryModifiableModel.addJarDirectory(file, false);
        }
        else {
          libraryModifiableModel.addRoot(file, OrderRootType.CLASSES);
        }

        ApplicationManager.getApplication().runWriteAction(() -> libraryModifiableModel.commit());

        final ModifiableModuleLibraryEntry libraryEntry = flexConfigEditor.createModuleLibraryEntry(bc.getDependencies(), libraryId);
        libraryEntry.getDependencyType().setLinkageType(LinkageType.Merged);
        bc.getDependencies().getModifiableEntries().add(libraryEntry);
      }
    }
  }

  private static Collection<VirtualFile> filterAlreadyExistingRoots(final Collection<VirtualFile> roots,
                                                                    final FlexProjectConfigurationEditor flexConfigEditor,
                                                                    final Module module,
                                                                    final ModifiableFlexBuildConfiguration bc) {
    final Set<VirtualFile> result = new HashSet<>(roots);

    final DependencyEntry[] entries = bc.getDependencies().getEntries();
    for (DependencyEntry entry : entries) {
      if (entry instanceof ModifiableModuleLibraryEntry) {
        final LibraryOrderEntry orderEntry = FlexProjectRootsUtil
          .findOrderEntry((ModuleLibraryEntry)entry, flexConfigEditor.getModifiableRootModel(module));
        if (orderEntry != null) {
          for (VirtualFile file : orderEntry.getRootFiles(OrderRootType.CLASSES)) {
            result.remove(file);
          }
        }
      }
      else if (entry instanceof ModifiableSharedLibraryEntry) {
        final Library library = FlexProjectRootsUtil.findOrderEntry(module.getProject(), (SharedLibraryEntry)entry);
        if (library != null) {
          for (VirtualFile file : library.getFiles(OrderRootType.CLASSES)) {
            result.remove(file);
          }
        }
      }
    }

    return result;
  }

  private void openProjectStructure(final Module module, final FlexBuildConfiguration bc) {
    ApplicationManager.getApplication().invokeLater(() -> {
      final ProjectStructureConfigurable configurable = ProjectStructureConfigurable.getInstance(myProject);

      ShowSettingsUtil.getInstance().editConfigurable(myProject, configurable, () -> {
        final Place place = FlexBuildConfigurationsExtension.getInstance().getConfigurator()
          .getPlaceFor(module, bc.getName())
          .putPath(CompositeConfigurable.TAB_NAME, DependenciesConfigurable.getTabName());
        configurable.navigateTo(place, true);
      });
    });
  }
}