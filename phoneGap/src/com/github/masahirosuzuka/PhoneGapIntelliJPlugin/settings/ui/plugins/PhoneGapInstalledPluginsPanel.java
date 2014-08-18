package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.ui.plugins;

import com.intellij.openapi.project.Project;
import com.intellij.webcore.packaging.InstalledPackagesPanel;
import com.intellij.webcore.packaging.PackagesNotificationPanel;

public class PhoneGapInstalledPluginsPanel extends InstalledPackagesPanel {

  public PhoneGapInstalledPluginsPanel(Project project,
                                       PackagesNotificationPanel area) {
    super(project, area);
  }
}
