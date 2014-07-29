package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.plugins;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommands;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.webcore.packaging.PackagesNotificationPanel;

import javax.swing.*;
import java.awt.*;


public class PhoneGapPluginsView {
  private final PhoneGapInstalledPluginsPanel myPanel;
  private final JPanel myComponent;
  private Project myProject;
  private PackagesNotificationPanel packagesNotificationPanel;
  private volatile String myCurrentPath;

  public PhoneGapPluginsView(Project project) {
    myProject = project;

    myProject = project;
    //panel.setBorder(IdeBorderFactory.createTitledBorder("Plugins", false));
    packagesNotificationPanel = new PackagesNotificationPanel(project);
    myPanel = new PhoneGapInstalledPluginsPanel(project, packagesNotificationPanel);
    myPanel.setPreferredSize(new Dimension(400, 400));
    //panel.add(myPanel, BorderLayout.CENTER);
    //panel.add(packagesNotificationPanel.getComponent(), BorderLayout.SOUTH);
    //panel.setPreferredSize(new Dimension(400, 400));
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(FormBuilder.createFormBuilder().addComponent(myPanel).addComponent(packagesNotificationPanel.getComponent()).getPanel());
    myComponent = wrapper;
  }

  public void setupService(String path) {
    PhoneGapPackageManagementService service = null;
    if (myCurrentPath == path) {
      return;
    }

    packagesNotificationPanel.hide();
    packagesNotificationPanel.removeAllLinkHandlers();

    try {
      if (!StringUtil.isEmpty(path)) {
        new PhoneGapCommands(path, myProject.getBasePath()).version();
        service = new PhoneGapPackageManagementService(myProject, path);
      }
    }
    catch (RuntimeException e) {
      packagesNotificationPanel.showError("Please correct path to phoneGap/cordova executable", null, null);
    }

    myCurrentPath = path;
    myPanel.updatePackages(service);
  }

  public JPanel getPanel() {
    return myComponent;
  }
}
