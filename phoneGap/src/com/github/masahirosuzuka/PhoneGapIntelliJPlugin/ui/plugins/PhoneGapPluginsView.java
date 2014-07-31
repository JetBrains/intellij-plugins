package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.plugins;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.FormBuilder;
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

  public void setupService(PhoneGapCommandLine commandLine) {
    PhoneGapPackageManagementService service = null;

    if (!commandLine.isCorrectExecutable()) {
      packagesNotificationPanel.showError("Please correct path to phoneGap/cordova executable", null, null);
      myPanel.updatePackages(null);
      return;
    }

    packagesNotificationPanel.hide();
    packagesNotificationPanel.removeAllLinkHandlers();

    try {
        ProcessOutput output = commandLine.pluginListRaw();
        if (StringUtil.isEmpty(output.getStderr())) {
          service = new PhoneGapPackageManagementService(commandLine);
          if (commandLine.isOld()) {
            packagesNotificationPanel.showWarning("Phonegap/Cordova version before 3.5 doesn't support plugin version management");
          }
        } else {
          packagesNotificationPanel.showError("Project root directory is not phonegap/cordova project", null, null);
        }
    }
    catch (Exception e) {
      packagesNotificationPanel.showError("Please correct path to phoneGap/cordova executable", null, null);
    }

    myPanel.updatePackages(service);
  }

  public JPanel getPanel() {
    return myComponent;
  }
}
