// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.runner;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.mxunit.CfmlUnitRunConfiguration;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunnerKt;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

final class CfmlRunner extends GenericProgramRunner {
  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) {
    final RunProfile runProfileRaw = env.getRunProfile();
    if (runProfileRaw instanceof CfmlRunConfiguration runProfile) {
      FileDocumentManager.getInstance().saveAllDocuments();

      //check if CfmlRunConfiguration generated from default server http://localhost:8500/
      if (runProfile.isFromDefaultHost()) {
        showDefaultRunConfigWarn(env, runProfile);
      }
      else {
        final CfmlRunnerParameters params = runProfile.getRunnerParameters();
        BrowserLauncher.getInstance().browse(params.getUrl(), params.getCustomBrowser(), env.getProject());
      }
      return null;
    }
    else {
      return DefaultProgramRunnerKt.executeState(state, env, this);
    }
  }

  @Override
  public @NotNull String getRunnerId() {
    return "CfmlRunner";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) &&
           (profile instanceof CfmlRunConfiguration || profile instanceof CfmlUnitRunConfiguration);
  }

  private static void showDefaultRunConfigWarn(@NotNull ExecutionEnvironment env, CfmlRunConfiguration runProfile) {
    DialogBuilder db = new DialogBuilder(env.getProject());
    JLabel info = new JLabel(CfmlBundle.message("cfml.runconfig.dialog.template.label"));
    info.setMaximumSize(new Dimension(400, 500));
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.LINE_AXIS));
    JLabel webPathLabel = new JLabel(CfmlBundle.message("cfml.runconfig.editor.server.url"));
    JTextField webPathField = new JTextField(runProfile.getRunnerParameters().getUrl());
    centerPanel.add(webPathLabel);
    centerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    centerPanel.add(webPathField);

    info.setIcon(UIUtil.getWarningIcon());
    db.setNorthPanel(info);
    db.setCenterPanel(centerPanel);
    db.setPreferredFocusComponent(info);
    db.setTitle(CfmlBundle.message("cfml.runconfig.dialog.template.title"));
    db.addOkAction().setText(CfmlBundle.message("cfml.runconfig.dialog.template.button.run"));
    db.addCancelAction().setText(CfmlBundle.message("cfml.runconfig.dialog.template.button.cancel"));
    db.setOkOperation(() -> {
      runProfile.setFromDefaultHost(false);
      final CfmlRunnerParameters params = runProfile.getRunnerParameters();
      RunnerAndConfigurationSettings configurationTemplate =
        RunManager.getInstance(env.getProject()).getConfigurationTemplate(CfmlRunConfigurationType.getInstance());
      ((CfmlRunConfiguration)configurationTemplate.getConfiguration()).getRunnerParameters().setUrl(webPathField.getText());
      BrowserLauncher.getInstance().browse(params.getUrl(), params.getCustomBrowser(), env.getProject());
      db.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
    });
    db.show();
  }

}