// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.containers.SmartHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Set;

@State(name = "TerraformToolProjectSettings", storages = @Storage("terraform.xml"))
public class TerraformToolProjectSettings implements PersistentStateComponent<TerraformToolProjectSettings.State> {
  private final State myState = new State();

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
    myState.ignoredTemplateCandidatePaths = state.ignoredTemplateCandidatePaths;
  }

  public String getTerraformPath() {
    return myState.myTerraformPath.trim();
  }

  public String getActualTerraformPath() {
    final String path = getTerraformPath();
    return path.isEmpty() ? getDefaultTerraformPath() : path;
  }

  public void setTerraformPath(@NotNull String terraformPath) {
    myState.myTerraformPath = terraformPath.trim();
  }

  public void addIgnoredTemplateCandidate(@NotNull String filePath) {
    myState.ignoredTemplateCandidatePaths.add(filePath);
  }

  public boolean isIgnoredTemplateCandidate(@NotNull String filePath) {
    return myState.ignoredTemplateCandidatePaths.contains(filePath);
  }

  public static class State {
    public String myTerraformPath = getDefaultTerraformPath();
    public Set<String> ignoredTemplateCandidatePaths = new SmartHashSet<>();
  }

  public static String getDefaultTerraformPath() {
    String executorFileName = SystemInfo.isWindows ? "terraform.exe" : "terraform";
    File terraform = PathEnvironmentVariableUtil.findInPath(executorFileName);

    if (terraform != null && terraform.canExecute()) {
      return terraform.getAbsolutePath();
    }

    return executorFileName;
  }
}
