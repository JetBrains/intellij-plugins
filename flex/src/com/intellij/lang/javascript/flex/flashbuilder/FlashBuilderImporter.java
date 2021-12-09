// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.ModifiableModelCommitter;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.projectImport.ProjectImportBuilder;
import com.intellij.util.PathUtil;
import com.intellij.util.io.ZipUtil;
import icons.FlexIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class FlashBuilderImporter extends ProjectImportBuilder<String> {

  public static final String DOT_PROJECT = ".project";
  public static final String DOT_FXP = ".fxp";
  public static final String DOT_FXPL = ".fxpl";
  public static final String DOT_ZIP = ".zip";
  public static final String DOT_ACTION_SCRIPT_PROPERTIES = ".actionScriptProperties";
  public static final String DOT_FXP_PROPERTIES = ".fxpProperties";
  public static final String DOT_FLEX_PROPERTIES = ".flexProperties";
  public static final String DOT_FLEX_LIB_PROPERTIES = ".flexLibProperties";

  private Parameters myParameters;

  public static class Parameters {
    private String myInitiallySelectedPath = "";
    private List<String> myFlashBuilderProjectFilePaths = Collections.emptyList();
    private String myExtractPath = "";
    private boolean myExtractToSubfolder = false;
    private boolean myOpenProjectSettingsAfter = false;
  }

  public Parameters getParameters() {
    if (myParameters == null) {
      myParameters = new Parameters();
    }
    return myParameters;
  }

  @Override
  public void cleanup() {
    super.cleanup();
    myParameters = null;
  }

  @Override
  @NotNull
  public String getName() {
    return FlexBundle.message("flash.builder");
  }

  @Override
  public Icon getIcon() {
    return FlexIcons.Flex.Flash_builder;
  }

  @Override
  public boolean isMarked(final String element) {
    return true;
  }

  @Override
  public boolean isOpenProjectSettingsAfter() {
    return getParameters().myOpenProjectSettingsAfter;
  }

  @Override
  public void setOpenProjectSettingsAfter(boolean openProjectSettingsAfter) {
    getParameters().myOpenProjectSettingsAfter = openProjectSettingsAfter;
  }

  public boolean isExtractToSubfolder() {
    return getParameters().myExtractToSubfolder;
  }

  public void setExtractToSubfolder(final boolean extractToSubfolder) {
    getParameters().myExtractToSubfolder = extractToSubfolder;
  }

  public String getExtractPath() {
    return getParameters().myExtractPath;
  }

  public void setExtractPath(final String extractPath) {
    getParameters().myExtractPath = extractPath;
  }

  @Override
  public List<String> getList() {
    return getParameters().myFlashBuilderProjectFilePaths;
  }

  @Override
  public void setList(final List<String> flashBuilderProjectFiles) /*throws ConfigurationException*/ {
    getParameters().myFlashBuilderProjectFilePaths = flashBuilderProjectFiles;
  }

  void setInitiallySelectedPath(final String dirPath) {
    getParameters().myInitiallySelectedPath = dirPath;
  }

  String getInitiallySelectedPath() {
    return getParameters().myInitiallySelectedPath;
  }

  public String getSuggestedProjectName() {
    final String path = getInitiallySelectedPath();
    final VirtualFile file = path.isEmpty() ? null : LocalFileSystem.getInstance().findFileByPath(path);

    if (file == null) {
      return PathUtil.getFileName(path);
    }

    if (file.isDirectory()) {
      final VirtualFile dotProjectFile = file.findChild(DOT_PROJECT);
      if (dotProjectFile != null && FlashBuilderProjectFinder.isFlashBuilderProject(dotProjectFile)) {
        return FlashBuilderProjectLoadUtil.readProjectName(dotProjectFile.getPath());
      }
    }
    else if (FlashBuilderProjectFinder.hasArchiveExtension(path)) {
      return file.getNameWithoutExtension();
    }


    if (DOT_PROJECT.equalsIgnoreCase(file.getName())) {
      return FlashBuilderProjectLoadUtil.readProjectName(file.getPath());
    }

    return PathUtil.getFileName(path);
  }

  @Override
  public List<Module> commit(final Project project,
                             final ModifiableModuleModel model,
                             final ModulesProvider modulesProvider,
                             final ModifiableArtifactModel artifactModel) {
    //FlexModuleBuilder.setupResourceFilePatterns(project);

    final boolean needToCommit = model == null;
    final ModifiableModuleModel moduleModel = model != null ? model : ModuleManager.getInstance(project).getModifiableModel();

    final List<String> paths = getList();
    final boolean isArchive = paths.size() == 1 && FlashBuilderProjectFinder.hasArchiveExtension(paths.get(0));
    final List<String> dotProjectPaths = getDotProjectPaths(project);
    final List<FlashBuilderProject> flashBuilderProjects = FlashBuilderProjectLoadUtil.loadProjects(dotProjectPaths, isArchive);

    final Map<FlashBuilderProject, ModifiableRootModel> flashBuilderProjectToModifiableModelMap = new HashMap<>();
    final Map<Module, ModifiableRootModel> moduleToModifiableModelMap = new HashMap<>();
    final Set<String> moduleNames = new HashSet<>(flashBuilderProjects.size());

    final FlexProjectConfigurationEditor currentFlexEditor =
      FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();
    assert needToCommit == (currentFlexEditor == null);

    for (FlashBuilderProject flashBuilderProject : flashBuilderProjects) {
      final String moduleName = makeUnique(flashBuilderProject.getName(), moduleNames);
      moduleNames.add(moduleName);

      final String moduleFilePath = flashBuilderProject.getProjectRootPath() + "/" + moduleName + ModuleFileType.DOT_DEFAULT_EXTENSION;

      if (LocalFileSystem.getInstance().findFileByPath(moduleFilePath) != null) {
        ApplicationManager.getApplication().runWriteAction(() -> ModuleBuilder.deleteModuleFile(moduleFilePath));
      }

      final Module module = moduleModel.newModule(moduleFilePath, FlexModuleType.getInstance().getId());
      final ModifiableRootModel rootModel = currentFlexEditor != null
                                            ? currentFlexEditor.getModifiableRootModel(module)
                                            : ModuleRootManager.getInstance(module).getModifiableModel();

      flashBuilderProjectToModifiableModelMap.put(flashBuilderProject, rootModel);
      moduleToModifiableModelMap.put(module, rootModel);
    }

    final FlexProjectConfigurationEditor flexConfigEditor = currentFlexEditor != null
                                                            ? currentFlexEditor
                                                            : FlexProjectConfigurationEditor
                                                              .createEditor(project, moduleToModifiableModelMap, null, null);

    final FlashBuilderSdkFinder sdkFinder =
      new FlashBuilderSdkFinder(project, getParameters().myInitiallySelectedPath, flashBuilderProjects);

    final FlashBuilderModuleImporter flashBuilderModuleImporter =
      new FlashBuilderModuleImporter(project, flexConfigEditor, flashBuilderProjects, sdkFinder);

    for (final FlashBuilderProject flashBuilderProject : flashBuilderProjects) {
      flashBuilderModuleImporter.setupModule(flashBuilderProjectToModifiableModelMap.get(flashBuilderProject), flashBuilderProject);
    }

    if (needToCommit) {
      try {
        flexConfigEditor.commit();
      }
      catch (ConfigurationException e) {
        Logger.getInstance(FlashBuilderImporter.class).error(e);
      }

      ApplicationManager.getApplication().runWriteAction(() -> ModifiableModelCommitter.multiCommit(moduleToModifiableModelMap.values(), moduleModel));
    }

    return new ArrayList<>(moduleToModifiableModelMap.keySet());
  }

  private List<String> getDotProjectPaths(final Project project) {
    final boolean creatingNewProject = !isUpdate();
    final List<String> paths = getList();

    if (paths.size() == 1 && FlashBuilderProjectFinder.hasArchiveExtension(paths.get(0))) {
      final List<String> dotProjectFiles = new ArrayList<>();
      final boolean multipleProjects = FlashBuilderProjectFinder.isMultiProjectArchive(paths.get(0));

      final String basePath = creatingNewProject ? project.getBasePath() : getExtractPath();
      assert basePath != null;
      final String extractDir = multipleProjects || isExtractToSubfolder()
                                ? basePath + "/" + FileUtilRt.getNameWithoutExtension(PathUtil.getFileName(paths.get(0)))
                                : basePath;

      try {
        final File outputDir = new File(extractDir);
        ZipUtil.extract(new File(paths.get(0)), outputDir, null);
        dotProjectFiles.add(extractDir + "/" + DOT_PROJECT);

        extractNestedFxpAndAppendProjects(outputDir, dotProjectFiles);

        ApplicationManager.getApplication().runWriteAction(() -> {
          for (String dotProjectFile : dotProjectFiles) {
            final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(PathUtil.getParentPath(dotProjectFile));
            if (file != null) {
              file.refresh(false, true);
            }
          }
        });
      }
      catch (IOException e) {
        Messages.showErrorDialog(project, FlexBundle.message("failed.to.extract.project", e.getMessage()),
                                 FlexBundle.message("open.project.0", PathUtil.getFileName(paths.get(0))));
        return Collections.emptyList();
      }

      return dotProjectFiles;
    }

    return paths;
  }

  private static void extractNestedFxpAndAppendProjects(final File dir, final List<String> dotProjectFiles) throws IOException {
    final FilenameFilter filter = (dir1, name) -> {
      final String lowercased = StringUtil.toLowerCase(name);
      return lowercased.endsWith(DOT_FXP) || lowercased.endsWith(DOT_FXPL);
    };

    for (File file : dir.listFiles(filter)) {
      final File extractDir = new File(file.getParentFile().getParentFile(), FileUtilRt.getNameWithoutExtension(file.getName()));
      ZipUtil.extract(file, extractDir, null);
      dotProjectFiles.add(extractDir + "/" + DOT_PROJECT);

      extractNestedFxpAndAppendProjects(extractDir, dotProjectFiles);
    }
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
