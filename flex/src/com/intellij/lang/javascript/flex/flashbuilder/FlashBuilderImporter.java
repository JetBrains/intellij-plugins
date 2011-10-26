package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleBuilder;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.*;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
import com.intellij.util.PlatformUtils;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import javax.swing.*;
import java.util.*;

public class FlashBuilderImporter extends ProjectImportBuilder<String> {

  public static final String DOT_PROJECT = ".project";
  public static final String DOT_ACTION_SCRIPT_PROPERTIES = ".actionScriptProperties";
  public static final String DOT_FLEX_PROPERTIES = ".flexProperties";
  public static final String DOT_FLEX_LIB_PROPERTIES = ".flexLibProperties";

  private static final Icon flashBuilderIcon = IconLoader.getIcon("flash_builder.png", FlashBuilderImporter.class);

  private Parameters myParameters;

  public static class Parameters {
    public String initiallySelectedDirPath;
    private List<String> myFlashBuilderProjectFilePaths = Collections.emptyList();
    private boolean myOpenProjectSettingsAfter;
  }

  public Parameters getParameters() {
    if (myParameters == null) {
      myParameters = new Parameters();
    }
    return myParameters;
  }

  public void cleanup() {
    super.cleanup();
    myParameters = null;
  }

  public String getName() {
    return FlexBundle.message("flash.builder");
  }

  public Icon getIcon() {
    return flashBuilderIcon;
  }

  public boolean isMarked(final String element) {
    return true;
  }

  public boolean isOpenProjectSettingsAfter() {
    return getParameters().myOpenProjectSettingsAfter;
  }

  public void setOpenProjectSettingsAfter(boolean openProjectSettingsAfter) {
    getParameters().myOpenProjectSettingsAfter = openProjectSettingsAfter;
  }

  public List<String> getList() {
    return getParameters().myFlashBuilderProjectFilePaths;
  }

  public void setList(final List<String> flashBuilderProjectFiles) /*throws ConfigurationException*/ {
    getParameters().myFlashBuilderProjectFilePaths = flashBuilderProjectFiles;
  }

  void setInitiallySelectedDirPath(final String dirPath) {
    getParameters().initiallySelectedDirPath = dirPath;
  }

  public String getSuggestedProjectName() {
    if (getParameters().myFlashBuilderProjectFilePaths.size() == 1) {
      return FlashBuilderProjectLoadUtil.getProjectName(getParameters().myFlashBuilderProjectFilePaths.get(0));
    }
    return FlexBundle.message("unnamed");
  }

