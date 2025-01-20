// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.execution;

import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.linter.JSLinterError;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintFixInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TsLinterError extends JSLinterError {
  private final @Nullable String myPath;
  private final @Nullable TsLintFixInfo myFixInfo;
  private final boolean myIsGlobal;

  public TsLinterError(@Nullable String path,
                       int line,
                       int column,
                       int endLine,
                       int endColumn,
                       @NotNull @InspectionMessage String description,
                       @Nullable String code,
                       boolean isWarning,
                       @Nullable TsLintFixInfo fixInfo) {
    super(line, column, endLine, endColumn, description, code, isWarning ? HighlightSeverity.WARNING : HighlightSeverity.ERROR);
    myPath = path;
    myFixInfo = fixInfo;
    myIsGlobal = false;
  }

  private TsLinterError(final @NotNull @InspectionMessage String description) {
    super(1, 1, description, null);
    myPath = null;
    myFixInfo = null;
    myIsGlobal = true;
  }

  public @Nullable String getAbsoluteFilePath() {
    return myPath;
  }

  public boolean hasFix() {
    return myFixInfo != null;
  }

  public @Nullable TsLintFixInfo getFixInfo() {
    return myFixInfo;
  }

  public boolean isGlobal() {
    return myIsGlobal;
  }

  @Override
  public String toString() {
    return "TsLinterError{" +
           "myDescription='" + myDescription + '\'' +
           ", myCode='" + myCode + '\'' +
           ", myPath='" + myPath + '\'' +
           ", myEndLine=" + getEndLine() +
           ", myEndColumn=" + getEndColumn() +
           ", myFixInfo=" + myFixInfo +
           ", myIsGlobal=" + myIsGlobal +
           '}';
  }

  public static TsLinterError createGlobalError(final @NotNull @InspectionMessage String description) {
    return new TsLinterError(description);
  }
}
