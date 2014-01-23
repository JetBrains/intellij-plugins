package org.jetbrains.jps.osmorc.build;

import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author michael.golubev
 */
public class OsmorcBuildException extends Exception {

  private final String mySourcePath;
  private boolean myWarningNotError;

  public OsmorcBuildException(String message, @Nullable Throwable cause, @Nullable String sourcePath) {
    super(message, cause);
    mySourcePath = sourcePath;
    myWarningNotError = false;
  }

  public OsmorcBuildException(String message, @Nullable Throwable cause, File source) {
    this(message, cause, source.getAbsolutePath());
  }

  public OsmorcBuildException(String message) {
    this(message, null, (String)null);
  }

  public OsmorcBuildException(String message, String sourcePath) {
    this(message, null, sourcePath);
  }

  public OsmorcBuildException(String message, File source) {
    this(message, null, source);
  }

  public OsmorcBuildException(String message, Throwable cause) {
    this(message, cause, (String)null);
  }

  public OsmorcBuildException setWarning() {
    myWarningNotError = true;
    return this;
  }

  public boolean isWarningNotError() {
    return myWarningNotError;
  }

  public String getSourcePath() {
    return mySourcePath;
  }
}
