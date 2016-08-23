package org.jetbrains.appcode.reveal;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.cidr.execution.AppCodeRunConfiguration;
import com.jetbrains.cidr.execution.BuildDestination;
import com.jetbrains.cidr.xcode.frameworks.AppleSdk;
import com.jetbrains.cidr.xcode.model.XCBuildConfiguration;
import icons.AppcodeRevealIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;

public class RefreshRevealAction extends AnAction implements AnAction.TransparentUpdate {
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
  public void update(AnActionEvent e) {
    super.update(e);

    e.getPresentation().setIcon(ICON);

    String title = "Show in Reveal";

    XCBuildConfiguration xcBuildConfiguration = myConfiguration.getConfiguration();
    AppleSdk sdk = xcBuildConfiguration == null ? null : xcBuildConfiguration.getBaseSdk();

    File appBundle = Reveal.getDefaultRevealApplicationBundle();
    if (appBundle.exists() == false) return;

    File lib = Reveal.getRevealLib(appBundle, sdk);
    boolean compatible = Reveal.isCompatible(appBundle);

    e.getPresentation().setEnabled(lib != null
                                   && compatible

                                   && !myDisabled

                                   && myProcessHandler.isStartNotified()
                                   && !myProcessHandler.isProcessTerminating()
                                   && !myProcessHandler.isProcessTerminated()
    );

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
  public void actionPerformed(AnActionEvent e) {
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
    if (appBundle.exists() == false) return;

    try {
      Reveal.refreshReveal(appBundle, myBundleID, myDestination.getDisplayName());
    }
    catch (ExecutionException ex) {
      Reveal.LOG.info(ex);
      ExecutionUtil.handleExecutionError(myEnvironment, ex);
    }
  }

}
