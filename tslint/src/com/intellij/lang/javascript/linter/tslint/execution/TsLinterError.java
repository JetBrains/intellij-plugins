package com.intellij.lang.javascript.linter.tslint.execution;

import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.linter.JSLinterError;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintFixInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TsLinterError extends JSLinterError {
  @Nullable
  private final String myPath;
  private final int myEndLine;
  private final int myEndColumn;

  @Nullable
  private final TsLintFixInfo myFixInfo;
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
    super(line, column, description, code, isWarning ? HighlightSeverity.WARNING : HighlightSeverity.ERROR);
    myPath = path;
    myEndLine = endLine;
    myEndColumn = endColumn;
    myFixInfo = fixInfo;
    myIsGlobal = false;
  }

  private TsLinterError(final @NotNull @InspectionMessage String description) {
    super(1, 1, description, null);
    myPath = null;
    myEndLine = 1;
    myEndColumn = 1;
    myFixInfo = null;
    myIsGlobal = true;
  }

  public int getEndLine() {
    return myEndLine;
  }

  public int getEndColumn() {
    return myEndColumn;
  }

  @Nullable
  public String getAbsoluteFilePath() {
    return myPath;
  }

  public boolean hasFix() {
    return myFixInfo != null;
  }

  @Nullable
  public TsLintFixInfo getFixInfo() {
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
           ", myEndLine=" + myEndLine +
           ", myEndColumn=" + myEndColumn +
           ", myFixInfo=" + myFixInfo +
           ", myIsGlobal=" + myIsGlobal +
           '}';
  }

  public static TsLinterError createGlobalError(final @NotNull @InspectionMessage String description) {
    return new TsLinterError(description);
  }
}
