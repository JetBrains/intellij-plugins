// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.XMap;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class DartCommandLineRunnerParameters implements Cloneable {
  private @Nullable @NlsSafe String myFilePath = null;
  private @Nullable @NlsSafe String myVMOptions = null;
  private boolean myCheckedModeOrEnableAsserts = true;
  private @Nullable @NlsSafe String myArguments = null;
  private @Nullable @NlsSafe String myWorkingDirectory = null;
  private @NotNull Map<String, String> myEnvs = new LinkedHashMap<>();
  private boolean myIncludeParentEnvs = true;

  /**
   * Get the Dart project directory for the given file (or folder) by looking for the first parent that contains a pubspec.
   * In case none can be found, the file's parent (or the folder itself) is used instead.
   */
  public static @NlsSafe String suggestDartWorkingDir(final @NotNull Project project, final @NotNull VirtualFile dartFileOrFolder) {
    final VirtualFile pubspec = PubspecYamlUtil.findPubspecYamlFile(project, dartFileOrFolder);
    if (pubspec != null) {
      final VirtualFile parent = pubspec.getParent();
      if (parent != null) {
        return parent.getPath();
      }
    }
    if (dartFileOrFolder.isDirectory()) {
      return dartFileOrFolder.getPath();
    }
    else {
      return dartFileOrFolder.getParent().getPath();
    }
  }

  public @Nullable @NlsSafe String getFilePath() {
    return myFilePath;
  }

  public void setFilePath(final @Nullable @NlsSafe String filePath) {
    myFilePath = filePath;
  }

  public @Nullable @NlsSafe String getVMOptions() {
    return myVMOptions;
  }

  public void setVMOptions(final @Nullable @NlsSafe String vmOptions) {
    myVMOptions = vmOptions;
  }

  /**
   * For Dart 2 it means 'enable asserts' flag; for Dart 1 - 'checked mode' flag
   */
  @OptionTag("checkedMode") // compatibility
  public boolean isCheckedModeOrEnableAsserts() {
    return myCheckedModeOrEnableAsserts;
  }

  /**
   * For Dart 2 it means 'enable asserts' flag; for Dart 1 - 'checked mode' flag
   */
  public void setCheckedModeOrEnableAsserts(final boolean checkedModeOrEnableAsserts) {
    myCheckedModeOrEnableAsserts = checkedModeOrEnableAsserts;
  }

  public @Nullable @NlsSafe String getArguments() {
    return myArguments;
  }

  public void setArguments(final @Nullable String arguments) {
    myArguments = arguments;
  }

  public @Nullable @NlsSafe String getWorkingDirectory() {
    return myWorkingDirectory;
  }

  public void setWorkingDirectory(final @Nullable @NlsSafe String workingDirectory) {
    myWorkingDirectory = workingDirectory;
  }

  @OptionTag
  @XMap
  public @NotNull Map<String, String> getEnvs() {
    return myEnvs;
  }

  public void setEnvs(final Map<String, String> envs) {
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

  public @NotNull @NlsSafe String computeProcessWorkingDirectory(final @NotNull Project project) {
    if (!StringUtil.isEmptyOrSpaces(myWorkingDirectory)) return myWorkingDirectory;

    try {
      return suggestDartWorkingDir(project, getDartFileOrDirectory());
    }
    catch (RuntimeConfigurationError error) {
      return "";
    }
  }

  public @NotNull VirtualFile getDartFileOrDirectory() throws RuntimeConfigurationError {
    if (StringUtil.isEmptyOrSpaces(myFilePath)) {
      throw new RuntimeConfigurationError(DartBundle.message("path.to.dart.file.not.set"));
    }

    final VirtualFile dartFile = LocalFileSystem.getInstance().findFileByPath(myFilePath);
    if (dartFile == null) {
      throw new RuntimeConfigurationError(DartBundle.message("dart.file.not.found", FileUtil.toSystemDependentName(myFilePath)));
    }

    if (!FileTypeRegistry.getInstance().isFileOfType(dartFile, DartFileType.INSTANCE) && !dartFile.isDirectory()) {
      throw new RuntimeConfigurationError(DartBundle.message("not.a.dart.file.or.directory", FileUtil.toSystemDependentName(myFilePath)));
    }

    return dartFile;
  }

  public void check(final @NotNull Project project) throws RuntimeConfigurationError {
    // check sdk
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) {
      throw new RuntimeConfigurationError(DartBundle.message("dart.sdk.is.not.configured"),
                                          () -> DartConfigurable.openDartSettings(project));
    }

    // check main dart file
    getDartFileOrDirectory();

    // check working directory
    if (!StringUtil.isEmptyOrSpaces(myWorkingDirectory)) {
      final VirtualFile workDir = LocalFileSystem.getInstance().findFileByPath(myWorkingDirectory);
      if (workDir == null || !workDir.isDirectory()) {
        throw new RuntimeConfigurationError(
          DartBundle.message("work.dir.does.not.exist", FileUtil.toSystemDependentName(myWorkingDirectory)));
      }
    }
  }

  @Override
  protected DartCommandLineRunnerParameters clone() {
    try {
      final DartCommandLineRunnerParameters clone = (DartCommandLineRunnerParameters)super.clone();
      clone.myEnvs = new LinkedHashMap<>();
      clone.myEnvs.putAll(myEnvs);
      return clone;
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