  public List<Module> commit(final Project project,
                             final ModifiableModuleModel model,
                             final ModulesProvider modulesProvider,
                             final ModifiableArtifactModel artifactModel) {
    FlexModuleBuilder.setupResourceFilePatterns(project);

    final ModifiableModuleModel moduleModel = model != null ? model : ModuleManager.getInstance(project).getModifiableModel();
    final Collection<FlashBuilderProject> flashBuilderProjects = FlashBuilderProjectLoadUtil.loadProjects(getList());

    final ModuleType moduleType = PlatformUtils.isFlexIde() ? FlexModuleType.getInstance() : StdModuleTypes.JAVA;

    final Map<FlashBuilderProject, ModifiableRootModel> flashBuilderProjectToModifiableModelMap =
      new THashMap<FlashBuilderProject, ModifiableRootModel>();
    final Map<Module, ModifiableRootModel> moduleToModifiableModelMap = new THashMap<Module, ModifiableRootModel>();
    final Set<String> moduleNames = new THashSet<String>(flashBuilderProjects.size());

    for (FlashBuilderProject flashBuilderProject : flashBuilderProjects) {
      final String moduleName = makeUnique(flashBuilderProject.getName(), moduleNames);
      moduleNames.add(moduleName);

      final String moduleFilePath = flashBuilderProject.getProjectRootPath() + "/" + moduleName + ModuleFileType.DOT_DEFAULT_EXTENSION;

      if (LocalFileSystem.getInstance().findFileByPath(moduleFilePath) != null) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            ModuleBuilder.deleteModuleFile(moduleFilePath);
          }
        });
      }

      final Module module = moduleModel.newModule(moduleFilePath, moduleType);
      final ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();

      flashBuilderProjectToModifiableModelMap.put(flashBuilderProject, rootModel);
      moduleToModifiableModelMap.put(module, rootModel);
    }

    final FlexProjectConfigurationEditor currentFlexEditor =
      PlatformUtils.isFlexIde() ? FlexIdeBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor() : null;

    final boolean needToCommitFlexEditor = PlatformUtils.isFlexIde() && currentFlexEditor == null;
    final LibraryTableBase.ModifiableModelEx globalLibrariesModifiableModel;
    final FlexProjectConfigurationEditor flexConfigEditor;

    if (!PlatformUtils.isFlexIde()) {
      globalLibrariesModifiableModel = null;
      flexConfigEditor = null;
    }
    else if (currentFlexEditor != null) {
      globalLibrariesModifiableModel = null;
      flexConfigEditor = currentFlexEditor;
    }
    else {
      globalLibrariesModifiableModel =
        (LibraryTableBase.ModifiableModelEx)ApplicationLibraryTable.getApplicationTable().getModifiableModel();
      flexConfigEditor = createFlexConfigEditor(project, moduleToModifiableModelMap, globalLibrariesModifiableModel);
    }

    final FlashBuilderSdkFinder sdkFinder =
      new FlashBuilderSdkFinder(project, flexConfigEditor, getParameters().initiallySelectedDirPath, flashBuilderProjects);

    final FlashBuilderModuleImporter flashBuilderModuleImporter =
      new FlashBuilderModuleImporter(project, flexConfigEditor, flashBuilderProjects, sdkFinder);

    for (final FlashBuilderProject flashBuilderProject : flashBuilderProjects) {
      flashBuilderModuleImporter.setupModule(flashBuilderProjectToModifiableModelMap.get(flashBuilderProject), flashBuilderProject);
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        if (PlatformUtils.isFlexIde()) {
          if (globalLibrariesModifiableModel != null) {
            globalLibrariesModifiableModel.commit();
          }

          if (needToCommitFlexEditor) {
            try {
              flexConfigEditor.commit();
            }
            catch (ConfigurationException e) {
              Logger.getInstance(FlashBuilderImporter.class).error(e);
            }
          }
        }

        final Collection<ModifiableRootModel> rootModels = moduleToModifiableModelMap.values();
        ProjectRootManager.getInstance(project).multiCommit(moduleModel, rootModels.toArray(new ModifiableRootModel[rootModels.size()]));
      }
    });

    return new ArrayList<Module>(moduleToModifiableModelMap.keySet());
  }

  private static String makeUnique(final String name, final Set<String> moduleNames) {
    String uniqueName = name;
    int i = 1;
    while (moduleNames.contains(uniqueName)) {
      uniqueName = name + '(' + i++ + ')';
    }
    return uniqueName;
  }

  private static FlexProjectConfigurationEditor createFlexConfigEditor(final Project project,
                                                                       final Map<Module, ModifiableRootModel> moduleToModifiableModelMap,
                                                                       final LibraryTableBase.ModifiableModelEx globalLibrariesModifiableModel) {
    final FlexProjectConfigurationEditor.ProjectModifiableModelProvider provider =
      new FlexProjectConfigurationEditor.ProjectModifiableModelProvider() {
        public Module[] getModules() {
          final Set<Module> modules = moduleToModifiableModelMap.keySet();
          return modules.toArray(new Module[modules.size()]);
        }

        public ModifiableRootModel getModuleModifiableModel(final Module module) {
          return moduleToModifiableModelMap.get(module);
        }

        public void addListener(final FlexIdeBCConfigurator.Listener listener,
                                final Disposable parentDisposable) {
          // modules and BCs are not removed here
        }

        public void commitModifiableModels() throws ConfigurationException {
          // commit will be performed outside of #setupRootModel()
        }

        public LibraryTableBase.ModifiableModelEx getLibrariesModifiableModel(final String level) {
          if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(level)) {
            return globalLibrariesModifiableModel;
          }
          else {
            throw new UnsupportedOperationException();
          }
        }
      };

    return new FlexProjectConfigurationEditor(project, provider);
  }
}
