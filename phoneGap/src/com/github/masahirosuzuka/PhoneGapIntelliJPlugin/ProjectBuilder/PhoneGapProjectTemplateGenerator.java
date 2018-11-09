// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import icons.PhoneGapIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapStartupActivity.EXCLUDED_WWW_DIRECTORY;

public class PhoneGapProjectTemplateGenerator extends WebProjectTemplate<PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings> {

  private static final Logger LOG = Logger.getInstance(PhoneGapProjectTemplateGenerator.class);

  @Nls
  @NotNull
  @Override
  public String getName() {
    return PhoneGapBundle.message("phonegap.app.name");
  }

  @Override
  public String getDescription() {
    return PhoneGapBundle.message("phonegap.app.name.titile");
  }

  @Override
  public void generateProject(@NotNull final Project project,
                              final @NotNull VirtualFile baseDir,
                              @NotNull final PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings settings,
                              @NotNull Module module) {
    try {
      Ref<PhoneGapCommandLine> commandLineRef = Ref.create();
      ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {

        try {
          ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
          indicator.setText("Creating...");
          File tempProject = createTemp();
          PhoneGapCommandLine commandLine = new PhoneGapCommandLine(settings.getExecutable(), tempProject.getPath());
          commandLineRef.set(commandLine);

          if (!commandLine.isCorrectExecutable()) {
            showErrorMessage("Incorrect path");
            return;
          }
          commandLine.createNewProject(settings.name(), indicator);

          File[] array = tempProject.listFiles();
          if (array != null && array.length != 0) {
            File from = ContainerUtil.getFirstItem(ContainerUtil.newArrayList(array));
            assert from != null;
            FileUtil.copyDir(from, new File(baseDir.getPath()));
            deleteTemp(tempProject);
          }
          else {
            showErrorMessage(PhoneGapBundle.message("phonegap.project.template.create.no.files") +
                             " " +
                             tempProject.getAbsolutePath());
          }
        }
        catch (Exception e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }, PhoneGapBundle.message("phonegap.project.template.create.title"), false, project);

      ApplicationManager.getApplication().runWriteAction(() -> {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);

        propertiesComponent.setValue(PhoneGapSettings.PHONEGAP_WORK_DIRECTORY, project.getBasePath());
        PhoneGapSettings.State state = PhoneGapSettings.getInstance().getState();
        if (!StringUtil.equals(settings.getExecutable(), state.getExecutablePath())) {
          PhoneGapSettings.getInstance().loadState(new PhoneGapSettings.State(settings.executable, state.repositoriesList));
        }
        VfsUtil.markDirty(false, true, baseDir);
        createRunConfiguration(project, settings, commandLineRef.get());

        baseDir.refresh(true, true, () -> {
          if (PhoneGapSettings.getInstance().isExcludePlatformFolder()) {
            VirtualFile platformsFolder = baseDir.findChild(PhoneGapUtil.FOLDER_PLATFORMS);

            if (platformsFolder != null) {
              PhoneGapStartupActivity.excludeFolder(project, platformsFolder);
            }

            VirtualFile wwwFolder = baseDir.findChild(PhoneGapUtil.FOLDER_WWW);
            if (wwwFolder != null) {
              if (PhoneGapStartupActivity.isIonic2WwwDirectory(wwwFolder, baseDir)) {
                PropertiesComponent.getInstance(project).setValue(EXCLUDED_WWW_DIRECTORY, true);
                PhoneGapStartupActivity.excludeFolder(project, wwwFolder);
              }
            }
          }
        });
      });
    }
    catch (Exception e) {
      LOG.warn(e);
      showErrorMessage(e.getMessage());
    }
  }

  private static void createRunConfiguration(@NotNull Project project,
                                             @NotNull PhoneGapProjectSettings settings,
                                             @Nullable PhoneGapCommandLine commandLine) {
    if (commandLine == null) {
      commandLine = new PhoneGapCommandLine("", null);
    }
    final RunManager runManager = RunManager.getInstance(project);
    PhoneGapConfigurationType configurationType = ConfigurationTypeUtil.findConfigurationType(PhoneGapConfigurationType.class);
    RunnerAndConfigurationSettings configuration =
      runManager.createConfiguration(
        PhoneGapBundle.message("phonegap.project.template.create.run.configuration.title", commandLine.getPlatformName()),
        configurationType.getConfigurationFactories()[0]);

    PhoneGapRunConfiguration runConfiguration = (PhoneGapRunConfiguration)configuration.getConfiguration();
    runConfiguration.setExecutable(settings.executable);
    runConfiguration.setWorkDir(project.getBasePath());
    runConfiguration.setPlatform(
      SystemInfo.isMac ? PhoneGapRunConfigurationEditor.PLATFORM_IOS : PhoneGapRunConfigurationEditor.PLATFORM_ANDROID);
    runConfiguration.setCommand(PhoneGapCommandLine.COMMAND_EMULATE);
    runManager.addConfiguration(configuration);
    runManager.setSelectedConfiguration(configuration);
  }

  protected File createTemp() throws IOException {
    return FileUtil.createTempDirectory("intellij-phonegap-generator", null, false);
  }

  protected void deleteTemp(File tempProject) {
    if (!FileUtil.delete(tempProject)) {
      LOG.warn("Cannot delete " + tempProject);
    }
    else {
      LOG.info("Successfully deleted " + tempProject);
    }
  }

  @NotNull
  @Override
  public PhoneGapProjectPeer createPeer() {
    return new PhoneGapProjectPeer();
  }

  @Override
  public Icon getIcon() {
    return PhoneGapIcons.PhonegapIntegration;
  }

  final static class PhoneGapProjectSettings {
    private final String name = "example";
    @Nullable
    private String executable;
    @Nullable
    private String options;

    public void setExecutable(@Nullable String executable) {
      this.executable = executable;
    }

    public void setOptions(@Nullable String options) {
      this.options = options;
    }

    @Nullable
    public String getExecutable() {
      return executable;
    }

    @Nullable
    public String getOptions() {
      return StringUtil.nullize(options, true);
    }


    public String name() {
      return name;
    }
  }

  private static void showErrorMessage(@NotNull String message) {
    String fullMessage = PhoneGapBundle.message("phonegap.project.template.error.text", message);
    String title = PhoneGapBundle.message("phonegap.project.template.error.notification.title");
    Notifications.Bus.notify(
      new Notification(PhoneGapBundle.message("phonegap.project.template.error.notification"), title, fullMessage, NotificationType.ERROR)
    );
  }
}
