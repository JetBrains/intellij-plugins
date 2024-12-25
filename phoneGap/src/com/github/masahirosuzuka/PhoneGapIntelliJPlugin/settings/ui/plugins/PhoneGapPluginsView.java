// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.ui.plugins;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapBundle;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.ui.PhoneGapConfigurable;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.packaging.PackagesNotificationPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;


public class PhoneGapPluginsView {
  private final PhoneGapInstalledPluginsPanel myPanel;
  private final JPanel myComponent;
  private final PackagesNotificationPanel packagesNotificationPanel;
  private final Project myProject;

  public PhoneGapPluginsView(@NotNull Project project) {
    myProject = project;
    packagesNotificationPanel = new PackagesNotificationPanel();
    myPanel = new PhoneGapInstalledPluginsPanel(project, packagesNotificationPanel);
    myPanel.setPreferredSize(JBUI.size(400, 400));
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setBorder(IdeBorderFactory.createTitledBorder(PhoneGapBundle.message("phonegap.plugins"), false));
    wrapper.add(FormBuilder.createFormBuilder().addComponent(myPanel).addComponent(packagesNotificationPanel.getComponent()).getPanel());
    myComponent = wrapper;
  }

  public interface VersionCallback {
    void forVersion(@Nls String version);
  }

  public synchronized void setupService(final @Nullable String path,
                                        final @NotNull String workDir,
                                        final @NotNull PhoneGapConfigurable.RepositoryStore repositoryStore,
                                        final @NotNull VersionCallback callback) {
    packagesNotificationPanel.removeAllLinkHandlers();
    packagesNotificationPanel.hide();
    callback.forVersion("");
    if (StringUtil.isEmpty(path) || StringUtil.isEmpty(workDir)) {
      return;
    }

    ApplicationManager.getApplication().executeOnPooledThread(() -> runOnPooledThread(path, workDir, repositoryStore, callback));
  }

  private synchronized void runOnPooledThread(String path,
                                 String workDir,
                                 PhoneGapConfigurable.RepositoryStore repositoryStore,
                                 final VersionCallback callback) {
    final Ref<PhoneGapPackageManagementService> service = new Ref<>();
    final Ref<@Nls String> error = new Ref<>();
    final Ref<@Nls String> warning = new Ref<>();
    final Ref<@Nls String> version = new Ref<>();
    try {
      PhoneGapCommandLine commandLine = checkParams(error, warning, version, path, workDir);

      if (error.get() == null) {
        service.set(new PhoneGapPackageManagementService(commandLine, repositoryStore));
      }
    }
    catch (Exception e) {
      error.set(PhoneGapBundle.message("phonegap.plugins.executable.error"));
    }

    UIUtil.invokeLaterIfNeeded(() -> {
      myPanel.updatePackages(service.get());
      if (error.get() != null) {
        packagesNotificationPanel.showError(error.get(), null, null);
      }

      if (warning.get() != null) {
        packagesNotificationPanel.showWarning(warning.get());
      }

      callback.forVersion(version.get());
    });
  }

  private PhoneGapCommandLine checkParams(Ref<String> error,
                                          Ref<String> warning,
                                          Ref<@NlsSafe String> version,
                                          String path,
                                          String workDir) throws ExecutionException {

    boolean pathError = false;
    if (!new File(workDir).exists()) {
      pathError = true;
      workDir = myProject.getBasePath();
    }

    PhoneGapCommandLine commandLine = new PhoneGapCommandLine(path, workDir);
    if (!commandLine.isCorrectExecutable()) {
      error.set(PhoneGapBundle.message("phonegap.plugins.executable.error"));
      return commandLine;
    }
    version.set(commandLine.version());

    if (pathError) {
      error.set(PhoneGapBundle.message("phonegap.plugins.executable.work.path.error", commandLine.getPlatformName()));
      return commandLine;
    }

    ProcessOutput output = commandLine.pluginListRaw();
    if (!StringUtil.isEmpty(output.getStderr())) {
      error.set(PhoneGapBundle.message("phonegap.plugins.executable.work.path.error", commandLine.getPlatformName()));
      return commandLine;
    }

    if (commandLine.isOld()) {
      warning.set(PhoneGapBundle.message("phonegap.plugins.executable.version.error"));
    }

    return commandLine;
  }

  public JPanel getPanel() {
    return myComponent;
  }
}
