package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.plugins;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.PhoneGapConfigurable;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.packaging.PackagesNotificationPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;


public class PhoneGapPluginsView {
  private final PhoneGapInstalledPluginsPanel myPanel;
  private final JPanel myComponent;
  private PackagesNotificationPanel packagesNotificationPanel;
  private Project myProject;

  public PhoneGapPluginsView(@NotNull Project project) {
    myProject = project;
    packagesNotificationPanel = new PackagesNotificationPanel(project);
    myPanel = new PhoneGapInstalledPluginsPanel(project, packagesNotificationPanel);
    myPanel.setPreferredSize(new Dimension(400, 400));
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setBorder(IdeBorderFactory.createTitledBorder("Plugins", false));
    wrapper.add(FormBuilder.createFormBuilder().addComponent(myPanel).addComponent(packagesNotificationPanel.getComponent()).getPanel());
    myComponent = wrapper;
  }

  public interface VersionCallback {
    void forVersion(String version);
  }

  public void setupService(@Nullable final String path,
                           @NotNull final PhoneGapConfigurable.RepositoryStore repositoryStore,
                           @NotNull final VersionCallback callback) {
    packagesNotificationPanel.removeAllLinkHandlers();
    packagesNotificationPanel.hide();
    callback.forVersion("");
    if (StringUtil.isEmpty(path)) {
      return;
    }

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        final Ref<PhoneGapPackageManagementService> service = new Ref<PhoneGapPackageManagementService>();
        final Ref<String> error = new Ref<String>();
        final Ref<String> warning = new Ref<String>();
        final Ref<String> version = new Ref<String>();
        try {
          PhoneGapCommandLine commandLine = new PhoneGapCommandLine(path, myProject.getBasePath());

          if (commandLine.isCorrectExecutable()) {
            version.set(commandLine.version());
            ProcessOutput output = commandLine.pluginListRaw();
            if (StringUtil.isEmpty(output.getStderr())) {
              service.set(new PhoneGapPackageManagementService(commandLine, repositoryStore));

              if (commandLine.isOld()) {
                warning.set(PhoneGapBundle.message("phonegap.plugins.executable.version.error"));
              }
            }

            else {
              error.set("Project root directory is not " + commandLine.getPlatformName() + " project");
            }
          }
          else {
            error.set(PhoneGapBundle.message("phonegap.plugins.executable.error"));
          }
        }
        catch (Exception e) {
          error.set(PhoneGapBundle.message("phonegap.plugins.executable.error"));
        }

        UIUtil.invokeLaterIfNeeded(new Runnable() {
          @Override
          public void run() {
            myPanel.updatePackages(service.get());
            if (error.get() != null) {
              packagesNotificationPanel.showError(error.get(), null, null);
            }

            if (warning.get() != null) {
              packagesNotificationPanel.showWarning(warning.get());
            }

            callback.forVersion(version.get());
          }
        });
      }
    });
  }

  public JPanel getPanel() {
    return myComponent;
  }
}
