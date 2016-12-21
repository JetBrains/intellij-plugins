package com.intellij.lang.javascript.linter.tslint;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.linter.JSLinterError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public class TsLinterError extends JSLinterError {
  private final int myEndLine;
  private final int myEndColumn;

  public TsLinterError(int line, int column, @NotNull String description, @Nullable String code, int endLine, int endColumn) {
    super(line, column, description, code);
    myEndLine = endLine;
    myEndColumn = endColumn;
  }

  public TsLinterError(int line,
                       int column,
                       @NotNull String description,
                       @Nullable String code,
                       @Nullable HighlightSeverity severity, int endLine, int endColumn) {
    super(line, column, description, code, severity);
    myEndLine = endLine;
    myEndColumn = endColumn;
  }

  public int getEndLine() {
    return myEndLine;
  }

  public int getEndColumn() {
    return myEndColumn;
  }
}
