// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@State(name = "PhoneGapSettings", storages = @Storage("phonegap.xml"))
public final class PhoneGapSettings implements PersistentStateComponent<PhoneGapSettings.State> {

  public static String PHONEGAP_WORK_DIRECTORY = "js.phonegap.settings.workdir";

  public static final String ANDROID_SDK = "android";
  public static final String IOS_SIM = "ios-sim";

  public static class State {
    //don't touch for back compatibility
    public String phoneGapExecutablePath;
    public String cordovaExecutablePath;

    public String executablePath;

    public boolean isExcludePlatformFolder = true;

    public List<String> repositoriesList = new ArrayList<>();

    public State() {}

    public State(String path,List<String> repositories) {
      executablePath = path;
      repositoriesList = repositories;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof State)) return false;
      if (!StringUtil.equals(getExecutablePath(), ((State)o).getExecutablePath())) return false;
      if (repositoriesList == ((State)o).repositoriesList) return true;
      if (repositoriesList == null) return false;
      return repositoriesList.equals(((State)o).repositoriesList) && ((State)o).isExcludePlatformFolder == isExcludePlatformFolder;
    }

    @Override
    public int hashCode() {
      return String.valueOf(getExecutablePath()).hashCode();
    }

    public String getExecutablePath() {
      if (!StringUtil.isEmpty(phoneGapExecutablePath)) {
        executablePath = phoneGapExecutablePath;
        phoneGapExecutablePath = null;
      }
      else if (!StringUtil.isEmpty(cordovaExecutablePath)) {
        executablePath = cordovaExecutablePath;
        cordovaExecutablePath = null;
      }

      if (StringUtil.isEmpty(executablePath)) {
        executablePath = detectDefaultPath();
      }

      return executablePath;
    }
  }


  public static PhoneGapSettings getInstance() {
    return ApplicationManager.getApplication().getService(PhoneGapSettings.class);
  }

  private State myState = new State();

  @NotNull
  @Override
  public State getState() {
    return myState;
  }

  public @Nullable String getWorkingDirectory(@NotNull Project project) {
    return PropertiesComponent.getInstance(project).getValue(PHONEGAP_WORK_DIRECTORY);
  }

  public void setWorkingDirectory(@Nullable Project project, @Nullable String dir) {
    if (project == null) return;
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    propertiesComponent.setValue(PHONEGAP_WORK_DIRECTORY, dir);
  }

  public boolean isExcludePlatformFolder() {
    return myState.isExcludePlatformFolder;
  }


  @Nullable
  public String getExecutablePath() {
    return myState.getExecutablePath();
  }

  @Override
  public void loadState(@NotNull State state) {
    myState = state;
  }

  @Nullable
  private static String detectDefaultPath() {
    return ContainerUtil.getFirstItem(PhoneGapUtil.getDefaultExecutablePaths());
  }
}
