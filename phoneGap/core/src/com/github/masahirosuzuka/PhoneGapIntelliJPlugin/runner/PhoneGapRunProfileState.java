// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * PhoneGapRunProfileState.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/06.
 */
public class PhoneGapRunProfileState extends CommandLineState {

  @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
  private final ExecutionEnvironment env;
  private final PhoneGapRunConfiguration phoneGapRunConfiguration;
  private final Project myProject;

  public PhoneGapRunProfileState(Project project,
                                 @NotNull ExecutionEnvironment env,
                                 PhoneGapRunConfiguration phoneGapRunConfiguration) {
    super(env);
    this.myProject = project;
    this.env = env;
    this.phoneGapRunConfiguration = phoneGapRunConfiguration;
  }

  @Override
  protected @NotNull ProcessHandler startProcess() throws ExecutionException {
    PhoneGapCommandLine line = phoneGapRunConfiguration.getCommandLine();
    String command = phoneGapRunConfiguration.getCommand();
    assert command != null;
    String platform = phoneGapRunConfiguration.getPlatform();
    assert platform != null;
    return line.runCommand(command, platform, phoneGapRunConfiguration.hasTarget() ? phoneGapRunConfiguration.getTarget() : null, phoneGapRunConfiguration.getExtraArgs());
  }
}
