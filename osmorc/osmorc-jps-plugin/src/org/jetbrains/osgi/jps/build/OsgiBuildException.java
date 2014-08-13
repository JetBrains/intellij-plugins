package org.jetbrains.osgi.jps.build;

import org.jetbrains.annotations.Nullable;

public class OsgiBuildException extends Exception {
  private final String mySourcePath;

  public OsgiBuildException(String message) {
    this(message, null, null);
  }

  public OsgiBuildException(String message, @Nullable Throwable cause, @Nullable String sourcePath) {
    super(message, cause);
    mySourcePath = sourcePath;
  }

  @Nullable
  public String getSourcePath() {
    return mySourcePath;
  }
}
