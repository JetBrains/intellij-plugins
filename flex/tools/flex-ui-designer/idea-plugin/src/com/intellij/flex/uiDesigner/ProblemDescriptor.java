package com.intellij.flex.uiDesigner;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProblemDescriptor {
  private final String message;
  private final int lineNumber;

  private final VirtualFile virtualFile;

  public ProblemDescriptor(@NotNull String message, @Nullable VirtualFile virtualFile, int line) {
    this.message = message;
    this.virtualFile = virtualFile;
    this.lineNumber = line;
  }

  public ProblemDescriptor(@NotNull String message, @NotNull VirtualFile mxmlFile) {
    this(message, mxmlFile, -1);
  }

  @Nullable
  public VirtualFile getFile() {
    return virtualFile;
  }

  @NotNull
  public String getMessage() {
    return message;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public boolean hasLineNumber() {
    return lineNumber != -1;
  }
}
