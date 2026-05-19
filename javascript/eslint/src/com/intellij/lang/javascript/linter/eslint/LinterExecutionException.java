package com.intellij.lang.javascript.linter.eslint;

import com.intellij.execution.ExecutionException;
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation;
import org.jetbrains.annotations.NotNull;

public class LinterExecutionException extends ExecutionException {
  private final @NotNull JSLinterFileLevelAnnotation myAnnotation;

  public LinterExecutionException(@NotNull JSLinterFileLevelAnnotation annotation) {
    super(annotation.getMessage());
    myAnnotation = annotation;
  }

  public @NotNull JSLinterFileLevelAnnotation getAnnotation() {
    return myAnnotation;
  }
}