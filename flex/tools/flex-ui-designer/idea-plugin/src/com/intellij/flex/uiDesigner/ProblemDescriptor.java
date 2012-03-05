package com.intellij.flex.uiDesigner;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProblemDescriptor {
  private final String message;
  private final int lineNumber;
  private final VirtualFile file;

  public ProblemDescriptor(@NotNull String message, @Nullable VirtualFile file, int line) {
    this.message = message;
    this.file = file;
    this.lineNumber = line;
  }

  public ProblemDescriptor(@NotNull String message, @NotNull VirtualFile mxmlFile) {
    this(message, mxmlFile, -1);
  }

  @Nullable
  public VirtualFile getFile() {
    return file;
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof ProblemDescriptor) {
      ProblemDescriptor a = (ProblemDescriptor)obj;
      return a.getMessage().equals(getMessage()) && a.getLineNumber() == getLineNumber() && a.getFile() == getFile();
    }

    return false;
  }

  @Override
  public int hashCode() {
    int hashCode = message.hashCode();
    if (hasLineNumber()) {
      hashCode += lineNumber;
    }
    if (file != null) {
      hashCode += file.hashCode();
    }
    return hashCode;
  }
}
