package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
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
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import icons.PhoneGapIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class PhoneGapProjectTemplateGenerator extends WebProjectTemplate<PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings> {

  private static final Logger LOG = Logger.getInstance(PhoneGapProjectTemplateGenerator.class);

  @Nls
  @NotNull
  @Override
  public String getName() {
    return "PhoneGap/Cordova App";
  }

  @Override
  public String getDescription() {
    return "<html>PhoneGap/Cordova application skeleton</html>";
  }

  @Override
  public void generateProject(@NotNull final Project project,
                              final @NotNull VirtualFile baseDir,
                              @NotNull final PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings settings,
                              @NotNull Module module) {
    try {

      ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
        @Override
        public void run() {

          try {
            ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setText("Creating...");
            File tempProject = createTemp();
            PhoneGapCommandLine commandLine = new PhoneGapCommandLine(settings.getExecutable(), tempProject.getPath());

            if (!commandLine.isCorrectExecutable()) {
              showErrorMessage("Incorrect path");
              return;
            }
            commandLine.createNewProject(settings.name());

            File[] array = tempProject.listFiles();
            if (array != null && array.length != 0) {
              File from = ContainerUtil.getFirstItem(ContainerUtil.newArrayList(array));
              assert from != null;
              FileUtil.copyDir(from, new File(baseDir.getPath()));
              deleteTemp(tempProject);
            }
            else {
              showErrorMessage("Cannot find files in the directory " + tempProject.getAbsolutePath());
            }
          }
          catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
          }
        }
      }, "Creating Phonegap/Cordova project", false, project);

      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
          propertiesComponent.setValue(PhoneGapSettings.PHONEGAP_WORK_DIRECTORY, project.getBasePath());
          PhoneGapSettings.State state = PhoneGapSettings.getInstance().getState();
          if (!StringUtil.equals(settings.getExecutable(), state.getExecutablePath())) {
            PhoneGapSettings.getInstance().loadState(new PhoneGapSettings.State(settings.executable, state.repositoriesList));
          }
          baseDir.refresh(false, true);
        }
      });
    }
    catch (Exception e) {
      LOG.warn(e);
      showErrorMessage(e.getMessage());
    }
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
    private String name = "example";
    private String executable;

    public void setExecutable(String executable) {
      this.executable = executable;
    }

    public String getExecutable() {
      return executable;
    }

    public String name() {
      return name;
    }
  }

  private static void showErrorMessage(@NotNull String message) {
    String fullMessage = "Error creating PhoneGap/Cordova App. " + message;
    String title = "Create PhoneGap/Cordova Project";
    Notifications.Bus.notify(
      new Notification("PhoneGap/Cordova Generator", title, fullMessage, NotificationType.ERROR)
    );
  }
}
