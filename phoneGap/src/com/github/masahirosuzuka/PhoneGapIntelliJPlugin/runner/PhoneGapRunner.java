package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.PhoneGapRunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Masahiro Suzuka on 2014/04/06.
 */
public class PhoneGapRunner extends DefaultProgramRunner {
  @NotNull
  @Override
  public String getRunnerId() {
    return "PhoneGap Runner";
  }

  @Override
  public boolean canRun(@NotNull String s, @NotNull RunProfile runProfile) {
    if (!(runProfile instanceof PhoneGapRunConfiguration)) {
      return false;
    }

    return true;
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull Project project,
                                           @NotNull RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           @NotNull ExecutionEnvironment env) throws ExecutionException {

    return super.doExecute(project, state, contentToReuse, env);
  }
}
