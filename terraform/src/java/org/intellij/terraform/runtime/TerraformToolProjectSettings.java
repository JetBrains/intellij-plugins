// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@State(
    name = "TerraformToolProjectSettings",
    storages = @Storage("terraform.xml")
)
public class TerraformToolProjectSettings implements PersistentStateComponent<TerraformToolProjectSettings.State> {
  private State myState = new State();

  public static TerraformToolProjectSettings getInstance(@NotNull final Project project) {
    return project.getService(TerraformToolProjectSettings.class);
  }

  @Nullable
  @Override
  public State getState() {
    return myState;
  }

  @Override
  public void loadState(@NotNull State state) {
    myState.myTerraformPath = state.myTerraformPath;
  }

  public String getTerraformPath() {
    return myState.myTerraformPath;
  }

  public void setTerraformPath(String terraformPath) {
    myState.myTerraformPath = terraformPath;
  }

  public static class State {
    public String myTerraformPath = getDefaultTerraformPath();
  }

  public static String getDefaultTerraformPath() {
    File terraform = PathEnvironmentVariableUtil.findInPath("terraform");

    if (terraform != null && terraform.canExecute()) {
      return terraform.getAbsolutePath();
    }

    if (SystemInfo.isUnix) {
      return "terraform";
    } else {
      return "terraform.exe";
    }
  }

}
