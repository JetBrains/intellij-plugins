// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.appcode.reveal;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.cidr.execution.AppCodeRunConfiguration;
import com.jetbrains.cidr.execution.BuildDestination;
import com.jetbrains.cidr.execution.SimulatedBuildDestination;
import com.jetbrains.cidr.xcode.Xcode;
import com.jetbrains.cidr.xcode.frameworks.AppleSdk;
import com.jetbrains.cidr.xcode.model.XCBuildConfiguration;
import com.jetbrains.cidr.xcode.model.XCBuildSettings;
import icons.AppcodeRevealIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

public class RefreshRevealAction extends AnAction implements AnAction.TransparentUpdate, DumbAware {
  public static final Icon ICON = AppcodeRevealIcons.RunWithReveal;

  @NotNull private final AppCodeRunConfiguration myConfiguration;
  @NotNull private final ExecutionEnvironment myEnvironment;
  @NotNull private final ProcessHandler myProcessHandler;
  @NotNull private final BuildDestination myDestination;
  @NotNull private final String myBundleID;

  private boolean myDisabled = false;

  public RefreshRevealAction(@NotNull AppCodeRunConfiguration configuration,
                             @NotNull ExecutionEnvironment environment,
                             @NotNull ProcessHandler handler,
                             @NotNull BuildDestination destination,
                             @NotNull String bundleId) {
    myConfiguration = configuration;
    myEnvironment = environment;
    myProcessHandler = handler;
    myDestination = destination;
    myBundleID = bundleId;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {

    e.getPresentation().setIcon(ICON);

    String title = "Show in Reveal";

    XCBuildConfiguration xcBuildConfiguration = myConfiguration.getConfiguration();
    AppleSdk sdk = xcBuildConfiguration == null ? null : XCBuildSettings.getRawBuildSettings(xcBuildConfiguration).getBaseSdk();

    File lib = null;
    boolean compatible = false;

    File appBundle = Reveal.getDefaultRevealApplicationBundle();
    if (appBundle != null) {
      lib = Reveal.getRevealLib(appBundle, sdk);
      compatible = Reveal.isCompatible(appBundle);

      e.getPresentation().setEnabled(lib != null
              && compatible

              && !myDisabled

              && myProcessHandler.isStartNotified()
              && !myProcessHandler.isProcessTerminating()
              && !myProcessHandler.isProcessTerminated()
      );
    }

    if (lib == null) {
      title += " (Reveal library not found)";
    }
    else if (!compatible) {
      title += " (Reveal.app is not compatible, please update)";
    }
    else if (myDisabled) {
      title += " (Action is disabled until configuration relaunch)";
    }

    e.getPresentation().setText(title, false);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = getEventProject(e);
    if (project == null) return;

    RevealRunConfigurationExtension.RevealSettings settings = RevealRunConfigurationExtension.getRevealSettings(myConfiguration);
    if (!settings.autoInject) {
      int response = Messages.showYesNoDialog(project,
                                              "Reveal library was not injected.<br><br>" +
                                              "Would you like to enable automatic library injection for this run configuration?<br><br>" +
                                              "You'll need to relaunch the configuration after this change.",
                                              "Reveal", Messages.getQuestionIcon()
      );
      if (response != Messages.YES) return;

      settings.autoInject = true;
      RevealRunConfigurationExtension.setRevealSettings(myConfiguration, settings);

      myDisabled = true; // disable button until restart
      return;
    }

    File appBundle = Reveal.getDefaultRevealApplicationBundle();
    if (appBundle == null) return;

    try {
      Reveal.refreshReveal(project, appBundle, myBundleID, getDeviceName(myDestination));
    }
    catch (ExecutionException ex) {
      Reveal.LOG.info(ex);
      ExecutionUtil.handleExecutionError(myEnvironment, ex);
    }
  }

  @Nullable
  private static String getDeviceName(@NotNull BuildDestination destination) throws ExecutionException {
    if (XcodeBase.getVersion().is(8)) {
      // Xcode 8's simulators use the host computer's name
      return ExecUtil.execAndReadLine(new GeneralCommandLine("scutil", "--get", "ComputerName"));
    } else if (destination.isDevice()) {
      return destination.getDeviceSafe().getName();
    } else if (destination.isSimulator()) {
      SimulatedBuildDestination.Simulator simulator = destination.getSimulator();
      if (simulator == null) throw new ExecutionException("Simulator not specified.");

      return simulator.getName();
    }
    throw new ExecutionException("Unsupported destination: " + destination);
  }
}
