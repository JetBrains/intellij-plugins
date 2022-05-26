package com.jetbrains.plugins.meteor.settings;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@State(name = "MeteorSettings", storages = @Storage("meteorSettings.xml"))
public class MeteorSettings implements PersistentStateComponent<MeteorSettings> {
  public static final String METEOR_SIMPLE_NAME = "meteor";
  private boolean isExcludeMeteorLocalFolder = true;
  private String executablePath;
  private boolean isWeakSearch = true;
  private boolean isStartOnce = false;
  private boolean isAutoImport = true;

  public boolean isAutoImport() {
    return isAutoImport;
  }

  public boolean isWeakSearch() {
    return isWeakSearch;
  }

  public void setIsWeakSearch(boolean isWeakSearch) {
    this.isWeakSearch = isWeakSearch;
  }

  public void setAutoImport(boolean autoImport) {
    isAutoImport = autoImport;
  }

  public static MeteorSettings getInstance() {
    return ApplicationManager.getApplication().getService(MeteorSettings.class);
  }

  @Nullable
  @Override
  public MeteorSettings getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull MeteorSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public boolean isExcludeMeteorLocalFolder() {
    return isExcludeMeteorLocalFolder;
  }

  public void setExcludeMeteorLocalFolder(boolean isExcludeMeteorLocalFolder) {
    this.isExcludeMeteorLocalFolder = isExcludeMeteorLocalFolder;
  }

  public String getExecutablePath() {
    return StringUtil.isEmpty(executablePath) ? detectMeteorExecutablePath() : executablePath;
  }

  public void setExecutablePath(String executablePath) {
    this.executablePath = executablePath;
  }

  public boolean isStartOnce() {
    return isStartOnce;
  }

  public void setStartOnce(boolean isStartOnce) {
    this.isStartOnce = isStartOnce;
  }

  @SuppressWarnings("ConstantConditions")
  @NotNull
  public static String detectMeteorExecutablePath() {
    List<String> strings = detectMeteorExecutablePaths();
    return strings.isEmpty() ? "" : ContainerUtil.getFirstItem(strings);
  }

  @NotNull
  public static List<String> detectMeteorExecutablePaths() {
    List<File> result = new ArrayList<>();
    if (SystemInfo.isWindows) {
      ContainerUtil.addIfNotNull(result, PathEnvironmentVariableUtil.findInPath("meteor.bat"));
      ContainerUtil.addIfNotNull(result, PathEnvironmentVariableUtil.findInPath("meteor.cmd"));
      ContainerUtil.addIfNotNull(result, PathEnvironmentVariableUtil.findInPath("meteor.exe"));
    }
    else {
      ContainerUtil.addIfNotNull(result, PathEnvironmentVariableUtil.findInPath(METEOR_SIMPLE_NAME));
    }

    return ContainerUtil.map(result, file -> file.getAbsolutePath());
  }
}
