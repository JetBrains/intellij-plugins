// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapStartupActivity;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.PhoneGapConfigurationType;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.PhoneGapRunConfiguration;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui.PhoneGapRunConfigurationEditor;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.filters.Filter;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThreeState;
import icons.PhoneGapIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.List;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapStartupActivity.EXCLUDED_WWW_DIRECTORY;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine.isIonicPath;

public class CordovaProjectGenerator extends NpmPackageProjectGenerator {
  @Override
  protected Filter @NotNull [] filters(@NotNull Project project, @NotNull VirtualFile baseDir) {
    return Filter.EMPTY_ARRAY;
  }

  @Override
  protected void onGettingSmartAfterProjectGeneration(@NotNull Project project, @NotNull VirtualFile baseDir) {
    super.onGettingSmartAfterProjectGeneration(project, baseDir);
    createRunConfiguration(project);
    excludePlatforms(project, baseDir);
  }

  @Override
  protected String[] generatorArgs(Project project, VirtualFile dir, Settings settings) {
    NodePackage aPackage = settings.myPackage;
    File file = settings.myPackage.findBinFile(aPackage.getName(), null);
    String path = file == null ? aPackage.getName() : file.getPath();
    PhoneGapCommandLine commandLine = new PhoneGapCommandLine(path, dir.getPath());

    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    propertiesComponent.setValue(PhoneGapSettings.PHONEGAP_WORK_DIRECTORY, project.getBasePath());
    PhoneGapSettings.State state = PhoneGapSettings.getInstance().getState();
    if (!StringUtil.equals(path, state.getExecutablePath())) {
      PhoneGapSettings.getInstance().loadState(new PhoneGapSettings.State(path, state.repositoriesList));
    }

    return commandLine.getCreateNewProjectCommand(dir.getName());
  }

  @Override
  protected void customizeModule(@NotNull VirtualFile baseDir, ContentEntry entry) {

  }

  @Override
  public String getId() {
    return "Cordova";
  }

  @Nls
  @NotNull
  @Override
  public String getName() {
    return PhoneGapBundle.message("phonegap.app.name");
  }

  @NotNull
  @Override
  protected String packageName() {
    throw new IncorrectOperationException();
  }

  @Override
  protected List<String> packageNames() {
    return List.of("ionic", "cordova", "phonegap");
  }

  @Override
  protected boolean generateInTemp() {
    return true;
  }

  @Override
  protected @NotNull String presentablePackageName() {
    return PhoneGapBundle.message("phonegap.conf.name");
  }

  private static void createRunConfiguration(@NotNull Project project) {
    final RunManager runManager = RunManager.getInstance(project);
    PhoneGapConfigurationType configurationType = ConfigurationTypeUtil.findConfigurationType(PhoneGapConfigurationType.class);
    String path = PhoneGapSettings.getInstance().getExecutablePath();
    String name = isIonicPath(path) == ThreeState.YES ? "Ionic" : "Cordova";
    RunnerAndConfigurationSettings configuration =
      runManager.createConfiguration(
        PhoneGapBundle.message("phonegap.project.template.create.run.configuration.title", name),
        configurationType.getConfigurationFactories()[0]);

    PhoneGapRunConfiguration runConfiguration = (PhoneGapRunConfiguration)configuration.getConfiguration();

    runConfiguration.setExecutable(path);
    runConfiguration.setWorkDir(project.getBasePath());
    runConfiguration.setPlatform(
      SystemInfo.isMac ? PhoneGapRunConfigurationEditor.PLATFORM_IOS : PhoneGapRunConfigurationEditor.PLATFORM_ANDROID);
    runConfiguration.setCommand(PhoneGapCommandLine.COMMAND_EMULATE);
    runManager.addConfiguration(configuration);
    runManager.setSelectedConfiguration(configuration);
  }

  protected static void excludePlatforms(@NotNull Project project, @NotNull VirtualFile baseDir) {
    if (PhoneGapSettings.getInstance().isExcludePlatformFolder()) {
      VirtualFile platformsFolder = baseDir.findChild(PhoneGapUtil.FOLDER_PLATFORMS);

      if (platformsFolder != null) {
        PhoneGapStartupActivity.excludeFolder(project, platformsFolder);
      }

      VirtualFile wwwFolder = baseDir.findChild(PhoneGapUtil.FOLDER_WWW);
      if (wwwFolder != null) {
        if (PhoneGapStartupActivity.isIonic2WwwDirectory(baseDir)) {
          PropertiesComponent.getInstance(project).setValue(EXCLUDED_WWW_DIRECTORY, true);
          PhoneGapStartupActivity.excludeFolder(project, wwwFolder);
        }
      }
    }
  }


  @Override
  public Icon getIcon() {
    return PhoneGapIcons.PhonegapIntegration;
  }

  @NlsContexts.DetailedDescription
  @Override
  public String getDescription() {
    return PhoneGapBundle.message("phonegap.app.name.description");
  }
}
