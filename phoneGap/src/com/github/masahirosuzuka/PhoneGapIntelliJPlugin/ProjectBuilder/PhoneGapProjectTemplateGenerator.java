package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.icons.PhoneGapIcons;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
  public void generateProject(@NotNull Project project,
                              final @NotNull VirtualFile baseDir,
                              @NotNull PhoneGapProjectTemplateGenerator.PhoneGapProjectSettings settings,
                              @NotNull Module module) {
    try {

      File tempProject = createTemp();
      GeneralCommandLine commandLine = new GeneralCommandLine(settings.getCommand());
      commandLine.setWorkDirectory(tempProject);
      commandLine.addParameters(settings.params());
      Process process = commandLine.createProcess();
      process.waitFor();

      File[] array = tempProject.listFiles();
      assert array != null && array.length != 0;
      File from = ContainerUtil.getFirstItem(ContainerUtil.newArrayList(array));
      assert from != null;

      FileUtil.copyDir(from, new File(baseDir.getPath()));

      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          baseDir.refresh(false, true);
        }
      });

      deleteTemp(tempProject);
    }
    catch (Exception e) {
      LOG.warn(e);
      showErrorMessage(project, e.getMessage());
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
    return PhoneGapIcons.get16pxIcon();
  }

  final static class PhoneGapProjectSettings {
    private String executable;

    public void setExecutable(String executable) {
      this.executable = executable;
    }

    public String getCommand() {
      return executable;
    }

    public List<String> params() {
      return ContainerUtil.newArrayList("create", "example");
    }
  }

  private static void showErrorMessage(@NotNull Project project, @NotNull String message) {
    String fullMessage = "Error creating PhoneGap/Cordova App. " + message;
    String title = "Create PhoneGap/Cordova Project";
    Messages.showErrorDialog(project, fullMessage, title);
    Notifications.Bus.notify(
      new Notification("PhoneGap/Cordova Generator", title, fullMessage, NotificationType.ERROR)
    );
  }
}
