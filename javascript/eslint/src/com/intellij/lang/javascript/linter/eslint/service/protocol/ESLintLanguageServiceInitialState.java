package com.intellij.lang.javascript.linter.eslint.service.protocol;

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceInitialState;
import com.intellij.lang.javascript.service.protocol.LocalFilePath;
import org.jetbrains.annotations.Nullable;

public class ESLintLanguageServiceInitialState extends JSLanguageServiceInitialState {
  public LocalFilePath eslintPackagePath;
  public String linterPackageVersion;
  public LocalFilePath standardPackagePath;
  /**
   * Path to package.json declaring dependency (eslint or standard).
   * Allows requiring dependencies in proper context, e.g. useful in Yarn PnP environment.
   */
  public @Nullable LocalFilePath packageJsonPath;
  public @Nullable LocalFilePath additionalRootDirectory;
  public boolean includeSourceText;
}
