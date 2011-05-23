package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
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
    final List<Module> modules = new ArrayList<Module>(flashBuilderProjects.size());
    final Collection<ModifiableRootModel> rootModels = new ArrayList<ModifiableRootModel>(flashBuilderProjects.size());

    final Collection<String> allImportedModuleNames = new ArrayList<String>(flashBuilderProjects.size());
    final Set<String> pathVariables = new THashSet<String>();
    for (final FlashBuilderProject flashBuilderProject : flashBuilderProjects) {
      allImportedModuleNames.add(flashBuilderProject.getName());
      pathVariables.addAll(flashBuilderProject.getUsedPathVariables());
    }

    final FlashBuilderSdkFinder sdkFinder =
      new FlashBuilderSdkFinder(project, getParameters().initiallySelectedDirPath, flashBuilderProjects);

    final FlashBuilderModuleImporter flashBuilderModuleImporter =
      new FlashBuilderModuleImporter(project, allImportedModuleNames, sdkFinder, pathVariables);

    final Set<String> moduleNames = new THashSet<String>(flashBuilderProjects.size());

    for (final FlashBuilderProject flashBuilderProject : flashBuilderProjects) {
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

      final Module module = moduleModel.newModule(moduleFilePath, StdModuleTypes.JAVA);
      final ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
      rootModels.add(rootModel);
      flashBuilderModuleImporter.setupModule(rootModel, flashBuilderProject);
      modules.add(module);
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        ProjectRootManager.getInstance(project).multiCommit(moduleModel, rootModels.toArray(new ModifiableRootModel[rootModels.size()]));
      }
    });
    return modules;
  }

  private static String makeUnique(final String name, final Set<String> moduleNames) {
    String uniqueName = name;
    int i = 1;
    while (moduleNames.contains(uniqueName)) {
      uniqueName = name + '(' + i++ + ')';
    }
    return uniqueName;
  }
}
