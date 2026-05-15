package com.intellij.lang.javascript.linter.jshint.config;

import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.lang.javascript.linter.jshint.JSHintOptionsState;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class JSHintConfigLookupResult {
  private final VirtualFile myConfigFile;
  private final @InspectionMessage String myErrorMessage;
  private final JSHintOptionsState myOptionsState;

  public JSHintConfigLookupResult(@NotNull VirtualFile configFile,
                                  @Nullable @InspectionMessage String errorMessage,
                                  @Nullable JSHintOptionsState optionsState) {
    myConfigFile = configFile;
    myErrorMessage = errorMessage;
    myOptionsState = optionsState;
  }

  public @NotNull VirtualFile getConfigFile() {
    return myConfigFile;
  }

  public @Nullable @InspectionMessage String getErrorMessage() {
    return myErrorMessage;
  }

  public @Nullable JSHintOptionsState getOptionsState() {
    return myOptionsState;
  }

  public static @NotNull JSHintConfigLookupResult createSuccessfulResult(@NotNull VirtualFile configFile,
                                                                         @NotNull JSHintOptionsState optionsState) {
    return new JSHintConfigLookupResult(configFile, null, optionsState);
  }

  public static JSHintConfigLookupResult createErrorResult(@NotNull VirtualFile configFile,
                                                           @NotNull @InspectionMessage String parseErrorMessage) {
    return new JSHintConfigLookupResult(configFile, parseErrorMessage, null);
  }
}
