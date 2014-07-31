package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.plugins;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.packaging.PackagesNotificationPanel;

import javax.swing.*;
import java.awt.*;


public class PhoneGapPluginsView {
  private final PhoneGapInstalledPluginsPanel myPanel;
  private final JPanel myComponent;
  private PackagesNotificationPanel packagesNotificationPanel;

  public PhoneGapPluginsView(Project project) {
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

  public void setupService(final String path, final String workdir, final VersionCallback callback) {
    packagesNotificationPanel.removeAllLinkHandlers();
    packagesNotificationPanel.hide();
    callback.forVersion("");
    if (StringUtil.isEmpty(path)) {
      return;
    }

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        final Ref<String> error = new Ref<String>();
        final Ref<String> warning = new Ref<String>();
        final Ref<PhoneGapPackageManagementService> service = new Ref<PhoneGapPackageManagementService>();
        final Ref<String> version = new Ref<String>();
        try {
          PhoneGapCommandLine commandLine = new PhoneGapCommandLine(path, workdir);

          if (commandLine.isCorrectExecutable()) {
            version.set(commandLine.version());
            ProcessOutput output = commandLine.pluginListRaw();
            if (StringUtil.isEmpty(output.getStderr())) {
              service.set(new PhoneGapPackageManagementService(commandLine));

              if (commandLine.isOld()) {
                warning.set("Phonegap/Cordova version before 3.5 doesn't support plugin version management");
              }
            }
            else {
              error.set("Project root directory is not phonegap/cordova project");
            }
          }
          else {
            error.set("Please correct path to phoneGap/cordova executable");
          }
        }
        catch (Exception e) {
          error.set("Please correct path to phoneGap/cordova executable");
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
