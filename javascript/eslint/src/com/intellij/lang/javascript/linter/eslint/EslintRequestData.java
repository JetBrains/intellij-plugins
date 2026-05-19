package com.intellij.lang.javascript.linter.eslint;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class EslintRequestData {
  private final VirtualFile myFileToLint;
  private final @NotNull EslintUtil.FileKind myFileKind;
  private final String myFileToLintContent;
  private final VirtualFile mySpecifiedConfigFile;
  private final Collection<VirtualFile> myPossibleConfigs;
  private final @Nullable VirtualFile myEslintIgnoreFile;
  private boolean myFlatConfigMode;

  public EslintRequestData(@NotNull VirtualFile fileToLint,
                           @NotNull EslintUtil.FileKind fileKind,
                           @NotNull String fileToLintContent,
                           @Nullable VirtualFile specifiedConfigurationFile,
                           @NotNull Collection<VirtualFile> possibleConfigs,
                           @Nullable VirtualFile eslintIgnoreFile,
                           boolean flatConfigMode) {
    myFileToLint = fileToLint;
    myFileKind = fileKind;
    myFileToLintContent = fileToLintContent;
    mySpecifiedConfigFile = specifiedConfigurationFile;
    myPossibleConfigs = possibleConfigs;
    myEslintIgnoreFile = eslintIgnoreFile;
    myFlatConfigMode = flatConfigMode;
  }

  public @NotNull VirtualFile getFileToLint() {
    return myFileToLint;
  }

  public @NotNull EslintUtil.FileKind getFileKind() {
    return myFileKind;
  }

  public @NotNull String getFileToLintContent() {
    return myFileToLintContent;
  }

  public @Nullable VirtualFile getSpecifiedConfigFile() {
    return mySpecifiedConfigFile;
  }

  public @NotNull Collection<VirtualFile> getPossibleConfigs() {
    return myPossibleConfigs;
  }

  public @Nullable VirtualFile getEslintIgnoreFile() {
    return myEslintIgnoreFile;
  }

  public boolean isFlatConfigMode() {
    return myFlatConfigMode;
  }
}
