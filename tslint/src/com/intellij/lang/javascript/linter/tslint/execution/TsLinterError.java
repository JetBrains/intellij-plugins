package com.intellij.lang.javascript.linter.tslint.execution;

import com.intellij.lang.javascript.integration.JSAnnotationRangeError;
import com.intellij.lang.javascript.linter.JSLinterError;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintFixInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public final class TsLinterError extends JSLinterError implements JSAnnotationRangeError {

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
                       @NotNull String description,
                       @Nullable String code,
                       @Nullable TsLintFixInfo fixInfo) {
    super(line, column, description, code);
    myPath = path;
    myEndLine = endLine;
    myEndColumn = endColumn;
    myFixInfo = fixInfo;
    myIsGlobal = false;
  }

  public TsLinterError(final @NotNull String description) {
    super(1, 1, description, null);
    myPath = null;
    myEndLine = 1;
    myEndColumn = 1;
    myFixInfo = null;
    myIsGlobal = true;
  }

  @Override
  public int getEndLine() {
    return myEndLine;
  }

  @Override
  public int getEndColumn() {
    return myEndColumn;
  }

  @Nullable
  @Override
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
}
