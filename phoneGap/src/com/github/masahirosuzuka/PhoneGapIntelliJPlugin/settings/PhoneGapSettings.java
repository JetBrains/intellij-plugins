package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * PhoneGapSettings.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/12.
 */
@State(
  name = "PhoneGapSettings",
  storages = {@Storage(
    file = StoragePathMacros.APP_CONFIG + "/phonegap.xml")})
public final class PhoneGapSettings implements PersistentStateComponent<PhoneGapSettings.State> {

  public static String PHONEGAP_WORK_DIRECTORY = "js.phonegap.settings.workdir";

  // External tools PATH
  public static String NODEJS_PATH = "/usr/local/bin/node";
  public static String ANDROID_SDK = "android";
  public static String IOS_SIM = "ios-sim";

  public static class State {
    //don't touch for back compatibility
    public String phoneGapExecutablePath;
    public String cordovaExecutablePath;

    public String executablePath;

    public boolean isExcludePlatformFolder = true;

    public List<String> repositoriesList = ContainerUtil.newArrayList();

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
    return ServiceManager.getService(PhoneGapSettings.class);
  }

  private State myState = new State();

  @NotNull
  @Override
  public State getState() {
    return myState;
  }

  @NotNull
  public String getWorkingDirectory(@Nullable Project project) {
    if (project == null) return "";
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(project);
    String value = propertiesComponent.getValue(PHONEGAP_WORK_DIRECTORY);
    if (value != null) return value;

    String item = ContainerUtil.getFirstItem(PhoneGapUtil.getDefaultWorkingDirectory(project));
    return item == null ? "" : item;
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
  public void loadState(State state) {
    myState = state;
  }

  @Nullable
  private static String detectDefaultPath() {
    return ContainerUtil.getFirstItem(PhoneGapUtil.getDefaultExecutablePaths());
  }
}
