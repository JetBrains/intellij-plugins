package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class DartCommandLineRunnerParameters implements Cloneable {
  private @Nullable String myFilePath = null;
  private @Nullable String myVMOptions = null;
  private boolean myCheckedMode = true;
  private @Nullable String myArguments = null;
  private @Nullable String myWorkingDirectory = null;
  private @NotNull Map<String, String> myEnvs = new LinkedHashMap<String, String>();
  private boolean myIncludeParentEnvs = true;

  @Nullable
  public String getFilePath() {
    return myFilePath;
  }

  public void setFilePath(final @Nullable String filePath) {
    myFilePath = filePath;
  }

  @Nullable
  public String getVMOptions() {
    return myVMOptions;
  }

  public void setVMOptions(final @Nullable String vmOptions) {
    myVMOptions = vmOptions;
  }

  public boolean isCheckedMode() {
    return myCheckedMode;
  }

  public void setCheckedMode(final boolean checkedMode) {
    myCheckedMode = checkedMode;
  }

  @Nullable
  public String getArguments() {
    return myArguments;
  }

  public void setArguments(final @Nullable String arguments) {
    myArguments = arguments;
  }

  @Nullable
  public String getWorkingDirectory() {
    return myWorkingDirectory;
  }

  public void setWorkingDirectory(final @Nullable String workingDirectory) {
    myWorkingDirectory = workingDirectory;
  }

  @NotNull
  @MapAnnotation(surroundWithTag = false, surroundKeyWithTag = false, surroundValueWithTag = false)
  public Map<String, String> getEnvs() {
    return myEnvs;
  }

  public void setEnvs(@SuppressWarnings("NullableProblems") final Map<String, String> envs) {
    if (envs != null) { // null comes from old projects or if storage corrupted
      myEnvs = envs;
    }
  }

  public boolean isIncludeParentEnvs() {
    return myIncludeParentEnvs;
  }

  public void setIncludeParentEnvs(final boolean includeParentEnvs) {
    myIncludeParentEnvs = includeParentEnvs;
  }

  public String computeProcessWorkingDirectory() throws RuntimeConfigurationError {
    return StringUtil.isEmptyOrSpaces(getWorkingDirectory()) ? getDartFile().getParent().getPath() : getWorkingDirectory();
  }

  @NotNull
  public VirtualFile getDartFile() throws RuntimeConfigurationError {
    final VirtualFile dartFile = getDartFileOrDirectory();
    if (dartFile.isDirectory()) {
      throw new RuntimeConfigurationError(DartBundle.message("dart.file.not.found", FileUtil.toSystemDependentName(getFilePath())));
    }
    return dartFile;
  }

  @NotNull
  public VirtualFile getDartFileOrDirectory() throws RuntimeConfigurationError {
    final String filePath = getFilePath();
    if (StringUtil.isEmptyOrSpaces(filePath)) {
      throw new RuntimeConfigurationError(DartBundle.message("path.to.dart.file.not.set"));
    }

    final VirtualFile dartFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (dartFile == null) {
      throw new RuntimeConfigurationError(DartBundle.message("dart.file.not.found", FileUtil.toSystemDependentName(filePath)));
    }

    if (dartFile.getFileType() != DartFileType.INSTANCE && !dartFile.isDirectory()) {
      throw new RuntimeConfigurationError(DartBundle.message("not.a.dart.file.or.directory", FileUtil.toSystemDependentName(filePath)));
    }

    return dartFile;
  }

  public void check(final @NotNull Project project) throws RuntimeConfigurationError {
    // check sdk
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) {
      throw new RuntimeConfigurationError(DartBundle.message("dart.sdk.is.not.configured"), new Runnable() {
        public void run() {
          DartConfigurable.openDartSettings(project);
        }
      });
    }

    // check main dart file
    getDartFileOrDirectory();

    // check working directory
    final String workDirPath = getWorkingDirectory();
    if (!StringUtil.isEmptyOrSpaces(workDirPath)) {
      final VirtualFile workDir = LocalFileSystem.getInstance().findFileByPath(workDirPath);
      if (workDir == null || !workDir.isDirectory()) {
        throw new RuntimeConfigurationError(DartBundle.message("work.dir.does.not.exist", FileUtil.toSystemDependentName(workDirPath)));
      }
    }
  }

  @Override
  protected DartCommandLineRunnerParameters clone() {
    try {
      final DartCommandLineRunnerParameters clone = (DartCommandLineRunnerParameters)super.clone();
      clone.myEnvs = new LinkedHashMap<String, String>();
      clone.myEnvs.putAll(myEnvs);
      return clone;
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
