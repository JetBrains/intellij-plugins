package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * User: ksafonov
 */
public class NonStructuralModifiableCompilerOptions implements CompilerOptions {
  private final CompilerOptionsImpl myOriginal;

  NonStructuralModifiableCompilerOptions(final CompilerOptionsImpl compilerOptions) {
    myOriginal = compilerOptions;
  }

  public void setAdditionalConfigFilePath(@NotNull final String path) {
    // TODO is this really non-structural? should we restart highlighting?
    myOriginal.setAdditionalConfigFilePath(path);
  }

  @NotNull
  @Override
  public ResourceFilesMode getResourceFilesMode() {
    return myOriginal.getResourceFilesMode();
  }

  @Override
  @Nullable
  public String getOption(@NotNull final String name) {
    return myOriginal.getOption(name);
  }

  @Override
  public Map<String, String> getAllOptions() {
    return myOriginal.getAllOptions();
  }

  @Override
  public Collection<String> getFilesToIncludeInSWC() {
    return myOriginal.getFilesToIncludeInSWC();
  }

  @Override
  @NotNull
  public String getAdditionalConfigFilePath() {
    return myOriginal.getAdditionalConfigFilePath();
  }

  @Override
  @NotNull
  public String getAdditionalOptions() {
    return myOriginal.getAdditionalOptions();
  }

}
