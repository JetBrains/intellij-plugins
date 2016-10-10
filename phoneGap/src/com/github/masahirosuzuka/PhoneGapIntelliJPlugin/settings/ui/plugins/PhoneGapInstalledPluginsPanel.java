package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.ui.plugins;

import com.intellij.openapi.project.Project;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.InstalledPackagesPanel;
import com.intellij.webcore.packaging.PackagesNotificationPanel;
import org.jetbrains.annotations.NotNull;

public class PhoneGapInstalledPluginsPanel extends InstalledPackagesPanel {

  public PhoneGapInstalledPluginsPanel(Project project,
                                       PackagesNotificationPanel area) {
    super(project, area);
  }

  protected boolean installEnabled() {
    return false;
  }

  protected boolean canInstallPackage(@NotNull final InstalledPackage pyPackage) {
    return false;
  }

}
