package com.intellij.lang.javascript.linter.tslint.execution;

import com.intellij.lang.javascript.integration.JSAnnotationRangeError;
import com.intellij.lang.javascript.linter.JSLinterError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public class TsLinterError extends JSLinterError implements JSAnnotationRangeError {
  private final String myPath;
  private final int myEndLine;
  private final int myEndColumn;

  public TsLinterError(String path, int line, int column, @NotNull String description, @Nullable String code, int endLine, int endColumn) {
    super(line, column, description, code);
    myPath = path;
    myEndLine = endLine;
    myEndColumn = endColumn;
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
}
